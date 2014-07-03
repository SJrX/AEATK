package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.ParameterException;

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aeatk.misc.string.SplitQuotedString;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.AbstractSyncTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc.mechanism.ReverseTCPMechanism;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc.mechanism.TCPMechanism;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc.mechanism.UDPMechanism;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.exceptions.TargetAlgorithmAbortException;

/***
 * IPC Based Target Algorithm Evaluator
 * 
 * Uses various IPC mechanisms to allow another process to answer our requests
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
@ThreadSafe
public class IPCTargetAlgorithmEvaluator extends AbstractSyncTargetAlgorithmEvaluator 
{
	
	private static final Logger log = LoggerFactory.getLogger(IPCTargetAlgorithmEvaluator.class);
	
	private final IPCTargetAlgorithmEvaluatorOptions options;
			
	private final ServerSocket serverSocket;
	
	private final Process proc;;
	
	private final ExecutorService executors = Executors.newCachedThreadPool(new SequentiallyNamedThreadFactory("IPC Target Algorithm Evaluator Script Watcher", true));
	
	public IPCTargetAlgorithmEvaluator (IPCTargetAlgorithmEvaluatorOptions options) {
		super();
		
		this.options = options;
		int localPort = 0;
	
		switch(this.options.ipcMechanism)
		{
			case TCP:
				verifyRemoteAddress();
				serverSocket = null;
				
				log.info("Target Algorithm Evaluator making TCP connections to {}:{}.",options.remoteHost,options.remotePort);
				
				break;
			case UDP:

				verifyRemoteAddress();
				serverSocket = null;
				
				log.info("Target Algorithm Evaluator making UDP connections to {}:{}.",options.remoteHost,options.remotePort);
				
				break;
			case REVERSE_TCP:
				try {
					serverSocket = new ServerSocket(this.options.localPort);
					
					localPort = serverSocket.getLocalPort();
					log.info("IPC Target Algorithm Evaluator is listening on port {}", localPort);
				} catch (IOException e) {
					throw new IllegalStateException("Couldn't start server on local port", e);
				}
				
				break;
				
			default:
			
				throw new ParameterException("Not implemented:" + this.options.ipcMechanism);
		}
		
		
		if(options.execScript != null && options.execScript.trim().length() > 0)
		{
			
			String[] args = SplitQuotedString.splitQuotedString(options.execScript +" " + localPort);
			
			ProcessBuilder pb = new ProcessBuilder();
			pb.redirectErrorStream(true);
			pb.command(args);
			Process proc;
			try {
				proc = pb.start();
			} catch (IOException e) {
				//log.error("Couldn't start client script: {}", options.execScript);
				log.debug("Couldn't start process exec script:",e);
				throw new ParameterException("Could not start IPC Target Algorithm Evaluator execution script, please check your arguments and try again, error was: " + e.getMessage() );
			}
			IPCStreamReader sg = new IPCStreamReader(proc.getInputStream(), options.execScriptOutput);
			
			this.executors.execute(sg);
			
			this.proc = proc;
			
		} else
		{
			proc = null;
			
		}
		
	}


	/**
	 * @throws ParameterException
	 */
	private void verifyRemoteAddress() throws ParameterException {
		if(this.options.remotePort <= 0 || this.options.remotePort > 65535)
		{
			throw new ParameterException("To use the " + this.options.ipcMechanism + " mechanism you must specify a port in [1,65535]");
		}
		
		if(this.options.remoteHost == null)
		{
			throw new ParameterException("You must specify a remote host to use the " + this.options.ipcMechanism );
		}
		
		try {
			 InetAddress.getByName(this.options.remoteHost);

		} catch(UnknownHostException e)
		{
			throw new ParameterException("Could resolve hostname: " + this.options.remoteHost);
		}
	}


	@Override
	public boolean isRunFinal() {
		return false;
	}

	@Override
	public boolean areRunsPersisted() {
		return options.persistent;
	}

	

	@Override
	public boolean areRunsObservable() {
		return false;
	}

	private final AtomicBoolean isShutdown = new AtomicBoolean(false);
	
	private final AtomicBoolean subProcessShutdownDetected = new AtomicBoolean(false);
	@Override
	protected void subtypeShutdown() {
		
		isShutdown.set(true);
		if(serverSocket != null)
		{
		    try {
                serverSocket.close();
            } catch (IOException e) {
                log.error("Could not close server socket.",e);
            }
		}
		
		this.executors.shutdownNow();
		
		if(this.proc != null)
		{
			proc.destroy();
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}	
		}
		
	
		
	}

	@Override
	public List<AlgorithmRunResult> evaluateRun(List<AlgorithmRunConfiguration> runConfigs,
			TargetAlgorithmEvaluatorRunObserver runStatusObserver) {
		
		List<AlgorithmRunResult> completedRuns = new ArrayList<AlgorithmRunResult>();
		
		if(subProcessShutdownDetected.get()) 
		{
			log.warn("Exec script has terminated, it is unclear if this run will ever be completed");
		}
		 
		
		for(AlgorithmRunConfiguration rc : runConfigs)
		{

			switch(this.options.ipcMechanism)
			{
			case UDP:
				UDPMechanism udp = new UDPMechanism(this.options.encodingMechanism.getEncoder());
				AlgorithmRunResult run = udp.evaluateRun(rc, this.options.remotePort, this.options.remoteHost, this.options.udpPacketSize);
				completedRuns.add(run);
				break;
			case TCP:
				TCPMechanism tcp = new TCPMechanism(this.options.encodingMechanism.getEncoder());
				 run = tcp.evaluateRun(rc, this.options.remoteHost, this.options.remotePort);
				completedRuns.add(run);
				break;
			case REVERSE_TCP:
				while(true)
				{
					ReverseTCPMechanism rtcp = new ReverseTCPMechanism(this.options.encodingMechanism.getEncoder());
					Socket socket;
					try {
						socket = serverSocket.accept();
						run = rtcp.evaluateRun(socket, rc);
						completedRuns.add(run);
						break;
					} catch (IOException e) {
						log.error("Error occured during IPC call, trying connection again in 10 seconds",e);
						
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							Thread.currentThread().interrupt();
							throw new TargetAlgorithmAbortException(e1);
						}
						
					}
					
					
				}
			
				break;
			default: 
				throw new IllegalStateException("Not sure what this was");
			}
		}
		
		return completedRuns;
	}

	private class IPCStreamReader implements Runnable {
	    InputStream is;
		private boolean output;

	    private IPCStreamReader(InputStream is, boolean output) {
	        this.is = is;
	        this.output = output;
	    }

	    @Override
	    public void run() {
	    	  Queue<String> last10Lines = new LinkedList<String>();
	        try {
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String line = null;
	          
	            while ((line = br.readLine()) != null)
	            {	
		            if(output)
		            {
		            	
		    			
		            	log.info("IPC-TAE Client> " + line);
		            } else
		            {
		            	last10Lines.add(line);
		    			if(last10Lines.size() > 10)
		    			{
		    				last10Lines.poll();
		    			}
		            }
		            
	            }
	              
	        }
	        catch (IOException ioe) {
	        	//This doesn't matter.
	        }
	        if(!isShutdown.get())
            {
            	try {
					proc.waitFor();
					subProcessShutdownDetected.set(true);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
            	
            	try 
            	{
            		int retValue = proc.exitValue();
            		
            		if(retValue > 0)
            		{
            			log.warn("Calling script shutdown with non-zero exit ({}) code, last 10 lines were:", retValue);
            			for(String aLine : last10Lines)
            			{
            				log.warn("> " + aLine);
            			}
            		}
            	
            	} catch(IllegalThreadStateException e)
            	{
            		//Still running
            	}
            	
            	
            }
            
	        
	    }
	}
	
}
