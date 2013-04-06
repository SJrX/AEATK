package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.util.Collections;
import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.exceptions.TargetAlgorithmAbortException;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.TAECallback;
/**
 * Abstract Decorator class for TargetAlgorithmEvalutator
 * 
 * <b>Implementation Note:</b>  Almost every decorator that is doing something interesting, will
 * in fact redirect evaluateRun(RunConfig) to it's own local evaluateRun(List<RunConfig>) method.
 * You should not rely on evaluateRun() being called directly.
 *  
 * @author Steve Ramage 
 *
 */
public abstract class AbstractTargetAlgorithmEvaluatorDecorator implements
		TargetAlgorithmEvaluator {

	protected final TargetAlgorithmEvaluator tae;

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
		return evaluateRun(runConfigs, null);
	}


	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, CurrentRunStatusObserver obs) {
		return tae.evaluateRun(runConfigs, obs);
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
	@Override
	public String getManualCallString(RunConfig runConfig) {
		return tae.getManualCallString(runConfig);
	}
	
	@Override
	public void notifyShutdown()
	{
		tae.notifyShutdown();
	}
	
	@Override
	public boolean isRunFinal()
	{
		return tae.isRunFinal();
	}
	
	@Override
	public boolean areRunsPersisted()
	{
		return tae.areRunsPersisted();
	}
	
	@Override
	public boolean areRunsObservable()
	{
		return tae.areRunsObservable();
	}
	
	@Override
	public void evaluateRunsAsync(RunConfig runConfig, TAECallback handler) {
		tae.evaluateRunsAsync(runConfig, handler);
	}

	@Override
	public void evaluateRunsAsync(List<RunConfig> runConfigs, final TAECallback handler) {
		tae.evaluateRunsAsync(runConfigs, handler);
	}

	@Override
	public void evaluateRunsAsync(List<RunConfig> runConfigs,
			final TAECallback handler, CurrentRunStatusObserver obs) {
		tae.evaluateRunsAsync(runConfigs, handler, obs);
	}

	
	
}
