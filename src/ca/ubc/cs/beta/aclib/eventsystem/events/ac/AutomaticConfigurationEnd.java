package ca.ubc.cs.beta.aclib.eventsystem.events.ac;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.eventsystem.events.AbstractTimeEvent;

public class AutomaticConfigurationEnd extends AbstractTimeEvent {

	private final ParamConfiguration incumbent;
	private final double empiricalPerformance;
	private final long wallTime;
	private final double cpuTime;

	public AutomaticConfigurationEnd(ParamConfiguration incumbent, double empiricalPerformance, long wallClockTime, double tunerTime) {
		
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
