package ca.ubc.cs.beta.aclib.expectedimprovement;


import static ca.ubc.cs.beta.aclib.misc.math.ArrayMathOps.*;

public class LowerConfidenceBound implements ExpectedImprovementFunction {
	
	
	public LowerConfidenceBound()
	{
		
	}
	
	@Override
	public double[] computeNegativeExpectedImprovement(double k,
			double[] predmean, double[] predvar) {
			if(predmean.length != predvar.length)
			{
				throw new IllegalArgumentException("Expected predmean and predvar to have the same length");
			}
			return	add( predmean,	times(-k,sqrt(predvar)));
	}


}
