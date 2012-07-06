package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.factory;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;

/**
 * Factory class for TargetAlgorithmEvalutars
 * 
 * <b>Implementation Note:</b> Unlike what you may think this interface does not exist because
 * someone had drunk design pattern Kool-aid. We load TargetAlgorithmEvaluators via SPI
 * and unfortunately we need to use a no arg constructor, but we clearly need a one arg constructor, hence this interface.
 * 
 * @author Steve Ramage 
 *
 */
public interface TargetAlgorithmEvaluatorFactory {

	/**
	 * Returns a friendly name (WITHOUT WHITE SPACE) representing the evaluator
	 * <p>
	 * This is used to present the user with options, so should be constant as the user will specify them on the command line
	 * for instance if your targetalgorithmevalutor simply returns Random results, it might be used via an option --algoExecSystem RANDOM
	 * <p> 
	 * Where RANDOM is what is returned from this method.
	 * 
	 * @return a friendly name for the command line 
	 */
	public String getName();
	
	/**
	 * Retrieves a Target Algorithm Evaluator
	 * @param 	execConfig 				    The Execution Configuration for the Target Algorithm
	 * @param   maxConcurrentExecutions 	The maximum number of concurrent executions
	 * @return	the target algorithm evaluator
	 */
	public TargetAlgorithmEvaluator getTargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig, int maxConcurrentExecutions);
	
	
}
