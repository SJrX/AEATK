package ca.ubc.cs.beta.seedgenerator;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import ca.ubc.cs.beta.probleminstance.ProblemInstance;

public interface InstanceSeedGenerator extends Serializable {

	public void reinit();

	public int getNextSeed(ProblemInstance pi);

	/**
	 * Used so that MATLAB can have access to the same seeds as us
	 * @param id
	 * @return
	 */
	@Deprecated
	public int getNextSeed(Integer id);

	public boolean hasNextSeed(ProblemInstance pi);

	/**
	 * Returns the order of instances specified in instance seed pairs. An empty list is returned
	 * if no instance seed pairs were used. 
	 * @return
	 */
	public List<ProblemInstance> getProblemInstanceOrder(Collection<ProblemInstance> instances);

	
	/**
	 * Returns the initial number of total seeds available 
	 * 
	 * @return total number seeds that are first available (not necessarily the number left)
	 */
	public int getInitialSeedCount();
	
	/**
	 * Returns whether or not all instances were initialized with the same number of seeds
	 * 
	 * This method is temporary, and exists because with unequal seeds most RunHistory implementations
	 * don't know how to find the next instance.
	 * 
	 * @return true if all instances had the same number of initial seeds.
	 */
	@Deprecated
	public boolean allInstancesHaveSameNumberOfSeeds();
	
	
}