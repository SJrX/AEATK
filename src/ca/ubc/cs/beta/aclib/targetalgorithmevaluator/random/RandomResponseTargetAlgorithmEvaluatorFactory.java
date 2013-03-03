package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.random;

import org.mangosdk.spi.ProviderFor;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
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
			AlgorithmExecutionConfig execConfig, AbstractOptions options) {
		return new RandomResponseTargetAlgorithmEvaluator(execConfig,(RandomResponseTargetAlgorithmEvaluatorOptions) options);
	}

	@Override
	public AbstractOptions getOptionObject() {
		return new RandomResponseTargetAlgorithmEvaluatorOptions();
	}
	
	

}
