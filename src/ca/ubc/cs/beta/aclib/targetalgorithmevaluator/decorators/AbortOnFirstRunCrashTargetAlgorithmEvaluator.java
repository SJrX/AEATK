package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators;

import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.exceptions.TargetAlgorithmAbortException;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;

/**
 * If the first run is a crash we will abort otherwise we ignore it
 * 
 * @author Steve Ramage 
 *
 */
public class AbortOnFirstRunCrashTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	public AbortOnFirstRunCrashTargetAlgorithmEvaluator(TargetAlgorithmEvaluator tae) {
		super(tae);
		
	}
	
	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig run) {
		return validate(super.evaluateRun(run));
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		return validate(super.evaluateRun(runConfigs));
	}
	

	private boolean firstRunChecked = false;
	private List<AlgorithmRun> validate(List<AlgorithmRun> runs)
	{
		
		if(firstRunChecked) 
		{
			return runs;
		} else
		{	
			
			firstRunChecked = true;
			
			//Note if runs.get(0) is non existant that is a bug with
			//the TAE implementation, NOT with this check.
			if(runs.get(0).getRunResult().equals(RunResult.CRASHED))
			{
				throw new TargetAlgorithmAbortException("First Run Crashed : " + runs.get(0).getRunConfig().toString()); 
			}
			
		}
		return runs;
	}
}
