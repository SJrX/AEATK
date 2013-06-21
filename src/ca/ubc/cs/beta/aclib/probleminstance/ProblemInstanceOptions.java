package ca.ubc.cs.beta.aclib.probleminstance;



import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import ca.ubc.cs.beta.aclib.exceptions.FeatureNotFoundException;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.random.SeedableRandomPoolConstants;

@UsageTextField(hiddenSection = true)
public class ProblemInstanceOptions extends AbstractOptions{

	@Parameter(names={"--instanceFile","-i","--instance_file","--instance_seed_file"}, description="file containing a list of instances to use during the automatic configuration phase (see Instance File Format section of the manual)", required=true)
	public String instanceFile;

	@UsageTextField(defaultValues="")
	@Parameter(names={"--instanceFeatureFile", "--feature_file"}, description="file that contains the all the instances features")
	public String instanceFeatureFile;
	
	@Parameter(names={"--testInstanceFile","--test_instance_file","--test_instance_seed_file"}, description="file containing a list of instances to use during the validation phase (see Instance File Format section of the manual)", required=true)
	public String testInstanceFile;

	@Parameter(names="--checkInstanceFilesExist", description="check if instances files exist on disk")
	public boolean checkInstanceFilesExist = false;
	
	
	/**
	 * Gets the training problem instances
	 * @param experimentDirectory	Directory to search for instance files
	 * @param seed					Seed to use for the instances
	 * @param deterministic			Whether or not the instances should be generated with deterministic (-1) seeds
	 * @param required				Whether the instance file is required
	 */
	public InstanceListWithSeeds getTrainingProblemInstances(String experimentDirectory, long seed, boolean deterministic, boolean required, boolean featuresRequired) throws IOException
	{
		
		
		
		if(instanceFile == null)
		{
			if(required)
			{			
				throw new ParameterException("The instance file option --instanceFile must be set");
			} else
			{
				return null;
			}
		}
	
		InstanceListWithSeeds ilws;
		
		Logger log = LoggerFactory.getLogger(getClass());
				
		try {
			ilws = ProblemInstanceHelper.getInstances(instanceFile,experimentDirectory, instanceFeatureFile, checkInstanceFilesExist, seed, deterministic);
			
			
		} catch(FeatureNotFoundException e)
		{
			if(featuresRequired)
			{
				throw new ParameterException("Training instances require features and there was a problem loading features for all instances: " + e.getMessage());
			} else
			{
				ilws = ProblemInstanceHelper.getInstances(instanceFile,experimentDirectory, null, checkInstanceFilesExist, seed, deterministic);
			}
			
			
			
		}
		
		
		
		
		//ilws = ProblemInstanceHelper.getInstances(options.scenarioConfig.instanceFile,options.experimentDir, options.scenarioConfig.instanceFeatureFile, options.scenarioConfig.checkInstanceFilesExist, pool.getRandom(SeedableRandomPoolConstants.INSTANCE_SEEDS).nextInt(), (options.scenarioConfig.algoExecOptions.deterministic));
		
		//instanceFileAbsolutePath = ilws.getInstanceFileAbsolutePath();
		//instanceFeatureFileAbsolutePath = ilws.getInstanceFeatureFileAbsolutePath();
	
		log.info("Training Instance Seed Generator reports {} seeds ",  ilws.getSeedGen().getInitialInstanceSeedCount());
		if(ilws.getSeedGen().allInstancesHaveSameNumberOfSeeds())
		{
			log.info("Training Instance Seed Generator reports that all instances have the same number of available seeds");
		} else
		{
			log.error("Training Instance Seed Generator reports that some instances have a different number of seeds than others");
			throw new ParameterException("All Training instances must have the same number of seeds in this version of SMAC");
		}
		
		return ilws;
		
		
		
	}
	/**
	 * Gets the testing problem instances
	 * @param experimentDirectory	Directory to search for instance files
	 * @param seed					Seed to use for the instances
	 * @param deterministic			Whether or not the instances should be generated with deterministic (-1) seeds
	 * @param required				Whether the instance file is required
	 */
	public InstanceListWithSeeds getTestingProblemInstances(String experimentDirectory, long seed, boolean deterministic, boolean required, boolean featuresRequired) throws IOException
	{
		

		if(testInstanceFile == null)
		{
			if(required)
			{			
				throw new ParameterException("The instance file option --instanceFile must be set");
			} else
			{
				return null;
			}
		}
	
		InstanceListWithSeeds ilws;
		
		Logger log = LoggerFactory.getLogger(getClass());
		try {
			ilws = ProblemInstanceHelper.getInstances(instanceFile,experimentDirectory, instanceFeatureFile, checkInstanceFilesExist, seed, deterministic);
			
			
		} catch(FeatureNotFoundException e)
		{
			if(featuresRequired)
			{
				throw new ParameterException("Testing instances require features and there was a problem loading features for all instances: " + e.getMessage());
			} else
			{
				ilws = ProblemInstanceHelper.getInstances(instanceFile,experimentDirectory, null, checkInstanceFilesExist, seed, deterministic);
			}
			
			
			
		}
		
		
		//ilws = ProblemInstanceHelper.getInstances(options.scenarioConfig.instanceFile,options.experimentDir, options.scenarioConfig.instanceFeatureFile, options.scenarioConfig.checkInstanceFilesExist, pool.getRandom(SeedableRandomPoolConstants.INSTANCE_SEEDS).nextInt(), (options.scenarioConfig.algoExecOptions.deterministic));
		
		//instanceFileAbsolutePath = ilws.getInstanceFileAbsolutePath();
		//instanceFeatureFileAbsolutePath = ilws.getInstanceFeatureFileAbsolutePath();
	
		log.info("Test Instance Seed Generator reports {} seeds ",  ilws.getSeedGen().getInitialInstanceSeedCount());
		if(ilws.getSeedGen().allInstancesHaveSameNumberOfSeeds())
		{
			log.info("Test Instance Seed Generator reports that all instances have the same number of available seeds");
		} else
		{
			log.error("Test Instance Seed Generator reports that some instances have a different number of seeds than others");
			throw new ParameterException("All Testing instances must have the same number of seeds in this version of SMAC");
		}
		
		return ilws;
	
		
		
	}
	
