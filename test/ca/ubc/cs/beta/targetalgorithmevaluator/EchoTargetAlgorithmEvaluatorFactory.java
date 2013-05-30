package ca.ubc.cs.beta.targetalgorithmevaluator;

import org.mangosdk.spi.ProviderFor;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorFactory;

@ProviderFor(TargetAlgorithmEvaluatorFactory.class)
public class EchoTargetAlgorithmEvaluatorFactory implements TargetAlgorithmEvaluatorFactory {

	@Override
	public String getName() {
		return "PARAMECHO";
	}

	@Override
	public TargetAlgorithmEvaluator getTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig, AbstractOptions options) {
		return new EchoTargetAlgorithmEvaluator(execConfig, (EchoTargetAlgorithmEvaluatorOptions) options);
	}

	@Override
	public AbstractOptions getOptionObject() {
		return new EchoTargetAlgorithmEvaluatorOptions();
	}

}
