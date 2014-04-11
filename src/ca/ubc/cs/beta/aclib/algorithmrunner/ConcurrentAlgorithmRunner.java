package ca.ubc.cs.beta.aclib.algorithmrunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.exceptions.TargetAlgorithmAbortException;

/**
 * Processes Algorithm Run Requests concurrently 
 * 
 * @author seramage
 * 
 */

class ConcurrentAlgorithmRunner extends AbstractAlgorithmRunner {

	
	private int numberOfConcurrentExecutions;
	private static final Logger log = LoggerFactory.getLogger(ConcurrentAlgorithmRunner.class);
	
	/**
	 * Default Constructor 
	 * @param execConfig	execution configuration of target algorithm
	 * @param runConfigs	run configurations to execute
	 * @param numberOfConcurrentExecutions	number of concurrent executions allowed
	 * @param obs 
	 * @param executionIDs 
	 */

	public ConcurrentAlgorithmRunner(List<RunConfig> runConfigs, int numberOfConcurrentExecutions, TargetAlgorithmEvaluatorRunObserver obs, CommandLineTargetAlgorithmEvaluatorOptions options, BlockingQueue<Integer> executionIDs) {
		super( runConfigs, obs, options,executionIDs);
		this.numberOfConcurrentExecutions = numberOfConcurrentExecutions;
	}

	@Override
	public synchronized List<AlgorithmRun> run() {
		
		log.debug("Creating Thread Pool Supporting " + numberOfConcurrentExecutions);
		
		ExecutorService p = Executors.newFixedThreadPool(numberOfConcurrentExecutions, new SequentiallyNamedThreadFactory("Command Line Target Algorithm Evaluator (ConcurrentRunner)"));
		/*
		 * Runs all algorithms in the thread pool
		 * Tells it to shutdown
		 * Waits for it to shutdown
		 * 
		 */
		try {
			
	
			
			try {
				
				List<AlgorithmRun> results = new ArrayList<AlgorithmRun>();
				List<Callable<AlgorithmRun>> runsToDo = runs;
				
				
				List<Future<AlgorithmRun>> futures = p.invokeAll(runsToDo);
				
				
				//p.invokeAll(runs);
				
				
				for(Future<AlgorithmRun> futRuns : futures)
				{
					AlgorithmRun run;
					try {
						run = futRuns.get();
					} catch (ExecutionException e) 
					{
						 throw new IllegalStateException("Unexpected exception occurred on call to Callable<AlgorithmRun>", e);
					}
					if (run.getRunResult().equals(RunResult.ABORT))
					{
						throw new TargetAlgorithmAbortException(run);
					}
					
					
					results.add(run);
					
					
					
				}
				
				return results;
				
				
			} catch (InterruptedException e) {
				//TODO We probably need to actually abort properly
				//We can't just let something else do it, I think.
				//Additionally runs are in an invalid state at this point
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Interrupted while processing runs");
			}
		} finally
		{
			
			p.shutdownNow();
		}
		
	}

}
