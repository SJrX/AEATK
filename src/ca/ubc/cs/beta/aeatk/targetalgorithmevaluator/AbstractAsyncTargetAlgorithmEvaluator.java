package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator;

import java.util.List;

import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.runconfig.RunConfig;

public abstract class AbstractAsyncTargetAlgorithmEvaluator extends AbstractTargetAlgorithmEvaluator implements TargetAlgorithmEvaluator{


	@Override
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		 return TargetAlgorithmEvaluatorHelper.evaluateRunSyncToAsync(runConfigs,this,obs);
	}

	
}