	/**
	 * Gets both the training and the test problem instances
	 * 
	 * @param experimentDirectory	Directory to search for instance files
	 * @param trainingSeed			Seed to use for the training instances
	 * @param testingSeed			Seed to use for the testing instances
	 * @param deterministic			Whether or not the instances should be generated with deterministic (-1) seeds
	 * @param trainingRequired		Whether the training instance file is required
	 * @param testRequired			Whether the test instance file is required
	 * @return
	 * @throws IOException
	 */
	public TrainTestInstances getTrainingAndTestProblemInstances(String experimentDirectory, long trainingSeed, long testingSeed, boolean deterministic, boolean trainingRequired, boolean testRequired, boolean trainingFeaturesRequired, boolean testingFeaturesRequired) throws IOException
	{
		
		InstanceListWithSeeds training = getTrainingProblemInstances(experimentDirectory, trainingSeed, deterministic, trainingRequired, trainingFeaturesRequired);
		InstanceListWithSeeds testing = getTestingProblemInstances(experimentDirectory, testingSeed, deterministic, testRequired, testingFeaturesRequired);

		return new TrainTestInstances(training, testing); 
	}
	

	public class TrainTestInstances
	{
		private final InstanceListWithSeeds trainingInstances;
		private final InstanceListWithSeeds testInstances;
		public TrainTestInstances(InstanceListWithSeeds training,
				InstanceListWithSeeds testing) {
			// 
			this.trainingInstances = training;
			this.testInstances = testing;
		}
		public InstanceListWithSeeds getTrainingInstances() {
			return trainingInstances;
		}
		public InstanceListWithSeeds getTestInstances() {
			return testInstances;
		}
		
	}
}
