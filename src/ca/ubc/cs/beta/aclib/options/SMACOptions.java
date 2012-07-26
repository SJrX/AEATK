package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.ubc.cs.beta.aclib.expectedimprovement.ExpectedImprovementFunctions;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.*;
import ca.ubc.cs.beta.aclib.misc.logging.LogLevel;
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
public class SMACOptions extends AbstractOptions {
	
	@ParametersDelegate
	public ScenarioOptions scenarioConfig = new ScenarioOptions();
	
	
	@ParametersDelegate
	public RandomForestOptions randomForestOptions = new RandomForestOptions();

	@ParametersDelegate
	public ValidationOptions validationOptions = new ValidationOptions();

	
	
	@Parameter(names={"--numRun","--seed"}, required=true, description="Number of this run (and seed)", validateWith=NonNegativeInteger.class)
	public long seed = 0;
	
	@Parameter(names={"-e","--experimentDir"}, description="Root Directory for Experiments Folder")
	public String experimentDir = System.getProperty("user.dir") + File.separator + "";
	

	@Parameter(names="--numIterations", description = "Total number of iterations to perform", validateWith=FixedPositiveInteger.class)
	public int numIteratations = Integer.MAX_VALUE;
	
	@Parameter(names="--runtimeLimit", description = "Total Wall clock time to execute for", validateWith=FixedPositiveInteger.class)
	public int runtimeLimit = Integer.MAX_VALUE;
	
	@Parameter(names="--totalNumRunLimit" , description = "Total number of target algorithm runs to execute", validateWith=FixedPositiveInteger.class)
	public int totalNumRunLimit = Integer.MAX_VALUE;

	@Parameter(names="--runHashCodeFile", description="File containing a list of Run Hashes one per line (Either with just the format on each line, or with the following text per line: \"Run Hash Codes: (Hash Code) After (n) runs\". The number of runs in this file need not match the number of runs that we execute, this file only ensures that the sequences never diverge. Note the n is completely ignored so the order they are specified in is the order we expect the hash codes in this version", converter=ReadableFileConverter.class)
	public File runHashCodeFile;
		
	@Parameter(names="--modelHashCodeFile", description="File containing a list of Model Hashes one per line with the following text per line: \"Preprocessed Forest Built With Hash Code: (n)\" or \"Random Forest Built with Hash Code: (n)\" where (n) is the hashcode", converter=ReadableFileConverter.class, hidden = true)
	public File modelHashCodeFile;
	
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

	@Parameter(names="--restoreStateFrom", description="Location (State Deserializer Dependent) of States")
	public String restoreStateFrom = null;
	
	@Parameter(names={"--restoreStateIteration","--restoreIteration"}, description="The Iteration of the State to Restore")
	public Integer restoreIteration = null;
	
	@Parameter(names="--executionMode", description="Execution mode of the Automatic Configurator")
	public ExecutionMode execMode = ExecutionMode.SMAC;

	@Parameter(names="--adaptiveCapping", description="Enable Adaptive Capping")
	public boolean adaptiveCapping = true;

	@Parameter(names="--capSlack", description="Amount to scale computed cap time of challengers by", validateWith=ZeroInfinityOpenInterval.class)
	public double capSlack = 1.3;
	
	@Parameter(names="--capAddSlack", description="Amount to increase computed cap time of challengers by [ general formula:   capTime = capSlack*computedCapTime + capAddSlack ]", validateWith=ZeroInfinityOpenInterval.class)
	public double capAddSlack = 1;
	
	@Parameter(names="--imputationIterations", description="Amount of times to impute censored data when building model", validateWith=NonNegativeInteger.class)
	public int imputationIterations = 10;
	
	
	@Parameter(names="--maxConcurrentAlgoExecs", description="Maximum number of concurrent target algorithm executions", validateWith=PositiveInteger.class)
	public int maxConcurrentAlgoExecs = 1;
	
	@Parameter(names={"--doValidation","--validation"}, description="Perform validation at the end")
	public boolean doValidation = true;
	
	@Parameter(names="--maxIncumbentRuns", description="Maximum Number of Incumbent Runs allowed", validateWith=FixedPositiveInteger.class)
	public int maxIncumbentRuns = 2000;
	
	@Parameter(names={"--intensificationPercentage","--frac_rawruntime"}, description="Percent of time to spend intensifying versus model learning", validateWith=ZeroOneHalfOpenRightDouble.class)
	public double intensificationPercentage = 0.50;
	
	
	@Parameter(names="--consoleLogLevel",description="Default Error Level of Console Output")
	public LogLevel consoleLogLevel = LogLevel.INFO;
	
	@Parameter(names="--abortOnCrash", description="Treat algorithm crashes as ABORT (Useful if the algorithm really should never CRASH)")
	public boolean abortOnCrash = false;

	@Parameter(names="--abortOnFirstRunCrash", description="If the first run of the algorithm CRASHED treat it as an ABORT, otherwise leave it alone")
	public boolean abortOnFirstRunCrash = true;
	
	@Parameter(names="--countSMACTimeAsTunerTime", description="Include the CPU Time of SMAC as part of the tunerTimeout")
	public boolean countSMACTimeAsTunerTime = true;
			
	@Parameter(names="--retryTargetAlgorithmRunCount", description="Number of times to retry an algorithm run before eporting crashed (NOTE: The original crashes DO NOT count towards any time limits, they are in effect lost). Additionally this only retries CRASHED runs, not ABORT runs, this is by design as ABORT is only for cases when we shouldn't bother further runs", validateWith=NonNegativeInteger.class)
	public int retryCount = 0;
	
}
