package ei;

public interface ExpectedImprovementFunction {

	/**
	 * Computes the expected improvment for each predmean, predvar 
	 * @param f_min_samples
	 * @param predmean
	 * @param predvar
	 * @return
	 */
	public double[] computeNegativeExpectedImprovement(double f_min_samples, double[] predmean, double[] predvar);
	
}
