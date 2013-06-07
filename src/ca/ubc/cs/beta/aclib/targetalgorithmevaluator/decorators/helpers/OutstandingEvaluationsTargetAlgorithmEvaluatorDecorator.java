package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.helpers;

import java.util.List;

import net.jcip.annotations.ThreadSafe;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.concurrent.ReducableSemaphore;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;

/**
 * Target Algorithm Evaluator that implements the monitoring outstanding evalutions
 * <b>NOTE:</b> You need to be VERY careful with respect to the decorator order, things like the BoundedTargetAlgorithmEvaluator if applied on this will break
 * 
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
@ThreadSafe
public class OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	
	private final ReducableSemaphore outstandingRunBlocks = new ReducableSemaphore(1);
	
	
	public OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(
			TargetAlgorithmEvaluator tae) {
		super(tae);
	}


	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
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
			final TargetAlgorithmEvaluatorCallback handler, TargetAlgorithmEvaluatorRunObserver obs) {
		
		
		outstandingRunBlocks.reducePermits();
		TargetAlgorithmEvaluatorCallback callback = new TargetAlgorithmEvaluatorCallback()
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
	@Override
	public void waitForOutstandingEvaluations()
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
	
	@Override
	public int getNumberOfOutstandingEvaluations()
	{
		return 1 - outstandingRunBlocks.availablePermits();
	}
	
	
}
