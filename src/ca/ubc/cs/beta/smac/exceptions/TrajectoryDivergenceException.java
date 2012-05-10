package ca.ubc.cs.beta.smac.exceptions;

public class TrajectoryDivergenceException extends SMACException {

	private static final long serialVersionUID = -104669424346723440L;
	
	
	public TrajectoryDivergenceException(int expectedHashCode, int computedHashCode, int runNumber)
	{
		super("Expected Hash Code:" + expectedHashCode + " Computed Hash Code:" + computedHashCode + " Run Number: " + runNumber);
	}


	public TrajectoryDivergenceException(String string) {
		super(string);
	}
	
}
