package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators;

import java.util.Collections;
import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;

/**
 * Utility TargetAlgorithmEvaluator that wraps two, and checks that they always return the same value.
 * 
 * Mainly used for testing and development.
 * 
 * @author Steve Ramage 
 *
 */
public class EqualTargetAlgorithmEvaluatorTester implements
		TargetAlgorithmEvaluator {

	private final TargetAlgorithmEvaluator tae1;
	private final TargetAlgorithmEvaluator tae2;

	public EqualTargetAlgorithmEvaluatorTester(TargetAlgorithmEvaluator tae1, TargetAlgorithmEvaluator tae2)
	{
		this.tae1 = tae1;
		this.tae2 = tae2;
	}
	
	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig run) {
		return this.evaluateRun(Collections.singletonList(run));	
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		
		List<AlgorithmRun> runTae1 = tae1.evaluateRun(runConfigs);
		List<AlgorithmRun> runTae2 = tae2.evaluateRun(runConfigs);

		if(runTae1.size() != runTae2.size()) throw new IllegalStateException("Run sizes did not match");
		
		for(int i=0; i<runTae1.size(); i++)
		{
			if(!runTae1.get(i).equals(runTae2.get(i)))
			{
				throw new IllegalStateException(runTae1.get(i) + " did not equals " + runTae2.get(i) );
				
			}
		}
		
		return runTae1;
	}

	@Override
	public int getRunCount() {

		if(tae1.getRunCount() != tae2.getRunCount()) throw new IllegalStateException("RunCount should have been the same between two target algorithm evaluators");
		return tae1.getRunCount();
	}

	@Override
	public int getRunHash() {
		if(tae1.getRunHash() != tae2.getRunHash())  throw new IllegalStateException("Run Hash should have been the same between two target algorithm evaluators");
		return tae1.getRunHash();
	}

	@Override
	public void seek(List<AlgorithmRun> runs) {
		tae1.seek(runs);
		tae2.seek(runs);

	}

	@Override
	public String getManualCallString(RunConfig runConfig) {
		return tae1.getManualCallString(runConfig);
	}

	@Override
	public void notifyShutdown() {
		
	}

}
