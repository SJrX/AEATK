package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators;

import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.exceptions.TargetAlgorithmAbortException;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.TAECallback;

/**
 * Modifies Target Algorithm Evaluators to treat CRASHES as aborts
 * @author Steve Ramage 
 *
 */
@ThreadSafe
public class AbortOnCrashTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	public AbortOnCrashTargetAlgorithmEvaluator(TargetAlgorithmEvaluator tae) {
		super(tae);
		
	}
	
	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig run) {
		return validate(super.evaluateRun(run));
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		return evaluateRun(runConfigs, null);
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, CurrentRunStatusObserver obs) {
		return validate(super.evaluateRun(runConfigs, obs));
	}
	

	private List<AlgorithmRun> validate(List<AlgorithmRun> runs)
	{
		
		for(AlgorithmRun run : runs)
		{
			if(run.getRunResult().equals(RunResult.CRASHED))
			{
				throw new TargetAlgorithmAbortException("Target Algorithm Run Reported Crashed: " + run.toString());
			}
		}
		return runs;
	}
	
	@Override
	public void evaluateRunsAsync(RunConfig runConfig, TAECallback handler) {
		evaluateRunsAsync(Collections.singletonList(runConfig), handler, null);
	}

	@Override
	public void evaluateRunsAsync(List<RunConfig> runConfigs,
			final TAECallback handler) {
				evaluateRunsAsync(runConfigs, handler, null);
			}

	@Override
	public void evaluateRunsAsync(List<RunConfig> runConfigs,
			final TAECallback handler, CurrentRunStatusObserver obs) {
		
		
		TAECallback myHandler = new TAECallback()
		{

			@Override
			public void onSuccess(List<AlgorithmRun> runs) {
				try {
					validate(runs);
					handler.onSuccess(runs);
				} catch(TargetAlgorithmAbortException e)
				{
					handler.onFailure(e);
				}
				
			}

			@Override
			public void onFailure(RuntimeException t) {
				handler.onFailure(t);
				
			}
			
		};
		
		tae.evaluateRunsAsync(runConfigs, myHandler, obs);
	}

}
