package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.debug;

import org.mangosdk.spi.ProviderFor;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.factory.TargetAlgorithmEvaluatorFactory;

@ProviderFor(TargetAlgorithmEvaluatorFactory.class)
public class PreloadedResponseTargetAlgorithmEvaluatorFactory implements
		TargetAlgorithmEvaluatorFactory {

	@Override
	public String getName() {
		return "PRELOADED";
	}

	@Override
	public TargetAlgorithmEvaluator getTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig,AbstractOptions obs) {
		return new PreloadedResponseTargetAlgorithmEvaluator(execConfig);
	}



	@Override
	public AbstractOptions getOptionObject() {
		return null;
	}

}
