package ca.ubc.cs.beta.aclib.eventsystem.events.ac;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.eventsystem.ConfigurationTimeLimits;
import ca.ubc.cs.beta.aclib.eventsystem.events.AbstractTimeEvent;

public class IncumbentChangeEvent extends AbstractTimeEvent{


	
	private final double empericalPerformance;
	private final ParamConfiguration incumbent;
	private final int runCount;

	public IncumbentChangeEvent( ConfigurationTimeLimits limits, double empericalPerformance , ParamConfiguration incumbent, int runCount) {
		super( limits);
		
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
