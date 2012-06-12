package ca.ubc.cs.beta.smac.state.legacy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.ac.config.ProblemInstance;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.smac.OverallObjective;
import ca.ubc.cs.beta.smac.RunObjective;
import ca.ubc.cs.beta.smac.exceptions.StateSerializationException;
import ca.ubc.cs.beta.smac.state.StateDeserializer;
import ca.ubc.cs.beta.smac.state.StateFactory;
import ca.ubc.cs.beta.smac.state.StateSerializer;

/**
 * Stores Files In a Format compatible with Matlab versions.
 * 
 * 
 * 
 * @author seramage
 */
public class LegacyStateFactory implements StateFactory{

	
	private final String saveStatePath;
	private final String restoreFromPath;
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public LegacyStateFactory(String saveStatePath, String restoreFromPath)
	{
		
		this.saveStatePath = saveStatePath;
		this.restoreFromPath = restoreFromPath;
		
		if(saveStatePath != null)
		{
			File f = new File(this.saveStatePath);
			
			
			
			if(!f.exists())
			{
				if(!f.mkdirs())
				{
					log.error("Could not create directory to save states: {} " + f.getAbsolutePath());
					throw new IllegalArgumentException("Could not create directory" + f.getAbsolutePath());
				} else
				{
					log.info("Directory created for states: {}" + f.getAbsolutePath());
				}
				
			} else
			{
				if(f.isDirectory() && f.listFiles().length > 0)
				{
					File newFileName = new File(f.getParent() + File.separator + "/old-state-" + System.currentTimeMillis() + "/");
					f.renameTo(newFileName);
					log.warn("Found previous state output, renamed to: {}", newFileName.getAbsolutePath());  
					f = new File(this.saveStatePath);
					f.mkdir();
				}
			}
			
			if(!f.isDirectory())
			{
				throw new IllegalArgumentException("Not a directory: " + f.getAbsolutePath());
			}
			
			if(!f.canWrite())
			{
				throw new IllegalArgumentException("Can't write to state saving directory: " + f.getAbsolutePath());
		}
		
		}
		
	}
	@Override
	public StateDeserializer getStateDeserializer(String id, int iteration, ParamConfigurationSpace configSpace, OverallObjective overallObj, RunObjective runObj, List<ProblemInstance> instances, AlgorithmExecutionConfig execConfig) throws StateSerializationException
	{
		if(restoreFromPath == null) 
		{
			throw new IllegalArgumentException("This Serializer does not support restoring state");
		}
		return new LegacyStateDeserializer(restoreFromPath, id, iteration, configSpace, overallObj, runObj, instances, execConfig);
	}

	@Override
	public StateSerializer getStateSerializer(String id, int iteration)	throws StateSerializationException 
	{
		if(saveStatePath == null) 
		{
			throw new IllegalArgumentException("This Serializer does not support saving State");
		}
		return new LegacyStateSerializer(saveStatePath, id, iteration);
	}


	/**
	 * Package private methods to get consistent filenames
	 */
	
	static String getUniqConfigurationsFilename(String path, String id, int iteration)
	{
		return getUniqConfigurationsFilename(path, id, String.valueOf(iteration), "-");
	}
	
	
	static String getUniqConfigurationsFilename(String path, String id, String iteration, String dash)
	{
		if(path.equals(""))
		{
			return "uniq_configurations" + dash + id + iteration + ".csv";
		} else
		{
			return path + File.separator + "uniq_configurations" + dash + id + iteration + ".csv";
		}
	}
	
	
	static String getParamStringsFilename(String path, String id, int iteration) {
		return getParamStringsFilename(path, id, String.valueOf(iteration), "-");
			
	}
	
	static String getParamStringsFilename(String path, String id, String iteration, String dash) {
		
		if(path.equals(""))
		{ 
			return "paramstrings" + dash + id + iteration + ".txt";
		} else
		{
			return path + File.separator + "paramstrings-" + dash + id + iteration + ".txt";
		}
			
	}

	static String getRunAndResultsFilename(String path, String id,
			int iteration) 
	{
	
		return getRunAndResultsFilename(path, id, String.valueOf(iteration));
	}
	
	
	public static String getRunAndResultsFilename(String path, String id, String iteration)
	{
		
		return getRunAndResultsFilename(path, id, iteration, "-");
		
	}
	
	public static String getRunAndResultsFilename(String path, String id, String iteration, String dash)
	{
		
		if(!path.equals(""))
		{
			return path + File.separator + "runs_and_results" + dash + id + iteration + ".csv";
		} else
		{
			return  "runs_and_results"+dash + id + iteration + ".csv";
		}
		
	}
	
	static String getJavaObjectDumpFilename(String path, String id, int iteration)
	{
	 	return path + File.separator + "java_obj_dump-"+id + iteration +".obj";
	}
	
	static String getJavaQuickObjectDumpFilename(String path, String id,
			int iteration) {
		return path + File.separator + "java_obj_dump-"+id + "quick.obj";
	}
	public static String getJavaQuickBackObjectDumpFilename(String path, String id,
			int iteration) {
		return path + File.separator + "java_obj_dump-"+id + "quick-bak.obj";
	}
	public static int readIterationFromObjectFile(File javaObjDumpFile) {
		// TODO Auto-generated method stub
		ObjectInputStream oReader =  null;
		try{
			try {
				
				oReader =  new ObjectInputStream(new FileInputStream(javaObjDumpFile));
					
				return oReader.readInt();
		
			} finally
			{
				if(oReader != null) oReader.close();
			}
		} catch(IOException e)
		{
			return -1;
		}	
	}

	
}
