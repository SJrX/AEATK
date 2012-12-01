package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.random;

import org.mangosdk.spi.ProviderFor;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.factory.TargetAlgorithmEvaluatorFactory;

@ProviderFor(TargetAlgorithmEvaluatorFactory.class)
public class RandomResponseTargetAlgorithmEvaluatorFactory implements
		TargetAlgorithmEvaluatorFactory {

	
	@Override
	public String getName() {
		return "RANDOM";
	}

	@Override
	public TargetAlgorithmEvaluator getTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig, int maxConcurrentExecutions) {
		return new RandomResponseTargetAlgorithmEvaluator(execConfig);
	}

}
