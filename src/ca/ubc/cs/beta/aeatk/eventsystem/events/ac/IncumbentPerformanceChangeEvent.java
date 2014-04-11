package ca.ubc.cs.beta.aeatk.eventsystem.events.ac;

import ca.ubc.cs.beta.aeatk.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aeatk.eventsystem.events.AbstractTimeEvent;
import ca.ubc.cs.beta.aeatk.misc.cputime.CPUTime;
import ca.ubc.cs.beta.aeatk.termination.TerminationCondition;

public class IncumbentPerformanceChangeEvent extends AbstractTimeEvent {

	private final double acTime;
	private final ParamConfiguration incumbent;

	private final double empiricalPerformance;

	private final ParamConfiguration oldIncumbent;
	private final long runCount;
	
	public IncumbentPerformanceChangeEvent(double tunerTime, double walltime, double empiricalPerformance, ParamConfiguration incumbent , long runCount, ParamConfiguration oldIncumbent,CPUTime cpuTime) 
	{
		super(tunerTime, walltime);
		this.empiricalPerformance = empiricalPerformance;
		this.incumbent = incumbent;
		this.acTime = cpuTime.getCPUTime();
		this.oldIncumbent = oldIncumbent;
		this.runCount = runCount;
		
	}

	public IncumbentPerformanceChangeEvent(TerminationCondition termCond, double empiricalPerformance, ParamConfiguration incumbent, long runCount, ParamConfiguration oldIncumbent, CPUTime cpuTime ) 
	{
		super(termCond);
		this.empiricalPerformance = empiricalPerformance;
		this.incumbent = incumbent;
		this.acTime = cpuTime.getCPUTime();
		this.oldIncumbent = oldIncumbent;
		this.runCount = runCount;
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

	public boolean incumbentChanged()
	{
		return !incumbent.equals(oldIncumbent);
	}

	public long getIncumbentRunCount() {
		return runCount;
	}
	
	
	

}
