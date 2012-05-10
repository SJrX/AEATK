package ca.ubc.cs.beta.smac.ac.runs;

import ca.ubc.cs.beta.ac.RunResult;
import ca.ubc.cs.beta.ac.config.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.ac.config.RunConfig;

/**
 * This class represents a single run of the target algorithm given by the AlgorithmExecutionConfig object and the AlgorithmInstanceRunConfig object
 * 
 * @author seramage
 *
 */
public abstract class AbstractAlgorithmRun implements Runnable, AlgorithmRun{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1860615761848618478L;
	
	protected final RunConfig instanceConfig;
	protected final AlgorithmExecutionConfig execConfig;
	
	protected RunResult acResult;
	protected double runtime;
	protected int runLength;
	protected int quality;
	protected long resultSeed; 
	
	
	protected String resultLine;
	protected String rawResultLine;
	
	protected boolean runCompleted = false;
	protected boolean runResultWellFormed = false;
	
	public AbstractAlgorithmRun(AlgorithmExecutionConfig execConfig, RunConfig instanceConfig)
	{
		if(execConfig == null || instanceConfig == null)
		{
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		
		
		this.instanceConfig = instanceConfig;
		this.execConfig = execConfig;
		
	}
	
	/**
	 * Calling this method should 
	 */
	public abstract void run();
	
	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun#getExecutionConfig()
	 */
	@Override
	public final AlgorithmExecutionConfig getExecutionConfig()
	{
		return execConfig;
	}
	
	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun#getInstanceRunConfig()
	 */
	@Override
	public final RunConfig getInstanceRunConfig()
	{
		return instanceConfig;
	}
	
	
	
	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun#getAutomaticConfiguratorResult()
	 */
	@Override
	public final RunResult getRunResult() {
		if(!isRunResultWellFormed()) throw new IllegalStateException("Execution Result was not well formed");
		return acResult;
	}

	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun#getRuntime()
	 */
	@Override
	public final double getRuntime() {
		if(!isRunResultWellFormed()) throw new IllegalStateException("Execution Result was not well formed");
		return runtime;
	}

	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun#getRunLength()
	 */
	@Override
	public final int getRunLength() {
		if(!isRunResultWellFormed()) throw new IllegalStateException("Execution Result was not well formed");
		return runLength;
	}

	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun#getBestSolution()
	 */
	@Override
	public final int getQuality() {
		if(!isRunResultWellFormed()) throw new IllegalStateException("Execution Result was not well formed");
		return quality;
	}

	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun#getResultSeed()
	 */
	@Override
	public final long getResultSeed() {
		if(!isRunResultWellFormed()) throw new IllegalStateException("Execution Result was not well formed");
		return resultSeed;
	}

	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun#getResultLine()
	 */
	@Override
	public final String getResultLine() {
		if(!isRunResultWellFormed()) throw new IllegalStateException("Execution Result was not well formed");
		return resultLine;
	}

	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun#isRunCompleted()
	 */
	@Override
	public final synchronized boolean isRunCompleted() {
		return runCompleted;
	}

	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun#isRunResultWellFormed()
	 */
	@Override
	public final synchronized boolean isRunResultWellFormed() {
		if(!isRunCompleted()) throw new IllegalStateException("Run has not yet completed");
		return runResultWellFormed;
	}
	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun#rawResultLine()
	 */
	@Override
	public final String rawResultLine()
	{
		if(!isRunCompleted()) throw new IllegalStateException("Run has not yet completed");
		return rawResultLine;
	}
	
	public int hashCode()
	{
		//I believe that just instanceConfig and not execConfig hashCodes should be good enough
		//Since it's rare for two different execConfigs to have identical instanceConfigs
		//System.out.println("*****************");
		//System.out.println("ABC:" + execConfig.hashCode());
		//System.out.println(execConfig);
		//System.out.println("*****************");
		return instanceConfig.hashCode();
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof AbstractAlgorithmRun)
		{
			AbstractAlgorithmRun aro = (AbstractAlgorithmRun) o;
			return aro.execConfig.equals(execConfig) && aro.instanceConfig.equals(instanceConfig);
		} 
		return false;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(execConfig.toString()).append("\n");
		sb.append(instanceConfig.toString());
		sb.append("\nResult Line:" + resultLine) ;
		sb.append("\nRawResultLine:" + rawResultLine);
		sb.append("\nrunCompleted:" + runCompleted);
		sb.append("\nacResult:" + acResult);
		sb.append("\nClass:" + this.getClass().getSimpleName());
		return sb.toString();
		
		
	}
	
	
	
	
}
