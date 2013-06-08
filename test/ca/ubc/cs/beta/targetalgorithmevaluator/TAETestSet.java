package ca.ubc.cs.beta.targetalgorithmevaluator;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.algorithmrunner.AutomaticConfiguratorFactory;
import ca.ubc.cs.beta.aclib.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.exceptions.IllegalWrapperOutputException;
import ca.ubc.cs.beta.aclib.exceptions.TargetAlgorithmAbortException;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.logback.MarkerFilter;
import ca.ubc.cs.beta.aclib.misc.logging.LoggingMarker;
import ca.ubc.cs.beta.aclib.misc.random.SeedableRandomSingleton;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.options.TargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.WaitableTAECallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.preloaded.PreloadedResponseTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.random.RandomResponseTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.random.RandomResponseTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.random.RandomResponseTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.debug.EqualTargetAlgorithmEvaluatorTester;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.helpers.BoundedTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.prepostcommand.PrePostCommandErrorException;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.safety.AbortOnCrashTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.safety.AbortOnFirstRunCrashTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.safety.ResultOrderCorrectCheckerTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.safety.TimingCheckerTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.safety.VerifySATTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.init.TargetAlgorithmEvaluatorBuilder;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.init.TargetAlgorithmEvaluatorLoader;
import ca.ubc.cs.beta.targetalgorithmevaluator.impl.SolQualSetTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.targetalgorithmevaluator.massiveoutput.MassiveOutputParamEchoExecutor;

