package ca.ubc.cs.beta.aclib.eventsystem.events.ac;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.eventsystem.events.AbstractTimeEvent;

public class IncumbentChangeEvent extends AbstractTimeEvent{


	
	private final double empiricalPerformance;
	private final ParamConfiguration incumbent;
	private final int runCount;

	public IncumbentChangeEvent( double empiricalPerformance , ParamConfiguration incumbent, int runCount) {
		this.empiricalPerformance = empiricalPerformance;
		this.incumbent = incumbent;
		this.runCount = runCount;
		
	}


	public double getEmpiricalPerformance() {
		return empiricalPerformance;
	}

	public ParamConfiguration getIncumbent() {
		return incumbent;
	}


	public int getRunCount() {
		return runCount;
	}
	
	

}
