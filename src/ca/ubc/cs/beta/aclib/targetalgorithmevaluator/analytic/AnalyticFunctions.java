package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.analytic;

public enum AnalyticFunctions {
	ADD,
	CAMELBACK;
	public double evaluate(double x, double y)
	{
		switch(this){
			case CAMELBACK:
				return (4-2.1*Math.pow(x,2)+(Math.pow(x,4)/3))*Math.pow(x,2)+x*y+(-4+4*Math.pow(y,2))*Math.pow(y,2)+5;
			case ADD:
				return x+y;
			default:
				throw new IllegalStateException(this+" not implemented currently");
		}
	}
	
}
