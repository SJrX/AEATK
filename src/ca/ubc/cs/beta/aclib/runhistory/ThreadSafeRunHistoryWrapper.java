package ca.ubc.cs.beta.aclib.runhistory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;

public class ThreadSafeRunHistoryWrapper implements ThreadSafeRunHistory {

	private final RunHistory runHistory;
	
	private final ReentrantReadWriteLock myLock = new ReentrantReadWriteLock(true);
	
	//private final static Logger log = LoggerFactory.getLogger(ThreadSafeRunHistoryWrapper.class);
	
	public ThreadSafeRunHistoryWrapper(RunHistory runHistory)
	{
		this.runHistory = runHistory;
	}
	
	@Override
	public void append(Collection<AlgorithmRun> runs)
			throws DuplicateRunException {

		try 
		{
			myLock.readLock().unlock();
			throw new IllegalStateException(" I should not be releasable");
		} catch(IllegalMonitorStateException ex)
		{
			//System.out.println("I'm okay");
		}
		myLock.writeLock().lock();
		try {
			for(AlgorithmRun run : runs)
			{
				//log.debug("Atomically appending run {} " + run.getRunConfig());
				runHistory.append(run);
			}
			
		} finally
		{
			myLock.writeLock().unlock();
		}
		
	}
	
	
	@Override
	public void append(AlgorithmRun run) throws DuplicateRunException {
		
		
		try 
		{
			myLock.readLock().unlock();
			throw new IllegalStateException(" I should not be releasable");
		} catch(IllegalMonitorStateException ex)
		{
			//System.out.println("I'm okay");
		}
		
			myLock.writeLock().lock();
		try {
			//log.debug("Appending single run {} " + run.getRunConfig());
			runHistory.append(run);
		} finally
		{
			myLock.writeLock().unlock();
		}
		
		
	}

