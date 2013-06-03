package ca.ubc.cs.beta.aclib.eventsystem.exceptions;

public class EventManagerShutdownException extends IllegalStateException {

	public EventManagerShutdownException()
	{
		super("Event Manager has previously been shutdown and you cannot fire new events, or register new handlers to it");
	}
}
