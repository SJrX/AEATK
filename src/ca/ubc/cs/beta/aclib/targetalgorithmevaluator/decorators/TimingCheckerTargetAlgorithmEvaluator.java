package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;


/**
 * Logs warnings if timing invariants on algorithm aren't holding
 * 
 * This class generally increases the time after a single warning, so as not to be spammy.
 * 
 * @author Steve Ramage 
 *
 */
public class TimingCheckerTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

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
	
	
	
	private double totalWallClockOverhead = 0;
	private double totalRuntimeOverhead = 0;
	
	private static Logger log = LoggerFactory.getLogger(TimingCheckerTargetAlgorithmEvaluator.class);
	
	public TimingCheckerTargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig, TargetAlgorithmEvaluator tae) {
		super(tae);
		
		wallClockDeltaToRequireLogging = Math.min(1.5*execConfig.getAlgorithmCutoffTime(), 10);
		

	}
	
	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig run) {
		return tae.evaluateRun(Collections.singletonList(run));
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
		
		
		for(AlgorithmRun run : runs)
		{
			
			double runtimeOverhead = run.getRuntime() - run.getRunConfig().getCutoffTime();
			
			
			totalRuntimeOverhead += Math.max(runtimeOverhead, 0);
			
			if(runtimeOverhead > runtimeDeltaToRequireLogging)
			{
				runtimeDeltaToRequireLogging = runtimeOverhead + 1;
				log.warn("Algorithm has exceeded allowed runtime by {} seconds, next warning at: {} ", runtimeOverhead, runtimeDeltaToRequireLogging);
			}
			
			double wallClockOverhead = run.getWallclockExecutionTime() - run.getRunConfig().getCutoffTime();
			
			totalWallClockOverhead += Math.max(wallClockOverhead, 0);
			
			if(wallClockOverhead > wallClockDeltaToRequireLogging)
			{
				wallClockDeltaToRequireLogging = wallClockOverhead + 1;
				log.warn("Algorithm has exceeded allowed wallclock time by {} seconds, next warning at: {} ", wallClockOverhead, wallClockDeltaToRequireLogging);
			}
			
			
			
		}
		
		return runs;
	}
	
	public void notifyShutdown()
	{
		log.info("Total Runtime Overhead: {} seconds", totalRuntimeOverhead );
		log.info("Total wallclock Overhead: {} seconds", totalWallClockOverhead );
		tae.notifyShutdown();
	}
	

}
