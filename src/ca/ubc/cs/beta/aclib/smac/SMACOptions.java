package ca.ubc.cs.beta.aclib.smac;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.expectedimprovement.ExpectedImprovementFunctions;
import ca.ubc.cs.beta.aclib.help.HelpOptions;
import ca.ubc.cs.beta.aclib.initialization.InitializationMode;
import ca.ubc.cs.beta.aclib.misc.file.HomeFileUtils;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.*;
import ca.ubc.cs.beta.aclib.misc.logging.LogLevel;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.options.RandomForestOptions;
import ca.ubc.cs.beta.aclib.options.RunGroupOptions;
import ca.ubc.cs.beta.aclib.options.SeedOptions;
import ca.ubc.cs.beta.aclib.options.scenario.ScenarioOptions;
import ca.ubc.cs.beta.aclib.probleminstance.InstanceListWithSeeds;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceOptions.TrainTestInstances;
import ca.ubc.cs.beta.aclib.random.SeedableRandomPool;
import ca.ubc.cs.beta.aclib.random.SeedableRandomPoolConstants;
import ca.ubc.cs.beta.aclib.state.StateFactory;
import ca.ubc.cs.beta.aclib.state.StateFactoryOptions;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterFile;
import com.beust.jcommander.ParametersDelegate;


/**
 * Represents the configuration for SMAC, 
 * 
 * @author seramage
 *
 *
 *
 */
@UsageTextField(title="SMAC Options", description="General Options for Running SMAC", claimRequired={"--instanceFile"})
public class SMACOptions extends AbstractOptions {
	
	@ParametersDelegate
	public ScenarioOptions scenarioConfig = new ScenarioOptions();
	
	@ParametersDelegate
	public RandomForestOptions randomForestOptions = new RandomForestOptions();

	@ParametersDelegate
	public ValidationOptions validationOptions = new ValidationOptions();

	
	@UsageTextField(defaultValues="<current working directory>")
	@Parameter(names={"--experimentDir","-e"}, description="root directory for experiments Folder")
	public String experimentDir = System.getProperty("user.dir") + File.separator + "";

	
	@Parameter(names="--modelHashCodeFile", description="file containing a list of model hashes one per line with the following text per line: \"Preprocessed Forest Built With Hash Code: (n)\" or \"Random Forest Built with Hash Code: (n)\" where (n) is the hashcode", converter=ReadableFileConverter.class, hidden = true)
	public File modelHashCodeFile;
	
	@ParametersDelegate
	public RunGroupOptions runGroupOptions = new RunGroupOptions();
	
	
	@Parameter(names="--numPCA", description="number of principal components features to use when building the model", validateWith=FixedPositiveInteger.class)
	public int numPCA = 7;

	@Parameter(names="--expectedImprovementFunction", description="expected improvement function to use during local search")
	public ExpectedImprovementFunctions expFunc = ExpectedImprovementFunctions.EXPONENTIAL;

	@Parameter(names={"--numChallengers","--numberOfChallengers"}, description="number of challengers needed for local search", validateWith=FixedPositiveInteger.class)
	public int numberOfChallengers = 10;
	
	@Parameter(names={"--numEIRandomConfigs","--numberOfRandomConfigsInEI","--numRandomConfigsInEI","--numberOfEIRandomConfigs"} , description="number of random configurations to evaluate during EI search", validateWith=NonNegativeInteger.class)
	public int numberOfRandomConfigsInEI = 10000;
	
	@Parameter(names="--executionMode", description="execution mode of the automatic configurator")
	public ExecutionMode execMode = ExecutionMode.SMAC;
	
	@UsageTextField(defaultValues="Defaults to true when --intraInstanceObjective is RUNTIME, false otherwise")
	@Parameter(names="--adaptiveCapping", description="Use Adaptive Capping")
	public Boolean adaptiveCapping = null;

	@Parameter(names="--capSlack", description="amount to scale computed adaptive capping value of challengers by", validateWith=ZeroInfinityOpenInterval.class)
	public double capSlack = 1.3;
	
	@Parameter(names="--capAddSlack", description="amount to increase computed adaptive capping value of challengers by (post scaling)", validateWith=ZeroInfinityOpenInterval.class)
	public double capAddSlack = 1;
	
	@Parameter(names="--imputationIterations", description="amount of times to impute censored data when building model", validateWith=NonNegativeInteger.class)
	public int imputationIterations = 2;

	@Parameter(names="--treatCensoredDataAsUncensored", description="builds the model as-if the response values observed for cap values, were the correct ones [NOT RECOMMENDED]")
	public boolean maskCensoredDataAsUncensored = false;
	
	@Parameter(names={"--doValidation","--validation"}, description="perform validation when SMAC completes")
	public boolean doValidation = true;
	
	@Parameter(names={"--maxIncumbentRuns","--maxRunsForIncumbent"}, description="maximum number of incumbent runs allowed", validateWith=FixedPositiveInteger.class)
	public int maxIncumbentRuns = 2000;
	
	@Parameter(names={"--initialN","--initialChallenge"}, description="initial amount of runs to request when intensifying on a challenger", validateWith=FixedPositiveInteger.class)
	public int initialChallengeRuns = 1;
	
	@Parameter(names={"--initialIncumbentRuns","--defaultConfigRuns"}, description="initial amount of runs to schedule against for the default configuration", validateWith=FixedPositiveInteger.class)
	public int initialIncumbentRuns = 1;
	
