package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterFile;
import com.beust.jcommander.ParametersDelegate;

import ca.ubc.cs.beta.aclib.misc.jcommander.converter.OverallObjectiveConverter;
import ca.ubc.cs.beta.aclib.misc.jcommander.converter.RunObjectiveConverter;
import ca.ubc.cs.beta.aclib.misc.jcommander.converter.StringToDoubleConverterWithMax;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.*;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;

/**
 * Object which contains all information about a scenario
 * @author seramage
 *
 */
@UsageTextField(title="Scenario Options", description="Standard Scenario Options for use with SMAC. In general consider using the --scenarioFile directive to specify these parameters and Algorithm Execution Options")
public class ScenarioOptions extends AbstractOptions{
	
	@Parameter(names={"--runObj","--run_obj"}, description="per target algorithm run objective type that we are optimizing for", converter=RunObjectiveConverter.class, required=true)
	public RunObjective runObj;
	
	@Parameter(names={"--intraInstanceObj","--overallObj", "--overall_obj","--intra_instance_obj"}, description="objective function used to aggregate multiple runs for a single instance", converter=OverallObjectiveConverter.class, required=true)
	public OverallObjective intraInstanceObj;
	
	@Parameter(names={"--interInstanceObj","--inter_instance_obj"}, description="objective function used to aggregate over multiple instances (that have already been aggregated under the Intra-Instance Objective)", converter=OverallObjectiveConverter.class)
	public OverallObjective interInstanceObj = OverallObjective.MEAN;
	
	
	
	@Parameter(names="--tunerTimeout", description="limits the total cpu time allowed between SMAC and the target algorithm runs during the automatic configuration phase", validateWith=NonNegativeInteger.class)
	public int tunerTimeout = Integer.MAX_VALUE;
	
	@Parameter(names={"--instanceFile","-i","--instance_file","--instance_seed_file"}, description="file containing a list of instances to use during the automatic configuration phase (see Instance File Format section of the manual)", required=true)
	public String instanceFile;

	@UsageTextField(defaultValues="")
	@Parameter(names={"--instanceFeatureFile", "--feature_file"}, description="file that contains the all the instances features")
	public String instanceFeatureFile;
	
	@Parameter(names={"--testInstanceFile","--test_instance_file","--test_instance_seed_file"}, description="file containing a list of instances to use during the validation phase (see Instance File Format section of the manual)", required=true)
	public String testInstanceFile;

	@UsageTextField(defaultValues="")
	@Parameter(names="--scenarioFile", description="scenario file")
	@ParameterFile
	public File scenarioFile = null;
	
	@Parameter(names="--checkInstanceFilesExist", description="check if instances files exist on disk")
	public boolean checkInstanceFilesExist = false;

	@UsageTextField(defaultValues="<current working directory>/smac-output")
	@Parameter(names={"--outputDirectory","--outdir"}, required=false, description="Output Directory")
	public String outputDirectory = System.getProperty("user.dir") + File.separator + "smac-output";

	@ParametersDelegate
	public AlgorithmExecutionOptions algoExecOptions = new AlgorithmExecutionOptions();

}
