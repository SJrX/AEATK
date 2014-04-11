package ca.ubc.cs.beta.aeatk.example.tae;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.misc.jcommander.JCommanderHelper;
import ca.ubc.cs.beta.aeatk.misc.version.VersionTracker;
import ca.ubc.cs.beta.aeatk.options.AbstractOptions;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration.ParameterStringFormat;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import ec.util.MersenneTwister;

/**
 * A simple utility class that provides the ability to execute a single run against a <code>TargetAlgorithmEvaluator</code>.
 *
 * This class serves two purposes: 
 * <p>
 * From a usage perspective, people should be able to test their wrappers or target algorithms easily
 * <p>
 * From a documentation perspective, this class should serve as an example for using TargetAlgorithmEvaluators and other aspects AEATK.
 * 
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
public class TargetAlgorithmEvaluatorRunner 
{

	//SLF4J Logger object (not-initialized on start up in case command line options want to change it)
	private static Logger log;
	
	public static void main(String[] args)
	{
		 
		//JCommander Options object that specifies the main arguments to this project
		//It also includes a @ParametersDelegate for built in Option objects.
		TargetAlgorithmEvaluatorRunnerOptions mainOptions = new TargetAlgorithmEvaluatorRunnerOptions();
		
		//Map object that for each available TargetAlgorithmEvaluator gives it's associated options object
		Map<String,AbstractOptions> taeOptions = mainOptions.scenOptions.algoExecOptions.taeOpts.getAvailableTargetAlgorithmEvaluators();

		try {
			
			//Parses the options given in the args array and sets the values
			JCommander jcom;
			try {
				//This will check for help and version arguments 
				jcom = JCommanderHelper.parseCheckingForHelpAndVersion(args, mainOptions,taeOptions);
				
				//Does any setup work necessary to setup logger.
				mainOptions.logOpts.initializeLogging();
			} finally
			{
				//Initialize the logger *AFTER* the JCommander objects have been parsed
				//So that options that take effect
				log = LoggerFactory.getLogger(TargetAlgorithmEvaluatorRunner.class);
			}
		
			//Displays version information
			//See the TargetAlgorithmEvaluatorRunnerVersionInfo class for how to manage your own versions.
			VersionTracker.logVersions();
			
			
			for(String name : jcom.getParameterFilesToRead())
			{
				log.debug("Parsing (default) options from file: {} ", name);
			}
			
			
			//AlgorithmExecutionConfig object represents all the information needed to invoke the target algorithm / wrapper.
			//This includes information such as cutoff time, and the parameter space.
			//Like most domain objects in AEATK, AlgorithmExecutionConfig is IMMUTABLE. 
			AlgorithmExecutionConfiguration execConfig = mainOptions.getAlgorithmExecutionConfig();
			
			
			//Logs the options (since mainOptions implements AbstractOptions a 'nice-ish' printout is created).
			log.debug("==== Configuration====\n {} ", mainOptions);
			
			
			
			TargetAlgorithmEvaluator tae = null;
			try {
				//Retrieve the target algorithm evaluator with the necessary options
				tae = mainOptions.scenOptions.algoExecOptions.taeOpts.getTargetAlgorithmEvaluator( taeOptions);
				
				
				//Create a new problem instance to run (IMMUTABLE)
				//NOTE: We don't validate the instance name at all, it's entirely up to the target algorithm how to interpret these
				//commonly we use filenames, but as far as AEATK is concerned this is of no consequence.
				ProblemInstance pi;
				if(mainOptions.instanceName == null)
				{
					
					List<ProblemInstance> instances = mainOptions.getTrainingAndTestProblemInstances().getTrainingInstances().getInstances();
					if(instances == null || instances.size() == 0)
					{
						throw new ParameterException("No instances available, please specify one manually via --instance argument");
					}
					
					switch(mainOptions.instanceSelection)
					{
						case RANDOM:
							Random r = new MersenneTwister();
							pi = instances.get(r.nextInt(instances.size()));
							break;
						case FIRST:
							pi = instances.get(0);
							break;
						default:
							//Should always handle the default case and throw an exception if unsure
							throw new IllegalArgumentException("Unknown value for option : " + mainOptions.instanceSelection);
					}
					
				} else
				{
					pi = new ProblemInstance(mainOptions.instanceName);
				}
				
				
			
				//The following is a common convention used in AEATK
				if(execConfig.isDeterministicAlgorithm())
				{
					if (mainOptions.seed != -1)
					{
						//A simple log message with SLF4J
						log.warn("It is convention to use -1 as the seed for deterministic algorithms");
					}
				} else
				{
					if(mainOptions.seed == -1)
					{
						//A simple log message with SLF4J
						log.warn("It is convention that -1 be used as seed only for deterministic algorithms");
					}
				}
				
				//A problem instance seed pair object (IMMUTABLE)
				ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, mainOptions.seed);
				
				//A Configuration Space object it represents the space of allowable configurations (IMMUTABLE).
				//"ParamFile" is a deprecated term for it that is still in use in the code base
				ParameterConfigurationSpace configSpace = execConfig.getParameterConfigurationSpace();
			
				
				//If we are asked to supply a random a configuration, we need to pass a Random object
				Random configSpacePRNG = new MersenneTwister(mainOptions.configSeed);
				
				
				//Converts the string based configuration in the options object, to a point in the above space
				ParameterConfiguration config = configSpace.getParameterConfigurationFromString(mainOptions.config, ParameterStringFormat.NODB_OR_STATEFILE_SYNTAX, configSpacePRNG);
				
				//ParamConfiguration objects implement the Map<String, String> interface (but not all methods are implemented)
				//Other methods have restricted semantics, for instance you must ensure that you are only placing keys with valid values in the map. 
				//They are MUTABLE, but doing this after they have been "used" is likely to cause problems.
				for(Entry<String, String> entry : mainOptions.configSettingsToOverride.entrySet())
				{
					config.put(entry.getKey(), entry.getValue());
				}
			
				
				//A RunConfig object stores the information needed to actually request (compare the objects here to the information passed to the wrapper as listed in the Manual)
				//It is also IMMUTABLE
				AlgorithmRunConfiguration runConfig = new AlgorithmRunConfiguration(pisp, execConfig.getAlgorithmMaximumCutoffTime(), config,execConfig);
				
				processRunConfig(runConfig, tae, mainOptions.killTime);
				
				
			} finally
			{
				//We need to tell the TAE we are shutting down
				//Otherwise the program may not exit 
				if(tae != null)
				{
					tae.notifyShutdown();
				}
			}
		} catch(ParameterException e)
		{	
			log.error(e.getMessage());
			if(log.isDebugEnabled())
			{
				log.error("Stack trace:",e);
			}
		} catch(Exception e)
		{
			
			e.printStackTrace();
		}
	}
	

	
	
	/**
	 * Encapsulated method for evaluating a run
	 * 
	 * @param runConfig 	runConfig to evaluate
	 * @param tae 			target algorithm evaluator to use
	 */
	public static void processRunConfig(AlgorithmRunConfiguration runConfig, TargetAlgorithmEvaluator tae, final double killTime)
	{
		
		
		TargetAlgorithmEvaluatorRunObserver runStatus = new TargetAlgorithmEvaluatorRunObserver()
		{
			private long lastUpdate = 0;
			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) 
			{
				//As we print to standard out we want to make sure that a frequency that is too high doesn't spam the console
				if(System.currentTimeMillis() - lastUpdate < 1000)
				{
					return;
				}
				
				for(int i=0; i < runs.size(); i++)
				{
					AlgorithmRunResult run = runs.get(i);
					//Log messages with more than 2 arguments, must use pass them as an array.
					Object[] logArguments = { i, run.getAlgorithmRunConfiguration().getProblemInstanceSeedPair().getProblemInstance(), run.getRunStatus(), run.getRuntime()};
					log.info("Run {} on {} has status =>  {}, {}", logArguments);
					if(run.getRuntime() > killTime)
					{
						log.info("Dynamically killing run");
						run.kill();
					}
				}
				lastUpdate = System.currentTimeMillis();
				
			}
		
		};
		
		
		//Invoke the runs with the observer
		List<AlgorithmRunResult> runResults = tae.evaluateRun(Collections.singletonList(runConfig), runStatus); 
		
		 
		log.info("Run Completed");
		
		for(int i=0; i < runResults.size(); i++)
		{
			//AlgorithmRun objects can be viewed as an "answer" to the RunConfig "question"
			//They are IMMUTABLE.
			AlgorithmRunResult run = runResults.get(i);
		
			//This is the same RunConfig as above
			//But in general you should always use the information in the AlgorithmRun
			AlgorithmRunConfiguration resultRunConfig = run.getAlgorithmRunConfiguration();

			//Object representing whether the run reported SAT, UNSAT, TIMEOUT, etc...
			RunStatus runResult = run.getRunStatus();
		
			double runtime = run.getRuntime();
			double runLength = run.getRunLength();
			double quality = run.getQuality();
			
			//The algorithm must echo back the seed that we request to it (historically this has helped with debugging)
			long resultSeed = run.getResultSeed();
			long requestSeed = resultRunConfig.getProblemInstanceSeedPair().getSeed();
			
			//Additional run data is just a string that the algorithm returned and we will keep track of.
			String additionalData = run.getAdditionalRunData();
			
			if(resultSeed != requestSeed)
			{ 
				//A more complicated SLF4J log message. The {} are replaced with the parameters in order
				log.error("Algorithm Run Result does not have a matching seed, requested: {} , returned: {}", resultSeed, requestSeed );
			}
			
			//The toString() method does not return the actual configuration, this method is the best way to print them
			String configString = resultRunConfig.getParameterConfiguration().getFormattedParameterString(ParameterStringFormat.NODB_OR_STATEFILE_SYNTAX);
			
			//Log messages with more than 2 parameters must have them passed as an array.
			Object[] logArguments = { i, resultRunConfig.getProblemInstanceSeedPair().getProblemInstance(), configString, runResult, runtime, runLength, quality, resultSeed, additionalData};
			log.info("Run {} on {} with config: {} had the result => {}, {}, {}, {}, {}, {}", logArguments);
		}
	}

			
}
