package ca.ubc.cs.beta.aclib.algorithmrunner;

import java.util.List;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

/**
 * Factory that creates various runners
 * 
 * </b>NOTE:</b> This factory is probably unnecessary, originally it was meant to do more, but things got side tracked.
 * @see CommandLineTargetAlgorithmEvaluator
 * @author sjr
 *
 */
public class AutomaticConfiguratorFactory {

	/**
	 * Returns an AlgorithmRunner that executes all requests serially
	 * @param execConfig		execution configuration of the target algorithm
	 * @param runConfigs		run configurations to execute
	 * @return	algorithmrunner which will run it
	 */
	public static AlgorithmRunner getSingleThreadedAlgorithmRunner(AlgorithmExecutionConfig execConfig, List<RunConfig> runConfigs)
	{
		return new SingleThreadedAlgorithmRunner(execConfig, runConfigs);
	}
	
	/**
	 * Returns an AlgorithmRunner that executes as many requests concurrently as there are cores
	 * @param execConfig		execution configuration of the target algorithm
	 * @param runConfigs		run configurations to execute
	 * @return	algorithmrunner which will run it
	 */	
	public static AlgorithmRunner getConcurrentAlgorithmRunner(AlgorithmExecutionConfig execConfig, List<RunConfig> runConfigs)
	{
		if(runConfigs.size() == 1)
		{
			return getSingleThreadedAlgorithmRunner(execConfig, runConfigs);
		}
		return getConcurrentAlgorithmRunner(execConfig, runConfigs, Runtime.getRuntime().availableProcessors());
	}
	
	/**
	 * Returns an AlgorithmRunner that executse up to nThreads concurrently
	 * @param execConfig		execution configuration of the target algorithm
	 * @param runConfigs		run configurations to execute
	 * @param nThreads			number of concurrent executions to allow
	 * @return	algorithmrunner which will run it
	 */
	public static AlgorithmRunner getConcurrentAlgorithmRunner(AlgorithmExecutionConfig execConfig, List<RunConfig> runConfigs, int nThreads)
	{
		return new ConcurrentAlgorithmRunner(execConfig, runConfigs, nThreads);
	}
}
