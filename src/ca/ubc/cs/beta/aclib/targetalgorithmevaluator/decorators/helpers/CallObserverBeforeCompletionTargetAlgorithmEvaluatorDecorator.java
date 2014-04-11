package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.helpers;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;

/**
 * Helpful decorator that ensures the observer is notified of the final set of runs before the call back
 * 
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
public class CallObserverBeforeCompletionTargetAlgorithmEvaluatorDecorator extends AbstractTargetAlgorithmEvaluatorDecorator {

	
	public CallObserverBeforeCompletionTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae) {
		super(tae);
	}

	@Override
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		
		
		
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs, obs);
		
		if(obs != null)
		{
			List<AlgorithmRun> kruns = new ArrayList<AlgorithmRun>();
			
			
			for(AlgorithmRun run : runs)
			{
				kruns.add(run);
			}
			obs.currentStatus(kruns);
		}
		return runs;
		
	}

	@Override
	public final void evaluateRunsAsync(List<RunConfig> runConfigs,
			final TargetAlgorithmEvaluatorCallback oHandler, final TargetAlgorithmEvaluatorRunObserver obs) {
		
		//We need to make sure wrapped versions are called in the same order
		//as there unwrapped versions.
	
		TargetAlgorithmEvaluatorCallback myHandler = new TargetAlgorithmEvaluatorCallback()
		{
			private final TargetAlgorithmEvaluatorCallback handler = oHandler;

			@Override
			public void onSuccess(List<AlgorithmRun> runs) {
				if(obs != null)
				{
					List<AlgorithmRun> kruns = new ArrayList<AlgorithmRun>();
					
					
					for(AlgorithmRun run : runs)
					{
						kruns.add(run);
					}
					obs.currentStatus(kruns);
				}
					
					handler.onSuccess(runs);
			}

			@Override
			public void onFailure(RuntimeException t) {
					handler.onFailure(t);
			}
		};
		
		tae.evaluateRunsAsync(runConfigs, myHandler, obs);

	}

	
	@Override
	protected void postDecorateeNotifyShutdown() {
		//NOOP
	}

}
