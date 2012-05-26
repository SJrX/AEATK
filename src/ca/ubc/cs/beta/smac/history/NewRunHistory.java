package ca.ubc.cs.beta.smac.history;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.ac.RunResult;
import ca.ubc.cs.beta.ac.config.ProblemInstance;
import ca.ubc.cs.beta.ac.config.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.configspace.ParamConfiguration;
import ca.ubc.cs.beta.probleminstance.InstanceSeedGenerator;
import ca.ubc.cs.beta.smac.OverallObjective;
import ca.ubc.cs.beta.smac.RunObjective;
import ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun;

/**
 * THIS CLASS IS NOT THREAD SAFE!!!
 * @author seramage
 *
 */
public class NewRunHistory implements RunHistory {

	
	/**
	 * Generates seeds for given instances in sequence
	 */
	private final InstanceSeedGenerator instanceSeedGenerator;
	
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
	private final List<RunData> runHistoryList = new LinkedList<RunData>();
	
	
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
	 * Stores a list of Instance Seed Pairs whose runs were capped.
	 */
	private final HashMap<ParamConfiguration, Set<ProblemInstanceSeedPair>> cappedRuns = new HashMap<ParamConfiguration, Set<ProblemInstanceSeedPair>>(); 
	
	
	private Set<ProblemInstance> instancesRanSet = new HashSet<ProblemInstance>();
	
	
	public NewRunHistory(InstanceSeedGenerator instanceSeedGenerator, OverallObjective perInstanceObjectiveFunction,  OverallObjective aggregateInstanceObjectiveFunction, RunObjective runObj)
	{
		this.instanceSeedGenerator = instanceSeedGenerator;
		this.perInstanceObjectiveFunction = perInstanceObjectiveFunction;
		this.aggregateInstanceObjectiveFunction = aggregateInstanceObjectiveFunction;
		this.runObj = runObj;
	
		
		
	}
	
	@Override
	public void append(AlgorithmRun run) {

		log.trace("Appending Run {}",run);
		
		ParamConfiguration config = run.getInstanceRunConfig().getParamConfiguration();
		ProblemInstanceSeedPair pisp = run.getInstanceRunConfig().getAlgorithmInstanceSeedPair();
		ProblemInstance pi = pisp.getInstance();
		long seed = run.getResultSeed();
		
		Double runResult = runObj.getObjective(run);
		
		totalRuntimeSum += Math.max(0.1, RunObjective.RUNTIME.getObjective(run));
		
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
		
		if(dOldValue != null)
		{
			//If the value already existed then either
			//we have a duplicate run OR the previous run was capped
			
			Set<ProblemInstanceSeedPair> cappedRunsForConfig = getCappedAlgorithmInstanceSeedPairs(config);
			
			if(cappedRunsForConfig.contains(pisp))
			{
				//We remove it now and will re-add it if this current run was capped
				cappedRunsForConfig.remove(pisp); 
			} else
			{
			
			
				Object[] args = { run, config, pi};
				log.error("RunHistory already contains a run with identical config, instance and seed\nRun:{}\nConfig:{}\nInstance:{}", args);
				throw new IllegalStateException("RunHistory already contains a run with identical config, instance and seed");
			}
			
		}
		
		
		/**
		 * Add data to the run List
		 */
		int thetaIdx = paramConfigurationList.getOrCreateKey(config);
		
		
		//problemInstanceList.add(pi.getInstanceID(), pi);
		
		
		int instanceIdx = pi.getInstanceID();
		runHistoryList.add(new RunData(iteration, thetaIdx, instanceIdx, run,runResult));
		
		
		/**
		 * Increment the config run counter
		 */
		if(configToNumRunsMap.get(config) == null)
		{
			configToNumRunsMap.put(config, Integer.valueOf(1));
		} else
		{
			configToNumRunsMap.put(config, configToNumRunsMap.get(config) +1);
		}
		
		/**
		 * Add Instance to the set of instances ran 
		 */
		instancesRanSet.add(pi);
		
		/**
		 * Add to the capped runs set
		 */
		if(run.getRunResult().equals(RunResult.TIMEOUT) && run.getInstanceRunConfig().hasCutoffLessThanMax())
		{
			if(!cappedRuns.containsKey(config))
			{
				cappedRuns.put(config, new LinkedHashSet<ProblemInstanceSeedPair>());
			}
				
			cappedRuns.get(config).add(pisp);
		}
		
		Object[] args = {iteration, paramConfigurationList.getKey(config), pi.getInstanceID(), pisp.getSeed(), format.format(run.getInstanceRunConfig().getCutoffTime())};
		log.info("Iteration {}: running config {} on instance {} with seed {} and captime {}", args);
	
		
	}
	private static final DecimalFormat format = new DecimalFormat("#######.####");
	@Override
	public double getEmpiricalCost(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime)
	{
		Map<ProblemInstance, Map<Long, Double>> foo = Collections.emptyMap();
		return getEmpiricalCost(config, instanceSet, cutoffTime, foo);
	}
	
