package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.debug;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.associatedvalue.AssociatedValue;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractBlockingTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;

public class PreloadedResponseTargetAlgorithmEvaluator extends AbstractBlockingTargetAlgorithmEvaluator {
	
	
	Queue<AssociatedValue<RunResult, Double>> myQueue = new LinkedList<AssociatedValue<RunResult, Double>>();
	
	private final PreloadedResponseTargetAlgorithmEvaluatorOptions opts;
	
	
	public PreloadedResponseTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig, Queue<AssociatedValue<RunResult, Double>> myQueue, PreloadedResponseTargetAlgorithmEvaluatorOptions opts) {
		super(execConfig);
		this.myQueue = 	myQueue;
		this.opts = opts;
		
	}

	@Override
	public boolean isRunFinal() {
		return true;
	}

	@Override
	public boolean areRunsPersisted() {
		return false;
	}

	@Override
	public boolean areRunsObservable() {
		return false;
	}

	@Override
	protected void subtypeShutdown() {

		
	}


	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs,
			CurrentRunStatusObserver obs) {
		List<AlgorithmRun> runs = new ArrayList<AlgorithmRun>();
		for(RunConfig rc : runConfigs)
		{
	
			AssociatedValue<RunResult, Double> v = myQueue.poll();
			if(v == null) throw new IllegalStateException("Error out of existing runs");
			runs.add(new ExistingAlgorithmRun(execConfig, rc, v.getAssociatedValue() , v.getValue() , opts.runLength ,opts.quality, rc.getProblemInstanceSeedPair().getSeed()));
			
			
		}
		return runs;
		
	}
		
	

}
