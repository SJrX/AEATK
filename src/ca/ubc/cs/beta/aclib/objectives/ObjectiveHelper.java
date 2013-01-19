package ca.ubc.cs.beta.aclib.objectives;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;

public class ObjectiveHelper {

	private final RunObjective runObj;
	private final OverallObjective intraObjective;
	private final OverallObjective interObjective;
	private final double cutoffTime;

	public ObjectiveHelper(RunObjective runObj,OverallObjective intraObjective , OverallObjective interObjective, double cutoffTime)
	{
		this.runObj = runObj;
		this.intraObjective = intraObjective;
		this.interObjective = interObjective;
		this.cutoffTime = cutoffTime;
	}
	
	/**
	 * Too lazy to pass this object around properly, you can delete this method and refactor what breaks
	 * @return
	 */
	public RunObjective getRunObjective()
	{
		return runObj;
	}
	/**
	 * Computes the objective for a given set of runs 
	 * @param runs				- a set of runs that all have the same configuration
	 * @param capSlack 
	 * @param runObj			- Run Objective
	 * @param intraObjective	- Intra Instance Objective
	 * @param interObjective	- Inter Instance Objective
	 * @return
	 */
	public double computeObjective(List<? extends AlgorithmRun> runs, final double capSlack)
	{
		
		List<ProblemInstance> instances = new ArrayList<ProblemInstance>(runs.size());
		ConcurrentHashMap<ProblemInstance, List<ProblemInstanceSeedPair>> map = new ConcurrentHashMap<ProblemInstance, List<ProblemInstanceSeedPair>>();
		ConcurrentHashMap<ProblemInstance, List<Double>> performance = new ConcurrentHashMap<ProblemInstance, List<Double>>();
		
		
		double remainingCapSlack = capSlack;
		for(AlgorithmRun run : runs)
		{
			ProblemInstance pi = run.getRunConfig().getProblemInstanceSeedPair().getInstance();
			
			instances.add(pi);
			map.putIfAbsent(pi,new ArrayList<ProblemInstanceSeedPair>());
			performance.putIfAbsent(pi,new ArrayList<Double>());
		
			map.get(pi).add(run.getRunConfig().getProblemInstanceSeedPair());
			
			double obj = runObj.getObjective(run);
			obj -= remainingCapSlack;
			
			if(obj < 0)
			{ //Remaining slack left over
				remainingCapSlack = -obj;
				obj = 0;
			} else
			{
				remainingCapSlack = 0;
			}
			
			performance.get(pi).add(obj);
			
		}
		
		
		
		List<Double> intraInstanceObjectiveValues = new ArrayList<Double>(instances.size());
		
		for(Entry<ProblemInstance, List<Double>> prefEnt : performance.entrySet())
		{
			intraInstanceObjectiveValues.add(intraObjective.aggregate(prefEnt.getValue(), cutoffTime));			
		}
		
		
		
		return interObjective.aggregate(intraInstanceObjectiveValues, cutoffTime);
	}

	public double computeObjective(List<? extends AlgorithmRun> runs) {
		return computeObjective(runs,0);
	}
	
	
/*
	public double getEmpiricalCost(ParamConfiguration config, Set<ProblemInstance> instanceSet, double cutoffTime, Map<ProblemInstance, Map<Long,Double>> hallucinatedValues, double minimumResponseValue)
	{
		if (!configToPerformanceMap.containsKey(config) && hallucinatedValues.isEmpty()){
			return Double.MAX_VALUE;
		}
		ArrayList<Double> instanceCosts = new ArrayList<Double>();
		
		Map<ProblemInstance, LinkedHashMap<Long, Double>> instanceSeedToPerformanceMap = configToPerformanceMap.get(config);

		if(instanceSeedToPerformanceMap == null) 
		{
			instanceSeedToPerformanceMap = new HashMap<ProblemInstance, LinkedHashMap<Long, Double>>();
			
		}
		/*
		 * Compute the Instances to use in the cost calculation
		 * It's everything we ran out of everything we requested.
		 *
		Set<ProblemInstance> instancesToUse = new HashSet<ProblemInstance>();
		instancesToUse.addAll(instanceSet);
		
		Set<ProblemInstance> instancesToKeep = new HashSet<ProblemInstance>(instanceSeedToPerformanceMap.keySet());
		instancesToKeep.addAll(hallucinatedValues.keySet());
		instancesToUse.retainAll(instancesToKeep);
		
		
		for(ProblemInstance pi : instancesToUse)
		{
			
			Map<Long, Double> seedToPerformanceMap = new HashMap<Long, Double>();
			if(instanceSeedToPerformanceMap.get(pi) != null) seedToPerformanceMap.putAll(instanceSeedToPerformanceMap.get(pi));
			if(hallucinatedValues.get(pi) != null) seedToPerformanceMap.putAll(hallucinatedValues.get(pi));
			
			/*
			 * Aggregate the cost over the instances
			 *
			ArrayList<Double> localCosts = new ArrayList<Double>();
			for(Map.Entry<Long, Double> ent : seedToPerformanceMap.entrySet())
			{
					localCosts.add( Math.max(minimumResponseValue, ent.getValue()) );	
			}
			instanceCosts.add( perInstanceObjectiveFunction.aggregate(localCosts,cutoffTime)); 
		}
		return aggregateInstanceObjectiveFunction.aggregate(instanceCosts,cutoffTime);
	}
	*/
}
