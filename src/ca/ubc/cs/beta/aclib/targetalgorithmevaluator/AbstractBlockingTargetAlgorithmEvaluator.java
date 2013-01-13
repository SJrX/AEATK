package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.TAECallback;

/**
 * Abstract type that simple blocks on asynchronous requests
 * 
 * Useful if you don't expect that there will be any gain with concurrency
 * 
 * @author sjr
 */
public abstract class AbstractBlockingTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluator {

	ExecutorService execService = Executors.newCachedThreadPool();
	
	public AbstractBlockingTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig) {
		super(execConfig);
	}

	@Override
	public void evaluateRunsAsync(RunConfig runConfig,
			TAECallback handler) {
		this.evaluateRunsAsync(Collections.singletonList(runConfig), handler);
	}

	@Override
	public  void evaluateRunsAsync(final List<RunConfig> runConfigs,
			final TAECallback handler) {
		
		Runnable run = new Runnable()
		{

			@Override
			public void run() {

				try {
					List<AlgorithmRun> runs;
					synchronized(this)
					{
						runs = AbstractBlockingTargetAlgorithmEvaluator.this.evaluateRun(runConfigs);
					}
					handler.onSuccess(runs);
				} catch(RuntimeException e)
				{
					handler.onFailure(e);
				}				
			}
			
		};
		
		
		
		if(this.areRunsPersisted())
		{
			//Need to ensure that the runs get checked for being done.
			run.run();
		} else
		{
			execService.execute(run);
		}

	}

	/**
	 * Template method for ensuring subtype gets notified. 
	 */
	protected abstract void subtypeShutdown();
	
	
	/**
	 * We must be notified of the shutdown, so we will prevent subtypes from overriding this method.
	 */
	@Override
	public final void notifyShutdown()
	{
		execService.shutdown();
		this.subtypeShutdown();
	}
}
