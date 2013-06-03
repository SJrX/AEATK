package ca.ubc.cs.beta.aclib.eventsystem.events.ac;

import java.util.UUID;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.eventsystem.events.AutomaticConfiguratorEvent;

public class ChallengeStartEvent extends AutomaticConfiguratorEvent {

	
	private final ParamConfiguration challenger; 
	
	public ChallengeStartEvent( ParamConfiguration challenger) {

		this.challenger = challenger;
	}

	public ParamConfiguration getChallenger()
	{
		return challenger;
	}
	
	
}
