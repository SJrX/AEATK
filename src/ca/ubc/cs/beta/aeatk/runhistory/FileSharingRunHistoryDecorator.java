package ca.ubc.cs.beta.aeatk.runhistory;



import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aeatk.json.JSONConverter;
import ca.ubc.cs.beta.aeatk.objectives.OverallObjective;
import ca.ubc.cs.beta.aeatk.objectives.RunObjective;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;

public class FileSharingRunHistoryDecorator implements ThreadSafeRunHistory {

	private final RunHistory runHistory;
	
	ReadWriteLockThreadTracker rwltt = new ReadWriteLockThreadTracker();
	
	private final static Logger log = LoggerFactory.getLogger(FileSharingRunHistoryDecorator.class);
	
	private final String sharedFileName;
	private final FileOutputStream fout;
	private final ObjectMapper map = new ObjectMapper();
	
	private final static String JSON_FILE_PREFIX = "live-rundata-";
	private final static String JSON_FILE_SUFFIX = ".json";
	
	private final File outputDir;
	private final int MSBetweenUpdates;
	
	private final  JsonFactory factory = new JsonFactory();

	private final JsonGenerator g;
	
	public FileSharingRunHistoryDecorator(RunHistory runHistory, File directory, int outputID, List<ProblemInstance> pis, int secondsBetweenUpdates)
	{
		this.runHistory = runHistory;
		this.outputDir = directory;
		
		this.MSBetweenUpdates = secondsBetweenUpdates * 1000;
		sharedFileName = directory + File.separator + JSON_FILE_PREFIX+outputID + JSON_FILE_SUFFIX;
		File f = new File(sharedFileName);
		
		try {
			
			fout = new FileOutputStream(f);
			
			factory.setCodec(map);
			
			g = factory.createGenerator(fout);
			
			SimpleModule sModule = new SimpleModule("MyModule", new Version(1, 0, 0, null));
			map.registerModule(sModule);
			
			g.writeObject(pis);
			g.flush();
			
			//map.writeValue(fout, pis);
		} catch (IOException e) {
			throw new IllegalStateException("Couldn't create shared model output file :" + sharedFileName);
		}
	}
	
	@Override
	public void append(Collection<AlgorithmRunResult> runs)
			throws DuplicateRunException {

	
		lockWrite();
		
		try {
			for(AlgorithmRunResult run : runs)
			{
				//log.debug("Atomically appending run {} " + run.getRunConfig());
				
				runHistory.append(run);
	
				
				try {
					g.writeObject(run);
					g.flush();
					//map.writeValue(fout, run);
					//fout.flush();
				} catch (IOException e) {
					throw new IllegalStateException("Couldn't save data as JSON", e);
				}
				
			}
			
		} finally
		{
			unlockWrite();
		}
		
	}
	

	
	private long lastUpdateTime = 0;
	private final void reReadFiles()
	{
	
		try 
		{
			lockWrite();
			if (System.currentTimeMillis() - lastUpdateTime < MSBetweenUpdates)
			{
				return;
			}
			try 
			{
				
				this.outputDir.listFiles(new FileFilter()
				{
					@Override
					public boolean accept(File pathname) {
						return pathname.getName().startsWith(JSON_FILE_PREFIX) && pathname.getName().endsWith(JSON_FILE_SUFFIX);
					}
				});
			
				
			
			
			
			
			
			
			
			
			} finally
			{
				lastUpdateTime = System.currentTimeMillis();
			}
		} finally
		{
			unlockWrite();
		}
		
	}
	@Override
	public void append(AlgorithmRunResult run) throws DuplicateRunException {
		this.append(Collections.singleton(run));
	}

	@Override
	public RunObjective getRunObjective() {
		lockRead();
		
		try {
			return runHistory.getRunObjective();
		} finally
		{
			unlockRead();
		}
	}

	@Override
	public OverallObjective getOverallObjective() {
		lockRead();
		try {
			return runHistory.getOverallObjective();
		} finally
		{
			unlockRead();
		}
	}

