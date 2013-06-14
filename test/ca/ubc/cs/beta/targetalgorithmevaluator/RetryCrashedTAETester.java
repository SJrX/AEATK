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
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aclib.misc.random.SeedableRandomPool;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.helpers.RetryCrashedRunsTargetAlgorithmEvaluator;

public class RetryCrashedTAETester {

	
private static TargetAlgorithmEvaluator tae;
	
	private static AlgorithmExecutionConfig execConfig;
	
	private static ParamConfigurationSpace configSpace;
	
	private static final int TARGET_RUNS_IN_LOOPS = 50;
	
	private static final SeedableRandomPool pool = new SeedableRandomPool(System.currentTimeMillis());
	
	

	@BeforeClass
	public static void beforeClass()
	{
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt");
		configSpace = new ParamConfigurationSpace(paramFile);
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(FailingEchoExecutor.class.getCanonicalName());
		
		
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
	}
	
	@Before
	public void beforeTest()
	{
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(execConfig);
	}
	
	
	/**
	 * Tests that the echo executor actually fails sometimes
	 */
	@Test
	public void testFailingEchoExecutor()
	{
		Random r =  pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
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
		tae = new RetryCrashedRunsTargetAlgorithmEvaluator(  0, tae);
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
		
		int crashedRuns = 0;
		for(AlgorithmRun run : runs)
		{
			if(run.getRunResult().equals(RunResult.CRASHED))
			{
				crashedRuns++;
			} else
			{
				ParamConfiguration config  = run.getRunConfig().getParamConfiguration();
				assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
				assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
				assertDEquals(config.get("quality"), run.getQuality(), 0.1);
				assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
				assertEquals(config.get("solved"), run.getRunResult().name());
			}
		}
		
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
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				config.put("runlength", String.valueOf(i));
				RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		//=== My probability is a bit rusty but the expected 
		//number of runs we need to retry is 1/2, so after the number / log 2, we should have 0,so I Will try 5 more times after that
		//theoretically this test should only fail once every 1000 times
		tae = new RetryCrashedRunsTargetAlgorithmEvaluator(  (int) (Math.log(TARGET_RUNS_IN_LOOPS)/Math.log(2)) + 10, tae);
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
		
		assertEquals(runs.size(), tae.getRunCount());

		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			AlgorithmRun run = runs.get(i);
			
			//This tests that the order is correct
			assertDEquals(run.getRunConfig().getParamConfiguration().get("runlength"),i, 0.1);
			
			if(run.getRunResult().equals(RunResult.CRASHED))
			{
				fail("Expected zero crashes but run " + i + " crashed ");
			} else
			{
				ParamConfiguration config  = run.getRunConfig().getParamConfiguration();
				assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
				assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
				assertDEquals(config.get("quality"), run.getQuality(), 0.1);
				assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
				assertEquals(config.get("solved"), run.getRunResult().name());
			}
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
}
