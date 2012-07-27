package ca.ubc.cs.beta.aclib.model.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.misc.math.distribution.TruncatedNormalDistribution;
import ca.ubc.cs.beta.aclib.misc.model.SMACRandomForestHelper;
import ca.ubc.cs.beta.aclib.model.data.SanitizedModelData;
import ca.ubc.cs.beta.aclib.options.RandomForestOptions;
import ca.ubc.cs.beta.aclib.runhistory.RunHistory;
import ca.ubc.cs.beta.models.fastrf.RandomForest;
import ca.ubc.cs.beta.models.fastrf.RegtreeBuildParams;

/**
 * Builds a model with Capped Data using the algorithm defined in:
 * 
 * Bayesian Optimization With Censored Response Data 
 * Frank Hutter, Holger Hoos, and Kevin Leyton-Brown
 * 2011
 * (http://www.cs.ubc.ca/labs/beta/Projects/SMAC/papers/11-NIPS-workshop-BO-with-censoring.pdf)
 * 
 * @author sjr
 *
 */
public class AdaptiveCappingModelBuilder implements ModelBuilder{

	
	protected final RandomForest forest;
	protected final RandomForest preprocessedForest;
	
	private static final Logger log = LoggerFactory.getLogger(AdaptiveCappingModelBuilder.class);
	
