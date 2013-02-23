package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;

/**
 * TAECallback that has a wait method() that lets you block until it is complete
 * 
 * @author Steve Ramage
 *
 */
public class WaitableTAECallback implements TAECallback {

	
	private final CountDownLatch completeCount = new CountDownLatch(1);
	private final TAECallback handler;
			
	public WaitableTAECallback(TAECallback handler)
	{
		this.handler = handler;
	}
	@Override
	public void onSuccess(List<AlgorithmRun> runs) {
		handler.onSuccess(runs);
		completeCount.countDown();

	}

	@Override
	public void onFailure(RuntimeException t) {
		handler.onFailure(t);
		completeCount.countDown();
	}

	/**
	 * Wait for the handlers to successfully finish
	 */
	public void waitForCompletion()
	{
		try 
		{
			completeCount.await();
		} catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}
	
}
