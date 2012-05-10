package ca.ubc.cs.beta.smac.model;

import ca.ubc.cs.beta.ac.config.RandomForestConfig;
import ca.ubc.cs.beta.config.SMACConfig;
import ca.ubc.cs.beta.models.fastrf.RegtreeBuildParams;
import ca.ubc.cs.beta.random.SeedableRandomSingleton;
import ec.util.MersenneTwister;

public class SMACRandomForestHelper {

	public static RegtreeBuildParams getRandomForestBuildParams(RandomForestConfig rfConfig, int numberOfFeatures, int[] categoricalSize, int[][] condParents, int[][][] condParentVals)
	{
	/**
	 * Parameter File Generator
	 */
	RegtreeBuildParams buildParams = new RegtreeBuildParams();
	
	buildParams.condParents = null;
	/**
	 * Most of the defaults are either read from the config or were 
	 * pilfered from a run of the MATLAB
	 * The actual values may need to be more intelligently chosen.
	 */
	buildParams.splitMin = rfConfig.splitMin;
	buildParams.ratioFeatures = rfConfig.ratioFeatures;//(5.0/6);
	
	buildParams.logModel = ((rfConfig.logModel) ? 1 : 0);
	buildParams.storeResponses = rfConfig.storeDataInLeaves;
	buildParams.random = SeedableRandomSingleton.getRandom();
	
	buildParams.minVariance = rfConfig.minVariance;
	
	
	//buildParams.random = new MersenneTwister(87);
	
	buildParams.random = SeedableRandomSingleton.getRandom();
	
	
	//int numberOfParameters = params.getParameterNames().size();
	//int numberOfFeatures = features.getDataRow(0).length;
	
	/**
	 * THis needs to be the length of the number of parameters in a configuration + the number of features in a configuration
	 */
	
	
	buildParams.catDomainSizes = new int[categoricalSize.length+ numberOfFeatures];
	System.arraycopy(categoricalSize, 0, buildParams.catDomainSizes, 0, categoricalSize.length);
	
	
	//buildParams.catDomainSizes[i] = 0;
	
	
	
	buildParams.condParents = new int[categoricalSize.length+numberOfFeatures][];
	for(int i=0; i < categoricalSize.length; i++)
	{
		buildParams.condParents[i] = condParents[i];
	}
	
	
	buildParams.condParentVals = new int[categoricalSize.length+numberOfFeatures][][];
	
	for(int i=0; i < condParentVals.length; i++)
	{
		buildParams.condParentVals[i] = condParentVals[i];
	}
	
	for(int i=categoricalSize.length; i < buildParams.condParents.length; i++)
	{
		buildParams.condParents[i] = new int[0];
		buildParams.condParentVals[i] = new int[0][0];
	}


	return buildParams;	
	}
}
