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
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.exceptions.TargetAlgorithmAbortException;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.random.SeedableRandomSingleton;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.CommandLineTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbortOnCrashTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbortOnFirstRunCrashTargetAlgorithmEvaluator;


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
		
		
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
	}
	Random r;
	
	@Before
	public void beforeTest()
	{
		tae = new CommandLineTargetAlgorithmEvaluator( execConfig, false);
		SeedableRandomSingleton.reinit();
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		this.r = SeedableRandomSingleton.getRandom();
	}
	
	/**
	 * This just tests to see if ParamEchoExecutor does what it should
	 */
	@Test
	public void testMirror()
	{
		
	
		
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
			ParamConfiguration config  = run.getRunConfig().getParamConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunResult().name());

		}
	}
	
	/**
	 * Tests that the runCount actually increments over time
	 */
	@Test
	public void testRunCountIncrement()
	{
	
		
			
			configSpace.setPRNG(r);
			
			List<RunConfig> runConfigs = new ArrayList<RunConfig>(2);
			for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
			{
				runConfigs.clear();
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
				tae.evaluateRun(runConfigs);
				
				
				assertEquals(i+1,tae.getRunCount());
				
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
				tae.evaluateRun(run);
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
				assertEquals(run.getRunResult(), RunResult.valueOf(run.getRunConfig().getParamConfiguration().get("solved")));
				assertDEquals(run.getRunConfig().getParamConfiguration().get("runtime"), run.getRuntime(),0.05);
			}
			
			
			
		}
			
			
	}
	
	/**
	 * Tests that an algorithm that reports crash actually triggers an abort exception
	 */
	@Test
	public void testAbortOnCrashTAE()
	{
		
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved","CRASHED");
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(RunConfig run : runConfigs)
		{
			
			try {
				 tae.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 
				fail("Should not have crashed here, unwrapped TAE should not be aborting");
			}
			
			continue;

		}
		
		
		TargetAlgorithmEvaluator abortOnCrash = new AbortOnCrashTargetAlgorithmEvaluator(tae);
		
		for(RunConfig run : runConfigs)
		{
			try {
				 abortOnCrash.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 
				//This is what we wanted
				
				continue;
			}
			
			fail("We should have gotten the a TargetAlgorithmAbortException");

		}
		
		
		
		
		
	}


	
	/**
	 * Tests that the FirstRunCrashTAE behaves as expected 
	 * (i.e. if the first run crashes it aborts, otherwise it treats as crashes)
	 * This test (unlike the next, has a first run actually crash)
	 * 
	 */
	@Test
	public void testAbortOnFirstRunCrashTAEfirstIsACrash()
	{
		
	
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved","CRASHED");
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		}
		
		
		
		
		for(RunConfig run : runConfigs)
		{
			
			try {
					tae.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 
				fail("Should not have crashed here, unwrapped TAE should not be aborting");
			}
			
			continue;

		}
		
		
		TargetAlgorithmEvaluator abortOnCrash = new AbortOnFirstRunCrashTargetAlgorithmEvaluator(tae);
		
		boolean firstRun = true;
		for(RunConfig run : runConfigs)
		{
			try {
				abortOnCrash.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 

				if(firstRun) 
				{
					//This is what we wanted
					firstRun = false;
				} else
				{
					fail("Expected only the first run to trigger this");
				}
				continue;
			}
			
			
			if(firstRun)
			{
				fail("Expected the first to run trigget a target algorithm abort exception");
			}
		}
	}
	
	
	/**
	 * Tests that the FirstRunCrashTAE behaves as expected 
	 * (i.e. if the first run crashes it aborts, otherwise it treats as crashes)
	 * This tests has the first run not actually crash
	 * 
	 */
	@Test
	public void testAbortOnFirstRunCrashTAEfirstIsSAT()
	{
		
		
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration();
			if(i == 0 )
			{
				config.put("solved","SAT");
			} else
			{
				config.put("solved","CRASHED");
			}
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		}
		
		
		
		for(RunConfig run : runConfigs)
		{
			
			try {
				tae.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 
				fail("Should not have crashed here, unwrapped TAE should not be aborting");
			}
			
			continue;

		}
		
		TargetAlgorithmEvaluator abortOnCrash = new AbortOnFirstRunCrashTargetAlgorithmEvaluator(tae);

		for(RunConfig run : runConfigs)
		{
			try {
				abortOnCrash.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 
				fail("Only the first run should be able to trigger this and it was a SAT");				
			}
		}
	}

	
}
