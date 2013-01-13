package ca.ubc.cs.beta.aclib.options;


import com.beust.jcommander.Parameter;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.*;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;

/**
 * Options object controlling validation
 * 
 */
@UsageTextField(title="Validation Options", description="Options that control validation")
public class ValidationOptions extends AbstractOptions{

	@Parameter(names={"--numSeedsPerTestInstance","--numberOfSeedsPerTestInstance"}, description="number of test seeds to use per instance during validation", validateWith=FixedPositiveInteger.class)
	public int numberOfTestSeedsPerInstance = 1000;
	
	@Parameter(names={"--numTestInstances","--numberOfTestInstances"}, description = "number of instances to test against (will execute min of this, and number of instances in test Instance File)", validateWith=FixedPositiveInteger.class)
	public int numberOfTestInstances = Integer.MAX_VALUE;

	@Parameter(names={"--numValidationRuns","--numberOfValidationRuns"}, description = "approximate number of validation runs to do", validateWith=FixedPositiveInteger.class)
	public int numberOfValidationRuns = 1000;
	
	@Parameter(names="--validationRoundingMode", description="selects whether to round the number of validation (to next multiple of numTestInstances")
	public ValidationRoundingMode validationRoundingMode = ValidationRoundingMode.UP;

	@Parameter(names="--validationHeaders", description="put headers on output CSV files for validation")
	public boolean validationHeaders = true;
	
	@UsageTextField(domain="[0, Infinity) U {-1}",defaultValues="Auto Detect")
	@Parameter(names="--maxTimestamp", description="maximimum relative timestamp in the trajectory file to configure against. -1 means auto-detect", required=false)
	public double maxTimestamp = -1;
	
	
	@Parameter(names="--minTimestamp", description="minimum relative timestamp in the trajectory file to configure against.", required=false, validateWith=ZeroInfinityHalfOpenIntervalRight.class)
	public double minTimestamp = 0;
	
	@Parameter(names="--multFactor", description="base of the geometric progression of timestamps to validate (timestamps selected are: maxTime*multFactor$^{-n}$ where $n$ is $\\{1,2,3,4...\\}$ while timestamp >= minTimestamp )", validateWith=ZeroInfinityOpenInterval.class)
	public double multFactor = 2;

	@Parameter(names="--validateOnlyLastIncumbent", description="validate only the last incumbent found")
	public boolean validateOnlyLastIncumbent = true;
	
	@Parameter(names="--outputFileSuffix", description="Suffix to add to validation run files (for grouping)")
	public String outputFileSuffix = "";

	@Parameter(names="--validateAll",description="Validate every entry in the trajectory file (overrides other validation options)")
	public boolean validateAll = false;

	@Parameter(names={"--writeConfigurationMatrix","--writeThetaMatrix"}, description="Write the configuration matrix")
	public boolean writeThetaMatrix;

	@Parameter(names={"--saveStateFile"}, description="Save a state file consisting of all the runs we did")
	public boolean saveStateFile;
}
