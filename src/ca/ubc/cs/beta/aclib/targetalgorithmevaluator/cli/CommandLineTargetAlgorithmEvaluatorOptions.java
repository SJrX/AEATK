package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.cli;

import ca.ubc.cs.beta.aclib.misc.jcommander.validator.FixedPositiveInteger;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;

import com.beust.jcommander.Parameter;


@UsageTextField(title="Command Line Target Algorithm Evaluator Options")
public class CommandLineTargetAlgorithmEvaluatorOptions extends AbstractOptions {
	
	@Parameter(names="--cli-observer-frequency", description="How often to notify observer of updates (in milli-seconds)", validateWith=FixedPositiveInteger.class)
	public int observerFrequency = 100;

	@Parameter(names="--cli-concurrent-execution", description="Whether to allow concurrent execution (NOTE: If --cores > 1, you should set this to true)")
	public boolean concurrentExecution = false;
	
	@Parameter(names="--logAllCallStrings", description="log every call string")
	public boolean logAllCallStrings = false;
	
	@Parameter(names="--logAllProcessOutput", description="log all process output")
	public boolean logAllProcessOutput = false;

}