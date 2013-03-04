package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ca.ubc.cs.beta.aclib.misc.jcommander.converter.BinaryDigitBooleanConverter;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.NonNegativeInteger;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.ReadableFileConverter;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.validators.PositiveInteger;


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

	@ParametersDelegate
	public TargetAlgorithmEvaluatorOptions taeOpts = new TargetAlgorithmEvaluatorOptions();
	
}
