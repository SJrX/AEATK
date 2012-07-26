package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterFile;
import com.beust.jcommander.ParametersDelegate;

import ca.ubc.cs.beta.aclib.misc.jcommander.converter.OverallObjectiveConverter;
import ca.ubc.cs.beta.aclib.misc.jcommander.converter.RunObjectiveConverter;
import ca.ubc.cs.beta.aclib.misc.jcommander.converter.StringToDoubleConverterWithMax;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.*;
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
	
	@Parameter(names={"--runObj","--run_obj"}, description="Per Target Algorithm Run Objective Type that we are optimizing for", converter=RunObjectiveConverter.class, required=true)
	public RunObjective runObj;
	
	@Parameter(names={"--intraInstanceObj","--overallObj", "--overall_obj","--intra_instance_obj"}, description="Objective function used to aggregate multiple runs for a single instance", converter=OverallObjectiveConverter.class, required=true)
	public OverallObjective intraInstanceObj;
	
	@Parameter(names={"--interInstanceObj","--inter_instance_obj"}, description="Objective function used to aggregate over multiple instances (that have already been aggregated under the Intra-Instance Objective)", converter=OverallObjectiveConverter.class)
	public OverallObjective interInstanceObj = OverallObjective.MEAN;
	
	@Parameter(names={"--cutoffTime","--cutoff_time"}, description="Cap Time for an Individual Run", required=true, validateWith=ZeroInfinityOpenInterval.class)
	public double cutoffTime;
	
	@Parameter(names={"--cutoffLength","--cutoff_length"}, description="Cap Time for an Individual Run [Not Implemented Currently]", converter=StringToDoubleConverterWithMax.class, hidden=true)
	public double cutoffLength = -1.0;
	
	@Parameter(names="--tunerTimeout", description="Total CPU Time to execute for", validateWith=NonNegativeInteger.class)
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
	
	@Parameter(names="--checkInstanceFilesExist", description="Check if instances files exist on disk")
	public boolean checkInstanceFilesExist = false;

	@Parameter(names={"--outputDirectory","--outdir"}, required=false, description="Output Directory")
	public String outputDirectory = System.getProperty("user.dir") + File.separator + "smac-output";

	@ParametersDelegate
	public ParamFileDelegate paramFileDelegate = new ParamFileDelegate();
	
}
