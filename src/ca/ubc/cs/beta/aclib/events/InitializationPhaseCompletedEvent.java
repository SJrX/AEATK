package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

public class InitializationPhaseCompletedEvent extends
		AutomaticConfiguratorEvent {

	public InitializationPhaseCompletedEvent(UUID uuid) {
		super(uuid);
	}

}
