package ca.ubc.cs.beta.aclib.algorithmrunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.CommandLineAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.algorithmrun.RunningAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillHandler;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.StatusVariableKillHandler;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;

/**
 * Used to Actually Run the Target Algorithm
 * @author sjr
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
	
	private final ExecutorService execService = Executors.newCachedThreadPool();
	
	private static final Logger log = LoggerFactory.getLogger(AbstractAlgorithmRunner.class); 
	
	/**
	 * Standard Constructor
	 * 
	 * @param execConfig	execution configuration of the target algorithm
	 * @param runConfigs	run configurations of the target algorithm
	 * @param obs 
	 */
	public AbstractAlgorithmRunner(AlgorithmExecutionConfig execConfig,final List<RunConfig> runConfigs, final CurrentRunStatusObserver obs)
	{
		if(execConfig == null || runConfigs == null)
		{
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		
		this.execConfig = execConfig;
		this.runConfigs = runConfigs;
		List<AlgorithmRun> runs = new ArrayList<AlgorithmRun>(runConfigs.size());
		
		final ConcurrentHashMap<RunConfig,KillableAlgorithmRun> runStatus = new ConcurrentHashMap<RunConfig,KillableAlgorithmRun>(runConfigs.size());
		final ConcurrentHashMap<RunConfig, Integer> listIndex = new ConcurrentHashMap<RunConfig,Integer>(runConfigs.size());
		final Semaphore changes = new Semaphore(0);
		final Semaphore changeProcess = new Semaphore(0);
		int i=0; 
		for(final RunConfig rc: runConfigs)
		{
			KillHandler killH = new StatusVariableKillHandler();
			listIndex.put(rc, i);
			
			runStatus.put(rc, new RunningAlgorithmRun(execConfig, rc, "RUNNING,0.0,0,0," + rc.getProblemInstanceSeedPair().getSeed(), killH));
			
			CurrentRunStatusObserver individualRunObserver = new CurrentRunStatusObserver()
			{
				@Override
				public void currentStatus(List<? extends KillableAlgorithmRun> runs) {
					runStatus.put(runs.get(0).getRunConfig(), runs.get(0));
					changes.release();
				}
			};
		
			
			final AlgorithmRun run = new CommandLineAlgorithmRun(execConfig, rc,individualRunObserver, killH); 
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
						
						changes.acquire();
						changes.drainPermits();
						KillableAlgorithmRun[] runs = new KillableAlgorithmRun[runConfigs.size()];
						//We will quit if all runs are done
						boolean outstandingRuns = false;
						for(Entry<RunConfig,KillableAlgorithmRun> entries : runStatus.entrySet())
						{
							KillableAlgorithmRun run = entries.getValue();
							if(run.getRunResult().equals(RunResult.RUNNING))
							{
								outstandingRuns = true;
							}
							runs[listIndex.get(entries.getKey())]=run;
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
						//Thread.sleep(100);
						changeProcess.release(runs.length);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					
				}
				
				
			}
			
		};
		
		execService.submit(runStatusWatchingThread);
		
		
	}
	
	
	@Override
	public abstract List<AlgorithmRun> run();
	
	public static void shutdown()
	{
		//execService.shutdownNow();
	}
	
}
