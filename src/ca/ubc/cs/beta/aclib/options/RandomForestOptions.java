package ca.ubc.cs.beta.aclib.options;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

/**
 * Options object for Random Forest 
 * @author sjr
 *
 */
public class RandomForestOptions extends AbstractOptions{

	@Parameter(names = "--splitMin", description = "Minimum number of elements needed to split a node ", validateWith=PositiveInteger.class )
	public int splitMin = 10;

	@Parameter(names = "--fullTreeBootstrap", description = "Bootstrap all data points into trees")
	public boolean fullTreeBootstrap = false;

	@Parameter(names = {"--storeDataInLeaves"}, description = "Store full data in leaves of trees")
	public boolean storeDataInLeaves = false;
	
	@Parameter(names = {"--logModel"}, description = "Store data in Log Normal form")
	public boolean logModel = false;

	@Parameter(names = {"--nTrees"}, description = "Number of Trees in Random Forest", validateWith=PositiveInteger.class)
	public int numTrees = 10;
	
	@Parameter(names="--minVariance", description="Minimum allowed variance")
	public double minVariance = Math.pow(10,-14);

	@Parameter(names="--ratioFeatures", description="Number of features to consider when building Regression Forest")
	public double ratioFeatures = 5.0/6.0;

	@Parameter(names="--preprocessMarginal", description="Build Random Forest with Preprocessed Marginal")
	public boolean preprocessMarginal = true;	//TODO: cleaner way of setting defaults.

}
