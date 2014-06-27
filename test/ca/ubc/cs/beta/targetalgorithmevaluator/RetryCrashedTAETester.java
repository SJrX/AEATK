package ca.ubc.cs.beta.targetalgorithmevaluator;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.functionality.OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.helpers.RetryCrashedRunsTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.resource.BoundedTargetAlgorithmEvaluator;

public class RetryCrashedTAETester {

	
	private static TargetAlgorithmEvaluator tae;
	
	private static AlgorithmExecutionConfiguration execConfig;
	
	private static ParameterConfigurationSpace configSpace;
	
	private static final int TARGET_RUNS_IN_LOOPS = 50;
	
	private static final SeedableRandomPool pool = new SeedableRandomPool(System.currentTimeMillis());
	
	

	@BeforeClass
	public static void beforeClass()
	{
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt");
		configSpace = new ParameterConfigurationSpace(paramFile);
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(FailingEchoExecutor.class.getCanonicalName());
		
		
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
	}
	
	@Before
	public void beforeTest()
	{
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
	}
	
	
	/**
	 * Tests that the echo executor actually fails sometimes
	 */
	@Test
	public void testFailingEchoExecutor()
	{
		Random r =  pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		tae = new RetryCrashedRunsTargetAlgorithmEvaluatorDecorator(  0, tae);
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		int crashedRuns = 0;
		for(AlgorithmRunResult run : runs)
		{
			if(run.getRunStatus().equals(RunStatus.CRASHED))
			{
				crashedRuns++;
			} else
			{
				ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
				assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
				assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
				assertDEquals(config.get("quality"), run.getQuality(), 0.1);
				assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
				assertEquals(config.get("solved"), run.getRunStatus().name());
			}
		}
		
		tae.notifyShutdown();
		if(TARGET_RUNS_IN_LOOPS / 3 > crashedRuns)
		{
			fail("Expected atleast " +  TARGET_RUNS_IN_LOOPS / 3 + " crashes " + " only got " + crashedRuns);
		} else
		{
			System.out.println("Crashed Runs were " + crashedRuns);
		}
	}
		

	
	
