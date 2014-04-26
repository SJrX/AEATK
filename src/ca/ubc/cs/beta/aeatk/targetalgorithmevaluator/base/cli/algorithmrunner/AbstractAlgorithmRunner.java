package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.algorithmrunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunningAlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.kill.KillHandler;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.kill.StatusVariableKillHandler;
import ca.ubc.cs.beta.aeatk.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineAlgorithmRun;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorOptions;

/**
 * Used to Actually Run the Target Algorithm
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
abstract class AbstractAlgorithmRunner implements AlgorithmRunner {

	
	/**
	 * Stores the run configurations of the target algorithm
	 */
	protected final List<AlgorithmRunConfiguration> runConfigs;

	protected final List<Callable<AlgorithmRunResult>> runs;
	
	private final ExecutorService execService = Executors.newCachedThreadPool(new SequentiallyNamedThreadFactory("Command Line Algorithm Runner (not run) Thread"));
	
	private static final Logger log = LoggerFactory.getLogger(AbstractAlgorithmRunner.class); 
	
	//Set to true if we should terminate the observers
	private final AtomicBoolean shutdownRunnerRequested = new AtomicBoolean(false);
	
	private final Semaphore shutdownRunnerCompleted = new Semaphore(0);
	private final Semaphore updatedRunMapSemaphore = new Semaphore(0);
	
	/**
	 * Standard Constructor
	 * 
	 * @param execConfig	execution configuration of the target algorithm
	 * @param runConfigs	run configurations of the target algorithm
	 * @param obs 
	 */
	public AbstractAlgorithmRunner(final List<AlgorithmRunConfiguration> runConfigs, final TargetAlgorithmEvaluatorRunObserver obs, final CommandLineTargetAlgorithmEvaluatorOptions options, BlockingQueue<Integer> executionIDs)
	{
		if(runConfigs == null)
		{
			throw new IllegalArgumentException("Arguments cannot be null");
		}


		this.runConfigs = runConfigs;
		List<Callable<AlgorithmRunResult>> runs = new ArrayList<Callable<AlgorithmRunResult>>(runConfigs.size());
		
		//maps the run configs to the most recent update we have
		final ConcurrentHashMap<AlgorithmRunConfiguration,AlgorithmRunResult> runConfigToLatestUpdatedRunMap = new ConcurrentHashMap<AlgorithmRunConfiguration,AlgorithmRunResult>(runConfigs.size());
		
		//Maps runconfigs to the index in the supplied list
		final ConcurrentHashMap<AlgorithmRunConfiguration, Integer> runConfigToPositionInListMap = new ConcurrentHashMap<AlgorithmRunConfiguration,Integer>(runConfigs.size());
		
		int i=0; 
		
		
		//Initializes data structures for observation
		for(final AlgorithmRunConfiguration rc: runConfigs)
		{
			KillHandler killH = new StatusVariableKillHandler();
			

			runConfigToPositionInListMap.put(rc, i);
			runConfigToLatestUpdatedRunMap.put(rc, new RunningAlgorithmRunResult( rc, 0,0,0, rc.getProblemInstanceSeedPair().getSeed(), 0, killH));
			
			TargetAlgorithmEvaluatorRunObserver individualRunObserver = new TargetAlgorithmEvaluatorRunObserver()
			{
				@Override
				public void currentStatus(List<? extends AlgorithmRunResult> runs) {
					
					/**
					 * If the map already contains something for our runConfig that is completed, but
					 * we are not completed then there is some bug or other race condition.
					 * 
					 * TAEs should not notify us of an incompleted run after it has been marked completed..
					 */
					if(runConfigToLatestUpdatedRunMap.get(runs.get(0).getAlgorithmRunConfiguration()).isRunCompleted() && !runs.get(0).isRunCompleted())
					{
						StringBuilder sb = new StringBuilder("Current Run Status being notified: " + runs.get(0).getAlgorithmRunConfiguration());
						sb.append("\n Current status in table").append(runConfigToLatestUpdatedRunMap.get(runs.get(0).getAlgorithmRunConfiguration()).getAlgorithmRunConfiguration());
						IllegalStateException e = new IllegalStateException("RACE CONDITION: " + sb.toString());
						
						//We are logging this here because this may cause a dead lock somewhere else ( since the runs will never finish ), and the exception never handled.
						log.error("Some kind of race condition has occurred", e);
						e.printStackTrace();
						throw e;
					}
					
					runConfigToLatestUpdatedRunMap.put(runs.get(0).getAlgorithmRunConfiguration(), runs.get(0));
					
					updatedRunMapSemaphore.release();
				}
			};

			final Callable<AlgorithmRunResult> run = new CommandLineAlgorithmRun( rc,individualRunObserver, killH, options, executionIDs); 
			runs.add(run);
			i++;
		}
			
		
		this.runs = runs;
		i++;
		
		//==== Watches the map of runs in this group, and on changes notifies the observer
		Runnable runStatusWatchingThread = new Runnable()
		{

			@Override
			public void run() {
				
				try {
					while(true)
					{
						try {
							
							
							//This will release either because some run updated
							//or because we are done
							updatedRunMapSemaphore.acquire();
							
							updatedRunMapSemaphore.drainPermits();
							
							if(shutdownRunnerRequested.get())
							{
								break;
							}
							
							AlgorithmRunResult[] runs = new AlgorithmRunResult[runConfigs.size()];
							
							//We will quit if all runs are done
							boolean outstandingRuns = false;
							
							for(Entry<AlgorithmRunConfiguration,AlgorithmRunResult> entries : runConfigToLatestUpdatedRunMap.entrySet())
							{
								AlgorithmRunResult run = entries.getValue();
								if(run.getRunStatus().equals(RunStatus.RUNNING))
								{
									outstandingRuns = true;
								}
								runs[runConfigToPositionInListMap.get(entries.getKey())]=run;
							}
							
	
							try {
								List<AlgorithmRunResult> runList = Arrays.asList(runs);
								if(obs != null)
								{
									obs.currentStatus(runList);
								}
							} catch(RuntimeException e)
							{
								log.error("Error occured while notifying observer ", e);
								throw e;
							}
							
							if(!outstandingRuns)
							{
								
								break;
							}
							
							if(execService.isShutdown()) 
							{
								break;
							}
						} catch (InterruptedException e) 
						{
							Thread.currentThread().interrupt();
						}
					}
				} finally
				{
					shutdownRunnerCompleted.release();
				}
			}
			
		};
		
		execService.execute(runStatusWatchingThread);
		
		
	}
	
	
	@Override
	public abstract List<AlgorithmRunResult> run();
	
	@Override
	public void shutdownThreadPool() {
		this.execService.shutdown();
		try {
			//Want to force that the observer is done
			shutdownRunnerRequested.set(true);
			updatedRunMapSemaphore.release();
			
			//Wait for it to finish 
			shutdownRunnerCompleted.acquire();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
	}

	
}
