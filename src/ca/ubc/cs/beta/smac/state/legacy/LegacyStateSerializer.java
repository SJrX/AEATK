package ca.ubc.cs.beta.smac.state.legacy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.EnumMap;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.ac.RunResult;
import ca.ubc.cs.beta.configspace.ParamConfiguration;
import ca.ubc.cs.beta.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.probleminstance.InstanceSeedGenerator;
import ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun;
import ca.ubc.cs.beta.smac.exceptions.StateSerializationException;
import ca.ubc.cs.beta.smac.history.RunData;
import ca.ubc.cs.beta.smac.history.RunHistory;
import ca.ubc.cs.beta.smac.state.RandomPoolType;
import ca.ubc.cs.beta.smac.state.StateSerializer;
import ca.ubc.cs.beta.smac.util.AutoStartStopWatch;

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
	
	
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public LegacyStateSerializer(String path, String id, int iteration) {
		this.id = id;
		this.iteration = iteration;
		this.path = path;
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
	public void save() 
	{
		boolean fullSave = true;
		if(runHistory == null)  fullSave = false;
		log.debug("State Serialization for iteration {} commencing", iteration);
		AutoStartStopWatch auto = new AutoStartStopWatch();
		if(runHistory != null)
		{
			StringBuilder paramStrings = new StringBuilder();
			StringBuilder uniqConfigurations = new StringBuilder();
			
			
			int i=1; 
			for(ParamConfiguration config : runHistory.getAllParameterConfigurationsRan())
			{
				paramStrings.append(i + ":" + config.getFormattedParamString(StringFormat.STATEFILE_SYNTAX) + "\n");
				uniqConfigurations.append(i).append(",").append(getStringFromConfiguration(config)).append("\n");
				i++;
			}	
			
			StringBuilder runResults = new StringBuilder();
			i=0;
			double cumulativeSum = 0;
			
			for(RunData runData: runHistory.getAlgorithmRunData())
			{
				i++;
				Integer thetaIdx = runData.getThetaIdx();
				Integer instanceIdx = runData.getInstanceidx();
				Integer iteration = runData.getIteration();
				
				AlgorithmRun run = runData.getRun();
				//Comments are just for dev reference when comparing code with LegacyStateDeserializer
				runResults.append(i).append(","); //0
				runResults.append(thetaIdx).append(","); //1
				runResults.append(instanceIdx).append(","); //2
				runResults.append(runHistory.getRunObjective().getObjective(run)).append(","); //3
				int isCensored = 0;
				
				if(run.getRunResult().equals(RunResult.TIMEOUT) && run.getInstanceRunConfig().hasCutoffLessThanMax())
				{
					isCensored = 1;
				}
				runResults.append(isCensored + ","); //Censored 4
				runResults.append(run.getInstanceRunConfig().getCutoffTime()).append(","); //5
				runResults.append(run.getResultSeed()).append(","); //6
				runResults.append(run.getRuntime()).append(","); //7
				runResults.append(run.getRunLength()).append(","); //8
				runResults.append((run.getRunResult().isSolved() ? 1 : 0) ).append(","); //9
				runResults.append(run.getQuality()).append(","); //10
				runResults.append(iteration).append(","); //11
				cumulativeSum += run.getRuntime(); //12
				runResults.append(cumulativeSum).append(","); //13
				runResults.append("\n");
			}
			
			
			try {
				
				//Write Unique Configurations File
				File f= new File(LegacyStateFactory.getUniqConfigurationsFilename(path, id, iteration));
				writeStringBuffer(uniqConfigurations, f);
				log.debug("Unique Configurations Saved in {}", f.getAbsolutePath());
				
				f = new File(LegacyStateFactory.getParamStringsFilename(path, id, iteration));
				writeStringBuffer(paramStrings, f);
				log.debug("Parameter Strings Saved in {}", f.getAbsolutePath());
				
				
				f = new File(LegacyStateFactory.getRunAndResultsFilename(path, id, iteration));
				writeStringBuffer(runResults, f);			
				log.debug("Run Results Saved in {}", f.getAbsolutePath());
				
			} catch (IOException e) {
	
				throw new StateSerializationException(e);
			}
			
			
		}
		

			//Write Object Dump
			
			try {
				if(fullSave)
				{ //We are a full dump so we will save everything
					File f = new File(LegacyStateFactory.getJavaObjectDumpFilename(path, id, iteration));
					if(!f.createNewFile()) throw new IllegalStateException("File: " + f.getAbsolutePath() + " already exists ");
					saveToFile(f);
				} else
				{
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
			
		
		log.info("State saved for iteration {} in {} ", iteration, path);
		log.info("Saving state took {} ms", auto.stop());
		
	}

	

	private void saveToFile(File f) throws IOException
	{
		

		ObjectOutputStream oWriter = new ObjectOutputStream(new FileOutputStream(f));
		oWriter.writeInt(iteration);
		oWriter.writeObject(randomMap);
		oWriter.writeObject(instanceSeedGenerator);
		oWriter.writeObject(incumbent.getFormattedParamString(StringFormat.STATEFILE_SYNTAX));
		oWriter.close();
	
		log.debug("Java Object Dump Saved in {}", f.getAbsolutePath());
	}
	public String getStringFromConfiguration(ParamConfiguration c)
	{
		double[] valueArray = c.toValueArray();
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < valueArray.length; i++)
		{
			sb.append(valueArray[i]);
			if(i+1 != valueArray.length) sb.append(",");
		}
		return sb.toString();
	}
	
	private void writeStringBuffer(StringBuilder b, File f) throws IOException
	{
		if(!f.createNewFile()) throw new IllegalStateException("File: " + f.getAbsolutePath() + " already exists ");
		
		FileWriter writer = new FileWriter(f);
		writer.write(b.toString());
		writer.close();
	}

	@Override
	public void setIncumbent(ParamConfiguration incumbent) {
		this.incumbent = incumbent;
		
	}

}
