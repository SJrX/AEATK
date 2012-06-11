package ca.ubc.cs.beta.smac.ac.runners;

import java.util.List;

import ca.ubc.cs.beta.ac.config.RunConfig;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;

public class AutomaticConfiguratorFactory {

	public static AlgorithmRunner getSingleThreadedAlgorithmRunner(AlgorithmExecutionConfig execConfig, List<RunConfig> instanceConfigs)
	{
		return new SingleThreadedAlgorithmRunner(execConfig, instanceConfigs);
	}
	
	
	public static AlgorithmRunner getConcurrentAlgorithmRunner(AlgorithmExecutionConfig execConfig, List<RunConfig> instanceConfigs)
	{
		if(instanceConfigs.size() == 1)
		{
			return getSingleThreadedAlgorithmRunner(execConfig, instanceConfigs);
		}
		return getConcurrentAlgorithmRunner(execConfig, instanceConfigs, Runtime.getRuntime().availableProcessors());
	}
	
	public static AlgorithmRunner getConcurrentAlgorithmRunner(AlgorithmExecutionConfig execConfig, List<RunConfig> instanceConfigs, int nThreads)
	{
		return new ConcurrentAlgorithmRunner(execConfig, instanceConfigs, nThreads);
	}
}
