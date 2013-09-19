package ca.ubc.cs.beta.aclib.acquisitionfunctions;

import com.beust.jcommander.ParameterException;
/**
 * Enumeration that outlines the various expected improvement functions
 * @author sjr
 *
 */
public enum AcquisitionFunctions {
	/**
	 * The standard expected improvement function
	 */
	EXPONENTIAL(ExpectedExponentialImprovement.class),
	/**
	 * A simple expected improvement function
	 */
	SIMPLE(SimpleAcquisitionFunction.class),
	/**
	 * Lower Confidence Bound
	 */
	LCB(LowerConfidenceBound.class),
	
	/**
	 * Standard EI Improvement Function
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
	
	Class<? extends AcquisitionFunction> c;
	
	AcquisitionFunctions(Class<? extends AcquisitionFunction> c)
	{
		this.c=c;
	}
	
	AcquisitionFunctions()
	{
		this.c = null;
	}
	
	public AcquisitionFunction getFunction()
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
