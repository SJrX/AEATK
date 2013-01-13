package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.constant;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aclib.exceptions.TargetAlgorithmAbortException;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractBlockingTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractNonBlockingTargetAlgorithmEvaluator;

public class ConstantTargetAlgorithmEvaluator extends AbstractNonBlockingTargetAlgorithmEvaluator {

	public ConstantTargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig) {
		super(execConfig);
		if(execConfig.getAlgorithmCutoffTime() <= 0.01)
		{
			throw new IllegalArgumentException("To use the Constant TAE, cutoff time must be greater than 0.01 seconds");
		}
	}

	@Override
	public boolean isRunFinal() {
		return true;
	}

	@Override
	public boolean areRunsPersisted() {
		return true;
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		List<AlgorithmRun> runs = new ArrayList<AlgorithmRun>();
		
		for(RunConfig rc : runConfigs)
		{
			runs.add(new ExistingAlgorithmRun(execConfig, rc, "SAT,0.01,0,0," + rc.getProblemInstanceSeedPair().getSeed()));
		}
		
		return runs;
		
	}

	@Override
	protected void subtypeShutdown() {
		// TODO Auto-generated method stub
		
	}

}
