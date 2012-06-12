package ca.ubc.cs.beta.smac.ac.runs;

import java.io.Serializable;

import ca.ubc.cs.beta.ac.RunResult;
import ca.ubc.cs.beta.ac.config.RunConfig;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;

public interface AlgorithmRun extends Runnable, Serializable {

	public AlgorithmExecutionConfig getExecutionConfig();

	public RunConfig getInstanceRunConfig();

	public RunResult getRunResult();

	public double getRuntime();


	public double getRunLength();

	public double getQuality();

	public long getResultSeed();

	/**
	 * Note: This should always return a well-formatted result line. It may NOT necessarily correspond to values
	 * that the methods return. 
	 * 
	 * (i.e. You should be able to use this output as standard output without any validation, but it may not correspond to what we got this time)
	 * 
	 * Some extreme examples are when we clean up messy wrappers output (for instance SAT >= timeout). Depending on how the cleanup is done, this
	 * may change the result flagged, or we may massage the timeout. 
	 * 
	 * @return
	 */
	public String getResultLine();
	
	
	/**
	 * Calling this method guarantees that isRunCompleted() is true
	 * You should not call this method if isRunCompleted() is true (implementations should ignore it if so.
	 * Some implementations may always have isRunCompleted() as true, (dummy implementations for instance).
	 */
	public void run();

	public boolean isRunCompleted();

	public boolean isRunResultWellFormed();

	/**
	 * Returns the raw output of the line we matched (if any) 
	 * @return
	 */
	public abstract String rawResultLine();

}