	@Override
	public RunObjective getRunObjective() {
		myLock.readLock().lock();
		
		try {
			return runHistory.getRunObjective();
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public OverallObjective getOverallObjective() {
		myLock.readLock().lock();
		try {
			return runHistory.getOverallObjective();
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public void incrementIteration() {
		myLock.writeLock().lock();
		
		try {
			 runHistory.incrementIteration();
		} finally
		{
			myLock.writeLock().unlock();
		}
	}

	@Override
	public int getIteration() {

		myLock.readLock().lock();
		try {
			return runHistory.getIteration();
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public Set<ProblemInstance> getInstancesRan(ParamConfiguration config) {
		myLock.readLock().lock();
		try {
			return runHistory.getInstancesRan(config);
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public Set<ProblemInstanceSeedPair> getAlgorithmInstanceSeedPairsRan(
			ParamConfiguration config) {
		
		myLock.readLock().lock();
		try {
			return runHistory.getAlgorithmInstanceSeedPairsRan(config);
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public double getEmpiricalCost(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime) {
		myLock.readLock().lock();
		try {
			return runHistory.getEmpiricalCost(config, instanceSet, cutoffTime);
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public double getEmpiricalCost(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime,
			Map<ProblemInstance, Map<Long, Double>> hallucinatedValues) {
		myLock.readLock().lock();
		try {
			return runHistory.getEmpiricalCost(config, instanceSet, cutoffTime, hallucinatedValues);
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public double getEmpiricalCost(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime,
			Map<ProblemInstance, Map<Long, Double>> hallucinatedValues,
			double minimumResponseValue) {
		myLock.readLock().lock();
		try {
			return runHistory.getEmpiricalCost(config, instanceSet, cutoffTime, hallucinatedValues, minimumResponseValue);
		} finally
		{
			myLock.readLock().unlock();
		}
	}


	@Override
	public int getTotalNumRunsOfConfig(ParamConfiguration config) {
		myLock.readLock().lock();
		try {
			return runHistory.getTotalNumRunsOfConfig(config);
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public double getTotalRunCost() {
		myLock.readLock().lock();
		try {
			return runHistory.getTotalRunCost();
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public double[] getRunResponseValues() {
		myLock.readLock().lock();
		try {
			return runHistory.getRunResponseValues();
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public Set<ProblemInstance> getUniqueInstancesRan() {
		myLock.readLock().lock();
		try {
			return runHistory.getUniqueInstancesRan();
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public Set<ParamConfiguration> getUniqueParamConfigurations() {
		myLock.readLock().lock();
		try {
			return runHistory.getUniqueParamConfigurations();
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public int[][] getParameterConfigurationInstancesRanByIndex() {
		myLock.readLock().lock();
		try {
			return runHistory.getParameterConfigurationInstancesRanByIndex();
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public boolean[] getCensoredFlagForRuns() {
		myLock.readLock().lock();
		try {
			return runHistory.getCensoredFlagForRuns();
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public List<ParamConfiguration> getAllParameterConfigurationsRan() {
		myLock.readLock().lock();
		try {
			return runHistory.getAllParameterConfigurationsRan();
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public double[][] getAllConfigurationsRanInValueArrayForm() {
		myLock.readLock().lock();
		try {
			return runHistory.getAllConfigurationsRanInValueArrayForm();
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public List<AlgorithmRun> getAlgorithmRuns() {
		myLock.readLock().lock();
		try {
			return runHistory.getAlgorithmRuns();
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public List<RunData> getAlgorithmRunData() {
		myLock.readLock().lock();
		try {
			return runHistory.getAlgorithmRunData();
		} finally
		{
			myLock.readLock().unlock();
	
		}
		
	
	}



	@Override
	public Set<ProblemInstanceSeedPair> getCappedAlgorithmInstanceSeedPairs(
			ParamConfiguration config) {
		myLock.readLock().lock();
		try {
			return runHistory.getCappedAlgorithmInstanceSeedPairs(config);
		} finally
		{
			myLock.readLock().unlock();
	
		}
	}

	@Override
	public double getEmpiricalPISPCost(ParamConfiguration config,
			Set<ProblemInstanceSeedPair> instanceSet, double cutoffTime) {
		myLock.readLock().lock();
		try {
			return runHistory.getEmpiricalPISPCost(config, instanceSet, cutoffTime);
		} finally
		{
			myLock.readLock().unlock();
	
		}
		
	}

	@Override
	public double getEmpiricalPISPCost(ParamConfiguration config,
			Set<ProblemInstanceSeedPair> instanceSet, double cutoffTime,
			Map<ProblemInstance, Map<Long, Double>> hallucinatedValues) {
		myLock.readLock().lock();
		try {
			return runHistory.getEmpiricalPISPCost(config, instanceSet, cutoffTime, hallucinatedValues);
		} finally
		{
			myLock.readLock().unlock();
	
		}
	}

	@Override
	public int getThetaIdx(ParamConfiguration configuration) {
		myLock.readLock().lock();
		try {
			return runHistory.getThetaIdx(configuration);
		} finally
		{
			myLock.readLock().unlock();
	
		}
	}

	@Override
	public double getEmpiricalCost(ParamConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime,
			double minimumResponseValue) {
		myLock.readLock().lock();
		try {
			return runHistory.getEmpiricalCost(config, instanceSet, cutoffTime);
		} finally
		{
			myLock.readLock().unlock();
	
		}
	}

	@Override
	public int getNumberOfUniqueProblemInstanceSeedPairsForConfiguration(
			ParamConfiguration config) {
		myLock.readLock().lock();
		try {
			return runHistory.getNumberOfUniqueProblemInstanceSeedPairsForConfiguration(config);
		} finally
		{
			myLock.readLock().unlock();
	
		}
	}


	@Override
	public void readLock() {
		myLock.readLock().lock();
	}


	@Override
	public void releaseReadLock() {
		myLock.readLock().unlock();
		
	}

	@Override
	public List<AlgorithmRun> getAlgorithmRunData(ParamConfiguration config) {
		myLock.readLock().lock();
		try {
			return runHistory.getAlgorithmRunData(config);
		} finally
		{
			myLock.readLock().unlock();
	
		}
	}

	@Override
	public Map<ProblemInstance, LinkedHashMap<Long, Double>> getPerformanceForConfig(
			ParamConfiguration configuration) {
		myLock.readLock().lock();
		try {
			return runHistory.getPerformanceForConfig(configuration);
		} finally
		{
			myLock.readLock().unlock();
		}
	}

	@Override
	public List<Long> getSeedsUsedByInstance(ProblemInstance pi) {
		myLock.readLock().lock();
		try {
			return runHistory.getSeedsUsedByInstance(pi);
		} finally
		{
			myLock.readLock().unlock();
		}
	}


	

	

}
