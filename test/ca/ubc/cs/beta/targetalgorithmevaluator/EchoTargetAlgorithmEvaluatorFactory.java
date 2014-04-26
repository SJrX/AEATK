package ca.ubc.cs.beta.targetalgorithmevaluator;

import org.mangosdk.spi.ProviderFor;

import ca.ubc.cs.beta.aeatk.options.AbstractOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorFactory;

@ProviderFor(TargetAlgorithmEvaluatorFactory.class)
public class EchoTargetAlgorithmEvaluatorFactory extends AbstractTargetAlgorithmEvaluatorFactory  {

	@Override
	public String getName() {
		return "PARAMECHO";
	}

	@Override
	public TargetAlgorithmEvaluator getTargetAlgorithmEvaluator(AbstractOptions options) {
		return new EchoTargetAlgorithmEvaluator( (EchoTargetAlgorithmEvaluatorOptions) options);
	}

	@Override
	public AbstractOptions getOptionObject() {
		return new EchoTargetAlgorithmEvaluatorOptions();
	}

}
