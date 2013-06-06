package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.helpers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.algorithmrun.RunningAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillHandler;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableWrappedAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.StatusVariableKillHandler;
import ca.ubc.cs.beta.aclib.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aclib.exceptions.TargetAlgorithmAbortException;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;

/**
 * Simulates the Delays and Observer information for TAEs that work nearly instantaneously.
 * <br>
 * <b>NOTE:</b>This Decorator assumes that all runs can execute simultaneously and so roughly the delay is given by the maximum runtime of any response.
 * If you need to otherwise serialize or limit these, you should wrap this decorator with the <code>BoundedTargetAlgorithmEvaluator</code>. 
 * <br>
 * <b>NOTE:</b>The instantaneous aspect of the above is KEY. This is written as if calls to evaluateRun() on the wrapped TAE are quick, and while it does start timing before it is invoked, asynchronous calls will become
 * very synchronous. Additionally we do not pass the observer to the wrapper TAE, so there will be no updates to the client or any kills until long after this is complete.
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
@ThreadSafe
public class SimulatedDelayTargetAlgorithmEvaluatorDecorator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	
	private final long observerFrequencyMs;

	//A cached thread pool is used here because another decorator will handle bounding the number of runs, if necessary.
	private final ExecutorService execService = Executors.newCachedThreadPool(new SequentiallyNamedThreadFactory("Simulated Delay Target Algorithm Evaluator Callback Thread"));
	
	private static final Logger log = LoggerFactory.getLogger(SimulatedDelayTargetAlgorithmEvaluatorDecorator.class);
	
	private final AtomicInteger threadsWaiting = new AtomicInteger(0);
	
	public SimulatedDelayTargetAlgorithmEvaluatorDecorator(
			TargetAlgorithmEvaluator tae, long observerFrequencyMs) {
		super(tae);
		this.observerFrequencyMs = observerFrequencyMs;
		if(observerFrequencyMs <= 0) throw new IllegalArgumentException("Observer Frequency cannot be zero");
	}


	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		 
		return evaluateRunConfigs(runConfigs, obs, new CountDownLatch(0));
		
	}


	@Override
	public void evaluateRunsAsync(final List<RunConfig> runConfigs,
			final TargetAlgorithmEvaluatorCallback handler, final TargetAlgorithmEvaluatorRunObserver obs) {
	
		final CountDownLatch latch = new CountDownLatch(1);
		execService.execute(new Runnable()
		{

			@Override
			public void run() {
				try {
					try { 
					handler.onSuccess(evaluateRunConfigs(runConfigs, obs, latch));
					} finally
					{
						latch.countDown();
					}
				} catch(RuntimeException e)
				{
					handler.onFailure(e);
				}
				
			}
			
		});
		
		try {
			//The latch fires if we sleep in evaluateRunConfigs
			//or if we complete the handler.
			//This roughly simulates the required behaivor that if the runs are done/fast we should invoke the callback before hand.
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
	}
	

	@Override
	public boolean areRunsObservable()
	{
		//We support a limited form of Observation
		return true;
	}
	
	@Override
	public boolean areRunsPersisted()
	{
		return false;
	}
	
	
	@Override
	public void notifyShutdown()
	{
		tae.notifyShutdown();
		this.execService.shutdown();
	}
	
	
	/**
	 * Evaluate the runConfigs, and notify the observer as appropriate
	 * @param runConfigs 		runConfigs
	 * @param obs				observer
	 * @param asyncReleaseLatch	latch that we will decrement if we sleep (this is used for async evaluation)
	 * @return
	 */
	private List<AlgorithmRun> evaluateRunConfigs(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs, CountDownLatch asyncReleaseLatch)
	{
		try {
			threadsWaiting.incrementAndGet();
			
			long startTimeInMs = System.currentTimeMillis();
			//We don't pass the Observer to the decorated TAE because it might report too much too soon.
			//We also make this list unmodifiable so that we don't accidentally tamper with it.
			
			Set<String> configIDs = new HashSet<String>();
			
			for(RunConfig rc : runConfigs)
			{
				configIDs.add(rc.getParamConfiguration().getFriendlyIDHex());
			}
			
			
			log.info("Scheduling runs synchronously for configs {}", configIDs);
			
			final List<AlgorithmRun> runsFromWrappedTAE = Collections.unmodifiableList(tae.evaluateRun(runConfigs, null));
			double timeToSleep = Double.NEGATIVE_INFINITY;
			//Stores a mapping of Run Config objects to Algorithm Run Objects
			//The kill handlers may modify these.
			final LinkedHashMap<RunConfig, AlgorithmRun> runConfigToAlgorithmRunMap = new LinkedHashMap<RunConfig, AlgorithmRun>();
			final LinkedHashMap<RunConfig, KillHandler> runConfigToKillHandlerMap = new LinkedHashMap<RunConfig, KillHandler>();
			
			for(AlgorithmRun run : runsFromWrappedTAE)
			{
				timeToSleep = Math.max(timeToSleep, Math.max(run.getRuntime(), run.getWallclockExecutionTime()));
				runConfigToKillHandlerMap.put(run.getRunConfig(), new StatusVariableKillHandler() );
				runConfigToAlgorithmRunMap.put(run.getRunConfig(), new RunningAlgorithmRun(run.getExecutionConfig(), run.getRunConfig(), 0,0,0, run.getRunConfig().getProblemInstanceSeedPair().getSeed(), null));
				
			}
			
			Object[] args = {  timeToSleep, configIDs, getNicelyFormattedWakeUpTime(timeToSleep), threadsWaiting.get()}; 
			log.debug("Simulating {} elapsed seconds of running for configs ({}) . Wake-up estimated in/at: {}  ( ~({}) threads currently waiting )", args);
			
			sleepAndNotifyObservers(startTimeInMs, timeToSleep, asyncReleaseLatch, obs, runsFromWrappedTAE, runConfigs, runConfigToKillHandlerMap, runConfigToAlgorithmRunMap);
			
			if(obs == null)
			{ 
				//None of the runResultsChanged so we can return them unmodified
				return runsFromWrappedTAE;
			} else
			{
				//Build a new list of run results based on how the map changed
				List<AlgorithmRun> completedRuns = new ArrayList<AlgorithmRun>(runsFromWrappedTAE.size());
				for(AlgorithmRun run : runsFromWrappedTAE)
				{
					
					AlgorithmRun newRun = runConfigToAlgorithmRunMap.get(run.getRunConfig());
					if(!newRun.isRunCompleted())
					{
						log.error("Expected all runs to be returned would be done by now, however this run isn't {}.  ", newRun );
						for(AlgorithmRun runFromTAE : runsFromWrappedTAE)
						{
							log.error("Response from TAE was this run {}", runFromTAE);
						}
						
						throw new IllegalStateException("Expected that all runs would be completed by now, but not all are");
					}
					completedRuns.add(newRun);
					
				}
				
				return completedRuns;
			}
			
		} finally
		{
			threadsWaiting.decrementAndGet();
		}
	}

	private void sleepAndNotifyObservers(long startTimeInMs, double maxRuntime, CountDownLatch asyncReleaseLatch, TargetAlgorithmEvaluatorRunObserver observer, List<AlgorithmRun> runsFromWrappedTAE, List<RunConfig> runConfigs, final LinkedHashMap<RunConfig, KillHandler> khs, final LinkedHashMap<RunConfig, AlgorithmRun> runResults)
	{
		
		long sleepTimeInMS = (long) maxRuntime * 1000;
		do {
			long waitTimeRemainingMs;
			
			long currentTimeInMs =  System.currentTimeMillis();
			if(observer != null)
			{
				updateRunsAndNotifyObserver(startTimeInMs, currentTimeInMs, maxRuntime, asyncReleaseLatch, observer, runsFromWrappedTAE, runConfigs, khs, runResults);
			}	
			
			//In case the observers took significant amounts of time, we get the time again
			currentTimeInMs =  System.currentTimeMillis();
			waitTimeRemainingMs =  startTimeInMs - currentTimeInMs +  sleepTimeInMS; 
		
			if(waitTimeRemainingMs <= 0)
			{
				break;
			} else
			{
				long sleepTime = Math.min(observerFrequencyMs, waitTimeRemainingMs);
				if(sleepTime > 0)
				{
					try {
						//Release the call to async
						asyncReleaseLatch.countDown();
						
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						
						//We are interrupted we are just going to return the measured runs
						throw new TargetAlgorithmAbortException(e);
					}
				
				} 	
			}
		}
		while( true );
		
		long currentTimeInMs =  System.currentTimeMillis();
		updateRunsAndNotifyObserver(startTimeInMs, currentTimeInMs, maxRuntime, asyncReleaseLatch, observer, runsFromWrappedTAE, runConfigs, khs, runResults);
			
	}
	
	private void updateRunsAndNotifyObserver(long startTimeInMs, long currentTimeInMs, double maxRuntime, CountDownLatch asyncReleaseLatch, TargetAlgorithmEvaluatorRunObserver observer, List<AlgorithmRun> runsFromWrappedTAE, List<RunConfig> runConfigs, final LinkedHashMap<RunConfig, KillHandler> killHandlers, final LinkedHashMap<RunConfig, AlgorithmRun> runConfigToAlgorithmRunMap)
	{

		List<KillableAlgorithmRun> kars = new ArrayList<KillableAlgorithmRun>(runsFromWrappedTAE.size());
		//Update the table
		
		for(AlgorithmRun run : runsFromWrappedTAE)
		{
		
			RunConfig rc  = run.getRunConfig();
			
			double currentRuntime = (currentTimeInMs - startTimeInMs) / 1000.0;
			if(runConfigToAlgorithmRunMap.get(rc).isRunCompleted())
			{
				//We don't need to do anything because the run is already done
			} else if(currentRuntime >=  run.getRuntime())
			{
				//We are done and can simply throw this run on the list
				runConfigToAlgorithmRunMap.put(rc, run);
			} else if(killHandlers.get(rc).isKilled())
			{
				//We should kill this run
				runConfigToAlgorithmRunMap.put(rc, new ExistingAlgorithmRun(run.getExecutionConfig(), rc, RunResult.KILLED, currentRuntime, 0, 0, rc.getProblemInstanceSeedPair().getSeed(),currentRuntime));
			} else
			{
				//Update the run
				runConfigToAlgorithmRunMap.put(rc, new RunningAlgorithmRun(run.getExecutionConfig(), run.getRunConfig(), currentRuntime,0,0, run.getRunConfig().getProblemInstanceSeedPair().getSeed(), killHandlers.get(rc)));
			}
			
			AlgorithmRun currentRun = runConfigToAlgorithmRunMap.get(rc);
			if( currentRun instanceof KillableAlgorithmRun)
			{
				kars.add((KillableAlgorithmRun) currentRun);
			} else
			{
				kars.add(new KillableWrappedAlgorithmRun(currentRun));
			}
			
		}	
		
		observer.currentStatus(kars);
	}
	
	
	
	private String getNicelyFormattedWakeUpTime(double timeToSleep)
	{
		long time = System.currentTimeMillis()  + (long) (timeToSleep * 1000);
		Date d = new Date(time);

		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
		String releaseTime = df.format(d);
		
		if(timeToSleep * 1000 > 86400000)
		{
			releaseTime =  timeToSleep * 1000 / 86400000 + " days " + releaseTime;
		}
		
		return releaseTime;
	}
	
	
}
