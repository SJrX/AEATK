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
	
	public static List<ProblemInstance> getInstances(String filename, String experimentDir, boolean checkFileExistsOnDisk) throws IOException
	{
		return getInstances(filename, experimentDir, null, checkFileExistsOnDisk);
	}
	
	private static Map<String, ProblemInstance> cachedProblemInstances = new HashMap<String, ProblemInstance>();
	
	public static List<ProblemInstance> getInstances(String filename, String experimentDir, String featureFileName, boolean checkFileExistsOnDisk) throws IOException {
		
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
				
				featuresMap.put(features.getKeyForDataRow(i), Collections.unmodifiableMap(instFeatMap));
				
				for (int j=0; j < features.getNumberOfDataColumns(); j++)
				{		
						String key = features.getDataKeyByIndex(j);
						Double value = features.getDoubleDataValue(i, j);
						instFeatMap.put(key, value);
				}
			}
		}
		
		
		
		List<String> instanceList = new ArrayList<String>(featuresMap.size());
		
		if(filename != null)
		{
			File instanceListFile = getFileForPath(experimentDir, filename);
			BufferedReader br = null;
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
		} else
		{
			instanceList.addAll(featuresMap.keySet());
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
				features = featuresMap.get(instanceFile);
				
				if(features == null)
				{
					String path = instanceFile.replace(experimentDir,"");
					features = featuresMap.get(path);
				}
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
		return instances;
		
		
	}
}
