package ca.ubc.cs.beta.aeatk.algorithmrunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.RunResult;
import ca.ubc.cs.beta.aeatk.runconfig.RunConfig;
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
	public SingleThreadedAlgorithmRunner(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs, CommandLineTargetAlgorithmEvaluatorOptions options, BlockingQueue<Integer> executionIDs) {
		super( runConfigs,obs, options, executionIDs);
		
	}

	@Override
	public List<AlgorithmRun> run() 	
	{
		
		List<AlgorithmRun> runsToReturn = new ArrayList<AlgorithmRun>();

		for(Callable<AlgorithmRun> run : runs)
		{
			AlgorithmRun result;
			try {
				result = run.call();
			} catch (Exception e) {
				//log.error("Not sure what happened", e);
				throw new IllegalStateException("Unexpected exception occurred on call to Callable<AlgorithmRun>", e);
			}
			
			if(result.getRunResult().equals(RunResult.ABORT))
			{
				throw new TargetAlgorithmAbortException(result);
			}
			
			runsToReturn.add(result);
		}
			
		return runsToReturn;	
	}

	
}
