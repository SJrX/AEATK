package ca.ubc.cs.beta.probleminstance;

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

}