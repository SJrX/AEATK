package ca.ubc.cs.beta.smac.history;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ca.ubc.cs.beta.ac.config.ProblemInstance;
import ca.ubc.cs.beta.ac.config.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.configspace.ParamConfiguration;
import ca.ubc.cs.beta.probleminstance.InstanceSeedGenerator;
import ca.ubc.cs.beta.smac.OverallObjective;
import ca.ubc.cs.beta.smac.RunObjective;
import ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun;

public interface RunHistory {

	/*
	 * EASY TO IMPLEMENT
	 * METHODS
	 */
	
	/**
	 * Append a run to the RunHistory
	 * @param run - The Run to Log
	 */
	public void append(AlgorithmRun run);
		
	/**
	 * Get the Run Objective we are opitimizing
	 * @return 
	 */
	public RunObjective getRunObjective();

	/**
	 * Get the Overall objective we are optimizing
	 * @return
	 */
	public OverallObjective getOverallObjective();

	/**
	 * Increment the iteraction we are storing runs with
	 */
	public void incrementIteration();

	
	/**
	 * Get's the current Iteration
	 * @return current Iteration
	 */
	public int getIteration();


	
	
	/**
	 * Return the set of instances we have run a ParamConfiguration on
	 * @param config
	 * @return
	 */
	public Set<ProblemInstance> getInstancesRan(ParamConfiguration config);

	/**
	 * Returns the set of instance seed pairs we have run a Param Configuration on.
	 * @param config
	 * @return
	 */
	public Set<ProblemInstanceSeedPair> getAlgorithmInstanceSeedPairsRan(ParamConfiguration config);
	
	
	/**
	 * Compute and return the empirical cost of a parameter configuration on the subset of provided instances we have runs for.
	 * @param config - ParamConfiguration to get Cost of
	 * @param instanceSet - Instances to compute cost over
	 * @param cutoffTime - cutoff time (Not sure what this is used for)
	 * @return cost (Double.MAX_VALUE) if we haven't seen the configuration, otherwise the cost 
	 */
	public double getEmpiricalCost(ParamConfiguration config, Set<ProblemInstance> instanceSet, double cutoffTime);
	
	/**
	 * Compute and return the empirical cost of a parameter configuration on the subset of provided instances we have runs for.
	 * @param config - ParamConfiguration to get Cost of
	 * @param instanceSet - Instances to compute cost over
	 * @param cutoffTime - cutoff time (Not sure what this is used for)
	 * @return cost (Double.MAX_VALUE) if we haven't seen the configuration, otherwise the cost 
	 */
	double getEmpiricalCost(ParamConfiguration config, Set<ProblemInstance> instanceSet, double cutoffTime,	Map<ProblemInstance, Map<Long, Double>> hallucinatedValues);
	

	/**
	 * Returns a random instance with the fewest runs for the configuration
	 * @param config - ParamConfiguration to run
	 * @param instanceList - List of problem instances.
	 * @param rand - Random object used to break ties
	 * @return A random instance with the fewest runs for a configuration
	 */
	public ProblemInstance getRandomInstanceWithFewestRunsFor(ParamConfiguration config, List<ProblemInstance> instanceList, Random rand);

	/**
	 * Decides candidate Instance Seed Pairs to be run
	 * NOTE: When ProblemInstanceSeedPairs are run from here we don't actually know they have been run until we get
	 * an append call. This needs to be fixed later (i.e. we may execute duplicate requests)
	 * @param config - ParamConfiguration to run 
	 * @param instanceList - List of problem instances
	 * @param rand - Random object used to break ties
	 * @return Random instance seed pair
	 */
	public ProblemInstanceSeedPair getRandomInstanceSeedWithFewestRunsFor(ParamConfiguration config, List<ProblemInstance> instanceList, Random rand);
	
	/**
	 * Returns the total number of runs for a configuration
	 * @param config - ParamConfiguration
	 * @return number of runs
	 */
	public int getTotalNumRunsOfConfig(ParamConfiguration config);
	
	/**
	 * Returns the total cost of the all the runs used.
	 * @return
	 */
	public double getTotalRunCost();
	
	/**
	 * Returns a breakdown of each individual run cost
	 * @return
	 */
	public double[] getRunResponseValues();


	/**
	 * Get the list of Unique instances ran
	 * @return
	 */
	public Set<ProblemInstance> getUniqueInstancesRan();
	
	/**
	 * Gets a list of Unique Param Configurations Ran
	 * @return
	 */
	public Set<ParamConfiguration> getUniqueParamConfigurations();
	
	/**
	 * Not sure what this does
	 * @return
	 */
	public int[][] getParameterConfigurationInstancesRanByIndex();
	
	/**
	 * Returns the param configurations ran in order
	 * @return
	 */
	public List<ParamConfiguration> getAllParameterConfigurationsRan();
	
	/**
	 * Not sure what this does.
	 * @return
	 */
	public double[][] getAllConfigurationsRanInValueArrayForm();
	
	/**
	 * Returns a list of all the runs
	 * @return
	 */
	public List<AlgorithmRun> getAlgorithmRuns();
	
	/**
	 * Returns a list of all the Run Data
	 * @return
	 */
	public List<RunData> getAlgorithmRunData();

	/**
	 * Returns the Instance Seed Generator
	 * @return
	 */
	public InstanceSeedGenerator getInstanceSeedGenerator();

	/**
	 * Returns a set of Instance Seed Pairs that were capped for a given configuration
	 * @param config
	 * @return
	 */
	public Set<ProblemInstanceSeedPair> getCappedAlgorithmInstanceSeedPairs(ParamConfiguration config);

	double getEmpiricalPISPCost(ParamConfiguration config,
			Set<ProblemInstanceSeedPair> instanceSet, double cutoffTime);

	double getEmpiricalPISPCost(ParamConfiguration config,
			Set<ProblemInstanceSeedPair> instanceSet, double cutoffTime,
			Map<ProblemInstance, Map<Long, Double>> hallucinatedValues);

	
	
}
