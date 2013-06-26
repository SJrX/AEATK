package ca.ubc.cs.beta.aclib.termination.standard;

import java.util.Collection;
import java.util.Collections;

import net.jcip.annotations.ThreadSafe;

import ca.ubc.cs.beta.aclib.eventsystem.EventHandler;
import ca.ubc.cs.beta.aclib.eventsystem.EventManager;
import ca.ubc.cs.beta.aclib.eventsystem.events.AutomaticConfiguratorEvent;
import ca.ubc.cs.beta.aclib.eventsystem.events.basic.AlgorithmRunCompletedEvent;
import ca.ubc.cs.beta.aclib.eventsystem.events.model.ModelBuildEndEvent;
import ca.ubc.cs.beta.aclib.termination.ConditionType;
import ca.ubc.cs.beta.aclib.termination.TerminationCondition;
import ca.ubc.cs.beta.aclib.termination.ValueMaxStatus;
import static ca.ubc.cs.beta.aclib.misc.cputime.CPUTime.*;

@ThreadSafe
public class NoRunsForManyIterationTerminationCondition extends AbstractTerminationCondition implements EventHandler<AutomaticConfiguratorEvent> {

	private final String NAME = "NUMBER OF RUNS";
	private final  long iterationWithOutRuns;
	private volatile long successfullyBuiltModelsSinceLastRun = 0;

	public NoRunsForManyIterationTerminationCondition(long iterationsWithoutRun)
	{
		this.iterationWithOutRuns = iterationsWithoutRun;
	}
		

	@Override
	public boolean haveToStop() {
		return (successfullyBuiltModelsSinceLastRun >= iterationWithOutRuns);
			
	}

	@Override
	public Collection<ValueMaxStatus> currentStatus() {
		return Collections.emptySet();
	}

	@Override
	public synchronized void handleEvent(AutomaticConfiguratorEvent event) {
		
		if(event instanceof ModelBuildEndEvent)
		{
			successfullyBuiltModelsSinceLastRun++;
		} else if(event instanceof AlgorithmRunCompletedEvent)
		{
			successfullyBuiltModelsSinceLastRun = 0;
		}
		
	}
	
	@Override
	public String toString()
	{
		return currentStatus().toString();
	}

	@Override
	public void registerWithEventManager(EventManager evtManager) {
		evtManager.registerHandler(ModelBuildEndEvent.class, this);
		evtManager.registerHandler(AlgorithmRunCompletedEvent.class, this);
	}


	@Override
	public String getTerminationReason() {
		if(haveToStop())
		{
			return "Too many iterations / models have been built without a successful run (" +  successfullyBuiltModelsSinceLastRun +  ") has been reached";
		} else
		{
			return "";
		}
		
	}
	
}
