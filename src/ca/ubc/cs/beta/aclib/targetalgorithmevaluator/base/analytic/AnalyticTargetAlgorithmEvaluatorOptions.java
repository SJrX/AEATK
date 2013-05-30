package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.analytic;

import com.beust.jcommander.Parameter;

import ca.ubc.cs.beta.aclib.misc.jcommander.validator.FixedPositiveInteger;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.NonNegativeInteger;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;

@UsageTextField(title="Analytic Target Algorithm Evaluator Options", description="This Target Algorithm Evaluator uses an analytic function to generate a runtime")
public class AnalyticTargetAlgorithmEvaluatorOptions extends AbstractOptions {
	
	@Parameter(names="--analytic-simulate-delay", description = "If set to true the TAE will simulate the wallclock delay")
	public boolean simulateDelay = false;
	
	@Parameter(names="--analytic-simulate-cores", description = "If set to greater than 0, the TAE will serialize requests so that no more than these number will execute concurrently. ", validateWith=NonNegativeInteger.class)
	public int cores = 0;
	
	@Parameter(names="--analytic-observer-frequency", description="How often to notify observer of updates (in milli-seconds)", validateWith=FixedPositiveInteger.class)
	public int observerFrequency = 100;
	
	@Parameter(names="--analytic-function", description="Which analytic function to use")
	public AnalyticFunctions func = AnalyticFunctions.CAMELBACK;
	
}
