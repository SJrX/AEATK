package ca.ubc.cs.beta.aclib.eventsystem.events.model;

import java.util.UUID;

import ca.ubc.cs.beta.aclib.eventsystem.ConfigurationTimeLimits;
import ca.ubc.cs.beta.aclib.eventsystem.events.AbstractTimeEvent;

public class ModelBuildEndEvent extends AbstractTimeEvent
{

	public ModelBuildEndEvent(ConfigurationTimeLimits configurationTimeLimit) {
		super( configurationTimeLimit);
	}
	

}
