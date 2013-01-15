package ca.ubc.cs.beta.aclib.state.legacy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.exceptions.StateSerializationException;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.state.StateDeserializer;
import ca.ubc.cs.beta.aclib.state.StateFactory;
import ca.ubc.cs.beta.aclib.state.StateSerializer;

/**
 * Supports saving and restoring files from disk in a way that is mostly compatible with MATLAB
 * 
 * 
 * @author seramage
 */
public class LegacyStateFactory implements StateFactory{

	
	private final String saveStatePath;
	private final String restoreFromPath;
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	/**
	 * Stores for each iteration a set of files written
	 */
	private final ConcurrentSkipListMap<Integer, Set<String>> savedFilesPerIteration = new ConcurrentSkipListMap<Integer, Set<String>>();
	
	
	/**
	 * Constructs the LegacyStateFactory
	 * @param saveStatePath 	Where we should save files to
	 * @param restoreFromPath	Where we should restore from
	 */
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
					log.info("Directory created for states: {}",  f.getAbsolutePath());
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
	public StateDeserializer getStateDeserializer(String id, int iteration, ParamConfigurationSpace configSpace, OverallObjective intraInstanceObjective, OverallObjective interInstanceObjective, RunObjective runObj, List<ProblemInstance> instances, AlgorithmExecutionConfig execConfig) throws StateSerializationException
	{
		if(restoreFromPath == null) 
		{
			throw new IllegalArgumentException("This Serializer does not support restoring state");
		}
		return new LegacyStateDeserializer(restoreFromPath, id, iteration, configSpace, intraInstanceObjective, interInstanceObjective, runObj, instances, execConfig);
	}

