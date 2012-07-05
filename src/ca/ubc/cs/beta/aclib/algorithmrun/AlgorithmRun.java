package ca.ubc.cs.beta.aclib.algorithmrun;

import java.io.Serializable;
import java.util.concurrent.Callable;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

/**
 * Represents an execution of a target algorithm. 
 * 
 * All implementations should be effectively immutable (except for the run()) method that is.
 * 
 * NOTE: The following invariants exist, and implementations that don't follow this may have unexpected results
 * 
 * @author sjr
 */
public interface AlgorithmRun extends Runnable, Serializable,  Callable<Object> {

	/**
	 * Returns the AlgorithmExecutionConfig of the run
	 * 
	 * @return AlgorithmExecutionConfig of the run
	 * 
	 */
	public AlgorithmExecutionConfig getExecutionConfig();

	/**
	 * Return the run configuration associated with the AlgorithmRun
	 * @return run configuration of this run
	 */
	public RunConfig getRunConfig();

	/**
	 * Get the Run Result
	 * 
	 *  The Run Result should be TIMEOUT, CRASHED or ABORT if and only if the runtime() is >= the associated cutoff time.
	 * 
	 *  The Run Result should be TIMEOUT if the cutoff time is zero, and we probably shouldn't bother doing anything 
	 *  
	 * @return RunResult for run
	 * @throws IllegalStateException if the run has not completed
	 */
	public RunResult getRunResult();

	/**
	 * Get reported runtime of run
	 * 
	 * @return double for the runtime
	 * @throws IllegalStateException if the run has not completed
	 */
	public double getRuntime();

	/**
	 * Get the reported run length
	 * 
	 * @return double for the runlength
	 * @throws IllegalStateException if the run has not completed
	 */
	public double getRunLength();

	/**
	 * Get the reported quality 
	 * 
	 * @return double for the quality
	 * @throws IllegalStateException if the run has not completed
	 */
	public double getQuality();

	/**
	 * Get the seed that was returned
	 * 
	 * NOTE: For well behaved programs this should always be the seed in the ProblemInstanceSeedPair of RunConfig
	 * @return seed reported by algorithm
	 * @throws IllegalStateException if the run has not completed
	 */
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
	 * @return string representing a close approximation of this run that is guaranteed to be parsable.
	 * @throws IllegalStateException if the run has not completed
	 */
	public String getResultLine();
	
	
	/**
	 * Runs this AlgorithmRun
	 * 
	 * Subsequent calls to this should be noop, and are not error conditions.
	 * 
	 * If this method successfully returns it's guaranteed that isRunCompleted() is true
	 */
	public void run();
	
	/**
	 * Runs this Algorithm Run
	 * 
	 * Subsequent calls to this should be a noop, and are not error conditions
	 * 
	 * If this method successfully returns it's guaranteed that isRunCompleted is true
	 * 
	 * @return null (always)
	 */
	public Object call();
	

	/**
	 * Returns whether this run is completed
	 * @return <code>true</code> if this run has finished executing, <code>false</code> otherwise
	 */
	public boolean isRunCompleted();

	/**
	 * Returns whether this run gave us intelligible output
	 * @return <code>true</code> if this run returned something parsable, <code>false</code> otherwise
	 * @throws IllegalStateException if the run has not completed
	 * NOTE: This method will probably go away in favor of having algorithms just use CRASHED as the result
	 */
	public boolean isRunResultWellFormed();

	/**
	 * Returns the raw output of the line we matched (if any), this is for debug purposes only
	 * and there is no requirement that this actually return any particular string.
	 * 
	 * Implementation Note: An example where this is useful is if you use a weaker regex to match a possible output, and 
	 * then the stronger parsing fails. The weaker regex match could be returned here
	 * 
	 * @return string possibly containing a raw result
	 */
	public abstract String rawResultLine();

}