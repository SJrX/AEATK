package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.analytic;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractSyncTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.exceptions.TargetAlgorithmAbortException;

public class AnalyticTargetAlgorithmEvaluator extends AbstractSyncTargetAlgorithmEvaluator implements
		TargetAlgorithmEvaluator {

	private final AnalyticFunctions func;

	private final Logger log = LoggerFactory.getLogger(getClass());
	public AnalyticTargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig, AnalyticFunctions func) {
		super(execConfig);
		this.func = func;
	}

	@Override
	public boolean isRunFinal() {
		return true;
	}

	@Override
	public boolean areRunsPersisted() {
		return false;
	}

	@Override
	public boolean areRunsObservable() {
		return false;
	}

	@Override
	protected void subtypeShutdown() {
		log.info("Global minima for camelback function are near (-0.0898, 0.7126) & (0,0898, -0.7126)");
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs,
			TargetAlgorithmEvaluatorRunObserver obs) {
		try{
			List<AlgorithmRun> ar = new ArrayList<AlgorithmRun>(runConfigs.size());
			
			for(RunConfig rc : runConfigs)
			{ 
				double time = func.evaluate(Double.valueOf(rc.getParamConfiguration().get("x")), Double.valueOf(rc.getParamConfiguration().get("y")));
								
				if(time >= rc.getCutoffTime())
				{
					ar.add(new ExistingAlgorithmRun(execConfig, rc, RunResult.TIMEOUT,  rc.getCutoffTime() ,-1,0, rc.getProblemInstanceSeedPair().getSeed()));
				} else
				{
					ar.add(new ExistingAlgorithmRun(execConfig, rc, RunResult.SAT,  time ,-1,0, rc.getProblemInstanceSeedPair().getSeed()));
				}
				this.runCount.incrementAndGet();
			}
			return ar;
		}
		catch(RuntimeException e){
			throw new TargetAlgorithmAbortException("Error while evaluating function", e);
		}
	}
	

}
