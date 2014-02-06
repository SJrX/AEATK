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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.ThreadSafe;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunningAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillHandler;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableWrappedAlgorithmRun;
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
	private final LinkedBlockingQueue<EvaluationRequestToken> tokenToCallbackQueue = new LinkedBlockingQueue<EvaluationRequestToken>();
	
	/**
	 * Executor service for invoking callbacks
	 */
	private final Executor callbackExecService = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS, new SequentiallyNamedThreadFactory("Caching Target Algorithm Evaulator Callback Thread"));
	
	/**
	 * Queue that contains callbacks with every run done
	 */
	private final LinkedBlockingQueue<EvaluationRequestToken> tokenToObserverQueue = new LinkedBlockingQueue<EvaluationRequestToken>();
	
	/**
	 * Executor service for invoking callbacks
	 */
	private final Executor observerExecService = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS, new SequentiallyNamedThreadFactory("Caching Target Algorithm Evaulator Observer Thread"));
	
	
	
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
	private final ConcurrentHashMap<EvaluationRequestToken, Set<RunConfig>> outstandingRunsForTokenMap = new ConcurrentHashMap<EvaluationRequestToken, Set<RunConfig>>();
	
	/***
	 * This map is used to determine whether every outstanding run for a callback is done
	 * 
	 * [Used for Coordination Between Threads of scheduling callback]
	 * Populated: on call to {@link #evaluateRunsAsync(List, TargetAlgorithmEvaluatorCallback, TargetAlgorithmEvaluatorRunObserver)}
	 * Cleanup: After callback is fired remove the key from map
	 */
	private final ConcurrentHashMap<EvaluationRequestToken, AtomicInteger> outstandingRunsCountForTokenMap = new ConcurrentHashMap<EvaluationRequestToken, AtomicInteger>();
	
	
	/**
	 * Stores for every callback, the entire set of runconfigs associated with it, this is mainly so that we have a completed list after
	 * 
	 * Populated: on call to {@link #evaluateRunsAsync(List, TargetAlgorithmEvaluatorCallback, TargetAlgorithmEvaluatorRunObserver)}
	 * Cleanup: After callback is fired remove the key from map
	 */
	private final ConcurrentHashMap<EvaluationRequestToken, List<RunConfig>> allRunConfigsForTokenMap = new ConcurrentHashMap<EvaluationRequestToken, List<RunConfig>>();
	
	
	/**
	 * This map is used to determine which callbacks need to be notified on an individual run completion
	 * 
	 * Populated: on call to {@link #evaluateRunsAsync(List, TargetAlgorithmEvaluatorCallback, TargetAlgorithmEvaluatorRunObserver)}
	 * Cleanup: During callback firing the inner set has elements removed, the outter map is never cleaned up
	 */
	private final ConcurrentHashMap<RunConfig, Set<EvaluationRequestToken>> runConfigToTokenMap = new ConcurrentHashMap<RunConfig, Set<EvaluationRequestToken>>();
	
	/**
	 * This map is used to map an individual request token to the callback to be notified (the same callback could be used for multiple requests)
	 * 
	 * Populated: on call to {@link #evaluateRunsAsync(List, TargetAlgorithmEvaluatorCallback, TargetAlgorithmEvaluatorRunObserver)}
	 * Cleanup: During callback firing the token is removed.
	 */
	private final ConcurrentHashMap<EvaluationRequestToken, TargetAlgorithmEvaluatorCallback> evalRequestToCallbackMap = new ConcurrentHashMap<EvaluationRequestToken, TargetAlgorithmEvaluatorCallback>();
	
	
	/**
	 * This map is used to map an individual request token to the observer that should be notified (the same observer could be used for multiple requests)
	 * Populated: When entries are completed in {@link #evaluateRunsAsync(List, TargetAlgorithmEvaluatorCallback, TargetAlgorithmEvaluatorRunObserver)}
	 * Clreanup: During callback firing the token is removed
	 */
	private final ConcurrentHashMap<EvaluationRequestToken, TargetAlgorithmEvaluatorRunObserver> evalRequestToObserverMap = new ConcurrentHashMap<EvaluationRequestToken, TargetAlgorithmEvaluatorRunObserver>();
	
	
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
	
	
	
	/**
	 * Single instance of the null observer
	 */
	private static final NullTargetAlgorithmEvaluatorRunObserver NULL_OBSERVER = new NullTargetAlgorithmEvaluatorRunObserver();
	
	/**
	 * 
	 */
	private final ConcurrentHashMap<RunConfig, KillableAlgorithmRun> runConfigToLiveLatestStatusMap = new ConcurrentHashMap<RunConfig, KillableAlgorithmRun>();
	
	public CachingTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae) {
		super(tae);
		
		for(int i=0; i < AVAILABLE_PROCESSORS; i++)
		{
			submissionExecService.execute(new RunSubmitter());
			callbackExecService.execute(new CallbackInvoker());
			observerExecService.execute(new ObserverInvoker());
		}
	
	}

	
	
	@Override
	public void evaluateRunsAsync(final List<RunConfig> rcs, final TargetAlgorithmEvaluatorCallback callback, TargetAlgorithmEvaluatorRunObserver observer) 
	{

		if(rcs.isEmpty())
		{
			callback.onSuccess(Collections.<AlgorithmRun> emptyList());
			return;
		}
		
		
		final EvaluationRequestToken evalToken = new EvaluationRequestToken();
		
		evalRequestToCallbackMap.put(evalToken, callback);
		
		
		if(observer == null)
		{
			observer = NULL_OBSERVER;
		}
		
		final TargetAlgorithmEvaluatorRunObserver obs = observer;
		
		evalRequestToObserverMap.put(evalToken, obs);
		
		final List<RunConfig> runConfigs = Collections.unmodifiableList(rcs);
		
		List<RunConfig> myRCsToSubmit = new ArrayList<RunConfig>(runConfigs.size());
		
		Set<RunConfig> outstandingRunsForCallback = Collections.newSetFromMap(new ConcurrentHashMap<RunConfig, Boolean>());
		outstandingRunsForCallback.addAll(runConfigs);
		
		outstandingRunsForTokenMap.put(evalToken, outstandingRunsForCallback);
		outstandingRunsCountForTokenMap.put(evalToken,  new AtomicInteger(rcs.size()));
		allRunConfigsForTokenMap.put(evalToken, runConfigs);
		
		CountDownLatch countDownLatch = new CountDownLatch(1);
		//Try adding a new count down latch counter until it succeeds
		
		Set<EvaluationRequestToken> callbacksForRunConfig = Collections.newSetFromMap(new ConcurrentHashMap<EvaluationRequestToken,Boolean>());
		
		for(RunConfig rc : runConfigs )
		{	
			
			
			
			
			
			CountDownLatch value = cachedElements.putIfAbsent(rc, countDownLatch);
			
			//New value inserted
			if(value == null)
			{
				myRCsToSubmit.add(rc);
				
				//Create a new CountDownLatch only when needed.
				countDownLatch = new CountDownLatch(1);
				
				runConfigToLiveLatestStatusMap.putIfAbsent(rc, new RunningAlgorithmRun(rc, 0, 0, 0, rc.getProblemInstanceSeedPair().getSeed(), 0, new NullKillHandler()));
				
			}
			
			Set<EvaluationRequestToken> oSet =  runConfigToTokenMap.putIfAbsent(rc, callbacksForRunConfig);
			
			if(oSet == null)
			{
				oSet = callbacksForRunConfig;
				//Create a new set only when needed.
				callbacksForRunConfig = Collections.newSetFromMap(new ConcurrentHashMap<EvaluationRequestToken,Boolean>());
			} 
			
			oSet.add(evalToken);
		}
		
		int requests = cacheRequests.addAndGet(runConfigs.size());
		int misses = cacheRequestMisses.addAndGet(myRCsToSubmit.size());
		NumberFormat nf = NumberFormat.getPercentInstance();
		
		log.debug("Cache Local misses: {}, Local request: {},  Global misses {}, Global Requests {}, Hit Rate {} ", myRCsToSubmit.size(), runConfigs.size(),  misses, requests, nf.format( ((double) requests - misses) / requests)  );
		if(myRCsToSubmit.size() > 0)
		{
			submissionQueue.add(myRCsToSubmit);
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
				removeRC(evalToken,rc);
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
	private final void removeRC(EvaluationRequestToken token, RunConfig rc)
	{
		
		Set<RunConfig> outstandingRunsForCallback = outstandingRunsForTokenMap.get(token);
		
		if(outstandingRunsForCallback == null)
		{
			return;
		}
		
		boolean rcRemoved = outstandingRunsForCallback.remove(rc);
		
		if(rcRemoved)
		{
			int remaining = outstandingRunsCountForTokenMap.get(token).decrementAndGet();
			
			if(remaining < 0)
			{
				log.error("Desynchronization detected as the number of remaining elements seems to be less than zero for token: {} and rc: {}", token, rc);
			}
			if(remaining == 0)
			{
				//log.debug("Callback {} scheduled for invocation", callback);
				try {
					//We don't submit here in case there are many callbacks that are waiting
					
					tokenToCallbackQueue.add(token);
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
			
			final SubmissionObserver tObs = new SubmissionObserver();
			while(true)
			{
				List<RunConfig> rcs;
				try {
					rcs = submissionQueue.take();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
				
				tae.evaluateRunsAsync(rcs, new SubmissionOnCompleteHandler(rcs), tObs);
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
				for(EvaluationRequestToken t : runConfigToTokenMap.get(rc))
				{
					
					removeRC(t,rc);
				}
			}
		}

	}

	
	
	/**
	 * Updates the data structures with latest run information
	 * 
	 * 
	 * NOTE THIS OBJECT MUST BE IMMUTABLE BECAUSE IT IS SHARED BETWEEN MANY THREADS
	 */
	@ThreadSafe
	private class SubmissionObserver implements TargetAlgorithmEvaluatorRunObserver
	{

		@Override
		public void currentStatus(List<? extends KillableAlgorithmRun> runs)
		{
			for(KillableAlgorithmRun run : runs)
			{
				RunConfig rc = run.getRunConfig();
				runConfigToLiveLatestStatusMap.put(rc, new RunningAlgorithmRun(rc, run.getRuntime(), run.getRunLength(), run.getQuality(), run.getResultSeed(), run.getWallclockExecutionTime(), new NullKillHandler()));
				
				for(EvaluationRequestToken token : runConfigToTokenMap.get(rc))
				{
					token.updatedRuns();
					tokenToObserverQueue.add(token);
					
				}
			}
			
			
		}
		
	}
	
	/**
	 * Runnable that notifies observers
	 * @author Steve Ramage <seramage@cs.ubc.ca>
	 *
	 */
	private class ObserverInvoker implements Runnable
	{
		public void run()
		{
			while(true)
			{
				EvaluationRequestToken token;
				try {
					token = tokenToObserverQueue.take();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
					
					TargetAlgorithmEvaluatorRunObserver obs = evalRequestToObserverMap.get(token);
					
					if(obs == null)
					{
						continue;
					}
				
					boolean lockAcquired = token.tryLock();
					
					if(lockAcquired)
					{
						try 
						{
							//Observer can only fire before the callabck
							//callback sets this before acquiring lock, we have the lock so 
							//callback will wait for us.
							if(token.callbackFired())
							{
								continue;
							}
							
							//Token controls last time stamp we notified, so that we don't send duplicate updates
							if(!token.shouldNotify())
							{
								continue;
							}
							
							try {
								
								List<RunConfig> rcs = allRunConfigsForTokenMap.get(token);
								
								List<KillableAlgorithmRun> runs = new ArrayList<KillableAlgorithmRun>(rcs.size());
								
								for(RunConfig rc : rcs)
								{
									
									KillableAlgorithmRun krun;
									if(!completedRunConfigs.contains(rc))
									{
									
										
										krun = runConfigToLiveLatestStatusMap.get(rc);
									} else
									{
										 krun = new KillableWrappedAlgorithmRun(completedRunsMap.get(rc));
									}
									
									runs.add(krun); 
								}
								
								
								
								
								obs.currentStatus(runs);
								
							} catch(RuntimeException e)
							{
								log.error("Exception occurred while notifying observer",e);
								e.printStackTrace();
							}
						} finally
						{
							token.unlock();
						}
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
				EvaluationRequestToken token;
				try {
					token = tokenToCallbackQueue.take();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
					//Set the fired flag before we get the lock. Once we have the lock, we know observers get the lock first and then check the flag.
					token.fireCallback();
					token.lock();
					try 
					{
						TargetAlgorithmEvaluatorCallback callback = evalRequestToCallbackMap.get(token);
						try {
							
							List<RunConfig> rcs = allRunConfigsForTokenMap.get(token);
							
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
								
								runConfigToTokenMap.get(rc).remove(token);
							}
							
							
							callback.onSuccess(runs);
							
						} catch(RuntimeException e)
						{
							callback.onFailure(e);
						}
					} finally
					{
						
						allRunConfigsForTokenMap.remove(token);
						outstandingRunsCountForTokenMap.remove(token);
						
						evalRequestToCallbackMap.remove(token);
						//Remove this last so that other calls to removeRC() don't throw an NPE
						outstandingRunsForTokenMap.remove(token);
						token.unlock();
					}
			}
		}
		
	}
	
	/**
	 * Anonymous token class that essentially allows us to differentiate between requests with the same callback
	 * 
	 * @author Steve Ramage <seramage@cs.ubc.ca>
	 *
	 */
	private static final class EvaluationRequestToken
	{
		
		//Anonymous Token object
		
		private final ReentrantLock lock = new ReentrantLock();
		
		private final AtomicBoolean callbackFired = new AtomicBoolean();
		
		
		private final AtomicLong lastChangeUpdate = new AtomicLong(0);
		
		private final AtomicLong lastNotification = new AtomicLong(0);
		
		public boolean callbackFired()
		{
			return callbackFired.get();
		}
		
		public void fireCallback()
		{
			callbackFired.set(true);
		}
		
		public boolean tryLock()
		{
			return lock.tryLock();
		}
		
		public void lock()
		{
			lock.lock();
		}
		
		public void unlock()
		{
			lock.unlock();
		}
		
		public void updatedRuns()
		{
			lastChangeUpdate.set(System.currentTimeMillis());
		}
		
		public boolean shouldNotify()
		{
			
			long lastNotifyTime = lastNotification.get();
			while(true)
			{
				
				long lastChange = lastChangeUpdate.get();
				
				
				if(lastNotifyTime < lastChange)
				{
					long currentTime = System.currentTimeMillis();
					
					if(lastNotification.compareAndSet(lastNotifyTime, currentTime))
					{
						return true;
					} else
					{
						continue;
					}
					
				} else
				{
					return false;
				}
				
			}
			
			
			
		}
	}

	private final static class  NullTargetAlgorithmEvaluatorRunObserver implements TargetAlgorithmEvaluatorRunObserver
	{

		@Override
		public void currentStatus(List<? extends KillableAlgorithmRun> runs)
		{
			//NOOP
		}
		
	}
	
	
	/**
	 * Doesn't do anything
	 * @author Steve Ramage <seramage@cs.ubc.ca>
	 *
	 */
	private final static class NullKillHandler implements KillHandler
	{

		@Override
		public void kill() {
			//NOOP for the moment
			
		}

		@Override
		public boolean isKilled() {
			return false;
		}
		
	}
	
	
}
