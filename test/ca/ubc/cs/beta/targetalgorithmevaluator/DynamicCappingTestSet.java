package ca.ubc.cs.beta.targetalgorithmevaluator;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableAlgorithmRun;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.random.SeedableRandomPool;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.WaitableTAECallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.helpers.WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator;

@SuppressWarnings("unused")
public class DynamicCappingTestSet {

	
	private static TargetAlgorithmEvaluator tae;
	
	private static AlgorithmExecutionConfig execConfig;
	
	private static ParamConfigurationSpace configSpace;
	
	private static final int TARGET_RUNS_IN_LOOPS = 10;
	
	SeedableRandomPool pool = new SeedableRandomPool(System.currentTimeMillis());
	
	
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
		
		this.r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		
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

	

	/**
	 * Tests whether warnings are generated for Algorithms exceeding there runtime
	 */
	@Test
	public void testDynamicAdaptiveCappingSingleRun()
	{
		
	
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig);
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		assertTrue(tae.areRunsObservable());
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(1);
		for(int i=0; i < 1; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
			config.put("runtime", "100");
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 3000, config);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		//StringWriter sw = new StringWriter();
		//ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		//PrintStream out = System.out;
		//System.setOut(new PrintStream(bout));
		final AtomicBoolean evaluateDone = new AtomicBoolean(false);
		final AtomicBoolean failure = new AtomicBoolean(false);
		TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
		{
			
			@Override
			public void currentStatus(List<? extends KillableAlgorithmRun> runs) {
				
				if(evaluateDone.get())
				{
					failure.set(true);
				}
				double runtimeSum = 0.0;
				double walltimeSum = 0.0;
				for(AlgorithmRun run : runs)
				{
					runtimeSum += run.getRuntime();
					walltimeSum += run.getWallclockExecutionTime();
				}
				
				System.out.println(runtimeSum + ","+ walltimeSum);
				if(runtimeSum > 3)
				{
					System.out.println("Trying to kill");
					for(KillableAlgorithmRun run : runs)
					{
						run.kill();
					}
				}
				
				//System.out.println("CALLBACK");
			}
			
		};
		
