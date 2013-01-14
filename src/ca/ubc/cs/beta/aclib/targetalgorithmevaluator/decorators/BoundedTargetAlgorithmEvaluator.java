package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.concurrent.FairMultiPermitSemaphore;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.AbstractDeferredTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.TAECallback;
/**
 * Ensures that a Target Algorithm Evaluator gets no more than a certain number of runs issued simultaneously.
 * 
 * 
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class BoundedTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	
	private final ReentrantLock enqueueLock = new ReentrantLock(true);
	
	private final FairMultiPermitSemaphore availableRuns;
	
	private final static Logger log = LoggerFactory.getLogger(BoundedTargetAlgorithmEvaluator.class);

	public BoundedTargetAlgorithmEvaluator(TargetAlgorithmEvaluator tae, int numberOfConcurrentRuns) {
		super(tae);
		//this.availableRuns = new Semaphore(numberOfConcurrentRuns);
		if(numberOfConcurrentRuns <= 0) throw new IllegalArgumentException("Must be able to schedule at least one run");
		this.availableRuns = new FairMultiPermitSemaphore(numberOfConcurrentRuns);
	}

	@Override
	public void evaluateRunsAsync(RunConfig runConfig, TAECallback handler) {
		this.evaluateRunsAsync(Collections.singletonList(runConfig), handler, null);
	}

	@Override
	public void evaluateRunsAsync(final List<RunConfig> runConfigs, final TAECallback handler) {
		evaluateRunsAsync(runConfigs, handler, null);
	}

	@Override
	public void evaluateRunsAsync(final List<RunConfig> runConfigs, final TAECallback handler, CurrentRunStatusObserver obs) {

		if(runConfigs.isEmpty())
		{
			handler.onSuccess(Collections.<AlgorithmRun> emptyList());
			return;
		}
		
		try {
			enqueueLock.lock();
			
			//Stores order of runs in resulting list
			final Map<RunConfig, Integer> orderOfRuns = new ConcurrentHashMap<RunConfig, Integer>();
			
			//Stores the runs
			final Set<AlgorithmRun> completedRuns = Collections.newSetFromMap(new ConcurrentHashMap<AlgorithmRun, Boolean>());
			
			for(int i=0; i < runConfigs.size(); i++)
			{
				orderOfRuns.put(runConfigs.get(i), i);
			}
			
			
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
				
				tae.evaluateRunsAsync(subList, new TAECallback()
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
					
					
					
				}, obs);
			
				numberOfDispatchedRuns+=rcToRun;
			}
		} finally {
			enqueueLock.unlock();
		}

	}
	
	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig run) {
		return this.evaluateRun(Collections.singletonList(run));
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		return evaluateRun(runConfigs, null);
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, CurrentRunStatusObserver obs) {
		return AbstractDeferredTargetAlgorithmEvaluator.evaluateRunSyncToAsync(runConfigs, this, obs);
	}
}
