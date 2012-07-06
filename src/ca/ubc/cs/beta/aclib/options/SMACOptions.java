package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.ubc.cs.beta.aclib.expectedimprovement.ExpectedImprovementFunctions;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.ReadableFileConverter;
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
	
	@Parameter(names={"--seed", "--numRun"}, required=true, description="Seed for Random Number Generator (equivalent to numRun)")
	public long seed = 0;
	
	@Parameter(names={"-e","--experimentDir"}, description="Root Directory for Experiments Folder")
	public String experimentDir = System.getProperty("user.dir") + File.separator + "";
	

	@Parameter(names="--numIterations", description = "Total number of iterations to perform")
	public int numIteratations = Integer.MAX_VALUE;
	
	@Parameter(names="--runtimeLimit", description = "Total Wall clock time to execute for")
	public int runtimeLimit = Integer.MAX_VALUE;
	
	@Parameter(names="--totalNumRunLimit" , description = "Total number of target algorithm runs to execute")
	public int totalNumRunLimit = Integer.MAX_VALUE;

	@Parameter(names="--runHashCodeFile", description="File containing a list of Run Hashes one per line (Either with just the format on each line, or with the following text per line: \"Run Hash Codes: (Hash Code) After (n) runs\". The number of runs in this file need not match the number of runs that we execute, this file only ensures that the sequences never diverge. Note the n is completely ignored so the order they are specified in is the order we expect the hash codes in this version", converter=ReadableFileConverter.class)
	public File runHashCodeFile;
		
	@Parameter(names="--modelHashCodeFile", description="File containing a list of Model Hashes one per line with the following text per line: \"Preprocessed Forest Built With Hash Code: (n)\" or \"Random Forest Built with Hash Code: (n)\" where (n) is the hashcode", converter=ReadableFileConverter.class)
	public Object modelHashCodeFile;
	
	@Parameter(names="--runGroupName", description="Name of subfolder of outputdir to save all outputs of this group (different numruns) of runs")
	public String runGroupName = "RunGroup-" + (new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss-SSS")).format(new Date());
	
	
	@Parameter(names="--numPCA", description="Number of prinicipal components of features")
	public int numPCA = 7;

	@Parameter(names="--expectedImprovementFunction", description="Expected Improvement Function to Use")
	public ExpectedImprovementFunctions expFunc = ExpectedImprovementFunctions.EXPONENTIAL;

	@Parameter(names="--numberOfChallengers", description="Number of Challengers needed for Local Search", validateWith=PositiveInteger.class)
	public int numberOfChallengers = 10;
	
	@Parameter(names="--numberOfRandomConfigsInEI", description="Number of Random Configurations to evaluate in EI Search", validateWith=PositiveInteger.class)
	public int numberOfRandomConfigsInEI = 10000;
	

	@Parameter(names="--stateSerializer", description="Controls how the state will be saved to disk")
	public StateSerializers stateSerializer = StateSerializers.LEGACY;


	@Parameter(names="--stateDeserializer", description="Controls how the state will be restored from disk")
	public StateSerializers statedeSerializer = StateSerializers.LEGACY;

	@Parameter(names="--restoreStateFrom", description="The Location (State Deserializer Dependent) of States")
	public String restoreStateFrom = null;
	
	@Parameter(names="--restoreIteration", description="The Iteration to Restore")
	public Integer restoreIteration = null;
	
	@Parameter(names="--executionMode", description="Mode of Automatic Configurator to run")
	public ExecutionMode execMode = ExecutionMode.SMAC;
	
	@ParametersDelegate
	public RandomForestOptions randomForestOptions = new RandomForestOptions();

	@Parameter(names="--adaptiveCapping", description="Enable Adaptive Capping")
	public boolean adaptiveCapping = false;

	@Parameter(names="--capSlack", description="Amount to scale computed cap time of challengers by")
	public double capSlack = 1.3;
	
	@Parameter(names="--capAddSlack", description="Amount to increase computed cap time of challengers by [ general formula:   capTime = capSlack*computedCapTime + capAddSlack ]")
	public double capAddSlack = 1;
	
	@Parameter(names="--imputationIterations", description="Amount of times to impute censored data")
	public int imputationIterations = 10;
	
	@Parameter(names="--maxConcurrentAlgoExecs", description="Maximum number of concurrent target algorithm executions", validateWith=PositiveInteger.class)
	public int maxConcurrentAlgoExecs = 1;
	
	@Parameter(names="--skipValidation", description="Do not perform validation at the end")
	public boolean skipValidation = false;
	
	
	@Parameter(names="--maxIncumbentRuns", description="Maximum Number of Incumbent Runs allowed", validateWith=PositiveInteger.class)
	public int maxIncumbentRuns = 2000;
	@ParametersDelegate
	public ValidationOptions validationOptions = new ValidationOptions();

	@Parameter(names="--consoleLogLevel",description="Default Error Level of Console Output")
	public LogLevel consoleLogLevel = LogLevel.INFO;
			
	
}
