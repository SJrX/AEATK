package ca.ubc.cs.beta.aeatk.initialization.doubleracing;

public class RoundSetting {
    public RoundSetting(int numThetas, int numInstances, double capTime) {
		super();
		this.numThetas = numThetas;
		this.numInstances = numInstances;
		this.capTime = capTime;
	}
	public int numThetas;
    public int numInstances;
    public double capTime;
}
