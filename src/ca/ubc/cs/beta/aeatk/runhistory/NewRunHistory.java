package ca.ubc.cs.beta.aeatk.runhistory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.jcip.annotations.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.RunResult;
import ca.ubc.cs.beta.aeatk.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aeatk.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aeatk.objectives.OverallObjective;
import ca.ubc.cs.beta.aeatk.objectives.RunObjective;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.seedgenerator.InstanceSeedGenerator;

/**
 * Stores a list of runs that have been completed in multiple ways
 * that make it easier to query for certain kinds of information / comparsions.
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
@SuppressWarnings("unused")
@NotThreadSafe
public class NewRunHistory implements RunHistory {


	/**
	 * Objective function that allows us to aggregate various instance / seed pairs to 
	 * give us a value for the instance
	 */
	private final OverallObjective perInstanceObjectiveFunction;
	
	/**
	 * Objective function that allows us to aggeregate various instances (aggregated by the perInstanceObjectiveFunction), 
	 * to determine a cost for the set of instances.
	 */
	private final OverallObjective aggregateInstanceObjectiveFunction;
	
	/**
	 * Objective function that determines the response value from a run.
	 */
	private final RunObjective runObj;
	
	/**
	 * Current iteration we are on
	 */
	private int iteration = 0;

	/**
	 * Stores a list of Parameter Configurations along with there associted thetaIdx
	 */
	private final KeyObjectManager<ParamConfiguration> paramConfigurationList = new KeyObjectManager<ParamConfiguration>();
	
	/**
	 * Stores a list of RunData
	 */
	private final List<RunData> runHistoryList = new ArrayList<RunData>();
	
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	/**
	 * Stores the sum of all the runtimes
	 */
	private double totalRuntimeSum = 0;
	
	/**
	 * Stores for each configuration a mapping of instances to a map of seeds => response values
	 * 
	 * We store the Seeds in a LinkedHashMap because the order of them matters as far as determining which seed to pick to run
	 * as far as Matlab synchronizing goes. Otherwise it could be a regular map
	 */
	private final Map<ParamConfiguration, Map<ProblemInstance, LinkedHashMap<Long, Double>>> configToPerformanceMap =
			new HashMap<ParamConfiguration, Map<ProblemInstance, LinkedHashMap<Long, Double>>>();
	
	/**
	 * Stores for each instance the list of seeds used 
	 */
	private final HashMap<ProblemInstance, List<Long>> seedsUsedByInstance = new HashMap<ProblemInstance, List<Long>>();
	
	/**
	 * Stores the number of times a config has been run
	 */
	private final LinkedHashMap<ParamConfiguration, Integer> configToNumRunsMap = new LinkedHashMap<ParamConfiguration, Integer>();
	
	/**
	 * Stores the number of times a config has been run
	 */
	private final LinkedHashMap<ParamConfiguration, Integer> configToNumRunsIgnoringRedundantMap = new LinkedHashMap<ParamConfiguration, Integer>();
	
	/**
	 * Stores a list of Instance Seed Pairs whose runs were capped.
	 */
	private final HashMap<ParamConfiguration, Set<ProblemInstanceSeedPair>> censoredEarlyRuns = new HashMap<ParamConfiguration, Set<ProblemInstanceSeedPair>>(); 
	
	/**
	 * Stores the set of instances we have run
	 */
	private Set<ProblemInstance> instancesRanSet = new HashSet<ProblemInstance>();
	
	private final HashMap<ParamConfiguration, List<AlgorithmRun>> configToRunMap = new HashMap<ParamConfiguration, List<AlgorithmRun>>();
	
	/**
	 * Stores for each run the best known value at a given time. We use a linked hash map because order is important.
	 */
	private final LinkedHashMap<ParamConfiguration, LinkedHashMap<ProblemInstanceSeedPair,AlgorithmRun>> configToRunIgnoreRedundantMap = new LinkedHashMap<ParamConfiguration, LinkedHashMap<ProblemInstanceSeedPair,AlgorithmRun>>();
	
	private static final DecimalFormat format = new DecimalFormat("#######.####");
	
	/**
	 * Creates NewRunHistory object
	 * @param intraInstanceObjective	intraInstanceObjective to use when calculating costs
	 * @param interInstanceObjective	interInstanceObjective to use when calculating costs
	 * @param runObj					run objective to use 
	 */
	public NewRunHistory( OverallObjective intraInstanceObjective,  OverallObjective interInstanceObjective, RunObjective runObj)
	{
		this.perInstanceObjectiveFunction = intraInstanceObjective;
		this.aggregateInstanceObjectiveFunction = interInstanceObjective;
		this.runObj = runObj;
	
	}
	
	
	private volatile AlgorithmExecutionConfiguration firstExecConfig;
	
	@Override
	public void append(AlgorithmRun run) throws DuplicateRunException{

		
		
		if(firstExecConfig == null)
		{
			this.firstExecConfig = run.getRunConfig().getAlgorithmExecutionConfig();
		} else
		{
			if(!this.firstExecConfig.equals(run.getRunConfig().getAlgorithmExecutionConfig()))
			{
				throw new IllegalArgumentException("RunHistory object cannot store runs for different exec configs first was: " + firstExecConfig + " current run was : " + run.getRunConfig().getAlgorithmExecutionConfig());
			}
		}
		
		if(run.getRunResult().equals(RunResult.RUNNING))
		{
			throw new IllegalArgumentException("Runs with Run Result RUNNING cannot be saved to a RunHistory object");
		}
		ParamConfiguration config = run.getRunConfig().getParamConfiguration();
		ProblemInstanceSeedPair pisp = run.getRunConfig().getProblemInstanceSeedPair();
		ProblemInstance pi = pisp.getInstance();
		long seed = run.getResultSeed();
		
		Double runResult = runObj.getObjective(run);
		
		/**
		 * Add run data to the list of seeds used by Instance
		 */
		List<Long> instanceSeedList = seedsUsedByInstance.get(pi);
		if(instanceSeedList == null)
		{ //Initialize List if non existant
			instanceSeedList = new LinkedList<Long>();
			seedsUsedByInstance.put(pi,instanceSeedList);
		}
		instanceSeedList.add(seed);

		Map<ProblemInstance, LinkedHashMap<Long, Double>> instanceToPerformanceMap = configToPerformanceMap.get(config);
		if(instanceToPerformanceMap == null)
		{ //Initialize Map if non-existant
			instanceToPerformanceMap = new HashMap<ProblemInstance, LinkedHashMap<Long,Double>>();
			configToPerformanceMap.put(config,instanceToPerformanceMap);
		}
		
		LinkedHashMap<Long, Double> seedToPerformanceMap = instanceToPerformanceMap.get(pi);
		if(seedToPerformanceMap == null)
		{ //Initialize Map if non-existant
			seedToPerformanceMap = new LinkedHashMap<Long, Double>();
			instanceToPerformanceMap.put(pi, seedToPerformanceMap);
		}
		
		Double dOldValue = seedToPerformanceMap.put(seed,runResult);
		
		RunResult result = run.getRunResult();
		
		boolean censoredEarly = run.isCensoredEarly();
		
		if(configToRunIgnoreRedundantMap.get(config) == null)
		{
			configToRunIgnoreRedundantMap.put(config, new LinkedHashMap<ProblemInstanceSeedPair, AlgorithmRun>());
		}
		
		
		
		if(dOldValue != null)
		{
			//If the value already existed then either
			//we have a duplicate run OR the previous run was capped
			
			Set<ProblemInstanceSeedPair> censoredEarlyRunsForConfig = censoredEarlyRuns.get(config);

			if((censoredEarlyRunsForConfig != null) && censoredEarlyRunsForConfig.contains(pisp))
			{
				//We remove it now and will re-add it if this current run was capped
				censoredEarlyRunsForConfig.remove(pisp); 
			} else
			{
				AlgorithmRun matchingRun = null;
				for(AlgorithmRun algoRun : this.getAlgorithmRunsExcludingRedundant(config))
				{
					if(algoRun.getRunConfig().getProblemInstanceSeedPair().equals(run.getRunConfig().getProblemInstanceSeedPair()))
					{
						matchingRun = algoRun;
					}
				}
							
				//Restores the state of the RunHistory object to essentially idential to when we found it.
				seedToPerformanceMap.put(seed, dOldValue);
				
				Object[] args = {matchingRun, run, config, pi,dOldValue};				
				
				//log.error("RunHistory already contains a run with identical config, instance and seed \n Original Run:{}\nRun:{}\nConfig:{}\nInstance:{}\nPrevious Performance:{}", args);
				throw new DuplicateRunException("Duplicate Run Detected", run);
			}
			
			
			if(this.runObj != RunObjective.RUNTIME)
			{
				log.error("Not sure how to rectify early censored runs under different run objectives, current run seems to conflict with a previous one: {} " , run);
				throw new IllegalStateException("Unable to handle capped runs for the RunObjective: " + runObj);
			} else
			{
				//We know that both the previous and current result must be censored early, so we take the maximimum
				if(censoredEarly)
				{	
					
					seedToPerformanceMap.put(seed, Math.max(dOldValue, runResult));
					
					if(dOldValue < runResult)
					{
						this.configToRunIgnoreRedundantMap.get(config).put(pisp, run);
					}
				}
			}

		} else
		{ 
			//Haven't seen this run before, so we have new data
			if(configToNumRunsIgnoringRedundantMap.get(config) == null)
			{
				configToNumRunsIgnoringRedundantMap.put(config, Integer.valueOf(1));
			} else
			{
				configToNumRunsIgnoringRedundantMap.put(config, configToNumRunsIgnoringRedundantMap.get(config) +1);
			}
			
			this.configToRunIgnoreRedundantMap.get(config).put(pisp, run);
			
		}
		
	
		
		if(this.configToRunMap.get(config) == null)
		{
			this.configToRunMap.put(config, new ArrayList<AlgorithmRun>());
		}
		
		this.configToRunMap.get(config).add(run);
		totalRuntimeSum += Math.max(0.1, run.getRuntime());
		
		/*
		 * Add data to the run List
		 */
		int thetaIdx = paramConfigurationList.getOrCreateKey(config);
		
	
		
		int instanceIdx = pi.getInstanceID();
		
	
		runHistoryList.add(new RunData(iteration, thetaIdx, instanceIdx, run,runResult, censoredEarly));
		
		
		/*
		 * Increment the config run counter
		 */
		if(configToNumRunsMap.get(config) == null)
		{
			configToNumRunsMap.put(config, Integer.valueOf(1));
		} else
		{
			configToNumRunsMap.put(config, configToNumRunsMap.get(config) +1);
		}
		
		/*
		 * Add Instance to the set of instances ran 
		 */
		instancesRanSet.add(pi);
		
		/*
		 * Add to the capped runs set
		 */
		if(censoredEarly)
		{
			if(!censoredEarlyRuns.containsKey(config))
			{
				censoredEarlyRuns.put(config, new LinkedHashSet<ProblemInstanceSeedPair>());
			}
				
			censoredEarlyRuns.get(config).add(pisp);
		}
		
		Object[] args = {iteration, paramConfigurationList.getKey(config), pi.getInstanceID(), pisp.getSeed(), format.format(run.getRunConfig().getCutoffTime())};
		
		
		
		//
	
		
	}

	
	
	@Override
	public double getEmpiricalCost(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime)
	{
		Map<ProblemInstance, Map<Long, Double>> foo = Collections.emptyMap();
		return getEmpiricalCost(config, instanceSet, cutoffTime, foo);
	}
	
	@Override
	public double getEmpiricalCost(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime, double minimumResponseValue)
	{
		Map<ProblemInstance, Map<Long, Double>> foo = Collections.emptyMap();
		return getEmpiricalCost(config, instanceSet, cutoffTime, foo, minimumResponseValue);
	}
	
	
	public double getEmpiricalCost(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime, Map<ProblemInstance, Map<Long,Double>> hallucinatedValues)
	{
		return getEmpiricalCost(config, instanceSet, cutoffTime, hallucinatedValues, Double.NEGATIVE_INFINITY);
	}
	
	@Override
	public double getEmpiricalCostLowerBound(ParamConfiguration config,	Set<ProblemInstance> instanceSet, double cutoffTime) 
	{
		
		return getEmpiricalCostBound(config, instanceSet, cutoffTime, 0.0, Bound.LOWER);
		
	}

	@Override
	public double getEmpiricalCostUpperBound(ParamConfiguration config,	Set<ProblemInstance> instanceSet, double cutoffTime) 
	{	
		return getEmpiricalCostBound(config, instanceSet, cutoffTime, cutoffTime, Bound.UPPER);

	}
	
	private enum Bound{
		UPPER,
		LOWER
	}
	
	private double getEmpiricalCostBound(ParamConfiguration config, Set<ProblemInstance> instanceSet, double cutoffTime, Double boundValue, Bound b)
	{
		Map<ProblemInstance, Map <Long,Double>> hallucinatedValues = new HashMap<ProblemInstance,Map<Long, Double>>();
		
		Set<ProblemInstanceSeedPair> earlyCensoredPISPs = this.getEarlyCensoredProblemInstanceSeedPairs(config);
		
		for(ProblemInstance pi : instanceSet)
		{
			Map<Long,Double> instPerformance =  new HashMap<Long, Double>(); 
			hallucinatedValues.put(pi, instPerformance);
			
			//Pass one is to put the bound value in every necessary slot.
			
			if(seedsUsedByInstance.get(pi) == null)
			{
				seedsUsedByInstance.put(pi, new ArrayList<Long>());
			}
			for(Long l : seedsUsedByInstance.get(pi))
			{
				instPerformance.put(l, boundValue);
			}

			//Pass two puts the observed performance in the appropriate slot.
			Map<Long, Double> actualPerformance = new HashMap<Long, Double>();

			
			if(this.configToPerformanceMap.get(config) != null)
			{
				instPerformance.putAll(this.configToPerformanceMap.get(config).get(pi));
			}
			
			//Pass three rounds early censored values back up to the cuttoff time
			if(b == Bound.UPPER)
			{
				for(Long l : seedsUsedByInstance.get(pi))
				{
					ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, l);
					if(earlyCensoredPISPs.contains(pisp))
					{
						instPerformance.put(l, boundValue);
					}
				}
				
				
			} 
			
			
			if(instPerformance.size() == 0)
			{
				//== We insert a bound value  if we have nothing, because of the way getEmipricalCost is implemented.
				instPerformance.put(Long.MIN_VALUE, boundValue);
			}
			 
			
			
		}
		
		return getEmpiricalCost(config, instanceSet, cutoffTime, hallucinatedValues, 0);

	}
	@Override
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
		 */
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
			 */
			ArrayList<Double> localCosts = new ArrayList<Double>();
			for(Map.Entry<Long, Double> ent : seedToPerformanceMap.entrySet())
			{
					localCosts.add( Math.max(minimumResponseValue, ent.getValue()) );	
			}
			instanceCosts.add( perInstanceObjectiveFunction.aggregate(localCosts,cutoffTime)); 
		}
		return aggregateInstanceObjectiveFunction.aggregate(instanceCosts,cutoffTime);
	}

	@Override
	public RunObjective getRunObjective() {
		return runObj;
	}

	@Override
	public OverallObjective getOverallObjective() {
	
		return perInstanceObjectiveFunction;
	}

	@Override
	public void incrementIteration() {
		iteration++;

	}

	@Override
	public int getIteration() {
		return iteration;
	}

	@Override
	public Set<ProblemInstance> getProblemInstancesRan(ParamConfiguration config) {
		if (!configToPerformanceMap.containsKey(config)){
			return new HashSet<ProblemInstance>();
		}
		return new HashSet<ProblemInstance>( configToPerformanceMap.get(config).keySet() );
	}

	@Override
	public Set<ProblemInstanceSeedPair> getProblemInstanceSeedPairsRan(ParamConfiguration config) {
		if (!configToPerformanceMap.containsKey(config)){
			return new HashSet<ProblemInstanceSeedPair>();
		}
		Set<ProblemInstanceSeedPair> pispSet = new HashSet<ProblemInstanceSeedPair>();		
		Map<ProblemInstance, LinkedHashMap<Long, Double>> instanceSeedToPerformanceMap = configToPerformanceMap.get(config);
		
		for (Entry<ProblemInstance, LinkedHashMap<Long, Double>> kv : instanceSeedToPerformanceMap.entrySet()) {
			ProblemInstance pi =  kv.getKey();
			Map<Long, Double> hConfigInst = kv.getValue();
			for (Long seed: hConfigInst.keySet()) {
				pispSet.add( new ProblemInstanceSeedPair(pi, seed) );
			}
		}
		return pispSet;
	}

	@Override
	public Set<ProblemInstanceSeedPair> getEarlyCensoredProblemInstanceSeedPairs(ParamConfiguration config)
	{
		if(!censoredEarlyRuns.containsKey(config))
		{
			return Collections.emptySet();
		}
		
		return Collections.unmodifiableSet(censoredEarlyRuns.get(config));
	}

	

	@Override
	public double getTotalRunCost() {
		return totalRuntimeSum;
	}

	

	@Override
	public Set<ProblemInstance> getUniqueInstancesRan() {
		return Collections.unmodifiableSet(instancesRanSet);
	}

	@Override
	public Set<ParamConfiguration> getUniqueParamConfigurations() {
		return Collections.unmodifiableSet(configToNumRunsMap.keySet());
	}

	@Override
	public int[][] getParameterConfigurationInstancesRanByIndex() {
		int[][] result = new int[runHistoryList.size()][2];
		
		int i=0; 
		for(RunData runData : runHistoryList)
		{
			result[i][0] = runData.getThetaIdx();
			result[i][1] = runData.getInstanceidx();
			i++;
		}
		
		return result;
	}

	@Override
	public List<ParamConfiguration> getAllParameterConfigurationsRan() {
		List<ParamConfiguration> runs = new ArrayList<ParamConfiguration>(paramConfigurationList.size());
		
		for(int i=1; i <= paramConfigurationList.size(); i++)
		{
			runs.add(paramConfigurationList.getValue(i));
		}
		return runs;
	}

	@Override
	public double[][] getAllConfigurationsRanInValueArrayForm() {
		double[][] configs = new double[paramConfigurationList.size()][];
		for(int i=1; i <= paramConfigurationList.size(); i++)
		{
			configs[i-1] = paramConfigurationList.getValue(i).toValueArray();
		}
	
		return configs;
	}

	
	

	@Override
	public List<RunData> getAlgorithmRunData() {
		return Collections.unmodifiableList(runHistoryList);
	}


	@Override
	public int getThetaIdx(ParamConfiguration config) {
		Integer thetaIdx = paramConfigurationList.getKey(config);
		if(thetaIdx == null)
		{
			return -1;
		} else
		{
			return thetaIdx;
		}
		
	}
	
	@Override
	public int getOrCreateThetaIdx(ParamConfiguration config) {
		 return paramConfigurationList.getOrCreateKey(config);
		
	}
	

	@Override
	public int getNumberOfUniqueProblemInstanceSeedPairsForConfiguration(ParamConfiguration config)
	{
		 Map<ProblemInstance, LinkedHashMap<Long, Double>> runs = configToPerformanceMap.get(config);
		 
		 int total =0;
		 for(Entry<ProblemInstance, LinkedHashMap<Long, Double>> ent : runs.entrySet())
		 {
			 total+=ent.getValue().size();
		 }
		 
		 return total;
		
	}
	
	@Override
	public Map<ProblemInstance, LinkedHashMap<Long, Double>> getPerformanceForConfig(ParamConfiguration config)
	{
		Map<ProblemInstance, LinkedHashMap<Long,Double>> map =  configToPerformanceMap.get(config);
		if(map != null)
		{
			return Collections.unmodifiableMap(map);
		} else
		{
			return Collections.emptyMap();
		}
	}
	

	@Override
	public int getTotalNumRunsOfConfigIncludingRedundant(ParamConfiguration config) {
		Integer value = configToNumRunsMap.get(config);
		if( value != null)
		{
			return value;
		} else
		{
			return 0;
		}
	}
	
	@Override
	public int getTotalNumRunsOfConfigExcludingRedundant(ParamConfiguration config) {
		Integer value = configToNumRunsIgnoringRedundantMap.get(config);
		if( value != null)
		{
			return value;
		} else
		{
			return 0;
		}
		
	}
	
	@Override
	public List<AlgorithmRun> getAlgorithmRunsExcludingRedundant() {
		
		List<AlgorithmRun> list = new ArrayList<AlgorithmRun>();
		for(LinkedHashMap<ProblemInstanceSeedPair, AlgorithmRun> lhm : this.configToRunIgnoreRedundantMap.values())
		{
			list.addAll(lhm.values());
		}
		
		return list;
	}
	
	@Override
	/**
	 * Get a list of algorithm runs we have used
	 * 
	 * Slow O(n) method to generate a list of Algorithm Runs
	 * We could speed this up but at this point we only do this for restoring state
	 * @return list of algorithm runs we have recieved
	 */
	public List<AlgorithmRun> getAlgorithmRunsIncludingRedundant() 
	{
		
		List<AlgorithmRun> runs = new ArrayList<AlgorithmRun>(this.runHistoryList.size());
		for(RunData runData : getAlgorithmRunData() )
		{
			runs.add(runData.getRun());
		}
		return runs;
	}
	
	@Override
	public List<AlgorithmRun> getAlgorithmRunsIncludingRedundant(ParamConfiguration config) {
		
		List<AlgorithmRun> runs = this.configToRunMap.get(config);
		
		if(runs != null)
		{
			return Collections.unmodifiableList(runs);
		} else
		{
			return Collections.emptyList();
		}
	}
	
	@Override
	public List<AlgorithmRun> getAlgorithmRunsExcludingRedundant(ParamConfiguration config)
	{
		Map<ProblemInstanceSeedPair, AlgorithmRun> runs = this.configToRunIgnoreRedundantMap.get(config);
		
		if(runs == null)
		{
			runs = Collections.emptyMap();
		}
		
		return Collections.unmodifiableList(new ArrayList<AlgorithmRun>(runs.values()));
	}
	
	
	

	@Override
	public List<Long> getSeedsUsedByInstance(ProblemInstance pi) 
	{

		if(seedsUsedByInstance.get(pi) == null)
		{
			seedsUsedByInstance.put(pi, new ArrayList<Long>());
		}
		return Collections.unmodifiableList(seedsUsedByInstance.get(pi));
	}

	

	




}
