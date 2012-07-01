package ca.ubc.cs.beta.aclib.expectedimprovement;

public interface ExpectedImprovementFunction {

	/**
	 * Computes the expected improvement for each predmean, predvar 
	 * @param f_min_samples		the minimum emperical cost found so far
	 * @param predmean			predicted mean of the samples
	 * @param predvar			predicted variance of the samples
	 * @return					array of values which correspond to the expected improvement for the corresponding entries in predmean and predvar
	 */
	public double[] computeNegativeExpectedImprovement(double f_min_samples, double[] predmean, double[] predvar);
	
}
