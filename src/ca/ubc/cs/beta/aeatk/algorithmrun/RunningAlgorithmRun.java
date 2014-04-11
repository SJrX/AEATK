package ca.ubc.cs.beta.aeatk.algorithmrun;

import ca.ubc.cs.beta.aeatk.algorithmrun.kill.KillHandler;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;

/**
 * AlgorithmRun that reports that it's current status is RUNNING.
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
public class RunningAlgorithmRun extends ExistingAlgorithmRun {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5427091882882378946L;
	private final KillHandler handler;

	public RunningAlgorithmRun(
			AlgorithmRunConfiguration runConfig, double runtime, double runlength, double quality, long seed, double walltime, KillHandler handler) {
		super( runConfig, RunResult.RUNNING, runtime, runlength, quality, seed, walltime);
		this.handler = handler;
	}

	@Deprecated
	/**
	 * @deprecated  the constructor that doesn't take a result string is preferred.
	 */
	/*
	public RunningAlgorithmRun(AlgorithmExecutionConfig execConfig,
			RunConfig runConfig, String result, KillHandler handler) {
		super(execConfig, runConfig, result);
		this.handler = handler;
	}*/

	@Override
	public void kill() {
		handler.kill();
		
	}

}
