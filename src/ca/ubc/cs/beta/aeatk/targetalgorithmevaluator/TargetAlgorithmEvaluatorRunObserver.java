package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator;

import java.util.List;

import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;


public interface TargetAlgorithmEvaluatorRunObserver {

	/**
	 * Invoked on a best effort basis when new information is available
	 * @param runs
	 */
	public void currentStatus(List<? extends AlgorithmRun> runs);
}
