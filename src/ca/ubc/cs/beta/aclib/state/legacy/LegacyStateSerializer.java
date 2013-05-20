package ca.ubc.cs.beta.aclib.state.legacy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.aclib.exceptions.StateSerializationException;
import ca.ubc.cs.beta.aclib.misc.watch.AutoStartStopWatch;
import ca.ubc.cs.beta.aclib.runhistory.RunData;
import ca.ubc.cs.beta.aclib.runhistory.RunHistory;
import ca.ubc.cs.beta.aclib.seedgenerator.InstanceSeedGenerator;
import ca.ubc.cs.beta.aclib.state.RandomPoolType;
import ca.ubc.cs.beta.aclib.state.StateSerializer;

/**
 * Supports saving the state in a format mostly compatible with MATLAB:
 * 
 * Four files are generally generated per save:
 * 
 * paramstrings-xxx.csv - A list of parameter strings generated 
 * uniq_configurations-xxx.csv - The parameter strings in array notation
 * run_and_results-xxx.csv - A list of run results that can be used to recreate a new RunHistory;
 * java_obj_dump.obj - A serialized list of objects containing the iteration, a Random objec,then the InstanceSeedGenerator
 * 
 * 
 * @author seramage
 *
 */
public class LegacyStateSerializer implements StateSerializer {

	private RunHistory runHistory = null;
	private final EnumMap<RandomPoolType, Random> randomMap = new EnumMap<RandomPoolType,Random>(RandomPoolType.class);
	private ParamConfiguration incumbent;
	
	private final String id ;
	private InstanceSeedGenerator instanceSeedGenerator;
	private final int iteration;
	private final String path;
	
	private final LegacyStateFactory legacyStateFactory;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	
	private final Set<String> savedFiles = new HashSet<String>();
	/**
	 * Constructs the Legacy State Serializer
	 * @param path      	string representing directory we should save to
	 * @param id			the id of the save (generally "it" or "CRASH")
	 * @param iteration		the iteration we are saving
	 * @param legacyStateFactory 
	 */
	LegacyStateSerializer(String path, String id, int iteration, LegacyStateFactory legacyStateFactory) {
		this.id = id;
		this.iteration = iteration;
		this.path = (new File(path)).getAbsolutePath();
		this.legacyStateFactory = legacyStateFactory;
		
	}

	@Override
	public void setRunHistory(RunHistory runHistory) {
	
		this.runHistory = runHistory;

	}

	@Override
	public void setPRNG(RandomPoolType randType, Random random) {
		
		this.randomMap.put(randType, random);

	}

	@Override
	public void setInstanceSeedGenerator(InstanceSeedGenerator gen) {
		this.instanceSeedGenerator = gen;
		
	}
	
	@Override
	public void setIncumbent(ParamConfiguration incumbent) {
		this.incumbent = incumbent;
		
	}
	
	
	
