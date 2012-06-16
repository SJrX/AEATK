package ca.ubc.cs.beta.smac.state;

import java.util.Random;

import ca.ubc.cs.beta.configspace.ParamConfiguration;
import ca.ubc.cs.beta.seedgenerator.InstanceSeedGenerator;
import ca.ubc.cs.beta.smac.history.RunHistory;

/**
 * Interface for saving aspects of the state
 * 
 * After either/both of the RunHistory/PRNG are set call the save() method to write to disk.
 * 
 * 
 * 
 * @author seramage
 *
 */
public interface StateSerializer {

	/**
	 * Sets the runHistory to be associated with this State
	 * @param runHistory
	 */
	public void setRunHistory(RunHistory runHistory);
	
	/**
	 * Sets the Random Object to be associated with this State
	 * @param r
	 */
	public void setPRNG(RandomPoolType randType, Random r);
	
	/**
	 * Sets the Instance Seed Generator to be associated with this State
	 * @param gen
	 */
	public void setInstanceSeedGenerator(InstanceSeedGenerator gen);
	
	/**
	 * Saves the state
	 */
	public void save();

	/**
	 * Sets the incumbent configuration at this state
	 * @param config
	 */
	public void setIncumbent(ParamConfiguration config);
		
}
