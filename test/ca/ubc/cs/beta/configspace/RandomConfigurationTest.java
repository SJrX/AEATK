package ca.ubc.cs.beta.configspace;

import static org.junit.Assert.*;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ca.ubc.cs.beta.aeatk.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParamFileHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;


public class RandomConfigurationTest {

	
	
	
	private static final SeedableRandomPool pool = new SeedableRandomPool(System.currentTimeMillis());
	
	@AfterClass
	public static void afterClass()
	{
		pool.logUsage();
	}
	
	@BeforeClass
	public static void setUpClass()
	{
	
	}
	
	
	
	
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
	
	
	public static ParameterConfigurationSpace getConfigSpaceForFile(String f)
	{
		URL url = ParameterConfigurationSpace.class.getClassLoader().getResource(f);
		File file = new File(url.getPath());
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(file);
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
		
		Random random = pool.getRandom(DebugUtil.getCurrentMethodName());
		String paramFile = "a [ 1 , " + BUCKETS +  "] [1] i";
		
		
		StringReader reader = new StringReader(paramFile);
		
		
		
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(reader);
		
		int[] count = new int[BUCKETS];
		
		
		for(int i=0; i < TRIALS; i++)
		{
			count[Integer.valueOf(configSpace.getRandomParameterConfiguration(random).get("a"))-1]++;
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
		
		Random random = pool.getRandom(DebugUtil.getCurrentMethodName());
		String paramFile = "b [ 1 , " + BUCKETS +  "] [1] il";
		StringReader sr = new StringReader(paramFile);
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr);
		
		double[] count = new double[BUCKETS];
		
		
		
		
		for(int i=0; i < TRIALS; i++)
		{
			int index = Integer.valueOf(configSpace.getRandomParameterConfiguration(random).get("b"));
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
		ParameterConfigurationSpace f = ParamFileHelper.getParamFileParser(("/ubc/cs/home/s/seramage/arrowspace/sm/sample_inputs/spear-params.txt"));
		Random random = pool.getRandom(DebugUtil.getCurrentMethodName());
		
	
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
			ParameterConfiguration config = f.getRandomParameterConfiguration(random);
			
			for(String key : config.keySet())
			{
				seenValues.get(key).add(config.get(key));
			}
			System.out.println("Iteration: " + i++ );
		}
		
		
		
		
		
	}

}
