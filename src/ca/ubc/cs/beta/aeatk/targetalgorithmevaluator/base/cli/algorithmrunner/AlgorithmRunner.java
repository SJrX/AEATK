package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.algorithmrunner;

import java.util.List;
import java.util.concurrent.ExecutorService;

import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;

/**
 * Interface for objects that can handle running RunConfigs and AlgorithmExecutionConfigs
 * 
 * Use the AutomaticConfiguratorFactory to get your Runner
 * @author seramage
 * 
 */
public interface AlgorithmRunner {

	/**
	 * Runs the Algorithm, returning the AlgorithmRuns in the same order as they were specified
	 * @return list of <code>AlgorithmRun</code>, in order they were specified
	 */
	public List<AlgorithmRunResult> run(ExecutorService p);
	
	public void shutdownThreadPool();

	

}