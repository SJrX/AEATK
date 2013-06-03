package ca.ubc.cs.beta.aclib.eventsystem;

@Deprecated
public class ConfigurationTimeLimits {

	private final double tunerTime;
	private final double wallClockTime;
	private final int numberOfIterations;
	
	public ConfigurationTimeLimits(double tunerTime, double wallclockTime, int numberOfIterations)
	{
		this.tunerTime = tunerTime;
		this.wallClockTime = wallclockTime;
		this.numberOfIterations = numberOfIterations;

	}

	public double getTunerTime() {
		return tunerTime;
	}

	public double getWallClockTime() {
		return wallClockTime;
	}

	public int getNumberOfIterations() {
		return numberOfIterations;
	}
	
	
}
