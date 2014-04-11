package ca.ubc.cs.beta.objectives;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aeatk.objectives.RunObjective;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.random.RandomResponseTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.random.RandomResponseTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.targetalgorithmevaluator.ParamEchoExecutor;

public class RunObjectiveTester {

private static TargetAlgorithmEvaluator tae;
	
	private static AlgorithmExecutionConfiguration execConfig;
	
	private static ParameterConfigurationSpace configSpace;
	
	private static double kappaMax = 500;
	
	private static final SeedableRandomPool pool = new SeedableRandomPool(System.currentTimeMillis());
	@AfterClass
	public static void afterClass()
	{
		pool.logUsage();
	}
	
	@BeforeClass
	public static void beforeClass()
	{
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt");
		configSpace = new ParameterConfigurationSpace(paramFile);
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		
		
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, kappaMax);
		
	}
	
	
	@Before
	public void beforeTest()
	{
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
	
	}
	
	@Test
	public void testTimeoutReportedAsKappaMax()
	{
		
		
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(1);
		
		for(int i=0; i < 10; i++)
		{
			double runtime = Math.max(0,(double) Math.random() * kappaMax - 1.0);
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved","TIMEOUT");
			config.put("runtime", String.valueOf(runtime));
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), kappaMax, config, execConfig);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(AlgorithmRunConfiguration rc : runConfigs)
		{
				AlgorithmRunResult run = tae.evaluateRun(rc).get(0);
				assertDEquals(RunObjective.RUNTIME.getObjective(run),kappaMax,0.1);
				assertDEquals(Double.valueOf(run.getAlgorithmRunConfiguration().getParameterConfiguration().get("runtime")), run.getRuntime(), 0.1);
		}
	}
	
	@Test
	public void testCappedRunsReportedAsNonKappaMax()
	{
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(1);
		
		for(int i=0; i < 10; i++)
		{
			double runtime = Math.max(0,(double) Math.random() * kappaMax - 1.0);
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved","TIMEOUT");
			config.put("runtime", String.valueOf(runtime));
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), runtime, config,  execConfig);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(AlgorithmRunConfiguration rc : runConfigs)
		{
				AlgorithmRunResult run = tae.evaluateRun(rc).get(0);
				assertDEquals(RunObjective.RUNTIME.getObjective(run),run.getRuntime(),0.1);
				assertDEquals(Double.valueOf(run.getAlgorithmRunConfiguration().getParameterConfiguration().get("runtime")), run.getRuntime(), 0.1);
		}
	}
	
	
	
	@Test
	public void testCrashReportedAsKappaMax()
	{
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(1);
		
		for(int i=0; i < 10; i++)
		{
			double runtime = Math.max(0,(double) Math.random() * kappaMax - 1.0);
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved","CRASHED");
			config.put("runtime", String.valueOf(runtime));
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), kappaMax, config, execConfig);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(AlgorithmRunConfiguration rc : runConfigs)
		{
				AlgorithmRunResult run = tae.evaluateRun(rc).get(0);
				assertDEquals(RunObjective.RUNTIME.getObjective(run),kappaMax,0.1);
				assertDEquals(Double.valueOf(run.getAlgorithmRunConfiguration().getParameterConfiguration().get("runtime")), run.getRuntime(), 0.1);
		}
	}
	

	@Test
	public void testNonKappaMaxRuns()
	{
		
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(1);
		
		for(int i=0; i < 10; i++)
		{
			double runtime = Math.max(0,(double) Math.random() * kappaMax - 1.0);
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved","SAT");
			config.put("runtime", String.valueOf(runtime));
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), kappaMax,config, execConfig);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(AlgorithmRunConfiguration rc : runConfigs)
		{
				AlgorithmRunResult run = tae.evaluateRun(rc).get(0);
				assertDEquals(RunObjective.RUNTIME.getObjective(run),run.getRuntime(),0.1);
				assertDEquals(Double.valueOf(run.getAlgorithmRunConfiguration().getParameterConfiguration().get("runtime")), run.getRuntime(), 0.1);
		}
	}
	
	@Test
	public void testCappedTimeoutReportedAsTimeoutValue()
	{

		/**
		 * See Task 1567
		 */
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(1);
		double capTimeRequest = 5;
		
		for(int i=0; i < 10; i++)
		{
			
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved","TIMEOUT");
			config.put("runtime", "0.1");
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))),capTimeRequest, config, execConfig);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(AlgorithmRunConfiguration rc : runConfigs)
		{
				AlgorithmRunResult run = tae.evaluateRun(rc).get(0);
				assertDEquals(RunObjective.RUNTIME.getObjective(run),capTimeRequest,0.1);
				assertDEquals(Double.valueOf(run.getAlgorithmRunConfiguration().getParameterConfiguration().get("runtime")), run.getRuntime(), 0.1);
		}
	}

	@Test
	public void testRunObjectiveWithDynamicCapping()
	{
		RandomResponseTargetAlgorithmEvaluatorFactory rfact = new RandomResponseTargetAlgorithmEvaluatorFactory();

		
		RandomResponseTargetAlgorithmEvaluatorOptions o = rfact.getOptionObject();
		o.simulateDelay = true;
		o.minResponse = 5;
		o.maxResponse = 10;
		
		TargetAlgorithmEvaluator tae = rfact.getTargetAlgorithmEvaluator( o);

		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), ParameterConfigurationSpace.getSingletonConfigurationSpace(), false, false, kappaMax);
		
		AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("pi"), 1), 20, ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(), execConfig);
		
		
		List<AlgorithmRunResult> runs = tae.evaluateRun(Collections.singletonList(rc), new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) {
				for(AlgorithmRunResult run : runs)
				{
					run.kill();
				}
			}
			
		});
		
		for(AlgorithmRunResult run : runs)
		{
			assertEquals("Expect Runtime objective to be the same for dynamically capped runs ", run.getRuntime(), RunObjective.RUNTIME.getObjective(run) , 0.01);
			
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
		if(d1 - d2 > delta) throw new AssertionError("Expected "  + (d1 + "-"+ d2)+ " < " + delta);
		if(d2 - d1 > delta) throw new AssertionError("Expected "  + (d1 + "-"+ d2)+ " < " + delta);
		
	}
		
	
}
