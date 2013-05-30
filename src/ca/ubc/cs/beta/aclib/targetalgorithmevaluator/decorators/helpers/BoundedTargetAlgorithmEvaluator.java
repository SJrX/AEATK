package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractAsyncTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorHelper;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;
/**
 * Ensures that a Target Algorithm Evaluator gets no more than a certain number of runs issued simultaneously.
 * <p>
 * <b>Implementation Details:</b> This class is particularly tricky with regards to it's synchronization.
 * Effectively we hand the wrapped TargetAlgorithmEvaluator slices of the list we want evaluated, and change the handlers and the observer to update objects here.
 * <p>
 * In the case of updating the observer, we update the shared datastructure of ALL the runs and then notify the client's observer.
 * <p>
 * In the case of processing the callback, we update wait for all the outstanding runs to be completed, and then execute the client's callback. 
 * <p>
 * <b>Thread Safety:</b> All concurrent requests are serialized via the fair <code>enqueueLock</code> object. Callback and Observers use the runConfig object as a Mutex to prevent concurrent access.
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
@ThreadSafe
public class BoundedTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	
	private final ReentrantLock enqueueLock = new ReentrantLock(true);
	
	private final FairMultiPermitSemaphore availableRuns;

	private final AlgorithmExecutionConfig execConfig;
	
	private final static Logger log = LoggerFactory.getLogger(BoundedTargetAlgorithmEvaluator.class);

	public BoundedTargetAlgorithmEvaluator(TargetAlgorithmEvaluator tae, int numberOfConcurrentRuns, AlgorithmExecutionConfig execConfig) {
		super(tae);
		//this.availableRuns = new Semaphore(numberOfConcurrentRuns);
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
		
		try {
			enqueueLock.lock();
			
			//==== Stores the order of RunConfigs to put in all lists we return to the caller.
			final Map<RunConfig, Integer> orderOfRuns = new ConcurrentHashMap<RunConfig, Integer>();
			
			//Stores the completed runs
			final Set<AlgorithmRun> completedRuns = Collections.newSetFromMap(new ConcurrentHashMap<AlgorithmRun, Boolean>());
			
			//=== Stores outstanding runs 
			final Map<RunConfig, KillableAlgorithmRun> outstandingRuns  = new ConcurrentHashMap<RunConfig, KillableAlgorithmRun>();
			
			//=== Stores the kill handlers for each run
			final Map<RunConfig, KillHandler> killHandlers = new ConcurrentHashMap<RunConfig, KillHandler>();
			for(int i=0; i < runConfigs.size(); i++)
			{
				RunConfig rc = runConfigs.get(i);
				orderOfRuns.put(rc, i);
				KillHandler kh  = new StatusVariableKillHandler();
				killHandlers.put(rc, kh);
				outstandingRuns.put(runConfigs.get(i),new RunningAlgorithmRun(execConfig, rc, "RUNNING,0,0,0,"+ rc.getProblemInstanceSeedPair().getSeed() , kh));
			}
			
			
			
			TargetAlgorithmEvaluatorRunObserver updateMapObserver = new TargetAlgorithmEvaluatorRunObserver()
			{

				@Override
				public void currentStatus(List<? extends KillableAlgorithmRun> runs) {
					
					if(obs == null)
					{
						return;
					}
					synchronized(runConfigs)
					{
						final Map<RunConfig, KillableAlgorithmRun> outstandingRuns2 = outstandingRuns;
						for(KillableAlgorithmRun run : runs)
						{
							outstandingRuns.put(run.getRunConfig(),run);
						}
						
						List<KillableAlgorithmRun> allRunsForCaller = new ArrayList<KillableAlgorithmRun>();
						
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
						
						
						obs.currentStatus(allRunsForCaller);
						
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
				
				tae.evaluateRunsAsync(subList, new TargetAlgorithmEvaluatorCallback()
				{

					@Override
					public void onSuccess(List<AlgorithmRun> runs) {
						
						
						availableRuns.releasePermits(rcToRun);
						
						//=== Mutex on the runConfig to prevent multiple calls to onSuccess()
						synchronized(runConfigs)
						{
							if(failureOccured.get())
							{
								log.debug("Failure occured, silently discarding runs");
								return;
							} 
								
							completedRuns.addAll(runs);
							
							
							if(totalRunsNeeded == completedRuns.size())
							{
								List<AlgorithmRun> allRuns = new ArrayList<AlgorithmRun>(completedRuns.size());
								
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
								handler.onSuccess(allRuns);
							} 
						}
						
						
					}

					@Override
					public void onFailure(RuntimeException t) {
						availableRuns.releasePermits(rcToRun);
					
						failureOccured.set(true);
						handler.onFailure(t);
				
						
					}
					
					
					
				}, updateMapObserver);
			
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
}
