package ca.ubc.cs.beta.smac.ac.runners;

import java.util.List;

import ca.ubc.cs.beta.ac.RunResult;
import ca.ubc.cs.beta.ac.config.RunConfig;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun;
import ca.ubc.cs.beta.smac.exceptions.TargetAlgorithmAbortException;

public class SingleThreadedAlgorithmRunner extends AbstractAlgorithmRunner
{

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
