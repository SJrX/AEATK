package ca.ubc.cs.beta.aclib.events;

import java.util.UUID;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;

public class AlgorithmRunCompletedEvent extends AbstractTimeEvent {

	private final AlgorithmRun run;
	
	
	public AlgorithmRunCompletedEvent(UUID uuid, AlgorithmRun run, ConfigurationTimeLimits limits) {
		super(uuid, limits);
		this.run = run;
		
	}
	
	public AlgorithmRun getRun() {
		return run;
	}
	
	

}
