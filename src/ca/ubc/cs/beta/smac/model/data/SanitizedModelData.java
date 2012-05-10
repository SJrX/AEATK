package ca.ubc.cs.beta.smac.model.data;

/**
 * Methods here that are deprecated basically need to be rethought out in light of the new Decorator approach to sanitizing
 * the model data. (e.g. The clients of this, shouldn't care whether we are PCAing or not they should just get the new features, same 
 * with transformation of the columns, etc...)
 * @author seramage
 *
 */
public interface SanitizedModelData {

	public static final double MINIMUM_RESPONSE_VALUE = 0.005;
	
	
	@Deprecated
	public double[][] getPrePCAInstanceFeatures();

	@Deprecated
	public double[][] getPCAVectors();

	@Deprecated
	public double[] getPCACoefficients();

	@Deprecated
	public int[] getDataRichIndexes();

	@Deprecated
	public double[] getMeans();

	@Deprecated
	public double[] getStdDev();

	@Deprecated
	public double[][] getPCAFeatures();

	/**
	 * Return an array containing all the parameter configurations in array format
	 * @return
	 */
	public double[][] getConfigs();
	
	
	/**
	 * Returns the response values (transformed if necessary)
	 * @return
	 */
	public double[] getResponseValues();

	public int[] getCategoricalSize();

	public int[][] getCondParents();

	public int[][][] getCondParentVals();
	
}
