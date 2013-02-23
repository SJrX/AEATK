package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.random.SeedableRandomSingleton;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractBlockingTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;

@ThreadSafe
public class RandomResponseTargetAlgorithmEvaluator extends
		AbstractBlockingTargetAlgorithmEvaluator {

	private final double scale;
	
	private final boolean sleep;
	
	private double maxValue = 0;
	private static final Logger log = LoggerFactory.getLogger(RandomResponseTargetAlgorithmEvaluator.class);
	
	public RandomResponseTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig) {
		super(execConfig);
		double scale;
		try {
			scale = Math.abs(Double.valueOf(execConfig.getAlgorithmExecutable())) * Math.random();
		}catch(NumberFormatException e)
		{
			scale = 10.0;
		}
		
		this.scale = scale;
		sleep = execConfig.isDeterministicAlgorithm();
		maxValue = execConfig.getAlgorithmCutoffTime();
		
	}



	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		return evaluateRun(runConfigs,null);
	}



	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, CurrentRunStatusObserver obs) {
		Random rand = SeedableRandomSingleton.getRandom();
		
		List<AlgorithmRun> ar = new ArrayList<AlgorithmRun>(runConfigs.size());
		for(RunConfig rc : runConfigs)
		{ 
			double time = rand.nextDouble()*maxValue;
			/*
			if(sleep)
			{
				log.debug("Sleeping");
				
				try {
					Thread.sleep( (long) time*1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				*/
				

			
			
			if(time >= rc.getCutoffTime())
			{
				ar.add(new ExistingAlgorithmRun(execConfig, rc, "TIMEOUT," + rc.getCutoffTime() + ",-1,0," + rc.getProblemInstanceSeedPair().getSeed()));
			} else
			{
				ar.add(new ExistingAlgorithmRun(execConfig, rc, "SAT, " + time + ",-1,0," + rc.getProblemInstanceSeedPair().getSeed()));
			}
		}
		
		return ar;
	}

	@Override
	public boolean isRunFinal() {
		return false;
	}

	@Override
	public boolean areRunsPersisted() {
		return false;
	}

	@Override
	protected void subtypeShutdown() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public boolean areRunsObservable() {
		return false;
	}

}
