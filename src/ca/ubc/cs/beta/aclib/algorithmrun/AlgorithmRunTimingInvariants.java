package ca.ubc.cs.beta.aclib.algorithmrun;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

/**
 * Corrects output from misbehaiving wrappers
 * 
 * Specifically it ensures that:
 * 
 * SAT results are always less than the captime.
 * 
 * @author seramage
 *
 */
public class AlgorithmRunTimingInvariants implements AlgorithmRun {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4108923561335725916L;
	
	private final AlgorithmRun wrappedRun;
	
	/**
	 * Wraps the specified run with methods that clean the output
	 * @param run	run to wrap
	 */
	public AlgorithmRunTimingInvariants(AlgorithmRun run)
	{
		this.wrappedRun = run;
		
	}

	@Override
	public AlgorithmExecutionConfig getExecutionConfig() {

		return wrappedRun.getExecutionConfig();
	}

	@Override
	public RunConfig getRunConfig() {
		return wrappedRun.getRunConfig();
	}

	@Override
	public RunResult getRunResult() {
		if(getRuntime() >= wrappedRun.getRunConfig().getCutoffTime())
		{ 
			return RunResult.TIMEOUT; 
		}
		return wrappedRun.getRunResult();
	}

	@Override
	public double getRuntime() {
		return wrappedRun.getRuntime();
	}

	@Override
	public double getRunLength() {
		return wrappedRun.getRunLength();
	}

	@Override
	public double getQuality() {
		return wrappedRun.getQuality();
	}

	@Override
	public long getResultSeed() {
		return wrappedRun.getResultSeed();
	}

	@Override
	public String getResultLine() {
		return wrappedRun.getResultLine();
	}

	@Override
	public void run() {
		wrappedRun.run();
	}

	@Override
	public boolean isRunCompleted() {

		return wrappedRun.isRunCompleted();
	}

	@Override
	public boolean isRunResultWellFormed() {
		return wrappedRun.isRunResultWellFormed();
	}

	@Override
	public String rawResultLine() {
		return wrappedRun.rawResultLine();
	}

	@Override
	public Object call() {
		run();
		return null;
	}
	

	
	
	

	
}
