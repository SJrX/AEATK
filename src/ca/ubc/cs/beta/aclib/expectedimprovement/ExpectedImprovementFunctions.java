package ca.ubc.cs.beta.aclib.expectedimprovement;

import com.beust.jcommander.ParameterException;
/**
 * Enumeration that outlines the various expected improvement functions
 * @author sjr
 *
 */
public enum ExpectedImprovementFunctions {
	/**
	 * The standard expected improvement function
	 */
	EXPONENTIAL(ExpectedExponentialImprovement.class),
	/**
	 * A simple expected improvement function
	 */
	SIMPLE(SimpleExpectedImprovement.class),
	/**
	 * Lower Confidence Bound
	 */
	LCB(LowerConfidenceBound.class),
	
	
	/**
	 * EI Improvement Function (NOT IMPLEMENTED)
	 */
	
	EI(ExpectedImprovement.class);
	
	
	/**
	 * SPO Improvement Function (NOT IMPLEMENTED)
	 */
	//SPO,
	 /*
	 * EIh Improvement Function (NOT IMPLEMENTED)
	 */
	//EIh;
	
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
