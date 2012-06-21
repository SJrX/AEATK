package ca.ubc.cs.beta.targetalgorithmevaluator;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.ac.RunResult;
import ca.ubc.cs.beta.ac.config.RunConfig;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.configspace.ParamConfiguration;
import ca.ubc.cs.beta.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.random.SeedableRandomSingleton;
import ca.ubc.cs.beta.smac.ac.runners.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun;
import ca.ubc.cs.beta.smac.exceptions.TargetAlgorithmAbortException;


public class TAETestSet {

	
	private static TargetAlgorithmEvaluator tae;
	
	private static AlgorithmExecutionConfig execConfig;
	
	private static ParamConfigurationSpace configSpace;
	
	private static final int TARGET_RUNS_IN_LOOPS = 10;
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
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
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
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
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
		
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalCaptimeException()
	{
		SeedableRandomSingleton.reinit();
		
		Random r = SeedableRandomSingleton.getRandom();
		
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(100);
	
		ParamConfiguration config = configSpace.getRandomConfiguration();
		
		RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), -1, config);
		runConfigs.add(rc);
		
		System.out.println("Performing " + runConfigs.size() + " runs");
	}
	
	
	
	/**
	 * Checks to ensure that a misbehaiving wrapper has it's logical
	 * output corrected (so a SAT run should always have a time < cutoffTime)
	 * 
	 * This test fails currently
	 */
	@Ignore("Functionality not implemented yet, this test will fail. The feature that it is testing was pushed back")
	@Test
	public void testRuntimeGreaterThanCapTime()
	{
		SeedableRandomSingleton.reinit();
		
		Random r = SeedableRandomSingleton.getRandom();
		
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		
		configSpace.setPRNG(r);
		
		double cutoffTime = 300;
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration();
			
			config.put("solved", "SAT");
			
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), cutoffTime, config);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		
			
			
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
		
		for(AlgorithmRun run : runs)
		{
			
			if(run.getRuntime() >= cutoffTime)
			{
				assertEquals(RunResult.TIMEOUT, run.getRunResult());
			} else
			{
				assertEquals(run.getRunResult(), RunResult.valueOf(run.getInstanceRunConfig().getParamConfiguration().get("solved")));
				assertDEquals(run.getInstanceRunConfig().getParamConfiguration().get("runtime"), run.getRuntime(),0.05);
			}
			
			
			
		}
			
			
	}

	
}
