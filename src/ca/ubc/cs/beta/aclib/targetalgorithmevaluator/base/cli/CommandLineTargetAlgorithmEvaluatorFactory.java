package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli;

import org.mangosdk.spi.ProviderFor;

import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorFactory;
@ProviderFor(TargetAlgorithmEvaluatorFactory.class)
public class CommandLineTargetAlgorithmEvaluatorFactory extends AbstractTargetAlgorithmEvaluatorFactory  {

	
	public static String NAME = "CLI";
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public TargetAlgorithmEvaluator getTargetAlgorithmEvaluator( AbstractOptions options) {

		CommandLineTargetAlgorithmEvaluatorOptions cliOpts = (CommandLineTargetAlgorithmEvaluatorOptions) options;		
		TargetAlgorithmEvaluator tae =  new CommandLineTargetAlgorithmEvaluator( cliOpts );
		
		return tae;
	}

	@Override
	public CommandLineTargetAlgorithmEvaluatorOptions getOptionObject()
	{
		return new CommandLineTargetAlgorithmEvaluatorOptions();
	}

	public static CommandLineTargetAlgorithmEvaluatorOptions getCLIOPT()
	{
		return new CommandLineTargetAlgorithmEvaluatorOptions();
	}

	public static TargetAlgorithmEvaluator getCLITAE()

	{
		
		CommandLineTargetAlgorithmEvaluatorOptions opts = new CommandLineTargetAlgorithmEvaluatorOptions();
		opts.logAllCallStrings = true;
		opts.logAllProcessOutput = true;
		return new CommandLineTargetAlgorithmEvaluator( opts );
	}

	public static TargetAlgorithmEvaluator getCLITAE(CommandLineTargetAlgorithmEvaluatorOptions options)
	{
		return new CommandLineTargetAlgorithmEvaluator(options);
	}
	
	public static TargetAlgorithmEvaluator getCLITAE(int observerFrequency)
	{
		CommandLineTargetAlgorithmEvaluatorOptions options = new CommandLineTargetAlgorithmEvaluatorOptions();
		options.observerFrequency = observerFrequency;
		options.logAllProcessOutput = true;
		return new CommandLineTargetAlgorithmEvaluator(options);
	}
	
}
