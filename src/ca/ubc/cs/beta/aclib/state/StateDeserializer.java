package ca.ubc.cs.beta.aclib.state;

import java.util.Random;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.runhistory.RunHistory;
import ca.ubc.cs.beta.aclib.seedgenerator.InstanceSeedGenerator;

/**
 * Contains accessor methods to restore the state to the requested iteration.
 * NOTE: Implementations can NOT return null for objects they don't have. 
 * They may return default or 'empty' objects in certain cases however
 * 
 * (For instance they could return a new RunHistory object with no runs)
 * 
 * @author seramage
 *
 */
public interface StateDeserializer {
	
	/**
	 * Retrieves the RunHistory object
	 * @return runHistory object
	 */
	public RunHistory getRunHistory();
	
	/**
	 * Retrieves a random object of the corresponding type
	 * @param t 	type of the random object to return 
	 * @return	random object for the type
	 */
	public Random getPRNG(RandomPoolType t);
	
	/**
	 * Returns the instance seed generator saved in the state
	 * @return instanceseedgenerator
	 */
	public InstanceSeedGenerator getInstanceSeedGenerator();

	/**
	 * Returns the iteration represented by the state
	 * @return iteration
	 */
	public int getIteration();

	/**
	 * Returns the incumbent configuration in the state
	 * @return incumbent configuration
	 */
	public ParamConfiguration getIncumbent();
}
