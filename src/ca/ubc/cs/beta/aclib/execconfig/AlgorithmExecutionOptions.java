package ca.ubc.cs.beta.aclib.execconfig;

import java.util.Collections;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpaceOptions;
import ca.ubc.cs.beta.aclib.misc.jcommander.converter.BinaryDigitBooleanConverter;
import ca.ubc.cs.beta.aclib.misc.jcommander.converter.StringToDoubleConverterWithMax;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.ZeroInfinityOpenInterval;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorOptions;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;


/**
 * Options object that defines arguments for Target Algorithm Execution
 * @author sjr
 *
 */

@UsageTextField(title="Algorithm Execution Options", description="Options related to running the target algorithm")
public class AlgorithmExecutionOptions extends AbstractOptions {
	
	@Parameter(names={"--algoExec", "--algo"}, description="command string to execute algorithm with", required=true)
	public String algoExec;
	
	@Parameter(names={"--execDir","--execdir"}, description="working directory to execute algorithm in", required=true)
	public String algoExecDir;
	
	@Parameter(names="--deterministic", description="treat the target algorithm as deterministic", converter=BinaryDigitBooleanConverter.class)
	public boolean deterministic;

	@Parameter(names={"--cutoffTime","--cutoff_time"}, description="CPU time limit for an individual target algorithm run", required=true, validateWith=ZeroInfinityOpenInterval.class)
	public double cutoffTime;
	
	@Parameter(names={"--cutoffLength","--cutoff_length"}, description="cap limit for an individual run [not implemented currently]", converter=StringToDoubleConverterWithMax.class, hidden=true)
	public double cutoffLength = -1.0;
	
	@ParametersDelegate
	public TargetAlgorithmEvaluatorOptions taeOpts = new TargetAlgorithmEvaluatorOptions();
	
	@ParametersDelegate
	public ParamConfigurationSpaceOptions paramFileDelegate = new ParamConfigurationSpaceOptions();
	
	public AlgorithmExecutionConfig getAlgorithmExecutionConfig()
	{
		return new AlgorithmExecutionConfig(algoExec, algoExecDir, paramFileDelegate.getParamConfigurationSpace(Collections.singletonList(algoExecDir)), false, deterministic, this.cutoffTime);
	}
}
