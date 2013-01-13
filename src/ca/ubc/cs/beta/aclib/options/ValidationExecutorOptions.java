package ca.ubc.cs.beta.aclib.options;

import java.io.File;

import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;

import com.beust.jcommander.Parameter;
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
	
	
	
	@Parameter(names={"-e","--experimentDir"}, description="Root Directory for Experiments Folder")
	public String experimentDir = System.getProperty("user.dir") + File.separator + "";
	
	@Parameter(names="--seed", description="Seed for Random Number Generator")
	public long seed = 0;
	
	@Parameter(names="--numRun", description="Number of Run the Run", required=true)
	public long numRun = 0;
	
	
	@Parameter(names="--configuration", description="Parameter configuration to validate (In the same format calls are made to the algorithm) [Use 'DEFAULT' to validate the default]")
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
	
	@Parameter(names="--validateTestInstances", description="Use the test instances for validation")
	public boolean validateTestInstances = true;
	
	@Parameter(names="--waitForPersistedRunCompletion", description="If the Target Algorithm Evaluator is persistent, then you can optionally not wait for it to finish, and come back later")
	public boolean waitForPersistedRunCompletion = true;


	@Parameter(names="--randomConfigurations", description="Number of random configurations to validate")
	public int randomConfigurations = 0;
}
