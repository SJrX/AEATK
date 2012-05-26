package ca.ubc.cs.beta.smac.state.legacy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import ca.ubc.cs.beta.ac.RunResult;
import ca.ubc.cs.beta.ac.config.ProblemInstance;
import ca.ubc.cs.beta.ac.config.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.ac.config.RunConfig;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.configspace.ParamConfiguration;
import ca.ubc.cs.beta.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.probleminstance.InstanceSeedGenerator;
import ca.ubc.cs.beta.probleminstance.RandomInstanceSeedGenerator;
import ca.ubc.cs.beta.smac.OverallObjective;
import ca.ubc.cs.beta.smac.RunObjective;
import ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun;
import ca.ubc.cs.beta.smac.ac.runs.ExistingAlgorithmRun;
import ca.ubc.cs.beta.smac.exceptions.StateSerializationException;
import ca.ubc.cs.beta.smac.history.NewRunHistory;
import ca.ubc.cs.beta.smac.history.RunHistory;
import ca.ubc.cs.beta.smac.state.RandomPoolType;
import ca.ubc.cs.beta.smac.state.StateDeserializer;

/**
 * Supports deserializing the state from the files saved by the LegacyStateSerializer instance,
 * it should also support restoring from MATLAB save files.
 * 
 * NOTE: We do not support restoring MATLAB save files currently, as there is no way of getting the param configuration object
 * 
 * 
 * 
 * See the interface for more details on how this interface works or why it does what it does.
 * 
 * Note: We use the paramstrings file to restore state and not the uniq_configurations, so we can have better validation
 * @author seramage
 *
 */
public class LegacyStateDeserializer implements StateDeserializer {

	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private final RunHistory runHistory;
	private final InstanceSeedGenerator instanceSeedGenerator;
	private final EnumMap<RandomPoolType, Random> randomMap;
	private final int iteration;
	private final ParamConfiguration incumbent;
	private static int newSeeds = 1024;	
	/**
	 * Stores whether or not we were able to recover a complete state 
	 */
	private final boolean incompleteSavedState;
	
