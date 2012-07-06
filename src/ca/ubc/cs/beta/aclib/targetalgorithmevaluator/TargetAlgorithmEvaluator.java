package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

/**
 * Executes Target Algorithm Runs (Converts between RunConfig objects to AlgorithmRun objects)
 * <p>
 * <b>Implementation Details</b>
 * <p>
 * Clients should subtype this interface if they want to allow SMAC or other related projects to execute algorithms through
 * some other method. All implementations MUST have a constructor that takes a {@link ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig} object.
 * <p>
 * Additionally client implementations should probably not validate the output of AlgorithmRuns but rely on other wrappers to do this for them.
 * <p>
 * They may through TargetAlgorithmAbortExceptions, but again wrappers will take care of this if. 
 *
 * @author Steve Ramage
 * 
 */
public interface TargetAlgorithmEvaluator {
 

	/**
	 * Evaluate a run configuration
	 * @param run RunConfiguration to evaluate
	 * @return	list containing the <code>AlgorithmRun<code>
	 * @throws TargetAlgorithmAbortException
	 */
	public List<AlgorithmRun> evaluateRun(RunConfig run);

	/**
	 * Evaluate a sequence of run configurations
	 * @param runConfigs a list containing run configurations to evaluate
	 * @return	list containing the <code>AlgorithmRun</code> objects in the same order as runConfigs
	 * @throws TargetAlgorithmAbortException
	 */
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs);
	
	/**
	 * Returns the number of target algorithm runs that we have executed
	 * @return	total number of runs evaluated
	 * 
	 */
	public int getRunCount();
	
	
	/**
	 * May optionally return a unique number that should roughly correspond to a unique sequence of run requests.
	 *  
	 * [i.e. A user seeing the same sequence of run codes, should be confident that the runs by the Automatic Configurator are 
	 * identical]. Note: This method is optional and may just return zero. 
	 * 
	 * @return runHashCode computed
	 * 
	 */
	public int getRunHash();
	
	/**
	 * Sets the runCount to the given parameter
	 * 
	 * This is useful when we are restoring the state of SMAC 
	 * 
	 * @see ca.ubc.cs.beta.aclib.state.StateFactory
	 * @param runs
	 */
	public void seek(List<AlgorithmRun> runs);
	
	
}
