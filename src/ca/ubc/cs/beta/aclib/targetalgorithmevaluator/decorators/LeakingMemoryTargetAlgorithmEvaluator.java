package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.deferred.TAECallback;

/**
 * Leaks some amount of memory for every run
 * 
 * @author Steve Ramage 
 *
 */
@ThreadSafe
public class LeakingMemoryTargetAlgorithmEvaluator extends AbstractTargetAlgorithmEvaluatorDecorator {

	private final List<byte[]> leakedMemory = new ArrayList<byte[]>();
	private volatile long totalLeaked = 0;
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	private static volatile int memoryToLeak = 1024;
	
	
	public static void leakMemoryAmount(int newAmount)
	{
		if(newAmount < 0) throw new IllegalArgumentException("I'm not supplying a user-friendly error to something that is designed to leak memory. Don't use this, or at the very least have the good sense to leak a positive amount of memory");
		memoryToLeak = newAmount;
	}
	
	public LeakingMemoryTargetAlgorithmEvaluator(TargetAlgorithmEvaluator tae) {
		super(tae);
	}
	
	
	
	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig run) {
		List<AlgorithmRun> runs = tae.evaluateRun(run);
		
		leak(1);
		return runs;
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
		leak(runConfigs.size());
		return runs;
	}

	@Override
	public void evaluateRunsAsync(RunConfig runConfig, TAECallback handler) {
		tae.evaluateRunsAsync(runConfig, handler);
		leak(1);
		
		
	}

	@Override
	public void evaluateRunsAsync(List<RunConfig> runConfigs, TAECallback handler) {
		tae.evaluateRunsAsync(runConfigs, handler);
		leak(runConfigs.size());
	}

	private synchronized void leak(int size)
	{
		totalLeaked += size * memoryToLeak;
		leakedMemory.add(new byte[size * memoryToLeak]);
		log.warn("Leaking >= {} bytes of memory, total leaked: {} MB",size * memoryToLeak, totalLeaked/1024/1024);
	}
	

	
	
}
