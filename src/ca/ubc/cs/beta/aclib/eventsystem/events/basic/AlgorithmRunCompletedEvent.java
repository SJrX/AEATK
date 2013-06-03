package ca.ubc.cs.beta.aclib.eventsystem.events.basic;

import java.util.UUID;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.eventsystem.ConfigurationTimeLimits;
import ca.ubc.cs.beta.aclib.eventsystem.events.AbstractTimeEvent;

public class AlgorithmRunCompletedEvent extends AbstractTimeEvent {

	private final AlgorithmRun run;
	
	
	public AlgorithmRunCompletedEvent(AlgorithmRun run, ConfigurationTimeLimits limits) {
		super( limits);
		this.run = run;
		
	}
	
	public AlgorithmRun getRun() {
		return run;
	}
	
	

}
