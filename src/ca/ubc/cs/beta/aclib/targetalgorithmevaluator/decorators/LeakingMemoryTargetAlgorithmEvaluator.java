package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;

/**
 * Leaks some amount of memory for every run
 * 
 * @author Steve Ramage 
 *
 */
public class LeakingMemoryTargetAlgorithmEvaluator extends AbstractTargetAlgorithmEvaluatorDecorator {

	private final List<byte[]> leakedMemory = new ArrayList<byte[]>();
	
	private Logger log = LoggerFactory.getLogger(getClass());
	private static int memoryToLeak = 1024;
	
	private long totalLeaked = 0;
	public static void leakMemoryAmount(int newAmount)
	{
		if(memoryToLeak < 0) throw new IllegalArgumentException("I'm not supplying a user-friendly error to something that is designed to leak memory. Don't use this, or at the very least have the good sense to leak a positive amount of memory");
		memoryToLeak = newAmount;
	}
	
	public LeakingMemoryTargetAlgorithmEvaluator(TargetAlgorithmEvaluator tae) {
		super(tae);

	}
	
	
	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig run) {
		List<AlgorithmRun> runs = tae.evaluateRun(run);
		totalLeaked += runs.size() * memoryToLeak;
		log.warn("Leaking >= {} bytes of memory, total leaked: {} MB",runs.size() * memoryToLeak, totalLeaked/1024/1024);
		leakedMemory.add(new byte[runs.size() * memoryToLeak]);
		return runs;
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
		totalLeaked += runs.size() * memoryToLeak;
		log.warn("Leaking >= {} bytes of memory, total leaked: {} MB",runs.size() * memoryToLeak, totalLeaked/1024/1024);
		leakedMemory.add(new byte[runs.size() * memoryToLeak]);
		return runs;
	}

	

	
	
}
