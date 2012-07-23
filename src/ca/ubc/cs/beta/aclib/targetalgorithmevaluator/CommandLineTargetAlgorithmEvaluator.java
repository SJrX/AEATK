package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrunner.AlgorithmRunner;
import ca.ubc.cs.beta.aclib.algorithmrunner.AutomaticConfiguratorFactory;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

/**
 * Evalutes Given Run Configurations
 *
 */
public class CommandLineTargetAlgorithmEvaluator extends AbstractTargetAlgorithmEvaluator {
	
	
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Default Constructor
	 * @param execConfig	execution configuration of the target algorithm
	 */
	public CommandLineTargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig)
	{
		this(execConfig, true);
	}
	
	/**
	 * Constructs CommandLineTargetAlgorithmEvaluator
	 * @param execConfig 			execution configuration of the target algorithm
	 * @param concurrentExecution	<code>true</code> if we should execute algorithms concurrently, <code>false</code> otherwise
	 */
	public CommandLineTargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig, boolean concurrentExecution)
	{
		super(execConfig);
		log.debug("Initalized with the following Execution Configuration {}" , execConfig);
		this.concurrentExecution = concurrentExecution; 
	}
	
	
	
	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig run) 
	{
		return evaluateRun(Collections.singletonList(run));
	}
	
	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs)
	{
		AlgorithmRunner runner = getAlgorithmRunner(runConfigs);
		List<AlgorithmRun> runs =  runner.run();
		addRuns(runs);
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
	
	


}
