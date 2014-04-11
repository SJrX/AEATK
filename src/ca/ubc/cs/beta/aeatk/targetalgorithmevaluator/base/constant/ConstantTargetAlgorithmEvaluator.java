package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.constant;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.AbstractSyncTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;

public class ConstantTargetAlgorithmEvaluator extends AbstractSyncTargetAlgorithmEvaluator {

	private final ConstantTargetAlgorithmEvaluatorOptions options;
	
	public ConstantTargetAlgorithmEvaluator( ConstantTargetAlgorithmEvaluatorOptions options) {
		this.options = options;
	}

	@Override
	public boolean isRunFinal() {
		return true;
	}

	@Override
	public boolean areRunsPersisted() {
		return true;
	}

	@Override
	protected void subtypeShutdown() {
		
	}

	@Override
	public boolean areRunsObservable() {
		return false;
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<AlgorithmRunConfiguration> runConfigs,
			TargetAlgorithmEvaluatorRunObserver obs) {
		List<AlgorithmRun> runs = new ArrayList<AlgorithmRun>();
		
		for(AlgorithmRunConfiguration rc : runConfigs)
		{
			String addlRunData = "";
			
			if((options.additionalRunData != null) && (options.additionalRunData.trim().length() > 0))
			{
				addlRunData = "," + options.additionalRunData;
			}
			
			runs.add(new ExistingAlgorithmRun( rc, options.runResult , options.runtime , options.runlength , options.quality , rc.getProblemInstanceSeedPair().getSeed() , addlRunData));
		}
		
		return runs;
	}

}
