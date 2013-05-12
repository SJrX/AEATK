package ca.ubc.cs.beta.targetalgorithmevaluator;

import com.beust.jcommander.Parameter;

import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
@UsageTextField(title="Echo Target Algorithm Evaluator", description="Options for a target algorithm evaluator that reads it's response from the supplied configuration ")
public class EchoTargetAlgorithmEvaluatorOptions extends AbstractOptions {

	@Parameter(names="--paramecho-quick-eval", description = "If set to true responses will return immediately instead of ")
	public boolean quickEval = true;

}
