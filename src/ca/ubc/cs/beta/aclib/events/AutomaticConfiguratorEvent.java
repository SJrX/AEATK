package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

public abstract class AutomaticConfiguratorEvent {

	private final UUID uuid;
	public AutomaticConfiguratorEvent(UUID uuid)
	{
		this.uuid = uuid;
	}
	
	public UUID getUUID()
	{
		return uuid;
	}
}
