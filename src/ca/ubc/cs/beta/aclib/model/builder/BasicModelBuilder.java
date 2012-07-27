package ca.ubc.cs.beta.aclib.model.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.misc.model.SMACRandomForestHelper;
import ca.ubc.cs.beta.aclib.model.data.SanitizedModelData;
import ca.ubc.cs.beta.aclib.options.RandomForestOptions;
import ca.ubc.cs.beta.aclib.runhistory.RunHistory;
import ca.ubc.cs.beta.models.fastrf.RandomForest;
import ca.ubc.cs.beta.models.fastrf.RegtreeBuildParams;

/**
 * Formats the input arguments and begins building the actual model.
 * 
 * 
 * @author sjr
 *
 */
public class BasicModelBuilder implements ModelBuilder{

	
	protected final RandomForest forest;
	protected final RandomForest preprocessedForest;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	
	public BasicModelBuilder(SanitizedModelData smd, RandomForestOptions rfConfig, RunHistory runHistory)
	{
		
		double[][] features = smd.getPCAFeatures();
		
		
		double[][] configs = smd.getConfigs();
		double[] responseValues = smd.getResponseValues();
		int[] categoricalSize = smd.getCategoricalSize();
		int[][] condParents = smd.getCondParents();
		int[][][] condParentVals = smd.getCondParentVals();
		
		/*
		System.out.println("y = \n" + Arrays.toString(responseValues));
		System.out.println("categoricalSize = \n" + Arrays.toString(categoricalSize));
		System.out.println("parent_param_idxs = \n" + Arrays.deepToString(condParents));
		*/
		
		int numTrees = rfConfig.numTrees;
		
		/**
		 * N x 2 array of response values
		 */
		
		//TODO: put these indices through the model sanitizer
		int[][] theta_inst_idxs = runHistory.getParameterConfigurationInstancesRanByIndex();
		
		
		for(int i=0; i < theta_inst_idxs.length; i++)
		{
			theta_inst_idxs[i][0]--;
			theta_inst_idxs[i][1]--;
		}
		RegtreeBuildParams buildParams = SMACRandomForestHelper.getRandomForestBuildParams(rfConfig, features[0].length, categoricalSize, condParents, condParentVals);
		
		log.debug("Building Random Forest with Parameters: {}", buildParams);
		log.info("Building Random Forest with {} data points ", responseValues.length);
		
		if(rfConfig.fullTreeBootstrap)
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
		

		
		if(rfConfig.preprocessMarginal)
		{
			preprocessedForest = RandomForest.preprocessForest(forest, features);
			//RandomForest.save(preprocessedForest);

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
	
	
}
