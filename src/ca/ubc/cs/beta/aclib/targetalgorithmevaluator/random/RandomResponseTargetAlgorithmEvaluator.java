package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.random;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluator;

public class RandomResponseTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluator {

	public RandomResponseTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig) {
		super(execConfig);
	}

	@Override
	public void notifyShutdown() {

	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		
		List<AlgorithmRun> ar = new ArrayList<AlgorithmRun>(runConfigs.size());
		for(RunConfig rc : runConfigs)
		{
			ar.add(new ExistingAlgorithmRun(execConfig, rc, "SAT, " + Math.random() + ",-1,0," + rc.getProblemInstanceSeedPair().getSeed()));
		}
		
		return ar;
	}

}
