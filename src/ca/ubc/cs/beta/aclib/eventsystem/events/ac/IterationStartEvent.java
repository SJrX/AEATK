package ca.ubc.cs.beta.aclib.eventsystem.events.ac;

import ca.ubc.cs.beta.aclib.eventsystem.events.AbstractTimeEvent;
import ca.ubc.cs.beta.aclib.termination.TerminationCondition;

public class IterationStartEvent extends AbstractTimeEvent {

	private final int iteration;

	public IterationStartEvent(TerminationCondition cond, int iteration) {
		super(cond);
		this.iteration = iteration;
	}

	public int getIteration()
	{
		return iteration;
	}
}
