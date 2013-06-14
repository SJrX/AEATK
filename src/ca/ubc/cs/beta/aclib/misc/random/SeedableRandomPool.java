package ca.ubc.cs.beta.aclib.misc.random;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import ec.util.MersenneTwister;

import net.jcip.annotations.ThreadSafe;

/***
 * An object that maintains a map of all the random objects in use,
 * and seeds them differently as needed.
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
@ThreadSafe
public class SeedableRandomPool {

	
	private final int initialSeed;
	private final RandomFactory<? extends Random> fact;
	private final Map<String, Random> randomMap = new ConcurrentHashMap<String, Random>();
	private final Map<String, Integer> initialSeeds;

	public SeedableRandomPool(long initialSeed)
	{
		//casting to int will just drop the first few bits
		this((int) initialSeed, new DefaultRandomFactory(MersenneTwister.class), Collections.<String, Integer> emptyMap() );
	}
	

	public SeedableRandomPool(int initialSeed)
	{
		this(initialSeed, new DefaultRandomFactory(MersenneTwister.class), Collections.<String, Integer> emptyMap() );
	}
	
	/**
	 * 
	 * @param initialSeed  	The initial seed for the objects
	 * @param randomClass   A class object that extends Random and has a 1-arg constructor that takes an integer or a long.
	 */
	public SeedableRandomPool(int initialSeed, final Class<? extends Random> randomClass)
	{
		this(initialSeed, new DefaultRandomFactory(randomClass), Collections.<String, Integer> emptyMap() );
	}
	
	/**
	 * 
	 * @param initialSeed  The initial seed for the objects
	 * @param fact		   A factory method that allows us to create random objects, repeated invokations for the same seed should ALWAYS return a new object 			
	 */
	public SeedableRandomPool(int initialSeed, RandomFactory<? extends Random> fact)
	{
		this(initialSeed, fact, Collections.<String, Integer> emptyMap());
	}
	
	/**
	 * 
	 * @param initialSeed  	The initial seed for the objects
	 * @param randomClass   A class object that extends Random and has a 1-arg constructor that takes an integer or a long.
	 */
	public SeedableRandomPool(int initialSeed, final Class<? extends Random> randomClass, Map<String, Integer> initialSeeds)
	{
		this(initialSeed, new DefaultRandomFactory(randomClass) , initialSeeds);
	}
	
	/**
	 * 
	 * @param initialSeed  The initial seed for the objects
	 * @param fact		   A factory method that allows us to create random objects, repeated invokations for the same seed should ALWAYS return a new object 			
	 */
	public SeedableRandomPool(int initialSeed, RandomFactory<? extends Random> fact, Map<String, Integer> initialSeeds)
	{
		this.initialSeed = initialSeed;
		this.fact = fact;
		this.initialSeeds = new ConcurrentHashMap<String, Integer>(initialSeeds);
	}
	
	
	/**
	 * Returns a random object for a given name
	 * @param name 	The name of the random object
	 * @return	Random object appropriately seeded if the seed was set explicitly or if not the seed used is defined to be the hashCode() of the string XOR initial seed.
	 */
	public synchronized Random getRandom(String name)
	{
		Random random = randomMap.get(name);
		if(random == null)
		{
			Integer seed = this.initialSeeds.get(name);
			if(seed == null)
			{
				seed = name.hashCode() ^ initialSeed;
				this.initialSeeds.put(name,  seed);
			}
			
			random = fact.getRandom(seed);
			randomMap.put(name, fact.getRandom(seed));
			 
		}
		
		return random;
	}
	
	/**
	 * @return a mapping of all strings to seeds used
	 */
	public Map<String, Integer> getAllSeeds()
	{
		return Collections.unmodifiableMap(this.initialSeeds);
	}
	
	/**
	 * Returns the original seed used
	 * @return
	 */
	public int getInitialSeed()
	{
		return this.initialSeed;
	}
	
	private static class DefaultRandomFactory implements RandomFactory<Random>
	{
		private final Class<? extends Random> randomClass;
		public DefaultRandomFactory(Class<? extends Random> randomClass)
		{
			this.randomClass = randomClass;
			
		}
		
		@Override
		public Random getRandom(long seed) {
			
			try {
			Constructor<? extends Random> randConstruct = randomClass.getConstructor(long.class);
					
			if(randConstruct != null)
			{
				return randConstruct.newInstance(seed);
			}
				
			
			randConstruct = randomClass.getConstructor(int.class);
			
			if(randConstruct != null)
			{
				if(seed <= Integer.MAX_VALUE && seed >= Integer.MIN_VALUE)
				{
					return randConstruct.newInstance(seed);
				} else
				{
					throw new IllegalArgumentException("Seed (" + seed + ") specified is too big to fit in a integer, and we didn't find a long constructor");
				}
			}
					
			throw new IllegalArgumentException("Could not find a constructor that took an long or an integer as it's only parameter");
			
			} catch(IllegalArgumentException e)
			{
				throw e;
			} catch (Exception e) {
				throw new IllegalStateException("Error while generating random pool", e);
			} 
			
		}
	}
}


