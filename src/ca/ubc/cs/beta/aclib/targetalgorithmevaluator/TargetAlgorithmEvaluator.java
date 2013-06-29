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
 * They may throw TargetAlgorithmAbortExceptions, but again other wrappers ought to take care of it
 * <p>
 * <b>NOTE:</b>Implementations MUST be thread safe, and ideally concurrent calls to evaluateRun() should all be serialized in such a way 
 * that honours the concurrency requirements of the evaluator (in other words, if concurrency is limited to N processors, then 
 * regardless of how many times evaluateRun is called concurrently only N actual runs of the target algorithm should be running at any given time)
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 * 
 */
public interface TargetAlgorithmEvaluator {
 
	/**
	 * Evaluate a run configuration
	 * 
	 * <b>Implementation Note:</b> Any implementation of this method MUST be the same as calling 
	 * evaluateRun(List<RunConfig>) with that same run in the list.
	 * 
	 * @param runConfig RunConfig to evaluate
	 * @return	list containing the <code>AlgorithmRun<code>
	 * @throws TargetAlgorithmAbortException
	 */
	public List<AlgorithmRun> evaluateRun(RunConfig runConfig);

	/**
	 * Evaluate a sequence of run configurations
	 * @param runConfigs a list containing zero or more unique run configurations to evaluate
	 * @return	list of the exact same size as input containing the <code>AlgorithmRun</code> objects in the same order as runConfigs
	 * @throws TargetAlgorithmAbortException
	 */
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs);

	/**
	 * Evaluate a sequence of run configurations
	 * @param runConfigs	a list containing zero or more unique run configurations to evaluate
	 * @param observer 	 	observer that will be notified of the current run status
	 * @return	list of the exact same size as input containing the <code>AlgorithmRun</code> objects in the same order as runConfigs
	 * @throws TargetAlgorithmAbortException
	 */
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver observer);
	
	
	/**
	 * Evaluates the given configuration, and when complete the handler is invoked.
	 * <p>
	 * <b>Note:</b>You are guaranteed that when this method returns your runs have been 'delivered'
	 * to the eventual processor. In other words if the runs are dispatched to some external
	 * processing system, you can safely shutdown after this method call completes and know that they have been
	 * delivered. Additionally if the runs are already complete (for persistent TAEs), the call back is guaranteed to fire to completion <i>before</i> the program exits
	 * normally (that is you can do a normal shutdown, and the onSuccess method should fire)
	 *  
	 * @param runConfig  run configuration to evaluate
	 * @param callback    handler to invoke on completion or failure
	 */
	public void evaluateRunsAsync(RunConfig runConfig, TargetAlgorithmEvaluatorCallback callback );
	
	/**
	 * Evaluates the given configuration, and when complete the handler is invoked
	 * <p>
	 * <b>Note:</b>You are guaranteed that when this method returns your runs have been 'delivered'
	 * to the eventual processor. In other words if the runs are dispatched to some external
	 * processing system, you can safely shutdown after this method call completes and know that they have been
	 * delivered. Additionally if the runs are already complete (for persistent TAEs), the call back is guaranteed to fire to completion <i>before</i> the program exits
	 * normally (that is you can do a normal shutdown, and the onSuccess method should fire)
	 * 
	 * @param runConfigs list of zero or more unique run configuration to evaluate
	 * @param callback   handler to invoke on completion or failure
	 */
	public void evaluateRunsAsync(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorCallback callback);

	/**
	 * Evaluates the given configuration, and when complete the handler is invoked
	 * <p>
	 * <b>Note:</b>You are guaranteed that when this method returns your runs have been 'delivered'
	 * to the eventual processor. In other words if the runs are dispatched to some external
	 * processing system, you can safely shutdown after this method call completes and know that they have been
	 * delivered. Additionally if the runs are already complete (for persistent TAEs), the call back is guaranteed to fire to completion <i>before</i> the program exits
	 * normally (that is you can do a normal shutdown, and the onSuccess method should fire)
	 * 
	 * @param runConfigs list of zero or more unique run configuration to evaluate
	 * @param callback   handler to invoke on completion or failure
	 * @param observer	 observer that will be notified of the current run status
	 */
	public void evaluateRunsAsync(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorCallback callback, TargetAlgorithmEvaluatorRunObserver observer);
	

	/**
	 * Blocks waiting for all runs that have been invoked via evaluateRun or evaluateRunAsync to complete
	 * <b>NOTE:</b> This is NOT the same as waiting for the TAE to shutdown or be ready to shutdown, just that this TAE has no outstanding runs
	 * 
	 * @throws UnsupportedOperationException - if the TAE does not support this operation 
	 */
	public void waitForOutstandingEvaluations();
	
	/**
	 * Returns the total number of outstanding evaluations, that is the number of calls to evaluateRun or evaluateRunAsync to complete
	 * <b>NOTE:</b> This is NOT the number of runConfigs to be evaluated but the number of requests, and just because this returns zero doesn't mean it can't increase in the future.
	 * 
	 * @return number of outstanding evaluations
	 * @throws UnsupportedOperationException - if the TAE does not support this operation 
	 */
	public int getNumberOfOutstandingEvaluations();
	
	
	/**
	 * Returns the number of target algorithm runs that we have executed
	 * @return	total number of runs evaluated
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

	/**
	 * Returns a String that ought to be useful to the user to reproduce the results of a given run for a given
	 * runConfig.
	 * 
	 * For CommandLineTargetAlgorithmEvaluator this generally means a sample execution string for this algorithm.
	 * 
	 * For other evaluators it's implementation dependent, and you are free to return whatever <em>non-null</em> string you want.
	 *
	 * If you are lazy, or it really is meaningless for your implementation (say there is no other way to execute this except via SMAC)
	 * you should return <b>N/A</b> 
	 * 
	 * @param runConfig run configuration to generate a call string for
	 * @return string something the user can execute directly if necessary to reproduce the results
	 */
	public String getManualCallString(RunConfig runConfig);

	/**
	 * Notifies the TargetAlgorithmEvaluator that we are shutting down
	 * <p> 
	 * <b>Implementation Note:</b> Depending on what the TargetAlgorithmEvaluator does this can be a noop, the only purpose
	 * is to allow TargetAlgorithmEvaluators to shutdown any thread pools, that will prevent the JVM from exiting. The 
	 * TargetAlgorithmEvaluator may also choose to keep resources running for other reasons, and this method 
	 * should NOT be interpreted as requesting the TargetAlgorithmEvalutor to shutdown. 
	 * <p>
	 * Example: If this TAE were to allow for sharing of resources between multiple independent SMAC runs, a call to this method
	 * should NOT be taken as a requirement to shutdown the TAE, only that there is one less client using it. Once it recieved
	 * sufficient shutdown notices, it could then decide to shutdown.
	 * <p>
	 * Finally, if this method throws an exception, chances are the client will not catch it and will crash.
	 */
	public void notifyShutdown();
	
	/**
	 * Returns <code>true</code> if the TargetAlgorithmEvaluator run requests are final, that is
	 * rerunning the same request again would give you an identical answer.
	 * <p>
	 * <b>Implementation Note:</b> This is primarily of use to prevent decorators from trying to 
	 * get a different answer if they don't like the first one (for instance retry crashing runs, etc).
	 *
	 * @return <code>true</code> if run answers are final
	 */
	public boolean isRunFinal();
	
	/**
	 * Returns <code>true</code> if all the runs made to the TargetAlgorithmEvaluator will be persisted.
	 * <p> 
	 *<b>Implementation Note:</b> This is used to allow some programs to basically hand-off execution to some
	 * external process, say a pool of workers, and then if re-executed can get the same answer later. 
	 *
	 * @return <code>true</code> if runs can be retrieved externally of this currently running program
	 */
	public boolean areRunsPersisted();
	
	/**
	 * Returns <code>true</code> if all the runs made to the TargetAlgorithmEvaluator are observable
	 * <p>
	 * <b>Implementation Note:</b> The notification of observers is made on a best-effort basis,
	 * if this TAE just won't notify us then it should return false. This can allow for better logging 
	 * or experience for the user 
	 */
	public boolean areRunsObservable();
	
	
}
