package ca.ubc.cs.beta.aclib.runhistory;
//package ca.ubc.cs.beta.aclib.runhistory;
//
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Random;
//import java.util.Set;
//import java.util.TreeSet;
//
//import org.apache.commons.math.stat.StatUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import ca.ubc.cs.beta.aclib.runconfig.ProblemInstance;
//import ca.ubc.cs.beta.aclib.runconfig.ProblemInstanceSeedPair;
//import ca.ubc.cs.beta.aclib.runconfig.comparators.ParamConfigurationComparator;
//import ca.ubc.cs.beta.aclib.runconfig.comparators.ProblemInstanceComparator;
//import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
//import ca.ubc.cs.beta.smac.OverallObjective;
//import ca.ubc.cs.beta.smac.RunObjective;
//import ca.ubc.cs.beta.smac.ac.InstanceSeedGenerator;
//import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
//
////TODO This class has alot of redundant data structures, it was ported mostly from the previous ROAR base
//public class LegacyRunHistory implements RunHistory {
//	
//	//
//	private final ArrayList<ProblemInstance> pisUsed = new ArrayList<ProblemInstance>();
//	
//	
//	
//	@Deprecated 
//	private final ArrayList<ParamConfiguration> configsUsed = new ArrayList<ParamConfiguration>();
//	
//	
//	
//	private final Set<ProblemInstance> uniquePisUsed = new TreeSet<ProblemInstance>(new ProblemInstanceComparator());
//	private final Set<ParamConfiguration> uniqueConfigsUsed = new TreeSet<ParamConfiguration>(new ParamConfigurationComparator());
//	
//	private final Set<Long> seedsUsed = new HashSet<Long>();
//	
//	
//	private final HashMap<ParamConfiguration, HashMap<ProblemInstance, HashMap<Long, Double>>> configToPerformanceMap =
//		new HashMap<ParamConfiguration, HashMap<ProblemInstance, HashMap<Long, Double>>>();
//
//	
//	private final Map<ParamConfiguration, Integer> numRunsForConfig = new HashMap<ParamConfiguration, Integer>();
//	private final HashMap<ProblemInstance, List<Long>> seedsUsedByInstance = new HashMap<ProblemInstance, List<Long>>();
//	
//	private final HashMap<ParamConfiguration, Map<ProblemInstance, List<Long>>> paramInstanceToSeeds= new HashMap<ParamConfiguration, Map<ProblemInstance, List<Long>>>(); 
//	
//	private final Logger log = LoggerFactory.getLogger(getClass());
//	
//	private final InstanceSeedGenerator instanceSeedGenerator; 
//
//	
//	/**
//	 * Stores the run id for a configuration
//	 */
//	private final HashMap<ParamConfiguration, Integer> configIds = new LinkedHashMap<ParamConfiguration, Integer>();
//
//	//private final HashMap<ProblemInstance, Integer> instanceIds = new HashMap<ProblemInstance, Integer>();
//	
//	
//	/**
//	 * List of doubles that store the runCost for the nth run in index n-1.
//	 */
//	private final ArrayList<Double> runCosts = new ArrayList<Double>();
//	
//	private final ArrayList<int[]> thetaRunIndexes = new ArrayList<int[]>();
//
//	
//	
//	
//	/* Add the runtime data for a single target algorithm run to the history. */
//	private double runtimeSum = 0;
//	
//	private final List<AlgorithmRun> runs = new LinkedList<AlgorithmRun>();
//	private final List<RunData> runData = new LinkedList<RunData>();
//
//
//	private final OverallObjective overallObj;
//
//	private final RunObjective runObj;
//	
//	private int iteration = 0;
//	
//	public LegacyRunHistory(InstanceSeedGenerator instanceSeedGenerator, OverallObjective perInstanceObjectiveFunction,  OverallObjective aggregateInstanceObjectiveFunction, RunObjective runObj)
//	{
//		this.instanceSeedGenerator = instanceSeedGenerator;
//		this.overallObj = aggregateInstanceObjectiveFunction;
//		this.runObj = runObj;
//	}
//	
//	public void append(AlgorithmRun run){
//		
//		log.trace("Appending Run {}",run);
//		ParamConfiguration config = run.getInstanceRunConfig().getParamConfiguration(); 
//		ProblemInstance pi = run.getInstanceRunConfig().getAlgorithmInstanceSeedPair().getInstance();
//		long seed = run.getResultSeed();
//		double runCost = run.getRuntime();
//		
//		//Update Run Cost
//		//We use 0.1 seconds as a min to account for overhead
//		//See line 25 of dorun.m
//		
//		runtimeSum += Math.max(0.1, runCost);
//		
//		
//		//=== Update full map of config, instance, seed, and runCost.
//		if (!configToPerformanceMap.containsKey(config)){
//			configToPerformanceMap.put(config, new HashMap<ProblemInstance, HashMap<Long, Double>>());
//		}
//		HashMap<ProblemInstance, HashMap<Long, Double>> hConfig = configToPerformanceMap.get(config);
//
//		if (!hConfig.containsKey(pi)){
//			hConfig.put(pi, new HashMap<Long, Double>());
//		}
//		HashMap<Long, Double> hConfigInst = hConfig.get(pi);
//
//		if (hConfigInst.containsKey(seed)){
//			
//			throw new IllegalStateException("ERROR in control logic: we already logged a run with identical config, instance and seed.");
//		} else {
//			hConfigInst.put(seed, runCost);
//		}
//		
//		//=== Increase counter for total #runs for config.
//		if (!numRunsForConfig.containsKey(config)){
//			numRunsForConfig.put(config, 0);
//		}
//		numRunsForConfig.put(config, numRunsForConfig.get(config).intValue()+1 );
//
//		//=== Update single array lists.
//		configsUsed.add(config);
//		pisUsed.add(pi);
//		
//		uniquePisUsed.add(pi);
//		uniqueConfigsUsed.add(config);
//		
//		
//		if(seedsUsedByInstance.get(pi) == null)
//		{
//			seedsUsedByInstance.put(pi, new ArrayList<Long>());
//		}
//		
//		
//		if(paramInstanceToSeeds.get(config) == null)
//		{
//			paramInstanceToSeeds.put(config, new HashMap<ProblemInstance,List<Long>>());
//			
//		}
//		
//		seedsUsedByInstance.get(pi).add(seed);
//		
//		seedsUsed.add(seed);
//		
//		
//		if(paramInstanceToSeeds.get(config).get(pi) == null)
//		{
//			paramInstanceToSeeds.get(config).put(pi, new ArrayList<Long>());
//		}
//		paramInstanceToSeeds.get(config).get(pi).add(seed);
//		
//		Integer configId = configIds.get(config); 
//		if( configId == null)
//		{
//			configId = configIds.size() + 1;
//			configIds.put(config, configId);
//		}
//		
//		
//		/*
//		Integer instanceId = instanceIds.get(pi);
//		if(instanceId == null)
//		{
//			instanceId = instanceIds.size() + 1;
//			instanceIds.put(pi, instanceId);
//		}*/
//		int[] config_instance_rec = { configId, pi.getInstanceID() } ;
//		thetaRunIndexes.add(config_instance_rec);
//		runCosts.add(runCost);
//		runs.add(run);
//		runData.add(new RunData(iteration, configId, pi.getInstanceID(), run, run.getRuntime()));
//	}
//
//	public RunObjective getRunObjective()
//	{
//		return runObj;
//	}
//	public OverallObjective getOverallObjective()
//	{
//		return overallObj;
//	}
//	
//
//	public void incrementIteration()
//	{
//		iteration++;
//	}
//	
//	public int getIteration()
//	{
//		return iteration;
//	}
//	
//	/* Return instances we have already run for config. */
//	public Set<ProblemInstance> getInstancesRan(ParamConfiguration config){
//		if (!configToPerformanceMap.containsKey(config)){
//			return new HashSet<ProblemInstance>();
//		}
//		return new HashSet<ProblemInstance>( configToPerformanceMap.get(config).keySet() );
//	}
//
//	/* Return <instance,seed> combos we have already run for config. */
//	public Set<ProblemInstanceSeedPair> getAlgorithmInstanceSeedPairsRan(ParamConfiguration config){
//		if (!configToPerformanceMap.containsKey(config)){
//			return new HashSet<ProblemInstanceSeedPair>();
//		}
//		Set<ProblemInstanceSeedPair> AlgorithmInstanceSeedPairs = new HashSet<ProblemInstanceSeedPair>();
//		HashMap<ProblemInstance, HashMap<Long, Double>> hConfig = configToPerformanceMap.get(config);
//		for (Entry<ProblemInstance, HashMap<Long, Double>> kv : hConfig.entrySet()) {
//			//=== For each instance we have run for the config, add a pair for each of the run seeds.
//			ProblemInstance pi =  kv.getKey();
//			HashMap<Long, Double> hConfigInst = kv.getValue();
//			for (Long seed: hConfigInst.keySet()) {
//				AlgorithmInstanceSeedPairs.add( new ProblemInstanceSeedPair(pi, seed) );
//			}
//		}
//		return AlgorithmInstanceSeedPairs;
//	}
//	
//	
//	public double par10Mean(Collection<Double> c, double cutoffTime)
//	{
//		
//		double[] par10 = new double[c.size()];
//		int i=0;
//		for(double d : c)
//		{
//			par10[i] = (d >= cutoffTime) ? 10*d : d;
//		}
//		
//		return StatUtils.mean(par10);
//	}
//	
//	public double mean(Collection<Double> c, double cutoffTime)
//	{
//		double[] mean = new double[c.size()];
//		int i=0;
//		for(double d : c)
//		{
//			
//			mean[i] = d;
//			i++;
//		}
//		
//		return StatUtils.mean(mean);
//	}
//	
//	
//	/* Compute and return the empirical cost for a parameter configuration on the subset of the provided instance set that we have runs for. */
//	public double getEmpiricalCost(ParamConfiguration config, Set<ProblemInstance> instanceSet, double cutoffTime){
//		if (!configToPerformanceMap.containsKey(config)){
//			return Double.MAX_VALUE;
//		}
//		ArrayList<Double> instanceCosts = new ArrayList<Double>();
//		HashMap<ProblemInstance, HashMap<Long, Double>> hConfig = configToPerformanceMap.get(config);
//
//		//=== Instances to use: intersection of input instance set and instances ran.
//		Set<ProblemInstance> instancesToUse = new HashSet<ProblemInstance>();
//		instancesToUse.addAll(instanceSet);
//		instancesToUse.retainAll(hConfig.keySet());
//
//		for (Iterator<ProblemInstance> iterator = instancesToUse.iterator(); iterator.hasNext();) {
//			ProblemInstance pi = (ProblemInstance) iterator.next();
//			HashMap<Long, Double> hConfigInst = hConfig.get(pi);
//
//			//=== Aggregate costs for each of the seeds we ran for the instance.
//			ArrayList<Double> localCosts = new ArrayList<Double>();
//			for (Iterator<Long> iterator2 = hConfigInst.keySet().iterator(); iterator2.hasNext();) {
//				long seed = (Long) iterator2.next();
//				localCosts.add( hConfigInst.get(seed) );
//			}
//			instanceCosts.add( mean(localCosts,cutoffTime)); 
//		}
//
//		//=== In the end, aggregate costs across the instances.
//		return mean(instanceCosts,cutoffTime);
//	}
//
//	/* Get a random element of the set of instances for which config has the fewest runs. */
//	public ProblemInstance getRandomInstanceWithFewestRunsFor(ParamConfiguration config, List<ProblemInstance> instanceList, Random rand){
//		HashMap<ProblemInstance, HashMap<Long, Double>> hConfig = configToPerformanceMap.get(config);
//		
//		//=== Make every instance that hasn't been run yet for config a candidate.
//		List<ProblemInstance> candidates = new ArrayList<ProblemInstance>(instanceList.size());
//		candidates.addAll(instanceList);
//		if (configToPerformanceMap.containsKey(config)){
//			//Allegedly this is a very slow operation (http://www.ahmadsoft.org/articles/removeall/index.html)
//			candidates.removeAll(hConfig.keySet());
//		}
//		
//		//=== If all instances have at least one run, go through and get the instances with minimal #runs.
//		if (candidates.size() == 0){
//			int minNumRuns = Integer.MAX_VALUE;
//			for (Iterator<ProblemInstance> iterator = instanceList.iterator(); iterator.hasNext();) {
//				ProblemInstance inst = (ProblemInstance) iterator.next();
//				int numRuns = hConfig.get(inst).size();
//				if (numRuns <= minNumRuns){
//					if (numRuns < minNumRuns){ // new value for fewest runs -> ditch all previous candidates
//						candidates.clear();
//						minNumRuns = numRuns;
//					}
//					candidates.add(inst);
//				}
//			}
//		}
//		
//		//=== Return a random element of the candidate instance set (it's sad there is no method for that in Java's Set).\
//		int candidateIdx = rand.nextInt(candidates.size());
//		
//		log.debug("Scheduling Run for {}", candidates.get(candidateIdx));
//		return candidates.get(candidateIdx);
//		//return candidates[rand.nextInt(candidates.size())];
//	}
//	
//	/**
//	 * Decides candidate Instance Seed Pairs to be run
//	 * NOTE: When ProblemInstanceSeedPairs are run from here we don't actually know they have been run until we get
//	 * an append call. This needs to be fixed later (i.e. we may execute duplicate requests)
//	 * @param config
//	 * @param instanceList
//	 * @param rand
//	 * @return
//	 */
//	public ProblemInstanceSeedPair getRandomInstanceSeedWithFewestRunsFor(ParamConfiguration config, List<ProblemInstance> instanceList, Random rand){
//		ProblemInstance pi = getRandomInstanceWithFewestRunsFor(config, instanceList, rand);
//		
//		if(seedsUsedByInstance.get(pi) == null)
//		{
//			seedsUsedByInstance.put(pi, new ArrayList<Long>());
//		}
//		
//		
//		if(paramInstanceToSeeds.get(config) == null)
//		{
//			paramInstanceToSeeds.put(config, new HashMap<ProblemInstance,List<Long>>());
//			paramInstanceToSeeds.get(config).put(pi, new ArrayList<Long>());				
//		}
//		
//		
//		
//		
//		List<Long> seedsUsedByPi = seedsUsedByInstance.get(pi);
//		List<Long> seedsUsedByPiConfig = paramInstanceToSeeds.get(config).get(pi);
//		
//		if(seedsUsedByPiConfig == null)
//		{
//			seedsUsedByPiConfig = Collections.emptyList();
//		}
//		
//		
//		List<Long> potentialSeeds = new ArrayList<Long>(seedsUsedByPi.size() - seedsUsedByPiConfig.size());
//		
//		potentialSeeds.addAll(seedsUsedByPi);
//		potentialSeeds.removeAll(seedsUsedByPiConfig);
//		
//		long seed;
//		if(potentialSeeds.size() == 0)
//		{
//			//We generate only positive seeds 
//			 seed = instanceSeedGenerator.getNextSeed(pi);
//		} else
//		{
//			seed = potentialSeeds.get(rand.nextInt(potentialSeeds.size()));
//		}
//		System.out.println(seed);
//		ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, seed);
//		log.info("New Problem Instance Seed Pair Selected {}", pisp );
//		return pisp;
//	}
//	
//	
//	
//	public int getTotalNumRunsOfConfig(ParamConfiguration config){
//		if (numRunsForConfig.containsKey(config)){
//			return numRunsForConfig.get(config);
//		} else {
//			return 0;
//		}
//	}
//
//	public double getTotalRunCost() {
//
//		return runtimeSum;
//	}
//	
//	public double[] getRunResponseValues()
//	{
//		double[] costs = new double[runCosts.size()];
//		int i=0;
//		for(Double d : runCosts)
//		{
//			costs[i] = d;
//			i++;
//		}
//		return costs;
//	}
//
//
//	@Deprecated
//	public List<ParamConfiguration> getParameterConfigurations() {
//		// TODO Auto-generated method stub
//		return Collections.unmodifiableList(configsUsed);
//	}
//
//	
//	
//	public Set<ProblemInstance> getUniqueInstancesRan()
//	{
//		return Collections.unmodifiableSet(uniquePisUsed);
//	}
//	
//	public Set<ParamConfiguration> getUniqueParamConfigurations(){
//		return Collections.unmodifiableSet(uniqueConfigsUsed);
//	}
//
//	public int[][] getParameterConfigurationInstancesRanByIndex() {
//		 
//		int[][] result = thetaRunIndexes.toArray(new int[thetaRunIndexes.size()][]);
//		int[][] deepCopy = new int[result.length][2];
//		for(int i=0; i < result.length; i++)
//		{
//			deepCopy[i][0] = result[i][0];
//			deepCopy[i][1] = result[i][1];
//		}
//		return deepCopy;
//		
//	}
//	
//	/**
//	 * Returns the param configurations ran in order
//	 * @return
//	 */
//	public List<ParamConfiguration> getAllParameterConfigurationsRan()
//	{
//		List<ParamConfiguration> configs = new ArrayList<ParamConfiguration>(configIds.size());
//		configs.addAll(configIds.keySet());
//		return configs;
//		
//	}
//	
//	public double[][] getAllConfigurationsRanInValueArrayForm()
//	{
//		double[][] configs = new double[uniqueConfigsUsed.size()][];
//		for(Entry<ParamConfiguration, Integer> ent : configIds.entrySet())
//		{
//			int index = ent.getValue() - 1;
//			configs[index] = ent.getKey().toValueArray();
//		}
//		return configs;
//		
//	}
//	
//	public List<AlgorithmRun> getAlgorithmRuns()
//	{
//		return Collections.unmodifiableList(runs);
//	}
//	
//	
//	public List<RunData> getAlgorithmRunData() {
//		return Collections.unmodifiableList(runData);
//	}
//	
//	public InstanceSeedGenerator getInstanceSeedGenerator() {
//
//		return instanceSeedGenerator;
//	}
//	
//}
