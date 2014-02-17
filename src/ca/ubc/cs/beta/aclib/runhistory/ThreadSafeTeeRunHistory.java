package ca.ubc.cs.beta.aclib.runhistory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;


/**
 * TeeRunHistory is a RunHistory object that on top of notifying the decorated RunHistory object, also notifies another one but otherwise acts as a transparent decorator.
 * <br>
 * <b>Note:</b>Duplicate runs in the branch are simply silenced.
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
public class ThreadSafeTeeRunHistory implements ThreadSafeRunHistory {

	private final ThreadSafeRunHistory branch;
	private final ThreadSafeRunHistory rh; 

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	// We want additions to be atomic accross both
	private Object mutex = new Object();
	public ThreadSafeTeeRunHistory(ThreadSafeRunHistory out, ThreadSafeRunHistory branch) {
		this.branch = branch;
		this.rh = out;
	}

	@Override
	public void append(AlgorithmRun run) throws DuplicateRunException {
		
		synchronized(mutex)
		{
			rh.append(run);
			
			try {
				branch.append(run);
			} catch(DuplicateRunException e)
			{
				log.trace("Branch RunHistory object detected duplicate run: {}", run);
			}
		}
	}

	@Override
	public int getOrCreateThetaIdx(ParamConfiguration initialIncumbent) {
		
		synchronized(mutex)
		{
			try {
				return this.rh.getOrCreateThetaIdx(initialIncumbent);
			} finally
			{
				this.branch.getOrCreateThetaIdx(initialIncumbent);
			}
		}
	}

	@Override
	public void append(Collection<AlgorithmRun> runs)
			throws DuplicateRunException {
		synchronized(mutex)
		{
			rh.append(runs);
			
			try {
				branch.append(runs);
			} catch(DuplicateRunException e)
			{
				log.trace("Branch RunHistory object detected duplicate run: {}", runs);
			}
		}
		
	}

	@Override
	public void readLock() {
		branch.readLock();
		rh.readLock();
		
	}

	@Override
	public void releaseReadLock() {
		branch.releaseReadLock();
		rh.releaseReadLock();

	}
	
	@Override
	public RunObjective getRunObjective() {
		return rh.getRunObjective();
	}

	@Override
	public OverallObjective getOverallObjective() {
		return rh.getOverallObjective();
	}

	@Override
	public void incrementIteration() {
		rh.incrementIteration();
	}

	@Override
	public int getIteration() {
		return rh.getIteration();
	}

	@Override
	public Set<ProblemInstance> getProblemInstancesRan(ParamConfiguration config) {
		return rh.getProblemInstancesRan(config);
	}

	@Override
	public Set<ProblemInstanceSeedPair> getProblemInstanceSeedPairsRan(
			ParamConfiguration config) {
		return rh.getProblemInstanceSeedPairsRan(config);
	}

	@Override
	public double getEmpiricalCost(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime) {
		return rh.getEmpiricalCost(config, instanceSet, cutoffTime);
	}

	@Override
	public double getEmpiricalCost(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime,
			Map<ProblemInstance, Map<Long, Double>> hallucinatedValues) {
		return rh.getEmpiricalCost(config, instanceSet, cutoffTime,
				hallucinatedValues);
	}

	@Override
	public double getEmpiricalCost(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime,
			Map<ProblemInstance, Map<Long, Double>> hallucinatedValues,
			double minimumResponseValue) {
		return rh.getEmpiricalCost(config, instanceSet, cutoffTime,
				hallucinatedValues, minimumResponseValue);
	}

	
	@Override
	public double getTotalRunCost() {
		return rh.getTotalRunCost();
	}
	
	@Override
	public Set<ProblemInstance> getUniqueInstancesRan() {
		return rh.getUniqueInstancesRan();
	}

	@Override
	public Set<ParamConfiguration> getUniqueParamConfigurations() {
		return rh.getUniqueParamConfigurations();
	}

	@Override
	public int[][] getParameterConfigurationInstancesRanByIndex() {
		return rh.getParameterConfigurationInstancesRanByIndex();
	}

	@Override
	public List<ParamConfiguration> getAllParameterConfigurationsRan() {
		return rh.getAllParameterConfigurationsRan();
	}

	@Override
	public double[][] getAllConfigurationsRanInValueArrayForm() {
		return rh.getAllConfigurationsRanInValueArrayForm();
	}

	

	@Override
	public List<RunData> getAlgorithmRunData() {
		return rh.getAlgorithmRunData();
	}

	@Override
	public List<AlgorithmRun> getAlgorithmRunsExcludingRedundant() {
		return rh.getAlgorithmRunsExcludingRedundant();
	}
	
	@Override
	public List<AlgorithmRun> getAlgorithmRunsExcludingRedundant(ParamConfiguration config) {
		return rh.getAlgorithmRunsExcludingRedundant(config);
	}

	@Override
	public int getTotalNumRunsOfConfigExcludingRedundant(ParamConfiguration config) {
		return rh.getTotalNumRunsOfConfigExcludingRedundant(config);
	}

	@Override
	public List<AlgorithmRun> getAlgorithmRunsIncludingRedundant() {
		return rh.getAlgorithmRunsIncludingRedundant();
	}
	
	@Override
	public List<AlgorithmRun> getAlgorithmRunsIncludingRedundant(ParamConfiguration config) {
		return rh.getAlgorithmRunsIncludingRedundant(config);
	}

	@Override
	public int getTotalNumRunsOfConfigIncludingRedundant(ParamConfiguration config) {
		return rh.getTotalNumRunsOfConfigIncludingRedundant(config);
	}
	
	@Override
	public Set<ProblemInstanceSeedPair> getEarlyCensoredProblemInstanceSeedPairs(
			ParamConfiguration config) {
		return rh.getEarlyCensoredProblemInstanceSeedPairs(config);
	}

	@Override
	public int getThetaIdx(ParamConfiguration configuration) {
		return rh.getThetaIdx(configuration);
	}

	@Override
	public double getEmpiricalCost(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime,
			double minimumResponseValue) {
		return rh.getEmpiricalCost(config, instanceSet, cutoffTime,
				minimumResponseValue);
	}

	@Override
	public int getNumberOfUniqueProblemInstanceSeedPairsForConfiguration(
			ParamConfiguration config) {
		return rh
				.getNumberOfUniqueProblemInstanceSeedPairsForConfiguration(config);
	}

	@Override
	public Map<ProblemInstance, LinkedHashMap<Long, Double>> getPerformanceForConfig(
			ParamConfiguration configuration) {
		return rh.getPerformanceForConfig(configuration);
	}

	@Override
	public List<Long> getSeedsUsedByInstance(ProblemInstance pi) {
		return rh.getSeedsUsedByInstance(pi);
	}
	
	@Override
	public double getEmpiricalCostLowerBound(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime) {
		return rh.getEmpiricalCostLowerBound(config, instanceSet, cutoffTime);
	}

	@Override
	public double getEmpiricalCostUpperBound(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime) {
		return rh.getEmpiricalCostUpperBound(config, instanceSet, cutoffTime);
	}
	
	
}
