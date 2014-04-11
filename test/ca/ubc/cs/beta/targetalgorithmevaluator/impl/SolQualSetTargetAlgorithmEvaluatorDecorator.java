package ca.ubc.cs.beta.targetalgorithmevaluator.impl;

import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.AbstractForEachRunTargetAlgorithmEvaluatorDecorator;

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

	@Override
	protected void postDecorateeNotifyShutdown() {
		//No cleanup necessary
	}
}
