package ca.ubc.cs.beta.smac.ac.runners;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ca.ubc.cs.beta.ac.RunResult;
import ca.ubc.cs.beta.ac.config.RunConfig;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun;
import ca.ubc.cs.beta.smac.exceptions.TargetAlgorithmAbortException;

/**
 * Processes Algorithm Run Requests concurrently
 * 
 * 
 * @author seramage
 *
 */
public class ConcurrentAlgorithmRunner extends AbstractAlgorithmRunner {

	private int nThreads;
	public ConcurrentAlgorithmRunner(AlgorithmExecutionConfig execConfig,
			List<RunConfig> instanceConfigs, int nThreads) {
		super(execConfig, instanceConfigs);
		this.nThreads = nThreads;
	}

	@Override
	public synchronized List<AlgorithmRun> run() {
		//TODO It would be nice if this could detect an abort right away, as opposed
		//to simply waiting until all algorithms have ended
		//however this probably doesn't matter since they should all abort
		
		ExecutorService p = Executors.newFixedThreadPool(nThreads);
		
		try {
			for(AlgorithmRun run : runs)
			{
				p.submit(run);
			
			}
			
			
			p.shutdown();
			
			for(AlgorithmRun run : runs)
			{
				

				if (run.getRunResult().equals(RunResult.ABORT))
				{
					throw new TargetAlgorithmAbortException(run);
				}
			}
			
			
			try {
				p.awaitTermination(24000, TimeUnit.HOURS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		} finally
		{
			p.shutdown();
		}
		return runs;
		
	}

}
