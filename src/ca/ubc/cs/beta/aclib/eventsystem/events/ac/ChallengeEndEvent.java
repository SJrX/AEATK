package ca.ubc.cs.beta.aclib.eventsystem.events.ac;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.eventsystem.events.AbstractTimeEvent;
import ca.ubc.cs.beta.aclib.eventsystem.events.AutomaticConfiguratorEvent;
import ca.ubc.cs.beta.aclib.termination.TerminationCondition;

public class ChallengeEndEvent extends AbstractTimeEvent {

	
	private final ParamConfiguration challenger;
	private boolean newIncumbent; 
	
	public ChallengeEndEvent( TerminationCondition cond, ParamConfiguration challenger, boolean newIncumbent) {

		super(cond);
		this.challenger = challenger;
		this.newIncumbent = newIncumbent;
	}
	
	public ChallengeEndEvent(TerminationCondition cond, ParamConfiguration challenger) 
	{
		super(cond);
		this.challenger = challenger;
	}

	public ParamConfiguration getChallenger()
	{
		return challenger;
	}
	
	public boolean newIncumbent()
	{
		return newIncumbent;
	}
	

	
	
	
}
