package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;

public class ChallengeStartEvent extends AutomaticConfiguratorEvent {

	
	private final ParamConfiguration challenger; 
	
	public ChallengeStartEvent(UUID uuid, ParamConfiguration challenger) {
		super(uuid);
		this.challenger = challenger;
	}

	public ParamConfiguration getChallenger()
	{
		return challenger;
	}
	
	
}
