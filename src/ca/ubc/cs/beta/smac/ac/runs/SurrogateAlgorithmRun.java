package ca.ubc.cs.beta.smac.ac.runs;

import ca.ubc.cs.beta.ac.RunResult;
import ca.ubc.cs.beta.ac.config.RunConfig;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;

public class SurrogateAlgorithmRun extends AbstractAlgorithmRun {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2253548614421450243L;
	private static long seedValues = Long.MIN_VALUE;
	
	public SurrogateAlgorithmRun(AlgorithmExecutionConfig execConfig,
			RunConfig instanceConfig, double response  )
	{
		super(execConfig, instanceConfig);
		
		this.acResult = RunResult.SAT;
		this.runtime = response;
		this.resultSeed = seedValues++;
		
	
		this.resultLine = "SAT, " + runtime + ", 0, 0, " + resultSeed;
		
		runResultWellFormed = true;
		runCompleted = true;
	}

	@Override
	public void run() {
		
	}

}
