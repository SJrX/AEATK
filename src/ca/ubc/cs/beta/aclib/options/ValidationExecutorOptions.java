package ca.ubc.cs.beta.aclib.options;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.validators.PositiveInteger;

/**
 * Options controlling the Stand Alone Validation Utility
 * 
 * @see ValidationOptions 
 */
public class ValidationExecutorOptions {
	
	@ParametersDelegate
	public ScenarioOptions scenarioConfig = new ScenarioOptions();
	
	
	@Parameter(names="--maxConcurrentAlgoExecs", description="Maximum number of concurrent target algorithm executions", validateWith=PositiveInteger.class)
	public int maxConcurrentAlgoExecs = 1;
	
	@Parameter(names={"-e","--experimentDir"}, description="Root Directory for Experiments Folder")
	public String experimentDir = System.getProperty("user.dir") + File.separator + "";
	
	@Parameter(names="--seed", description="Seed for Random Number Generator [0 means don't use a seed]")
	public long seed = 0;
	
	@Parameter(names="--configuration", description="Parameter configuration to validate (In the same format calls are made to the algorithm")
	public String incumbent;
	
	@Parameter(names="--trajectoryFile", description="Trajectory File to read configurations from")
	public File trajectoryFile;
	
	@ParametersDelegate
	public ValidationOptions validationOptions = new ValidationOptions();

	@Parameter(names="--tunerTime", description="Tuner Time when Validation occured (when specifying the configuration this is simply reported in the output file, when using a trajectory file we use the incumbent at this time, if you set this to -1 we use the tuner time from the scenario file or 0 if reading configuration from command line)")
	public double tunerTime = -1; 
	
	@Parameter(names="--useScenarioOutDir")
	public boolean useScenarioOutDir = false;

	@Parameter(names="--empericalPerformance", description="Estimated performance of configuration on training set (-1 means use the trajectory file value or 0 if not trajectory file)")
	public double empericalPerformance = -1; 
	
	@Parameter(names="--tunerOverheadTime", description="Amount of Tuner Overhead time to report in the output (-1 means use trajectory file overhead or 0 if no trajectory file)")
	public double tunerOverheadTime = -1;
}
