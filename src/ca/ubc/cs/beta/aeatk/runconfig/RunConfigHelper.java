package ca.ubc.cs.beta.aeatk.runconfig;

import ca.ubc.cs.beta.aeatk.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aeatk.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ec.util.MersenneTwister;

public class RunConfigHelper {

	public static RunConfig getRandomSingletonRunConfig(AlgorithmExecutionConfig execConfig)
	{
		return new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("Random"), (long) (Math.random()*100000)),124.0, ParamConfigurationSpace.getSingletonConfigurationSpace().getRandomConfiguration(new MersenneTwister()), execConfig);
		
	}

}