	@Override
	public double getEmpiricalCost(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime, Map<ProblemInstance, Map<Long,Double>> hallucinatedValues) {
		if (!configToPerformanceMap.containsKey(config) && hallucinatedValues.isEmpty()){
			return Double.MAX_VALUE;
		}
		ArrayList<Double> instanceCosts = new ArrayList<Double>();
		
		Map<ProblemInstance, LinkedHashMap<Long, Double>> instanceSeedToPerformanceMap = configToPerformanceMap.get(config);

		if(instanceSeedToPerformanceMap == null) 
		{
			instanceSeedToPerformanceMap = new HashMap<ProblemInstance, LinkedHashMap<Long, Double>>();
			
		}
		/**
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
			
			/**
			 * Aggregate the cost over the instances
			 */
			ArrayList<Double> localCosts = new ArrayList<Double>();
			for(Map.Entry<Long, Double> ent : seedToPerformanceMap.entrySet())
			{
					localCosts.add( ent.getValue() );	
			}
			instanceCosts.add( perInstanceObjectiveFunction.aggregate(localCosts,cutoffTime)); 
		}
		return aggregateInstanceObjectiveFunction.aggregate(instanceCosts,cutoffTime);
	}

	@Override
	public double getEmpiricalPISPCost(ParamConfiguration config, Set<ProblemInstanceSeedPair> instanceSet, double cutoffTime)
	{
		Map<ProblemInstance, Map<Long, Double>> foo = Collections.emptyMap();
		return getEmpiricalPISPCost(config, instanceSet, cutoffTime, foo);
	}
	
	@Override
	public double getEmpiricalPISPCost(ParamConfiguration config, Set<ProblemInstanceSeedPair> instanceSet, double cutoffTime, Map<ProblemInstance, Map<Long,Double>> hallucinatedValues) {
		if (!configToPerformanceMap.containsKey(config) && hallucinatedValues.isEmpty()){
			return Double.MAX_VALUE;
		}
		ArrayList<Double> instanceCosts = new ArrayList<Double>();
		
		Map<ProblemInstance, LinkedHashMap<Long, Double>> instanceSeedToPerformanceMap = configToPerformanceMap.get(config);

		if(instanceSeedToPerformanceMap == null) 
		{
			instanceSeedToPerformanceMap = new HashMap<ProblemInstance, LinkedHashMap<Long, Double>>();
			
		}
		/**
		 * Compute the Instances to use in the cost calculation
		 * It's everything we ran out of everything we requested.
		 */
		Set<ProblemInstanceSeedPair> instancesToUse = new HashSet<ProblemInstanceSeedPair>();
		instancesToUse.addAll(instanceSet);
		
		//(instanceSeedToPerformanceMap.keySet());
		
		//Keep only the ones we have real values for
		//Set<ProblemInstanceSeedPair> instancesToKeep = instanceSeedToPerformanceMap.keySet();
		Set<ProblemInstanceSeedPair> instancesToKeep = new HashSet<ProblemInstanceSeedPair>();
		for(Entry<ProblemInstance, LinkedHashMap<Long, Double>> realValue : instanceSeedToPerformanceMap.entrySet())
		{
			for(Long l : realValue.getValue().keySet())
			{
				instancesToKeep.add(new ProblemInstanceSeedPair(realValue.getKey(),l));
			}
		}
		
		
		for(Entry<ProblemInstance, Map<Long,Double>> halVal : hallucinatedValues.entrySet())
		{
			for(Long l : halVal.getValue().keySet())
			{
				instancesToKeep.add(new ProblemInstanceSeedPair(halVal.getKey(),l));
			}
		}
		
		//instancesToKeep.addAll(hallucinatedValues.keySet());
		
		instancesToUse.retainAll(instancesToKeep);
		
		Map<ProblemInstance, List<ProblemInstanceSeedPair>> organizedPispsToUse = new HashMap<ProblemInstance,List<ProblemInstanceSeedPair>>();
		for(ProblemInstanceSeedPair pisp : instancesToUse)
		{
			if(organizedPispsToUse.get(pisp.getInstance()) == null)
			{
				organizedPispsToUse.put(pisp.getInstance(), new LinkedList<ProblemInstanceSeedPair>());
			}
			organizedPispsToUse.get(pisp.getInstance()).add(pisp);
		}
		
		
		for(Entry<ProblemInstance,List<ProblemInstanceSeedPair>> entry : organizedPispsToUse.entrySet())
		{
			ProblemInstance pi = entry.getKey();
			Map<Long, Double> seedToPerformanceMap = new HashMap<Long, Double>();
			if(instanceSeedToPerformanceMap.get(pi) != null) seedToPerformanceMap.putAll(instanceSeedToPerformanceMap.get(pi));
			if(hallucinatedValues.get(pi) != null) seedToPerformanceMap.putAll(hallucinatedValues.get(pi));
			
			/**
			 * Aggregate the cost over the instances
			 */
			ArrayList<Double> localCosts = new ArrayList<Double>();
			for(Map.Entry<Long, Double> ent : seedToPerformanceMap.entrySet())
			{
				if(entry.getValue().contains(new ProblemInstanceSeedPair(pi,ent.getKey())))
				{
					localCosts.add( ent.getValue() );
				}
						
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
	public Set<ProblemInstance> getInstancesRan(ParamConfiguration config) {
		if (!configToPerformanceMap.containsKey(config)){
			return new HashSet<ProblemInstance>();
		}
		return new HashSet<ProblemInstance>( configToPerformanceMap.get(config).keySet() );
	}

	@Override
	public Set<ProblemInstanceSeedPair> getAlgorithmInstanceSeedPairsRan(
			ParamConfiguration config) {
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
	public Set<ProblemInstanceSeedPair> getCappedAlgorithmInstanceSeedPairs(ParamConfiguration config)
	{
		if(!cappedRuns.containsKey(config))
		{
			return Collections.emptySet();
		}
		
		return Collections.unmodifiableSet(cappedRuns.get(config));
	}

	@Override
	public ProblemInstance getRandomInstanceWithFewestRunsFor(
			ParamConfiguration config, List<ProblemInstance> instanceList,
			Random rand) {

		Map<ProblemInstance, LinkedHashMap<Long, Double>> instanceSeedToPerformanceMap = configToPerformanceMap.get(config);
		
		/**
		 * First try and see if if there are some candidate instances with zero runs
		 */
		List<ProblemInstance> candidates = new ArrayList<ProblemInstance>(instanceList.size());
		candidates.addAll(instanceList);
		if (configToPerformanceMap.containsKey(config)){
			//Allegedly this is a very slow operation (http://www.ahmadsoft.org/articles/removeall/index.html)
			candidates.removeAll(instanceSeedToPerformanceMap.keySet());
		}
		
		/**
		 * If not find the set with the smallest number of runs
		 */
		if (candidates.size() == 0){
			int minNumRuns = Integer.MAX_VALUE;
			for (Iterator<ProblemInstance> iterator = instanceList.iterator(); iterator.hasNext();) {
				ProblemInstance inst = (ProblemInstance) iterator.next();
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
		
		log.debug("Scheduling Run for {}", candidates.get(candidateIdx));
		return candidates.get(candidateIdx);	
	}

	@Override
	public ProblemInstanceSeedPair getRandomInstanceSeedWithFewestRunsFor(
			ParamConfiguration config, List<ProblemInstance> instanceList,
			Random rand) {
		ProblemInstance pi = getRandomInstanceWithFewestRunsFor(config, instanceList, rand);
		
		if(seedsUsedByInstance.get(pi) == null)
		{
			seedsUsedByInstance.put(pi, new ArrayList<Long>());
		}
		
		
		List<Long> seedsUsedByPi = seedsUsedByInstance.get(pi);
		
		Set<Long> seedsUsedByPiConfigSet;
		if(configToPerformanceMap.get(config) == null || configToPerformanceMap.get(config).get(pi) == null) 
		{ 
			seedsUsedByPiConfigSet = Collections.emptySet();
		} else
		{
			seedsUsedByPiConfigSet= configToPerformanceMap.get(config).get(pi).keySet();
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
			//We generate only positive seeds 
			 seed = instanceSeedGenerator.getNextSeed(pi);
		} else
		{
			seed = potentialSeeds.get(rand.nextInt(potentialSeeds.size()));
		}
		ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, seed);
		log.info("New Problem Instance Seed Pair Selected {}", pisp );
		return pisp;
	}
	
	

	@Override
	public int getTotalNumRunsOfConfig(ParamConfiguration config) {
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
	public double getTotalRunCost() {
		return totalRuntimeSum;
	}

	@Override
	public double[] getRunResponseValues() {
		
		double[] responseValues = new double[runHistoryList.size()];
		int i=0;
		for(RunData runData : runHistoryList)
		{
			responseValues[i] = runData.getResponseValue();
			i++;
		}
		return responseValues;
	}

	@Override
	public boolean[] getCensoredFlagForRuns() {
		boolean[] responseValues = new boolean[runHistoryList.size()];
		int i=0;
		for(RunData runData : runHistoryList)
		{
			responseValues[i] = runData.getRun().getRunResult().equals(RunResult.TIMEOUT) && runData.getRun().getInstanceRunConfig().hasCutoffLessThanMax();
			i++;
		}
		return responseValues;
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
	/**
	 * Get a list of algorithm runs we have used
	 * 
	 * Slow O(n) method to generate a list of Algorithm Runs
	 * We could speed this up but at this point we only do this for restoring state
	 */
	public List<AlgorithmRun> getAlgorithmRuns() 
	{
		
		List<AlgorithmRun> runs = new LinkedList<AlgorithmRun>();
		for(RunData runData : getAlgorithmRunData() )
		{
			runs.add(runData.getRun());
		}
		return runs;
	}

	@Override
	public List<RunData> getAlgorithmRunData() {
		return Collections.unmodifiableList(runHistoryList);
	}

	@Override
	public InstanceSeedGenerator getInstanceSeedGenerator() {
		return instanceSeedGenerator;
	}

	@Override
	public int getThetaIdx(ParamConfiguration incumbent) {
		return paramConfigurationList.getKey(incumbent);
	}

	

}
