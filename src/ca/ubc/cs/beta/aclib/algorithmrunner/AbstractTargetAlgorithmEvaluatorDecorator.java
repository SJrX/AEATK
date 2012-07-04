package ca.ubc.cs.beta.aclib.algorithmrunner;

import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
/**
 * Abstract Decorator class for TargetAlgorithmEvalutator
 * 
 * @author Steve Ramage 
 *
 */
public abstract class AbstractTargetAlgorithmEvaluatorDecorator implements
		TargetAlgorithmEvaluator {

	private final TargetAlgorithmEvaluator tae;

	public AbstractTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae)
	{
		this.tae = tae;
		
	}
	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig run) {
		return tae.evaluateRun(run);
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		return tae.evaluateRun(runConfigs);
	}

	@Override
	public int getRunCount() {
		return tae.getRunCount();
	}

	@Override
	public int getRunHash() {
		return tae.getRunHash();
	}

	@Override
	public void seek(List<AlgorithmRun> runs) {
		tae.seek(runs);

	}

}