	@Parameter(names={"--intensificationPercentage","--frac_rawruntime"}, description="percent of time to spend intensifying versus model learning", validateWith=ZeroOneHalfOpenRightDouble.class)
	public double intensificationPercentage = 0.50;
	
	@Parameter(names="--consoleLogLevel",description="default log level of console output (this cannot be more verbose than the logLevel)")
	public LogLevel consoleLogLevel = LogLevel.INFO;
	
	@Parameter(names="--logLevel",description="Log Level for SMAC")
	public LogLevel logLevel = LogLevel.DEBUG;	
		

	
	@Parameter(names="--maskInactiveConditionalParametersAsDefaultValue", description="build the model treating inactive conditional values as the default value")
	public boolean maskInactiveConditionalParametersAsDefaultValue = true;
	
	@UsageTextField(defaultValues="")
	@ParameterFile
	@Parameter(names="--optionFile", description="read options from file")
	public File optionFile;
	
	@UsageTextField(defaultValues="")
	@ParameterFile
	@Parameter(names={"--optionFile2","--secondaryOptionsFile"}, description="read options from file")
	public File optionFile2;
	
	@ParametersDelegate
	public HelpOptions help = new HelpOptions();
	
	@Parameter(names={"--initialIncumbent"}, description="Initial Incumbent to use for configuration (you can use RANDOM, or DEFAULT as a special string to get a RANDOM or the DEFAULT configuration as needed). Other configurations are specified as: -name 'value' -name 'value' ... For instance: --quick-sort 'on' ")
	public String initialIncumbent = "DEFAULT";

	@Parameter(names={"--initMode","--initializationMode"}, description="Initialization Mode")
	public InitializationMode initializationMode = InitializationMode.CLASSIC;

	@Parameter(names={"--iterativeCappingK"}, description="Iterative Capping K")
	public int iterativeCappingK = 1;

	@Parameter(names={"--iterativeCappingBreakOnFirstCompletion"}, description="In Phase 2 of the initialization phase, we will abort the first time something completes and not look at anything else with the same kappa limits")
	public boolean iterativeCappingBreakOnFirstCompletion = false;

	@Parameter(names={"--maskCensoredDataAsKappaMax"}, description="Mask censored data as kappa Max")
	public boolean maskCensoredDataAsKappaMax = false;


	@Parameter(names={"--alwaysRunInitialConfiguration"}, description="if true we will always run the default and switch back to it if it is better than the incumbent")
	public boolean alwaysRunInitialConfiguration = false; 

	@UsageTextField(defaultValues="~/.aclib/smac.opt")
	@Parameter(names="--smacDefaultsFile", description="file that contains default settings for SMAC")
	@ParameterFile(ignoreFileNotExists = true) 
	public File smacDefaults = HomeFileUtils.getHomeFile(".aclib" + File.separator  + "smac.opt");

	@Parameter(names={"--deterministic-instance-ordering","--deterministicInstanceOrdering"}, description="If true, instances will be selected from the instance list file in the specified order")
	public boolean deterministicInstanceOrdering = false;
	
	@ParametersDelegate
	public SeedOptions seedOptions = new SeedOptions();
	
	@ParametersDelegate
	public StateFactoryOptions stateOpts = new StateFactoryOptions();
	
	
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
	public TrainTestInstances getTrainingAndTestProblemInstances(SeedableRandomPool pool) throws IOException
	{
			return this.scenarioConfig.getTrainingAndTestProblemInstances(this.experimentDir, pool.getRandom(SeedableRandomPoolConstants.INSTANCE_SEEDS).nextInt(), pool.getRandom(SeedableRandomPoolConstants.TEST_SEED_INSTANCES).nextInt(), true, this.doValidation, false, false);
	}
	
	public String getRunGroupName(Collection<AbstractOptions> opts)
	{	
		opts = new HashSet<AbstractOptions>(opts);
		opts.add(this);
		return runGroupOptions.getRunGroupName(opts);	
	}
	
	/**
	 * Checks if the verify sat option is compatible with this set of probelm instances
	 * @param instances 	The problem instances
	 */
	public void checkProblemInstancesCompatibleWithVerifySAT(List<ProblemInstance> instances)
	{
		this.scenarioConfig.algoExecOptions.taeOpts.checkProblemInstancesCompatibleWithVerifySAT(instances);
	}

	/**
	 * Returns a state factory
	 * @param outputDir	output directory
	 * @return
	 */
	public StateFactory getRestoreStateFactory(String outputDir) {
		return stateOpts.getRestoreStateFactory(outputDir, this.seedOptions.numRun);
	}

	public StateFactory getSaveStateFactory(String outputDir) {
		return stateOpts.getSaveStateFactory(outputDir, this.seedOptions.numRun);
	}
	
	public String getOutputDirectory(String runGroupName)
	{
		File outputDir = new File(this.scenarioConfig.outputDirectory + File.separator + runGroupName);
		if(!outputDir.isAbsolute())
		{
			outputDir = new File(experimentDir + File.separator + this.scenarioConfig.outputDirectory + File.separator + runGroupName);
		}
		
		return outputDir.getAbsolutePath();
	}

	public void saveContextWithState(ParamConfigurationSpace configSpace, InstanceListWithSeeds trainingILWS,	StateFactory sf)
	{
		this.stateOpts.saveContextWithState(configSpace, trainingILWS, this.scenarioConfig.scenarioFile, sf);
	}

	public AlgorithmExecutionConfig getAlgorithmExecutionConfig() {
		return this.scenarioConfig.algoExecOptions.getAlgorithmExecutionConfig(experimentDir);
	}
}
