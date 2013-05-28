package ca.ubc.cs.beta.aclib.exceptions;

public class FrankOwesSteveABeerException extends DeveloperMadeABooBooException {

	public FrankOwesSteveABeerException(Exception e) {
		super(e);
	}

	public FrankOwesSteveABeerException(String s) {
		super(s);
	}

	public String getMessage()
	{
		return "Please e-mail the developers as one of them said if this exception was ever seen by anyone, they would by the other one a beer " + super.getMessage();
	}
	
	
}
