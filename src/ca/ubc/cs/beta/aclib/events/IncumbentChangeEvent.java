package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;

public class IncumbentChangeEvent extends AbstractTimeEvent{


	
	private final double empericalPerformance;
	private final ParamConfiguration incumbent;
	private final int runCount;

	public IncumbentChangeEvent(UUID uuid, ConfigurationTimeLimits limits, double empericalPerformance , ParamConfiguration incumbent, int runCount) {
		super(uuid, limits);
		
		this.empericalPerformance = empericalPerformance;
		this.incumbent = incumbent;
		this.runCount = runCount;
		
	}


	public double getEmpericalPerformance() {
		return empericalPerformance;
	}

	public ParamConfiguration getIncumbent() {
		return incumbent;
	}


	public int getRunCount() {
		return runCount;
	}
	
	

}
