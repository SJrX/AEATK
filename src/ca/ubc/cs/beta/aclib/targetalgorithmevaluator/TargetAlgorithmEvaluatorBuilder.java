package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.options.AlgorithmExecutionOptions;
import ca.ubc.cs.beta.aclib.options.ScenarioOptions;
import ca.ubc.cs.beta.aclib.options.TargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbortOnCrashTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbortOnFirstRunCrashTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.TimingCheckerTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.LeakingMemoryTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.RetryCrashedRunsTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.RunHashCodeVerifyingAlgorithmEvalutor;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.VerifySATTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.loader.TargetAlgorithmEvaluatorLoader;


public class TargetAlgorithmEvaluatorBuilder {

	private static Logger log = LoggerFactory.getLogger(TargetAlgorithmEvaluatorBuilder.class);
	
	/**
	 * Generates the TargetAlgorithmEvaluator with the given runtime behaivor
	 * 
	 * @param options 		   Target Algorithm Evaluator Options
	 * @param execConfig	   Execution configuration for the target algorithm
	 * @param noHashVerifiers  Whether we should apply hash verifiers				
	 * @return
	 */
	public static TargetAlgorithmEvaluator getTargetAlgorithmEvaluator(TargetAlgorithmEvaluatorOptions options, AlgorithmExecutionConfig execConfig, boolean hashVerifiersAllowed, Map<String, AbstractOptions> taeOptionsMap)
	{
		return getTargetAlgorithmEvaluator(options, execConfig, hashVerifiersAllowed, taeOptionsMap, null);
	}
	
	/**
	 * Generates the TargetAlgorithmEvaluator with the given runtime behaivor
	 * 
	 * @param options 		   Target Algorithm Evaluator Options
	 * @param execConfig	   Execution configuration for the target algorithm
	 * @param noHashVerifiers  Whether we should apply hash verifiers
	 * @param tae			   Existing Target Algorithm Evaluator to wrap (if null, will use the options to construct one)				
	 * @return
	 */
	public static TargetAlgorithmEvaluator getTargetAlgorithmEvaluator(TargetAlgorithmEvaluatorOptions options, AlgorithmExecutionConfig execConfig, boolean hashVerifiersAllowed, Map<String, AbstractOptions> taeOptionsMap, TargetAlgorithmEvaluator tae)
	{
		
		if(taeOptionsMap == null)
		{
			throw new IllegalArgumentException("taeOptionsMap must be non-null and contain the option objects for all target algorithm evaluators");
		}
		
		TargetAlgorithmEvaluator algoEval;
		if(tae == null)
		{
			String taeKey = options.targetAlgorithmEvaluator;
			AbstractOptions taeOptions = taeOptionsMap.get(taeKey);
			algoEval = TargetAlgorithmEvaluatorLoader.getTargetAlgorithmEvaluator(execConfig, options.maxConcurrentAlgoExecs, taeKey,taeOptions);
		}  else
		{
			algoEval = tae;
		}
		
		if(algoEval == null)
		{
			throw new IllegalStateException("TAE should have been non-null");
		}
		//===== Note the decorators are not in general commutative
		//Specifically Run Hash codes should only see the same runs the rest of the applications see
		//Additionally retrying of crashed runs should probably happen before Abort on Crash
		
		algoEval = new RetryCrashedRunsTargetAlgorithmEvaluator(options.retryCount, algoEval);
		
		
		if(options.abortOnCrash)
		{
			algoEval = new AbortOnCrashTargetAlgorithmEvaluator(algoEval);
		}
		
		
		if(options.abortOnFirstRunCrash)
		{
			algoEval = new AbortOnFirstRunCrashTargetAlgorithmEvaluator(algoEval);
			
			if(options.abortOnCrash)
			{
				log.warn("Configured to treat all crashes as aborts, it is redundant to also treat the first as an abort");
			}
		}
		
		
		if(options.verifySAT != null)
		{
			if(options.verifySAT)
			{
				log.debug("Verifying SAT Responses");
				algoEval = new VerifySATTargetAlgorithmEvaluator(algoEval);
				
			}
		}
		//==== Run Hash Code Verification should be last
		if(hashVerifiersAllowed)
		{
			
			if(options.leakMemory)
			{
				LeakingMemoryTargetAlgorithmEvaluator.leakMemoryAmount(options.leakMemoryAmount);
				log.warn("Target Algorithm Evaluators will leak memory. I hope you know what you are doing");
				algoEval = new LeakingMemoryTargetAlgorithmEvaluator(algoEval);
				
			}
			
			
			
			
			if(options.runHashCodeFile != null)
			{
				log.info("Algorithm Execution will verify run Hash Codes");
				Queue<Integer> runHashCodes = parseRunHashCodes(options.runHashCodeFile);
				algoEval = new RunHashCodeVerifyingAlgorithmEvalutor(algoEval, runHashCodes);
				 
			} else
			{
				log.info("Algorithm Execution will NOT verify run Hash Codes");
				algoEval = new RunHashCodeVerifyingAlgorithmEvalutor(algoEval);
			}

		}
		
		
		algoEval = new TimingCheckerTargetAlgorithmEvaluator(execConfig, algoEval);
		
		return algoEval;
	}
	
	
	
	private static Pattern RUN_HASH_CODE_PATTERN = Pattern.compile("^Run Hash Codes:\\d+( After \\d+ runs)?\\z");
	
	private static Queue<Integer> parseRunHashCodes(File runHashCodeFile) 
	{
		log.info("Run Hash Code File Passed {}", runHashCodeFile.getAbsolutePath());
		Queue<Integer> runHashCodeQueue = new LinkedList<Integer>();
		BufferedReader bin = null;
		try {
			try{
				bin = new BufferedReader(new FileReader(runHashCodeFile));
			
				String line;
				int hashCodeCount=0;
				int lineCount = 1;
				while((line = bin.readLine()) != null)
				{
					
					Matcher m = RUN_HASH_CODE_PATTERN.matcher(line);
					if(m.find())
					{
						Object[] array = { ++hashCodeCount, lineCount, line};
						log.debug("Found Run Hash Code #{} on line #{} with contents:{}", array);
						int colonIndex = line.indexOf(":");
						int spaceIndex = line.indexOf(" ", colonIndex);
						String lineSubStr = line.substring(colonIndex+1,spaceIndex);
						runHashCodeQueue.add(Integer.valueOf(lineSubStr));
						
					} else
					{
						log.trace("No Hash Code found on line: {}", line );
					}
					lineCount++;
				}
				if(hashCodeCount == 0)
				{
					log.warn("Hash Code File Specified, but we found no hash codes");
				}
			
			} finally
			{
				if(bin != null) bin.close();
			}
			
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		
		
		return runHashCodeQueue;
		
	}
	
}
