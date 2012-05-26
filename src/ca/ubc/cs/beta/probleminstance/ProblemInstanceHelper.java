package ca.ubc.cs.beta.probleminstance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import ca.ubc.cs.beta.ac.config.ProblemInstance;
import ca.ubc.cs.beta.models.surrogate.helpers.csv.ConfigCSVFileHelper;

import com.beust.jcommander.ParameterException;

public class ProblemInstanceHelper {
	
	private static File getFileForPath(String context, String path)
	{
		File f;
		logger.trace("Trying to find file with context {} and path {}", context, path);
		if(path.substring(0, 1).equals(File.separator))
		{
			logger.trace("Absolute path given for path, checking {}", path);
			f = new File(path);
		} else
		{
			Object[] args = { context, File.separator, path };
			logger.trace("Relative path given for path, checking {}{}{}", args);
			f = new File(context + File.separator + path);
		}
		
		if(!f.exists())
		{
			throw new ParameterException("Could not find needed file:" + path + " Context:" + context);
		}
		
		return f;
	}
	

	private static Logger logger = LoggerFactory.getLogger(ProblemInstanceHelper.class);
	
	public static InstanceListWithSeeds getInstances(String filename, String experimentDir, boolean checkFileExistsOnDisk) throws IOException
	{
		return getInstances(filename, experimentDir, null, checkFileExistsOnDisk);
	}
	
