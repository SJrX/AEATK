package ca.ubc.cs.beta.aclib.exceptions;

import com.beust.jcommander.ParameterException;

public class FeatureNotFoundException extends ParameterException {

	public FeatureNotFoundException(String string) {
		super(string);
	}

}
