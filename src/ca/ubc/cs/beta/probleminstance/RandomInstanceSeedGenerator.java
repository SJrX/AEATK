package ca.ubc.cs.beta.probleminstance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.ac.config.ProblemInstance;
import ec.util.MersenneTwister;

public class RandomInstanceSeedGenerator implements InstanceSeedGenerator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5545604876585367506L;
	
	private final HashMap<Integer, Random> randomPool;
	private final HashMap<Integer, Set<Integer>> usedSeeds;
	
	//
	private static transient final Logger log = LoggerFactory.getLogger(RandomInstanceSeedGenerator.class);
	
	private long nextSeed;
	private final long initialSeed;
	private final int initialNumberOfInstances;

	private final int maxSeedsPerInstance;
	
	
	

	public RandomInstanceSeedGenerator(List<ProblemInstance> instances, long seed)
	{
		this(instances.size(), seed);
	}
	
	public RandomInstanceSeedGenerator(int numberOfInstances, long seed)
	{
		this(numberOfInstances,seed,Integer.MAX_VALUE);
	}
	
	public RandomInstanceSeedGenerator(int numberOfInstances, long seed, int maxSeedsPerInstance)
	{
		//Negative Values are used for SetInstanceSeedGenerator we will take the absolute value
		maxSeedsPerInstance = Math.abs(maxSeedsPerInstance);
		this.maxSeedsPerInstance = maxSeedsPerInstance;
		log.debug("Initializing Instance Seed PRNG with Seed {} and {} allowed seeds", seed, maxSeedsPerInstance);
		randomPool = new HashMap<Integer, Random>();
		usedSeeds = new HashMap<Integer, Set<Integer>>();
		initialSeed = seed;
		initialNumberOfInstances = numberOfInstances;
		
		reinit();
	}
	
	
	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.probleminstance.InstanceSeedGenerator#reinit()
	 */
	@Override
	public void reinit()
	{
		log.info("Re-Initializing Instance Seed PRNG with Seed {} and instances {} ", initialSeed, initialNumberOfInstances);	
		nextSeed = initialSeed;
		randomPool.clear();
		usedSeeds.clear();
		
		for(int i=0; i < initialNumberOfInstances; i++)
		{
			randomPool.put(i, new MersenneTwister(nextSeed++));
			usedSeeds.put(i, new HashSet<Integer>());
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.probleminstance.InstanceSeedGenerator#getNextSeed(ca.ubc.cs.beta.ac.config.ProblemInstance)
	 */
	@Override
	public int getNextSeed(ProblemInstance pi)
	{
		if(hasNextSeed(pi))
		{
			return getNextSeed(pi.getInstanceID()-1);
		} else
		{
			throw new IllegalStateException("No more Seeds for Problem Instance: " + pi.getInstanceName());
		}
	}
	
	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.probleminstance.InstanceSeedGenerator#getNextSeed(java.lang.Integer)
	 */
	@Override
	public int getNextSeed(Integer id)
	{
		
		Random r = randomPool.get(id);
		
		if(r == null)
		{
			log.warn("Received ID that I haven't seed before: {}", id);
			r = new MersenneTwister(nextSeed++);
			randomPool.put(id,r);
			usedSeeds.put(id, new HashSet<Integer>());
		}
		
		Set<Integer> generatedSeeds = usedSeeds.get(id);
		
		int i=0;
		do
		{
			i = r.nextInt(256*256*256);
		} while(!generatedSeeds.add(i));
		
		return i;
	}
	
	
	/* (non-Javadoc)
	 * @see ca.ubc.cs.beta.probleminstance.InstanceSeedGenerator#hasNextSeed(ca.ubc.cs.beta.ac.config.ProblemInstance)
	 */
	@Override
	public boolean hasNextSeed(ProblemInstance pi)
	{
		if(usedSeeds.get(pi.getInstanceID()-1) == null )
		{
			return 1 <= maxSeedsPerInstance;
		}
		if(usedSeeds.get(pi.getInstanceID()-1).size() >= maxSeedsPerInstance)
		{
			return false;
		} else
		{
			return true;
		}
	}


	@Override
	public List<ProblemInstance> getProblemInstanceOrder(
			Collection<ProblemInstance> instances) {
		SortedMap<Integer, ProblemInstance> instanceMap = new TreeMap<Integer, ProblemInstance>();
		
		for(ProblemInstance pi : instances)
		{
			instanceMap.put(pi.getInstanceID(), pi);
		}
		
		List<ProblemInstance> instanceList = new ArrayList<ProblemInstance>();
		
		
		instanceList.addAll(instanceMap.values());
		
		return instanceList;
		
		
		
		
	}
	
	
}
