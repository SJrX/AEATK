package ca.ubc.cs.beta.config;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.ubc.cs.beta.models.surrogate.helpers.jcommander.validator.ReadableFileConverter;
import ca.ubc.cs.beta.smac.OverallObjective;
import ca.ubc.cs.beta.smac.RunObjective;
import ca.ubc.cs.beta.smac.state.StateSerializers;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.validators.PositiveInteger;

import ei.ExpectedImprovementFunctions;

/**
 * Represents the configuration for SMAC, 
 * if you add any fields here, make sure to handle them in toString
 * @author seramage
 *
 */
public class SMACConfig extends AbstractConfigToString {
	
	@ParametersDelegate
	public ScenarioConfig scenarioConfig = new ScenarioConfig();
	
	@Parameter(names="--seed", description="Seed for Random Number Generator [0 means don't use a seed]")
	public long seed = 0;
	
	@Parameter(names={"-e","--experimentDir"}, description="Root Directory for Experiments Folder")
	public String experimentDir = System.getProperty("user.dir") + File.separator + "";
	

	@Parameter(names={"-p", "--paramFile","--paramfile"}, description="File containing Parameter Space of Execution", required=true)
	public String paramFile;
	
	@Parameter(names="--numIterations", description = "Total number of iterations to perform")
	public int numIteratations = Integer.MAX_VALUE;
	
	@Parameter(names="--runtimeLimit", description = "Total Wall clock time to execute for")
	public int runtimeLimit = Integer.MAX_VALUE;
	
	@Parameter(names="--totalNumRunLimit" , description = "Total number of target algorithm runs to execute")
	public int totalNumRunLimit = Integer.MAX_VALUE;

	@Parameter(names="--numTestInstances", description = "Number of instances to test against (Will execute min of this, and number of instances in test Instance File)")
	public int numberOfTestInstances = 10;

	
	@Parameter(names="--runHashCodeFile", description="File containing a list of Run Hashes one per line (Either with just the format on each line, or with the following text per line: \"Run Hash Codes: (Hash Code) After (n) runs\". The number of runs in this file need not match the number of runs that we execute, this file only ensures that the sequences never diverge. Note the n is completely ignored so the order they are specified in is the order we expect the hash codes in this version", converter=ReadableFileConverter.class)
	public File runHashCodeFile;
	
	
	@Parameter(names="--modelHashCodeFile", description="File containing a list of Model Hashes one per line with the following text per line: \"Preprocessed Forest Built With Hash Code: (n)\" or \"Random Forest Built with Hash Code: (n)\" where (n) is the hashcode", converter=ReadableFileConverter.class)
	public Object modelHashCodeFile;
	
	
	@Parameter(names="--runID", description="String that identifies this run for logging purposes")
	public String runID = "Run-" + (new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss-SSS")).format(new Date());
	
	
	@Parameter(names="--numPCA", description="Number of prinicipal components of features")
	public int numPCA = 7;

	
	
	@Parameter(names="--expectedImprovementFunction", description="Expected Improvement Function to Use")
	public ExpectedImprovementFunctions expFunc = ExpectedImprovementFunctions.EXPONENTIAL;

	@Parameter(names="--nuberOfChallengers", description="Number of Challengers needed for Local Search", validateWith=PositiveInteger.class)
	public int numberOfChallengers = 10;
	
	@Parameter(names="--numberOfRandomConfigsInEI", description="Number of Random Configurations to evaluate in EI Search", validateWith=PositiveInteger.class)
	public int numberOfRandomConfigsInEI = 10000;
	

	@Parameter(names="--stateSerializer", description="Controls how the state will be saved to disk")
	public StateSerializers stateSerializer = StateSerializers.LEGACY;


	@Parameter(names="--stateDeserializer", description="Controls how the state will be saved to disk")
	public StateSerializers statedeSerializer = StateSerializers.LEGACY;

	@Parameter(names="--restoreStateFrom", description="The Location (State Deserializer Dependent) of States")
	public String restoreStateFrom = null;
	
	@Parameter(names="--restoreIteration", description="The Iteration to Restore")
	public Integer restoreIteration = null;
	
	@Parameter(names="--executionMode", description="Mode of Automatic Configurator to run")
	public ExecutionMode execMode = ExecutionMode.SMAC;
	
	@ParametersDelegate
	public RandomForestConfig randomForestConfig = new RandomForestConfig();

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
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("Configuration\n");
		try {
		for(Field f : this.getClass().getDeclaredFields())
		{
			if(f.getAnnotation(Parameter.class) != null)
			sb.append(f.getName());
			sb.append(" = ");
			
			Class<?> o = f.getType();
			if(o.isPrimitive())
			{
				sb.append(f.get(this).toString());
			} else
			{
				Object obj = f.get(this);
				if(obj == null)
				{
					sb.append("null");
				} else if(obj instanceof File)
				{
					sb.append(((File) obj).getAbsolutePath());
				} else if (obj instanceof String)
				{
					sb.append(obj);
				} else if (obj instanceof Long)
				{
					sb.append(obj.toString());
				} else if(obj instanceof Integer)
				{
					sb.append(obj.toString());
				} else if (obj instanceof Enum)
				{
					sb.append(((Enum) obj).name());
				} else if (obj instanceof RandomForestConfig)
				{
					sb.append(obj.toString());
				} else if(obj instanceof ScenarioConfig)
				{
					sb.append(obj.toString());
				}
				else {
					//We throw this because we have no guarantee that toString() is meaningful
					throw new IllegalArgumentException("Failed to convert type configuration option to a string " + f.getName() + "=" +  obj + " type: " + o) ;
				}
			}
			sb.append("\n");
		}
		return sb.toString();
		} catch(RuntimeException e)
		{
			throw e;
			
		} catch(Exception e)
		{
			throw new RuntimeException(e); 
		}
		
	}
			
	
}
