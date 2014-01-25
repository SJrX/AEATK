package ca.ubc.cs.beta.aclib.algorithmrunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.algorithmrun.RunningAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillHandler;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.StatusVariableKillHandler;
import ca.ubc.cs.beta.aclib.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli.CommandLineAlgorithmRun;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorOptions;

/**
 * Used to Actually Run the Target Algorithm
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
abstract class AbstractAlgorithmRunner implements AlgorithmRunner {

	/**
	 * Stores the target algorithm execution configuration
	 */
	protected final AlgorithmExecutionConfig execConfig;
	
	/**
	 * Stores the run configurations of the target algorithm
	 */
	protected final List<RunConfig> runConfigs;

	protected final List<AlgorithmRun> runs;
	
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
	public AbstractAlgorithmRunner(AlgorithmExecutionConfig execConfig,final List<RunConfig> runConfigs, final TargetAlgorithmEvaluatorRunObserver obs, final CommandLineTargetAlgorithmEvaluatorOptions options, BlockingQueue<Integer> executionIDs)
	{
		if(execConfig == null || runConfigs == null)
		{
			throw new IllegalArgumentException("Arguments cannot be null");
		}

		this.execConfig = execConfig;
		this.runConfigs = runConfigs;
		List<AlgorithmRun> runs = new ArrayList<AlgorithmRun>(runConfigs.size());
		
		//maps the run configs to the most recent update we have
		final ConcurrentHashMap<RunConfig,KillableAlgorithmRun> runConfigToLatestUpdatedRunMap = new ConcurrentHashMap<RunConfig,KillableAlgorithmRun>(runConfigs.size());
		
		//Maps runconfigs to the index in the supplied list
		final ConcurrentHashMap<RunConfig, Integer> runConfigToPositionInListMap = new ConcurrentHashMap<RunConfig,Integer>(runConfigs.size());
		
		int i=0; 
		
		
		//Initializes data structures for observation
		for(final RunConfig rc: runConfigs)
		{
			KillHandler killH = new StatusVariableKillHandler();
			
			runConfigToPositionInListMap.put(rc, i);
			runConfigToLatestUpdatedRunMap.put(rc, new RunningAlgorithmRun(execConfig, rc, 0,0,0, rc.getProblemInstanceSeedPair().getSeed(), 0, killH));
			
			TargetAlgorithmEvaluatorRunObserver individualRunObserver = new TargetAlgorithmEvaluatorRunObserver()
			{
				@Override
				public void currentStatus(List<? extends KillableAlgorithmRun> runs) {
					
					/**
					 * If the map already contains something for our runConfig that is completed, but
					 * we are not completed then there is some bug or other race condition.
					 * 
					 * TAEs should not notify us of an incompleted run after it has been marked completed..
					 */
					if(runConfigToLatestUpdatedRunMap.get(runs.get(0).getRunConfig()).isRunCompleted() && !runs.get(0).isRunCompleted())
					{
						StringBuilder sb = new StringBuilder("Current Run Status being notified: " + runs.get(0).getRunConfig());
						sb.append("\n Current status in table").append(runConfigToLatestUpdatedRunMap.get(runs.get(0).getRunConfig()).getRunConfig());
						IllegalStateException e = new IllegalStateException("RACE CONDITION: " + sb.toString());
						
						//We are logging this here because this may cause a dead lock somewhere else ( since the runs will never finish ), and the exception never handled.
						log.error("Some kind of race condition has occurred", e);
						e.printStackTrace();
						throw e;
					}
					
					runConfigToLatestUpdatedRunMap.put(runs.get(0).getRunConfig(), runs.get(0));
					
					updatedRunMapSemaphore.release();
				}
			};
		
			
			final AlgorithmRun run = new CommandLineAlgorithmRun(execConfig, rc,individualRunObserver, killH, options, executionIDs); 
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
				
				while(true)
				{
					try {
						
					
						try {
							
							
							//This will release either because some run updated
							//or because we are done
							updatedRunMapSemaphore.acquire();
							
							updatedRunMapSemaphore.drainPermits();
							
							if(shutdownRunnerRequested.get())
							{
								break;
							}
							
							KillableAlgorithmRun[] runs = new KillableAlgorithmRun[runConfigs.size()];
							
							//We will quit if all runs are done
							boolean outstandingRuns = false;
							
							for(Entry<RunConfig,KillableAlgorithmRun> entries : runConfigToLatestUpdatedRunMap.entrySet())
							{
								KillableAlgorithmRun run = entries.getValue();
								if(run.getRunResult().equals(RunResult.RUNNING))
								{
									outstandingRuns = true;
								}
								runs[runConfigToPositionInListMap.get(entries.getKey())]=run;
							}
							
	
							try {
								List<KillableAlgorithmRun> runList = Arrays.asList(runs);
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
					} finally
					{
						shutdownRunnerCompleted.release();
					}
				}
				
			
			}
			
		};
		
		execService.submit(runStatusWatchingThread);
		
		
	}
	
	
	@Override
	public abstract List<AlgorithmRun> run();
	
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
