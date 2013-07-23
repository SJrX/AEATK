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

	@Parameter(names = {"--rf-split-min","--split-min","--splitMin"}, description = "minimum number of elements needed to split a node ", validateWith=NonNegativeInteger.class )
	public int splitMin = 10;

	@Parameter(names = {"--rf-full-tree-bootstrap","--fullTreeBootstrap"}, description = "bootstrap all data points into trees")
	public boolean fullTreeBootstrap = false;

	@Parameter(names = {"--rf-store-data","--rf-store-data-in-leaves","--storeDataInLeaves"}, description = "store full data in leaves of trees")
	public boolean storeDataInLeaves = false;
	
	@UsageTextField(defaultValues="true if optimizing runtime, false if optimizing quality")
	@Parameter(names = {"--rf-log-model","--log-model","--logModel"}, description = "store response values in log-normal form")
	public Boolean logModel = null;

	@Parameter(names = {"--rf-num-trees","--num-trees","--numTrees","--nTrees", "--numberOfTrees"}, description = "number of trees to create in random forest", validateWith=FixedPositiveInteger.class)
	public int numTrees = 10;
	
	@Parameter(names={"--rf-min-variance","--minVariance"}, description="minimum allowed variance", validateWith=ZeroInfinityOpenInterval.class)
	public double minVariance = Math.pow(10,-14);

	@Parameter(names={"--rf-ratio-features","--ratioFeatures"}, description="ratio of the number of features to consider when splitting a node", validateWith=ZeroOneHalfOpenLeftDouble.class)
	public double ratioFeatures = 5.0/6.0;

	@Parameter(names={"--rf-preprocess-marginal", "preprocessMarginal"}, description="build random forest with preprocessed marginal")
	public boolean preprocessMarginal = true;
	
	@Parameter(names={"--rf-shuffle-imputed-values","--shuffleImputedValues"}, description="shuffle imputed value predictions between trees")
	public boolean shuffleImputedValues = false;

	@Parameter(names={"--rf-ignore-conditionality","--ignoreConditionality"}, description="ignore conditionality for building the model")
	public boolean ignoreConditionality = false;

	@Parameter(names="--useBrokenVarianceCalculation", description="use the broken variance calculation when building the model", hidden=true)
	public boolean brokenVarianceCalculation = false;
	
	@Parameter(names={"--rf-penalize-imputed-values","--penalizeImputedValues"}, description="treat imputed values that fall above the cutoff time, and below the penalized max time, as the penalized max time")
	public boolean penalizeImputedValues = false;
	
	@Parameter(names={"--rf-subsample-values-when-low-on-memory","--subsampleValuesWhenLowOnMemory","--subsampleValuesWhenLowMemory"}, description="subsample model input values when the amount of memory available drops below a certain threshold (see --subsampleValuesWhenLowMemory) (Not Tested)")
	public boolean subsampleValuesWhenLowMemory = false;
	
	@Parameter(names={"--rf-subsample-memory-percentage","--freeMemoryPecentageToSubsample"}, description="when free memory percentage drops below this percent we will apply the subsample percentage", validateWith=ZeroOneHalfOpenLeftDouble.class)
	public double freeMemoryPercentageToSubsample=0.25;
	
	@Parameter(names={"--rf-subsample-percentage","--subsamplePercentage"}, description="multiply the number of points used when building model by this value", validateWith=ZeroOneHalfOpenLeftDouble.class)
	public double subsamplePercentage = 0.9;

	@Parameter(names={"--rf-impute-mean","--imputeMean"}, description="impute the mean value for the all censored data points")
	public boolean imputeMean; 
}
