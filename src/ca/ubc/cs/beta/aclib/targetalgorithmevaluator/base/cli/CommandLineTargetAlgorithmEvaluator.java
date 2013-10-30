package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli;

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
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractSyncTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;

/**
 * Evalutes Given Run Configurations
 *
 */
public class CommandLineTargetAlgorithmEvaluator extends AbstractSyncTargetAlgorithmEvaluator {
	
	
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	
	private final int observerFrequency;
	
	private final CommandLineTargetAlgorithmEvaluatorOptions options;
	
	/**
	 * Constructs CommandLineTargetAlgorithmEvaluator
	 * @param execConfig 			execution configuration of the target algorithm
	 * @param options	<code>true</code> if we should execute algorithms concurrently, <code>false</code> otherwise
	 */
	CommandLineTargetAlgorithmEvaluator(CommandLineTargetAlgorithmEvaluatorOptions options)
	{
		
		this.observerFrequency = options.observerFrequency;
		this.concurrentExecution = options.concurrentExecution;
		if(observerFrequency < 50) throw new ParameterException("Observer Frequency can't be less than 50 ms");
		log.debug("Concurrent Execution {}", options.concurrentExecution);
		this.options = options;
		
	}
	

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs)
	{
		
		if(runConfigs.size() == 0)
		{
			return Collections.emptyList();
		}
		AlgorithmRunner runner =null;
		
		try {
		runner = getAlgorithmRunner(runConfigs,obs);
		List<AlgorithmRun> runs =  runner.run();
		addRuns(runs);
		return runs;
		} finally
		{
			if(runner != null)
			{
				runner.shutdownThreadPool();
			}
		}
		
	
	}
	

	private final boolean concurrentExecution; 
	
	/**
	 * Helper method which selects the AlgorithmRunner to use
	 * @param runConfigs 	runConfigs to evaluate
	 * @return	AlgorithmRunner to use
	 */
	private AlgorithmRunner getAlgorithmRunner(List<RunConfig> runConfigs,TargetAlgorithmEvaluatorRunObserver obs)
	{
		
		
		if(concurrentExecution && options.cores > 1)
		{
			log.debug("Using concurrent algorithm runner");
			return AutomaticConfiguratorFactory.getConcurrentAlgorithmRunner(runConfigs,obs, options);
			
		} else
		{
			log.debug("Using single-threaded algorithm runner");
			return AutomaticConfiguratorFactory.getSingleThreadedAlgorithmRunner(runConfigs,obs, options);
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
