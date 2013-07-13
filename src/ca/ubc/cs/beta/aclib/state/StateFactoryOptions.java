package ca.ubc.cs.beta.aclib.state;

import java.io.File;

import com.beust.jcommander.Parameter;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.probleminstance.InstanceListWithSeeds;
import ca.ubc.cs.beta.aclib.state.legacy.LegacyStateFactory;
import ca.ubc.cs.beta.aclib.state.nullFactory.NullStateFactory;

@UsageTextField(hiddenSection = true)
public class StateFactoryOptions extends AbstractOptions{

	@Parameter(names={"--state-serializer","--stateSerializer"}, description="determines the format of the files to save the state in")
	public StateSerializers stateSerializer = StateSerializers.LEGACY;

	@Parameter(names={"--state-deserializer","--stateDeserializer"}, description="determines the format of the files that store the saved state to restore")
	public StateSerializers statedeSerializer = StateSerializers.LEGACY;
	
	@UsageTextField(defaultValues="N/A (No state is being restored)")
	@Parameter(names={"--restore-state-from","--restoreStateFrom"}, description="location of state to restore")
	public String restoreStateFrom = null;

	@UsageTextField(defaultValues="N/A (No state is being restored)")
	@Parameter(names={"--restore-iteration","--restoreStateIteration","--restoreIteration"}, description="iteration of the state to restore")
	public Integer restoreIteration = null;
	
	/**
	 * Restore scenario is done before we parse the configuration and fixes input args
	 * in the input string to jcommander 
	 */
	@Parameter(names={"--restore-iteration","--restoreScenario"}, description="Restore the scenario & state in the state folder")
	public File restoreScenario =null; 
	
	@Parameter(names={"--clean-old-state-on-success","--cleanOldStateOnSuccess"}, description="will clean up much of the useless state files if smac completes successfully")
	public boolean cleanOldStatesOnSuccess = true;
	
	@Parameter(names={"--save-context","--saveContext","--saveContextWithState" }, description="saves some context with the state folder so that the data is mostly self-describing (Scenario, Instance File, Feature File, Param File are saved)")
	public boolean saveContextWithState = true;
	
	
	public StateFactory getRestoreStateFactory(String outputDirectory, int numRun)
	{
	/*
	 * Build the Serializer object used in the model 
	 */
		StateFactory restoreSF;
		switch(statedeSerializer)
		{
			case NULL:
				restoreSF = new NullStateFactory();
				break;
			case LEGACY:
				restoreSF = new LegacyStateFactory(outputDirectory +  File.separator + "state-run" + numRun + File.separator, restoreStateFrom);
				break;
			default:
				throw new IllegalArgumentException("State Serializer specified is not supported");
		}
		
		return restoreSF;
	}


	public StateFactory getSaveStateFactory(String outputDir, int numRun) {

		StateFactory sf;
		switch(stateSerializer)
		{
			case NULL:
				sf = new NullStateFactory();
				break;
			case LEGACY:
				String savePath = outputDir + File.separator + "state-run" + numRun + File.separator;
				sf = new LegacyStateFactory(savePath, restoreStateFrom);
				break;
			default:
				throw new IllegalArgumentException("State Serializer specified is not supported");
		}
		
		return sf;
	}
	
	public void saveContextWithState(ParamConfigurationSpace configSpace, InstanceListWithSeeds trainingILWS, File scenarioFile, StateFactory sf)
	{
		if(saveContextWithState)
		{
			sf.copyFileToStateDir("param-file.txt", new File(configSpace.getParamFileName()));
			
			String instanceFileAbsolutePath = trainingILWS.getInstanceFileAbsolutePath();
			if(instanceFileAbsolutePath != null)
			{
				sf.copyFileToStateDir("instances.txt", new File(instanceFileAbsolutePath));
			}
			
			String instanceFeatureFileAbsolutePath = trainingILWS.getInstanceFeatureFileAbsolutePath();
			
			if(instanceFeatureFileAbsolutePath != null)
			{
				sf.copyFileToStateDir("instance-features.txt", new File(instanceFeatureFileAbsolutePath));
			}
	
			File scenFile = scenarioFile;
			
			if ((scenFile != null) && (scenFile.exists()))
			{
				sf.copyFileToStateDir("scenario.txt", scenFile);
			}

		}
		
	}
}
