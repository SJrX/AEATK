package ca.ubc.cs.beta.config;

import java.io.File;
import java.lang.reflect.Field;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import ca.ubc.cs.beta.jcommander.converter.OverallObjectiveConverter;
import ca.ubc.cs.beta.jcommander.converter.RunObjectiveConverter;
import ca.ubc.cs.beta.jcommander.converter.StringToDoubleConverterWithMax;
import ca.ubc.cs.beta.smac.OverallObjective;
import ca.ubc.cs.beta.smac.RunObjective;

/**
 * Helper Configuration Object that contains 'Scenario parameters'
 * @author seramage
 *
 */
public class ScenarioConfig extends AbstractConfigToString{

	@ParametersDelegate
	public AlgoExecConfig algoExecConfig = new AlgoExecConfig();
	
	@Parameter(names={"--runObj","--run_obj"}, description="Per Target Algorithm Run Objective Type that we are optimizing for", converter=RunObjectiveConverter.class)
	public RunObjective runObj = RunObjective.RUNTIME;
	
	@Parameter(names={"--overallObj","--overall_obj"}, description="Aggregate over all Run's Objective Type that we are optimizing for", converter=OverallObjectiveConverter.class)
	public OverallObjective overallObj = OverallObjective.MEAN;
	
	@Parameter(names={"--cutoffTime","--cutoff_time"}, description="Cap Time for an Individual Run")
	public double cutoffTime = 300;
	
	@Parameter(names={"--cutoffLength","--cutoff_length"}, description="Cap Time for an Individual Run", converter=StringToDoubleConverterWithMax.class)
	public double cutoffLength = -1.0;
	
	@Parameter(names="--tunerTimeout", description="Total CPU Time to execute for")
	public int tunerTimeout = Integer.MAX_VALUE;
	
	@Parameter(names={"--instanceFile","-i","--instance_file","--instance_seed_file"}, description="File containing instances in either \"<instance filename>\", or \"<seed>,<instance filename>\" format", required=true)
	public String instanceFile;

	@Parameter(names={"--instanceFeatureFile", "--feature_file"}, description="File that contains the all the instances features")
	public String instanceFeatureFile;
	
	@Parameter(names={"--testInstanceFile","--test_instance_file","--test_instance_seed_file"}, description="File containing instances specified one instance per line", required=true)
	public String testInstanceFile;

	@Parameter(names="--scenarioFile", description="Scenario File")
	@ParameterFile
	public File scenarioFile = null;
	
	@Parameter(names="--deterministic", description="Whether the target algorithm is deterministic")
	public int deterministic = 0;
	
	@Parameter(names="--skipInstanceFileCheck", description="Do not check if instances files exist on disk")
	public boolean skipInstanceFileCheck = false;

	@Parameter(names={"--outputDirectory","--outdir"}, required=false, description="Output Directory")
	public String outputDirectory = System.getProperty("user.dir") + File.separator + "smac-output";

	
}
