package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.ParameterException;

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.AbstractAsyncTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.algorithmrunner.AlgorithmRunner;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.algorithmrunner.AutomaticConfiguratorFactory;

/**
 * Evalutes Given Run Configurations
 *
 */
public class CommandLineTargetAlgorithmEvaluator extends AbstractAsyncTargetAlgorithmEvaluator {
	
	
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	
	private final int observerFrequency;
	
	private final CommandLineTargetAlgorithmEvaluatorOptions options;
	
	private final BlockingQueue<Integer> executionIDs; 
	
	private final ExecutorService asyncExecService;
	
	private final ExecutorService commandLineAlgorithmRunExecutorService;
	private final ExecutorService observerExecutorService = Executors.newCachedThreadPool(new SequentiallyNamedThreadFactory("Command Line Target Algorithm Evaluator Observer Threads", true));
	
	private final Semaphore asyncExecutions;
	
	private final AtomicBoolean shutdown = new AtomicBoolean(false);
	
	
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
		log.trace("Concurrent Execution {}", options.concurrentExecution);
		this.options = options;
		
		executionIDs = new ArrayBlockingQueue<Integer>(options.cores);
		this.asyncExecService = Executors.newFixedThreadPool(options.cores, new SequentiallyNamedThreadFactory("Command Line Target Algorithm Evaluator Request Processor"));
		
		this.commandLineAlgorithmRunExecutorService = Executors.newCachedThreadPool(new SequentiallyNamedThreadFactory("Command Line Target Algorithm Evaluator (ConcurrentRunner)", true));
		
		
		this.asyncExecutions = new Semaphore(options.cores,true);
		
		for(int i = 0; i < options.cores ; i++)
		{
			executionIDs.add(Integer.valueOf(i));
			
		}
	}
	

	@Override
	public void evaluateRunsAsync(final List<AlgorithmRunConfiguration> runConfigs,final  TargetAlgorithmEvaluatorCallback taeCallback, final TargetAlgorithmEvaluatorRunObserver runStatusObserver) 
	{
		
		if(runConfigs.size() == 0)
		{
			taeCallback.onSuccess(Collections.<AlgorithmRunResult> emptyList());
			return;
		}
		
		
		try {
			this.asyncExecutions.acquire();
		} catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
			taeCallback.onFailure(new IllegalStateException("Request interrupted", e));
			return;
		}
		
		
		this.asyncExecService.execute(new Runnable()
		{

			@Override
			public void run() {
				

				AlgorithmRunner runner =null;
				
				List<AlgorithmRunResult> runs = null;
				
				try 
				{
					try {
						runner = getAlgorithmRunner(runConfigs,runStatusObserver);
						runs =  runner.run(commandLineAlgorithmRunExecutorService);
					} finally
					{
						asyncExecutions.release();
						if(runner != null)
						{
							runner.shutdownThreadPool();
						}
						
					}
				} catch(RuntimeException e)
				{
					taeCallback.onFailure(e);
					return;
				} catch(Throwable e)
				{
					taeCallback.onFailure(new IllegalStateException("Unexpected Throwable:", e));
					if(e instanceof Error)
					{
						throw e;
					}
					
					return;
				}
				
				addRuns(runs);
			
				try {
					if(runStatusObserver != null)
					{
						runStatusObserver.currentStatus(runs);
					}
					taeCallback.onSuccess(runs);
				} catch(RuntimeException e)
				{
					taeCallback.onFailure(e);
				} catch(Throwable e)
				{
					taeCallback.onFailure(new IllegalStateException("Unexpected Throwable:", e));
					
					if(e instanceof Error)
					{
						throw e;
					}
				}
				
				
				
				
				
			}
			
		});
		
	}
	


	private final boolean concurrentExecution; 
	
	/**
	 * Helper method which selects the AlgorithmRunner to use
	 * @param runConfigs 	runConfigs to evaluate
	 * @return	AlgorithmRunner to use
	 */
	private AlgorithmRunner getAlgorithmRunner(List<AlgorithmRunConfiguration> runConfigs,TargetAlgorithmEvaluatorRunObserver obs)
	{
		
		
		if(concurrentExecution && options.cores > 1)
		{

			log.trace("Using concurrent algorithm runner");

			return AutomaticConfiguratorFactory.getConcurrentAlgorithmRunner(runConfigs,obs, options,executionIDs, this.observerExecutorService);
			
		} else
		{
			log.trace("Using single-threaded algorithm runner");

			return AutomaticConfiguratorFactory.getSingleThreadedAlgorithmRunner(runConfigs,obs, options,executionIDs, this.observerExecutorService);
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
	public boolean areRunsObservable() {
		return true;
	}


	@Override
	public void notifyShutdown() {
		
		
		try {
			this.asyncExecService.shutdown();
			this.commandLineAlgorithmRunExecutorService.shutdown();
			this.observerExecutorService.shutdown();
			
			log.debug("Awaiting Termination of existing command line algorithm runs");

			boolean terminated = this.asyncExecService.awaitTermination(10, TimeUnit.SECONDS);
			
			while(!terminated)
			{
				log.warn("Termination of target algorithm evaluator failed, outstanding runs must still exist, ");
				terminated = this.asyncExecService.awaitTermination(10, TimeUnit.MINUTES);
			}
			
			
			terminated = this.commandLineAlgorithmRunExecutorService.awaitTermination(10, TimeUnit.SECONDS);
			
			while(!terminated)
			{
				log.warn("Termination of target algorithm evaluator failed (CLI Runs), outstanding runs must still exist, ");
				terminated = this.commandLineAlgorithmRunExecutorService.awaitTermination(10, TimeUnit.MINUTES);
			}
			
			terminated = this.observerExecutorService.awaitTermination(10, TimeUnit.SECONDS);
			
			while(!terminated)
			{
				log.warn("Termination of target algorithm evaluator failed (CLI Runs), outstanding runs must still exist, ");
				terminated = this.observerExecutorService.awaitTermination(10, TimeUnit.MINUTES);
			}
			
			
			
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			this.asyncExecService.shutdownNow();
			this.commandLineAlgorithmRunExecutorService.shutdownNow();
			this.observerExecutorService.shutdownNow();
			
			return;
			
		}
	}


	
	
	


}
