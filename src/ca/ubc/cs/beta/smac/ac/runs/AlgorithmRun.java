package ca.ubc.cs.beta.smac.ac.runs;

import java.io.Serializable;

import ca.ubc.cs.beta.ac.RunResult;
import ca.ubc.cs.beta.ac.config.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.ac.config.RunConfig;

public interface AlgorithmRun extends Runnable, Serializable {

	public AlgorithmExecutionConfig getExecutionConfig();

	public RunConfig getInstanceRunConfig();

	public RunResult getRunResult();

	public double getRuntime();


	public int getRunLength();

	//TODO This should be a double
	public int getQuality();

	public long getResultSeed();

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
	 * Returns the raw output of the line we matched
	 * @return
	 */
	public abstract String rawResultLine();

}