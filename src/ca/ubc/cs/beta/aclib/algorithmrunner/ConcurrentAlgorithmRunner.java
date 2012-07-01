package ca.ubc.cs.beta.aclib.algorithmrunner;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.exceptions.TargetAlgorithmAbortException;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

/**
 * Processes Algorithm Run Requests concurrently
 * @author seramage
 */
public class ConcurrentAlgorithmRunner extends AbstractAlgorithmRunner {

	
	private int numberOfConcurrentExecutions;
	
	/**
	 * Default Constructor 
	 * @param execConfig	execution configuration of target algorithm
	 * @param runConfigs	run configurations to execute
	 * @param numberOfConcurrentExecutions	number of concurrent executions allowed
	 */
	public ConcurrentAlgorithmRunner(AlgorithmExecutionConfig execConfig,
			List<RunConfig> runConfigs, int numberOfConcurrentExecutions) {
		super(execConfig, runConfigs);
		this.numberOfConcurrentExecutions = numberOfConcurrentExecutions;
	}

	@Override
	public synchronized List<AlgorithmRun> run() {
		
		ExecutorService p = Executors.newFixedThreadPool(numberOfConcurrentExecutions);
		/*
		 * Runs all algorithms in the thread pool
		 * Tells it to shutdown
		 * Waits for it to shutdown
		 * 
		 */
		try {
			for(AlgorithmRun run : runs)
			{
				p.submit(run);	
			}
			
			p.shutdown();
			try {
				p.awaitTermination(24000, TimeUnit.DAYS);
				for(AlgorithmRun run : runs)
				{
					if (run.getRunResult().equals(RunResult.ABORT))
					{
						throw new TargetAlgorithmAbortException(run);
					}
				}
			} catch (InterruptedException e) {
				//TODO We probably need to actually abort properly
				//We can't just let something else do it, I think.
				//Additionally runs are in an invalid state at this point
				Thread.currentThread().interrupt();
			}
		} finally
		{
			p.shutdown();
		}
		return runs;
	}

}
