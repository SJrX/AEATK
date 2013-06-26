package ca.ubc.cs.beta.aclib.termination.standard;

import java.util.Collection;
import java.util.Collections;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.eventsystem.EventHandler;
import ca.ubc.cs.beta.aclib.eventsystem.EventManager;
import ca.ubc.cs.beta.aclib.eventsystem.events.basic.AlgorithmRunCompletedEvent;
import ca.ubc.cs.beta.aclib.termination.ConditionType;
import ca.ubc.cs.beta.aclib.termination.ValueMaxStatus;

public class ConfigurationSpaceExhaustedCondition extends AbstractTerminationCondition implements EventHandler<AlgorithmRunCompletedEvent> {

	private final double runLimit;
	private volatile long algorithmRuns = 0;
	private final String NAME = "CONFIG_SPACE";
	private final int runsPerConfiguration;
	private final double configSpaceSize;
	
	public ConfigurationSpaceExhaustedCondition(ParamConfigurationSpace configSpace, int runsPerConfiguration)
	{
		this.runLimit = configSpace.getUpperBoundOnSize() * runsPerConfiguration;
		this.runsPerConfiguration = runsPerConfiguration;
		this.configSpaceSize = configSpace.getUpperBoundOnSize();
		
	}
	
	@Override
	public boolean haveToStop() {
		if(this.runLimit  <= algorithmRuns)
		{
			return true;
		} else
		{
			return false;
		}
	}


	@Override
	public synchronized void handleEvent(AlgorithmRunCompletedEvent event) {
		algorithmRuns++;
	}
	
	@Override
	public String toString()
	{
		return currentStatus().toString();
	}

	@Override
	public void registerWithEventManager(EventManager evtManager) {
		evtManager.registerHandler(AlgorithmRunCompletedEvent.class, this);
	}


	@Override
	public Collection<ValueMaxStatus> currentStatus() {
		return Collections.singleton(new ValueMaxStatus(ConditionType.NUMBER_OF_RUNS, algorithmRuns, runLimit, NAME, "Configuration Space Searched " + (  (this.algorithmRuns * 100 / (double) this.runLimit))+ " % \n" ));
	}

	
	@Override
	public String getTerminationReason() {
		if(haveToStop())
		{
			return "Every possible configuration (" + this.configSpaceSize +") and problem instance seed pair (" + this.runsPerConfiguration + ") has been run ";
		} else
		{
			return "";
		}
	}
	
	
	
	
}
