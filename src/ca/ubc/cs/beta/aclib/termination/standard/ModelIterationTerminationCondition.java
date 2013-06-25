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
public class ModelIterationTerminationCondition extends AbstractTerminationCondition implements EventHandler<ModelBuildEndEvent> {

	private final String NAME = "NUMBER OF RUNS";
	private final  long modelBuildLimit;
	private long modelBuildIteration = 0;

	public ModelIterationTerminationCondition(long modelBuildLimit)
	{
		this.modelBuildLimit = modelBuildLimit;
	}
		

	@Override
	public boolean haveToStop() {
		return (modelBuildIteration >= modelBuildLimit);
			
	}

	@Override
	public Collection<ValueMaxStatus> currentStatus() {
		return Collections.singleton(new ValueMaxStatus(ConditionType.TUNERTIME, modelBuildIteration, modelBuildLimit, NAME, "Model/Iteration", ""));
	}

	@Override
	public synchronized void handleEvent(ModelBuildEndEvent event) {
		modelBuildIteration++;
	}
	
	@Override
	public String toString()
	{
		return currentStatus().toString();
	}

	@Override
	public void registerWithEventManager(EventManager evtManager) {
		evtManager.registerHandler(ModelBuildEndEvent.class, this);
	}


	@Override
	public String getTerminationReason() {
		if(haveToStop())
		{
			return "Model Building / Iteration Limit (" +  modelBuildIteration +  ") has been reached";
		} else
		{
			return "";
		}
		
	}
	
}
