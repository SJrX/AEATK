package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.functionality;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableAlgorithmRun;
import ca.ubc.cs.beta.aclib.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;

public class TerminateAllRunsOnFileDeleteTargetAlgorithmEvaluatorDecorator extends AbstractTargetAlgorithmEvaluatorDecorator {

	private final AtomicBoolean terminate = new AtomicBoolean(false);
	
	private final ScheduledExecutorService execService = Executors.newScheduledThreadPool(1,new SequentiallyNamedThreadFactory("Terminate Run FileWatcher", true));

	private final long POLL_FREQUENCY = 2;
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public TerminateAllRunsOnFileDeleteTargetAlgorithmEvaluatorDecorator(
			TargetAlgorithmEvaluator tae, final File fileToWatch) {
		super(tae);
		
		
		log.info("Terminating all runs if {} is deleted", fileToWatch);
		if(!fileToWatch.exists())
		{
			log.warn("File To Watch: {} does not exist, was it already deleted?", fileToWatch);
		}
		
		execService.scheduleAtFixedRate(new Runnable()
		{
			public void run()
			{
				if(!fileToWatch.exists())
				{
					if(!terminate.getAndSet(true))
					{
						log.info("File {} has been deleted, all runs will be terminated as quickly as possible", fileToWatch);
					}
				}
				
			}
		},POLL_FREQUENCY, POLL_FREQUENCY, TimeUnit.SECONDS);
		
		
	}
	
	@Override
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		return tae.evaluateRun(runConfigs, new TerminateAllRunOnFileDeleteTargetAlgorithmEvaluatorObserver(obs));
	}
	
	
	@Override
	public final void evaluateRunsAsync(List<RunConfig> runConfigs,
			final TargetAlgorithmEvaluatorCallback oHandler, TargetAlgorithmEvaluatorRunObserver obs) {
		
		//We need to make sure wrapped versions are called in the same order
		//as there unwrapped versions.
	
		TargetAlgorithmEvaluatorCallback myHandler = new TargetAlgorithmEvaluatorCallback()
		{
			private final TargetAlgorithmEvaluatorCallback handler = oHandler;

			@Override
			public void onSuccess(List<AlgorithmRun> runs) {		
					handler.onSuccess(runs);
			}

			@Override
			public void onFailure(RuntimeException t) {
					handler.onFailure(t);
			}
		};
		
		tae.evaluateRunsAsync(runConfigs, myHandler, new TerminateAllRunOnFileDeleteTargetAlgorithmEvaluatorObserver(obs));

	}
	
	private class TerminateAllRunOnFileDeleteTargetAlgorithmEvaluatorObserver implements TargetAlgorithmEvaluatorRunObserver
	{

		private TargetAlgorithmEvaluatorRunObserver obs;
		TerminateAllRunOnFileDeleteTargetAlgorithmEvaluatorObserver(TargetAlgorithmEvaluatorRunObserver obs)
		{
			this.obs = obs;
		}
		
		@Override
		public void currentStatus(List<? extends KillableAlgorithmRun> runs) 
		{
			
			if(TerminateAllRunsOnFileDeleteTargetAlgorithmEvaluatorDecorator.this.terminate.get())
			{
				
				for(KillableAlgorithmRun run : runs)
				{
					run.kill();
				}
			
			}
			
			if(obs != null)
			{
				obs.currentStatus(runs);
			}
		}
		
		
	}
	

	@Override
	protected void postDecorateeNotifyShutdown() {
		execService.shutdownNow();
		try 
		{
			execService.awaitTermination(24, TimeUnit.HOURS);
		} catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
			return;
		}
		
	}
}
