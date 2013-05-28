package ca.ubc.cs.beta.aclib.expectedimprovement;


import static ca.ubc.cs.beta.aclib.misc.math.ArrayMathOps.*;

public class LowerConfidenceBound implements ExpectedImprovementFunction {
	
	
	public LowerConfidenceBound()
	{
		
	}
	
	@Override
	public double[] computeNegativeExpectedImprovement(double k,
			double[] predmean, double[] predvar) {
			return	add( predmean,	times(-k,sqrt(predvar)));
								
						
		
	}


}
