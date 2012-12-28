package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;

public class IncumbentChangeEvent extends AbstractTimeEvent{


	
	private final double empericalPerformance;
	private final ParamConfiguration incumbent;

	public IncumbentChangeEvent(UUID uuid, ConfigurationTimeLimits limits, double empericalPerformance , ParamConfiguration incumbent) {
		super(uuid, limits);
		
		this.empericalPerformance = empericalPerformance;
		this.incumbent = incumbent;
		
	}


	public double getEmpericalPerformance() {
		return empericalPerformance;
	}

	public ParamConfiguration getIncumbent() {
		return incumbent;
	}
	
	

}
