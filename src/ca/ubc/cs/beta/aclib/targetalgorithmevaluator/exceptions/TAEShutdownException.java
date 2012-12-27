package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.exceptions;

/**
 * Occurs when the TAE has been shutdown
 * @author sjr
 *
 */
public class TAEShutdownException extends RuntimeException {

	
	private static final long serialVersionUID = 6812069375285515103L;

	public TAEShutdownException(Exception e) {
		super(e);
	}

}
