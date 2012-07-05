package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterFile;
import com.beust.jcommander.ParametersDelegate;

import ca.ubc.cs.beta.aclib.misc.jcommander.converter.OverallObjectiveConverter;
import ca.ubc.cs.beta.aclib.misc.jcommander.converter.RunObjectiveConverter;
import ca.ubc.cs.beta.aclib.misc.jcommander.converter.StringToDoubleConverterWithMax;
import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;

/**
 * Object which contains all information about a scenario
 * @author seramage
 *
 */
public class ScenarioOptions extends AbstractOptions{

	
	@ParametersDelegate
	public AlgorithmExecutionOptions algoExecOptions = new AlgorithmExecutionOptions();
	
	@Parameter(names={"--runObj","--run_obj"}, description="Per Target Algorithm Run Objective Type that we are optimizing for", converter=RunObjectiveConverter.class)
	public RunObjective runObj = RunObjective.RUNTIME;
	
	@Parameter(names={"--overallObj","--intraInstanceObj","--overall_obj","--intra_instance_obj"}, description="Aggregate over all Run's Objective Type that we are optimizing for", converter=OverallObjectiveConverter.class)
	public OverallObjective intraInstanceObj = OverallObjective.MEAN;
	
	
	@Parameter(names={"--interInstanceObj","--inter_instance_obj"}, description="Aggregate over all Run's Objective Type that we are optimizing for", converter=OverallObjectiveConverter.class)
	public OverallObjective interInstanceObj = OverallObjective.MEAN;
	
	
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
	
	
	
	@Parameter(names="--skipInstanceFileCheck", description="Do not check if instances files exist on disk")
	public boolean skipInstanceFileCheck = false;

	@Parameter(names={"--outputDirectory","--outdir"}, required=false, description="Output Directory")
	public String outputDirectory = System.getProperty("user.dir") + File.separator + "smac-output";

	@ParametersDelegate
	public ParamFileDelegate paramFileDelegate = new ParamFileDelegate();
	
}
