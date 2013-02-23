package ca.ubc.cs.beta.aclib.options;


import ca.ubc.cs.beta.aclib.misc.jcommander.validator.*;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;

import com.beust.jcommander.Parameter;

/**
 * Options object for Random Forest 
 * @author sjr
 *
 */
@UsageTextField(title="Random Forest Options", description="Options used when building the Random Forests")
public class RandomForestOptions extends AbstractOptions{

	@Parameter(names = "--splitMin", description = "minimum number of elements needed to split a node ", validateWith=NonNegativeInteger.class )
	public int splitMin = 10;

	@Parameter(names = "--fullTreeBootstrap", description = "bootstrap all data points into trees")
	public boolean fullTreeBootstrap = false;

	@Parameter(names = {"--storeDataInLeaves"}, description = "store full data in leaves of trees")
	public boolean storeDataInLeaves = false;
	
	@Parameter(names = {"--logModel"}, description = "store response values in log-normal form")
	public boolean logModel = true;

	@Parameter(names = {"--numTrees","--nTrees", "--numberOfTrees"}, description = "number of trees to create in random forest", validateWith=FixedPositiveInteger.class)
	public int numTrees = 10;
	
	@Parameter(names="--minVariance", description="minimum allowed variance", validateWith=ZeroInfinityOpenInterval.class)
	public double minVariance = Math.pow(10,-14);

	@Parameter(names="--ratioFeatures", description="ratio of the number of features to consider when splitting a node", validateWith=ZeroOneHalfOpenLeftDouble.class)
	public double ratioFeatures = 5.0/6.0;

	@Parameter(names="--preprocessMarginal", description="build random forest with preprocessed marginal")
	public boolean preprocessMarginal = true;
	
	@Parameter(names="--shuffleImputedValues", description="shuffle imputed value predictions between trees")
	public boolean shuffleImputedValues = false;

	@Parameter(names="--ignoreConditionality", description="ignore conditionality for building the model")
	public boolean ignoreConditionality = false;

	@Parameter(names="--useBrokenVarianceCalculation", description="use the broken variance calculation when building the model", hidden=true)
	public boolean brokenVarianceCalculation = false;
	
	@Parameter(names="--penalizeImputedValues", description="treat imputed values that fall above the cutoff time, and below the penalized max time, as the penalized max time")
	public boolean penalizeImputedValues = false;
	
	@Parameter(names={"--subsampleValuesWhenLowOnMemory","--subsampleValuesWhenLowMemory"}, description="subsample model input values when the amount of memory available drops below a certain threshold (see --subsampleValuesWhenLowMemory)")
	public boolean subsampleValuesWhenLowMemory = false;
	
	@Parameter(names="--freeMemoryPecentageToSubsample", description="when free memory percentage drops below this percent we will apply the subsample percentage", validateWith=ZeroOneHalfOpenLeftDouble.class)
	public double freeMemoryPercentageToSubsample=0.25;
	
	@Parameter(names="--subsamplePercentage", description="multiply the number of points used when building model by this value", validateWith=ZeroOneHalfOpenLeftDouble.class)
	public double subsamplePercentage = 0.9;

	@Parameter(names="--imputeMean", description="impute the mean value for the all censored data points")
	public boolean imputeMean; 
}
