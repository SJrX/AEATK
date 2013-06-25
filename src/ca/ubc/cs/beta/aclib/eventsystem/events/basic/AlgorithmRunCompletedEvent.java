package ca.ubc.cs.beta.aclib.eventsystem.events.basic;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.eventsystem.events.AbstractTimeEvent;

public class AlgorithmRunCompletedEvent extends AbstractTimeEvent {

	private final AlgorithmRun run;
	
	
	public AlgorithmRunCompletedEvent(AlgorithmRun run) {
		this.run = run;
		
	}
	
	public AlgorithmRun getRun() {
		return run;
	}
	
	

}
