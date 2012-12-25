package ca.ubc.cs.beta.objectives;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.random.SeedableRandomSingleton;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.CommandLineTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.targetalgorithmevaluator.ParamEchoExecutor;

public class RunObjectiveTester {

private static TargetAlgorithmEvaluator tae;
	
	private static AlgorithmExecutionConfig execConfig;
	
	private static ParamConfigurationSpace configSpace;
	
	private static double kappaMax = 500;
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
		
		
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, kappaMax);
		
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
	
	@Test
	public void testTimeoutReportedAsKappaMax()
	{
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(1);
		
		for(int i=0; i < 10; i++)
		{
			double runtime = Math.max(0,(double) Math.random() * kappaMax - 1.0);
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved","TIMEOUT");
			config.put("runtime", String.valueOf(runtime));
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(RunConfig rc : runConfigs)
		{
				AlgorithmRun run = tae.evaluateRun(rc).get(0);
				assertDEquals(RunObjective.RUNTIME.getObjective(run),kappaMax,0.1);
				assertDEquals(Double.valueOf(run.getRunConfig().getParamConfiguration().get("runtime")), run.getRuntime(), 0.1);
		}
	}
	
	@Test
	public void testCappedRunsReportedAsNonKappaMax()
	{
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(1);
		
		for(int i=0; i < 10; i++)
		{
			double runtime = Math.max(0,(double) Math.random() * kappaMax - 1.0);
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved","TIMEOUT");
			config.put("runtime", String.valueOf(runtime));
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, true);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(RunConfig rc : runConfigs)
		{
				AlgorithmRun run = tae.evaluateRun(rc).get(0);
				assertDEquals(RunObjective.RUNTIME.getObjective(run),run.getRuntime(),0.1);
				assertDEquals(Double.valueOf(run.getRunConfig().getParamConfiguration().get("runtime")), run.getRuntime(), 0.1);
		}
	}
	
	
	
	@Test
	public void testCrashReportedAsKappaMax()
	{
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(1);
		
		for(int i=0; i < 10; i++)
		{
			double runtime = Math.max(0,(double) Math.random() * kappaMax - 1.0);
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved","CRASHED");
			config.put("runtime", String.valueOf(runtime));
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(RunConfig rc : runConfigs)
		{
				AlgorithmRun run = tae.evaluateRun(rc).get(0);
				assertDEquals(RunObjective.RUNTIME.getObjective(run),kappaMax,0.1);
				assertDEquals(Double.valueOf(run.getRunConfig().getParamConfiguration().get("runtime")), run.getRuntime(), 0.1);
		}
	}
	

	@Test
	public void testNonKappaMaxRuns()
	{
		
		configSpace.setPRNG(r);
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(1);
		
		for(int i=0; i < 10; i++)
		{
			double runtime = Math.max(0,(double) Math.random() * kappaMax - 1.0);
			ParamConfiguration config = configSpace.getRandomConfiguration();
			config.put("solved","SAT");
			config.put("runtime", String.valueOf(runtime));
			RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(RunConfig rc : runConfigs)
		{
				AlgorithmRun run = tae.evaluateRun(rc).get(0);
				assertDEquals(RunObjective.RUNTIME.getObjective(run),run.getRuntime(),0.1);
				assertDEquals(Double.valueOf(run.getRunConfig().getParamConfiguration().get("runtime")), run.getRuntime(), 0.1);
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
