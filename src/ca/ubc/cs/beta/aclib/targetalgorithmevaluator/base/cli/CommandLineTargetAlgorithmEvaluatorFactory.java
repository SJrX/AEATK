package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli;

import org.mangosdk.spi.ProviderFor;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorFactory;
@ProviderFor(TargetAlgorithmEvaluatorFactory.class)
public class CommandLineTargetAlgorithmEvaluatorFactory extends AbstractTargetAlgorithmEvaluatorFactory  {

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
	public CommandLineTargetAlgorithmEvaluatorOptions getOptionObject()
	{
		return new CommandLineTargetAlgorithmEvaluatorOptions();
	}
	
	public static TargetAlgorithmEvaluator getCLITAE(AlgorithmExecutionConfig config)
	{
		return new CommandLineTargetAlgorithmEvaluator(config, new CommandLineTargetAlgorithmEvaluatorOptions());
	}

	public static TargetAlgorithmEvaluator getCLITAE(AlgorithmExecutionConfig config, int observerFrequency)
	{
		CommandLineTargetAlgorithmEvaluatorOptions options = new CommandLineTargetAlgorithmEvaluatorOptions();
		options.observerFrequency = observerFrequency;
		return new CommandLineTargetAlgorithmEvaluator(config,options);
	}
	
}
