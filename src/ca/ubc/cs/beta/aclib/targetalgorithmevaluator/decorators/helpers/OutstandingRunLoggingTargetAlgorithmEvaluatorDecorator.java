package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.helpers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.ThreadSafe;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableAlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;

@ThreadSafe
/***
 * Tracks the start and completion time of each run and logs it to a file
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
public class OutstandingRunLoggingTargetAlgorithmEvaluatorDecorator extends AbstractTargetAlgorithmEvaluatorDecorator {

	private final long ZERO_TIME = System.currentTimeMillis();
	private String resultFile;
	
	private String nameOfRuns;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final double resolutionInMS;
	public OutstandingRunLoggingTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae, String resultFile, double resolutionInSeconds, String nameOfRuns) {
		super(tae);
		this.resultFile = resultFile;
		this.nameOfRuns = nameOfRuns;
		this.resolutionInMS = resolutionInSeconds * 1000;
	}

	@Override
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, final TargetAlgorithmEvaluatorRunObserver obs) {
		
		TargetAlgorithmEvaluatorRunObserver wrappedObs = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends KillableAlgorithmRun> runs) {
				obs.currentStatus(runs);
				processRuns(runs);
			}
			
		};
		
		return processRuns(tae.evaluateRun(processRunConfigs(runConfigs), wrappedObs));
	}
	
	
	private final ConcurrentHashMap<RunConfig, Double> startTime = new ConcurrentHashMap<RunConfig, Double>();
	private final ConcurrentHashMap<AlgorithmRun, Double> endTime =  new ConcurrentHashMap<AlgorithmRun, Double>();
	private final ConcurrentHashMap<RunConfig, Double> startWalltime = new ConcurrentHashMap<RunConfig, Double>();
	private final ConcurrentHashMap<RunConfig, Double> startCPUtime = new ConcurrentHashMap<RunConfig, Double>();
	
	
	private final long bucketTime(long time)
	{
		 return (long) ((long) ( (long) (time / resolutionInMS)) * resolutionInMS);  
	}
	

	/**
	 * Template method that is invoked with each run that complete
	 * 
	 * @param run process the run
	 * @return run that will replace it in the values returned to the client
	 */
	protected <K extends AlgorithmRun> K processRun(K run)
	{
		
		if(run.isRunCompleted())
		{
			synchronized(run.getRunConfig())
			{
				if(endTime.get(run) == null)
				{
					endTime.put(run, Math.max(0,(bucketTime(System.currentTimeMillis()) - ZERO_TIME) / 1000.0));
				}
				
				if(startWalltime.get(run) == null)
				{
					startWalltime.put(run.getRunConfig(), Math.max(0,(bucketTime(System.currentTimeMillis() - ((long) run.getWallclockExecutionTime())*1000) - ZERO_TIME) / 1000.0));
				}
				
				if(startCPUtime.get(run) == null)
				{
					startCPUtime.put(run.getRunConfig(), Math.max(0,(bucketTime(System.currentTimeMillis() - ((long) run.getRuntime())*1000) - ZERO_TIME) / 1000.0));
				}
				
					
			}
		}
		
		return run;
	}
	
	/**
	 * Template method that is invoked with each runConfig that we request
	 * @param rc the runconfig  being requested
	 * @return runConfig object to replace the run
	 */
	protected RunConfig processRun(RunConfig rc)
	{
		synchronized(rc)
		{
			if(startTime.get(rc) == null)
			{
				startTime.put(rc, Math.max(0,(bucketTime(System.currentTimeMillis()) - ZERO_TIME) / 1000.0));
			}
		}
	
		
		return rc;
	}
	
	
	protected final <K extends AlgorithmRun> List<K> processRuns(List<K> runs)
	{
		for(int i=0; i < runs.size(); i++)
		{
			runs.set(i, processRun(runs.get(i)));
		}
		
		return runs;
	}
	
	protected final List<RunConfig> processRunConfigs(List<RunConfig> runConfigs)
	{	
		runConfigs = new ArrayList<RunConfig>(runConfigs);
		for(int i=0; i < runConfigs.size(); i++)
		{
			runConfigs.set(i, processRun(runConfigs.get(i)));
		}
		return runConfigs;
	}
	
	

	@Override
	public final void evaluateRunsAsync(final List<RunConfig> runConfigs,
			final TargetAlgorithmEvaluatorCallback oHandler, final TargetAlgorithmEvaluatorRunObserver obs) {
		
		//We need to make sure wrapped versions are called in the same order
		//as there unwrapped versions.
	
		TargetAlgorithmEvaluatorCallback myHandler = new TargetAlgorithmEvaluatorCallback()
		{
			private final TargetAlgorithmEvaluatorCallback handler = oHandler;

			@Override
			public void onSuccess(List<AlgorithmRun> runs) {
					runs = processRuns(runs);			
					handler.onSuccess(runs);
			}

			@Override
			public void onFailure(RuntimeException t) {
					handler.onFailure(t);
			}
		};
		
		
		TargetAlgorithmEvaluatorRunObserver wrappedObs = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends KillableAlgorithmRun> runs) {				
				processRuns(runs);
				if(obs != null)
				{
					obs.currentStatus(runs);
				}
			}
			
		};
		
		tae.evaluateRunsAsync(processRunConfigs(runConfigs), myHandler, wrappedObs);

	}
	
	public void notifyShutdown()
	{
		tae.notifyShutdown();
		
		log.debug("Processing detailed run statistics to {} ", this.resultFile);
		
		
		if(this.startTime.size() > this.endTime.size())
		{
			log.warn("Some runs are still outstanding, it is possible that we are shutting down prematurely, started: {} , finished: {}", this.startTime.size(), this.endTime.size());
		}
		
		if(this.startTime.size() < this.endTime.size())
		{
			
			for(Entry<RunConfig, Double> ent : this.startTime.entrySet())
			{
				log.error("At " + ent.getValue() + " : " + ent.getKey() + " started.");
			}
			
			for(Entry<AlgorithmRun, Double> ent : this.endTime.entrySet())
			{
				log.error("At " + ent.getValue() + " : " + ent.getKey().getRunConfig() + " ended. ");
			}
			
			throw new IllegalStateException("[BUG]: Determined that more algorithms ended " + this.endTime.size() + " than started " + this.startTime.size());
		}
		
		
		ConcurrentSkipListMap<Double, StartEnd> startEndMap = new ConcurrentSkipListMap<Double, StartEnd>();
		
		
		Collection[] myDoubles = { this.startTime.values(), this.endTime.values(), this.startWalltime.values(), this.startCPUtime.values()  };
		
		
		
		for(Collection cod : myDoubles)
		{
			for(Double d : (Collection<Double>) cod)
			{
				StartEnd e = startEndMap.get(d);
				
				if(e == null)
				{
					startEndMap.put(d, new StartEnd());
				}
				
			}
		}
		
		for(Entry<RunConfig, Double> startTimes : this.startTime.entrySet())
		{
			startEndMap.get(startTimes.getValue()).startDispatch++;
		}
		
		for(Entry<AlgorithmRun, Double> endTimes : this.endTime.entrySet())
		{
			startEndMap.get(endTimes.getValue()).endDispatch++;
		}
		
		for(Entry<RunConfig, Double> startCPUTimes : this.startCPUtime.entrySet())
		{
			startEndMap.get(startCPUTimes.getValue()).startCPUtime++;
		}
		
		for(Entry<RunConfig, Double> startCPUTimes : this.startWalltime.entrySet())
		{
			startEndMap.get(startCPUTimes.getValue()).startWalltime++;
		}
		
		
		
		File f = new File(this.resultFile);
		try {
			FileWriter writer = new FileWriter(f);
			
			writer.write("Time, Started, Ending, Number of " + this.nameOfRuns+  "  Runs, Approximate Start Based on CPU Time, Approximate Start Based on Walltime, Number of Running By CPU Time, Number of Running By Walltime\n");
			
			
			int outstanding = 0;
			int outstandingCPU = 0;
			int outstandingWall = 0;
			for(Entry<Double, StartEnd> ent : startEndMap.entrySet())
			{
				outstanding += ent.getValue().startDispatch - ent.getValue().endDispatch;
				
				outstandingCPU += ent.getValue().startCPUtime - ent.getValue().endDispatch;
				outstandingWall += ent.getValue().startWalltime - ent.getValue().endDispatch;
				
				writer.write(ent.getKey() + "," +  ent.getValue().startDispatch + "," + ent.getValue().endDispatch + "," + outstanding + "," + ent.getValue().startCPUtime + "," + ent.getValue().startWalltime + "," + outstandingCPU + "," + outstandingWall + "\n");
			}
			
			writer.flush();
			writer.close();

		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		
		log.debug("Processing complete");
	}
	
	
	private static class StartEnd
	{
		public long startDispatch = 0;
		public long endDispatch = 0;
		public long startCPUtime = 0;
		public long startWalltime = 0;
		
	}
}
