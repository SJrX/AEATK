package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.util.Collections;
import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.CommandLineAlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;

/**
 * Abstract Target Algorithm Evalutar
 * <p>
 * This class implements the default noop operation
 * 
 * @author Steve Ramage 
 *
 */
public abstract class AbstractTargetAlgorithmEvaluator implements TargetAlgorithmEvaluator {
	 /*
	 * Execution configuration of the target algorithm
	 */
	protected final AlgorithmExecutionConfig execConfig;
	
	protected int runCount = 0;


	/**
	 * Default Constructor
	 * @param execConfig	execution configuration of the target algorithm
	 */
	public AbstractTargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig)
	{
		this.execConfig = execConfig;
	}
	
	
	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig run) 
	{
		return evaluateRun(Collections.singletonList(run), null);
	}
	
	@Override
	public abstract List<AlgorithmRun>  evaluateRun(List<RunConfig> runConfigs);


	@Override
	public abstract List<AlgorithmRun>  evaluateRun(List<RunConfig> runConfigs, CurrentRunStatusObserver obs);
	
	@Override
	public int getRunCount()
	{
		return runCount;
	}
	

	@Override
	public int getRunHash()
	{
		return 0;
	}

	@Override
	public void seek(List<AlgorithmRun> runs) 
	{
		runCount = runs.size();	
	}

	protected void addRuns(List<AlgorithmRun> runs)
	{
		runCount+= runs.size();
	}

	@Override
	public String getManualCallString(RunConfig runConfig) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("cd ").append(execConfig.getAlgorithmExecutionDirectory()).append("; ");
		sb.append(CommandLineAlgorithmRun.getTargetAlgorithmExecutionCommand(execConfig, runConfig));
		sb.append("");
		
		return sb.toString();
	}
	

}