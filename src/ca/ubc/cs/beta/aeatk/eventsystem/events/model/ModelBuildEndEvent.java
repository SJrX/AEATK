package ca.ubc.cs.beta.aeatk.eventsystem.events.model;

import ca.ubc.cs.beta.aeatk.eventsystem.events.AbstractTimeEvent;
import ca.ubc.cs.beta.aeatk.termination.TerminationCondition;

public class ModelBuildEndEvent extends AbstractTimeEvent
{
	private final Object model; 

	public ModelBuildEndEvent(TerminationCondition cond) {
		super(cond);
		model = null;
	}

	public ModelBuildEndEvent(TerminationCondition cond, Object model) {
		super(cond);
		this.model = model;
	}

	public Object getModelIfAvailable()
	{
		return model;
	}
	
	
}
