package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunningAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillHandler;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.StatusVariableKillHandler;
import ca.ubc.cs.beta.aclib.concurrent.FairMultiPermitSemaphore;
import ca.ubc.cs.beta.aclib.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorHelper;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;
/**
 * Ensures that a Target Algorithm Evaluator gets no more than a certain number of runs issued simultaneously.
 * <p>
 * <b>Implementation Details:</b> This class is particularly tricky with regards to it's synchronization.
 * Effectively we hand the wrapped TargetAlgorithmEvaluator slices of the List<RunConfig> we want evaluated, and change the handlers and the observer to update objects here.
 * <p>
 * In the case of updating the observer, we update the shared data structure of ALL the runs and then notify the client's observer.
 * <p>
 * In the case of processing the callback, we update for all the outstanding runs to be completed, and then execute the client's callback. 
 * <p>
 * <b>Thread Safety:</b> All concurrent requests are serialized via the fair <code>enqueueLock</code> object. Callback and Observers use the runConfig object as a Mutex to prevent concurrent access.
 * Callback and Observers are notified via a new thread. This prevents evaluateRunAsync from being entered in twice by the same thread. 
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
@ThreadSafe
public class BoundedTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	
	/**
	 * Used to serialize requests (so that each run is processed completely in serial instead of being interleaved with other runs)
	 */
	private final ReentrantLock enqueueLock = new ReentrantLock(true);
	
	/**
	 * Stores the number of permitted runs that can go past this TAE at any time
	 */
	private final FairMultiPermitSemaphore availableRuns;

	/**
	 * The Execution Config we are executing for
	 */
	private final AlgorithmExecutionConfig execConfig;
	
	private final static Logger log = LoggerFactory.getLogger(BoundedTargetAlgorithmEvaluator.class);

	
	private final ExecutorService execService = Executors.newCachedThreadPool(new SequentiallyNamedThreadFactory("Bounded Target Algorithm Evaluator Callback Thread"));
	
	public BoundedTargetAlgorithmEvaluator(TargetAlgorithmEvaluator tae, int numberOfConcurrentRuns, AlgorithmExecutionConfig execConfig) {
		super(tae);
		if(numberOfConcurrentRuns <= 0) throw new IllegalArgumentException("Must be able to schedule at least one run");
		this.availableRuns = new FairMultiPermitSemaphore(numberOfConcurrentRuns);
		this.execConfig = execConfig;
	}



	@Override
	public void evaluateRunsAsync(final List<RunConfig> runConfigs, final TargetAlgorithmEvaluatorCallback handler, final TargetAlgorithmEvaluatorRunObserver obs) {

		if(runConfigs.isEmpty())
		{
			handler.onSuccess(Collections.<AlgorithmRun> emptyList());
			return;
		}
		
		if(this.enqueueLock.isHeldByCurrentThread())
		{
			//This is a paranoid check to ensure 
			throw new IllegalStateException("Current Thread already holds the lock");
		}
		try {	
			enqueueLock.lock();
			
			//==== Stores the order of RunConfigs to put in all lists we return to the caller.
			final Map<RunConfig, Integer> orderOfRuns = new ConcurrentHashMap<RunConfig, Integer>();
			
			//Stores the completed runs
			final Set<AlgorithmRun> completedRuns = Collections.newSetFromMap(new ConcurrentHashMap<AlgorithmRun, Boolean>());
			
			//=== Stores outstanding runs 
			final Map<RunConfig, KillableAlgorithmRun> outstandingRuns  = new ConcurrentHashMap<RunConfig, KillableAlgorithmRun>();
			
			//=== Stores the kill handlers for each run
			//Kill Handlers will keep track of which runs have been requested to be killed
			final Map<RunConfig, KillHandler> killHandlers = new ConcurrentHashMap<RunConfig, KillHandler>();
			for(int i=0; i < runConfigs.size(); i++)
			{
				RunConfig rc = runConfigs.get(i);
				orderOfRuns.put(rc, i);
				KillHandler kh  = new StatusVariableKillHandler();
				killHandlers.put(rc, kh);
				outstandingRuns.put(runConfigs.get(i),new RunningAlgorithmRun(execConfig, rc, 0,0,0,rc.getProblemInstanceSeedPair().getSeed() , kh));
			}
			
			//Observer maps
			final AtomicBoolean completionCallbackFired = new AtomicBoolean(false);
			TargetAlgorithmEvaluatorRunObserver updateMapObserver = new BoundedTargetAlgorithmEvaluatorMapUpdateObserver(runConfigs, obs, outstandingRuns, orderOfRuns, killHandlers, completionCallbackFired,execService);
			
			final int totalRunsNeeded = runConfigs.size();
			
			int numberOfDispatchedRuns = 0;
			
			final AtomicBoolean failureOccured = new AtomicBoolean(false);
			
			
			
			
			while((numberOfDispatchedRuns < runConfigs.size()) && !failureOccured.get())
			{
				int oRcToRun;
				try {
					oRcToRun = availableRuns.getUpToNPermits(runConfigs.size()-numberOfDispatchedRuns);
				} catch (InterruptedException e) {
					//=== We can just return from this method  
					Thread.currentThread().interrupt();
					return;
				}
				final int rcToRun = oRcToRun;
	
				
				Object[] logMsg = {runConfigs.size()-numberOfDispatchedRuns, rcToRun,numberOfDispatchedRuns};
				log.debug("Asked for permission to run {} things, got permission to run {} things, total completed for this batch {} " , logMsg );
				
				List<RunConfig> subList = runConfigs.subList(numberOfDispatchedRuns, numberOfDispatchedRuns+rcToRun);
				
				TargetAlgorithmEvaluatorCallback callBack = new SubListTargetAlgorithmEvaluatorCallback(availableRuns, rcToRun, runConfigs, handler, failureOccured, totalRunsNeeded, completedRuns, orderOfRuns, completionCallbackFired, execService);
				
				tae.evaluateRunsAsync(subList, callBack, updateMapObserver);
			
				numberOfDispatchedRuns+=rcToRun;
			}
		} finally {
			enqueueLock.unlock();
		}

	}
	

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		return TargetAlgorithmEvaluatorHelper.evaluateRunSyncToAsync(runConfigs, this, obs);
	}
	
	

	/**
	 * We need to throw this now because even if the lower level supplies it, we may break it.
	 * @throws UnsupportedOperationException - if the TAE does not support this operation 
	 */
	@Override
	public void waitForOutstandingEvaluations()
	{
		throw new UnsupportedOperationException(this.getClass().getCanonicalName() + " does NOT support waiting or observing the number of outstanding evaluations, even if the wrapper class does, you should probably wrap this TargetAlgorithmEvaluator with an instance of " + OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator.class );
	}
	
	/**
	 * We need to throw this now because even if the lower level supplies it, we may break it.
	 */
	@Override
	public int getNumberOfOutstandingEvaluations()
	{
		throw new UnsupportedOperationException(this.getClass().getCanonicalName() + " does NOT support waiting or observing the number of outstanding evaluations, even if the wrapper class does, you should probably wrap this TargetAlgorithmEvaluator with an instance of " + OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator.class );
	}
	
	@Override
	public void notifyShutdown()
	{
		this.execService.shutdown();
		tae.notifyShutdown();
		
		try {
			this.execService.awaitTermination(365, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			log.warn("Thread shutdown interrupted");
			Thread.currentThread().interrupt();
			return;
		}
		
	}
	
	
	/**
	* Observer that keeps track of the table status and forwards the observation calls to the client
	 */
	static class BoundedTargetAlgorithmEvaluatorMapUpdateObserver implements TargetAlgorithmEvaluatorRunObserver
	{

		private final List<RunConfig> runConfigs;
		private final TargetAlgorithmEvaluatorRunObserver callerRunObserver;
		private final Map<RunConfig, KillableAlgorithmRun> outstandingRuns;
		private final Map<RunConfig, Integer> orderOfRuns;
		private final Map<RunConfig, KillHandler> killHandlers;
		private final AtomicBoolean completedCallbackFired;
		private ExecutorService cachedThreadPool;
		
		
		BoundedTargetAlgorithmEvaluatorMapUpdateObserver(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver callerRunObserver, Map<RunConfig, KillableAlgorithmRun> outstandingRuns, Map<RunConfig, Integer> orderOfRuns, Map<RunConfig, KillHandler> killHandlers, AtomicBoolean onSuccessFired, ExecutorService cachedThreadPool)
		{
			this.runConfigs = runConfigs;
			this.callerRunObserver = callerRunObserver;
			this.outstandingRuns = outstandingRuns;
			this.orderOfRuns = orderOfRuns;
			this.killHandlers = killHandlers;
			this.completedCallbackFired = onSuccessFired;
			this.cachedThreadPool = cachedThreadPool;
			
			
		}
		
		/**
		 * Updates the table of runs 
		 */
		@Override
		public void currentStatus(List<? extends KillableAlgorithmRun> runs) {
			
			if(callerRunObserver == null)
			{
				return;
			}
			synchronized(runConfigs)
			{
				if(this.completedCallbackFired.get())
				{
					//Success already fired 
					return;
				}
				
				for(KillableAlgorithmRun run : runs)
				{
					outstandingRuns.put(run.getRunConfig(),run);
				}
				
				final List<KillableAlgorithmRun> allRunsForCaller = new ArrayList<KillableAlgorithmRun>();
				
				if(runConfigs.size() != outstandingRuns.size())
				{
					throw new IllegalStateException("Expected " + runConfigs.size() + " to equal " + outstandingRuns.size());
				}
				
				for(int i=0; i < runConfigs.size(); i++)
				{
					allRunsForCaller.add(runs.get(0));
				}
				
				for(int i=0; i < runConfigs.size(); i++)
				{
					KillableAlgorithmRun algoRun = outstandingRuns.get(runConfigs.get(i));
					allRunsForCaller.set(orderOfRuns.get(runConfigs.get(i)),algoRun);
				}
				
				//=== Invoke callback in another thread 
				cachedThreadPool.execute(new Runnable()
				{
					@Override
					public void run() {
						synchronized(runConfigs)
						{
							if(completedCallbackFired.get())
							{
								//Success already fired 
								return;
							}
							callerRunObserver.currentStatus(allRunsForCaller);
						}
					}
					
				});
				
				
				
				
				for(Entry<RunConfig,KillHandler> ent : killHandlers.entrySet())
				{
					if(ent.getValue().isKilled())
					{
						outstandingRuns.get(ent.getKey()).kill();
					}
				}
				
			}

		}
		
	};
	
	
	/**
	 * This callback updates the objects state upon completion and optionally fires the callback
	 */
	static class SubListTargetAlgorithmEvaluatorCallback implements TargetAlgorithmEvaluatorCallback
	{
		
		private final FairMultiPermitSemaphore availableRuns;
		private final int rcToRun;
		private final List<RunConfig> runConfigs;
		private final TargetAlgorithmEvaluatorCallback calleeCallback;
		private final AtomicBoolean failureOccured;
		private final Set<AlgorithmRun> completedRuns;
		private final int totalRunsNeeded;
		private final Map<RunConfig, Integer> orderOfRuns;
		private final AtomicBoolean completionCallbackFired;
		private final ExecutorService execService;

		public SubListTargetAlgorithmEvaluatorCallback(FairMultiPermitSemaphore availableRuns, int rcToRun, List<RunConfig> runConfigs, TargetAlgorithmEvaluatorCallback calleeCallback, AtomicBoolean failureOccured, int totalRunsNeeded, Set<AlgorithmRun> completedRuns, Map<RunConfig, Integer> orderOfRuns, AtomicBoolean onSuccessFired, ExecutorService execService)
		{
			this.availableRuns = availableRuns;
			this.rcToRun = rcToRun;
			this.runConfigs = runConfigs;
			this.calleeCallback = calleeCallback;
			this.failureOccured = failureOccured;
			this.completedRuns = completedRuns;
			this.totalRunsNeeded = totalRunsNeeded; 
			this.orderOfRuns = orderOfRuns;
			this.completionCallbackFired = onSuccessFired;
			this.execService = execService;
			
		}
		
		
		@Override
		public void onSuccess(List<AlgorithmRun> runs) {
			
			
			availableRuns.releasePermits(rcToRun);
			
			//=== Mutex on the runConfig to prevent multiple calls to onSuccess()
			synchronized(runConfigs)			
			{
				
				if(failureOccured.get())
				{
					this.completionCallbackFired.set(true);
					log.debug("Failure occured, silently discarding runs");
					return;
				} 
					
				completedRuns.addAll(runs);
				
				
				if(totalRunsNeeded == completedRuns.size())
				{
					this.completionCallbackFired.set(true);
					final List<AlgorithmRun> allRuns = new ArrayList<AlgorithmRun>(completedRuns.size());
					
					//=== We need the array to have everything in it before hand
					for(int i=0; i < completedRuns.size(); i++)
					{
						allRuns.add(runs.get(0));
					}
					
					
					for(AlgorithmRun run : completedRuns)
					{
						int index = orderOfRuns.get(run.getRunConfig());
						allRuns.set(index, run);
					}
					//==== Schedule callback in another thread
					//==== This is to prevent the onSuccess or observers from re-entering evaluateRun
					execService.execute(new Runnable()
					{
						@Override
						public void run() {
							synchronized(runConfigs)
							{
								try {
									calleeCallback.onSuccess(allRuns);
								} catch(RuntimeException e)
								{
									calleeCallback.onFailure(e);
								}
							}
							
						}
						
					});
					

				} 
			}
			
			
		}

		@Override
		public void onFailure(final RuntimeException t) {
			availableRuns.releasePermits(rcToRun);
			synchronized(runConfigs)
			{
				this.completionCallbackFired.set(true);
				if(failureOccured.get())
				{
					log.debug("Failure occured already, silently discarding subsequent failures");
				}
				failureOccured.set(true);
				
		
				execService.execute(new Runnable()
				{
					@Override
					public void run() {
						synchronized(runConfigs)
						{
							calleeCallback.onFailure(t);
						}
						
					}
					
				});
			}
		}
			
			
		
		
		
		
	}
	
}
