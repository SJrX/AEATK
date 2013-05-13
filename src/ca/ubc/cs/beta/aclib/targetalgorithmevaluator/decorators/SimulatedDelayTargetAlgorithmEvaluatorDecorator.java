package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.algorithmrun.RunningAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillHandler;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableWrappedAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.StatusVariableKillHandler;
import ca.ubc.cs.beta.aclib.exceptions.TargetAlgorithmAbortException;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.TAECallback;

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

public class SimulatedDelayTargetAlgorithmEvaluatorDecorator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	
	private long observerFrequencyMs;

	private final ExecutorService execService = Executors.newCachedThreadPool();;
	
	public SimulatedDelayTargetAlgorithmEvaluatorDecorator(
			TargetAlgorithmEvaluator tae, long observerFrequency) {
		super(tae);
		this.observerFrequencyMs = observerFrequency;
	}

	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig run) {
		return this.evaluateRun(Collections.singletonList(run));
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		return this.evaluateRun(runConfigs, null);
	}


	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, CurrentRunStatusObserver obs) {
		return evaluateRunConfigs(runConfigs, obs, new CountDownLatch(0));
	}

	@Override
	public void evaluateRunsAsync(RunConfig runConfig, TAECallback handler) {
		this.evaluateRunsAsync(Collections.singletonList(runConfig), handler);
	}

	@Override
	public void evaluateRunsAsync(List<RunConfig> runConfigs, final TAECallback handler) {
		this.evaluateRunsAsync(runConfigs, handler, null);
	}

	@Override
	public void evaluateRunsAsync(final List<RunConfig> runConfigs,
			final TAECallback handler, final CurrentRunStatusObserver obs) {
	
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
	
	
	/**
	 * Evaluate the runConfigs, and notify the observer as appropriate
	 * @param runConfigs 		runConfigs
	 * @param obs				observer
	 * @param asyncReleaseLatch	latch that we will decrement if we sleep (this is used for async evaluation)
	 * @return
	 */
	private List<AlgorithmRun> evaluateRunConfigs(List<RunConfig> runConfigs, CurrentRunStatusObserver obs, CountDownLatch asyncReleaseLatch)
	{
		
		long startTimeInMs = System.currentTimeMillis();
		//We don't pass the Observer to the decorated TAE because it might report too much too soon.
		//We also make this list unmodifiable so that we don't accidentally tamper with it.
		
		final List<AlgorithmRun> measuredRuns = Collections.unmodifiableList(tae.evaluateRun(runConfigs, null));
		
		double maxRuntime = Double.NEGATIVE_INFINITY;
		final Map<RunConfig, AlgorithmRun> runResults = new HashMap<RunConfig, AlgorithmRun>();
		
		
		final Map<RunConfig, KillHandler> khs = new HashMap<RunConfig, KillHandler>();
		
		for(AlgorithmRun run : measuredRuns)
		{
			maxRuntime = Math.max(maxRuntime, Math.max(run.getRuntime(), run.getWallclockExecutionTime()));
			khs.put(run.getRunConfig(), new StatusVariableKillHandler() );
			runResults.put(run.getRunConfig(), new RunningAlgorithmRun(run.getExecutionConfig(), run.getRunConfig(), 0,0,0, run.getRunConfig().getProblemInstanceSeedPair().getSeed(), null));
		}
		
		
		long waitTimeRemainingMs;
		do {
			long currentTimeInMs =  System.currentTimeMillis();
			waitTimeRemainingMs =  startTimeInMs  +  (long) maxRuntime*1000 - currentTimeInMs;
			
			if(obs != null && waitTimeRemainingMs > observerFrequencyMs)
			{
				
				List<KillableAlgorithmRun> kars = new ArrayList<KillableAlgorithmRun>(measuredRuns.size());
				//Update observer
				for(AlgorithmRun run : measuredRuns)
				{
				
					RunConfig rc  = run.getRunConfig();
					if(runResults.get(rc).isRunCompleted())
					{
						continue;
					}
					
					double currentRuntime = (currentTimeInMs - startTimeInMs) / 1000.0;
					if(currentRuntime>  run.getRuntime())
					{
						//We are done and can simply throw this run on the list
						runResults.put(rc, run);
					} else if(khs.get(rc).isKilled())
					{
						//We should kill this run
						runResults.put(rc, new ExistingAlgorithmRun(run.getExecutionConfig(), rc, RunResult.TIMEOUT, currentRuntime, 0, 0, rc.getProblemInstanceSeedPair().getSeed(),currentTimeInMs - startTimeInMs));
					} else
					{
						//Update the run
						runResults.put(rc, new RunningAlgorithmRun(run.getExecutionConfig(), run.getRunConfig(), currentRuntime,0,0, run.getRunConfig().getProblemInstanceSeedPair().getSeed(), khs.get(rc)));
					}
					
					AlgorithmRun currentRun = runResults.get(rc);
					if( currentRun instanceof KillableAlgorithmRun)
					{
						kars.add((KillableAlgorithmRun) currentRun);
					} else
					{
						kars.add(new KillableWrappedAlgorithmRun(currentRun));
					}
					
					obs.currentStatus(kars);
				}	
			}
			
			long sleepTime = Math.min(observerFrequencyMs, waitTimeRemainingMs);
			
			if(sleepTime > 0)
			{
				try {
					asyncReleaseLatch.countDown();
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					//We are interrupted we are just going to return the measured runs
					throw new TargetAlgorithmAbortException(e);
				}
			
			}
		}
		while( waitTimeRemainingMs > 0);
		
		return measuredRuns;
	}

	@Override
	public void notifyShutdown()
	{
		tae.notifyShutdown();
		this.execService.shutdown();
	}
}
