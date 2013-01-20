package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.random.SeedableRandomSingleton;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluator;

public class RandomResponseTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluator {

	private double scale = 1.0;
	
	private boolean sleep = false;
	
	private static final Logger log = LoggerFactory.getLogger(RandomResponseTargetAlgorithmEvaluator.class);
	
	public RandomResponseTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig) {
		super(execConfig);
		try {
			scale = Math.abs(Double.valueOf(execConfig.getAlgorithmExecutable()));
		}catch(NumberFormatException e)
		{
			scale = 10.0;
		}
		
		//sleep = execConfig.isDeterministicAlgorithm();
		
	}

	@Override
	public void notifyShutdown() {

	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		Random rand = SeedableRandomSingleton.getRandom();
		
		List<AlgorithmRun> ar = new ArrayList<AlgorithmRun>(runConfigs.size());
		for(RunConfig rc : runConfigs)
		{ 
			double time = rand.nextDouble()*scale;
			
			if(sleep)
			{
				log.debug("Sleeping");
				
				try {
					Thread.sleep( (long) time*1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				
				
			}
			
			ar.add(new ExistingAlgorithmRun(execConfig, rc, "SAT, " + time + ",-1,0," + rc.getProblemInstanceSeedPair().getSeed()));
		}
		
		return ar;
	}

}