	private static Map<String, ProblemInstance> cachedProblemInstances = new HashMap<String, ProblemInstance>();

	
	public static InstanceListWithSeeds getInstances(String filename, String experimentDir, String featureFileName, boolean checkFileExistsOnDisk) throws IOException	{
	
		return getInstances(filename, experimentDir, featureFileName, checkFileExistsOnDisk, 0, Integer.MAX_VALUE);
		
	}
	
	
	public static InstanceListWithSeeds getInstances(String filename, String experimentDir, String featureFileName, boolean checkFileExistsOnDisk, long seed, int maxSeedsPerInstance) throws IOException {
		
		logger.info("Loading instances from file: {} and experiment dir {}", filename, experimentDir);
		

		List<ProblemInstance> instances = new ArrayList<ProblemInstance>();
		Set<ProblemInstance> instancesSet = new HashSet<ProblemInstance>();
		
		
		
		String line = "";
		
		
		int instID=1; 
		
		
		
		Map<String, Map<String, Double>> featuresMap = new LinkedHashMap<String, Map<String, Double>>();
		
		int numberOfFeatures = 0;
		if(featureFileName != null)
		{
			logger.info("Feature File specified reading features from: {} ", featureFileName);
			File featureFile = getFileForPath(experimentDir, featureFileName);
			
			if(!featureFile.exists())
			{
				throw new ParameterException("Feature file given does not exist " + featureFile);
			}
			CSVReader featureCSV = new CSVReader(new FileReader(featureFile));
			ConfigCSVFileHelper features = new ConfigCSVFileHelper(featureCSV.readAll(),1,1);
			
			numberOfFeatures = features.getNumberOfDataColumns();
			logger.info("Feature File specifies: {} features for {} instances", numberOfFeatures, features.getNumberOfDataRows() );
			
			
			for(int i=0; i  < features.getNumberOfDataRows(); i++)
			{
				TreeMap<String, Double> instFeatMap = new TreeMap<String, Double>();
				
				featuresMap.put(features.getKeyForDataRow(i).replaceAll("//", "/"), Collections.unmodifiableMap(instFeatMap));
				
				for (int j=0; j < features.getNumberOfDataColumns(); j++)
				{		
						String key = features.getDataKeyByIndex(j);
						Double value = features.getDoubleDataValue(i, j);
						instFeatMap.put(key, value);
				}
			}
		}
		
		
		
		List<String> instanceList = new ArrayList<String>(featuresMap.size());
		InstanceSeedGenerator gen; 
		if(filename != null)
		{
			
			
			File instanceListFile = getFileForPath(experimentDir, filename);
			
			InstanceListWithSeeds insc = getListAndSeedGen(instanceListFile,seed, maxSeedsPerInstance);
			instanceList = insc.getInstancesByName();
			gen = insc.getSeedGen();
			
			
			
		} else
		{
			instanceList.addAll(featuresMap.keySet());
			gen = new RandomInstanceSeedGenerator(instanceList.size(), seed, maxSeedsPerInstance);
			
		}
		
		
		for(String instanceFile : instanceList)
		{
			
			
			if(checkFileExistsOnDisk)
			{
				File f = getFileForPath(experimentDir, instanceFile);
				
				//Should store the absolute file name if the file exists on disk
				//If we don't check if the file exists on disks we don't know whether to add experimentDir to it
				//This is primarily used for Surrogates
				
				instanceFile = f.getAbsolutePath();
				
				if(!f.exists())
				{
					logger.warn("Instance {} does not exist on disk", f.getAbsolutePath());
				}
			}
			Map<String, Double> features;
			if(featureFileName != null)
			{
				
				String[] possibleFiles = { instanceFile, instanceFile.replace(experimentDir, ""), instanceFile.replaceAll("//", "/"), instanceFile.replace(experimentDir, "").replaceAll("//","/")};
				
				features = null;
				for(String possibleFile : possibleFiles)
				{
					features = featuresMap.get(possibleFile.trim());
					
					if(features != null) 
					{
						logger.debug("Matched Features for file name : {}",possibleFile);
						break;
					} else
					{
						logger.debug("No features found for file name : {}",possibleFile);
					}
					
				}
				/*
				if(features == null)
				{
					String path = instanceFile.replace(experimentDir,"");
					features = featuresMap.get(path);
				}*/
				
				
				
				
				if(features == null)
				{
					logger.warn("Could not find features for instance {} trying more creative matching, may be error prone and slow [probably not really]", instanceFile);
					
					for(Entry<String, Map<String, Double>> e : featuresMap.entrySet())						
					{
						if(instanceFile.endsWith(e.getKey()))
						{
							logger.info("Matched instance {} with this entry {}", instanceFile, e.getKey());
							features = e.getValue();
							break;
						} else
						{
							logger.trace("Didn't match ({}) with ({})", instanceFile, e.getKey());
						}
					}
					
				}
				if(features == null)
				{
					throw new ParameterException("Feature file : " + featureFileName + " does not contain feature data for instance: " + instanceFile);
				}
				
				
				if(features.size() != numberOfFeatures)
				{
					throw new ParameterException("Feature file : " + featureFileName + " contains " + features.size() + " for instance: " + instanceFile +  " but expected " + numberOfFeatures );
				}
			} else
			{
				features = Collections.emptyMap();
			}
			
			instanceFile = instanceFile.replaceAll("//", "/");
			ProblemInstance ai;
			
			if(cachedProblemInstances.containsKey(instanceFile))
			{
				
				logger.trace("Instance file has already been loaded once this runtime, using cached instance of {}", instanceFile);
				ai = cachedProblemInstances.get(instanceFile);
				if(!ai.getFeatures().equals(features))
				{
					logger.error("We previously loaded an instance for filename {} but the instance Features don't match");
				}
				
			} else
			{
				ai = new ProblemInstance(instanceFile, instID++, features);
				cachedProblemInstances.put(instanceFile, ai);
			}
			
			

			if(instancesSet.contains(ai))
			{
				logger.warn("Instance file seems to contain duplicate entries for the following filename {}", line);
			}
			
			instances.add(ai);
			instancesSet.add(ai);
			
		}
		logger.info("Found Instances loaded");
		return new InstanceListWithSeeds(gen, instances);
		
		
	}

