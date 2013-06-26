package ca.ubc.cs.beta.aclib.termination.standard;

import java.util.Collection;
import java.util.Collections;

import net.jcip.annotations.ThreadSafe;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.eventsystem.EventHandler;
import ca.ubc.cs.beta.aclib.eventsystem.EventManager;
import ca.ubc.cs.beta.aclib.eventsystem.events.basic.AlgorithmRunCompletedEvent;
import ca.ubc.cs.beta.aclib.termination.ConditionType;
import ca.ubc.cs.beta.aclib.termination.TerminationCondition;
import ca.ubc.cs.beta.aclib.termination.ValueMaxStatus;
import static ca.ubc.cs.beta.aclib.misc.cputime.CPUTime.*;

@ThreadSafe
public class CPULimitCondition extends AbstractTerminationCondition 
{

	
	private final double tunerTimeLimit;
	private volatile double currentTime;
	

	private final String NAME = "CPUTIME";
	private final boolean countACTime;

	public CPULimitCondition(double tunerTimeLimit, boolean countACTime)
	{
		this.tunerTimeLimit = tunerTimeLimit;
		this.currentTime = 0;
		this.countACTime = countACTime;
	}
	
	public synchronized double getTunerTime()
	{
		
		return currentTime + ((countACTime) ? getCPUTime() : 0);
	}

	
	@Override
	public boolean haveToStop() {
		return (tunerTimeLimit <= getTunerTime());
			
	}

	@Override
	public Collection<ValueMaxStatus> currentStatus() {
		double tunerTime = getTunerTime();
		
		return Collections.singleton(new ValueMaxStatus(ConditionType.TUNERTIME, tunerTime, tunerTimeLimit, NAME, "Configuration Time Budget", "s"));
	}

	@Override
	public synchronized void notifyRun(AlgorithmRun run) {
		currentTime+=run.getRuntime();
	}
	
	@Override
	public String toString()
	{
		return currentStatus().toString();
	}

	@Override
	public String getTerminationReason() {
		if(haveToStop())
		{
			return "Tuner Time Limit (" +  tunerTimeLimit +  " s) has been reached";
		} else
		{
			return "";
		}
		
	}

	
}
