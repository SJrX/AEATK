package ca.ubc.cs.beta.aclib.smac;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.file.HomeFileUtils;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.FixedPositiveInteger;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.options.ScenarioOptions;
import ca.ubc.cs.beta.aclib.options.SeedOptions;
import ca.ubc.cs.beta.aclib.probleminstance.InstanceListWithSeeds;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceOptions.TrainTestInstances;
import ca.ubc.cs.beta.aclib.random.SeedableRandomPool;
import ca.ubc.cs.beta.aclib.random.SeedableRandomPoolConstants;
import ca.ubc.cs.beta.aclib.trajectoryfile.TrajectoryFileOptions;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterFile;
import com.beust.jcommander.ParametersDelegate;

/**
 * Options controlling the Stand Alone Validation Utility
 * 
 * @see ValidationOptions 
 */
@UsageTextField(title="Validation Executor Options", description="Options that control the stand-alone validator")
public class ValidationExecutorOptions extends AbstractOptions {
	
	@ParametersDelegate
	public ScenarioOptions scenarioConfig = new ScenarioOptions();
	
	@UsageTextField(defaultValues="~/.aclib/smac-validate.opt")
	@Parameter(names="--validationDefaultsFile", description="file that contains default settings for SMAC-Validate")
	@ParameterFile(ignoreFileNotExists = true) 
	public File smacValidateDefaults = HomeFileUtils.getHomeFile(".aclib" + File.separator  + "smac-validate.opt");
	
	@Parameter(names={"-e","--experimentDir"}, description="Root Directory for Experiments Folder")
	public String experimentDir = System.getProperty("user.dir") + File.separator + "";
	
	
	@ParametersDelegate
	public SeedOptions seedOptions = new SeedOptions();
	
	
	/*
	@Parameter(names="--seed", description="Seed for Random Number Generator")
	public long seed = 0;
	
	@Parameter(names="--numRun", description="Number of Run the Run", required=true)
	public long numRun = 0;
	*/
	
	@Parameter(names="--configuration", description="Parameter configuration to validate (In the same format calls are made to the algorithm) [Use 'DEFAULT' to validate the default]")
	public String incumbent;
	
	
	@ParametersDelegate
	public ValidationOptions validationOptions = new ValidationOptions();

	@Parameter(names="--tunerTime", description="Tuner Time when Validation occured (when specifying the configuration this is simply reported in the output file, when using a trajectory file we use the incumbent at this time, if you set this to -1 we use the tuner time from the scenario file or 0 if reading configuration from command line)")
	public double tunerTime = -1; 
	
	@Parameter(names="--useScenarioOutDir", description="Use the scenarios output directory")
	public boolean useScenarioOutDir = false;

	@Parameter(names="--empericalPerformance", description="Estimated performance of configuration on training set (-1 means use the trajectory file value or 0 if not trajectory file)")
	public double empericalPerformance = -1; 
	
	@Parameter(names="--tunerOverheadTime", description="Amount of Tuner Overhead time to report in the output (-1 means use trajectory file overhead or 0 if no trajectory file)")
	public double tunerOverheadTime = -1;
	
	@Parameter(names="--validateTestInstances", description="Use the test instances for validation")
	public boolean validateTestInstances = true;
	
	@Parameter(names="--waitForPersistedRunCompletion", description="If the Target Algorithm Evaluator is persistent, then you can optionally not wait for it to finish, and come back later")
	public boolean waitForPersistedRunCompletion = true;


	@Parameter(names={"--randomConfigurations","--random"}, description="Number of random configurations to validate", validateWith=FixedPositiveInteger.class)
	public int randomConfigurations = 0;


	@Parameter(names="--includeDefaultAsFirstRandom", description="Use the default as the first random default configuration")
	public boolean includeRandomAsFirstDefault = false;


	@Parameter(names="--configurationList", description="Listing of configurations to validate against (Can use DEFAULT for a default configuration or a RANDOM for a random one")
	public File configurationList;
	
	@Parameter(names="--autoIncrementTunerTime", description="Auto Increment Tuner Time")
	public boolean autoIncrementTunerTime = true;

	@Parameter(names="--wallTime", description="Wall Time when Validation occured (when specifying the configuration this is simply reported in the output file, when using a trajectory file we use the incumbent at this time, if you set this to -1 we use the wall time from the scenario file or 0 if reading configuration from command line)")
	public double wallTime;

	@ParametersDelegate
	public TrajectoryFileOptions trajectoryFileOptions = new TrajectoryFileOptions();
	

	/**
	 * Gets both the training and the test problem instances
	 * 
	 * @param experimentDirectory	Directory to search for instance files
	 * @param trainingSeed			Seed to use for the training instances
	 * @param testingSeed			Seed to use for the testing instances
	 * @param trainingRequired		Whether the training instance file is required
	 * @param testRequired			Whether the test instance file is required
	 * @return
	 * @throws IOException
	 */
	public InstanceListWithSeeds getTrainingAndTestProblemInstances(SeedableRandomPool pool) throws IOException
	{
			TrainTestInstances tti = this.scenarioConfig.getTrainingAndTestProblemInstances(this.experimentDir, pool.getRandom(SeedableRandomPoolConstants.INSTANCE_SEEDS).nextInt(), pool.getRandom(SeedableRandomPoolConstants.TEST_SEED_INSTANCES).nextInt(), true, false, false, false);
			
			if(this.validateTestInstances)
			{
				return tti.getTestInstances();
			} else
			{
				return tti.getTrainingInstances();
			}
	}
	
	/**
	 * Checks if the verify sat option is compatible with this set of probelm instances
	 * @param instances 	The problem instances
	 */
	public void checkProblemInstancesCompatibleWithVerifySAT(List<ProblemInstance> instances)
	{
		this.scenarioConfig.algoExecOptions.taeOpts.checkProblemInstancesCompatibleWithVerifySAT(instances);
	}

	public AlgorithmExecutionConfig getAlgorithmExecutionConfig() {
		return this.scenarioConfig.algoExecOptions.getAlgorithmExecutionConfig(null);
	}
	
}
