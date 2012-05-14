package ca.ubc.cs.beta.smac.state;

import java.util.Random;

import ca.ubc.cs.beta.configspace.ParamConfiguration;
import ca.ubc.cs.beta.probleminstance.InstanceSeedGenerator;
import ca.ubc.cs.beta.smac.history.RunHistory;

/**
 * Contains accessor methods to restore the state to the requested iteration.
 * NOTE: Implementations can NOT return null for objects they don't have. They should
 * either return default implementations.
 * @author seramage
 *
 */
public interface StateDeserializer {
	
	public RunHistory getRunHistory();
	
	public Random getPRNG(RandomPoolType t);
	
	public InstanceSeedGenerator getInstanceSeedGenerator();

	public int getIteration();

	public ParamConfiguration getIncumbent();
}
