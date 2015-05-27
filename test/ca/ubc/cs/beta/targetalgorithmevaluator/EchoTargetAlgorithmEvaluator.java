package ca.ubc.cs.beta.targetalgorithmevaluator;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.ExistingAlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
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

	
	
	public EchoTargetAlgorithmEvaluator()
	{
		this( new EchoTargetAlgorithmEvaluatorOptions());
	}
	
	public EchoTargetAlgorithmEvaluator( EchoTargetAlgorithmEvaluatorOptions options) {
		super();
	}
    
	
	public volatile double wallClockTime = 0;
	
	@Override
	public List<AlgorithmRunResult> evaluateRun(List<AlgorithmRunConfiguration> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		
		List<AlgorithmRunResult> results = new ArrayList<AlgorithmRunResult>();
		
		
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
			
			ParameterConfiguration config = rc.getParameterConfiguration();
			
			sb.append(config.get("solved")).append(",");
			sb.append(config.get("runtime")).append(",");
			sb.append(config.get("runlength")).append(",");
			sb.append(config.get("quality")).append(",");
			sb.append(config.get("seed"));
			
			
			
			results.add(ExistingAlgorithmRunResult.getRunFromString(rc, sb.toString(),wallClockTime));
			
			if(RunStatus.valueOf(config.get("solved")).equals(RunStatus.ABORT))
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
