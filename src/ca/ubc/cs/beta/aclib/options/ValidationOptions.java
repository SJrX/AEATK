package ca.ubc.cs.beta.aclib.options;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.*;

/**
 * Options object controlling validation
 * 
 */
public class ValidationOptions extends AbstractOptions{

	@Parameter(names="--numSeedsPerTestInstance", description="Number of test seeds to use per instance during validation", validateWith=PositiveInteger.class)
	public int numberOfTestSeedsPerInstance = 1000;
	
	@Parameter(names="--numTestInstances", description = "Number of instances to test against (Will execute min of this, and number of instances in test Instance File)", validateWith=FixedPositiveInteger.class)
	public int numberOfTestInstances = Integer.MAX_VALUE;

	@Parameter(names="--numberOfValidationRuns", description = "Approximate Number of Validation Runs to do", validateWith=FixedPositiveInteger.class)
	public int numberOfValidationRuns = 1000;
	
	@Parameter(names="--validationRoundingMode", description="Whether to round the number of validation runs up or down (to next multiple of numTestInstances")
	public ValidationRoundingMode validationRoundingMode = ValidationRoundingMode.UP;

	@Parameter(names="--validationHeaders", description="Put headers on output CSV files for Validation")
	public boolean validationHeaders = true;
	
	@Parameter(names="--maxTimestamp", description="The Relative Timestamp of the config in the trajectory file to min (Defaults to last tuner time)", required=false)
	public double maxTimestamp = -1;
	
	@Parameter(names="--minTimestamp", description="The Relative Timestamp of the config in the trajectory file to max", required=false)
	public double minTimestamp = 1;
	
	@Parameter(names="--multFactor", description="Other timestamps to generate are used as the maxTime*multFactor^-n where n is {1,2,3,4...} while timestamp >= minTimestamp ")
	public double multFactor = 2;

	
}