	/**
	 * Tests that the RetryCrashedRunsTargetAlgorithmEvaluator behaves at it should
	 */
	@Test
	public void testRetryCrashedRunsTargetAlgorithmEvaluator()
	{
		
		Random r =  pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		final List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				config.put("runlength", String.valueOf(i));
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		final AtomicReference<RuntimeException> failureException = new AtomicReference<RuntimeException>();
		TargetAlgorithmEvaluatorRunObserver taeObserver = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) 
			{
				if(runs.size() != runConfigs.size())
				{
					failureException.compareAndSet(null, new IllegalStateException("Expected that runs.size() == runConfigs.size() but got: " +  runs.size() + " vs. " + runConfigs.size()));
				}
				
				for(AlgorithmRunResult runResult : runs)
				{
					if(runResult.getRunStatus().equals(RunStatus.CRASHED))
					{
						failureException.compareAndSet(null, new IllegalStateException("Saw a CRASHED run unexpectedly in: " + runResult));
					}
				}
				
			}
			
		};
		
		
		//=== My probability is a bit rusty but the expected 
		//number of runs we need to retry is 1/2, so after the number / log 2, we should have 0,so I Will try 5 more times after that
		//theoretically this test should only fail once every 1000 times
		tae = new RetryCrashedRunsTargetAlgorithmEvaluatorDecorator(  (int) (Math.log(TARGET_RUNS_IN_LOOPS)/Math.log(2)) + 10, tae);
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs, taeObserver);
		
		tae.notifyShutdown();
		assertEquals(runs.size(), tae.getRunCount());

		if(failureException.get() != null)
		{
			throw failureException.get();
		}
			
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			AlgorithmRunResult run = runs.get(i);
			
			//This tests that the order is correct
			assertDEquals(run.getAlgorithmRunConfiguration().getParameterConfiguration().get("runlength"),i, 0.1);
			
			if(run.getRunStatus().equals(RunStatus.CRASHED))
			{
				fail("Expected zero crashes but run " + i + " crashed ");
			} else
			{
				ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
				assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
				assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
				assertDEquals(config.get("quality"), run.getQuality(), 0.1);
				assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
				assertEquals(config.get("solved"), run.getRunStatus().name());
			}
		}
		
		
		
	}
	
	/**
	 * Tests that the RetryCrashedRunsTargetAlgorithmEvaluator behaves at it should asynchronously
	 */
	@Test
	public void testRetryCrashedRunsTargetAlgorithmEvaluatorAsync()
	{
		
		Random r =  pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		final List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				config.put("runlength", String.valueOf(i));
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		//=== My probability is a bit rusty but the expected 
		//number of runs we need to retry is 1/2, so after the number / log 2, we should have 0,so I Will try 5 more times after that
		//theoretically this test should only fail once every 1000 times
		tae = new RetryCrashedRunsTargetAlgorithmEvaluatorDecorator(  (int) (Math.log(TARGET_RUNS_IN_LOOPS)/Math.log(2)) + 10, tae);
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		final AtomicReference<RuntimeException> failureException = new AtomicReference<RuntimeException>();
		TargetAlgorithmEvaluatorRunObserver taeObserver = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) 
			{
				if(runs.size() != runConfigs.size())
				{
					failureException.compareAndSet(null, new IllegalStateException("Expected that runs.size() == runConfigs.size() but got: " +  runs.size() + " vs. " + runConfigs.size()));
				}
				
				for(AlgorithmRunResult runResult : runs)
				{
					if(runResult.getRunStatus().equals(RunStatus.CRASHED))
					{
						failureException.compareAndSet(null, new IllegalStateException("Saw a CRASHED run unexpectedly in: " + runResult));
					}
				}
				
			}
			
		};
		
		
		TargetAlgorithmEvaluatorCallback taeCallback = new TargetAlgorithmEvaluatorCallback()
		{

			@Override
			public void onSuccess(List<AlgorithmRunResult> runs) {
			
				try 
				{
					assertEquals(runs.size(), tae.getRunCount());

					for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
					{
						AlgorithmRunResult run = runs.get(i);
						
						//This tests that the order is correct
						assertDEquals(run.getAlgorithmRunConfiguration().getParameterConfiguration().get("runlength"),i, 0.1);
						
						if(run.getRunStatus().equals(RunStatus.CRASHED))
						{
							fail("Expected zero crashes but run " + i + " crashed ");
						} else
						{
							ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
							assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
							assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
							assertDEquals(config.get("quality"), run.getQuality(), 0.1);
							assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
							assertEquals(config.get("solved"), run.getRunStatus().name());
						}
					}
					
				} catch(RuntimeException e)
				{
					failureException.compareAndSet(null, e);
				}
			}

			@Override
			public void onFailure(RuntimeException e) {
				failureException.compareAndSet(null, e);
			}
			
		};
		
		tae.evaluateRunsAsync(runConfigs, taeCallback, taeObserver);
		
		tae.waitForOutstandingEvaluations();
		tae.notifyShutdown();
		if(failureException.get() != null)
		{
			throw failureException.get();
		}
		
		
		
		
	}
	
	/**
	 * Tests that the RetryCrashedRunsTargetAlgorithmEvaluator behaves at it should asynchronously
	 */
	@Test
	public void testRetryCrashedRunsTargetAlgorithmEvaluatorAsyncDoSeeCrashes()
	{
		
		Random r =  pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		final List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				config.put("runlength", String.valueOf(i));
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		//In 50 runs 
		tae = new RetryCrashedRunsTargetAlgorithmEvaluatorDecorator(  2, tae);
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		final AtomicReference<RuntimeException> failureException = new AtomicReference<RuntimeException>();
		final AtomicInteger observerFailuresSeen = new AtomicInteger(0);
		final AtomicInteger callbackFailuresSeen = new AtomicInteger(0);
		TargetAlgorithmEvaluatorRunObserver taeObserver = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) 
			{
				if(runs.size() != runConfigs.size())
				{
					failureException.compareAndSet(null, new IllegalStateException("Expected that runs.size() == runConfigs.size() but got: " +  runs.size() + " vs. " + runConfigs.size()));
				}
				
				for(AlgorithmRunResult runResult : runs)
				{
					if(runResult.getRunStatus().equals(RunStatus.CRASHED))
					{
						observerFailuresSeen.incrementAndGet();
					}
				}
				
			}
			
		};
		
		
		TargetAlgorithmEvaluatorCallback taeCallback = new TargetAlgorithmEvaluatorCallback()
		{

			@Override
			public void onSuccess(List<AlgorithmRunResult> runs) {
			
				try 
				{
					assertEquals(runs.size(), tae.getRunCount());

					for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
					{
						AlgorithmRunResult run = runs.get(i);
						
						//This tests that the order is correct
						assertDEquals(run.getAlgorithmRunConfiguration().getParameterConfiguration().get("runlength"),i, 0.1);
						
						if(run.getRunStatus().equals(RunStatus.CRASHED))
						{
							callbackFailuresSeen.incrementAndGet();
						} else
						{
							ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
							assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
							assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
							assertDEquals(config.get("quality"), run.getQuality(), 0.1);
							assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
							assertEquals(config.get("solved"), run.getRunStatus().name());
						}
					}
					
				} catch(RuntimeException e)
				{
					failureException.compareAndSet(null, e);
				}
			}

			@Override
			public void onFailure(RuntimeException e) {
				failureException.compareAndSet(null, e);
			}
			
		};
		
		tae.evaluateRunsAsync(runConfigs, taeCallback, taeObserver);
		
		tae.waitForOutstandingEvaluations();
		tae.notifyShutdown();
		if(failureException.get() != null)
		{
			throw failureException.get();
		}
		
		assertNotEquals("Expected that we would have seen atleast one crash via the observer",0, observerFailuresSeen.get());
		assertNotEquals("Expected that we would have seen atleast one crash via the callback",0, callbackFailuresSeen.get());
		
		
		
		
		
		
	}
	
	
	/**
	 * Tests that the RetryCrashedRunsTargetAlgorithmEvaluator behaves at it should asynchronously
	 */
	@Test
	public void testRetryCrashedRunsTargetAlgorithmEvaluatorAsyncDoSeeKilled()
	{
		
		Random r =  pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		final List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				config.put("runlength", String.valueOf(i));
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		tae = new BoundedTargetAlgorithmEvaluator(tae, 2);
		
		//=== My probability is a bit rusty but the expected 
		//number of runs we need to retry is 1/2, so after the number / log 2, we should have 0,so I Will try 5 more times after that
		//theoretically this test should only fail once every 1000 times
		tae = new RetryCrashedRunsTargetAlgorithmEvaluatorDecorator(  (int) (Math.log(TARGET_RUNS_IN_LOOPS)/Math.log(2)) + 10, tae);
		

		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		final AtomicReference<RuntimeException> failureException = new AtomicReference<RuntimeException>();
		final AtomicInteger observerKillsSeen = new AtomicInteger(0);
		final AtomicInteger callbackKillsSeen = new AtomicInteger(0);
		TargetAlgorithmEvaluatorRunObserver taeObserver = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) 
			{
				if(runs.size() != runConfigs.size())
				{
					failureException.compareAndSet(null, new IllegalStateException("Expected that runs.size() == runConfigs.size() but got: " +  runs.size() + " vs. " + runConfigs.size()));
				}
				
				boolean sawNonCrash = false;
				for(AlgorithmRunResult runResult : runs)
				{
					if(!runResult.getRunStatus().equals(RunStatus.CRASHED) && runResult.isRunCompleted())
					{
						//System.out.println("Non crash:" + runResult); 
						sawNonCrash = true;
						
						
					}
					
					if(runResult.getRunStatus().equals(RunStatus.KILLED))
					{
						observerKillsSeen.incrementAndGet();
					}
				}
				
				if(sawNonCrash)
				{
					for(AlgorithmRunResult run : runs)
					{
						run.kill();
					}
				}
				
			}
			
		};
		
		
		TargetAlgorithmEvaluatorCallback taeCallback = new TargetAlgorithmEvaluatorCallback()
		{

			@Override
			public void onSuccess(List<AlgorithmRunResult> runs) {
			
				try 
				{
					assertEquals(runs.size(), tae.getRunCount());

					for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
					{
						AlgorithmRunResult run = runs.get(i);
						
						//This tests that the order is correct
						assertDEquals(run.getAlgorithmRunConfiguration().getParameterConfiguration().get("runlength"),i, 0.1);
						
						if(run.getRunStatus().equals(RunStatus.KILLED))
						{
							callbackKillsSeen.incrementAndGet();
						} else
						{
							/*
							ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
							assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
							assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
							assertDEquals(config.get("quality"), run.getQuality(), 0.1);
							assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
							assertEquals(config.get("solved"), run.getRunStatus().name());
							*/
						}
						System.out.println(run);
					}
					
					
				} catch(RuntimeException e)
				{
					failureException.compareAndSet(null, e);
				}
			}

			@Override
			public void onFailure(RuntimeException e) {
				failureException.compareAndSet(null, e);
			}
			
		};
		
		tae.evaluateRunsAsync(runConfigs, taeCallback, taeObserver);
		
		tae.waitForOutstandingEvaluations();
		if(failureException.get() != null)
		{
			throw failureException.get();
		}
		
		tae.notifyShutdown();
		assertNotEquals("Expected that we would have seen atleast one kill via the observer",0, observerKillsSeen.get());
		assertNotEquals("Expected that we would have seen atleast one kill via the callback",0, callbackKillsSeen.get());
		
		
		
		
		
		
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
}
