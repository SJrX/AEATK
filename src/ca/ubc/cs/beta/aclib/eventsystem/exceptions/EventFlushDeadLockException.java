package ca.ubc.cs.beta.aclib.eventsystem.exceptions;

public class EventFlushDeadLockException extends IllegalStateException {



	public EventFlushDeadLockException() {
		super("Deadlock detected while processing events (chances are you called flush() as a side effect of processing an event): ");
		
	}


}
