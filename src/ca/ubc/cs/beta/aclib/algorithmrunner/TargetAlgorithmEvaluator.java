package ca.ubc.cs.beta.aclib.algorithmrunner;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

/**
 * Evalutes Given Run Configurations
 *
 */
public class TargetAlgorithmEvaluator {
	
	/**
	 * Execution configuration of the target algorithm
	 */
	private final AlgorithmExecutionConfig execConfig;
	
	//Fields that should be cleaned up when we fix the runHashCode Generation
	private int runHashCodes = 0;
	private int runCount = 1;
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Default Constructor
	 * @param execConfig	execution configuration of the target algorithm
	 */
	public TargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig)
	{
		this(execConfig, true);
	}
	
	/**
	 * Constructs TargetAlgorithmEvaluator
	 * @param execConfig 			execution configuration of the target algorithm
	 * @param concurrentExecution	<code>true</code> if we should execute algorithms concurrently, <code>false</code> otherwise
	 */
	public TargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig, boolean concurrentExecution)
	{
		log.debug("Initalized with the following Execution Configuration {}" , execConfig);
		this.execConfig = execConfig;
		this.concurrentExecution = concurrentExecution; 
	}
	
	
	
	/**
	 * Evaluate a run configuration
	 * @param run RunConfiguration to evaluate
	 * @return	list containing the <code>AlgorithmRun<code>
	 */
	public List<AlgorithmRun> evaluateRun(RunConfig run) 
	{
		return evaluateRun(Collections.singletonList(run));
	}
	/**
	 * Evaluate a sequence of run configurations
	 * @param runConfigs a list containing run configurations to evaluate
	 * @return	list containing the <code>AlgorithmRun</code> objects in the same order as runConfigs
	 */
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs)
	{
		AlgorithmRunner runner = getAlgorithmRunner(runConfigs);
		List<AlgorithmRun> runs =  runner.run();
		return runs;
	}
	
	private final boolean concurrentExecution; 
	
	/**
	 * Helper method which selects the AlgorithmRunner to use
	 * @param runConfigs 	runConfigs to evaluate
	 * @return	AlgorithmRunner to use
	 */
	private AlgorithmRunner getAlgorithmRunner(List<RunConfig> runConfigs)
	{
		
		if(concurrentExecution)
		{
			log.info("Using concurrent algorithm runner");
			return AutomaticConfiguratorFactory.getConcurrentAlgorithmRunner(execConfig,runConfigs);
			
		} else
		{
			log.info("Using single-threaded algorithm runner");
			return AutomaticConfiguratorFactory.getSingleThreadedAlgorithmRunner(execConfig,runConfigs);
		}
	}
	
	/**
	 * Returns the number of target algorithm runs that we have executed
	 * @return	total number of runs evaluated
	 */
	public int getRunCount()
	{
		return runCount;
	}
	
	/**
	 * May optionally return a unique number that should roughly correspond to a unique sequence of run requests.
	 *  
	 * [i.e. A user seeing the same sequence of run codes, should be confident that the runs by the Automatic Configurator are 
	 * identical]. Note: This method is optional and may just return zero. 
	 * 
	 * @return runHashCode computed
	 */
	public int getRunHash()
	{
		return runHashCodes;
	}

	/**
	 * Sets the runCount to the given parameter
	 * 
	 * This is useful when we are restoring the state of SMAC 
	 * 
	 * @see ca.ubc.cs.beta.aclib.state.StateFactory
	 * @param runs
	 */
	public void seek(List<AlgorithmRun> runs) {
		runCount = runs.size();
		
	}
	
}
