package ca.ubc.cs.beta.configspace;

import static org.junit.Assert.*;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.StatUtils;
import org.junit.Ignore;
import org.junit.Test;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.configspace.ParamFileHelper;


public class RandomConfigurationTest {

	
	
	public boolean allValues(Map<String, Integer> possibleValues, Map<String, Set<String>> seenValues)
	{
		
		for(String key : possibleValues.keySet())
		{
			
			if(seenValues.get(key).size() == possibleValues.get(key))
			{
				continue;
			} 
			return false;
		}
		return true;
	}
	
	
	public static ParamConfigurationSpace getConfigSpaceForFile(String f)
	{
		URL url = ParamConfigurationSpace.class.getClassLoader().getResource(f);
		File file = new File(url.getPath());
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(file);
		return configSpace;
	}
	
	private static final int BUCKETS = 100;
	private static final int TRIALS = BUCKETS*10000;
	
	
	/**
	 * Tests that all integers are returned uniformly at random
	 */
	@Test
	public void testUniformInteger()
	{
		
		String paramFile = "a [ 1 , " + BUCKETS +  "] [1] i";
		
		
		StringReader reader = new StringReader(paramFile);
		
		
		
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(reader);
		
		int[] count = new int[BUCKETS];
		
		
		for(int i=0; i < TRIALS; i++)
		{
			count[Integer.valueOf(configSpace.getRandomConfiguration().get("a"))-1]++;
		}

		//Each bucket is binomially distributed
		double p = 1.0 / BUCKETS;
		
		double decaStandardDeviation = 5*Math.sqrt(TRIALS*(p)*(1-p));
		double expectedMean = TRIALS*p;
		
		
		
		
		for(int i=0; i < BUCKETS; i++)
		{
			System.out.println((i+1) + ":" + count[i]);
		}
		

		StringBuilder failureMessages = new StringBuilder();
		for(int i=0; i < BUCKETS;i++)
		{
			if ((count[i] < (expectedMean-decaStandardDeviation)) || (count[i] > (expectedMean+decaStandardDeviation)))
			{
				failureMessages.append("Bucket " + (i+1) + " was too far from the mean=" + expectedMean + " and 10 standard deviations=" + decaStandardDeviation + "with value=" + count[i]).append("\n");
			}
			
		}
		
		if(failureMessages.length() > 0)
		{
			fail(failureMessages.toString());
		}
	}
	
	/**
	 * Tests that all integers are returned uniformly at random
	 */
	@Test
	public void testLogInteger()
	{
		
		String paramFile = "b [ 1 , " + BUCKETS +  "] [1] il";
		StringReader sr = new StringReader(paramFile);
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr);
		
		double[] count = new double[BUCKETS];
		
		
		
		
		for(int i=0; i < TRIALS; i++)
		{
			int index = Integer.valueOf(configSpace.getRandomConfiguration().get("b"));
			count[index-1]++;
		}

		
		
		
		
		//System.out.println("sigma^2=" + Math.sqrt(StatUtils.populationVariance(count)));
		
		StringBuilder failureMessages = new StringBuilder();
		
		double ultimateMin = Math.log10(0.5);
		double ultimateMax = Math.log10(BUCKETS+0.5);
		double cdf = 0.0;
		for(int i=0; i < BUCKETS; i++)
		{
			
			
			double maxRange = (1.5+ (double)i); 
			double minRange =  ((double)i+0.5);
			maxRange = Math.log10(maxRange);
			minRange = Math.log10(minRange);
			//maxRange = maxRange*(BUCKETS);
			//minRange = minRange*(BUCKETS);
			
			double p = (maxRange - minRange) / (ultimateMax - ultimateMin);
			
			double decaStandardDeviation = 5*Math.sqrt(TRIALS*(p)*(1-p));
			double expectedMean = TRIALS*p;
			 cdf += p;
			
			System.out.println( (i+1) + "(" + minRange + "," + maxRange + "," + p+ "," + cdf+"):" + count[i] );
			if ((count[i] < (expectedMean-decaStandardDeviation)) || (count[i] > (expectedMean+decaStandardDeviation)))
			{
				failureMessages.append("Bucket " + (i+1) + " was too far from the mean=" + expectedMean + " and 10 standard deviations=" + decaStandardDeviation + "with value=" + count[i]).append("\n");
			}
			
			
			
			
			
		}
		
		
		if(failureMessages.length() > 0)
		{
			fail(failureMessages.toString());
		}
		
	}
	
	
	
	
	
	
	/**
	 * This tests to make sure that getRandomConfiguration() returns every possible value that we expect
	 */
	@Test
	@Ignore("Fix input paramfile")
	public void testAllValuesAppear() {
		ParamConfigurationSpace f = ParamFileHelper.getParamFileParser(("/ubc/cs/home/s/seramage/arrowspace/sm/sample_inputs/spear-params.txt"), 1234);
		
	
		Map<String,Integer> possibleValues =  new HashMap<String, Integer>(); 
		Map<String, Set<String>> seenValues = new HashMap<String, Set<String>>();
		
		
		for(String param : f.getParameterNames())
		{
			possibleValues.put(param, f.getValuesMap().get(param).size());
			seenValues.put(param, new HashSet<String>());
		}
		
		int i=0;
		
		while(!allValues(possibleValues, seenValues))
		{
			ParamConfiguration config = f.getRandomConfiguration();
			
			for(String key : config.keySet())
			{
				seenValues.get(key).add(config.get(key));
			}
			System.out.println("Iteration: " + i++ );
		}
		
		
		
		
		
	}

}
