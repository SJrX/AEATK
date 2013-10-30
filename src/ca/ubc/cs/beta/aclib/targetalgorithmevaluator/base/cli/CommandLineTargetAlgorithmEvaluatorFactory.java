package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli;

import org.mangosdk.spi.ProviderFor;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.resource.BoundedTargetAlgorithmEvaluator;
@ProviderFor(TargetAlgorithmEvaluatorFactory.class)
public class CommandLineTargetAlgorithmEvaluatorFactory extends AbstractTargetAlgorithmEvaluatorFactory  {

	@Override
	public String getName() {
		return "CLI";
	}

	@Override
	public TargetAlgorithmEvaluator getTargetAlgorithmEvaluator( AbstractOptions options) {

		CommandLineTargetAlgorithmEvaluatorOptions cliOpts = (CommandLineTargetAlgorithmEvaluatorOptions) options;
		//CLI TAE doesn't bound properly accross runs and the workaround, until we rewrite the
		//the AutomaticConfiguratorRunner crap is to simply bound it (See Issue #1811 for more info)
		return new BoundedTargetAlgorithmEvaluator(new CommandLineTargetAlgorithmEvaluator( cliOpts ), cliOpts.cores);
	}

	@Override
	public CommandLineTargetAlgorithmEvaluatorOptions getOptionObject()
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

	public static TargetAlgorithmEvaluator getCLITAE(int observerFrequency)
	{
		CommandLineTargetAlgorithmEvaluatorOptions options = new CommandLineTargetAlgorithmEvaluatorOptions();
		options.observerFrequency = observerFrequency;
		options.logAllProcessOutput = true;
		return new CommandLineTargetAlgorithmEvaluator(options);
	}
	
}
