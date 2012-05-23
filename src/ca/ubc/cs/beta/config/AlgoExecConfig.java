package ca.ubc.cs.beta.config;

import ca.ubc.cs.beta.configspace.ParamConfigurationSpace;

import com.beust.jcommander.Parameter;


public class AlgoExecConfig extends AbstractConfigToString {

	@Parameter(names={"--algoExec", "--algo"}, description="algorithm call to execute", required=true)
	public String algoExec;
	
	@Parameter(names={"--execDir","--execdir"}, description="Execution Directory for Algorithm", required=true)
	public String algoExecDir;
	
	
	 
	public AlgorithmExecutionConfig getAlgorithmExecutionConfig(ParamConfigurationSpace p)
	{
		return new AlgorithmExecutionConfig(algoExec, algoExecDir, p, false);
	}
	
	
}
