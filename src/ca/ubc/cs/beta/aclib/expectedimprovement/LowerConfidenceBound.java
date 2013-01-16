package ca.ubc.cs.beta.aclib.expectedimprovement;


import static ca.ubc.cs.beta.aclib.misc.math.ArrayMathOps.*;

public class LowerConfidenceBound implements ExpectedImprovementFunction {
	
	
	public LowerConfidenceBound()
	{
		
	}
	
	@Override
	public double[] computeNegativeExpectedImprovement(double k,
			double[] predmean, double[] predvar) {
			return add(times(-1,predmean),	times(k,sqrt(predvar)));
		
	}


}
