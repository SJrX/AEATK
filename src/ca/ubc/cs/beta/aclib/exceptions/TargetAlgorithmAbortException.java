package ca.ubc.cs.beta.aclib.exceptions;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;

/**
 * Exception thrown if a target algorithm signals an abort
 * @author sjr
 *
 */
public class TargetAlgorithmAbortException extends RuntimeException {

	private static final long serialVersionUID = 772736289871868435L;
	private AlgorithmRun run;

	public TargetAlgorithmAbortException(AlgorithmRun run)
	{
		super("Target algorithm execution signaled that we should ABORT");
		this.run = run;
	}
	
	public AlgorithmRun getAlgorithmRun()
	{
		return run;
	}
}
