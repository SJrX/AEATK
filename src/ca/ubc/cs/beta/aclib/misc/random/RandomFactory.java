package ca.ubc.cs.beta.aclib.misc.random;

import java.util.Random;




/***
 * Factory interface that allows creating a random of a specific type
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public interface RandomFactory<T extends Random> {

	/**
	 * Construct a Random object with the given seed 
	 * @param seed 	seed to use in the random object
	 * @return	the random object
	 */
	public T getRandom(long seed);
}
