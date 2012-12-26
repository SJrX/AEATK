package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred;

import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;

/**
 * Handler interface for Deferred Target Algorithm Evaluator runs
 * @author Steve Ramage 
 *
 */
public interface TAECallback {

	/**
	 * Invoked if/when the runs complete
	 * @param runs the list of completed runs
	 */
	public void onSuccess(List<AlgorithmRun> runs);
	
	/**
	 * Invoked if there is a failure.
	 * @param t throwable that occurred
	 */
	public void onFailure(RuntimeException t);
	
}
