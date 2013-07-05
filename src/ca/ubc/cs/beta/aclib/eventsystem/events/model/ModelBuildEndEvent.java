package ca.ubc.cs.beta.aclib.eventsystem.events.model;

import ca.ubc.cs.beta.aclib.eventsystem.events.AbstractTimeEvent;
import ca.ubc.cs.beta.aclib.termination.TerminationCondition;

public class ModelBuildEndEvent extends AbstractTimeEvent
{

	public ModelBuildEndEvent(TerminationCondition cond) {
		super(cond);
	}
	

}
