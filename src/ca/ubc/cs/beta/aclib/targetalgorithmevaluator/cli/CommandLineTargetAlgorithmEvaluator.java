package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.cli;

import java.io.File;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.ParameterException;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrunner.AlgorithmRunner;
import ca.ubc.cs.beta.aclib.algorithmrunner.AutomaticConfiguratorFactory;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractBlockingTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;

/**
 * Evalutes Given Run Configurations
 *
 */
public class CommandLineTargetAlgorithmEvaluator extends AbstractBlockingTargetAlgorithmEvaluator {
	
	
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	
	private final int observerFrequency;
	
	private final CommandLineTargetAlgorithmEvaluatorOptions options;
	
	/**
	 * Constructs CommandLineTargetAlgorithmEvaluator
	 * @param execConfig 			execution configuration of the target algorithm
	 * @param options	<code>true</code> if we should execute algorithms concurrently, <code>false</code> otherwise
	 */
	CommandLineTargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig, CommandLineTargetAlgorithmEvaluatorOptions options)
	{
		super(execConfig);
		this.observerFrequency = options.observerFrequency;
		log.debug("Initalized with the following Execution Configuration {} " , execConfig);
		this.concurrentExecution = options.concurrentExecution;
		if(observerFrequency < 50) throw new ParameterException("Observer Frequency can't be less than 50 ms");
		log.debug("Concurrent Execution {}", options.concurrentExecution);
		this.options = options;
		File execDir = new File(execConfig.getAlgorithmExecutionDirectory());
		if(!execDir.exists()) throw new ParameterException("The Algorithm Execution Directory does not exist (" + execConfig.getAlgorithmExecutionDirectory() + ")");
		if(!execDir.isDirectory()) throw new ParameterException("The Algorithm Execution Directory is NOT a directory (" + execConfig.getAlgorithmExecutionDirectory() + ")");
	}
	

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, CurrentRunStatusObserver obs)
	{
		
		if(runConfigs.size() == 0)
		{
			return Collections.emptyList();
		}
		
		AlgorithmRunner runner = getAlgorithmRunner(runConfigs,obs);
		List<AlgorithmRun> runs =  runner.run();
		addRuns(runs);
		runner.shutdownThreadPool();
		return runs;
	}
	

	private final boolean concurrentExecution; 
	
	/**
	 * Helper method which selects the AlgorithmRunner to use
	 * @param runConfigs 	runConfigs to evaluate
	 * @return	AlgorithmRunner to use
	 */
	private AlgorithmRunner getAlgorithmRunner(List<RunConfig> runConfigs,CurrentRunStatusObserver obs)
	{
		
		
		if(concurrentExecution)
		{
			log.debug("Using concurrent algorithm runner");
			return AutomaticConfiguratorFactory.getConcurrentAlgorithmRunner(execConfig,runConfigs,obs, options);
			
		} else
		{
			log.debug("Using single-threaded algorithm runner");
			return AutomaticConfiguratorFactory.getSingleThreadedAlgorithmRunner(execConfig,runConfigs,obs, options);
		}
	}

	
	@Override
	public boolean isRunFinal() {
		return false;
	}

	@Override
	public boolean areRunsPersisted() {
		return false;
	}

	@Override
	protected void subtypeShutdown() {
		//We don't create any ThreadPools currently
	}

	@Override
	public boolean areRunsObservable() {
		return true;
	}
	
	


}
