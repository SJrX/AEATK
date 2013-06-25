package ca.ubc.cs.beta.aclib.termination;

import java.util.Collection;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.eventsystem.EventManager;

public interface TerminationCondition {
	
	/**
	 * Determines whether we have to stop
	 * @return <code>true</code> have to stop
	 */
	public boolean haveToStop();
	

	/**
	 * Collection of ValueMaxStatus triples
	 * 
	 * @return A collection of ValueMaxStatus
	 */
	public Collection<ValueMaxStatus> currentStatus();
	
	/**
	 * Allows conditions to register with the event manager
	 * @param evtManager
	 */
	public void registerWithEventManager(EventManager evtManager);


	/**
	 * Notify the termination conditions about a run that has completed
	 * <br/>
	 * <b>IMPLEMENTATION NOTE:</b> This mechanism is used instead of the event manager, because the actual AlgorithmRunCompletedEvent
	 * requires the tuner time to be updated 
	 * 
	 * @param run
	 */
	public void notifyRun(AlgorithmRun run);
	
	public double getTunerTime();
	
}

