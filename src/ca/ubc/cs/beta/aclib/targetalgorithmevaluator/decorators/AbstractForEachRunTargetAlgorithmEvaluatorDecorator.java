package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators;

import java.util.Collections;
import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.TAECallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.WrappedTAECallback;
/**
 * Abstraact Decorator for TargetAlgorithmEvaluators 
 * 
 * 
 *  
 * @author Steve Ramage 
 *
 */
public abstract class AbstractForEachRunTargetAlgorithmEvaluatorDecorator extends AbstractTargetAlgorithmEvaluatorDecorator {

	

	public AbstractForEachRunTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae)
	{
		super(tae);
	}
	
	
	@Override
	public final List<AlgorithmRun> evaluateRun(RunConfig run) {
		return processRuns(tae.evaluateRun(run));
	}

	@Override
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		return processRuns(tae.evaluateRun(runConfigs));
	}


	@Override
	public final void evaluateRunsAsync(RunConfig runConfig,
			final TAECallback handler) {
		
		evaluateRunsAsync(Collections.singletonList(runConfig), handler);
	}


	@Override
	public final void evaluateRunsAsync(List<RunConfig> runConfigs,
			TAECallback handler) {
		
		//We need to make sure wrapped versions are called in the same order
		//as there unwrapped versions.
	
		TAECallback myHandler = new TAECallback()
		{
			private TAECallback handler;

			@Override
			public void onSuccess(List<AlgorithmRun> runs) {
					runs = processRuns(runs);			
					handler.onSuccess(runs);
			}

			@Override
			public void onFailure(RuntimeException t) {
					handler.onFailure(t);
				
			}
		};

	}

	
	/**
	 * Process the run in question
	 * 
	 * @param input run from previous result
	 * @return run to replace it with
	 */
	abstract protected AlgorithmRun processRun(AlgorithmRun run);
	
	
	protected final List<AlgorithmRun> processRuns(List<AlgorithmRun> runs)
	{
		for(int i=0; i < runs.size(); i++)
		{
			runs.set(i, processRun(runs.get(i)));
		}
		
		return runs;
	}
	
}
