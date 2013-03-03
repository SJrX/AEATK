package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.cli;

import org.mangosdk.spi.ProviderFor;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.factory.TargetAlgorithmEvaluatorFactory;
@ProviderFor(TargetAlgorithmEvaluatorFactory.class)
public class CommandLineTargetAlgorithmEvaluatorFactory implements
		TargetAlgorithmEvaluatorFactory {

	@Override
	public String getName() {
		return "CLI";
	}

	@Override
	public TargetAlgorithmEvaluator getTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig config, AbstractOptions options) {

		return new CommandLineTargetAlgorithmEvaluator(config, (CommandLineTargetAlgorithmEvaluatorOptions) options );
	}

	@Override
	public AbstractOptions getOptionObject()
	{
		return new CommandLineTargetAlgorithmEvaluatorOptions();
	}
	
	public static TargetAlgorithmEvaluator getCLITAE(AlgorithmExecutionConfig config)
	{
		return new CommandLineTargetAlgorithmEvaluator(config, new CommandLineTargetAlgorithmEvaluatorOptions());
	}

}
