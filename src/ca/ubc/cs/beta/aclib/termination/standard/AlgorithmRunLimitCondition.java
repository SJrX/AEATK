package ca.ubc.cs.beta.aclib.termination.standard;

import java.util.Collection;
import java.util.Collections;

import net.jcip.annotations.ThreadSafe;

import ca.ubc.cs.beta.aclib.eventsystem.EventHandler;
import ca.ubc.cs.beta.aclib.eventsystem.EventManager;
import ca.ubc.cs.beta.aclib.eventsystem.events.basic.AlgorithmRunCompletedEvent;
import ca.ubc.cs.beta.aclib.termination.ConditionType;
import ca.ubc.cs.beta.aclib.termination.TerminationCondition;
import ca.ubc.cs.beta.aclib.termination.ValueMaxStatus;
import static ca.ubc.cs.beta.aclib.misc.cputime.CPUTime.*;

@ThreadSafe
public class AlgorithmRunLimitCondition extends AbstractTerminationCondition implements EventHandler<AlgorithmRunCompletedEvent> {

	private final String NAME = "NUMBER OF RUNS";
	private final long algorithmRunLimit;
	private volatile long algorithmRuns = 0;

	public AlgorithmRunLimitCondition(long algorithmRunLimit)
	{
		this.algorithmRunLimit = algorithmRunLimit;
	}
		

	@Override
	public boolean haveToStop() {
		return (algorithmRuns >= algorithmRunLimit);
			
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
		return Collections.singleton(new ValueMaxStatus(ConditionType.NUMBER_OF_RUNS, algorithmRuns, algorithmRunLimit, NAME, "Algorithm Runs", ""));
	}

	
	@Override
	public String getTerminationReason() {
		if(haveToStop())
		{
			return "Algorithm Run Limit (" +  algorithmRuns +  " runs) has been reached";
		} else
		{
			return "";
		}
	}
	
}
