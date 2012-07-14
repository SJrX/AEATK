package ca.ubc.cs.beta.aclib.misc.jcommander.validator;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class ZeroOneHalfOpenRightDouble implements IParameterValidator {
	
	public void validate(String name, String value) throws ParameterException 
	{
		    double n = Double.parseDouble(value);
		    
    		if(n < 0 || n >= 1)
    		{
    			throw new ParameterException("Parameter " + name + " must have a value in [0,1) (A value from 0 up to but not including 1)");
    		}
  
		    
		  
	}

}