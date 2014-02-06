package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.resource.caching;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.ThreadSafe;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorHelper;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;


/**
 * This Target Algorithm Evaluator Decorator can be used as a cache of runs
 * 
 * Specifically it can be used to simplify algorithms or processes that may make repeated
 * sets of run requests. We need to be very careful that the target algorithm evaluator 
 * only sees one request regardless of how many times the request comes in, and regardless
 * of thread interleavings.
 * 
 * Finally it also should be safe with respect to Interruption, that is
 * a thread that is interuppted with respect to the client shouldn't be able to negatively affect other
 * threads / requests
 * 
 * 
 * 
 * 
 * Care must be taken to ensure that we don't notify the client with out of order data. We also don't use the same callback thread to notify the client
 * because it can create deadlocks (if the callback causes more runs to submit, it can get trapped here).
 * <p>
 * In the case of updating the observer, we update the shared data structure of ALL the runs and then notify the client's observer.
 * <p>
 * In the case of processing the callback, we update for all the outstanding runs to be completed, and then execute the client's callback. 
 * <p>
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
@ThreadSafe
public class CachingTargetAlgorithmEvaluatorDecorator extends AbstractTargetAlgorithmEvaluatorDecorator {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final AtomicInteger cacheRequests = new AtomicInteger(0);
	private final AtomicInteger cacheRequestMisses = new AtomicInteger(0);
	
	
	private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	
	/**
	 * Runs are enqueued here for another thread to retrieve
	 */
	private final LinkedBlockingQueue<List<RunConfig>> submissionQueue = new LinkedBlockingQueue<List<RunConfig>>();
	
	
	/**
	 * Executor service for submitting runs to the wrapped TAEs
	 */
	private final Executor submissionExecService = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS, new SequentiallyNamedThreadFactory("Caching Target Algorithm Evaulator Submission Thread"));
	
	/**
	 * Queue that contains callbacks with every run done
	 */
	private final LinkedBlockingQueue<TargetAlgorithmEvaluatorCallback> callbackQueue = new LinkedBlockingQueue<TargetAlgorithmEvaluatorCallback>();
	
	/**
	 * Executor service for invoking callbacks
	 */
	private final Executor callbackExecService = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS, new SequentiallyNamedThreadFactory("Caching Target Algorithm Evaulator Submission Thread"));
	
	
	
	/**
	 * Threads coordinate on this map, if they put something in this map
	 * it means that thread is responsible for ensuring that the run config is delivered
	 * 
	 * [Used for Coordination Between Threads on Job Submission]
	 * 
	 * Populated: On call to {@link #evaluateRunsAsync(List, TargetAlgorithmEvaluatorCallback, TargetAlgorithmEvaluatorRunObserver)}
	 * Cleanup: Entries are never removed
	 */
	private final ConcurrentHashMap<RunConfig, CountDownLatch> cachedElements = new ConcurrentHashMap<RunConfig, CountDownLatch>();
	
	
	/***
	 * This map is used to determine whether every outstanding run for a callback is done
	 * 
	 * [Used for Coordination Between Threads of decrementing counter]
	 * Populated: on call to {@link #evaluateRunsAsync(List, TargetAlgorithmEvaluatorCallback, TargetAlgorithmEvaluatorRunObserver)}
	 * Cleanup: After callback is fired remove the key from map
	 */
	private final ConcurrentHashMap<TargetAlgorithmEvaluatorCallback, Set<RunConfig>> outstandingRunsForCallbackMap = new ConcurrentHashMap<TargetAlgorithmEvaluatorCallback, Set<RunConfig>>();
	
	/***
	 * This map is used to determine whether every outstanding run for a callback is done
	 * 
	 * [Used for Coordination Between Threads of scheduling callback]
	 * Populated: on call to {@link #evaluateRunsAsync(List, TargetAlgorithmEvaluatorCallback, TargetAlgorithmEvaluatorRunObserver)}
	 * Cleanup: After callback is fired remove the key from map
	 */
	private final ConcurrentHashMap<TargetAlgorithmEvaluatorCallback, AtomicInteger> outstandingRunsCountForCallbackMap = new ConcurrentHashMap<TargetAlgorithmEvaluatorCallback, AtomicInteger>();
	
	
	/**
	 * Stores for every callback, the entire set of runconfigs associated with it, this is mainly so that we have a completed list after
	 * 
	 * Populated: on call to {@link #evaluateRunsAsync(List, TargetAlgorithmEvaluatorCallback, TargetAlgorithmEvaluatorRunObserver)}
	 * Cleanup: After callback is fired remove the key from map
	 */
	private final ConcurrentHashMap<TargetAlgorithmEvaluatorCallback, List<RunConfig>> allRunConfigsForCallbackMap = new ConcurrentHashMap<TargetAlgorithmEvaluatorCallback, List<RunConfig>>();
	
	
	/**
	 * This map is used to determine which callbacks need to be notified on an individual run completion
	 * 
	 * Populated: on call to {@link #evaluateRunsAsync(List, TargetAlgorithmEvaluatorCallback, TargetAlgorithmEvaluatorRunObserver)}
	 * Cleanup: During callback firing the inner set has elements removed, the outter map is never cleaned up
	 */
	private final ConcurrentHashMap<RunConfig, Set<TargetAlgorithmEvaluatorCallback>> runConfigToCallbacksMap = new ConcurrentHashMap<RunConfig, Set<TargetAlgorithmEvaluatorCallback>>();
	
	
	/**
	 * This map stores the set of completed run configs
	 * A run is completed if has a result in either the completed runs map, or the completed exceptions map (they should be populated first)
	 * 
	 * Populated: When entries are completed in {@link SubmissionOnCompleteHandler#onComplete()}
	 * Cleanup: Entries are never removed
	 */
	private final Set<RunConfig> completedRunConfigs = Collections.newSetFromMap(new ConcurrentHashMap<RunConfig, Boolean>());
	
	/**
	 * This map stores the completed runs
	 * 
	 * Populated: When entries are completed: {@link SubmissionOnCompleteHandler#onSuccess()}
	 * Cleanup: Entries are never removed
	 */
	private final ConcurrentHashMap<RunConfig, AlgorithmRun> completedRunsMap = new ConcurrentHashMap<RunConfig, AlgorithmRun>();
	
	
	/**
	 * This map stores completed exceptions
	 * 
	 * Populated: When entries are completed: {@link SubmissionOnCompleteHandler#onFailure()}
	 * Cleanup: Entries are never removed
	 */
	private final ConcurrentHashMap<RunConfig, RuntimeException> completedExceptionMap = new ConcurrentHashMap<RunConfig, RuntimeException>();
	
	
	
	public CachingTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae) {
		super(tae);
		
		for(int i=0; i < AVAILABLE_PROCESSORS; i++)
		{
			submissionExecService.execute(new RunSubmitter());
			callbackExecService.execute(new CallbackInvoker());
		}
	
	}

	
	
	@Override
	public void evaluateRunsAsync(final List<RunConfig> rcs, final TargetAlgorithmEvaluatorCallback callback, final TargetAlgorithmEvaluatorRunObserver obs) 
	{

		if(rcs.isEmpty())
		{
			callback.onSuccess(Collections.<AlgorithmRun> emptyList());
			return;
		}
		
		
		final List<RunConfig> runConfigs = Collections.unmodifiableList(rcs);
		
		List<RunConfig> myRCsToSubmit = new ArrayList<RunConfig>(runConfigs.size());
		
		Set<RunConfig> outstandingRunsForCallback = Collections.newSetFromMap(new ConcurrentHashMap<RunConfig, Boolean>());
		outstandingRunsForCallback.addAll(runConfigs);
		
		
		outstandingRunsForCallbackMap.put(callback, outstandingRunsForCallback);
		outstandingRunsCountForCallbackMap.put(callback,  new AtomicInteger(rcs.size()));
		allRunConfigsForCallbackMap.put(callback, runConfigs);
		
		CountDownLatch countDownLatch = new CountDownLatch(1);
		//Try adding a new count down latch counter until it succeeds
		
		Set<TargetAlgorithmEvaluatorCallback> callbacksForRunConfig = Collections.newSetFromMap(new ConcurrentHashMap<TargetAlgorithmEvaluatorCallback,Boolean>());
		
		for(RunConfig rc : runConfigs )
		{			
			CountDownLatch value = cachedElements.putIfAbsent(rc, countDownLatch);
			
			//New value inserted
			if(value == null)
			{
				myRCsToSubmit.add(rc);
				
				//Create a new CountDownLatch only when needed.
				countDownLatch = new CountDownLatch(1);
			}
			
			Set<TargetAlgorithmEvaluatorCallback> oSet =  runConfigToCallbacksMap.putIfAbsent(rc, callbacksForRunConfig);
			
			if(oSet == null)
			{
				oSet = callbacksForRunConfig;
				//Create a new set only when needed.
				callbacksForRunConfig = Collections.newSetFromMap(new ConcurrentHashMap<TargetAlgorithmEvaluatorCallback,Boolean>());
			} 
			
			oSet.add(callback);
		}
		
		int requests = cacheRequests.addAndGet(runConfigs.size());
		int misses = cacheRequestMisses.addAndGet(myRCsToSubmit.size());
		NumberFormat nf = NumberFormat.getPercentInstance();
		log.debug("Cache Local misses: {}, Local request: {},  Global misses {}, Global Requests {}, Hit Rate {} ", myRCsToSubmit.size(), runConfigs.size(),  misses, requests, nf.format( ((double) requests - misses) / requests)  );
		if(myRCsToSubmit.size() > 0)
		{
			boolean submissionSucceeded = false;
			boolean interrupted = false;
			do 
			{
				try {
					submissionQueue.put(myRCsToSubmit);
					submissionSucceeded = true;
				} catch (InterruptedException e) {
					//Keep trying until it succeeds
					interrupted = true;
				}
				
				
			} while(!submissionSucceeded);
			
			if(interrupted)
			{
				Thread.currentThread().interrupt();
				return;
			}
			
		}
			
		
		/**
		 * Wait until everything is submitted
		 */
		for(RunConfig rc : runConfigs)
		{
			try {
				cachedElements.get(rc).await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
		
		for(RunConfig rc : runConfigs)
		{
			if(completedRunConfigs.contains(rc))
			{
				removeRC(callback,rc);
			}
		}
		
		
		
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		return TargetAlgorithmEvaluatorHelper.evaluateRunSyncToAsync(runConfigs, this, obs);
	}
	
	
	@Override
	protected void postDecorateeNotifyShutdown() {
		
		NumberFormat nf = NumberFormat.getPercentInstance();
		int misses = cacheRequestMisses.get();
		int requests =  cacheRequests.get();
		
		log.info("Cache misses {}, Cache requests {}, Hit Rate {} ", misses, requests, nf.format( ((double) requests - misses) / requests)  );
		
		
	}

	
	/**
	 * Removes a runconfig from the set of outstanding callbacks
	 * 
	 * @param callback
	 * @param rc
	 * @return
	 */
	private final void removeRC(TargetAlgorithmEvaluatorCallback callback, RunConfig rc)
	{
		
		Set<RunConfig> outstandingRunsForCallback = outstandingRunsForCallbackMap.get(callback);
		
		if(outstandingRunsForCallback == null)
		{
			return;
		}
		
		boolean rcRemoved = outstandingRunsForCallback.remove(rc);
		
		if(rcRemoved)
		{
			int remaining = outstandingRunsCountForCallbackMap.get(callback).decrementAndGet();
			
			if(remaining < 0)
			{
				log.error("Desynchronization detected as the number of remaining elements seems to be less than zero for callback: {} and rc: {}", callback, rc);
			}
			if(remaining == 0)
			{
				//log.debug("Callback {} scheduled for invocation", callback);
				try {
					//We don't submit here in case there are many callbacks that are waiting
					callbackQueue.add(callback);
				} catch(IllegalStateException e)
				{
					e.printStackTrace();
					log.error("Exception occurred while adding stuff for callback", e);
				}
			}
		}
		
	
		
	}
	
	private class RunSubmitter implements Runnable
	{

		@Override
		public void run() {
			
			while(true)
			{
				List<RunConfig> rcs;
				try {
					rcs = submissionQueue.take();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
				
				tae.evaluateRunsAsync(rcs, new SubmissionOnCompleteHandler(rcs));
				log.debug("Runs have been successfully submitted to TAE: {}", rcs);
				for(RunConfig rc : rcs)
				{
					CountDownLatch lt = cachedElements.get(rc);
					if(lt == null)
					{
						log.error("No countdown latch for run config, this is a violation of our invariant {}" ,rc);
					} else if(lt.getCount() != 1)
					{
						log.error("Count down latch seemingly has the wrong value of latches left for rc  {}  value {} ", rc, lt);
					} else
					{
						lt.countDown();
					}
				}
			}
			
		}
		
	}
	
	/**
	 * Callback that is fired once the batch of runs we submitted are done
	 * 
	 * This essentially determines which callbacks should now be fired and queues them.
	 * 
	 * @author Steve Ramage <seramage@cs.ubc.ca>
	 */
	private class SubmissionOnCompleteHandler implements TargetAlgorithmEvaluatorCallback
	{
		private final List<RunConfig> submissionRunConfigs;
		
		public SubmissionOnCompleteHandler(List<RunConfig> rcs)
		{
			this.submissionRunConfigs = rcs;
		}

		@Override
		public void onSuccess(List<AlgorithmRun> runs) 
		{

			for(AlgorithmRun run : runs)
			{
				completedRunsMap.put(run.getRunConfig(), run);
			}
			onComplete();
			
		}

		@Override
		public void onFailure(RuntimeException e) 
		{
			
			for(RunConfig rc : submissionRunConfigs)
			{
				completedExceptionMap.put(rc, e);
			}
			
			onComplete();
		}
		
		private void onComplete()
		{
			for(RunConfig rc : submissionRunConfigs)
			{
				completedRunConfigs.add(rc);
				for(TargetAlgorithmEvaluatorCallback cb : runConfigToCallbacksMap.get(rc))
				{
					removeRC(cb,rc);
				}
			}
		}

	}
	
	/**
	 * Runnable that notifies the callbacks 
	 * @author Steve Ramage <seramage@cs.ubc.ca>
	 *
	 */
	private class CallbackInvoker implements Runnable
	{
		public void run()
		{
			while(true)
			{
				TargetAlgorithmEvaluatorCallback callback;
				try {
					callback = callbackQueue.take();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
					try 
					{
						
						try {
					
							List<RunConfig> rcs = allRunConfigsForCallbackMap.get(callback);
							
							List<AlgorithmRun> runs = new ArrayList<AlgorithmRun>(rcs.size());
							
							for(RunConfig rc : rcs)
							{
								if(!completedRunConfigs.contains(rc))
								{
									log.error("Run is not marked as completed, yet callback is being fired: {}", rc);
									
									throw new IllegalStateException("Callback fired but not all runs are complete");
								}
							
								AlgorithmRun run = completedRunsMap.get(rc);
								
								if(run != null)
								{
									runs.add(run); 
								} else
								{
									throw completedExceptionMap.get(rc);
								}
								
								runConfigToCallbacksMap.get(rc).remove(callback);
							}
							
							
							callback.onSuccess(runs);
							
						} catch(RuntimeException e)
						{
							callback.onFailure(e);
						}
					} finally
					{
						
						allRunConfigsForCallbackMap.remove(callback);
						outstandingRunsCountForCallbackMap.remove(callback);
						
						//Remove this last so that other calls to removeRC() don't throw an NPE
						outstandingRunsForCallbackMap.remove(callback);
					}
			}
		}
		
	}

}