		long startTime  = System.currentTimeMillis();
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs,obs);
		evaluateDone.set(true);
		long endTime = System.currentTimeMillis();
		//System.setOut(out);
		//System.out.println(bout.toString());
		
		for(AlgorithmRun run : runs)
		{
			System.out.println(run.getResultLine());
			
			ParamConfiguration config  = run.getRunConfig().getParamConfiguration();
			
			if(run.getRunResult().isSuccessfulAndCensored())
			{
				continue;
			}
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunResult().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());
			

		}
		
		tae.notifyShutdown();
		assertFalse("Callback fired after evaluateRun was done ", failure.get());
		
		assertTrue("Should have taken less than five seconds to run, it took " + (endTime - startTime)/1000.0 + " seconds", (endTime - startTime) < (long) 6000);
		
	}
	
	
	
	

	/**
	 * Tests whether warnings are generated for Algorithms exceeding there runtime
	 */
	@Test
	public void testDynamicAdaptiveCappingMultiRunSingleCore()
	{
		
	
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig);	
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		assertTrue(tae.areRunsObservable());
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(10);
		for(int i=0; i < 10; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
			config.put("runtime", ""+(i+1));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 3000, config);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		//StringWriter sw = new StringWriter();
		//ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		//PrintStream out = System.out;
		//System.setOut(new PrintStream(bout));
		
		TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
		{
			
			@Override
			public void currentStatus(List<? extends KillableAlgorithmRun> runs) {
				
				double runtimeSum = 0.0; 
				for(AlgorithmRun run : runs)
				{
					runtimeSum += run.getRuntime();
				}
				
				//System.out.println(runtimeSum);
				if(runtimeSum > 4)
				{
					System.out.println("Issuing kill order on " + runtimeSum);
					for(KillableAlgorithmRun run : runs)
					{
						run.kill();
					}
				}
			}
			
		};
		
		long startTime  = System.currentTimeMillis();
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs,obs);
		long endTime = System.currentTimeMillis();
		//System.setOut(out);
		//System.out.println(bout.toString());
		
		for(AlgorithmRun run : runs)
		{
			System.out.println(run.getResultLine());
			
			ParamConfiguration config  = run.getRunConfig().getParamConfiguration();
			
			if(run.getRunResult().isSuccessfulAndCensored())
			{
				continue;
			}
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunResult().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());
			

		}
		
		tae.notifyShutdown();
		
		assertTrue("Should have taken less than five seconds to run, it took " + (endTime - startTime)/1000.0 + " seconds", (endTime - startTime) < (long) 6000);
	}
	


	@Test
	public void testDynamicAdaptiveCappingMultiRunMultiCore()
	{
		
	
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		
		CommandLineTargetAlgorithmEvaluatorOptions opt = fact.getOptionObject();
		opt.cores = 4;
		tae = fact.getTargetAlgorithmEvaluator(execConfig, opt);
		
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		assertTrue(tae.areRunsObservable());
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(10);
		for(int i=0; i < 10; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
			
			config.put("runtime", ""+(i+1));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 3000, config);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		//StringWriter sw = new StringWriter();
		//ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		//PrintStream out = System.out;
		//System.setOut(new PrintStream(bout));
		
		TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
		{
			
			@Override
			public void currentStatus(List<? extends KillableAlgorithmRun> runs) {
				
				//System.out.println(runs.get(0).getWallclockExecutionTime());
				double runtimeSum = 0.0; 
				double walltimeSum = 0.0;
				for(AlgorithmRun run : runs)
				{
					runtimeSum += run.getRuntime();
					walltimeSum += run.getWallclockExecutionTime();
				}
				
				//System.out.println(runtimeSum);
			
				
				if(runtimeSum > 8)
				{
					for(KillableAlgorithmRun run : runs)
					{
				//		System.out.println(run.toString());
					}
					for(KillableAlgorithmRun run : runs)
					{
						run.kill();
					}
				}
				
			}
			
		};
		
		long startTime  = System.currentTimeMillis();
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs,obs);
		long endTime = System.currentTimeMillis();
		//System.setOut(out);
		//System.out.println(bout.toString());
		
		long runtimeSum = 0;
		long wallclockTime = 0;
		for(AlgorithmRun run : runs)
		{
			System.out.println("Result: " + run);
			
			ParamConfiguration config  = run.getRunConfig().getParamConfiguration();
			
			runtimeSum+= run.getRuntime();
			wallclockTime += run.getWallclockExecutionTime();
			
			if(run.getRunResult().isSuccessfulAndCensored())
			{
				
				assertEquals(run.getRunResult(), RunResult.KILLED);
				continue;
			}
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunResult().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());
			

		}
		
		assertTrue("Runtime should be greater than 5 seconds", runtimeSum > 5);
		
		assertTrue("Wallclocktime should be greater than 5 seconds", wallclockTime > 5);
		
		
		
		tae.notifyShutdown();
		
		assertTrue("Should have taken less than five seconds to run, it took " + (endTime - startTime)/1000.0 + " seconds", (endTime - startTime) < (long) 6000);
	}
	
	
	

	/**
	 * Tests whether the Observer is fired after the evaluateRuns() method completes
	 * See Task #1575
	 */
	@Test
	public void testCallbackOnlyBeforeDone()
	{
		
	
		for(int j=0; j < 5; j++)
		{
			StringBuilder b = new StringBuilder();
			b.append("java -cp ");
			b.append(System.getProperty("java.class.path"));
			b.append(" ");
			b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
			execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
			
			tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig, 50);	
			tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
			
			assertTrue(tae.areRunsObservable());
			
			
			List<RunConfig> runConfigs = new ArrayList<RunConfig>(1);
			for(int i=0; i < 1; i++)
			{
				ParamConfiguration config = configSpace.getRandomConfiguration(r);
				config.put("runtime", "1");
				if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
				{
					//Only want good configurations
					i--;
					continue;
				} else
				{
					RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 3000, config);
					runConfigs.add(rc);
				}
			}
			
			System.out.println("Performing " + runConfigs.size() + " runs");
			
			
			final AtomicBoolean evaluateDone = new AtomicBoolean(false);
			final AtomicBoolean failure = new AtomicBoolean(false);
			TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
			{
				
				@Override
				public void currentStatus(List<? extends KillableAlgorithmRun> runs) {
					
					if(evaluateDone.get())
					{
						failure.set(true);
						System.out.println("Failed");
					}
					
					//System.out.println("Callback: " + runs.get(0).getRuntime());
				}
				
			};
			
			long startTime  = System.currentTimeMillis();
			List<AlgorithmRun> runs = tae.evaluateRun(runConfigs,obs);
			evaluateDone.set(true);
			System.out.println("DONE");
			System.out.flush();
			long endTime = System.currentTimeMillis();
			//System.setOut(out);
			//System.out.println(bout.toString());
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				Thread.currentThread();
			}
			
			tae.notifyShutdown();
			assertFalse("Callback fired after evaluateRun was done ", failure.get());

		}
		
		
	}
	

	/**
	 * Tests whether the Observer is fired after the onSuccess method is.
	 * See Task #1575
	 */
	@Test
	public void testAsyncCallbackOnlyBeforeDone()
	{
		
		//Try 5 times as this is testing a race condition.
		for(int j=0; j < 5; j++)
		{
			StringBuilder b = new StringBuilder();
			b.append("java -cp ");
			b.append(System.getProperty("java.class.path"));
			b.append(" ");
			b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
			execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
			CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
			
			CommandLineTargetAlgorithmEvaluatorOptions opt = fact.getOptionObject();
			opt.cores = 1;
			opt.observerFrequency = 50;
			tae = fact.getTargetAlgorithmEvaluator(execConfig, opt);
			
				
			tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
			
			assertTrue(tae.areRunsObservable());
			
			
			List<RunConfig> runConfigs = new ArrayList<RunConfig>(1);
			for(int i=0; i < 1; i++)
			{
				ParamConfiguration config = configSpace.getRandomConfiguration(r);
				config.put("runtime", "1");
				if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
				{
					//Only want good configurations
					i--;
					continue;
				} else
				{
					RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 3000, config);
					runConfigs.add(rc);
				}
			}
			
			System.out.println("Performing " + runConfigs.size() + " runs");
			
			
			final AtomicBoolean evaluateDone = new AtomicBoolean(false);
			final AtomicBoolean failure = new AtomicBoolean(false);
			TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
			{
				
				@Override
				public void currentStatus(List<? extends KillableAlgorithmRun> runs) {
					
					if(evaluateDone.get())
					{
						failure.set(true);
						System.out.println("Failed");
					}
					
					//System.out.println("Callback: " + runs.get(0).getRuntime());
				}
				
			};
			
			long startTime  = System.currentTimeMillis();
			
			
			
			
			TargetAlgorithmEvaluatorCallback asyncCallback = new TargetAlgorithmEvaluatorCallback()
			{

				@Override
				public void onSuccess(List<AlgorithmRun> runs) {
					evaluateDone.set(true);
				}

				@Override
				public void onFailure(RuntimeException t) {
					t.printStackTrace();
					
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			};
			
			WaitableTAECallback taeCallback = new WaitableTAECallback(asyncCallback);
			
			tae.evaluateRunsAsync(runConfigs,taeCallback, obs);
			
			taeCallback.waitForCompletion();
						
			tae.notifyShutdown();
			assertFalse("Callback fired after evaluateRun was done ", failure.get());

		}
		
		
	}
	
	
	
	
	
}
