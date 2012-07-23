package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators;

import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.exceptions.TargetAlgorithmAbortException;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;

/**
 * Modifies Target Algorithm Evaluators to treat CRASHES as aborts
 * @author Steve Ramage 
 *
 */
public class AbortOnCrashTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	public AbortOnCrashTargetAlgorithmEvaluator(TargetAlgorithmEvaluator tae) {
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
	

	private List<AlgorithmRun> validate(List<AlgorithmRun> runs)
	{
		
		for(AlgorithmRun run : runs)
		{
			if(run.getRunResult().equals(RunResult.CRASHED))
			{
				throw new TargetAlgorithmAbortException("Target Algorithm Run Reported Crashed: " + run.getRunConfig().toString());
			}
		}
		return runs;
	}
}
