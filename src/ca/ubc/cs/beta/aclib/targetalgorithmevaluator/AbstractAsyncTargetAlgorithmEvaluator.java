package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

public abstract class AbstractAsyncTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluator implements TargetAlgorithmEvaluator{



	public AbstractAsyncTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig) {
		super(execConfig);
	}


	@Override
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		 return TargetAlgorithmEvaluatorHelper.evaluateRunSyncToAsync(runConfigs,this,obs);
	}

	
}