	@Override
	public StateSerializer getStateSerializer(String id, int iteration)	throws StateSerializationException 
	{
		if(saveStatePath == null) 
		{
			throw new IllegalArgumentException("This Serializer does not support saving State");
		}
		return new LegacyStateSerializer(saveStatePath, id, iteration, this);
	}

	
	/**
	 * Copies the file to the State Dir
	 * @param name name of the file to write
	 * @param f source file
	 */
	@Override
	public void copyFileToStateDir(String name, File f)
	{
		if(saveStatePath == null)
		{
			throw new IllegalArgumentException("This Serializer does not support saving State");
		}
		
		if(!f.isFile())
		{
			throw new IllegalArgumentException("Input file f is not a file :" + f.getAbsolutePath());
		}
		
		if(!f.exists())
		{
			throw new IllegalArgumentException("Input file f does not exist :" + f.getAbsolutePath());
		}
		
		File outputFile = new File(saveStatePath + File.separator + name);
		
		try {
			InputStream in = new FileInputStream(f);
			OutputStream out = new FileOutputStream(outputFile);
			
			
			byte[] buf = new byte[8172];
			int len;
			
			while((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}
			log.info("File copied to {} ", outputFile.getAbsolutePath());
			
		} catch(IOException e)
		{
			throw new IllegalStateException("IOException occured :",e);
		}
		
		
	}

	/**
	 * Generates the filename on disk that we should use to store uniq_configurations (array format of configurations)
	 * @param path 			directory to look into
	 * @param id 			id of the save (generally "it" or "CRASH")
	 * @param iteration		iteration of the file
	 * @return string representing the file name
	 */
	static String getUniqConfigurationsFilename(String path, String id, int iteration)
	{
		return getUniqConfigurationsFilename(path, id, String.valueOf(iteration), "-");
	}
	
	/**
	 * Generates the filename on disk that we should use to store uniq_configurations (array format of configurations)
	 * @param path 			directory to look into
	 * @param id 			id of the save (generally "it" or "CRASH")
	 * @param iteration		iteration of the file
	 * @param dash			character we use as a dash (this is poorly named).
	 * @return string representing the file name
	 */
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
	
	/**
	 * Generates the filename on disk that we should use to store paramstrings (String format of configurations)
	 * @param path 			directory to look into
	 * @param id 			id of the save (generally "it" or "CRASH")
	 * @param iteration		iteration of the file
	 * @return string representing the file name
	 */
	static String getParamStringsFilename(String path, String id, int iteration) {
		return getParamStringsFilename(path, id, String.valueOf(iteration), "-");
			
	}
	
	/**
	 * Generates the filename on disk that we should use to store paramstrings (String format of configurations)
	 * @param path 			directory to look into
	 * @param id 			id of the save (generally "it" or "CRASH")
	 * @param iteration		iteration of the file
	 * @param dash			character we use as a dash (this is poorly named).
	 * @return string representing the file name
	 */
	static String getParamStringsFilename(String path, String id, String iteration, String dash) {
		
		if(path.equals(""))
		{ 
			return "paramstrings" + dash + id + iteration + ".txt";
		} else
		{
			return path + File.separator + "paramstrings" + dash + id + iteration + ".txt";
		}
			
	}
	
	/**
	 * Generates the filename on disk that we should use to store uniq_configurations (array format of configurations)
	 * @param path 			directory to look into
	 * @param id 			id of the save (generally "it" or "CRASH")
	 * @param iteration		iteration of the file
	 * @return string representing the file name
	 */
	static String getRunAndResultsFilename(String path, String id, int iteration) 
	{	
		return getRunAndResultsFilename(path, id, String.valueOf(iteration));
	}
	
	/**
	 * Generates the filename on disk that we should use to store run_and_results 
	 * @param path 			directory to look into
	 * @param id 			id of the save (generally "it" or "CRASH")
	 * @param iteration		iteration of the file
	 * @return string representing the file name
	 */
	public static String getRunAndResultsFilename(String path, String id, String iteration)
	{
		
		return getRunAndResultsFilename(path, id, iteration, "-");
		
	}
	
	/**
	 * Generates the filename on disk that we should use to store run_and_results
	 * @param path 			directory to look into
	 * @param id 			id of the save (generally "it" or "CRASH")
	 * @param iteration		iteration of the file
	 * @param dash			character we use as a dash (this is poorly named).
	 * @return string representing the file name
	 */
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
	
	/**
	 * Generates the filename on disk that we should use to store the java object dump
	 * @param path 			directory to look into
	 * @param id 			id of the save (generally "it" or "CRASH")
	 * @param iteration		iteration of the file
	 * @return string representing the file name
	 */
	static String getJavaObjectDumpFilename(String path, String id, int iteration)
	{
	 	return path + File.separator + "java_obj_dump-"+id + iteration +".obj";
	}
	
	/**
	 * Generates the filename on disk that we should use to store run_and_results 
	 * @param path 			directory to look into
	 * @param id 			id of the save (generally "it" or "CRASH")
	 * @param iteration		iteration of the file (not used, probably should be deleted)
	 * @return string representing the file name
	 */
	static String getJavaQuickObjectDumpFilename(String path, String id,
			int iteration) {
		return path + File.separator + "java_obj_dump-"+id + "quick.obj";
	}
	
	/**
	 * Generates the filename on disk that we should use to store run_and_results 
	 * @param path 			directory to look into
	 * @param id 			id of the save (generally "it" or "CRASH")
	 * @param iteration		iteration of the file (not used, probably should be deleted)
	 * @return string representing the file name
	 */
	public static String getJavaQuickBackObjectDumpFilename(String path, String id,
			int iteration) {
		return path + File.separator + "java_obj_dump-"+id + "quick-bak.obj";
	}
	
	/**
	 * Reads the iteration that this java object file contains
	 * @param javaObjDumpFile file to read
	 * @return iteration stored in it
	 */
	public static int readIterationFromObjectFile(File javaObjDumpFile) {
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

	
	
	static final String RUN_NUMBER_HEADING = "Run Number";
	@Override
	public void purgePreviousStates() {
		
		
		if(savedFilesPerIteration.size() == 0)
		{ //No iterations
			return;
		}
		
		Set<String> filesToDelete = new HashSet<String>();
		
		for(Set<String> files : savedFilesPerIteration.values())
		{
			filesToDelete.addAll(files);
		}
		
		
		filesToDelete.removeAll(savedFilesPerIteration.lastEntry().getValue());
		
		Integer lastIteration = savedFilesPerIteration.lastKey();
		
		log.info("Deleting all saved state files except those applicable to iteration {} ", lastIteration);
		
		
		if(log.isDebugEnabled())
		{
			for(String filename : filesToDelete)
			{
				log.debug("Deleting file {}", filename);
				if(!(new File(filename)).delete())
				{
					log.warn("Could not delete file {} ", filename);
				}
				
			}
		}
		
		
		
	}
	
	void addWrittenFilesForIteration(int iteration,
			Set<String> savedFiles) {
		
			 this.savedFilesPerIteration.putIfAbsent(iteration, new HashSet<String>());
			 Set<String> iterationFiles = this.savedFilesPerIteration.get(iteration);
			iterationFiles.addAll(savedFiles);
	}
}
