package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.runconfig.RunConfig;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;


/**
 * This decorator checks to see if the Target Algorithm Evaluator is honouring post-conditions
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class SanityCheckingTargetAlgorithmEvaluatorDecorator extends AbstractTargetAlgorithmEvaluatorDecorator {

	
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public SanityCheckingTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae) {
		super(tae);
	}

	
	@Override
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		
		List<RunConfig> originalList = Collections.unmodifiableList( new ArrayList<RunConfig>(runConfigs));
		
		
		
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs, new SanityCheckingTargetAlgorithmEvaluatorObserver(obs, originalList));
		
		List<RunConfig> returnedRuns = new ArrayList<RunConfig>(runs.size());
		
		for(AlgorithmRun run : runs)
		{
			returnedRuns.add(run.getRunConfig());
		}
		
		
		if(!returnedRuns.equals(originalList))
		{
			log.error("Misbehaiving Target Algorithm Evaluator Detected, run configs of returned runs DO NOT match run configs of submitted runs, either different run configs, or different order detected: \n Submitted: {},\n Observed: {}", originalList, returnedRuns );
		}
		
		return runs;
	}

	@Override
	public final void evaluateRunsAsync(final List<RunConfig> runConfigs,
			final TargetAlgorithmEvaluatorCallback oHandler, TargetAlgorithmEvaluatorRunObserver obs) {
		
		final List<RunConfig> originalList = Collections.unmodifiableList( new ArrayList<RunConfig>(runConfigs));
		
		
		TargetAlgorithmEvaluatorCallback handler = new TargetAlgorithmEvaluatorCallback()
		{

			@Override
			public void onSuccess(List<AlgorithmRun> runs) {
				
				
				List<RunConfig> returnedRuns = new ArrayList<RunConfig>(runs.size());
				
				for(AlgorithmRun run : runs)
				{
					returnedRuns.add(run.getRunConfig());
				}
				
				
				if(!returnedRuns.equals(originalList))
				{
					log.error("Misbehaiving Target Algorithm Evaluator Detected, run configs of returned runs DO NOT match run configs of submitted runs, either different run configs, or different order detected: \n Submitted: {},\n Observed: {}", originalList, returnedRuns );
				}
				
				oHandler.onSuccess(runs);
			}

			@Override
			public void onFailure(RuntimeException e) {
				oHandler.onFailure(e);
				
			}
			
		};
		tae.evaluateRunsAsync(runConfigs, handler,  new SanityCheckingTargetAlgorithmEvaluatorObserver(obs, originalList));

	}
	
	@Override
	protected void postDecorateeNotifyShutdown() {
		// TODO Auto-generated method stub
		
	}

	private class SanityCheckingTargetAlgorithmEvaluatorObserver implements TargetAlgorithmEvaluatorRunObserver{

		private final TargetAlgorithmEvaluatorRunObserver obs;
		private final List<RunConfig> originalList;
		public SanityCheckingTargetAlgorithmEvaluatorObserver(TargetAlgorithmEvaluatorRunObserver obs, List<RunConfig> originalList)
		{
			this.obs = obs;
			this.originalList = originalList;
		}
		@Override
		public void currentStatus(List<? extends AlgorithmRun> runs) {
			
			List<RunConfig> observerRuns = new ArrayList<RunConfig>(runs.size());
			
			for(AlgorithmRun run : runs)
			{
				observerRuns.add(run.getRunConfig());
			}
			
			
			if(!observerRuns.equals(originalList))
			{
				log.error("Misbehaiving Target Algorithm Evaluator Detected, run configs of observed runs DO NOT match run configs of submitted runs, either different run configs, or different order detected: \n Submitted: {},\n Observed: {}", originalList, observerRuns );
			}
			
			if(obs != null)
			{
				obs.currentStatus(runs);
			}
		}
		
	}
	
}
