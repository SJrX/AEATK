package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.loader;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.factory.TargetAlgorithmEvaluatorFactory;


public class TargetAlgorithmEvaluatorLoader {

	private static final Logger log = LoggerFactory.getLogger(TargetAlgorithmEvaluatorLoader.class);
	
	public static TargetAlgorithmEvaluator getTargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig, int maxConcurrentExecutions, String name)
	{
		Iterator<TargetAlgorithmEvaluatorFactory> taeIt = ServiceLoader.load(TargetAlgorithmEvaluatorFactory.class).iterator();
		
		while(taeIt.hasNext())
		{
			
			try { 
				TargetAlgorithmEvaluatorFactory tae= taeIt.next();
				log.debug("Found Target Algorithm Evaluator {}", tae.getName());
				
				if(tae.getName().contains(" "))
				{
					log.warn("Target Algorithm Evaluator has white space in it's name, this is a violation of the contract of {}", TargetAlgorithmEvaluatorFactory.class.getName());
				}
				if(tae.getName().trim().equals(name.trim()))
				{
					return tae.getTargetAlgorithmEvaluator(execConfig, maxConcurrentExecutions);
				}
			
			} catch(ServiceConfigurationError e)
			{
				log.warn("Error occured while retrieving instance", e);
			}
		}
			
		
		throw new IllegalStateException("No Target Algorithm Evalutor found for name: " + name);
	
	}
}
