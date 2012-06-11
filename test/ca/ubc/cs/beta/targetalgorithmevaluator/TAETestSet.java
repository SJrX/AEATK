package ca.ubc.cs.beta.targetalgorithmevaluator;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.ac.config.ProblemInstance;
import ca.ubc.cs.beta.ac.config.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.ac.config.RunConfig;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.configspace.ParamConfiguration;
import ca.ubc.cs.beta.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.instancespecificinfo.InstanceSpecificInfoTestExecutor;
import ca.ubc.cs.beta.random.SeedableRandomSingleton;
import ca.ubc.cs.beta.smac.ac.runners.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun;
import ca.ubc.cs.beta.smac.exceptions.TargetAlgorithmAbortException;


public class TAETestSet {

	
	private static TargetAlgorithmEvaluator tae;
	
	private static AlgorithmExecutionConfig execConfig;
	
	private static ParamConfigurationSpace configSpace;
	@BeforeClass
	public static void beforeClass()
	{
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt");
		configSpace = new ParamConfigurationSpace(paramFile);
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		
		
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false);
		
	}
	
	@Before
	public void beforeTest()
	{
		tae = new TargetAlgorithmEvaluator( execConfig, false); 	
	}
	
	/**
	 * This just tests to see if ParamEchoExecutor does what it should
	 */
	@Test
	public void testMirror()
	{
		SeedableRandomSingleton.reinit();
		
		Random r = SeedableRandomSingleton.getRandom();
		
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(100);
		for(int i=0; i < 100; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration();
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
		
		
		for(AlgorithmRun run : runs)
		{
			ParamConfiguration config  = run.getInstanceRunConfig().getParamConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunResult().name());

		}
	}
	
	public void assertDEquals(String d1, double d2, double delta)
	{
		assertDEquals(Double.valueOf(d1), d2, delta);
	}
	public void assertDEquals(String d1, String d2, double delta)
	{
		assertDEquals(Double.valueOf(d1), Double.valueOf(d2), delta);
	}
	
	
	public void assertDEquals(double d1, double d2, double delta)
	{
		if(d1 - d2 > delta) throw new AssertionError("Expected "  + (d1 - d2)+ " < " + delta);
		if(d2 - d1 > delta) throw new AssertionError("Expected "  + (d1 - d2)+ " < " + delta);
		
	}

	@Test
	public void testABORT()
	{
		SeedableRandomSingleton.reinit();
		
		Random r = SeedableRandomSingleton.getRandom();
		
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(100);
		for(int i=0; i < 100; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved","ABORT");
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(RunConfig run : runConfigs)
		{
			
			try {
				List<AlgorithmRun> runs = tae.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 
				//This is what we wanted 
				continue;
			}
			fail("Should not have reached this point, algorithm should have thrown an error");

		}
	}
		
	@Test
	public void testIllegalCaptimeException()
	{
		SeedableRandomSingleton.reinit();
		
		Random r = SeedableRandomSingleton.getRandom();
		
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(100);
		for(int i=0; i < 100; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved","ABORT");
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(RunConfig run : runConfigs)
		{
			
			try {
				List<AlgorithmRun> runs = tae.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 
				//This is what we wanted 
				continue;
			}
			fail("Should not have reached this point, algorithm should have thrown an error");

		}
	}
		
	
}
