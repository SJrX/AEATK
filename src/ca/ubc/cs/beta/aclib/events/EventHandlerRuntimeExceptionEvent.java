package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

/**
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class EventHandlerRuntimeExceptionEvent extends AutomaticConfiguratorEvent{

	
	private final RuntimeException e;
	
	public EventHandlerRuntimeExceptionEvent(UUID uuid, RuntimeException e)
	{
		super(uuid);
		this.e = e;
	}
	
	public RuntimeException getRuntimeException()
	{
		return e;
	}
	

}
