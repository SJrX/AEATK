package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred;

import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;

/**
 * Handler interface for Deferred Target Algorithm Evaluator runs
 * <p>
 * <b>Client Note:</b> If the onSuccess() method throws an exception, you should call the onFailure() method,
 * this primarily simplifies the implementations of decorators.
 * 
 *
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public interface TAECallback {

	/**
	 * Invoked if/when the runs complete
	 * @param runs the list of completed runs
	 */
	public void onSuccess(List<AlgorithmRun> runs);
	
	/**
	 * Invoked if/when there is a failure
	 * @param t throwable that occurred
	 */
	public void onFailure(RuntimeException t);
	
	
	
}
