package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.util.concurrent.AtomicDouble;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.algorithmrun.RunningAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillHandler;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableWrappedAlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorHelper;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;

/**
 * Ensures that observed runtimes for RUNNING/KILLED runs always strictly increasing
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class StrictlyIncreasingRuntimesTargetAlgorithmEvaluatorDecorator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	
	private final ConcurrentHashMap<RunConfig, AtomicDouble> maxRuntimeObserved = new ConcurrentHashMap<RunConfig, AtomicDouble>();
	
	public StrictlyIncreasingRuntimesTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae) {
		super(tae);
	}

	@Override
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		return TargetAlgorithmEvaluatorHelper.evaluateRunSyncToAsync(runConfigs, this, obs);
	}

	@Override
	public final void evaluateRunsAsync(List<RunConfig> runConfigs,
			final TargetAlgorithmEvaluatorCallback oHandler, final TargetAlgorithmEvaluatorRunObserver obs) {

		
		
		
		
		
		for(RunConfig rc : runConfigs)
		{
			maxRuntimeObserved.put(rc, new AtomicDouble(0));
		}
		
	
		TargetAlgorithmEvaluatorCallback myHandler = new TargetAlgorithmEvaluatorCallback()
		{
			private final TargetAlgorithmEvaluatorCallback handler = oHandler;

			@Override
			public void onSuccess(List<AlgorithmRun> runs) {
					List<AlgorithmRun> fixedRuns = new ArrayList<AlgorithmRun>(runs.size());
					
					
					for(AlgorithmRun run : runs)
					{
						
						double runtime = 0;
						
						switch(run.getRunResult())
						{
							case KILLED:
								runtime = Math.max(maxRuntimeObserved.get(run.getRunConfig()).get(), run.getRuntime());
								break;
							default:
								runtime = run.getRuntime();
						}
						
						fixedRuns.add(new ExistingAlgorithmRun(run.getRunConfig(),run.getRunResult(),runtime, run.getRunLength(), run.getQuality(),run.getResultSeed(),run.getAdditionalRunData(), run.getWallclockExecutionTime()));
					}
					handler.onSuccess(fixedRuns);
			}

			@Override
			public void onFailure(RuntimeException t) {
					handler.onFailure(t);
			}
		};
		
		TargetAlgorithmEvaluatorRunObserver runObs = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends KillableAlgorithmRun> runs) 
			{

				List<KillableAlgorithmRun> fixedRuns = new ArrayList<KillableAlgorithmRun>(runs.size());
				for( final KillableAlgorithmRun run : runs)
				{
					
					
					if(run.getRunResult().equals(RunResult.RUNNING))
					{
						
						KillHandler kh = new KillHandler()
						{

							AtomicBoolean b = new AtomicBoolean(false);
							@Override
							public void kill() {
								b.set(true);
								run.kill();
							}

							@Override
							public boolean isKilled() {
								return b.get();
							}
							
						};
					
						fixedRuns.add(new RunningAlgorithmRun(run.getRunConfig(),updateRuntime(run.getRunConfig(),run.getRuntime()),run.getRunLength(), run.getQuality(), run.getResultSeed(), run.getWallclockExecutionTime(),kh));						
					} else if(run.getRunResult().equals(RunResult.KILLED))
					{
						fixedRuns.add(new KillableWrappedAlgorithmRun(new ExistingAlgorithmRun(run.getRunConfig(), run.getRunResult(),updateRuntime(run.getRunConfig(),run.getRuntime()), run.getRunLength(), run.getQuality(),run.getResultSeed(),run.getAdditionalRunData(), run.getWallclockExecutionTime())));
					}	else
					{
						fixedRuns.add(new KillableWrappedAlgorithmRun(new ExistingAlgorithmRun(run.getRunConfig(), run.getRunResult(), run.getRuntime(), run.getRunLength(), run.getQuality(),run.getResultSeed(),run.getAdditionalRunData(), run.getWallclockExecutionTime())));
					}
				}
				
				if(obs != null)
				{
					obs.currentStatus(fixedRuns);
				}
				
				
			}
			
			private double updateRuntime(RunConfig rc, double runtime)
			{
				AtomicDouble d = maxRuntimeObserved.get(rc);
				while(true)
				{
					double oldValue = d.get();
					if(oldValue < runtime)
					{
						d.compareAndSet(oldValue, runtime);
					} else
					{
						return runtime;
					}
					
				}
				
			}
			
		};
		
		tae.evaluateRunsAsync(runConfigs, myHandler, runObs);

	}
	

	@Override
	protected void postDecorateeNotifyShutdown() {
		// TODO Auto-generated method stub
		
	}

	
}
