package ca.ubc.cs.beta.smac.ac;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.ac.config.ProblemInstance;
import ec.util.MersenneTwister;

public class InstanceSeedGenerator implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5545604876585367506L;
	
	private final HashMap<Integer, Random> randomPool;
	private final HashMap<Integer, Set<Integer>> usedSeeds;
	
	//
	private static transient final Logger log = LoggerFactory.getLogger(InstanceSeedGenerator.class);
	
	private long nextSeed;
	private final long initialSeed;
	private final int initialNumberOfInstances;
	
	
	
	public InstanceSeedGenerator(List<ProblemInstance> instances, long seed)
	{
		this(instances.size(), seed);
	}
	
	public InstanceSeedGenerator(int numberOfInstances, long seed)
	{
		log.debug("Initializing Instance Seed PRNG with Seed {}", seed);
		randomPool = new HashMap<Integer, Random>();
		usedSeeds = new HashMap<Integer, Set<Integer>>();
		initialSeed = seed;
		initialNumberOfInstances = numberOfInstances;
		
		reinit();
	}
	
	public void reinit()
	{
		log.info("Re-Initializing Instance Seed PRNG with Seed {}", initialSeed);	
		nextSeed = initialSeed;
		randomPool.clear();
		usedSeeds.clear();
		
		for(int i=0; i < initialNumberOfInstances; i++)
		{
			randomPool.put(i, new MersenneTwister(nextSeed++));
			usedSeeds.put(i, new HashSet<Integer>());
		}
		
	}
	
	
	public int getNextSeed(ProblemInstance pi)
	{
		return getNextSeed(pi.getInstanceID());
	}
	
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
	
	
	public boolean hasNextSeed(ProblemInstance pi)
	{
		return true;
	}
	
	
}