	@Override
	public void incrementIteration() {
		lockWrite();
		
		try {
			 runHistory.incrementIteration();
		} finally
		{
			unlockWrite();
		}
	}

	@Override
	public int getIteration() {

		lockRead();
		try {
			return runHistory.getIteration();
		} finally
		{
			unlockRead();
		}
	}

	@Override
	public Set<ProblemInstance> getProblemInstancesRan(ParameterConfiguration config) {
		lockRead();
		try {
			return runHistory.getProblemInstancesRan(config);
		} finally
		{
			unlockRead();
		}
	}

	@Override
	public Set<ProblemInstanceSeedPair> getProblemInstanceSeedPairsRan(
			ParameterConfiguration config) {
		
		lockRead();
		try {
			return runHistory.getProblemInstanceSeedPairsRan(config);
		} finally
		{
			unlockRead();
		}
	}

	@Override
	public double getEmpiricalCost(ParameterConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime) {
		lockRead();
		try {
			return runHistory.getEmpiricalCost(config, instanceSet, cutoffTime);
		} finally
		{
			unlockRead();
		}
	}

	@Override
	public double getEmpiricalCost(ParameterConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime,
			Map<ProblemInstance, Map<Long, Double>> hallucinatedValues) {
		lockRead();
		try {
			return runHistory.getEmpiricalCost(config, instanceSet, cutoffTime, hallucinatedValues);
		} finally
		{
			unlockRead();
		}
	}

	@Override
	public double getEmpiricalCost(ParameterConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime,
			Map<ProblemInstance, Map<Long, Double>> hallucinatedValues,
			double minimumResponseValue) {
		lockRead();
		try {
			return runHistory.getEmpiricalCost(config, instanceSet, cutoffTime, hallucinatedValues, minimumResponseValue);
		} finally
		{
			unlockRead();
		}
	}




	@Override
	public double getTotalRunCost() {
		lockRead();
		try {
			return runHistory.getTotalRunCost();
		} finally
		{
			unlockRead();
		}
	}
	
	@Override
	public Set<ProblemInstance> getUniqueInstancesRan() {
		lockRead();
		try {
			return runHistory.getUniqueInstancesRan();
		} finally
		{
			unlockRead();
		}
	}

	@Override
	public Set<ParameterConfiguration> getUniqueParamConfigurations() {
		lockRead();
		try {
			return runHistory.getUniqueParamConfigurations();
		} finally
		{
			unlockRead();
		}
	}

	@Override
	public int[][] getParameterConfigurationInstancesRanByIndex() {
		lockRead();
		try {
			return runHistory.getParameterConfigurationInstancesRanByIndex();
		} finally
		{
			unlockRead();
		}
	}


	
	@Override
	public List<ParameterConfiguration> getAllParameterConfigurationsRan() {
		lockRead();
		try {
			return runHistory.getAllParameterConfigurationsRan();
		} finally
		{
			unlockRead();
		}
	}

	@Override
	public double[][] getAllConfigurationsRanInValueArrayForm() {
		lockRead();
		try {
			return runHistory.getAllConfigurationsRanInValueArrayForm();
		} finally
		{
			unlockRead();
		}
	}


	@Override
	public List<RunData> getAlgorithmRunData() {
		lockRead();
		try {
			return runHistory.getAlgorithmRunData();
		} finally
		{
			unlockRead();
	
		}
		
	
	}



	@Override
	public Set<ProblemInstanceSeedPair> getEarlyCensoredProblemInstanceSeedPairs(
			ParameterConfiguration config) {
		lockRead();
		try {
			return runHistory.getEarlyCensoredProblemInstanceSeedPairs(config);
		} finally
		{
			unlockRead();
	
		}
	}



	@Override
	public int getThetaIdx(ParameterConfiguration configuration) {
		lockRead();
		try {
			return runHistory.getThetaIdx(configuration);
		} finally
		{
			unlockRead();
	
		}
	}

