package ca.ubc.cs.beta.aclib.algorithmrunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.CommandLineAlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

/**
 * Used to Actually Run the Target Algorithm
 * @author sjr
 */
abstract class AbstractAlgorithmRunner implements AlgorithmRunner {

	/**
	 * Stores the target algorithm execution configuration
	 */
	protected final AlgorithmExecutionConfig execConfig;
	
	/**
	 * Stores the run configurations of the target algorithm
	 */
	protected final List<RunConfig> runConfigs;

	protected final List<AlgorithmRun> runs;
	
	/**
	 * Standard Constructor
	 * 
	 * @param execConfig	execution configuration of the target algorithm
	 * @param runConfigs	run configurations of the target algorithm
	 */
	public AbstractAlgorithmRunner(AlgorithmExecutionConfig execConfig, List<RunConfig> runConfigs)
	{
		if(execConfig == null || runConfigs == null)
		{
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		
		this.execConfig = execConfig;
		this.runConfigs = runConfigs;
		List<AlgorithmRun> runs = new ArrayList<AlgorithmRun>(runConfigs.size());
		
		for(RunConfig instConf: runConfigs)
		{
			runs.add(new CommandLineAlgorithmRun(execConfig, instConf));
		}
		
		this.runs = runs;
	}
	
	
	@Override
	public abstract List<AlgorithmRun> run();
	
	
	
}
