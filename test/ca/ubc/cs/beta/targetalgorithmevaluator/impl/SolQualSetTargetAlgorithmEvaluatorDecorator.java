package ca.ubc.cs.beta.targetalgorithmevaluator.impl;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractForEachRunTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;

public class SolQualSetTargetAlgorithmEvaluatorDecorator extends
		AbstractForEachRunTargetAlgorithmEvaluatorDecorator {
	private final int solQual;
	
	public SolQualSetTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae, int solQual) {
		super(tae);
		this.solQual = solQual;
	}



	@Override
	protected AlgorithmRun processRun(AlgorithmRun run) {
		return new ExistingAlgorithmRun(run.getExecutionConfig(), run.getRunConfig(), run.getRunResult(), run.getRuntime(), run.getRunLength(), solQual, run.getResultSeed());
		
	}

}
