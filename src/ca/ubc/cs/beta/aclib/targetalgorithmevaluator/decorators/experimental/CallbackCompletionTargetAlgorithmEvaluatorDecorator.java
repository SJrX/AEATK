package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.experimental;

import java.util.Collections;
import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.concurrent.ReducableSemaphore;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.TAECallback;

/**
 * Experimental TAE that allows you to wait for all runs to be completed.
 * <b>USER NOTE:</b> This class may be removed in the future as there may be a better way,
 * or individual TAEs may use this. It's unclear at this time.
 * 
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
public class CallbackCompletionTargetAlgorithmEvaluatorDecorator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	
	private final ReducableSemaphore outstandingRunBlocks = new ReducableSemaphore(1);
	
	
	public CallbackCompletionTargetAlgorithmEvaluatorDecorator(
			TargetAlgorithmEvaluator tae) {
		super(tae);
	}


	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, CurrentRunStatusObserver obs) {
		try{
			outstandingRunBlocks.reducePermits();
			return tae.evaluateRun(runConfigs, obs);
		} finally
		{
			outstandingRunBlocks.release();
		}
		
	}


	@Override
	public void evaluateRunsAsync(List<RunConfig> runConfigs,
			final TAECallback handler, CurrentRunStatusObserver obs) {
		
		
		outstandingRunBlocks.reducePermits();
		TAECallback callback = new TAECallback()
		{

			@Override
			public void onSuccess(List<AlgorithmRun> runs) {
				
				handler.onSuccess(runs);
				outstandingRunBlocks.release();
			}

			@Override
			public void onFailure(RuntimeException t) {
				try {
					handler.onFailure(t);
				} finally
				{
					outstandingRunBlocks.release();
				}
				
			}
			
		};
		tae.evaluateRunsAsync(runConfigs, callback, obs);
		
	}

	/**
	 * Waits for there to be no outstanding runs
	 * <b>NOTE:</b> This isn't the same as waiting for a shutdown, this waits until the number of runs in progress is zero, it can later go higher again.
	 */
	public void waitForAllOutstandingRunsCompletion()
	{
		try {
			try {
				outstandingRunBlocks.acquire();
			} catch(InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return;
			}
		} finally
		{
			outstandingRunBlocks.release();
		}
	}
	
	
	
}
