package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.ubc.cs.beta.aclib.expectedimprovement.ExpectedImprovementFunctions;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.*;
import ca.ubc.cs.beta.aclib.misc.logging.LogLevel;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.state.StateSerializers;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.validators.PositiveInteger;


/**
 * Represents the configuration for SMAC, 
 * 
 * @author seramage
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

	@Parameter(names="--seedOffset", description="Offset of numRun to use from seed (This plus --numRun should be less than LONG_MAX)")
	public long seedOffset = 0 ;
	
	@Parameter(names={"--numRun","--seed"}, required=true, description="Number of this run (and seed)", validateWith=NonNegativeInteger.class)
	public long numRun = 0;
	
	@UsageTextField(defaultValues="<current working directory>")
	@Parameter(names={"--experimentDir","-e"}, description="Root Directory for Experiments Folder")
	public String experimentDir = System.getProperty("user.dir") + File.separator + "";

	@Parameter(names="--numIterations", description = "Total number of iterations to perform", validateWith=FixedPositiveInteger.class)
	public int numIteratations = Integer.MAX_VALUE;
	
	@Parameter(names={"--runtimeLimit", "--wallClockLimit"}, description = "Total Wall clock time to execute for", validateWith=FixedPositiveInteger.class)
	public int runtimeLimit = Integer.MAX_VALUE;
	
	@Parameter(names="--totalNumRunLimit" , description = "Total number of target algorithm runs to execute", validateWith=FixedPositiveInteger.class)
	public int totalNumRunLimit = Integer.MAX_VALUE;

	@Parameter(names="--modelHashCodeFile", description="File containing a list of Model Hashes one per line with the following text per line: \"Preprocessed Forest Built With Hash Code: (n)\" or \"Random Forest Built with Hash Code: (n)\" where (n) is the hashcode", converter=ReadableFileConverter.class, hidden = true)
	public File modelHashCodeFile;
	
	@UsageTextField(defaultValues="RunGroup-<current date and time>")
	@Parameter(names="--runGroupName", description="Name of subfolder of outputdir to save all the output files of this run to")
	public String runGroupName = "RunGroup-" + (new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss-SSS")).format(new Date());
	
	@Parameter(names="--numPCA", description="Number of Prinicipal Components Features to use when building the model", validateWith=FixedPositiveInteger.class)
	public int numPCA = 7;

	@Parameter(names="--expectedImprovementFunction", description="Expected Improvement Function to Use")
	public ExpectedImprovementFunctions expFunc = ExpectedImprovementFunctions.EXPONENTIAL;

	@Parameter(names="--numberOfChallengers", description="Number of Challengers needed for Local Search", validateWith=FixedPositiveInteger.class)
	public int numberOfChallengers = 10;
	
	@Parameter(names="--numberOfRandomConfigsInEI", description="Number of Random Configurations to evaluate in EI Search", validateWith=NonNegativeInteger.class)
	public int numberOfRandomConfigsInEI = 10000;
	

	@Parameter(names="--stateSerializer", description="Determines the format of the files to save the state in")
	public StateSerializers stateSerializer = StateSerializers.LEGACY;

	@Parameter(names="--stateDeserializer", description="Determines the format of the files that store the saved state to restore")
	public StateSerializers statedeSerializer = StateSerializers.LEGACY;
	
	@UsageTextField(defaultValues="N/A (No state is being restored)")
	@Parameter(names="--restoreStateFrom", description="Location (State Deserializer Dependent) of States")
	public String restoreStateFrom = null;

	@UsageTextField(defaultValues="N/A (No state is being restored)")
	@Parameter(names={"--restoreStateIteration","--restoreIteration"}, description="The Iteration of the State to Restore")
	public Integer restoreIteration = null;
	
	@Parameter(names="--executionMode", description="Execution mode of the Automatic Configurator")
	public ExecutionMode execMode = ExecutionMode.SMAC;
	
	@UsageTextField(defaultValues="Defaults to true when --intraInstanceObjective is RUNTIME, false otherwise")
	@Parameter(names="--adaptiveCapping", description="Use Adaptive Capping")
	public Boolean adaptiveCapping = null;

	@Parameter(names="--capSlack", description="Amount to scale computed adaptive capping value of challengers by", validateWith=ZeroInfinityOpenInterval.class)
	public double capSlack = 1.3;
	
	@Parameter(names="--capAddSlack", description="Amount to increase computed adaptive capping value of challengers by (post scaling)", validateWith=ZeroInfinityOpenInterval.class)
	public double capAddSlack = 1;
	
	@Parameter(names="--imputationIterations", description="Amount of times to impute censored data when building model", validateWith=NonNegativeInteger.class)
	public int imputationIterations = 2;

	@Parameter(names="--treatCensoredDataAsUncensored", description="Builds the model as-if the response values observed for cap values, were the correct ones [NOT RECOMMENDED]")
	public boolean maskCensoredDataAsUncensored = false;
	
	@Parameter(names={"--doValidation","--validation"}, description="Perform validation on trajectory file")
	public boolean doValidation = true;
	
	@Parameter(names="--maxIncumbentRuns", description="Maximum Number of Incumbent Runs allowed", validateWith=FixedPositiveInteger.class)
	public int maxIncumbentRuns = 2000;
	
	@Parameter(names={"--intensificationPercentage","--frac_rawruntime"}, description="Percent of time to spend intensifying versus model learning", validateWith=ZeroOneHalfOpenRightDouble.class)
	public double intensificationPercentage = 0.50;
	
	@Parameter(names="--consoleLogLevel",description="Default Error Level of Console Output (Note this cannot be more verbose than the logLevel)")
	public LogLevel consoleLogLevel = LogLevel.INFO;
	
	@Parameter(names="--logLevel",description="Log Level for SMAC")
	public LogLevel logLevel = LogLevel.DEBUG;	
		
	@Parameter(names="--countSMACTimeAsTunerTime", description="Include the CPU Time of SMAC as part of the tunerTimeout")
	public boolean countSMACTimeAsTunerTime = true;
	
	@Parameter(names="--maskInactiveConditionalParametersAsDefaultValue", description="When building the model treat inactive conditional values as the default value")
	public boolean maskInactiveConditionalParametersAsDefaultValue = true;
}
