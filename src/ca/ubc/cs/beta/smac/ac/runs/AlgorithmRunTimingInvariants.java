package ca.ubc.cs.beta.smac.ac.runs;

import ca.ubc.cs.beta.ac.RunResult;
import ca.ubc.cs.beta.ac.config.RunConfig;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;

/**
 * Corrects output from misbehaiving wrappers
 * 
 * Specifically it ensures that:
 * 
 * SAT results are always less than the captime.
 * 
 * 
 * 
 * @author seramage
 *
 */
public class AlgorithmRunTimingInvariants implements AlgorithmRun {

	private final AlgorithmRun run;
	
	
	public AlgorithmRunTimingInvariants(AlgorithmRun run)
	{
		this.run = run;
		
	}

	@Override
	public AlgorithmExecutionConfig getExecutionConfig() {

		return run.getExecutionConfig();
	}

	@Override
	public RunConfig getInstanceRunConfig() {
		return run.getInstanceRunConfig();
	}

	@Override
	public RunResult getRunResult() {
		if(getRuntime() >= run.getInstanceRunConfig().getCutoffTime())
		{ 
			return RunResult.TIMEOUT; 
		}
		return run.getRunResult();
	}

	@Override
	public double getRuntime() {
		return run.getRuntime();
	}

	@Override
	public double getRunLength() {
		return run.getRunLength();
	}

	@Override
	public double getQuality() {
		return run.getQuality();
	}

	@Override
	public long getResultSeed() {
		return run.getResultSeed();
	}

	@Override
	public String getResultLine() {
		return run.getResultLine();
	}

	@Override
	public void run() {
		run.run();
	}

	@Override
	public boolean isRunCompleted() {

		return run.isRunCompleted();
	}

	@Override
	public boolean isRunResultWellFormed() {
		return run.isRunResultWellFormed();
	}

	@Override
	public String rawResultLine() {
		return run.rawResultLine();
	}
	

	
	
	

	
}
