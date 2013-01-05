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

public abstract class AbstractDeferredTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluator implements DeferredTargetAlgorithmEvaluator{



	public AbstractDeferredTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig) {
		super(execConfig);
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		
		
		if(runConfigs.size() == 0) return Collections.emptyList();
		
		final Semaphore b = new Semaphore(0);
		
		final AtomicBoolean success = new AtomicBoolean();
		final AtomicReference<List<AlgorithmRun>> list = new AtomicReference<List<AlgorithmRun>>(); 
		final AtomicReference<RuntimeException> rt = new AtomicReference<RuntimeException>(); 
		
		
		evaluateRunsAsync(runConfigs, new TAECallback(){

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
			
		});

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
