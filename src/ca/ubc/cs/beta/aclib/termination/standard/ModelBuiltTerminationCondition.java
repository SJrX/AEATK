package ca.ubc.cs.beta.aclib.termination.standard;

import java.util.Collection;
import java.util.Collections;

import net.jcip.annotations.ThreadSafe;

import ca.ubc.cs.beta.aclib.eventsystem.EventHandler;
import ca.ubc.cs.beta.aclib.eventsystem.EventManager;
import ca.ubc.cs.beta.aclib.eventsystem.events.basic.AlgorithmRunCompletedEvent;
import ca.ubc.cs.beta.aclib.eventsystem.events.model.ModelBuildEndEvent;
import ca.ubc.cs.beta.aclib.termination.ConditionType;
import ca.ubc.cs.beta.aclib.termination.TerminationCondition;
import ca.ubc.cs.beta.aclib.termination.ValueMaxStatus;
import static ca.ubc.cs.beta.aclib.misc.cputime.CPUTime.*;

@ThreadSafe
public class ModelBuiltTerminationCondition extends AbstractTerminationCondition implements EventHandler<ModelBuildEndEvent> {

	private final String NAME = "NUMBER OF RUNS";
	private final  long modelBuildLimit;
	private long modelBuilds = 0;

	public ModelBuiltTerminationCondition(long modelBuildLimit)
	{
		this.modelBuildLimit = modelBuildLimit;
	}
		

	@Override
	public boolean haveToStop() {
		return (modelBuilds >= modelBuildLimit);
			
	}

	@Override
	public Collection<ValueMaxStatus> currentStatus() {
		return Collections.singleton(new ValueMaxStatus(ConditionType.TUNERTIME, modelBuilds, modelBuildLimit, NAME, "Model Iteration", ""));
	}

	@Override
	public synchronized void handleEvent(ModelBuildEndEvent event) {
		modelBuilds++;
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
	
}
