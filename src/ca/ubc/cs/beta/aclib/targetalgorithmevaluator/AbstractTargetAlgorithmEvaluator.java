package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

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
	
	//Fields that should be cleaned up when we fix the runHashCode Generation
	protected  int runHashCodes = 0;
	protected int runCount = 1;
	
	private final Logger log = LoggerFactory.getLogger(getClass());

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
		return evaluateRun(Collections.singletonList(run));
	}
	
	@Override
	public abstract List<AlgorithmRun>  evaluateRun(List<RunConfig> runConfigs);
	
	@Override
	public int getRunCount()
	{
		return runCount;
	}
	

	@Override
	public int getRunHash()
	{
		return runHashCodes;
	}

	@Override
	public void seek(List<AlgorithmRun> runs) {
		runCount = runs.size();	
	}
}