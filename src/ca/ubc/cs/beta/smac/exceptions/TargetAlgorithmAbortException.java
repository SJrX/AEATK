package ca.ubc.cs.beta.smac.exceptions;

import ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun;

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