	/**
	 * Builds the Model 
	 * @param mds 						sanitized model data
	 * @param rfOptions					random forest configuration options
	 * @param runHistory				runhistory object containing the runs to use
	 * @param rand						random object to use during construction
	 * @param imputationIterations		the number of imputation iterations
	 * @param cutoffTime				the max algorithm run time
	 * @param penaltyFactor			    the penalty factor for runs which timed out
	 */
	public AdaptiveCappingModelBuilder(SanitizedModelData mds, RandomForestOptions rfOptions, RunHistory runHistory, Random rand, int imputationIterations, double cutoffTime, double penaltyFactor)
	{
		double maxValue = mds.transformResponseValue(cutoffTime*penaltyFactor);
		
		/**
		 * General Algorithm is as follows
		 * 
		 * 1) Build a tree with non censored data
		 * 
		 *    while(numIterations < imputedLimit && mean value of all imputed values increases by epsilon)
		 *    {
		 *       foreach(tree)
		 *       {
		 *       	Subsample the data points.
		 *       	Build the response vector for the data points used.
		 *       	For each censored value in the response vector, sample from the last built tree.
		 *    	 }
		 *    
		 *    }
		 *    
		 */
		
		//=== Get predictors, response values, and censoring indicators from RunHistory.
		int[][] theta_inst_idxs = runHistory.getParameterConfigurationInstancesRanByIndex();
		//=== Change to 0-based indexing
		for(int i=0; i < theta_inst_idxs.length; i++)
		{
			theta_inst_idxs[i][0]--;
			theta_inst_idxs[i][1]--;
		}
		
		double[] responseValues = mds.getResponseValues();
		boolean[] censoringIndicators = runHistory.getCensoredFlagForRuns();

		//=== Initialize subsets corresponding to censored/noncensored values.
		ArrayList<int[]> censoredThetaInst = new ArrayList<int[]>(responseValues.length);
		ArrayList<int[]> nonCensoredThetaInst = new ArrayList<int[]>(responseValues.length);
		ArrayList<Double> nonCensoredResponses = new ArrayList<Double>(responseValues.length);

		//=== Break into censored and noncensored.
		int censoredCount = 0;
		for(int i=0; i < responseValues.length; i++)
		{
			if(!censoringIndicators[i])
			{
				nonCensoredThetaInst.add(theta_inst_idxs[i]);
				nonCensoredResponses.add(responseValues[i]);
			} else
			{
				censoredThetaInst.add(theta_inst_idxs[i]);
				censoredCount++;
			}
		}
		int[][] non_cens_theta_inst_idxs = nonCensoredThetaInst.toArray(new int[0][]);
		double[] non_cens_responses = convertToPrimitiveArray(nonCensoredResponses.toArray(new Double[0]));
		
		log.info("Building Random Forest with {} censored runs out of {} total ", censoredCount, censoringIndicators.length);
		
		//=== Building random forest with non censored data.
		RandomForest rf = buildRandomForest(mds,rfOptions,non_cens_theta_inst_idxs, non_cens_responses, false);
		
		if(rfOptions.fullTreeBootstrap)
		{
			throw new IllegalStateException("Cannot build random forest with Adaptive Capping on Full Tree Bootstap");
			/**
			 * This should be an easy fix, we just need to sample correctly.
			 */
		}

		int numTrees = rfOptions.numTrees;
		int sampleSize = responseValues.length;

		//=== Initialize map from censored response indices to Map from trees to their dataIdxs for that response (only for trees that actually have that data point).
		Map<Integer, Map<Integer, List<Integer>>> censoredSampleIdxs = new HashMap<Integer, Map<Integer, List<Integer>>>();
		for (int i = 0; i < sampleSize; i++) {
			if(censoringIndicators[i]){
				censoredSampleIdxs.put(i,new HashMap<Integer,List<Integer>>());
			}
		}		
		
		//=== Set up dataIdx once and for all (via bootstrap sampling), and keep track of which runs are censored in censoredSampleIdxs.
	    int[][] dataIdxs = new int[numTrees][sampleSize];
//		theta_inst_idxs = runHistory.getParameterConfigurationInstancesRanByIndex();
	    for (int j = 0; j < numTrees; j++) {
	        for (int k = 0; k < sampleSize; k++) {
        	   int sampleIdxToUse =  rand.nextInt(sampleSize);
               dataIdxs[j][k] = sampleIdxToUse;
               if (censoringIndicators[sampleIdxToUse]){
            	   if(censoredSampleIdxs.get(sampleIdxToUse).get(j) == null){
                	   censoredSampleIdxs.get(sampleIdxToUse).put(j, new ArrayList<Integer>());
            	   }            		   
            	   censoredSampleIdxs.get(sampleIdxToUse).get(j).add(k);
               }
	        }
	    }
	    
	    /**
		 * While imputed values change more than a limit, continue.
		 */
		double differenceFromLastMean = 0;
		double[][] yHallucinated = new double[numTrees][sampleSize];
		
		//=== Initialize yHallucinated to the observed data (for censored data points that's a lower bound).
		for(int tree=0; tree<yHallucinated.length; tree++){
			for (int sampleCount = 0; sampleCount < yHallucinated[tree].length; sampleCount++){
				yHallucinated[tree][sampleCount] = responseValues[dataIdxs[tree][sampleCount]];
			}
		}
		
		for(int i=0; i < imputationIterations; i++)
		{
			if( censoredSampleIdxs.isEmpty() ) break;
			//=== Get predictions for all censored values once and for all in this iteration. 
			int Xlength = mds.getConfigs()[0].length + mds.getPCAFeatures()[0].length;
			double[][] predictors = new double[censoredSampleIdxs.size()][Xlength];
			int j=0;
			//=== Loop over all the censored data points.
			for (Integer sampleIdxToUse: censoredSampleIdxs.keySet()){
				double[] configArray = mds.getConfigs()[theta_inst_idxs[sampleIdxToUse][0]];
				double[] featureArray = mds.getPCAFeatures()[theta_inst_idxs[sampleIdxToUse][1]];
				for(int m=0; m < configArray.length; m++)
				{
					predictors[j][m] = configArray[m];
				}
				for(int m=0; m < featureArray.length; m++)
				{
					predictors[j][m+configArray.length] = featureArray[m];
				}
				j++;
			}
			//== Now predict.
			double[][] prediction = RandomForest.apply(rf, predictors);
			
			j=0;
			//=== Loop over all the censored data points.
			for (Entry<Integer, Map<Integer, List<Integer>>> ent : censoredSampleIdxs.entrySet()){
				int sampleIdxToUse = ent.getKey();
				
				//=== Collect number of samples we need to take for this point.
				Map<Integer, List<Integer>> treeDataIdxsMap = ent.getValue();
				int numSamplesToGet = 0;
				for (List<Integer> l : treeDataIdxsMap.values()){
					numSamplesToGet += l.size();
				}
			
				//=== Get the samples (but cap them at maxValue). 
				TruncatedNormalDistribution tNorm = new TruncatedNormalDistribution(prediction[j][0], prediction[j][1], responseValues[sampleIdxToUse],rand);
				j++;
				double[] samples = tNorm.getValuesAtStratifiedShuffledIntervals(numSamplesToGet);
				for (int k = 0; k < samples.length; k++) {
					samples[k] = Math.min(samples[k], maxValue);
				}

				//=== Populate the trees at their dataIdxs with the samples (and update differenceFromLastMean)
				int count=0;
				double increaseThisDataPoint = 0;
				for( Entry<Integer, List<Integer>> ent2 : treeDataIdxsMap.entrySet() ){
					int tree = ent2.getKey();
					List<Integer> responseLocationsInTree = ent2.getValue();
					for(int k = 0; k<responseLocationsInTree.size(); k++){
						int responseLocationInTree = responseLocationsInTree.get(k);
						increaseThisDataPoint += (samples[count] - yHallucinated[tree][responseLocationInTree]);
						yHallucinated[tree][responseLocationInTree] = samples[count++];
					}
				}
				differenceFromLastMean += (increaseThisDataPoint / count);
			}
			differenceFromLastMean /= censoredSampleIdxs.size();			
			
			//=== Build a new random forest.
			log.info("Building random forest with imputed values iteration {}", i);
			rf = buildImputedRandomForest(mds,rfOptions,theta_inst_idxs, dataIdxs, yHallucinated, false);
			
			if(differenceFromLastMean < Math.pow(10,-10))
			{
				log.info("Means of imputed values stopped increasing in imputation iteration {} (increase {})",i,differenceFromLastMean);
				break;
			} else
			{
		    	log.info("Mean increase in imputed values in imputation iteration {}:{}", i, differenceFromLastMean);
	        }
		}
		
		forest = rf;
		
		if(rfOptions.preprocessMarginal)
		{
			preprocessedForest = RandomForest.preprocessForest(forest, mds.getPCAFeatures());
		} else
		{
			preprocessedForest = null;
		}
	}
	
	
	@Override
	public RandomForest getRandomForest()
	{
		return forest;
	}
	@Override
	public RandomForest getPreparedRandomForest() {
		return preprocessedForest;
	}
	
