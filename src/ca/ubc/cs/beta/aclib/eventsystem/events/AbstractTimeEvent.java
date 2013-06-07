package ca.ubc.cs.beta.aclib.eventsystem.events;

import ca.ubc.cs.beta.aclib.eventsystem.ConfigurationTimeLimits;

public abstract class AbstractTimeEvent extends AutomaticConfiguratorEvent {

	private final ConfigurationTimeLimits configurationTimeLimit;

	public AbstractTimeEvent(ConfigurationTimeLimits configurationTimeLimit) {
		
		
		this.configurationTimeLimit = configurationTimeLimit;
	}

	public ConfigurationTimeLimits getConfigurationTimeLimit() {
		return configurationTimeLimit;
	}
	
	

}
