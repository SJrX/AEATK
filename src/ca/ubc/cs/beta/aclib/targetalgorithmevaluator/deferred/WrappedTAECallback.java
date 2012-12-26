package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred;

import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;

/**
 * Handler interface for Deferred Target Algorithm Evaluator runs
 * @author Steve Ramage 
 *
 */
public interface WrappedTAECallback extends TAECallback{

	public void onComplete(TAECallback handler);
	
}
