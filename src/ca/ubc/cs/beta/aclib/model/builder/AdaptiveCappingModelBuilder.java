package ca.ubc.cs.beta.aclib.model.builder;

import java.util.ArrayList;
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
	 * @param mds 						sanatized model data
	 * @param rfOptions					random forest configuration options
	 * @param runHistory				runhistory object containing the runs to use
	 * @param rand						random object to use during construction
	 * @param imputationIterations		the number of imputation iterations
	 * @param cutoffTime				the max algorithm run time
	 * @param penaltyFactory			the penalty factor for runs which timed out
	 */
	public AdaptiveCappingModelBuilder(SanitizedModelData mds, RandomForestOptions rfOptions, RunHistory runHistory, Random rand, int imputationIterations, double cutoffTime, double penaltyFactory)
	{
		
		
		double maxValue = mds.transformResponseValue(cutoffTime*penaltyFactory);
		
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
		int[][] theta_inst_idxs = runHistory.getParameterConfigurationInstancesRanByIndex();
		double[] responseValues = mds.getResponseValues();
		boolean[] censoredValues = runHistory.getCensoredFlagForRuns();
		ArrayList<int[]> censoredThetaInst = new ArrayList<int[]>(responseValues.length);
		ArrayList<int[]> nonCensoredThetaInst = new ArrayList<int[]>(responseValues.length);
		ArrayList<Double> nonCensoredResponses = new ArrayList<Double>(responseValues.length);
		
		int censoredCount = 0;
		
		for(int i=0; i < responseValues.length; i++)
		{
			if(!censoredValues[i])
			{
				nonCensoredThetaInst.add(theta_inst_idxs[i]);
				nonCensoredResponses.add(responseValues[i]);
			} else
			{
				censoredThetaInst.add(theta_inst_idxs[i]);
				censoredCount++;
			}
		}
		
		log.info("Building Random Forest with {} censored runs out of {} total ", censoredCount, censoredValues.length);
		int[][] non_cens_theta_inst_idxs = nonCensoredThetaInst.toArray(new int[0][]);
		double[] non_cens_responses = convertToPrimitive(nonCensoredResponses.toArray(new Double[0]));
		
				
		
		log.info("Building random forest with non censored data");
		RandomForest rf = buildRandomForest(mds,rfOptions,non_cens_theta_inst_idxs, non_cens_responses, false);
		
		if(rfOptions.fullTreeBootstrap)
		{
			throw new IllegalStateException("Cannot build random forest with Adaptive Capping on Full Tree Bootstap");
			/**
			 * This should be an easy fix, we just need to sample correctly.
			 */
			
		}
		/**
		 * While imputed values change more than a limit, continue.
		 */
		double last_mean = Double.NEGATIVE_INFINITY;
		
		for(int i=0; i < imputationIterations; i++)
		{
			double imputedValues_sum = 0;
			double imputedValues_count = 0;
			// Do bootstrap sampling for data for each tree.
			int numTrees = rfOptions.numTrees;
			int sampleSize =responseValues.length;
			
		    int[][] dataIdxs = new int[numTrees][sampleSize];
		    theta_inst_idxs = runHistory.getParameterConfigurationInstancesRanByIndex();
		    
		    double[][] imputedResponses = new double[numTrees][sampleSize];
		       for (int j = 0; j < numTrees; j++) {
		    	   
		    	   ArrayList<double[]> predictionsNeeded = new ArrayList<double[]>(sampleSize);
		           for (int k = 0; k < sampleSize; k++) {
		        	   int theta_inst_indx_to_use =  rand.nextInt(sampleSize);
		               dataIdxs[j][k] = theta_inst_indx_to_use; 
		               if(censoredValues[k])
		               {
		            	   imputedResponses[j][k] = Double.NaN;
		            	   
		            	   double[] configArray = mds.getConfigs()[theta_inst_idxs[theta_inst_indx_to_use][0]-1];
		            	   double[] featureArray = mds.getPCAFeatures()[theta_inst_idxs[theta_inst_indx_to_use][1]-1];
		            	   int Xlength = configArray.length + featureArray.length;
		            	   double[] predictionX = new double[Xlength];		            	   
		            	   for(int m=0; m < configArray.length; m++)
		            	   {
		            		   predictionX[m] = configArray[m];
		            	   }
		            	   
		            	   for(int m=0; m < featureArray.length; m++)
		            	   {
		            		   predictionX[m+configArray.length] = featureArray[m];
		            		   
		            	   }
		            	   
		            	   
		            	   predictionsNeeded.add(predictionX);
		               } else
		               {
		            	   imputedResponses[j][k] = responseValues[k];
		               }
		               
		           }
		           
		           double[][] predictions = RandomForest.apply(rf, predictionsNeeded.toArray(new double[0][]));
		           
		           int nextPrediction = 0;
		           for(int k = 0; k < sampleSize; k++)
		           {
		        	   if(Double.isNaN(imputedResponses[j][k]))
		        	   {
		        		   double[] prediction = predictions[nextPrediction++];
		        		   TruncatedNormalDistribution tNorm = new TruncatedNormalDistribution(prediction[0], prediction[1],responseValues[k],rand);
		        		   imputedResponses[j][k] = Math.min(tNorm.sample(),maxValue);
		        		   imputedValues_sum += imputedResponses[j][k];
		        		   imputedValues_count++;
		        		   if(Double.isInfinite(imputedResponses[j][k]))
		        		   {
		        			   System.out.println("Hello");
		        		   }
		        	   }
		           }
		       }
		       log.info("Building random forest with imputed values iteration {}", i);
		       rf = buildImputedRandomForest(mds,rfOptions,theta_inst_idxs, dataIdxs, imputedResponses, false);
		       double meanIncrease = (imputedValues_sum / imputedValues_count) - last_mean;
		       last_mean = (imputedValues_sum / imputedValues_count);
		       if(i >= 2 && meanIncrease < Math.pow(10,-10))
		       {
		    	   log.info("Means of imputed values stopped increasing in imputation iteration {} (increase {})",i,meanIncrease);
		    	   break;
		       } else
		       {
		    	   log.info("Mean increase in imputed values in imputation iteration {}:{}", i, meanIncrease);
		       }
		}
		
		
		
		
		
		
		forest = rf;
		
		preprocessedForest = RandomForest.preprocessForest(forest, mds.getPCAFeatures());
		
		
	
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
	private double[] convertToPrimitive(Double[] arr)
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
		
		
		
		for(int i=0; i < theta_inst_idxs.length; i++)
		{
			theta_inst_idxs[i][0]--;
			theta_inst_idxs[i][1]--;
		}
		
		RegtreeBuildParams buildParams = SMACRandomForestHelper.getRandomForestBuildParams(rfOptions, features[0].length, categoricalSize, condParents, condParentVals);
		
		log.debug("Building Random Forest with Parameters: {}", buildParams);
		RandomForest forest, preprocessedForest;
		
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
		

		
		if(preprocessed)
		{
			preprocessedForest = RandomForest.preprocessForest(forest, features);
			RandomForest.save(preprocessedForest);

		} else
		{
			preprocessedForest = null;
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
		

		
		for(int i=0; i < theta_inst_idxs.length; i++)
		{
			theta_inst_idxs[i][0]--;
			theta_inst_idxs[i][1]--;
		}
		RegtreeBuildParams buildParams = SMACRandomForestHelper.getRandomForestBuildParams(rfOptions, features[0].length, categoricalSize, condParents, condParentVals);
		
		log.debug("Building Random Forest with Parameters: {}", buildParams);
		RandomForest forest, preprocessedForest;
		
		        
		        
		   forest = RandomForest.learnModelImputedValues(numTrees, configs, features, theta_inst_idxs, responseValues, dataIdxs, buildParams);
		

		
		if(preprocessed)
		{
			preprocessedForest = RandomForest.preprocessForest(forest, features);
			RandomForest.save(preprocessedForest);

		} else
		{
			preprocessedForest = null;
		}
		
		return forest;
	}
	
	
	
	
	
}
