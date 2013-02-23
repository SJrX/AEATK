package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

public abstract class AbstractTimeEvent extends AutomaticConfiguratorEvent {

	private final ConfigurationTimeLimits configurationTimeLimit;

	public AbstractTimeEvent(UUID uuid, ConfigurationTimeLimits configurationTimeLimit) {
		super(uuid);
		
		this.configurationTimeLimit = configurationTimeLimit;
	}

	public ConfigurationTimeLimits getConfigurationTimeLimit() {
		return configurationTimeLimit;
	}
	
	

}
