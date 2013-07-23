package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.analytic;

import java.util.List;

public enum AnalyticFunctions {
	ADD("xN = 0, for all N"),
	CAMELBACK( "(x0,x1) = (-0.0898, 0.7126) & (0,0898, -0.7126), xN , N > 2 are all ignored");
	
	private String minima;
	private AnalyticFunctions(String minima)
	{
		this.minima = minima;
	}
	
	public double evaluate(List<Double> xVals)
	{
		double[] myXVals = new double[xVals.size()];
		for(int i=0; i < xVals.size(); i++)
		{
			myXVals[i] = xVals.get(i);
		}
		return evaluate(myXVals);
		
	}
	public double evaluate(double[] xVals)
	{
		switch(this){
		case CAMELBACK:
			{
				if(xVals.length < 2)
				{
					throw new IllegalArgumentException(this +" function requires at least two parameters, x0, x1");
				}
				double x = xVals[0];
				double y = xVals[1];
				return (4-2.1*Math.pow(x,2)+(Math.pow(x,4)/3))*Math.pow(x,2)+x*y+(-4+4*Math.pow(y,2))*Math.pow(y,2)+5;
			}
		case ADD:
			double sum = 0;
			for(double d : xVals)
			{
				sum += d;
			}
			
			return sum;
		default:
			throw new IllegalStateException(this+" not implemented currently");
		}
	}
	
	public String getMinima() {
		return minima;
	}
	
	
	
}
