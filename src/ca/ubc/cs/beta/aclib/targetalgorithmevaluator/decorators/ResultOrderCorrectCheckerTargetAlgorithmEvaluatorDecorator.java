package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators;

import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.TAECallback;

/**
 * This TAE Decorator ensures that the order of results in correct
 * 
 * Specifically it throws an error if order of the RunConfig objects in the  <code>List&gt;AlgorithmRun&lt;</code> doesn't match the order we submitted them in the <code>List&gt;RunConfig&lt;</code> 
 * 
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
@ThreadSafe
public class ResultOrderCorrectCheckerTargetAlgorithmEvaluatorDecorator extends AbstractTargetAlgorithmEvaluatorDecorator {
	
	public ResultOrderCorrectCheckerTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae)
	{
		super(tae);
	}
	
	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig run) {
		return evaluateRun(Collections.singletonList(run));
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		return evaluateRun(runConfigs, null);
	}
	
	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, CurrentRunStatusObserver obs) {
		List<AlgorithmRun> runs = tae.evaluateRun(Collections.unmodifiableList(runConfigs), obs);
		
		runOrderIsConsistent(runConfigs, runs);
		
		return runs;	
	}

	@Override
	public void evaluateRunsAsync(RunConfig runConfig, TAECallback handler) {
		evaluateRunsAsync(Collections.singletonList(runConfig), handler);
	}

	@Override
	public void evaluateRunsAsync(List<RunConfig> runConfigs, final TAECallback handler) {
		evaluateRunsAsync(runConfigs, handler, null);
	}

	@Override
	public void evaluateRunsAsync(final List<RunConfig> runConfigs,
			final TAECallback handler, CurrentRunStatusObserver obs) {
		TAECallback callback = new TAECallback()
		{

			@Override
			public void onSuccess(List<AlgorithmRun> runs) 
			{
				try {
					runOrderIsConsistent(runConfigs,runs);
					handler.onSuccess(runs);
				} catch(RuntimeException e)
				{
					handler.onFailure(e);
				}
			}

			@Override
			public void onFailure(RuntimeException t) 
			{
				handler.onFailure(t);
			}
			
		};
		
		
		tae.evaluateRunsAsync(Collections.unmodifiableList(runConfigs), callback, obs);
	}

	
	private void runOrderIsConsistent(List<RunConfig> runConfigs, List<AlgorithmRun> runs)
	{
		if(runConfigs.size() != runs.size())
		{
			throw new IllegalStateException("TAE did not return the correct sized results, submitted: " + runConfigs.size() + " got back: " + runs.size());
		}
		
		for(int i=0; i < runConfigs.size(); i++)
		{
			if(runs.get(i).getRunConfig().equals(runConfigs.get(i)))
			{
				continue;
			} else
			{
				throw new IllegalStateException("TAE did not return results in the correct order entry (" + i + ") was RunConfig: " + runConfigs.get(i) + " but the resulting run was :" + runs.get(i).getRunConfig());
			}
		}
	}


}
