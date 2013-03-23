package ca.ubc.cs.beta.targetalgorithmevaluator;

import com.beust.jcommander.Parameter;

import ca.ubc.cs.beta.aclib.options.AbstractOptions;

public class EchoTargetAlgorithmEvaluatorOptions extends AbstractOptions {

	@Parameter(names="--paramecho-quick-eval", description = "If set to true responses will return immediately instead of ")
	public boolean quickEval = true;

}
