package ca.ubc.cs.beta.config;

import ca.ubc.cs.beta.ac.config.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.configspace.ParamConfigurationSpace;

import com.beust.jcommander.Parameter;


public class AlgoExecConfig {

	@Parameter(names="--algoExec, --algo", description="algorithm call to execute", required=true)
	public String algoExec;
	
	@Parameter(names="--execDir", description="Execution Directory for Algorithm")
	public String algoExecDir;
	
	
	 
	public AlgorithmExecutionConfig getAlgorithmExecutionConfig(ParamConfigurationSpace p)
	{
		return new AlgorithmExecutionConfig(algoExec, algoExecDir, p, false);
	}
	
	
}
