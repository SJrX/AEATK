package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

public class ModelBuildStartEvent extends AbstractTimeEvent
{

	public ModelBuildStartEvent(UUID uuid,
			ConfigurationTimeLimits configurationTimeLimit) {
		super(uuid, configurationTimeLimit);
	}
	

}
