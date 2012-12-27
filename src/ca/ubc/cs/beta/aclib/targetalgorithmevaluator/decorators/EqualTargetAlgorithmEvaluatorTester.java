package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators;

import java.util.Collections;
import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.TAECallback;

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
			
			if(Math.abs(runTae1.get(i).getRuntime() - runTae2.get(i).getRuntime()) > 0.1) throw new IllegalStateException("Runtimes did not agree");
			if(runTae1.get(i).getResultSeed() != runTae2.get(i).getResultSeed()) throw new IllegalStateException("Result Seeds did not agree");
			if(Math.abs(runTae1.get(i).getQuality() - runTae2.get(i).getQuality()) > 0.1) throw new IllegalStateException("Quality did not agree");
			if(!runTae1.get(i).getRunResult().equals(runTae2.get(i).getRunResult())) throw new IllegalStateException("Run Results did not agree");
			
			
			
			
			
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
		tae1.notifyShutdown();
		tae2.notifyShutdown();
	}

	@Override
	public void evaluateRunsAsync(RunConfig runConfig, TAECallback handler) {
		throw new UnsupportedOperationException("This TAE does not support Asynchronous Execution at the moment");
		
	}

	@Override
	public void evaluateRunsAsync(List<RunConfig> runConfigs,
			TAECallback handler) {
		throw new UnsupportedOperationException("This TAE does not support Asynchronous Execution at the moment");
	}

	@Override
	public boolean isRunFinal() {
		return false;
	}

	@Override
	public boolean areRunsPersisteted() {
		return false;
	}

}