	@SuppressWarnings("unchecked")
	public LegacyStateDeserializer(String restoreFromPath, String id, int iteration, ParamConfigurationSpace configSpace, OverallObjective overallObj, RunObjective runObj, List<ProblemInstance> instances, AlgorithmExecutionConfig execConfig) 
	{
			if (configSpace == null) throw new IllegalArgumentException("Config Space cannot be null");
			if(overallObj == null) throw new IllegalArgumentException("Overall Objective Cannot be null");
			if(runObj == null) throw new IllegalArgumentException("Run Objective cannot be null");
			if(instances == null) throw new IllegalArgumentException("Instances cannot be null");
			if(execConfig == null) throw new IllegalArgumentException("Execution Config cannot be null");
			
			Object[] args = { iteration, id, restoreFromPath };
			log.info("Trying to restore iteration: {} id: {} from path: {}", args );
			try {
				
				
			FileLocations f = getLocations(restoreFromPath, id, iteration);
			f.validate();
			
			log.trace("Run and Results File: {}", f.runHistoryFile.getAbsolutePath());
			log.trace("Param Strings File: {}", f.paramStringsFile.getAbsolutePath());
			if(f.javaObjDumpFile != null)
			{
				log.trace("Java Objective Dump File: {}",f.javaObjDumpFile.getAbsolutePath());
			} else
			{
				log.trace("Java Objective Dump File: null");
			}
			
			if(f.javaObjDumpFile != null)
			{
				incompleteSavedState = false;
				/**
				 * Java Object
				 */
				ObjectInputStream oReader =  null;
				try {
					oReader =  new ObjectInputStream(new FileInputStream(f.javaObjDumpFile));
						
					int storedIteration = oReader.readInt();
					if(storedIteration != iteration) 
					{
						
						throw new IllegalStateException("File Found claimed to be for iteration " + iteration + " but contained iteration " + storedIteration + " in file: " + f.javaObjDumpFile.getAbsolutePath());
					}
					this.iteration = storedIteration;
					
					randomMap = (EnumMap<RandomPoolType, Random>) oReader.readObject();
					instanceSeedGenerator = (InstanceSeedGenerator) oReader.readObject();
					
					incumbent = configSpace.getConfigurationFromString((String) oReader.readObject(), StringFormat.STATEFILE_SYNTAX);
					
				} finally
				{
					if(oReader != null) oReader.close(); 
				}
			} else
			{
				incompleteSavedState = true;
				randomMap = null;
				//The RunHistory object will need an instanceseed generator. We will not allow this to be returned to the client however
				instanceSeedGenerator = new RandomInstanceSeedGenerator(0, 0);
				this.incumbent = null;
				this.iteration = iteration;
			}
			
			
			

			
			/**
			 * Get Param Strings
			 */
			BufferedReader reader = null;
			
			Map<Integer,ParamConfiguration> configMap = new HashMap<Integer, ParamConfiguration>();
			
			try {
				reader =  new BufferedReader(new FileReader(f.paramStringsFile));
				
				String line = null;
			
				while( (line = reader.readLine()) != null)
				{
					log.trace("Parsing config line: {}", line);
					String[] lineResults = line.split(":",2);
					
					if(lineResults.length != 2) throw new IllegalArgumentException("Configuration Param Strings File is corrupted, no colon detected on line: \"" + line + "\"");
					
					Integer configId = Integer.valueOf(lineResults[0]);
					configMap.put(configId,configSpace.getConfigurationFromString(lineResults[1], StringFormat.STATEFILE_SYNTAX));
					
					
				}
			} finally
			{
				if (reader != null) reader.close();
			}
			
			
			/**
			 * Create map of InstanceID to instance
			 */
			Map<Integer, ProblemInstance> instanceMap = new HashMap<Integer, ProblemInstance>();
			for(ProblemInstance instance : instances)
			{
				instanceMap.put(instance.getInstanceID(), instance);
			}
			
			
			/**
			 * Create Run History Object
			 */
			
			runHistory = new NewRunHistory(instanceSeedGenerator, overallObj, overallObj, runObj);
			
			
			CSVReader runlist = null;
			try {
					runlist = new CSVReader( new FileReader(f.runHistoryFile));
			
				String[] runHistoryLine = null;
				int i=0;
				boolean censoredErrorLogged = false;
				boolean seedErrorLogged = false;
				boolean runLengthErrorLogged = false;
				
				while((runHistoryLine = runlist.readNext()) != null)
				{
					i++;
					try {
						//The magic constants here are basically from LegacyStateSerializer
						//Perhaps that should be refactored as this is fragile
						
						//Don't need cumulative sum NOR run number (index 0 and 12).
						int thetaIdx = Integer.valueOf(runHistoryLine[1]);
						int instanceIdx = Integer.valueOf(runHistoryLine[2]);
						double y = Double.valueOf(runHistoryLine[3]);
						boolean isCensored = ((runHistoryLine[4].trim().equals("0") ? false : true));
						if(isCensored)
						{
							if(!censoredErrorLogged)
							{
								log.error("Censored Run Detected not supported but continuing on line {} ", i );
								censoredErrorLogged = true;
							}
							
						}
						double cutOffTime = Double.valueOf(runHistoryLine[5]);
						long seed = -1;
						try {
							seed = Long.valueOf(runHistoryLine[6]);
							if(seed == -1)
							{
								log.trace("Seed is -1 which means it was deterministic, using a random seed");
								seed = newSeeds++;
							}
						} catch(NumberFormatException e)
						{
							seed = Double.valueOf(runHistoryLine[6]).longValue();
							if(!seedErrorLogged)
							{
								log.warn("Seed value specified in imprecise format on line {}  contents: {}", i,  runHistoryLine);
								seedErrorLogged = true;
							}
							
						}
						double runtime = Double.valueOf(runHistoryLine[7]);
						
						int runLength = -1;
						
						try {
							runLength = Integer.valueOf(runHistoryLine[8]); 
						} catch(NumberFormatException e)
						{
							runLength = Double.valueOf(runHistoryLine[8]).intValue();
							if(!runLengthErrorLogged)
							{
								log.warn("RunLength value specified in imprecise format on line {}  contents: {}", i,  runHistoryLine);
								runLengthErrorLogged = true;
							}
							
						}
						
						RunResult runResult  = (Integer.valueOf(runHistoryLine[9]) == 1) ? RunResult.SAT : RunResult.TIMEOUT;
						int quality = (int) (double) Double.valueOf(runHistoryLine[10]);
						int runIteration = Integer.valueOf(runHistoryLine[11]);

						if(runIteration > iteration) break;
						if(runIteration < runHistory.getIteration())
						{
							throw new StateSerializationException("Run History File contains run data for iteration " + runIteration + " but we have already played back to " + runHistory.getIteration());
						}
						while(runIteration > runHistory.getIteration())
						{
							runHistory.incrementIteration();
						}
						
						
						
						
						ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(instanceMap.get(instanceIdx), seed); 
						RunConfig runConfig = new RunConfig(pisp, cutOffTime, configMap.get(thetaIdx),isCensored);
						
						
						
						StringBuffer resultLine = new StringBuffer();
						
						resultLine.append(runResult.getResultCode()).append(", ");
						resultLine.append(runtime).append(", ");
						resultLine.append(runLength).append(", ");
						resultLine.append(quality).append(", ");
						resultLine.append(seed);
						
						AlgorithmRun run = new ExistingAlgorithmRun(execConfig, runConfig, resultLine.toString());
						
						log.trace("Appending new run to runHistory: ", run);
						runHistory.append(run);
					} catch(RuntimeException e)
					{
						throw new StateSerializationException("Error occured while parsing the following line of the runHistory file: " + i + " data "+ Arrays.toString(runHistoryLine), e);
					}
				}
			
			} finally
			{
				if(runlist != null) runlist.close();
			}
			
			
			
			
		} catch(IOException e)
		{
			throw new StateSerializationException("Could not restore state", e);
		} catch (ClassNotFoundException e) {
			throw new StateSerializationException("Java Serialization Failed", e);
		}
		
		
		log.info("Successfully restored iteration: {} id: {} from path: {}", args );
			
	}

	
	@Override
	public RunHistory getRunHistory() {

		return runHistory;
	}

