package ca.ubc.cs.beta.aclib.example.tae;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.jcommander.JCommanderHelper;
import ca.ubc.cs.beta.aclib.misc.options.UsageSection;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.options.ConfigToLaTeX;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorBuilder;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.loader.TargetAlgorithmEvaluatorLoader;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import ec.util.MersenneTwister;

/**
 * This is essentially the same class <code>TargetAlgorithmEvaluatorRunner</code>
 * but much less verbose. It does not print the options, versions, or available Target Algorithm Evaluators.
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class MinimalTargetAlgorithmEvaluatorRunner 
{
	private static Logger log  =  LoggerFactory.getLogger(MinimalTargetAlgorithmEvaluatorRunner.class); ;
	
	public static void main(String[] args)
	{
		
		TargetAlgorithmEvaluatorRunnerOptions mainOptions = new TargetAlgorithmEvaluatorRunnerOptions();
		Map<String,AbstractOptions> taeOptions = TargetAlgorithmEvaluatorLoader.getAvailableTargetAlgorithmEvaluators();

		try {
			
			JCommander jcom = JCommanderHelper.getJCommander(mainOptions, taeOptions);
			jcom.parse(args);
						
			AlgorithmExecutionConfig execConfig = mainOptions.algoExecOptions.getAlgorithmExecutionConfig();
			
			boolean hashVerifiers = false;
			TargetAlgorithmEvaluator tae = null;
			try {
				tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(mainOptions.algoExecOptions.taeOpts, execConfig, hashVerifiers, taeOptions);
				
				
				ProblemInstance pi = new ProblemInstance(mainOptions.instanceName);		
				ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, mainOptions.seed);
				
				
				ParamConfigurationSpace configSpace = execConfig.getParamFile();
				Random configSpacePRNG = new MersenneTwister(mainOptions.configSeed);
				ParamConfiguration config = configSpace.getConfigurationFromString(mainOptions.config, StringFormat.NODB_OR_STATEFILE_SYNTAX, configSpacePRNG);
				for(Entry<String, String> entry : mainOptions.configSettingsToOverride.entrySet())
				{
					config.put(entry.getKey(), entry.getValue());
				}
			
				RunConfig runConfig = new RunConfig(pisp, execConfig.getAlgorithmCutoffTime(), config);
				
				processRunConfig(runConfig, tae);
			} finally
			{
				if(tae != null) tae.notifyShutdown();
			}

		} catch(ParameterException e)
		{	
			List<UsageSection> sections = ConfigToLaTeX.getParameters(mainOptions, taeOptions);
			boolean showHiddenParameters = false; 
			ConfigToLaTeX.usage(sections, false);
			log.error(e.getMessage());
		}
	}
	
	public static void processRunConfig(RunConfig runConfig, TargetAlgorithmEvaluator tae)
	{
		List<AlgorithmRun> runResults = tae.evaluateRun(runConfig); 
		
		log.info("Run Completed");
		
		for(int i=0; i < runResults.size(); i++)
		{
			
			AlgorithmRun run = runResults.get(i);
			RunConfig resultRunConfig = run.getRunConfig();				
			ProblemInstance resultPi = resultRunConfig.getProblemInstanceSeedPair().getInstance();
			RunResult runResult = run.getRunResult();
		
			double runtime = run.getRuntime();
			double runLength = run.getRunLength();
			double quality = run.getQuality();
				
			long resultSeed = run.getResultSeed();
			long requestSeed = resultRunConfig.getProblemInstanceSeedPair().getSeed();
		
			String additionalData = run.getAdditionalRunData();
			
			Object[] logArguments = { i, resultRunConfig.getProblemInstanceSeedPair().getInstance(), resultRunConfig.getParamConfiguration().getFormattedParamString(StringFormat.NODB_OR_STATEFILE_SYNTAX), runResult, runtime, runLength, quality, resultSeed, additionalData};
			log.info("Run {} on {} with config: {} had the result => {}, {}, {}, {}, {}, {}", logArguments);
		}
		
	}
}
