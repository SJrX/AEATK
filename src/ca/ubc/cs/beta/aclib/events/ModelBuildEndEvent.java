package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

public class ModelBuildEndEvent extends AbstractTimeEvent
{

	public ModelBuildEndEvent(UUID uuid,
			ConfigurationTimeLimits configurationTimeLimit) {
		super(uuid, configurationTimeLimit);
	}
	

}
