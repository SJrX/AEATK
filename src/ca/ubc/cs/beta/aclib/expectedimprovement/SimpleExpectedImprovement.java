package ca.ubc.cs.beta.aclib.expectedimprovement;
import static ca.ubc.cs.beta.aclib.misc.math.ArrayMathOps.*;
/**
 * Simple expected improvement
 * (Probably look at the matlab code for the origin of this)
 * @author sjr
 *
 */
public class SimpleExpectedImprovement implements ExpectedImprovementFunction {

	
	@Override
	public double[] computeNegativeExpectedImprovement(double f_min_samples,
			double[] predmean, double[] predvar) {

		return times(-1,exp(times(-1,predmean)));
	}

}
