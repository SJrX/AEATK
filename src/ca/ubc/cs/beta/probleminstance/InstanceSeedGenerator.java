package ca.ubc.cs.beta.probleminstance;

import java.util.Collection;
import java.util.List;

import ca.ubc.cs.beta.ac.config.ProblemInstance;

public interface InstanceSeedGenerator {

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

}