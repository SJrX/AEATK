package ca.ubc.cs.beta.targetalgorithmevaluator;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.RunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.AbstractSyncTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.exceptions.TargetAlgorithmAbortException;

/**
 * Faster way of echoing results back
 * Only works with the paramEchoParamFile
 * 
 * 
 * @author Steve Ramage 
 *
 */
public class EchoTargetAlgorithmEvaluator  extends AbstractSyncTargetAlgorithmEvaluator  implements TargetAlgorithmEvaluator{

	private final boolean quickEval;
	
	public EchoTargetAlgorithmEvaluator()
	{
		this( new EchoTargetAlgorithmEvaluatorOptions());
	}
	
	public EchoTargetAlgorithmEvaluator( EchoTargetAlgorithmEvaluatorOptions options) {
		this.quickEval = options.quickEval;		
	}
    
	
	public volatile double wallClockTime = 0;
	
	@Override
	public List<AlgorithmRun> evaluateRun(List<AlgorithmRunConfiguration> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		
		List<AlgorithmRun> results = new ArrayList<AlgorithmRun>();
		
		
		 /* Configuration file generally looks something like this
		  *
		 * solved { SAT, UNSAT, TIMEOUT, CRASHED, ABORT, INVALID } [SAT]
				 * runtime [0,1000] [0]
				 * runlength [0,1000000][0]
				 * quality [0, 1000000] [0]
				 * seed [ -1,4294967295][1]i
		*/		 
		for(AlgorithmRunConfiguration rc : runConfigs)
		{
			StringBuilder sb = new StringBuilder();
			
			ParamConfiguration config = rc.getParameterConfiguration();
			
			sb.append(config.get("solved")).append(",");
			sb.append(config.get("runtime")).append(",");
			sb.append(config.get("runlength")).append(",");
			sb.append(config.get("quality")).append(",");
			sb.append(config.get("seed"));
			
			
			if(!this.quickEval)
			{
				
				double sleep = Double.valueOf(config.get("runtime"));
				
				try {
					Thread.sleep( (long) (sleep*1000));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				
			}
			
			results.add(ExistingAlgorithmRun.getRunFromString(rc, sb.toString(),wallClockTime));
			
			if(RunResult.valueOf(config.get("solved")).equals(RunResult.ABORT))
			{
				throw new TargetAlgorithmAbortException("Echoing abort");
			}
			
			
		}
		
		return results;
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
	protected void subtypeShutdown() {
		
	}

	@Override
	public boolean areRunsObservable() {
		return false;
	}

	
}
