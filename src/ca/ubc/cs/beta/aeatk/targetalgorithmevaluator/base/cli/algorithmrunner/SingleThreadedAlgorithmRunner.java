package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.algorithmrunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.exceptions.TargetAlgorithmAbortException;

class SingleThreadedAlgorithmRunner extends AbstractAlgorithmRunner
{

	/**
	 * Default Constructor 
	 * @param execConfig	execution configuration of target algorithm
	 * @param runConfigs	run configurations to execute
	 * @param obs 
	 */
	public SingleThreadedAlgorithmRunner(List<AlgorithmRunConfiguration> runConfigs, TargetAlgorithmEvaluatorRunObserver obs, CommandLineTargetAlgorithmEvaluatorOptions options, BlockingQueue<Integer> executionIDs) {
		super( runConfigs,obs, options, executionIDs);
		
	}

	@Override
	public List<AlgorithmRunResult> run() 	
	{
		
		List<AlgorithmRunResult> runsToReturn = new ArrayList<AlgorithmRunResult>();

		for(Callable<AlgorithmRunResult> run : runs)
		{
			AlgorithmRunResult result;
			try {
				result = run.call();
			} catch(TargetAlgorithmAbortException e)
			{
				throw e;
			} catch (Exception e) {
				//log.error("Not sure what happened", e);
				throw new IllegalStateException("Unexpected exception occurred while trying to run algorithm", e);
			}
			
			if(result.getRunStatus().equals(RunStatus.ABORT))
			{
				throw new TargetAlgorithmAbortException(result);
			}
			
			runsToReturn.add(result);
		}
			
		return runsToReturn;	
	}

	
}
