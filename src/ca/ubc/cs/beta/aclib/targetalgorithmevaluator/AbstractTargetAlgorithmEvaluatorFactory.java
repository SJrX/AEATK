package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.util.Map;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;

/**
 * Abstract Target Algorithm Evaluator Factory that has a default implementation for getting the TargetAlgorithmEvaluator
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
public abstract class AbstractTargetAlgorithmEvaluatorFactory implements
		TargetAlgorithmEvaluatorFactory {

	@Override
	public TargetAlgorithmEvaluator getTargetAlgorithmEvaluator(
			Map<String, AbstractOptions> optionsMap) {
		return this.getTargetAlgorithmEvaluator( optionsMap.get(this.getName()));
	}

	
}
