package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;

public class IncumbentPerformanceChangeEvent extends AutomaticConfiguratorEvent {

	private final double acTime;
	private final ParamConfiguration incumbent;
	private final long wallTime;
	private final double empiricalPerformance;
	private final double cpuTime;

	public IncumbentPerformanceChangeEvent(UUID uuid, double tunerTime, double empiricalPerformance, long wallTime, ParamConfiguration incumbent, double acTime ) {
		super(uuid);
		this.cpuTime = tunerTime;
		this.empiricalPerformance = empiricalPerformance;
		this.wallTime = wallTime;
		this.incumbent = incumbent;
		this.acTime = acTime;
	}

	public double getAutomaticConfiguratorCPUTime() {
		return acTime;
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
	
	public double getEmpiricalPerformance() {
		return empiricalPerformance;
	}

	
	
	

}
