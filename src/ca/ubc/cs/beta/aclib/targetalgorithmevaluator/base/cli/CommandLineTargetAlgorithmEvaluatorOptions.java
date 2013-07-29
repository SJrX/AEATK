package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli;

import ca.ubc.cs.beta.aclib.misc.jcommander.validator.FixedPositiveInteger;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;


@UsageTextField(title="Command Line Target Algorithm Evaluator Options")
public class CommandLineTargetAlgorithmEvaluatorOptions extends AbstractOptions {
	
	@Parameter(names="--cli-observer-frequency", description="How often to notify observer of updates (in milli-seconds)", validateWith=FixedPositiveInteger.class)
	public int observerFrequency = 100;

	@Parameter(names="--cli-concurrent-execution", description="Whether to allow concurrent execution ")
	public boolean concurrentExecution = true;
	
	@Parameter(names="--cli-cores", validateWith=FixedPositiveInteger.class)
	public int cores = 1;
	
	@Parameter(names={"--cli-log-all-call-strings","--log-all-call-strings","--logAllCallStrings"}, description="log every call string")
	public boolean logAllCallStrings = false;
	
	@Parameter(names={"--cli-log-all-process-output","--log-all-process-output","--logAllProcessOutput"}, description="log all process output")
	public boolean logAllProcessOutput = false;
	
	@Parameter(names={"--cli-listen-for-updates"}, description="If true will create a socket and set environment variables so that we can have updates of CPU time")
	public boolean listenForUpdates = true;
	
	
}
