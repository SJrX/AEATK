package ca.ubc.cs.beta.smac.model.data;

import ca.ubc.cs.beta.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.smac.PCA;
import ca.ubc.cs.beta.smac.helper.ArrayMathOps;

public class RawSanitizedModelData implements SanitizedModelData {

	
	
	private final ParamConfigurationSpace configSpace;
	private final double[][] configs;
	private final double[] responseValues;
	private final double[][] prePCAInstanceFeatures;
	private double[] means;
	private double[] stdDev;
	private double[] pcaCoeff;
	private double[][] pcaVec;
	//private double[][] pcaFeatures;

	private final boolean logModel;
	public RawSanitizedModelData(double[][] instanceFeatures, double[][] paramValues, double[] responseValues, int[] usedInstances, boolean logModel)
	{
		this(instanceFeatures, paramValues, responseValues, usedInstances, logModel, null);
	}
	public RawSanitizedModelData(double[][] instanceFeatures, double[][] paramValues, double[] responseValues, int[] usedInstancesIdxs, boolean logModel, ParamConfigurationSpace configSpace)
	{
		this.configSpace = configSpace;
		this.configs = paramValues;
		this.responseValues = responseValues;
		this.logModel = logModel;
		this.prePCAInstanceFeatures = ArrayMathOps.copy(instanceFeatures);
		
		
		PCA pca = new PCA();
		double[][] usedInstanceFeatures = new double[usedInstancesIdxs.length][];
		
		for(int i=0; i < usedInstanceFeatures.length; i++)
		{
			usedInstanceFeatures[i] = instanceFeatures[usedInstancesIdxs[i]];
		}
		int[] constFeatures = pca.constantColumnsWithMissingValues(usedInstanceFeatures);
		instanceFeatures = pca.removeColumns(instanceFeatures, constFeatures);
		
		
		double[][] instanceFeaturesT = pca.transpose(instanceFeatures);
		
		
		//double[] firstStdDev = pca.getRowStdDev(instanceFeaturesT);
		//double[][] pcaedFeatures =pca.getPCA(instanceFeatures, numPCA); 
		
		
		if(logModel)
		{
			pca.max(responseValues, SanitizedModelData.MINIMUM_RESPONSE_VALUE);
			pca.log10(responseValues);
			
			
		}
		

		means = new double[0];
		stdDev = new double[0];
		pcaCoeff = new double[0];
		pcaVec = new double[0][];
		//pcaFeatures = new double[instanceFeatures.length][1];

	}
	
	@Override
	public double[][] getPrePCAInstanceFeatures() {
		return this.prePCAInstanceFeatures;
	}

	@Override
	public double[][] getPCAVectors() {
		return this.pcaVec;
	}

	@Override
	public double[] getPCACoefficients() {
		// TODO Auto-generated method stub
		return this.pcaCoeff;
		
	}

	@Override
	public int[] getDataRichIndexes() {
		// TODO Auto-generated method stub
		return new int[0];
	}

	@Override
	public double[] getMeans() {
		// TODO Auto-generated method stub
		return means;
	}

	@Override
	public double[] getStdDev() {
		// TODO Auto-generated method stub
		return stdDev;
	}

	@Override
	public double[][] getPCAFeatures() {
		return this.prePCAInstanceFeatures;
	}

	@Override
	public double[][] getConfigs() {

		return this.configs;
	}

	@Override
	public double[] getResponseValues() {

		return this.responseValues;
	}

	public int[] getCategoricalSize()
	{
		return configSpace.getCategoricalSize();
	}
	public int[][] getCondParents()
	{
		return configSpace.getCondParentsArray();
	}

	public int[][][] getCondParentVals()
	{
		return configSpace.getCondParentValsArray();
	}

	@Override
	public double transformResponseValue(double d) {
		if(logModel)
		{
			
			return Math.log10(Math.max(d, SanitizedModelData.MINIMUM_RESPONSE_VALUE));
		} else
		{
			return d;
		}
	}
	
}
