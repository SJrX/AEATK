package ca.ubc.cs.beta.targetalgorithmevaluator.impl;

import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.ExistingAlgorithmRunResult;
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
	protected AlgorithmRunResult processRun(AlgorithmRunResult run) {
		return new ExistingAlgorithmRunResult( run.getAlgorithmRunConfiguration(), run.getRunStatus(), run.getRuntime(), run.getRunLength(), solQual, run.getResultSeed());
		
	}

	@Override
	protected void postDecorateeNotifyShutdown() {
		//No cleanup necessary
	}
}
