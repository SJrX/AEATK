package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.experimental.queuefacade.basic;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.ThreadSafe;

import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.experimental.queuefacade.general.TargetAlgorithmEvaluatorQueueFacade;


/**
 * Facade for Target Algorithm Evaluators that simplifies Thread Safety management for clients using asynchronous execution
 * <p>
 * <b>Note:</b>This class essentially calls the runs asynchronously and then puts the results in a Queue, that can be read 
 * by the client. Provided that nothing is modifying the context object in between this should be thread safe.
 * By synchronizing all methods we are trying to ensure memory visibility guarantees. 
 * <p>
 * The related class {@link ca.ubc.cs.beta.aclib.targetalgorithmevaluator.experimental.queuefacade.general.TargetAlgorithmEvaluatorQueueFacade} is more general
 * and allows specifying arbitrary context. Ignoring the context objects, the interfaces should be identical.
 * 
 * @see ca.ubc.cs.beta.aclib.targetalgorithmevaluator.experimental.queuefacade.general.TargetAlgorithmEvaluatorQueueFacade
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
@ThreadSafe
public class BasicTargetAlgorithmEvaluatorQueue {

	private TargetAlgorithmEvaluatorQueueFacade<BasicTargetAlgorithmEvaluatorQueueResultContext> taeQueue;
	
	public BasicTargetAlgorithmEvaluatorQueue(TargetAlgorithmEvaluator tae, boolean throwExceptions)
	{
		taeQueue = new TargetAlgorithmEvaluatorQueueFacade<BasicTargetAlgorithmEvaluatorQueueResultContext>(tae, throwExceptions);
	}
	
	/**
	 * Retrieves a reference to the Target Algorithm Evaluator
	 * @return
	 */
	public synchronized TargetAlgorithmEvaluator getTargetAlgorithmEvaluator()
	{
		return taeQueue.getTargetAlgorithmEvaluator();
	}
	
	/**
	 * Evaluate a single run
	 * 
	 * @param context 		context result that will be populated with the answer
	 * @param runConfig		runConfig to run
	 */
	public synchronized void evaluateRunAsync(RunConfig runConfig)
	{
		this.evaluateRunAsync(Collections.singletonList(runConfig));
	}
	
	/**
	 * Evaluate many runs
	 * 
	 * @param runConfigs	a list of runConfigs to run
	 */
	public synchronized void evaluateRunAsync(List<RunConfig> runConfigs)
	{
		this.evaluateRunAsync(runConfigs, null);
	}
	
	/**
	 * Evaluate many runs
	 * 
	 *
	 * @param runConfigs	a list of runConfigs to run
	 * @param obs			observer to monitor
	 */
	public synchronized void evaluateRunAsync(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs)
	{
		BasicTargetAlgorithmEvaluatorQueueResultContext context = new BasicTargetAlgorithmEvaluatorQueueResultContext();
		taeQueue.evaluateRunAsync(context, runConfigs, obs);
	}
	
	/**
	 * Retrieves and removes an element from the set of completed results or <code>null</code> if there are none 
	 * @return 	context or <code>null</code> if none available
	 */
	public synchronized BasicTargetAlgorithmEvaluatorQueueResultContext poll()
	{
		return taeQueue.poll();
	}
	
	/**
	 * Returns a completed element when available
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized BasicTargetAlgorithmEvaluatorQueueResultContext take() throws InterruptedException
	{
		return taeQueue.take();
	}
	
	/**
	 * Returns a completed element when available or null if it times out
	 * @param timeout	the amount of time to wait
	 * @param unit		the unit for the amount of time
	 * @return			a completed context or <code>null</code>
	 * @throws InterruptedException
	 */
	public synchronized BasicTargetAlgorithmEvaluatorQueueResultContext poll(long timeout, TimeUnit unit) throws InterruptedException
	{
		return taeQueue.poll(timeout, unit);
	}
	
	/**
	 * Retrieves but does not remove an element from the set of completed results or <code>null</code> if there are none 
	 * @return 	context or <code>null</code> if none available
	 */
	public synchronized BasicTargetAlgorithmEvaluatorQueueResultContext peek()
	{
		return taeQueue.peek();
	}
	
	/**
	 * Retrieves an approximate number of outstanding runs (that is runs that were evaluated but are not queued)
	 * 
	 * @return approximate number of outstanding runs
	 */
	public synchronized int getApproximateNumberOutstandingRuns()
	{
		return taeQueue.getApproximateNumberOutstandingRuns();
	}
	
	/**
	 * Retrieves an approximate number of runs in the queue (Note: This can be an O(n) operation)
	 * 
	 * @return approxmiate number of queued runs
	 */
	public synchronized int getApproximateNumberOfQueuedRuns()
	{
		return taeQueue.getApproximateNumberOfQueuedRuns();
	}
	
	
}
