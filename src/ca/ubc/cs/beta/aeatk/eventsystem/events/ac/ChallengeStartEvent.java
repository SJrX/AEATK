package ca.ubc.cs.beta.aeatk.eventsystem.events.ac;

import ca.ubc.cs.beta.aeatk.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aeatk.eventsystem.events.AbstractTimeEvent;
import ca.ubc.cs.beta.aeatk.termination.TerminationCondition;

public class ChallengeStartEvent extends AbstractTimeEvent{

	
	private final ParamConfiguration challenger;
	

	public ChallengeStartEvent(TerminationCondition cond, ParamConfiguration challenger) 
	{
		super(cond);
		
		this.challenger = challenger;
		
	}


	public ParamConfiguration getChallenger()
	{
		return challenger;
	}
	
		
}
