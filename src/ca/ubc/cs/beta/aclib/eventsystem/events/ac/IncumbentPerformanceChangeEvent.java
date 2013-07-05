package ca.ubc.cs.beta.aclib.eventsystem.events.ac;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.eventsystem.events.AbstractTimeEvent;
import ca.ubc.cs.beta.aclib.eventsystem.events.AutomaticConfiguratorEvent;
import ca.ubc.cs.beta.aclib.termination.TerminationCondition;

public class IncumbentPerformanceChangeEvent extends AbstractTimeEvent {

	private final double acTime;
	private final ParamConfiguration incumbent;

	private final double empiricalPerformance;

public IncumbentPerformanceChangeEvent(double cpuTime, double walltime, double empiricalPerformance, ParamConfiguration incumbent, double acTime ) {
		
		super(cpuTime, walltime);
		this.empiricalPerformance = empiricalPerformance;
		
		this.incumbent = incumbent;
		this.acTime = acTime;
	}

	public IncumbentPerformanceChangeEvent(TerminationCondition termCond, double empiricalPerformance, ParamConfiguration incumbent, double acTime ) {
		
		super(termCond);
		this.empiricalPerformance = empiricalPerformance;
		
		this.incumbent = incumbent;
		this.acTime = acTime;
	}

	public double getAutomaticConfiguratorCPUTime() {
		return acTime;
	}

	public ParamConfiguration getIncumbent() {
		return incumbent;
	}
	
	public double getEmpiricalPerformance() {
		return empiricalPerformance;
	}

	
	
	

}
