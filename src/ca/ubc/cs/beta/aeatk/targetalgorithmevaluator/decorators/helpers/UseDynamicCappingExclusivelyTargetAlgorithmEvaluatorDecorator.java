package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.RunResult;
import ca.ubc.cs.beta.aeatk.algorithmrun.RunningAlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.kill.KillHandler;
import ca.ubc.cs.beta.aeatk.runconfig.RunConfig;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorHelper;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;


/**
 * This decorator changes cap times of submitted runs to be that of the execution configuration, and then uses an observer to enforce the time limit.
 * 
 * The primary benefit of this, is that it allows better use of caching{@link ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.resource.caching.CachingTargetAlgorithmEvaluatorDecorator}
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class UseDynamicCappingExclusivelyTargetAlgorithmEvaluatorDecorator
		extends AbstractTargetAlgorithmEvaluatorDecorator {

	
	private final Logger log = LoggerFactory.getLogger(getClass());
	public UseDynamicCappingExclusivelyTargetAlgorithmEvaluatorDecorator(
			TargetAlgorithmEvaluator tae) {
		super(tae);

	}
	

	@Override
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		return TargetAlgorithmEvaluatorHelper.evaluateRunSyncToAsync(runConfigs, this, obs);
	}

	@Override
	public final void evaluateRunsAsync(List<RunConfig> runConfigs,
			final TargetAlgorithmEvaluatorCallback oHandler, final TargetAlgorithmEvaluatorRunObserver obs) {

		
		final Map<RunConfig, RunConfig> transformedRuns = new ConcurrentHashMap<RunConfig, RunConfig>();
		
		List<RunConfig> newRunConfigs = new ArrayList<RunConfig>(runConfigs.size());
		
		for(RunConfig rc : runConfigs)
		{
			if(rc.hasCutoffLessThanMax())
			{
				
				
				RunConfig newRC = new RunConfig(rc.getProblemInstanceSeedPair(), rc.getParamConfiguration(), rc.getAlgorithmExecutionConfig());
				transformedRuns.put(newRC, rc);
				newRunConfigs.add(newRC);
			} else
			{
				
				transformedRuns.put(rc, rc);
				newRunConfigs.add(rc);
			}
			
			
		}
		
	
		TargetAlgorithmEvaluatorCallback myHandler = new TargetAlgorithmEvaluatorCallback()
		{
			private final TargetAlgorithmEvaluatorCallback handler = oHandler;

			@Override
			public void onSuccess(List<AlgorithmRun> runs) {
					List<AlgorithmRun> fixedRuns = new ArrayList<AlgorithmRun>(runs.size());
					for(AlgorithmRun run : runs)
					{
						RunResult r = run.getRunResult();
						double runtime = run.getRuntime();
						
						RunConfig origRunConfig = transformedRuns.get(run.getRunConfig());
						
						if(runtime > origRunConfig.getCutoffTime())
						{
							switch(r)
							{
								case SAT:
								case UNSAT:
								case KILLED:
								case TIMEOUT:
									r= RunResult.TIMEOUT;
									runtime = origRunConfig.getCutoffTime();
								break;
								default:
								//NOOP
							}
						
						}
						fixedRuns.add(new ExistingAlgorithmRun(origRunConfig, r, runtime, run.getRunLength(), run.getQuality(),run.getResultSeed(),run.getAdditionalRunData(), run.getWallclockExecutionTime()));
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
			public void currentStatus(List<? extends AlgorithmRun> runs) 
			{

				List<AlgorithmRun> fixedRuns = new ArrayList<AlgorithmRun>(runs.size());
				for( final AlgorithmRun run : runs)
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
						if(transformedRuns.get(run.getRunConfig()) == null)
						{
							log.error("Couldn't find original run config for {} in {} ", run.getRunConfig(), transformedRuns);
						}
						fixedRuns.add(new RunningAlgorithmRun(transformedRuns.get(run.getRunConfig()),run.getRuntime(),run.getRunLength(), run.getQuality(), run.getResultSeed(), run.getWallclockExecutionTime(),kh));
						
						
						if(transformedRuns.get(run.getRunConfig()).getCutoffTime() < run.getRuntime())
						{
							run.kill();
						}
						
					} else
					{
						fixedRuns.add(new ExistingAlgorithmRun(transformedRuns.get(run.getRunConfig()), run.getRunResult(), run.getRuntime(), run.getRunLength(), run.getQuality(),run.getResultSeed(),run.getAdditionalRunData(), run.getWallclockExecutionTime()));
					}
				}
				
				if(obs != null)
				{
					obs.currentStatus(fixedRuns);
				}
				
				
			}
			
		};
		
		tae.evaluateRunsAsync(newRunConfigs, myHandler, runObs);

	}
	

	@Override
	protected void postDecorateeNotifyShutdown() {
		// TODO Auto-generated method stub
		
	}

}