	@Override
	public double getEmpiricalCost(ParameterConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime,
			double minimumResponseValue) {
		lockRead();
		try {
			return runHistory.getEmpiricalCost(config, instanceSet, cutoffTime);
		} finally
		{
			unlockRead();
	
		}
	}

	@Override
	public int getNumberOfUniqueProblemInstanceSeedPairsForConfiguration(
			ParameterConfiguration config) {
		lockRead();
		try {
			return runHistory.getNumberOfUniqueProblemInstanceSeedPairsForConfiguration(config);
		} finally
		{
			unlockRead();
	
		}
	}


	@Override
	public void readLock() {
		lockRead();
	}


	@Override
	public void releaseReadLock() {
		unlockRead();
		
	}

	@Override
	public List<AlgorithmRunResult> getAlgorithmRunsExcludingRedundant(ParameterConfiguration config) {
		lockRead();
		try {
			return runHistory.getAlgorithmRunsExcludingRedundant(config);
		} finally
		{
			unlockRead();
	
		}
	}
	

	@Override
	public int getTotalNumRunsOfConfigExcludingRedundant(ParameterConfiguration config) {
		lockRead();
		try {
			return runHistory.getTotalNumRunsOfConfigExcludingRedundant(config);
		} finally
		{
			unlockRead();
		}
	}
	
	@Override
	public List<AlgorithmRunResult> getAlgorithmRunsExcludingRedundant() {
		lockRead();
		try {
			return runHistory.getAlgorithmRunsExcludingRedundant();
		} finally
		{
			unlockRead();
		}
	}
	
	@Override
	public List<AlgorithmRunResult> getAlgorithmRunsIncludingRedundant(ParameterConfiguration config) {
		lockRead();
		try {
			return runHistory.getAlgorithmRunsIncludingRedundant(config);
		} finally
		{
			unlockRead();
	
		}
	}
	

	@Override
	public int getTotalNumRunsOfConfigIncludingRedundant(ParameterConfiguration config) {
		lockRead();
		try {
			return runHistory.getTotalNumRunsOfConfigIncludingRedundant(config);
		} finally
		{
			unlockRead();
		}
	}
	
	@Override
	public List<AlgorithmRunResult> getAlgorithmRunsIncludingRedundant() {
		lockRead();
		try {
			return runHistory.getAlgorithmRunsIncludingRedundant();
		} finally
		{
			unlockRead();
		}
	}
	
	

	@Override
	public Map<ProblemInstance, LinkedHashMap<Long, Double>> getPerformanceForConfig(
			ParameterConfiguration configuration) {
		lockRead();
		try {
			return runHistory.getPerformanceForConfig(configuration);
		} finally
		{
			unlockRead();
		}
	}

	@Override
	public List<Long> getSeedsUsedByInstance(ProblemInstance pi) {
		lockRead();
		try {
			return runHistory.getSeedsUsedByInstance(pi);
		} finally
		{
			unlockRead();
		}
	}
	
	
	public void lockRead()
	{
		this.rwltt.lockRead();
	
	}
	
	private void unlockRead()
	{
		this.rwltt.unlockRead();
	}
	
	private void lockWrite()
	{
		this.rwltt.lockWrite();
	}
	
	private void unlockWrite()
	{
		this.rwltt.unlockWrite();
		
	}

	@Override
	public int getOrCreateThetaIdx(ParameterConfiguration config) {
		lockWrite();
		try {
			return this.runHistory.getOrCreateThetaIdx(config);
		} finally
		{
			unlockWrite();
		}
	
		
	}

	@Override
	public double getEmpiricalCostLowerBound(ParameterConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime) {
		return this.runHistory.getEmpiricalCostLowerBound(config, instanceSet, cutoffTime);
	}

	@Override
	public double getEmpiricalCostUpperBound(ParameterConfiguration config,
			Set<ProblemInstance> instanceSet, double cutoffTime) {
		return this.runHistory.getEmpiricalCostUpperBound(config, instanceSet, cutoffTime);
	}


	

	

}
