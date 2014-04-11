package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.algorithmrunner;

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

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.exceptions.TargetAlgorithmAbortException;

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

	public ConcurrentAlgorithmRunner(List<AlgorithmRunConfiguration> runConfigs, int numberOfConcurrentExecutions, TargetAlgorithmEvaluatorRunObserver obs, CommandLineTargetAlgorithmEvaluatorOptions options, BlockingQueue<Integer> executionIDs) {
		super( runConfigs, obs, options,executionIDs);
		this.numberOfConcurrentExecutions = numberOfConcurrentExecutions;
	}

	@Override
	public synchronized List<AlgorithmRunResult> run() {
		
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
				
				List<AlgorithmRunResult> results = new ArrayList<AlgorithmRunResult>();
				List<Callable<AlgorithmRunResult>> runsToDo = runs;
				
				
				List<Future<AlgorithmRunResult>> futures = p.invokeAll(runsToDo);
				
				
				//p.invokeAll(runs);
				
				
				for(Future<AlgorithmRunResult> futRuns : futures)
				{
					AlgorithmRunResult run;
					try {
						run = futRuns.get();
					} catch (ExecutionException e) 
					{
						 throw new IllegalStateException("Unexpected exception occurred on call to Callable<AlgorithmRun>", e);
					}
					if (run.getRunStatus().equals(RunStatus.ABORT))
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
