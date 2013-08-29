package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.experimental.queuefacade.basic;

import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.experimental.queuefacade.general.TargetAlgorithmEvaluatorQueueResultContext;

public class BasicTargetAlgorithmEvaluatorQueueResultContext implements TargetAlgorithmEvaluatorQueueResultContext {

	private RuntimeException runtimeException;
	private List<RunConfig> runConfigs;
	private List<AlgorithmRun> runs;

	@Override
	public List<AlgorithmRun> getAlgorithmRuns() {
		return runs;
	}

	@Override
	public void setAlgorithmRuns(List<AlgorithmRun> runs) {
		this.runs = runs;
		
	}

	@Override
	public List<RunConfig> getRunConfigs() {
		return runConfigs;
	}

	@Override
	public void setRunConfigs(List<RunConfig> runConfigs) {
		this.runConfigs = runConfigs;
		
	}

	@Override
	public void setRuntimeException(RuntimeException t) {
		this.runtimeException=t;
		
	}

	@Override
	public RuntimeException getRuntimeException() {
		return this.runtimeException; 
	}

}
