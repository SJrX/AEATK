package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.ipc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.ParameterException;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractSyncTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.ipc.mechanism.TCPMechanism;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.ipc.mechanism.UDPMechanism;

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
			
	public IPCTargetAlgorithmEvaluator (IPCTargetAlgorithmEvaluatorOptions options) {
		super();
		

	
		
		this.options = options;
		
		switch(this.options.ipcMechanism)
		{
			case TCP:
				verifyRemoteAddress();
				break;
			case UDP:

				verifyRemoteAddress();
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
			InetAddress IPAddress = InetAddress.getByName(this.options.remoteHost);

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
		
	}

	@Override
	public synchronized List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs,
			TargetAlgorithmEvaluatorRunObserver runStatusObserver) {
		
		List<AlgorithmRun> completedRuns = new ArrayList<AlgorithmRun>();
		
		for(RunConfig rc : runConfigs)
		{

			switch(this.options.ipcMechanism)
			{
			case UDP:
				UDPMechanism udp = new UDPMechanism();
				AlgorithmRun run = udp.evaluateRun(rc, this.options.remotePort, this.options.remoteHost, this.options.udpPacketSize);
				completedRuns.add(run);
				break;
			case TCP:
				TCPMechanism tcp = new TCPMechanism();
				 run = tcp.evaluateRun(rc, this.options.remoteHost, this.options.remotePort);
				completedRuns.add(run);
				break;
			default: 
				throw new IllegalStateException("Not sure what this was");
			}
		}
		
		return completedRuns;
	}

}
