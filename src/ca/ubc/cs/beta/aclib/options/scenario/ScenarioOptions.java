package ca.ubc.cs.beta.aclib.options.scenario;

import java.io.File;
import java.io.IOException;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterFile;
import com.beust.jcommander.ParametersDelegate;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionOptions;
import ca.ubc.cs.beta.aclib.misc.jcommander.converter.OverallObjectiveConverter;
import ca.ubc.cs.beta.aclib.misc.jcommander.converter.RunObjectiveConverter;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceOptions;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceOptions.TrainTestInstances;
import ca.ubc.cs.beta.aclib.termination.TerminationCriteriaOptions;

/**
 * Object which contains all information about a scenario
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
@UsageTextField(title="Scenario Options", description="Standard Scenario Options for use with SMAC. In general consider using the --scenarioFile directive to specify these parameters and Algorithm Execution Options")
public class ScenarioOptions extends AbstractOptions{
	
	@Parameter(names={"--run-obj","--runObj","--run_obj"}, description="per target algorithm run objective type that we are optimizing for", converter=RunObjectiveConverter.class)
	public RunObjective runObj = RunObjective.RUNTIME;
	
	@Parameter(names={"--intra-obj","--intra-instance-obj","--overall-obj","--intraInstanceObj","--overallObj", "--overall_obj","--intra_instance_obj"}, description="objective function used to aggregate multiple runs for a single instance", converter=OverallObjectiveConverter.class)
	public OverallObjective intraInstanceObj = OverallObjective.MEAN10;
	
	@Parameter(names={"--inter-obj","--inter-instance-obj","--interInstanceObj","--inter_instance_obj"}, description="objective function used to aggregate over multiple instances (that have already been aggregated under the Intra-Instance Objective)", converter=OverallObjectiveConverter.class)
	public OverallObjective interInstanceObj = OverallObjective.MEAN;
	
	@ParametersDelegate
	public TerminationCriteriaOptions limitOptions = new TerminationCriteriaOptions();
	
	
	@ParametersDelegate
	public ProblemInstanceOptions instanceOptions = new ProblemInstanceOptions();
	
	@UsageTextField(defaultValues="")
	@Parameter(names={"--scenario","--scenario-file","--scenarioFile"}, description="scenario file")
	@ParameterFile
	public File scenarioFile = null;
	
	@UsageTextField(defaultValues="<current working directory>/smac-output")
	@Parameter(names={"--output-dir","--outputDirectory","--outdir"}, required=false, description="Output Directory")
	public String outputDirectory = System.getProperty("user.dir") + File.separator + "smac-output";

	@ParametersDelegate
	public AlgorithmExecutionOptions algoExecOptions = new AlgorithmExecutionOptions();

	/**
	 * Gets both the training and the test problem instances
	 * 
	 * @param experimentDirectory			Directory to search for instance files
	 * @param trainingSeed					Seed to use for the training instances
	 * @param testingSeed					Seed to use for the testing instances
	 * @param trainingRequired				Whether the training instance file is required
	 * @param testRequired					Whether the test instance file is required
	 * @param trainingFeaturesRequired		Whether the training instance file is required
	 * @param testingFeaturesRequired		Whether the test instance file is required
	 * @return
	 * @throws IOException
	 */
	public TrainTestInstances getTrainingAndTestProblemInstances(String experimentDirectory, long trainingSeed, long testingSeed, boolean trainingRequired, boolean testRequired, boolean trainingFeaturesRequired, boolean testingFeaturesRequired) throws IOException
	{
			return this.instanceOptions.getTrainingAndTestProblemInstances(experimentDirectory, trainingSeed, testingSeed, this.algoExecOptions.deterministic, trainingRequired, testRequired, trainingFeaturesRequired, testingFeaturesRequired);
	}

	
}
