package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.preloaded;

import com.beust.jcommander.Parameter;

import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;

@UsageTextField(title="Preloaded Response Target Algorithm Evaluator", description="Target Algorithm Evaluator that provides preloaded responses")
public class PreloadedResponseTargetAlgorithmEvaluatorOptions extends
		AbstractOptions {

	@Parameter(names={"--preload-responseData"}, description="Preloaded Response Values in the format [{SAT,UNSAT,...}=x], where x is a runtime (e.g. [SAT=1],[UNSAT=1.1]... ")
	public String preloadedResponses = "";
	
	@Parameter(names={"--preload-runLength"}, description="Runlength to return on all values")
	public double runLength = -1;
	
	@Parameter(names={"--preload-quality"}, description="Quality to return on all values")
	public double quality = 0;
	
	@Parameter(names="--preload-additional-run-data", description="Additional Run Data to return")
	public String additionalRunData = "";

	

}
