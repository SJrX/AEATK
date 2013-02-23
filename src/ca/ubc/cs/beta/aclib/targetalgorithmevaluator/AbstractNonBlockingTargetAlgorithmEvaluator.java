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
public abstract class AbstractNonBlockingTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluator {

	
	ExecutorService execService;
	public AbstractNonBlockingTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig) {
		super(execConfig);
		this.execService = Executors.newCachedThreadPool();
	}
	
	@Override
	public final void notifyShutdown()
	{
		execService.shutdown();
		subtypeShutdown();
	}
	
	/**
	 * Template method for ensuring subtype gets notified. 
	 */
	protected abstract void subtypeShutdown();
	
	
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
			public void run()
			{
				try {
					List<AlgorithmRun> runs = AbstractNonBlockingTargetAlgorithmEvaluator.this.evaluateRun(runConfigs);
					
					handler.onSuccess(runs);
				} catch(RuntimeException e)
				{
					handler.onFailure(e);
				}
				
			}
		};
		
		execService.execute(run);
		
		

	}

	
}
