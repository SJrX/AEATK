package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;

public class AutomaticConfigurationEnd extends AbstractTimeEvent {

	private final ParamConfiguration incumbent;
	private final double empiricalPerformance;
	private final long wallTime;
	private final double cpuTime;

	public AutomaticConfigurationEnd(UUID uuid, ParamConfiguration incumbent, ConfigurationTimeLimits limits, double empiricalPerformance, long wallClockTime, double tunerTime) {
		super(uuid, limits);
		this.incumbent = incumbent;
		this.empiricalPerformance = empiricalPerformance;
		this.wallTime = wallClockTime;
		this.cpuTime = tunerTime;
		
	}

	public ParamConfiguration getIncumbent() {
		return incumbent;
	}

	public long getWallTime() {
		return wallTime;
	}

	public double getTunerTime() {
		return cpuTime;
	}
	
	public double getEmpiricalPerformance()
	{
		return empiricalPerformance;
	}

}