	@Override
	public void save() 
	{
		boolean fullSave = true;
		if(runHistory == null)  fullSave = false;
		log.debug("State Serialization for iteration {} commencing", iteration);
		AutoStartStopWatch auto = new AutoStartStopWatch();
		if(runHistory != null)
		{
			try {
				File f= new File(LegacyStateFactory.getUniqConfigurationsFilename(path, id, iteration));
				addFileToSet(f);
				log.debug("Unique Configurations Saved in {}", f.getAbsolutePath());
				
				FileWriter uniqConfigurations = new FileWriter(f);
				
				
				log.debug("Parameter Strings Saved in {}", f.getAbsolutePath());
				f = new File(LegacyStateFactory.getParamStringsFilename(path, id, iteration));
				addFileToSet(f);
				FileWriter paramStrings = new FileWriter(f);
				
				//StringBuilder paramStrings = new StringBuilder();
				//StringBuilder uniqConfigurations = new StringBuilder();
				
				
				int i=1; 
				for(ParamConfiguration config : runHistory.getAllParameterConfigurationsRan())
				{
					
					paramStrings.append(i + ":" + config.getFormattedParamString(StringFormat.STATEFILE_SYNTAX) + "\n");
					
					uniqConfigurations.append(i+",").append(getStringFromConfiguration(config)).append("\n");
					
					i++;
				}	
				
				paramStrings.close();
				uniqConfigurations.close();
				paramStrings = null;
				uniqConfigurations = null;
				
				f = new File(LegacyStateFactory.getRunAndResultsFilename(path, id, iteration));
				addFileToSet(f);
				//writeStringBuffer(runResults, f);			
				log.debug("Run Results Saved in {}", f.getAbsolutePath());
				FileWriter runResults = new FileWriter(f);
				
				//StringBuilder runResults = new StringBuilder();
				i=0;
				double cumulativeSum = 0;
				
				runResults.append(LegacyStateFactory.RUN_NUMBER_HEADING).append(","); //0
				runResults.append("Run History Configuration ID").append(","); //1
				runResults.append("Instance ID").append(","); //2
				runResults.append("Response Value (y)").append(","); //3
				runResults.append("Censored?").append(","); //4
				runResults.append("Cutoff Time Used").append(","); //5
				runResults.append("Seed").append(","); //6
				runResults.append("Runtime").append(","); //7
				runResults.append("Run Length").append(","); //8
				runResults.append("Run Result Code").append(","); //9
				runResults.append("Run Quality").append(","); //10
				runResults.append("SMAC Iteration").append(","); //11
				runResults.append("SMAC Cumulative Runtime").append(","); //12
				runResults.append("Run Result").append(","); //13
				runResults.append("Additional Algorithm Run Data").append(","); //14
				runResults.append("Wall Clock Time").append(","); //15
				runResults.append("\n");
				
	
				for(RunData runData: runHistory.getAlgorithmRunData())
				{
					i++;
					Integer thetaIdx = runData.getThetaIdx();
					Integer instanceIdx = runData.getInstanceidx();
					Integer iteration = runData.getIteration();
					
					AlgorithmRun run = runData.getRun();
					//Comments are just for dev reference when comparing code with LegacyStateDeserializer
					runResults.append(i+","); //0
					runResults.append(thetaIdx+","); //1
					runResults.append(instanceIdx+","); //2
					runResults.append(runHistory.getRunObjective().getObjective(run)+","); //3
					int isCensored = 0;
					
					if((run.getRunResult().equals(RunResult.TIMEOUT) && run.getRunConfig().hasCutoffLessThanMax()) || run.getRunResult().equals(RunResult.KILLED))
					{
						isCensored = 1;
					}
					runResults.append(isCensored + ","); //Censored 4
					runResults.append(run.getRunConfig().getCutoffTime()+","); //5
					runResults.append(run.getResultSeed()+","); //6
					runResults.append(run.getRuntime()+","); //7
					runResults.append(run.getRunLength()+","); //8
					runResults.append(String.valueOf(run.getRunResult().getResultCode())+","); //9
					runResults.append(run.getQuality()+","); //10
					runResults.append(iteration+","); //11
					cumulativeSum += run.getRuntime(); 
					runResults.append(cumulativeSum+","); //12
					runResults.append(run.getRunResult().name()+","); //13
					runResults.append(run.getAdditionalRunData()+",");//14
					runResults.append(run.getWallclockExecutionTime()+","); //15;
					runResults.append("\n");
					
					if(i % 100 == 0) {
						runResults.flush();
					}
				}
					runResults.close();
			} catch (IOException e) {
	
				throw new StateSerializationException(e);
			}
			
			
		}
		

			//Write Object Dump
			
			try {
				if(fullSave)
				{ 
					//We are a full dump so we will save everything
					File f = new File(LegacyStateFactory.getJavaObjectDumpFilename(path, id, iteration));
					
					if(!f.createNewFile()) throw new IllegalStateException("File: " + f.getAbsolutePath() + " already exists ");
					saveToFile(f);
					addFileToSet(f);
				} else
				{
					//Do not add the quick files to the save File set, as they shouldn't be deleted
					File currentFile = new File(LegacyStateFactory.getJavaQuickObjectDumpFilename(path, id, iteration));
					File oldFile = new File(LegacyStateFactory.getJavaQuickBackObjectDumpFilename(path, id, iteration));
					
					if(currentFile.exists())
					{
						if(oldFile.exists()) oldFile.delete();
						if(!currentFile.renameTo(oldFile)) throw new IllegalStateException("Could not rename file " + currentFile.getAbsolutePath() + " to " + oldFile.getAbsolutePath());
						
						currentFile = new File(LegacyStateFactory.getJavaQuickObjectDumpFilename(path, id, iteration));
						
					}
					
					
					saveToFile(currentFile);
					

					
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new StateSerializationException(e);
			}
			
		
		legacyStateFactory.addWrittenFilesForIteration(iteration, savedFiles);
		log.info("State saved for iteration {} in {} ", iteration, path);
		log.info("Saving state took {} ms", auto.stop());
		
	}


	
	
	
	/**
	 * Saves all java objects to file
	 * @param f file to save the objects to
	 * @throws IOException	if we encounter an error saving  the file
	 */
	private void saveToFile(File f) throws IOException
	{
		

		ObjectOutputStream oWriter = new ObjectOutputStream(new FileOutputStream(f));
		oWriter.writeInt(iteration);
		oWriter.writeObject(randomMap);
		oWriter.writeObject(instanceSeedGenerator);
		
		if(incumbent != null)
		{
			oWriter.writeObject(incumbent.getFormattedParamString(StringFormat.STATEFILE_SYNTAX));
		} else
		{
			oWriter.writeObject(null);
		}
		oWriter.close();
	
		log.debug("Java Object Dump Saved in {}", f.getAbsolutePath());
	}
	/**
	 * Converts ParamConfigurations to Strings
	 * @param config param configuration
	 * @return	paramconfiguration in string form.
	 */
	public String getStringFromConfiguration(ParamConfiguration config)
	{
		return config.getFormattedParamString(StringFormat.ARRAY_STRING_SYNTAX);
	}
	/**
	 * Writes the StringBuilder to file
	 * @param b				stringBuilder to save
	 * @param f				file to save to
	 * @throws IOException	when an error occurs 
	 */
	private void writeStringBuffer(StringBuilder b, File f) throws IOException
	{
		if(!f.createNewFile()) throw new IllegalStateException("File: " + f.getAbsolutePath() + " already exists ");
		
		FileWriter writer = new FileWriter(f);
		writer.write(b.toString());
		writer.close();
	}

	/**
	 * Adds a file to the set of files written for this iteration.
	 * @param f
	 */
	private void addFileToSet(File f)
	{
		this.savedFiles.add(f.getAbsolutePath());
	}

}