	enum InstanceFileFormat
	{
		NEW_CSV_SEED_INSTANCE_PER_ROW,
		NEW_CSV_INSTANCE_PER_ROW,
		LEGACY_INSTANCE_PER_ROW,
		LEGACY_SEED_INSTANCE_PER_ROW
	}
	private static InstanceListWithSeeds getListAndSeedGen(File instanceListFile, long seed, int maxSeedsPerConfig) throws IOException {
		
		String line;
		BufferedReader br = null;
		List<String> instanceList = new LinkedList<String>();
		
		
		logger.debug("Reading instance file detecting format");
		
		LinkedHashMap<String, List<Long>> instances;
		try
		{
			CSVReader reader = new CSVReader(new FileReader(instanceListFile),',','"',true);
			List<String[]> csvContents = reader.readAll();
			instances = parseCSVContents(csvContents, InstanceFileFormat.NEW_CSV_INSTANCE_PER_ROW, InstanceFileFormat.NEW_CSV_SEED_INSTANCE_PER_ROW);
			
		} catch(IllegalArgumentException e)
		{
			try { 
			CSVReader reader = new CSVReader(new FileReader(instanceListFile),' ');
			List<String[]> csvContents = reader.readAll();
			instances = parseCSVContents(csvContents, InstanceFileFormat.LEGACY_INSTANCE_PER_ROW, InstanceFileFormat.LEGACY_SEED_INSTANCE_PER_ROW);
			} catch(IllegalArgumentException e2)
			{
				throw new ParameterException("Could not parse instanceFile " + instanceListFile.getAbsolutePath());
			}
		}
		InstanceSeedGenerator gen;
		//We check if some entry has a non zero amount of seeds (if we are in an instance seed pair file all entries must have atleast one)
		//Then we use our manual instance seed generator
		if(instances.entrySet().iterator().next().getValue().size() > 0)
		{
			gen = new SetInstanceSeedGenerator(instances, maxSeedsPerConfig);
		} else
		{
			 gen = new RandomInstanceSeedGenerator(instances.size(),seed, maxSeedsPerConfig);
		}
		
		/*
		try
		{
			br = new BufferedReader(new FileReader(instanceListFile));
			while((line = br.readLine()) != null)
			{
				logger.trace("Read in line from file \"{}\"",line);
				instanceList.add(line);
				
			}
		} finally
		{
			if(br != null) br.close();
		}
		*/
		instanceList.addAll(instances.keySet());
		return new InstanceListWithSeeds(gen, null, instanceList);
	}
	
	private static LinkedHashMap<String,List<Long>> parseCSVContents(List<String[]> csvContents, InstanceFileFormat instanceOnly, InstanceFileFormat seedPair )
	{
		InstanceFileFormat possibleFormat = null;
	
		/**
		 * Note we make the determination of which instanceSeedGenerator to use based on the first entries list size()
		 */
		LinkedHashMap<String, List<Long>> instanceSeedMap = new LinkedHashMap<String, List<Long>>();
		for(String[] s : csvContents)
		{
			
			
			if(s.length == 1)
			{
				if(s[0].trim().equals("")) throw new IllegalArgumentException();
				if(possibleFormat == seedPair)
				{
					logger.debug("Line with only 1 entry found, we are not {}",seedPair);
					throw new IllegalArgumentException();
				}  else
				{
					if(possibleFormat == null)
					{
						possibleFormat = instanceOnly;
						logger.debug("Line with only 1 entry found, trying {}", possibleFormat);
					}
					instanceSeedMap.put(s[0], new LinkedList<Long>());
				}
			} else if(s.length == 2)
			{
				if(possibleFormat == instanceOnly)
				{
					
					logger.debug("Line with 2 entries found, we are not {}",instanceOnly);
					throw new IllegalArgumentException();
				}  else
				{
					if(possibleFormat == null)
					{
						possibleFormat = seedPair;
						logger.debug("Line with only 2 entries found, trying {}", possibleFormat);
					}
					String instanceName = s[1];
					try {
						if(instanceSeedMap.get(instanceName) == null)
						{
							instanceSeedMap.put(instanceName, new LinkedList<Long>());
						}
						
					
					instanceSeedMap.get(instanceName).add(Long.valueOf(s[0]));
					} catch(NumberFormatException e)
					{
						logger.debug("{} is not a valid long value", s[0]);
						
						throw new IllegalArgumentException();
					}
				}
			} else
			{
				logger.debug("Line with {} entries found unknown format", s.length);
				possibleFormat = null;
				throw new IllegalArgumentException();
			}
		}
			if(instanceSeedMap.size() == 0) throw new IllegalArgumentException("No Instances Found");
			return instanceSeedMap;
	}
}
