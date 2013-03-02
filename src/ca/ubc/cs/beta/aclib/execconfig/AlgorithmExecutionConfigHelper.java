package ca.ubc.cs.beta.aclib.execconfig;

import java.io.File;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;

public class AlgorithmExecutionConfigHelper {

	public static AlgorithmExecutionConfig getSingletonExecConfig()
	{
		return new AlgorithmExecutionConfig("foo",  (new File(".")).getAbsolutePath() , ParamConfigurationSpace.getSingletonConfigurationSpace(), false, false, 20);
	}

	/*
	public static AlgorithmExecutionConfig getParamEchoExecConfig()
	{
		//ParamEchoExecutor
		
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt"));
		
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		return new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
	}
	*/
}
