package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators;

import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
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
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		return processRuns(tae.evaluateRun(runConfigs, obs));
	}

	@Override
	public final void evaluateRunsAsync(List<RunConfig> runConfigs,
			final TargetAlgorithmEvaluatorCallback oHandler, TargetAlgorithmEvaluatorRunObserver obs) {
		
		//We need to make sure wrapped versions are called in the same order
		//as there unwrapped versions.
	
		TargetAlgorithmEvaluatorCallback myHandler = new TargetAlgorithmEvaluatorCallback()
		{
			private final TargetAlgorithmEvaluatorCallback handler = oHandler;

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
		
		tae.evaluateRunsAsync(runConfigs, myHandler, obs);

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