	@Override
	public Random getPRNG(RandomPoolType t) {
		if(incompleteSavedState) throw new StateSerializationException("This is an incomplete state with no java objects found");
		return randomMap.get(t);
	}

	@Override
	public InstanceSeedGenerator getInstanceSeedGenerator() {
		if(incompleteSavedState) throw new StateSerializationException("This is an incomplete state with no java objects found");
		return instanceSeedGenerator;
	}
	/**
	 * Logic for determining which files to get data out of.
	 * Each of the files returned should have the necessary data to restore the state.
	 * @param path
	 * @param id
	 * @param iteration
	 * @return
	 */
	
	private FileLocations getLocations(String path, String id, int iteration)
	{
		/**
		 * Restore algorithm is as follows:
		 * Find a runHistory and paramString containing the iteration data
		 * Find our current java object dump for our rand object.
		 * If it doesn't contain the instanceSeedFile, then load the instance Seed file from base version and replay
		 */
		FileLocations f = new FileLocations();
		File restoreDirectory = new File(path);
	
		Set<String> filenames = new HashSet<String>();
		
		if(!restoreDirectory.exists()) throw new IllegalArgumentException("Restore Directory specified: " + path + " does not exist");
		filenames.addAll(Arrays.asList(restoreDirectory.list()));
		int savedFileIteration = 0;
		boolean filesFound = false;
		for(savedFileIteration=iteration; savedFileIteration <= 2*iteration; savedFileIteration++ )
		{
			
			if(filenames.contains(LegacyStateFactory.getRunAndResultsFilename("", id, savedFileIteration)))
			{
				if(filenames.contains(LegacyStateFactory.getParamStringsFilename("", id, savedFileIteration)))
				{
					f.runHistoryFile = new File(LegacyStateFactory.getRunAndResultsFilename(path, id, savedFileIteration));
					f.paramStringsFile = new File(LegacyStateFactory.getParamStringsFilename(path, id, savedFileIteration));

					filesFound = true;
					break;
				} else
				{
					log.warn("Didn't find paramStrings file: {} but did find Run and Results file: {}, it's possible saved state directory structure is corrupted.", LegacyStateFactory.getParamStringsFilename(path, id, savedFileIteration), LegacyStateFactory.getRunAndResultsFilename(path, id, savedFileIteration) );
					continue;
				}
			}
			
			if(filenames.contains(LegacyStateFactory.getParamStringsFilename("", "CRASH", savedFileIteration)))
			{
				if(filenames.contains(LegacyStateFactory.getRunAndResultsFilename("", "CRASH", savedFileIteration)))
				{
					
					filesFound = true;
					f.runHistoryFile = new File(LegacyStateFactory.getRunAndResultsFilename(path, "CRASH", savedFileIteration));
					f.paramStringsFile = new File(LegacyStateFactory.getParamStringsFilename(path, "CRASH", savedFileIteration));

					break;
				} else
				{
					log.warn("Found paramStrings file: {} but no Run and Results file: {}, it's possible saved state directory structure is corrupted.", LegacyStateFactory.getUniqConfigurationsFilename(path, id, savedFileIteration), LegacyStateFactory.getParamStringsFilename(path, id, savedFileIteration) );
					continue;
				}
			}
			
			
			
			
			
			
		}
		
		if(!filesFound)
		{
			throw new StateSerializationException("Could not find data files to restore iteration " + iteration + " with id " + id + " in path " + path );
		}
		
		
		
		
		f.javaObjDumpFile = new File(LegacyStateFactory.getJavaObjectDumpFilename(path, id, iteration));
		
		if(!f.javaObjDumpFile.exists())
		{
			log.info("Could not find object dump file to restore from {}",f.javaObjDumpFile.getAbsolutePath());
			f.javaObjDumpFile = new File(LegacyStateFactory.getJavaQuickBackObjectDumpFilename(path, id, iteration));
			if(LegacyStateFactory.readIterationFromObjectFile(f.javaObjDumpFile) != iteration)
			{
				log.info("Could not find object dump file to restore from {}",f.javaObjDumpFile.getAbsolutePath());
				f.javaObjDumpFile = new File(LegacyStateFactory.getJavaQuickObjectDumpFilename(path, id, iteration));
				if(LegacyStateFactory.readIterationFromObjectFile(f.javaObjDumpFile) != iteration)
				{
					log.info("Could not find object dump file to restore from {}",f.javaObjDumpFile.getAbsolutePath());
					f.javaObjDumpFile = new File(LegacyStateFactory.getJavaObjectDumpFilename(path, "CRASH", iteration));
					if(LegacyStateFactory.readIterationFromObjectFile(f.javaObjDumpFile) != iteration)
					{
						
						log.info("Could not find object dump file to restore from {}, No Java Object Dump will be loaded",f.javaObjDumpFile.getAbsolutePath());
						f.javaObjDumpFile = null;
						//throw new StateSerializationException("Java Object File does not exist " +  LegacyStateFactory.getJavaQuickObjectDumpFilename(path, id, iteration));
					}
					
				}
						
			}
		}
	 
			
			
			
		
		
		
		
		return f;
		
	}
	private class FileLocations
	{
		public File runHistoryFile;
		public File paramStringsFile;
		public File javaObjDumpFile;
		
		public void validate()
		{
			if ((runHistoryFile != null) && (!runHistoryFile.exists())) throw new StateSerializationException("Run History File does not exist");
			if ((javaObjDumpFile != null) && (!javaObjDumpFile.exists())) throw new StateSerializationException("Java Object File does not exist");
			if ((paramStringsFile != null) && (!paramStringsFile.exists())) throw new StateSerializationException("Param Strings File does not exist");
			
		}
	}
	@Override
	public int getIteration() {
		return iteration;
	}


	@Override
	public ParamConfiguration getIncumbent() {
		if(incompleteSavedState) throw new StateSerializationException("This is an incomplete state with no java objects found");
		return incumbent;
	}
	
	
	

}
