package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.aclib.expectedimprovement.ExpectedImprovementFunctions;
import ca.ubc.cs.beta.aclib.initialization.InitializationMode;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.*;
import ca.ubc.cs.beta.aclib.misc.logging.LogLevel;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.state.StateSerializers;

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
@UsageTextField(title="SMAC Options", description="General Options for Running SMAC")
public class SMACOptions extends AbstractOptions {
	
	@ParametersDelegate
	public ScenarioOptions scenarioConfig = new ScenarioOptions();
	
	@ParametersDelegate
	public RandomForestOptions randomForestOptions = new RandomForestOptions();

	@ParametersDelegate
	public ValidationOptions validationOptions = new ValidationOptions();

	@Parameter(names="--seedOffset", description="offset of numRun to use from seed (this plus --numRun should be less than LONG_MAX)")
	public long seedOffset = 0 ;
	
	@Parameter(names={"--numRun","--seed"}, required=true, description="number of this run (and seed)", validateWith=NonNegativeInteger.class)
	public long numRun = 0;
	
	@UsageTextField(defaultValues="<current working directory>")
	@Parameter(names={"--experimentDir","-e"}, description="root directory for experiments Folder")
	public String experimentDir = System.getProperty("user.dir") + File.separator + "";

	@Parameter(names={"--numIterations","--numberOfIterations"}, description = "limits the number of iterations allowed during automatic configuration phase", validateWith=FixedPositiveInteger.class)
	public int numIteratations = Integer.MAX_VALUE;
	
	@Parameter(names={"--runtimeLimit", "--wallClockLimit"}, description = "limits the total wall-clock time allowed during the automatic configuration phase", validateWith=FixedPositiveInteger.class)
	public int runtimeLimit = Integer.MAX_VALUE;
	
	@Parameter(names={"--totalNumRunsLimit","--numRunsLimit","--numberOfRunsLimit"} , description = "limits the total number of target algorithm runs allowed during the automatic configuration phase", validateWith=FixedPositiveLong.class)
	public long totalNumRunsLimit = Long.MAX_VALUE;

	@Parameter(names="--modelHashCodeFile", description="file containing a list of model hashes one per line with the following text per line: \"Preprocessed Forest Built With Hash Code: (n)\" or \"Random Forest Built with Hash Code: (n)\" where (n) is the hashcode", converter=ReadableFileConverter.class, hidden = true)
	public File modelHashCodeFile;
	
	@UsageTextField(defaultValues="RunGroup-<current date and time>")
	@Parameter(names="--runGroupName", description="name of subfolder of outputdir to save all the output files of this run to")
	public String runGroupName = "RunGroup-" + (new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss-SSS")).format(new Date());
	
	@Parameter(names="--numPCA", description="number of principal components features to use when building the model", validateWith=FixedPositiveInteger.class)
	public int numPCA = 7;

	@Parameter(names="--expectedImprovementFunction", description="expected improvement function to use during local search")
	public ExpectedImprovementFunctions expFunc = ExpectedImprovementFunctions.EXPONENTIAL;

	@Parameter(names={"--numChallengers","--numberOfChallengers"}, description="number of challengers needed for local search", validateWith=FixedPositiveInteger.class)
	public int numberOfChallengers = 10;
	
	@Parameter(names={"--numEIRandomConfigs","--numberOfRandomConfigsInEI","--numRandomConfigsInEI","--numberOfEIRandomConfigs"} , description="number of random configurations to evaluate during EI search", validateWith=NonNegativeInteger.class)
	public int numberOfRandomConfigsInEI = 10000;
	

	@Parameter(names="--stateSerializer", description="determines the format of the files to save the state in")
	public StateSerializers stateSerializer = StateSerializers.LEGACY;

	@Parameter(names="--stateDeserializer", description="determines the format of the files that store the saved state to restore")
	public StateSerializers statedeSerializer = StateSerializers.LEGACY;
	
	@UsageTextField(defaultValues="N/A (No state is being restored)")
	@Parameter(names="--restoreStateFrom", description="location of state to restore")
	public String restoreStateFrom = null;

	@UsageTextField(defaultValues="N/A (No state is being restored)")
	@Parameter(names={"--restoreStateIteration","--restoreIteration"}, description="iteration of the state to restore")
	public Integer restoreIteration = null;
	
	@Parameter(names={"--cleanOldStateOnSuccess"}, description="will clean up much of the useless state files if smac completes successfully")
	public boolean cleanOldStatesOnSuccess = true;
	
	@Parameter(names={"--saveContext","--saveContextWithState" }, description="saves some context with the state folder so that the data is mostly self-describing (Scenario, Instance File, Feature File, Param File are saved)")
	public boolean saveContextWithState = true;
	
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
		
	@Parameter(names="--countSMACTimeAsTunerTime", description="include the CPU Time of SMAC as part of the tunerTimeout")
	public boolean countSMACTimeAsTunerTime = true;
	
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
	
	/**
	 * Note most of these actually will never be read as we will silently scan for them in the input arguments to avoid logging
	 */
	@UsageTextField(defaultValues="", domain="")
	@Parameter(names="--showHiddenParameters", description="show hidden parameters that no one has use for, and probably just break SMAC (no-arguments)")
	public boolean showHiddenParameters = false;
	
	@UsageTextField(defaultValues="", domain="" )
	@Parameter(names={"--help","-?","/?","-h"}, description="show help")
	public boolean showHelp = false;
	
	@UsageTextField(defaultValues="", domain="")
	@Parameter(names={"-v","--version"}, description="print version and exit")
	public boolean showVersion = false;

	@Parameter(names={"--initialIncumbent"}, description="Initial Incumbent to use for configuration (you can use RANDOM, or DEFAULT as a special string to get a RANDOM or the DEFAULT configuration as needed). Other configurations are specified as: -(name) 'value' -(name) 'value' ...")
	public String initialIncumbent = "DEFAULT";

	@Parameter(names={"--initMode","--initializationMode"}, description="Initialization Mode")
	public InitializationMode initializationMode = InitializationMode.CLASSIC;

	@Parameter(names={"--iterativeCappingK"}, description="Iterative Capping K")
	public int iterativeCappingK = 1;

	@Parameter(names={"--iterativeCappingBreakOnFirstCompletion"}, description="In Phase 2 of the initialization phase, we will abort the first time something completes and not look at anything else with the same kappa limits")
	public boolean iterativeCappingBreakOnFirstCompletion = false;

	@Parameter(names={"--maskCensoredDataAsKappaMax"}, description="Mask censored data as kappa Max")
	public boolean maskCensoredDataAsKappaMax = false;  

	
}
