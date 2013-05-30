package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;

public abstract class AbstractAsyncTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluator implements TargetAlgorithmEvaluator{



	public AbstractAsyncTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig) {
		super(execConfig);
	}


	@Override
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, CurrentRunStatusObserver obs) {
		return evaluateRunSyncToAsync(runConfigs,this,obs);
	}

	/***
	 * Utility method people can use to turn adapt a synchronous call to an Asynchronous TAE.
	 * 
	 * @param runConfigs - outstanding run configurations
	 * @param tae - Target Algorithm Evaluator to run asynchronously and wait for the callback to execute with
	 * @return runs - Algorithm runs
	 */
	public static List<AlgorithmRun> evaluateRunSyncToAsync(List<RunConfig> runConfigs, TargetAlgorithmEvaluator tae, CurrentRunStatusObserver obs)
	{
		if(runConfigs.size() == 0) return Collections.emptyList();
		
		final Semaphore b = new Semaphore(0);
		
		final AtomicBoolean success = new AtomicBoolean();
		final AtomicReference<List<AlgorithmRun>> list = new AtomicReference<List<AlgorithmRun>>(); 
		final AtomicReference<RuntimeException> rt = new AtomicReference<RuntimeException>(); 
		
		
		tae.evaluateRunsAsync(runConfigs, new TAECallback(){

			@Override
			public void onSuccess(List<AlgorithmRun> runs) {
				success.set(true);
				list.set(runs);
				b.release();
			}

			@Override
			public void onFailure(RuntimeException t) {
				success.set(false);
				rt.set(t);
				b.release();
			}
			
		}, obs);

		b.acquireUninterruptibly();
		
		if(success.get())
		{
			return list.get();
		} else
		{
			throw rt.get();
		}
	}

}
