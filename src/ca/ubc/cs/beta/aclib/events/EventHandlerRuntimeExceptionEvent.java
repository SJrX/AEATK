package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

/**
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class EventHandlerRuntimeExceptionEvent extends AutomaticConfiguratorEvent{

	
	private final RuntimeException e;
	private final AutomaticConfiguratorEvent event;
	
	public EventHandlerRuntimeExceptionEvent(UUID uuid, RuntimeException e, AutomaticConfiguratorEvent event)
	{
		super(uuid);
		this.e = e;
		this.event = event;
	}
	
	public RuntimeException getRuntimeException()
	{
		return e;
	}
	
	
	public AutomaticConfiguratorEvent getTriggeringEvent()
	{
		return event;
	}

}
