package ca.ubc.cs.beta.aclib.state;

import java.util.Random;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.runhistory.RunHistory;
import ca.ubc.cs.beta.aclib.seedgenerator.InstanceSeedGenerator;

/**
 * Interface for saving aspects of the state
 * 
 * After either/both of the RunHistory/PRNG are set call the save() method to write to disk.
 * 
 * @author seramage
 *
 */
public interface StateSerializer {

	/**
	 * Sets the runHistory to be associated with this State
	 * @param runHistory	runHistory object to save
	 */
	public void setRunHistory(RunHistory runHistory);
	
	/**
	 * Sets the Random Object to be associated with this State
	 * @param randType 	the type of the random object
	 * @param random 	the random object to save
	 */
	public void setPRNG(RandomPoolType randType, Random random);
	
	/**
	 * Sets the Instance Seed Generator to be associated with this State
	 * @param gen instance seed genarotr to save
	 */
	public void setInstanceSeedGenerator(InstanceSeedGenerator gen);
	
	/**
	 * Saves the state to the persistence device
	 * 
	 */
	public void save();

	/**
	 * Sets the incumbent configuration at this state
	 * @param config	configuration to mark as the incumbent
	 */
	public void setIncumbent(ParamConfiguration config);
		
}
