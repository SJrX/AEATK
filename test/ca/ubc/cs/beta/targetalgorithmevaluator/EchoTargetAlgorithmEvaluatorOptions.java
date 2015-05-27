package ca.ubc.cs.beta.targetalgorithmevaluator;

import com.beust.jcommander.Parameter;

import ca.ubc.cs.beta.aeatk.misc.jcommander.validator.FixedPositiveInteger;
import ca.ubc.cs.beta.aeatk.misc.jcommander.validator.NonNegativeInteger;
import ca.ubc.cs.beta.aeatk.misc.jcommander.validator.ZeroInfinityHalfOpenIntervalRight;
import ca.ubc.cs.beta.aeatk.misc.options.OptionLevel;
import ca.ubc.cs.beta.aeatk.misc.options.UsageTextField;
import ca.ubc.cs.beta.aeatk.options.AbstractOptions;
@UsageTextField(title="Echo Target Algorithm Evaluator", description="Options for a target algorithm evaluator that reads it's response from the supplied configuration ", level=OptionLevel.DEVELOPER)
public class EchoTargetAlgorithmEvaluatorOptions extends AbstractOptions {

	//@Parameter(names="--paramecho-quick-eval", description = "If set to true responses will return immediately instead of ")
	//public boolean quickEval = true;
	
	@Parameter(names="--paramecho-simulate-cores", description = "If set to greater than 0, the TAE will serialize requests so that no more than these number will execute concurrently. ", validateWith=NonNegativeInteger.class)
	public int cores = 0;
	

	@Parameter(names="--paramecho-observer-frequency", description="How often to notify observer of updates (in milli-seconds)", validateWith=FixedPositiveInteger.class)
	public int observerFrequency = 500;
	

}
