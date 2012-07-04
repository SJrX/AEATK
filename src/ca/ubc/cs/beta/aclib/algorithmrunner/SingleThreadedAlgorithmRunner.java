package ca.ubc.cs.beta.aclib.algorithmrunner;

import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.exceptions.TargetAlgorithmAbortException;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

class SingleThreadedAlgorithmRunner extends AbstractAlgorithmRunner
{

	/**
	 * Default Constructor 
	 * @param execConfig	execution configuration of target algorithm
	 * @param runConfigs	run configurations to execute
	 */
	public SingleThreadedAlgorithmRunner(AlgorithmExecutionConfig execConfig,
			List<RunConfig> runConfigs) {
		super(execConfig, runConfigs);
		
	}

	@Override
	public List<AlgorithmRun> run() 	
	{
		for(AlgorithmRun run : runs)
		{
			run.run();
			
			if(run.getRunResult().equals(RunResult.ABORT))
			{
				throw new TargetAlgorithmAbortException(run);
			}
		}
			
		return runs;
	
		
	}

}
