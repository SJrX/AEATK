package ca.ubc.cs.beta.aclib.runhistory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.seedgenerator.InstanceSeedGenerator;
import ca.ubc.cs.beta.aclib.seedgenerator.SetInstanceSeedGenerator;

public class RunHistoryHelper{

	private static Logger log =  LoggerFactory.getLogger(RunHistoryHelper.class);
	
	

	

	 /**
	 * Returns a random instance with the fewest runs for the configuration
	 * @param config  		ParamConfiguration to run
	 * @param instanceList  List of problem instances.
	 * @param rand			Random object used to break ties
	 * @return random instance with the fewest runs for a configuration
	 */
	protected static ProblemInstance getRandomInstanceWithFewestRunsFor(RunHistory rh,ParamConfiguration config, List<ProblemInstance> instanceList, Random rand) {

		Map<ProblemInstance, LinkedHashMap<Long, Double>> instanceSeedToPerformanceMap = rh.getPerformanceForConfig(config);
		
		/*
		 * First try and see if if there are some candidate instances with zero runs
		 */
		List<ProblemInstance> candidates = new ArrayList<ProblemInstance>(instanceList.size());
		candidates.addAll(instanceList);
		if (instanceSeedToPerformanceMap != null){
			//Allegedly this is a very slow operation (http://www.ahmadsoft.org/articles/removeall/index.html)
			candidates.removeAll(instanceSeedToPerformanceMap.keySet());
		}
		
		/*
		 * If not find the set with the smallest number of runs
		 */
		if (candidates.size() == 0){
			int minNumRuns = Integer.MAX_VALUE;
			for (Iterator<ProblemInstance> iterator = instanceList.iterator(); iterator.hasNext();) {
				ProblemInstance inst = iterator.next();
				int numRuns = instanceSeedToPerformanceMap.get(inst).size();
				if (numRuns <= minNumRuns){
					if (numRuns < minNumRuns){ // new value for fewest runs -> ditch all previous candidates
						candidates.clear();
						minNumRuns = numRuns;
					}
					candidates.add(inst);
				}
			}
		}
		
		//=== Return a random element of the candidate instance set (it's sad there is no method for that in Java's Set).\
		int candidateIdx = rand.nextInt(candidates.size());
		
		//log.error("Always selecting first instance");
		//int candidateIdx =0;
		
		log.trace("Selected Instance {}", candidates.get(candidateIdx));
		return candidates.get(candidateIdx);	
	}


	/**
	 * Determines a ProblemInstanceSeedPair to run for configuration subject to keeping all problem instances run within 1 of eachother
	 * 
	 * @param rh 			 			Thread Safe RunHistory object
	 * @param instanceSeedGenerator     Instance Seed Generator
	 * @param config			 		ParamConfiguration to run 
	 * @param instanceList 	 			List of problem instances
	 * @param rand					 	Random object used to break ties
	 * @return Random ProblemInstanceSeedPair object
	 */
	public static ProblemInstanceSeedPair getRandomInstanceSeedWithFewestRunsFor( ThreadSafeRunHistory rh, InstanceSeedGenerator instanceSeedGenerator, ParamConfiguration config, List<ProblemInstance> instanceList, Random rand)
	{
		try {
			rh.readLock();
			return getRandomInstanceSeedWithFewestRunsFor((RunHistory) rh, instanceSeedGenerator, config, instanceList, rand);
		} finally
		{
			rh.releaseReadLock();
		}
	}
	
	/**
	 * Determines a ProblemInstanceSeedPair to run for configuration subject to keeping all problem instances run within 1 of eachother
	 * 
	 * @param rh 			 			Thread Safe RunHistory object
	 * @param instanceSeedGenerator     Instance Seed Generator
	 * @param config 		 ParamConfiguration to run 
	 * @param instanceList 	 List of problem instances
	 * @param rand			 Random object used to break ties
	 * @return Random ProblemInstanceSeedPair object
	 */
	public static ProblemInstanceSeedPair getRandomInstanceSeedWithFewestRunsFor( RunHistory rh, InstanceSeedGenerator instanceSeedGenerator, ParamConfiguration config, List<ProblemInstance> instanceList, Random rand) {
		ProblemInstance pi = getRandomInstanceWithFewestRunsFor(rh, config, instanceList, rand);
		Map<ProblemInstance, LinkedHashMap<Long, Double>> instanceSeedToPerformanceMap = rh.getPerformanceForConfig(config);
		
		
		List<Long> seedsUsedByPi = rh.getSeedsUsedByInstance(pi);
		
		Set<Long> seedsUsedByPiConfigSet;
		if(instanceSeedToPerformanceMap == null || instanceSeedToPerformanceMap.get(pi) == null) 
		{ 
			seedsUsedByPiConfigSet = Collections.emptySet();
		} else
		{
			seedsUsedByPiConfigSet= instanceSeedToPerformanceMap.get(pi).keySet();
		}
		
		List<Long> seedsUsedByPiConfig = new ArrayList<Long>(seedsUsedByPiConfigSet.size());
				
		for(Long seed : seedsUsedByPiConfigSet)
		{
			seedsUsedByPiConfig.add(seed);
		}
		 
		
		List<Long> potentialSeeds = new ArrayList<Long>(seedsUsedByPi.size() - seedsUsedByPiConfig.size());
		
		potentialSeeds.addAll(seedsUsedByPi);
		potentialSeeds.removeAll(seedsUsedByPiConfig);
		
		long seed;
		if(potentialSeeds.size() == 0)
		{
		
			synchronized(instanceSeedGenerator)
			{	
				//We generate only positive seeds
				if(instanceSeedGenerator instanceof SetInstanceSeedGenerator)
				{
					if(instanceSeedGenerator.hasNextSeed(pi))
					{
						seed = instanceSeedGenerator.getNextSeed(pi); 
					} else
					{
						seed = -1;
					}
				} else
				{
					seed = instanceSeedGenerator.getNextSeed(pi); 
				}
				
			}
		} else
		{
			seed = potentialSeeds.get(rand.nextInt(potentialSeeds.size()));
		}
		ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, seed);
		log.trace("New Problem Instance Seed Pair Selected {}", pisp );
		return pisp;
	}
	
	

	
}
