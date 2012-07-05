package ca.ubc.cs.beta.aclib.options;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;

import com.beust.jcommander.Parameter;


/**
 * Options object that defines arguments for Target Algorithm Execution
 * @author sjr
 *
 */
public class AlgorithmExecutionOptions extends AbstractOptions {

	@Parameter(names={"--algoExec", "--algo"}, description="Command String to execution", required=true)
	public String algoExec;
	
	@Parameter(names={"--execDir","--execdir"}, description="Working directory to execute algorithm in", required=true)
	public String algoExecDir;
	
	@Parameter(names="--deterministic", description="Whether the target algorithm is deterministic (0 no, 1 yes) [An integer due to backwards compatibility]")
	public int deterministic = 0;
	
	@Parameter(names="--targetAlgorithmEvaluator", description="System we should use to dispatch algorithm requests [ See manual but CLI  (via Command Line) is default and generally what you want ]")
	public String targetAlgorithmEvaluator = "CLI";
	
	public AlgorithmExecutionConfig getAlgorithmExecutionConfig(ParamConfigurationSpace p)
	{
		return new AlgorithmExecutionConfig(algoExec, algoExecDir, p, false, (deterministic > 0));
	}
	
	
}