	/**
	 * Converts an array of Double[] to a double[] array.
	 * @param arr array of Double[]
	 * @return primitive value array
	 */
	private double[] convertToPrimitiveArray(Double[] arr)
	{
		double[] d = new double[arr.length];
		for(int i=0; i < d.length; i++)
		{
			d[i] = arr[i].doubleValue();
		}
		return d;
	}

//  This isn't used currently, maybe it will be used in the future, you can delete this if you want
//	@SuppressWarnings("unused")
//	/**
//	 * Converts an array of Integer[] to int[] 
//	 * @param arr	array of Integers
//	 * @return		array of int
//	 */
//	private int[] convertToPrimitive(Integer[] arr)
//	{
//		int[] d = new int[arr.length];
//		for(int i=0; i < d.length; i++)
//		{
//			d[i] = arr[i].intValue();
//		}
//		return d;
//	}
	
	/**
	 * Builds a Random forest 
	 * @param mds 				sanitized model data
	 * @param rfOptions			options for building the random forest
	 * @param theta_inst_idxs	array of [thetaIdx, instanceIdx] values
	 * @param responseValues	response values for model
	 * @param preprocessed		<code>true</code> if we should build a model with preprocessed marginals, <code>false</code> otherwise
	 * @return constructed random forest
	 */
	private static RandomForest buildRandomForest(SanitizedModelData mds, RandomForestOptions rfOptions, int[][] theta_inst_idxs, double[] responseValues, boolean preprocessed)
	{
		
		double[][] features = mds.getPCAFeatures();
		
		
		double[][] configs = mds.getConfigs();
		
		int[] categoricalSize = mds.getCategoricalSize();
		int[][] condParents = mds.getCondParents();
		int[][][] condParentVals = mds.getCondParentVals();
		
		/*
		System.out.println("y = \n" + Arrays.toString(responseValues));
		System.out.println("categoricalSize = \n" + Arrays.toString(categoricalSize));
		System.out.println("parent_param_idxs = \n" + Arrays.deepToString(condParents));
		*/
		
		int numTrees = rfOptions.numTrees;
		
		
/*		
		for(int i=0; i < theta_inst_idxs.length; i++)
		{
			theta_inst_idxs[i][0]--;
			theta_inst_idxs[i][1]--;
		}
*/
		RegtreeBuildParams buildParams = SMACRandomForestHelper.getRandomForestBuildParams(rfOptions, features[0].length, categoricalSize, condParents, condParentVals);
		
		log.debug("Building Random Forest with Parameters: {}", buildParams);
		RandomForest forest;
		
		if(rfOptions.fullTreeBootstrap)
		{
			
			 int N = responseValues.length;
			 int[][] dataIdxs = new int[numTrees][N];
		        for (int i = 0; i < numTrees; i++) {
		            for (int j = 0; j < N; j++) {
		                dataIdxs[i][j] = j;
		            }
		        }
		        
		        
		      forest = RandomForest.learnModel(numTrees, configs, features, theta_inst_idxs, responseValues, dataIdxs, buildParams);
		      
		} else
		{
			  forest = RandomForest.learnModel(numTrees, configs, features, theta_inst_idxs, responseValues, buildParams);
		}
		

		
		return forest;
	}
	
