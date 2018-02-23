package ca.ubc.cs.beta.aeatk.initialization.doubleracing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aeatk.exceptions.OutOfTimeException;
import ca.ubc.cs.beta.aeatk.initialization.InitializationProcedure;
import ca.ubc.cs.beta.aeatk.initialization.table.UnbiasChallengerInitializationProcedureOptions;
import ca.ubc.cs.beta.aeatk.objectives.ObjectiveHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.probleminstance.seedgenerator.InstanceSeedGenerator;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;
import ca.ubc.cs.beta.aeatk.runhistory.ThreadSafeRunHistory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.termination.CompositeTerminationCondition;
import ca.ubc.cs.beta.aeatk.termination.TerminationCondition;

public class DoubleRacingInitializationProcedure implements InitializationProcedure {

	private final ThreadSafeRunHistory runHistory;
	private final ParameterConfiguration initialIncumbent;
	private final TargetAlgorithmEvaluator tae;
	private final DoubleRacingInitializationProcedureOptions opts;
	private final Logger log = LoggerFactory.getLogger(DoubleRacingInitializationProcedure.class);
	private final int maxIncumbentRuns;
	private final List<ProblemInstance> instances;
	private final InstanceSeedGenerator insc;
	private ParameterConfiguration incumbent;
	private final TerminationCondition termCond;
	private final double cutoffTime;
	private final SeedableRandomPool pool;
	private final AlgorithmExecutionConfiguration algorithmExecutionConfig;
	private final ParameterConfigurationSpace configSpace;


	public DoubleRacingInitializationProcedure(ThreadSafeRunHistory runHistory, ParameterConfiguration initialIncumbent, TargetAlgorithmEvaluator tae, DoubleRacingInitializationProcedureOptions opts, InstanceSeedGenerator insc, List<ProblemInstance> instances,  int maxIncumbentRuns , TerminationCondition termCond, double cutoffTime, SeedableRandomPool pool, boolean deterministicInstanceOrdering, AlgorithmExecutionConfiguration execConfig)
	{
		this.runHistory =runHistory;
		this.initialIncumbent = initialIncumbent;
		this.initialIncumbent.lock();
		this.tae = tae;
		this.opts = opts;
		this.instances = instances;
		this.maxIncumbentRuns = maxIncumbentRuns;
		this.insc = insc;
		this.incumbent = initialIncumbent;
		this.termCond = termCond;
		this.cutoffTime = cutoffTime;
		this.pool = pool;
		this.algorithmExecutionConfig = execConfig;
		this.configSpace = initialIncumbent.getParameterConfigurationSpace();
		
		
	}
	

	@Override
	public void run() {
		log.debug("Using Double Racing Initialization");
		ParameterConfiguration incumbent = this.initialIncumbent;
		log.trace("Default Configuration Set as Incumbent: {}", incumbent);
		
		int N = opts.incumbentRunsInFinalInitRound;

		N = Math.min(N, insc.getInitialInstanceSeedCount());
		N = Math.min(N, maxIncumbentRuns);

		
		//=== Set up the rounds -- divide maximal captime by 4 in each step, and N by 2, to start with kappa >= 1 and at least one instance.
		double capTime=cutoffTime;
		int numInstances=N;
		int numThetas = 2;
		
		List<RoundSetting> roundSettings = new ArrayList<RoundSetting>();
		while(capTime/4 >= 1 && numInstances >= 2)
		{
			capTime /= 4;
			numThetas *= 4;
			numThetas = Math.min(numThetas, (int) configSpace.getUpperBoundOnSize());
			numInstances /= 2;
			roundSettings.add(new RoundSetting(numThetas, numInstances, capTime));
		}

		//=== Generate set of configurations for the first round: incumbent + many random configs.
		List<ParameterConfiguration> topConfigs = new ArrayList<ParameterConfiguration>();
		topConfigs.add(incumbent);
		Random configRandom = pool.getRandom("DOUBLING_INITIALIZATION_CONFIGS");
		while(topConfigs.size() < roundSettings.get(0).numThetas)
		{
			topConfigs.add(configSpace.getRandomParameterConfiguration(configRandom));
		}

		//=== Generate list of (instance, seed) pairs.
		Random rand = pool.getRandom("DOUBLING_INITIALIZATION_CONFIGS");		
		List<ProblemInstanceSeedPair> selectedPisps = getProblemInstanceSeedPairs(rand, N);		
		
		//=== Set up a hashtable with the average costs we get for each configuration.
		final Hashtable<ParameterConfiguration,Double> costs = new Hashtable<ParameterConfiguration,Double>();

		ParameterConfiguration localIncumbent = initialIncumbent;
		//=== Execute the rounds.
		for (int numRound = 0; numRound < roundSettings.size(); numRound++) {
			RoundSetting roundSetting = roundSettings.get(numRound);
			capTime = roundSetting.capTime;
//			numThetas = roundSetting.numThetas;
			numInstances = roundSetting.numInstances; // We'll use the first numInstances instances.
			log.debug("Starting new round in the initialization, in which we evaluate {} configurations on {} instances with capTime {}", topConfigs.size(), numInstances, capTime);
			
			//=== Execute the runs for all configs on all instances, and keep track of the average costs.
			for (ParameterConfiguration config: topConfigs){
				double cost = 0;
				for (int i = 0; i < numInstances; i++) {
					ProblemInstanceSeedPair pisp = selectedPisps.get(i);
					AlgorithmRunConfiguration runConfig = new AlgorithmRunConfiguration(pisp, cutoffTime, config, algorithmExecutionConfig);

					if(termCond.haveToStop())
					{
						log.debug("Initialization already ran out of time. Setting the local incumbent {} ({}) with cost {} to be incumbent and terminating.", this.runHistory.getThetaIdx(localIncumbent), localIncumbent, costs.get(localIncumbent));
				 		this.incumbent = localIncumbent;
						return;
					}
					
					AlgorithmRunResult algoResult = evaluateRun( runConfig );
					cost += algoResult.getQuality();
				}
				costs.put(config, cost/numInstances);
			}

			//=== Shrink the set of top configurations to the next numThetas (1 in the final iteration). (Done by creating an ordered set and getting the top ones back.)
			int nextNumTheta = 1;
			if (numRound < roundSettings.size()-1){
				nextNumTheta = roundSettings.get(numRound+1).numThetas;
			}
			Set<ParameterConfiguration> treeSet = new TreeSet<ParameterConfiguration>(new Comparator<ParameterConfiguration>() {
				  @Override
				  public int compare(ParameterConfiguration o1, ParameterConfiguration o2) {
				    return costs.get(o1).compareTo(costs.get(o2));
				  }
				});
			treeSet.addAll(topConfigs);
			topConfigs = new ArrayList<ParameterConfiguration>();
			int numTopConfigsAdded = 0;
			for (ParameterConfiguration topConfig : treeSet) {
				topConfigs.add(topConfig);
				numTopConfigsAdded++;
				if( numTopConfigsAdded >= nextNumTheta ) {
					break;
				}
			}
	 		localIncumbent = topConfigs.get(0);
			log.debug("In end of initialization round {}, the local incumbent {} ({}) incumbent has cost: {} ", this.runHistory.getThetaIdx(localIncumbent), localIncumbent, costs.get(localIncumbent));

		}

		log.debug("In end of initialization, the incumbent {} ({}) has cost: {} ", this.runHistory.getThetaIdx(localIncumbent), localIncumbent, costs.get(localIncumbent));
 		this.incumbent = localIncumbent;

		
	}