@SuppressWarnings("unused")
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
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
		
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 10; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
		
	
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
		
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
	
		
			
			
			
			List<RunConfig> runConfigs = new ArrayList<RunConfig>(2);
			for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
			{
				runConfigs.clear();
				ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(100);
	
		ParamConfiguration config = configSpace.getRandomConfiguration(r);
		
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
		
		
	
		
		
		
		double cutoffTime = 300;
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
			
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
		
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
		
	
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
		
		
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
		
	
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		
		
		ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
			
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
			
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
			
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
			
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
			
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
		
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(1);
		for(int i=0; i < 1; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
		
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
		
	
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
			config.put("solved", "SAT");
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("SAT",1,new HashMap<String, Double>(),"SAT"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		
			config = configSpace.getRandomConfiguration(r);
			config.put("solved", "SAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("UNSAT",2,new HashMap<String, Double>(),"UNSAT"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
			
			config = configSpace.getRandomConfiguration(r);
			config.put("solved", "UNSAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("UNSAT",2,new HashMap<String, Double>(),"UNSAT"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
			
			
			config = configSpace.getRandomConfiguration(r);
			config.put("solved", "UNSAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("SAT",1,new HashMap<String, Double>(),"SAT"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
			
			
			
			
			config = configSpace.getRandomConfiguration(r);
			config.put("solved", "SAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("SATISFIABLE",3,new HashMap<String, Double>(),"SATISFIABLE"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		
			config = configSpace.getRandomConfiguration(r);
			config.put("solved", "SAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("UNSATISFIABLE",4,new HashMap<String, Double>(),"UNSATISFIABLE"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
			
			config = configSpace.getRandomConfiguration(r);
			config.put("solved", "UNSAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("UNSATISFIABLE",4,new HashMap<String, Double>(),"UNSATISFIABLE"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
			
			
			config = configSpace.getRandomConfiguration(r);
			config.put("solved", "UNSAT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("SATISFIABLE",3,new HashMap<String, Double>(),"SATISFIABLE"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
			
			
			
			
			
			
			
			
			config = configSpace.getRandomConfiguration(r);
			config.put("solved", "TIMEOUT");
			rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("SAT",1,new HashMap<String, Double>(),"SAT"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		
			config = configSpace.getRandomConfiguration(r);
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
	
	
	@Test
	/**
	 * Tests the SAT consistency checker
	 */
	public void testSATConsistencyChecker()
	{
	
		Map<String, AbstractOptions> taeOptionsMap = TargetAlgorithmEvaluatorLoader.getAvailableTargetAlgorithmEvaluators();
		
		TargetAlgorithmEvaluatorOptions opts = new TargetAlgorithmEvaluatorOptions();
		System.out.println(taeOptionsMap);
		opts.targetAlgorithmEvaluator = "PRELOADED";
		opts.checkSATConsistency = true;
		opts.checkSATConsistencyException = false;
		opts.boundRuns = false;
		
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[TIMEOUT=4],[CRASHED=2],[TIMEOUT=3],[CRASHED=1],[SAT=1],[SAT=2],[UNSAT=2],[UNSAT=3],[TIMEOUT=4],[CRASHED=2],[TIMEOUT=3],[CRASHED=1],[SAT=1],[SAT=2],[UNSAT=2],[UNSAT=3]";
		TargetAlgorithmEvaluator tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts, execConfig, false, taeOptionsMap);
		
		ProblemInstance satPi = new ProblemInstance("SATInstance");
		ProblemInstance unsatPi = new ProblemInstance("UNSATInstance");
		
		
		ProblemInstanceSeedPair satPiOne = new ProblemInstanceSeedPair(satPi, 1);
		ProblemInstanceSeedPair satPiTwo = new ProblemInstanceSeedPair(satPi, 2);
		ProblemInstanceSeedPair satPiThree = new ProblemInstanceSeedPair(satPi, 3);
		ProblemInstanceSeedPair satPiFour = new ProblemInstanceSeedPair(satPi, 4);
		
		ProblemInstanceSeedPair unSatPiOne = new ProblemInstanceSeedPair(unsatPi, 1);
		ProblemInstanceSeedPair unSatPiTwo = new ProblemInstanceSeedPair(unsatPi, 2);
		ProblemInstanceSeedPair unSatPiThree = new ProblemInstanceSeedPair(unsatPi, 3);
		ProblemInstanceSeedPair unSatPiFour = new ProblemInstanceSeedPair(unsatPi, 4);
		
		RunConfig satPiOneRC = new RunConfig(satPiOne, 0, ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration());
		RunConfig satPiTwoRC = new RunConfig(satPiTwo, 0, ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration());
		RunConfig unSatPiOneRC = new RunConfig(unSatPiOne, 0, ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration());
		RunConfig unSatPiTwoRC = new RunConfig(unSatPiTwo, 0, ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration());
		
		
		RunConfig satPiThreeRC = new RunConfig(satPiThree, 0, ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration());
		RunConfig satPiFourRC = new RunConfig(satPiFour, 0, ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration());
		RunConfig unSatPiThreeRC = new RunConfig(unSatPiThree, 0, ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration());
		RunConfig unSatPiFourRC = new RunConfig(unSatPiFour, 0, ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration());
		
		
		
		List<RunConfig> rcs = Arrays.asList(satPiOneRC, satPiTwoRC, unSatPiOneRC, unSatPiTwoRC, satPiThreeRC, satPiFourRC, unSatPiThreeRC, unSatPiFourRC);
		
		tae.evaluateRun(rcs);
		
		
		rcs = Arrays.asList(satPiOneRC, satPiTwoRC, unSatPiOneRC, unSatPiTwoRC, satPiThreeRC,  unSatPiThreeRC, satPiFourRC, unSatPiFourRC);
		
		

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(bout);
		
		PrintStream oldOut = System.out;
		
		System.setOut(pw);
		
		try {
			tae.evaluateRun(rcs);
		} finally
		{
		   System.setOut(oldOut);
		   System.out.println(bout.toString());
		   pw.close();
		}
		
		//The actual string value isn't important, and if this test is failing because a log message changed you can simply update this string 
		assertTrue("Expected error message to appear",bout.toString().contains("SAT/UNSAT discrepancy detected on problem instance: Instance:UNSATInstance"));
		assertTrue("Expected error message to appear",bout.toString().contains("SAT/UNSAT discrepancy detected on problem instance: Instance:SATInstance"));
		
		
		
		tae.notifyShutdown();
		
		
		opts.checkSATConsistencyException = true;
		
		tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts, execConfig, false, taeOptionsMap);
		rcs = Arrays.asList(satPiOneRC, satPiTwoRC, unSatPiOneRC, unSatPiTwoRC, satPiThreeRC,  unSatPiThreeRC, satPiFourRC, unSatPiFourRC);
		boolean exception = false;
		try {
			tae.evaluateRun(rcs);
		} catch(TargetAlgorithmAbortException e)
		{
			exception = true;
		}
		assertTrue("Expected that a TargetAlgorithmAbortException would have occured", exception);
		
	}
	
	@Test
	/**
	 * Tests the Pre and Post commands work
	 */
	public void testPrePostCommandTAE()
	{
	
		Map<String, AbstractOptions> taeOptionsMap = TargetAlgorithmEvaluatorLoader.getAvailableTargetAlgorithmEvaluators();
		
		TargetAlgorithmEvaluatorOptions opts = new TargetAlgorithmEvaluatorOptions();
		System.out.println(taeOptionsMap);
		opts.targetAlgorithmEvaluator = "PRELOADED";
		opts.checkSATConsistency = true;
		opts.checkSATConsistencyException = false;
		opts.boundRuns = false;
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ReturnCodeTester.class.getCanonicalName()).append(" ");
		
		
		opts.prePostOptions.preCommand = b.toString() + "0";
		opts.prePostOptions.postCommand = b.toString() + "0";
		
		
		/***
		 * Normal Test
		 */
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(bout);		
		PrintStream oldOut = System.out;
		System.setOut(pw);
		TargetAlgorithmEvaluator tae;
		try {
			 tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts, execConfig, false, taeOptionsMap);
		} finally
		{
		   System.setOut(oldOut);
		   System.out.println(bout.toString());
		   pw.close();
		}
		
		assertTrue(bout.toString().contains("Command completed"));

		
		bout = new ByteArrayOutputStream();
		pw = new PrintStream(bout);		
		oldOut = System.out;
		System.setOut(pw);
		
		try {
			tae.notifyShutdown();
		} finally
		{
		   System.setOut(oldOut);
		   System.out.println(bout.toString());
		   pw.close();
		}
		
		/**
		 * Error Test (no exception)
		 */
		opts.prePostOptions.preCommand = b.toString() + "227";
		opts.prePostOptions.postCommand = b.toString() + "228";
		
		bout = new ByteArrayOutputStream();
		pw = new PrintStream(bout);		
		oldOut = System.out;
		System.setOut(pw);
	
		try {
			 tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts, execConfig, false, taeOptionsMap);
		} finally
		{
		   System.setOut(oldOut);
		   System.out.println(bout.toString());
		   pw.close();
		}
		
		assertTrue(bout.toString().contains("Got a non-zero return code from process: 227"));

		
		bout = new ByteArrayOutputStream();
		pw = new PrintStream(bout);		
		oldOut = System.out;
		System.setOut(pw);
		
		try {
			tae.notifyShutdown();
		} finally
		{
		   System.setOut(oldOut);
		   System.out.println(bout.toString());
		   pw.close();
		}
		
		assertTrue(bout.toString().contains("Got a non-zero return code from process: 228"));
		
		/**
		 * Error Test (exception on startup)
		 */
		
		
		opts.prePostOptions.preCommand = b.toString() + "2";
		opts.prePostOptions.exceptionOnError = true;
		bout = new ByteArrayOutputStream();
		pw = new PrintStream(bout);		
		oldOut = System.out;
		System.setOut(pw);
		boolean exceptionOccurred = true;
		try {
			try {
				 tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts, execConfig, false, taeOptionsMap);
			} finally
			{
			   System.setOut(oldOut);
			   System.out.println(bout.toString());
			   pw.close();
			}
		} catch(PrePostCommandErrorException e)
		{
			exceptionOccurred = true;
		}
		
		assertTrue("Expect exception", exceptionOccurred);
		
		
		
		
	}
	
	/**
	 * Test invalid wrapper outputs
	 */
	@Test
	public void testInvalidArguments()
	{
			
		Map<String, AbstractOptions> taeOptionsMap = TargetAlgorithmEvaluatorLoader.getAvailableTargetAlgorithmEvaluators();
		
		TargetAlgorithmEvaluatorOptions opts = new TargetAlgorithmEvaluatorOptions();
		System.out.println(taeOptionsMap);
		opts.targetAlgorithmEvaluator = "PRELOADED";
		opts.checkSATConsistency = true;
		opts.checkSATConsistencyException = false;
		opts.boundRuns = false;
		
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[SAT=-1],";
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).quality = 0.0;
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).runLength = 0.0;
		
		TargetAlgorithmEvaluator tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts, execConfig, false, taeOptionsMap);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 1; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
		
		
		try {
			List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
			fail("Expected exception to be thrown: " + runs);
		} catch(IllegalWrapperOutputException e)
		{
			System.out.println(e.getMessage());
		}
		

		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[SAT=1],";
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).quality = Double.NaN;
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).runLength = 0.0;
		
		tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts, execConfig, false, taeOptionsMap);
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		try {
			List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
			fail("Expected exception to be thrown: " + runs);
		} catch(IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
		}
		
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[SAT=1],";
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).quality = 0;
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).runLength = -2;
		
		tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts, execConfig, false, taeOptionsMap);
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		try {
			List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
			fail("Expected exception to be thrown: " + runs);
		} catch(IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
		}
		
		
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[SAT=1],";
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).quality = 0;
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).runLength = Double.NaN;
		
		tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts, execConfig, false, taeOptionsMap);
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		try {
			List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
			fail("Expected exception to be thrown: " + runs);
		} catch(IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
		}
			
		
		
		
		
		

		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[SAT=1],";
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).quality = 0;
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).runLength = -1;
		
		tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts, execConfig, false, taeOptionsMap);
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		try {
			List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
			
		} catch(IllegalArgumentException e)
		{
			fail("Unexpected Exception Thrown: " + e);
		}
		
		

		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[SAT=" + Double.POSITIVE_INFINITY + "],";
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).quality = 0;
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).runLength = -1;
		
		tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts, execConfig, false, taeOptionsMap);
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		try {
			List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
			
		} catch(IllegalArgumentException e)
		{
			fail("Unexpected exception");
			
		}
		
		
	}
	
	@Test
	public void testOrderCheckingDecorator()
	{
		

		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
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
		
		
		//Test the TAE when we shuffle responses
		RandomResponseTargetAlgorithmEvaluatorOptions randOpts = new RandomResponseTargetAlgorithmEvaluatorOptions();
		long seed = System.currentTimeMillis();
		System.out.println("Order Checking Decorator used seed" + seed);
		randOpts.seed =seed;
		randOpts.shuffleResponses = true;
		
		TargetAlgorithmEvaluator tae = new RandomResponseTargetAlgorithmEvaluator(execConfig, randOpts);
		
		tae = new ResultOrderCorrectCheckerTargetAlgorithmEvaluatorDecorator(tae);
		try {
			try {
				List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
				fail("Expected Exception to have occured");
			} catch(IllegalStateException e)
			{
				System.out.println("GOOD: " + e.getMessage());
			}
	
			
			final AtomicBoolean taeCompletedSuccessfully = new AtomicBoolean();
			TargetAlgorithmEvaluatorCallback callback = new TargetAlgorithmEvaluatorCallback()
			{
	
				@Override
				public void onSuccess(List<AlgorithmRun> runs) {
					taeCompletedSuccessfully.set(true);
				}
	
				@Override
				public void onFailure(RuntimeException t) {
					System.out.println("GOOD ASYNC: " + t.getMessage());	
				}
				
			};
			
			WaitableTAECallback wait = new WaitableTAECallback(callback);
				
			tae.evaluateRunsAsync(runConfigs, wait);
			
			wait.waitForCompletion();
			
			assertFalse("TAE Should not have completed successfully", taeCompletedSuccessfully.get());
		} finally
		{
			tae.notifyShutdown();
		}
		
		
		//Test the TAE when we don't shuffle
		try {
			randOpts = new RandomResponseTargetAlgorithmEvaluatorOptions();
			
			System.out.println("Order Checking Decorator used seed" + seed);
			randOpts.seed =seed;
			
			tae = new RandomResponseTargetAlgorithmEvaluator(execConfig, randOpts);
			
			tae = new ResultOrderCorrectCheckerTargetAlgorithmEvaluatorDecorator(tae);
			
			try {
				List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
				System.out.println("GOOD: Completed");
			} catch(IllegalStateException e)
			{
				throw e;
			}
	
			
			final AtomicBoolean taeCompletedSuccessfully = new AtomicBoolean();
			TargetAlgorithmEvaluatorCallback callback = new TargetAlgorithmEvaluatorCallback()
			{
	
				@Override
				public void onSuccess(List<AlgorithmRun> runs) {
					taeCompletedSuccessfully.set(true);
				}
	
				@Override
				public void onFailure(RuntimeException t) {
					taeCompletedSuccessfully.set(false);
					t.printStackTrace();
				}
				
			};
			
			WaitableTAECallback wait = new WaitableTAECallback(callback);
				
			tae.evaluateRunsAsync(runConfigs, wait);
			
			wait.waitForCompletion();
			
			assertTrue("TAE Should not have completed successfully", taeCompletedSuccessfully.get());
			
		} finally
		{
			tae.notifyShutdown();
		}
		
		
	}
	
	@Test
	/**
	 * This tests for that the CLI will return eventually
	 * This is related to issue https://mantis.sjrx.net/view.php?id=1675
	 * 
	 * This test roughly tries to simulate a deadlock, if you look at the commit 0702803124e3513b8b5479b8ae5391d2df5ba38a the changes will show you where the deadlocks were
	 * occurring.
	 * 
	 */
	public void testDeadLockinCommandLineTargetAlgorithmEvaluator()
	{
	
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		final List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 100; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
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
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.concurrentExecution = true;
		
		
		tae = fact.getTargetAlgorithmEvaluator(execConfig, options);
		
		tae = new BoundedTargetAlgorithmEvaluator(tae,100, execConfig);
		
		AutomaticConfiguratorFactory.setMaximumNumberOfThreads(100);
		final AtomicBoolean finishedRuns = new AtomicBoolean(false);
		Runnable run = new Runnable()
		{
			public void run()
			{
				for(int i=0; i < 10; i++)
				{
					List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
				}
			
				finishedRuns.set(true);
			}
		};
		
		
		Executors.newSingleThreadExecutor(new SequentiallyNamedThreadFactory("DeadLock JUnit Test")).submit(run);
		
		
		
		
			//This 45 second sleep is probably incredibly sensitive.

		for(int i=0; i < 45; i++)
		{
			try {
				Thread.sleep(1000);
				if(finishedRuns.get())
				{
					break;
				}
				
				if(i == 40)
				{
					System.err.println("IF THIS TEST IS STILL OUTPUTTING THEN THE 45 SECOND SLEEP IS TOO LITTLE");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
		assertTrue("Deadlock probably occured", finishedRuns.get());	
	}
	
	@Test
	public void testBlockingTAEResubmitRunsHandler()
	{
		RandomResponseTargetAlgorithmEvaluatorFactory fact = new RandomResponseTargetAlgorithmEvaluatorFactory();
		
		RandomResponseTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		options.persistent = true;
		TargetAlgorithmEvaluator tae = fact.getTargetAlgorithmEvaluator(execConfig, options);
		
		
		tae = new BoundedTargetAlgorithmEvaluator(tae, 1, execConfig);
		
		final List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 1; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
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
		
		
		final TargetAlgorithmEvaluator tae2 = tae;
		
		final List<RunConfig> runConfigs2 = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 1; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
				runConfigs2.add(rc);
			}
		}
		
		
		
		
		
		final CountDownLatch latch = new CountDownLatch(1); 
		
		tae.evaluateRunsAsync(runConfigs, new TargetAlgorithmEvaluatorCallback()
		{

			@Override
			public void onSuccess(List<AlgorithmRun> runs) {

				System.out.println(runs);
				
				
				System.out.println(tae2.evaluateRun(runConfigs2));
				
				System.out.println("UM WHAT");
				latch.countDown();
				
			}

			@Override
			public void onFailure(RuntimeException e) {
				e.printStackTrace();
				
			}
			
		});
		
		
		try {
			System.out.println("Deadlock if this is the last thing you see");
			latch.await();
		} catch (InterruptedException e1) {
			Thread.currentThread().interrupt();
			return;
		}
		
	}
	
	
	
	@Test
	@Ignore
	public void testBoundedTAESubmissionSpeed()
	{
		//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
		

	}
	
	@Test
	public void testDecoratorsApplyTheSameWay()
	{
		RandomResponseTargetAlgorithmEvaluatorFactory fact = new RandomResponseTargetAlgorithmEvaluatorFactory();
		
		RandomResponseTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		options.persistent = true;
		options.cores = 10000;
		TargetAlgorithmEvaluator tae = fact.getTargetAlgorithmEvaluator(execConfig, options);
		
		
		tae = new BoundedTargetAlgorithmEvaluator(tae, 1, execConfig);
		
		final List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 1; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
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
		
		TargetAlgorithmEvaluator tae10 = new SolQualSetTargetAlgorithmEvaluatorDecorator( new SolQualSetTargetAlgorithmEvaluatorDecorator(tae, 5), 10);
		TargetAlgorithmEvaluator tae5 = new SolQualSetTargetAlgorithmEvaluatorDecorator( new SolQualSetTargetAlgorithmEvaluatorDecorator(tae, 10), 5);
		
		
		
		
		List<AlgorithmRun> runs = tae10.evaluateRun(runConfigs);
		for(AlgorithmRun run : runs)
		{
			assertEquals("Expected quality to be 10", 10, run.getQuality(), 0.01);
		}
		
		
		runs = tae5.evaluateRun(runConfigs);
		for(AlgorithmRun run : runs)
		{
			assertEquals("Expected quality to be 5", 5, run.getQuality(), 0.01);
		}
		
		final Semaphore s = new Semaphore(0);
		final AtomicInteger solQual = new AtomicInteger(0);
		tae10.evaluateRunsAsync(runConfigs, new TargetAlgorithmEvaluatorCallback() {
			
			@Override
			public void onSuccess(List<AlgorithmRun> runs) {
				solQual.set((int) runs.get(0).getQuality());
				s.release();
				
			}
			
			@Override
			public void onFailure(RuntimeException e) {
				e.printStackTrace();
				
			}
		});

		s.acquireUninterruptibly();
		assertEquals("Expect SolQual to be 10", 10,  solQual.get());
		
		tae5.evaluateRunsAsync(runConfigs, new TargetAlgorithmEvaluatorCallback() {
			
			@Override
			public void onSuccess(List<AlgorithmRun> runs) {
				solQual.set((int) runs.get(0).getQuality());
				s.release();
				
			}
			
			@Override
			public void onFailure(RuntimeException e) {
				e.printStackTrace();
				
			}
		});

		s.acquireUninterruptibly();
		assertEquals("Expect SolQual to be 5", 5,  solQual.get());
		
		
		
	}
}
