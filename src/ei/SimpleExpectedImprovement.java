package ei;
import static ca.ubc.cs.beta.smac.helper.ArrayMathOps.*;
public class SimpleExpectedImprovement implements ExpectedImprovementFunction {

	
	@Override
	public double[] computeNegativeExpectedImprovement(double f_min_samples,
			double[] predmean, double[] predvar) {

		return times(-1,exp(times(-1,predmean)));
	}

}
