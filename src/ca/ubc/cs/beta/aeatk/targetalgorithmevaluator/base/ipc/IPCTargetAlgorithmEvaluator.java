package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.ParameterException;

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
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

	@Override
	protected void subtypeShutdown() {
		if(serverSocket != null)
		{
		    try {
                serverSocket.close();
            } catch (IOException e) {
                log.error("Could not close server socket.",e);
            }
		}
	}

	@Override
	public List<AlgorithmRunResult> evaluateRun(List<AlgorithmRunConfiguration> runConfigs,
			TargetAlgorithmEvaluatorRunObserver runStatusObserver) {
		
		List<AlgorithmRunResult> completedRuns = new ArrayList<AlgorithmRunResult>();
		
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

}
