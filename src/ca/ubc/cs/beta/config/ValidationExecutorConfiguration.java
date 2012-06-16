package ca.ubc.cs.beta.config;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.validators.PositiveInteger;

public class ValidationExecutorConfiguration {
	
	@ParametersDelegate
	public ScenarioConfig scenarioConfig = new ScenarioConfig();
	
	
	@Parameter(names="--maxConcurrentAlgoExecs", description="Maximum number of concurrent target algorithm executions", validateWith=PositiveInteger.class)
	public int maxConcurrentAlgoExecs = 1;
	
	@Parameter(names={"-e","--experimentDir"}, description="Root Directory for Experiments Folder")
	public String experimentDir = System.getProperty("user.dir") + File.separator + "";
	
	@Parameter(names="--seed", description="Seed for Random Number Generator [0 means don't use a seed]")
	public long seed = 0;
	
	@Parameter(names="--configuration", description="Parameter configuration to validate (In the same format calls are made to the algorithm")
	public String incumbent;
	
	@ParametersDelegate
	public ValidationOptions validationOptions = new ValidationOptions();

	@Parameter(names="--tunerTime", description="Tuner Time when Validation occured")
	public double tunerTime = 0; 
	
	@Parameter(names="--useScenarioOutDir")
	public boolean useScenarioOutDir = false;
}
