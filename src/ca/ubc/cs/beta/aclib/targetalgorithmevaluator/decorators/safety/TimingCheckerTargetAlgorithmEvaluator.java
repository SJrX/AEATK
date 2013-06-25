package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.safety;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractForEachRunTargetAlgorithmEvaluatorDecorator;


/**
 * Logs warnings if timing invariants on algorithm aren't holding
 * 
 * This class generally increases the time after a single warning, so as not to be spammy.
 * 
 * @author Steve Ramage 
 *
 */
@ThreadSafe
public class TimingCheckerTargetAlgorithmEvaluator extends	AbstractForEachRunTargetAlgorithmEvaluatorDecorator {


	
	
	private double totalWallClockOverhead = 0;
	private double totalRuntimeOverhead = 0;
	
	private static Logger log = LoggerFactory.getLogger(TimingCheckerTargetAlgorithmEvaluator.class);
	
	public TimingCheckerTargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig, TargetAlgorithmEvaluator tae) {
		super(tae);
		
		wallClockDeltaToRequireLogging = Math.min(1.5*execConfig.getAlgorithmCutoffTime(), 10);
		

	}
	/**
	 * Linear amount of time we should allow the algorithm to exceed the request before logging a warning. 
	 * 
	 * We use delta because cap times are not constant, and we are concerned with the uncounted overhead
	 */
	private double runtimeDeltaToRequireLogging = 1;
	
	/**
	 * Linear amount of time we should allow the algorithms wallclock time to exceed the request before logging a warning.
	 * 
	 * We use delta because cap times are not constant, and we are concerned with the uncounted overhead
	 */
	private double wallClockDeltaToRequireLogging;
	
	

	public void notifyShutdown()
	{
		synchronized(this)
		{
			log.info("Total Runtime Overhead (Sum of the amount of reported runtime that exceeded the cutoff time): {} seconds", totalRuntimeOverhead );
			log.info("Total Wallclock Overhead (Sum of the amount of wallclock time that exceeded the cutoff time): {} seconds", totalWallClockOverhead );
		}
		tae.notifyShutdown();
	}

	@Override
	protected synchronized AlgorithmRun processRun(AlgorithmRun run) {
		
		double runtimeOverhead = run.getRuntime() - run.getRunConfig().getCutoffTime();
		
		totalRuntimeOverhead += Math.max(runtimeOverhead, 0);
		
		if(runtimeOverhead > runtimeDeltaToRequireLogging)
		{
			runtimeDeltaToRequireLogging = runtimeOverhead + 1;
			
			Object[] args = {run.getRuntime(), run.getRunConfig().getCutoffTime(), runtimeOverhead, runtimeDeltaToRequireLogging};
			log.warn("Algorithm Run Result reported a runtime of {} (secs) that exceeded it's cutoff time of {} (secs) by {} (secs). Next warning at {} (secs)  ", args);
		}
		
		double wallClockOverhead = run.getWallclockExecutionTime() - run.getRunConfig().getCutoffTime();
		
		totalWallClockOverhead += Math.max(wallClockOverhead, 0);
		
		if(wallClockOverhead > wallClockDeltaToRequireLogging)
		{
			wallClockDeltaToRequireLogging = wallClockOverhead + 1;
			Object[] args = {run.getWallclockExecutionTime(), run.getRunConfig().getCutoffTime(), wallClockOverhead, wallClockDeltaToRequireLogging};
			log.warn("Algorithm Run Result reported wallclock time of {} (secs) that exceeded it's cutoff time of {} (secs) by {} (secs). Next warning at {} (secs)  ", args);
		}
		
		return run;
	}
	

}
