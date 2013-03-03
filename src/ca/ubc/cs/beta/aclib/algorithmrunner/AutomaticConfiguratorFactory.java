package ca.ubc.cs.beta.aclib.algorithmrunner;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.cli.CommandLineTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;

/**
 * Factory that creates various Algorithm Runners for things that request it.
 * 
 * </b>NOTE:</b> This factory is probably unnecessary, originally it was meant to do more, but things got side tracked.
 * @see CommandLineTargetAlgorithmEvaluator
 * @author sjr
 *
 */
public class AutomaticConfiguratorFactory {

	
	private static int maxThreads = Runtime.getRuntime().availableProcessors();
	
	/**
	 * Sets the maximum number of threads (defaults to the number of available processors)
	 * <p>
	 * <b>Note:</b> This method was only added to aid in Unit testing and speeding up some dummy
	 * algorithm runs. This mechanism in general does not work and should be avoided, if other TargetAlgorithmEvaluators
	 * need more control over this, that interface should be refactored.
	 *
	 * @param threads that can be executed directly
	 * @deprecated
	 */
	public static void setMaximumNumberOfThreads(int threads)
	{
		maxThreads = threads;
	}
	
	private static Logger log = LoggerFactory.getLogger(AutomaticConfiguratorFactory.class);
	/**
	 * Returns an AlgorithmRunner that executes all requests serially
	 * @param execConfig		execution configuration of the target algorithm
	 * @param runConfigs		run configurations to execute
	 * @return	algorithmrunner which will run it
	 */
	public static AlgorithmRunner getSingleThreadedAlgorithmRunner(AlgorithmExecutionConfig execConfig, List<RunConfig> runConfigs, CurrentRunStatusObserver obs)
	{
		return new SingleThreadedAlgorithmRunner(execConfig, runConfigs,obs);
	}
	
	/**
	 * Returns an AlgorithmRunner that executes as many requests concurrently as there are cores
	 * @param execConfig		execution configuration of the target algorithm
	 * @param runConfigs		run configurations to execute
	 * @return	algorithmrunner which will run it
	 */	
	public static AlgorithmRunner getConcurrentAlgorithmRunner(AlgorithmExecutionConfig execConfig, List<RunConfig> runConfigs, CurrentRunStatusObserver obs)
	{
		if(runConfigs.size() == 1)
		{
			return getSingleThreadedAlgorithmRunner(execConfig, runConfigs,obs);
		}
		return getConcurrentAlgorithmRunner(execConfig, runConfigs, maxThreads, obs);
	}
	
	/**
	 * Returns an AlgorithmRunner that executse up to nThreads concurrently
	 * @param execConfig		execution configuration of the target algorithm
	 * @param runConfigs		run configurations to execute
	 * @param nThreads			number of concurrent executions to allow
	 * @return	algorithmrunner which will run it
	 */
	public static AlgorithmRunner getConcurrentAlgorithmRunner(AlgorithmExecutionConfig execConfig, List<RunConfig> runConfigs, int nThreads, CurrentRunStatusObserver obs)
	{
		log.info("Concurrent Algorithm Runner created allowing {} threads");
		return new ConcurrentAlgorithmRunner(execConfig, runConfigs, nThreads, obs);
	}

}
