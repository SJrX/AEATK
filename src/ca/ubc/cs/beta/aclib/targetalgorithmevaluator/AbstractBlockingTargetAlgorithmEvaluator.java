package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.util.Collections;
import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.TAECallback;

/**
 * Abstract type that simple blocks on asynchronous requests
 * 
 * Useful if you you just want to implement something without dealing with the callbacks
 * 
 * @author sjr
 */
public abstract class AbstractBlockingTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluator {

	public AbstractBlockingTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig) {
		super(execConfig);
	}

	@Override
	public void evaluateRunsAsync(RunConfig runConfig,
			TAECallback handler) {
		this.evaluateRunsAsync(Collections.singletonList(runConfig), handler);
	}

	@Override
	public  void evaluateRunsAsync(List<RunConfig> runConfigs,
			TAECallback handler) {
		try {
			List<AlgorithmRun> runs;
			synchronized(this)
			{
				runs = this.evaluateRun(runConfigs);
			}
			handler.onSuccess(runs);
		} catch(RuntimeException e)
		{
			handler.onFailure(e);
		}
		

	}

	
}
