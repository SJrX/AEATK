package ca.ubc.cs.beta.targetalgorithmevaluator;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableAlgorithmRun;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.exceptions.TargetAlgorithmAbortException;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.logback.MarkerFilter;
import ca.ubc.cs.beta.aclib.misc.logging.LoggingMarker;
import ca.ubc.cs.beta.aclib.misc.random.SeedableRandomSingleton;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.cli.CommandLineTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.cli.CommandLineTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbortOnCrashTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbortOnFirstRunCrashTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.TimingCheckerTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.EqualTargetAlgorithmEvaluatorTester;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.VerifySATTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.targetalgorithmevaluator.massiveoutput.MassiveOutputParamEchoExecutor;


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
	}
	Random r;
	
	PrintStream old;
	ByteArrayOutputStream bout;
	public void startOutputCapture()
	{
	
		bout = new ByteArrayOutputStream();
		old = System.out;
		System.setOut(new PrintStream(bout));
	}
	
	
	public String stopOutputCapture()
	{
		System.setOut(old);
		String boutString = bout.toString();
		System.out.println(boutString);
		return boutString;
	}
	
	@Before
	public void beforeTest()
	{
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig);
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
		
	
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		
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
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());

		}
	}
	
	
	/**
	 * Tests whether warnings are generated for Algorithms exceeding there runtime
	 */
	@Test
	public void testTimingWarningGeneratorTAE()
	{
		
	
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(SleepyParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		tae = new EchoTargetAlgorithmEvaluator( execConfig);
		
		((EchoTargetAlgorithmEvaluator) tae).wallClockTime = 50;
		
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 10; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("runtime", ""+(i));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 0.01, config);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		TargetAlgorithmEvaluator tae = new TimingCheckerTargetAlgorithmEvaluator(execConfig, this.tae);
		
		StringWriter sw = new StringWriter();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		PrintStream out = System.out;
		System.setOut(new PrintStream(bout));
		
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
		assertTrue(bout.toString().contains("Algorithm has exceeded allowed wallclock time by 49.99 seconds"));
		assertTrue(bout.toString().contains("Algorithm has exceeded allowed runtime by 1.99 seconds"));
		assertTrue(bout.toString().contains("Algorithm has exceeded allowed runtime by 3.99 seconds"));
		assertTrue(bout.toString().contains("Algorithm has exceeded allowed runtime by 5.99 seconds"));
		assertTrue(bout.toString().contains("Algorithm has exceeded allowed runtime by 7.99 seconds"));
		
		System.setOut(out);
		System.out.println(bout.toString());
		
		for(AlgorithmRun run : runs)
		{
			ParamConfiguration config  = run.getRunConfig().getParamConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunResult().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());

		}
		
		tae.notifyShutdown();
	}
	
	
	
	
	
	/**
	 * This just tests to see if EchoTargetAlgorithmEvaluator matches the CLI Version
	 */
	@Test
	public void testCLIAndDirectEquality()
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
		TargetAlgorithmEvaluator tae = new EqualTargetAlgorithmEvaluatorTester(TAETestSet.tae, new EchoTargetAlgorithmEvaluator(execConfig));
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
		
		
		for(AlgorithmRun run : runs)
		{
			ParamConfiguration config  = run.getRunConfig().getParamConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunResult().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());

		}
	}
	
	
	
	/**
	 * This just tests to see if ParamEchoExecutor does what it should
	 */
	@Test
	public void testMirrorWithAdditionalData()
	{
		
	
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutorWithGibberish.class.getCanonicalName());
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig);
		SeedableRandomSingleton.reinit();
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		this.r = SeedableRandomSingleton.getRandom();
		
		
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
		
		int i=0; 
		for(AlgorithmRun run : runs)
		{
			ParamConfiguration config  = run.getRunConfig().getParamConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunResult().name());
			//This executor should not have any additional run data
			
			try {
				assertTrue(!run.getAdditionalRunData().equals("")); 
			} catch(AssertionError e)
			{
				System.out.println(run);
				throw e;
			}
			

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

	/**
	 * Tests to see if we runs with no matching output get treated as CRASHED and logged correctly
	 */
	@Test
	public void testCrashIfNoMatchingOutput()
	{
		
	
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		
		
		ParamConfiguration config = configSpace.getRandomConfiguration();
		config.put("solved","SAT");
		RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
		runConfigs.add(rc);
	
	
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(DoNothingExecutor.class.getCanonicalName());
		
		
		
		AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500); 
		
		TargetAlgorithmEvaluator tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig);
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
		for(AlgorithmRun run : runs)
		{
			assertEquals(RunResult.CRASHED,run.getRunResult());
		}
			
		
	}
	
	@Test
	public void testSatAliases()
	{
		AlgorithmExecutionConfig execConfig;
		
		ParamConfigurationSpace configSpace;
		
		
		File paramFile = TestHelper.getTestFile("paramFiles/paramAliasEchoParamFile.txt");
			configSpace = new ParamConfigurationSpace(paramFile);
			
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamAliasEchoExecutor.class.getCanonicalName());
		
		
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
			
		tae = new AbortOnCrashTargetAlgorithmEvaluator(CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig));
		SeedableRandomSingleton.reinit();
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		this.r = SeedableRandomSingleton.getRandom();
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
				
		
		
		
		for(String alias : RunResult.SAT.getAliases())
		{
			
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved", alias);
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
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
			assertEquals(RunResult.getAutomaticConfiguratorResultForKey(config.get("solved")), RunResult.SAT);
			assertEquals("",run.getAdditionalRunData()); //No Additional Run Data Expected

		}
		
	}
	

	@Test
	public void testUnSatAliases()
	{
		AlgorithmExecutionConfig execConfig;
		
		ParamConfigurationSpace configSpace;
		
		
		File paramFile = TestHelper.getTestFile("paramFiles/paramAliasEchoParamFile.txt");
			configSpace = new ParamConfigurationSpace(paramFile);
			
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamAliasEchoExecutor.class.getCanonicalName());
		
		
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
			
		tae = new AbortOnCrashTargetAlgorithmEvaluator(CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig));
		SeedableRandomSingleton.reinit();
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		this.r = SeedableRandomSingleton.getRandom();
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
				
		
		
		
		for(String alias : RunResult.UNSAT.getAliases())
		{
			
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved", alias);
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
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
			assertEquals(RunResult.getAutomaticConfiguratorResultForKey(config.get("solved")), RunResult.UNSAT);
			
			assertEquals("",run.getAdditionalRunData()); //No Additional Run Data Expected

		}
		
	}
	
	

	@Test
	/**
	 * This tests to ensure that the runs that don't have a Run Result are treated as crashed and the right error message appears
	 */
	public void testRegexMatchButNoRunResultOutput()
	{
		AlgorithmExecutionConfig execConfig;
		
		ParamConfigurationSpace configSpace;
		
		
		File paramFile = TestHelper.getTestFile("paramFiles/paramAliasEchoParamFile.txt");
			configSpace = new ParamConfigurationSpace(paramFile);
			
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(RegexMatchingButIncompleteOutputExecutor.class.getCanonicalName());
		
		
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
			
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig);
		SeedableRandomSingleton.reinit();
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		this.r = SeedableRandomSingleton.getRandom();
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
				
		
		
		
		for(String alias : RunResult.UNSAT.getAliases())
		{
			
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved", alias);
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		}
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(bout);
		
		PrintStream oldOut = System.out;
		
		System.setOut(pw);
		List<AlgorithmRun> runs = new ArrayList<AlgorithmRun>();
		try {
		runs = tae.evaluateRun(runConfigs);
		} finally
		{
			System.setOut(oldOut);
			System.out.println(bout.toString());
			pw.close();
			
		}
		
		assertEquals(runs.size(), runConfigs.size());
		assertTrue(bout.toString().contains("Most likely the Algorithm did not report a result string as one of"));
		
		
		for(AlgorithmRun run : runs)
		{
			assertEquals( RunResult.CRASHED, run.getRunResult());
		}
		
	}
	

	@Test
	/**
	 * This tests to ensure that the runs that don't have a Run Result are treated as crashed and the right error message appears
	 */
	public void testRegexMatchButInvalidNumber()
	{
		AlgorithmExecutionConfig execConfig;
		
		ParamConfigurationSpace configSpace;
		
		
		File paramFile = TestHelper.getTestFile("paramFiles/paramAliasEchoParamFile.txt");
			configSpace = new ParamConfigurationSpace(paramFile);
			
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(RegexMatchingButInvalidNumberOutputExecutor.class.getCanonicalName());
		
		
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
			
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig);
		SeedableRandomSingleton.reinit();
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		this.r = SeedableRandomSingleton.getRandom();
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
				
		
		
		
		for(String alias : RunResult.UNSAT.getAliases())
		{
			
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved", alias);
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		}
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(bout);
		
		PrintStream oldOut = System.out;
		
		System.setOut(pw);
		List<AlgorithmRun> runs = new ArrayList<AlgorithmRun>();
		try {
		runs = tae.evaluateRun(runConfigs);
		} finally
		{
			System.setOut(oldOut);
			System.out.println(bout.toString());
			pw.close();
			
		}
		
		assertEquals(runs.size(), runConfigs.size());
		
		
		
		for(AlgorithmRun run : runs)
		{
			assertEquals(RunResult.CRASHED, run.getRunResult());
		}
		
		assertTrue(bout.toString().contains("Most likely one of the values of runLength, runtime, quality could not be parsed as a Double, or the seed could not be parsed as a valid long"));
	}
	
	@Test
	/**
	 * This tests to ensure that the runs that don't have a Run Result are treated as crashed and the right error message appears
	 */
	public void testRegexMatchButMissingCommas()
	{
		AlgorithmExecutionConfig execConfig;
		
		ParamConfigurationSpace configSpace;
		
		
		File paramFile = TestHelper.getTestFile("paramFiles/paramAliasEchoParamFile.txt");
			configSpace = new ParamConfigurationSpace(paramFile);
			
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(RegexMatchingButMissingOutputExecutor.class.getCanonicalName());
		
		
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
			
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig);
		SeedableRandomSingleton.reinit();
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		this.r = SeedableRandomSingleton.getRandom();
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
				
		
		
		
		for(String alias : RunResult.UNSAT.getAliases())
		{
			
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved", alias);
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		}
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(bout);
		
		PrintStream oldOut = System.out;
		
		System.setOut(pw);
		List<AlgorithmRun> runs = new ArrayList<AlgorithmRun>();
		try {
		runs = tae.evaluateRun(runConfigs);
		} finally
		{
			System.setOut(oldOut);
			System.out.println(bout.toString());
			pw.close();
			
		}
		
		assertEquals(runs.size(), runConfigs.size());
		
		for(AlgorithmRun run : runs)
		{
			assertEquals( RunResult.CRASHED, run.getRunResult());
		}
		
		assertTrue(bout.toString().contains("Most likely the algorithm did not specify all of the required outputs that is <solved>,<runtime>,<runlength>,<quality>,<seed>"));
	}
	
	
	
	/**
	 * This just tests to see if ParamEchoExecutor does what it should
	 * This tested for out of memory issues with the regular expression but
	 * didn't conclusively find anything.
	 * 
	 */
	@Test
	@Ignore
	public void testMassiveOutput()
	{
		
		MarkerFilter.deny(LoggingMarker.FULL_PROCESS_OUTPUT);
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(MassiveOutputParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig);
		SeedableRandomSingleton.reinit();
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		this.r = SeedableRandomSingleton.getRandom();
		
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(1);
		for(int i=0; i < 1; i++)
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
		System.out.println("Suppressing Output");
		PrintStream out = System.out;
		System.setOut(new PrintStream(new NullOutputStream()));
		List<AlgorithmRun> runs;
		try {
		 runs = tae.evaluateRun(runConfigs);
		} finally{
			System.setOut(out);
		}
		
		
		for(AlgorithmRun run : runs)
		{
			ParamConfiguration config  = run.getRunConfig().getParamConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunResult().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());

		}
	}
	
	
	/**
	 * This tests to see if the Random white space executor and various other supported
	 * output formats are matched correctly
	 */
	@Test
	public void testWhiteSpaceInExecutorStart()
	{
		
	
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(RandomWhitespaceParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig);
		SeedableRandomSingleton.reinit();
		System.out.println("Seed" + SeedableRandomSingleton.getSeed());;
		this.r = SeedableRandomSingleton.getRandom();
		
		
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
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());

		}
	}
	
	
	/**
	 * This tests to make sure that VerifySATTargetAlgorithmEvaluator fires warnings when it is suppose to
	 * 
	 * 
	 * 
	 * 
	 */
	@Test
	public void testVerifySATTargetAlgorithmEvaluator()
	{
		
	
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		
		{
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved", "SAT");
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("SAT",1,new HashMap<String, Double>(),"SAT"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		
			config = configSpace.getRandomConfiguration();
			config.put("solved", "SAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("UNSAT",2,new HashMap<String, Double>(),"UNSAT"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
			
			config = configSpace.getRandomConfiguration();
			config.put("solved", "UNSAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("UNSAT",2,new HashMap<String, Double>(),"UNSAT"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
			
			
			config = configSpace.getRandomConfiguration();
			config.put("solved", "UNSAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("SAT",1,new HashMap<String, Double>(),"SAT"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
			
			
			
			
			config = configSpace.getRandomConfiguration();
			config.put("solved", "SAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("SATISFIABLE",3,new HashMap<String, Double>(),"SATISFIABLE"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		
			config = configSpace.getRandomConfiguration();
			config.put("solved", "SAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("UNSATISFIABLE",4,new HashMap<String, Double>(),"UNSATISFIABLE"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
			
			config = configSpace.getRandomConfiguration();
			config.put("solved", "UNSAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("UNSATISFIABLE",4,new HashMap<String, Double>(),"UNSATISFIABLE"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
			
			
			config = configSpace.getRandomConfiguration();
			config.put("solved", "UNSAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("SATISFIABLE",3,new HashMap<String, Double>(),"SATISFIABLE"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
			
			
			
			
			
			
			
			
			config = configSpace.getRandomConfiguration();
			config.put("solved", "TIMEOUT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("SAT",1,new HashMap<String, Double>(),"SAT"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		
			config = configSpace.getRandomConfiguration();
			config.put("solved", "SAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("UNKNOWN",2,new HashMap<String, Double>(),"UNKNOWN"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
			
		}	
			
			
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		TargetAlgorithmEvaluator tae =  new VerifySATTargetAlgorithmEvaluator(new EchoTargetAlgorithmEvaluator(execConfig));
		
		
		
		for(RunConfig rc : runConfigs)
		{
			
			
			
			startOutputCapture();
			List<AlgorithmRun> runs = tae.evaluateRun(rc);
			String output = stopOutputCapture();
			//System.out.println("<<<<<\n" + output+"\n<<<<<<");
			
			
			AlgorithmRun run = runs.get(0);
			
			
			switch(run.getRunResult())
			{
				case SAT:
					if(run.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceName().startsWith("SAT"))
					{
						assertFalse(output.contains("Mismatch occured between instance specific information"));
					} else
					{
						assertTrue(output.contains("Mismatch occured between instance specific information"));
					}
					
					break;
				case UNSAT:
					
					if(run.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceName().startsWith("UNSAT"))
					{
				
						assertFalse(output.contains("Mismatch occured between instance specific information"));
					} else
					{
						assertTrue(output.contains("Mismatch occured between instance specific information"));
					}
					
					break;
			
				default:
					assertFalse(output.contains("Mismatch occured between instance specific information"));	
			}
			ParamConfiguration config  = run.getRunConfig().getParamConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunResult().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());

		}
	}
	
	
}
