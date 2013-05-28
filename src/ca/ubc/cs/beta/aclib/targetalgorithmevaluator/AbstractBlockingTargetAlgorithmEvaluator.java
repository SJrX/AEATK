package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.TAECallback;

/**
 * Abstract TargetAlgorithmEvaluator that implements a basic form of asynchronous execution.
 * <br>
 * <b>Note:</b> Calls will just be made in a separate thread  
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
public abstract class AbstractBlockingTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluator {

	ExecutorService execService = Executors.newCachedThreadPool(new SequentiallyNamedThreadFactory("Abstract Blocking TAE Async Processing Thread"));
	
	public AbstractBlockingTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig) {
		super(execConfig);
	}

	@Override
	public void evaluateRunsAsync(RunConfig runConfig,
			TAECallback handler) {
		this.evaluateRunsAsync(Collections.singletonList(runConfig), handler, null);
	}

	@Override
	public  void evaluateRunsAsync(final List<RunConfig> runConfigs,
			final TAECallback handler) {
				evaluateRunsAsync(runConfigs, handler, null);
			}

	@Override
	public  void evaluateRunsAsync(final List<RunConfig> runConfigs,
			final TAECallback handler, final CurrentRunStatusObserver obs) {
		
		Runnable run = new Runnable()
		{

			@Override
			public void run() {

				try {
					List<AlgorithmRun> runs = AbstractBlockingTargetAlgorithmEvaluator.this.evaluateRun(runConfigs, obs);
					
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