	@Override
	public ParameterConfiguration getIncumbent() {
		return incumbent;
	}
	
	protected AlgorithmRunResult evaluateRun(AlgorithmRunConfiguration runConfig)
	{
		return evaluateRuns(Collections.singletonList(runConfig)).get(0);
	}
	
	/**
	 * Returns a set of problem instance seed pairs. The most frequently occurring instance should only occur at most once more than the least frequently occurring.
	 * @param rand
	 * @return List of problem instance seed pairs
	 */
	private List<ProblemInstanceSeedPair> getProblemInstanceSeedPairs(Random rand, int numInstances) {
		List<ProblemInstanceSeedPair> selectedPisps = new ArrayList<ProblemInstanceSeedPair>(); 
		
		log.debug("Generating {} (problem instance, seed) pairs for use in initialization.", numInstances);
		
		List<ProblemInstance> shuffledPis = new ArrayList<ProblemInstance>(instances);
		Collections.shuffle(shuffledPis,rand);
		
		//=== Create PISPS for table
		while(selectedPisps.size() < numInstances)
		{
			for(ProblemInstance pi : shuffledPis)
			{
				if(!insc.hasNextSeed(pi))
				{
					log.debug("Prematurely exhausted number of (problem instance, seed) pairs.");
					break;
//					throw new IllegalStateException("Should not have been able to generate this many requests for configurations");
				}
				
				long seed = insc.getNextSeed(pi);
				ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, seed);
				selectedPisps.add(pisp);
				
				if(selectedPisps.size() >= numInstances)
				{
					break;
				}
				
			}
		}
		return selectedPisps;
	}

	
	
	protected List<AlgorithmRunResult> evaluateRuns(List<AlgorithmRunConfiguration> runConfigs)
	{
	
		log.debug("Initialization: Scheduling {} run(s):",  runConfigs.size());
		for(AlgorithmRunConfiguration rc : runConfigs)
		{
			Object[] args = {  runHistory.getThetaIdx(rc.getParameterConfiguration())!=-1?" "+runHistory.getThetaIdx(rc.getParameterConfiguration()):"", rc.getParameterConfiguration(), rc.getProblemInstanceSeedPair().getProblemInstance().getInstanceID(),  rc.getProblemInstanceSeedPair().getSeed(), rc.getCutoffTime()};
			log.debug("Initialization: Scheduling run for config{} ({}) on instance {} with seed {} and captime {}", args);
		}
		
		List<AlgorithmRunResult> completedRuns = tae.evaluateRun(runConfigs);
		
		for(AlgorithmRunResult run : completedRuns)
		{
			AlgorithmRunConfiguration rc = run.getAlgorithmRunConfiguration();
			Object[] args = {  runHistory.getThetaIdx(rc.getParameterConfiguration())!=-1?" "+runHistory.getThetaIdx(rc.getParameterConfiguration()):"", rc.getParameterConfiguration(), rc.getProblemInstanceSeedPair().getProblemInstance().getInstanceID(),  rc.getProblemInstanceSeedPair().getSeed(), rc.getCutoffTime(), run.getResultLine(),  run.getWallclockExecutionTime()};
			log.debug("Initialization: Completed run for config{} ({}) on instance {} with seed {} and captime {} => Result: {}, wallclock time: {} seconds", args);
		}
		
		
		
		updateRunHistory(completedRuns);
		return completedRuns;
	}
	
	/**
	 * 
	 * @return the input parameter (unmodified, simply for syntactic convience)
	 */
	protected List<AlgorithmRunResult> updateRunHistory(List<AlgorithmRunResult> runs)
	{
		for(AlgorithmRunResult run : runs)
		{
			try {
					runHistory.append(run);
			} catch (DuplicateRunException e) {
				//We are trying to log a duplicate run
				throw new IllegalStateException(e);
			}
		}
		return runs;
	}
	


}
