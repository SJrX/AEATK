package ca.ubc.cs.beta.aclib.eventsystem.events.model;

import java.util.UUID;

import ca.ubc.cs.beta.aclib.eventsystem.ConfigurationTimeLimits;
import ca.ubc.cs.beta.aclib.eventsystem.events.AbstractTimeEvent;

public class ModelBuildStartEvent extends AbstractTimeEvent
{

	public ModelBuildStartEvent(
			ConfigurationTimeLimits configurationTimeLimit) {
		super(configurationTimeLimit);
	}
	

}
