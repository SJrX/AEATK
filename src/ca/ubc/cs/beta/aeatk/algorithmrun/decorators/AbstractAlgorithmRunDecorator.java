package ca.ubc.cs.beta.aeatk.algorithmrun.decorators;

import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.RunResult;
import ca.ubc.cs.beta.aeatk.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aeatk.runconfig.RunConfig;

/**
 * Abstract class that wraps another AlgorithmRun
 * @author Steve Ramage 
 *
 */
public class AbstractAlgorithmRunDecorator implements AlgorithmRun {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6324011218801256306L;
	
	private final AlgorithmRun wrappedRun;
	

	/**
	 * Wraps the specified run 
	 * @param run	run to wrap
	 */
	public AbstractAlgorithmRunDecorator(AlgorithmRun run)
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
	public double getWallclockExecutionTime() {
		return wrappedRun.getWallclockExecutionTime();
	}

	@Override
	public String getAdditionalRunData() {

		return wrappedRun.getAdditionalRunData();
	}

	@Override
	public boolean isCensoredEarly() {
		return wrappedRun.isCensoredEarly();
	}

	@Override
	public void kill() {
		wrappedRun.kill();
	}
	

	
}
