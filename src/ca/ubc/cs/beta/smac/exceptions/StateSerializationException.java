package ca.ubc.cs.beta.smac.exceptions;

public class StateSerializationException extends SMACException {

	
	private static final long serialVersionUID = 4394135089834489593L;

	public StateSerializationException(Exception e)
	{
		super(e);
	}
	
	public StateSerializationException(String s,Exception e)
	{
		super(s,e);
	}

	public StateSerializationException(String s) {
		super(s);
	}
	
	
}
