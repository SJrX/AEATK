package ca.ubc.cs.beta.aclib.exceptions;

public class IllegalWrapperOutputException extends IllegalArgumentException {

	/**
	 * Default constructor
	 * @param error 		 error with the result line
	 * @param resultLine	 result line text
	 */
	public IllegalWrapperOutputException(String error, String resultLine)
	{
		super("Illegal Wrapper Output Detected: " + error + " on result line: " + resultLine + " please consult the manual for more information");
	}
}
