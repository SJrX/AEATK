package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.constant;

import com.beust.jcommander.Parameter;

import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;

@UsageTextField(title="Constant Target Algorithm Evaluator Options", description="Parameters for the Constant Target Algorithm Evaluator")
public class ConstantTargetAlgorithmEvaluatorOptions extends AbstractOptions{

	
	@Parameter(names="--runResult", description="Run Result To return")
	public RunResult runResult = RunResult.SAT;
	
	@Parameter(names="--runTime", description="Runtime to return")
	public double runtime = 1.00;
	
	@Parameter(names="--runQuality", description="Quality to return")
	public double quality = 0;
	
	@Parameter(names="--runLength", description="Runlength to return")
	public double runlength = 0;
	
	@Parameter(names="--additionalRunData", description="Additional Run Data to return")
	public String additionalRunData = "";
	


}
