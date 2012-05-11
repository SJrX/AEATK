package ca.ubc.cs.beta.ac.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import ca.ubc.cs.beta.config.AlgoExecConfig;
import ca.ubc.cs.beta.smac.OverallObjective;
import ca.ubc.cs.beta.smac.RunObjective;

/**
 * Helper Configuration Object that contains 'Scenario parameters'
 * @author seramage
 *
 */
public class ScenarioConfig {

	@ParametersDelegate
	public AlgoExecConfig algoExecConfig = new AlgoExecConfig();
	
	@Parameter(names={"--runObj","--run_obj"}, description="Objective Type that we are optimizing for")
	public RunObjective runObj = RunObjective.RUNTIME;
	
	@Parameter(names={"--overallObj","--overall_obj"}, description="Objective Type that we are optimizing for")
	public OverallObjective overallObj = OverallObjective.MEAN;
	
	@Parameter(names={"--cutoffTime","--cutoff_time"}, description="Cap Time for an Individual Run")
	public int cutoffTime = 300;
	
	@Parameter(names="--tunerTimeout", description="Total CPU Time to execute for")
	public int tunerTimeout = Integer.MAX_VALUE;
	
	@Parameter(names={"--instanceFile","-i"}, description="File containing instances specified one instance per line", required=true)
	public String instanceFile;

	@Parameter(names="--instanceFeatureFile", description="File that contains the all the instances features")
	public String instanceFeatureFile;
	
	
	
}
