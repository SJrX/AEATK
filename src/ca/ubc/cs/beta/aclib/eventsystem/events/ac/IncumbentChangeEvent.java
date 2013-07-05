package ca.ubc.cs.beta.aclib.eventsystem.events.ac;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.eventsystem.events.AbstractTimeEvent;
import ca.ubc.cs.beta.aclib.termination.TerminationCondition;

public class IncumbentChangeEvent extends AbstractTimeEvent{


	
	private final double empiricalPerformance;
	private final ParamConfiguration incumbent;
	private final int runCount;

	public IncumbentChangeEvent(TerminationCondition termCond,  double empiricalPerformance , ParamConfiguration incumbent, int runCount) {
		super(termCond);
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
