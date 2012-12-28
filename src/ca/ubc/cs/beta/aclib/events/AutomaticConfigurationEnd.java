package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;

public class AutomaticConfigurationEnd extends AbstractTimeEvent {

	private final ParamConfiguration incumbent;
	private final double empericalPerformance;

	public AutomaticConfigurationEnd(UUID uuid, ParamConfiguration incumbent, ConfigurationTimeLimits limits, double empericalPerformance) {
		super(uuid, limits);
		this.incumbent = incumbent;
		this.empericalPerformance = empericalPerformance;
		
		
	}

	public ParamConfiguration getIncumbent() {
		return incumbent;
	}

		
	

}