	/**
	 * 
	 * @param mds 				sanitized model data
	 * @param rfOptions			options for building the random forest
	 * @param dataIdxs			array of values of theta_inst_idxs to use when building each tree. [For instance if we are building 10 trees, the size will be int[10][N] where N is the number of entries to build with]
	 * @param theta_inst_idxs	array of [thetaIdx, instanceIdx] values
	 * @param responseValues	array of values of the response to use when building each tree. [ For instance if we are building 10 trees, the size will be double[10][N] where N is the number of entries to build with]
	 * @param preprocessed		<code>true</code> if we should build a model with preprocessed marginals, <code>false</code> otherwise
	 * @return
	 */
	private static RandomForest buildImputedRandomForest(SanitizedModelData mds, RandomForestOptions rfOptions, int[][] theta_inst_idxs,int[][] dataIdxs, double[][] responseValues, boolean preprocessed)
	{
		
		double[][] features = mds.getPCAFeatures();
		
		
		double[][] configs = mds.getConfigs();
		
		int[] categoricalSize = mds.getCategoricalSize();
		int[][] condParents = mds.getCondParents();
		int[][][] condParentVals = mds.getCondParentVals();
		
		/*
		System.out.println("y = \n" + Arrays.toString(responseValues));
		System.out.println("categoricalSize = \n" + Arrays.toString(categoricalSize));
		System.out.println("parent_param_idxs = \n" + Arrays.deepToString(condParents));
		*/
		
		int numTrees = rfOptions.numTrees;
		

/*		
		for(int i=0; i < theta_inst_idxs.length; i++)
		{
			theta_inst_idxs[i][0]--;
			theta_inst_idxs[i][1]--;
		}
*/		
		RegtreeBuildParams buildParams = SMACRandomForestHelper.getRandomForestBuildParams(rfOptions, features[0].length, categoricalSize, condParents, condParentVals);
		
		log.debug("Building Random Forest with Parameters: {}", buildParams);
		RandomForest forest;
		
		        
		        
		   forest = RandomForest.learnModelImputedValues(numTrees, configs, features, theta_inst_idxs, responseValues, dataIdxs, buildParams);
		

		
		return forest;
	}
	
	
	
	
	
}
