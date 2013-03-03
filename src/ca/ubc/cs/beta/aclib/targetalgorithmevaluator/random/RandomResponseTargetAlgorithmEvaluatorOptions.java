package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.random;

import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;

import com.beust.jcommander.Parameter;

@UsageTextField(title="Random Target Algorithm Evaluator Options")
public class RandomResponseTargetAlgorithmEvaluatorOptions extends AbstractOptions {

	@Parameter(names="--random-quick-eval", description = "If set to true responses will return immediately instead of ")
	public boolean quickEval = false;
}
