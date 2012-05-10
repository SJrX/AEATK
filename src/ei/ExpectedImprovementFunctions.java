package ei;

import com.beust.jcommander.ParameterException;

public enum ExpectedImprovementFunctions {
	EXPONENTIAL(ExpectedExponentialImprovement.class),
	SIMPLE(SimpleExpectedImprovement.class),
	SPO,
	EI,
	EIh;
	
	Class<? extends ExpectedImprovementFunction> c;
	
	ExpectedImprovementFunctions(Class<? extends ExpectedImprovementFunction> c)
	{
		this.c=c;
	}
	
	ExpectedImprovementFunctions()
	{
		this.c = null;
	}
	
	public ExpectedImprovementFunction getFunction()
	{
		if(c == null)
		{
			throw new IllegalArgumentException("This Expected Improvement Function is not implemented at the moment: " + this.toString());
		} else
		{
			Class<?>[] args = {};
			try {
				return c.getConstructor(args).newInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new ParameterException(e);
			}
		}
	}
}
