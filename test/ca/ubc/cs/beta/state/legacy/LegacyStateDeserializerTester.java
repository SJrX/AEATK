package ca.ubc.cs.beta.state.legacy;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.algorithmrunner.AutomaticConfiguratorFactory;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.exceptions.*;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aclib.runhistory.RunData;
import ca.ubc.cs.beta.aclib.runhistory.RunHistory;
import ca.ubc.cs.beta.aclib.seedgenerator.InstanceSeedGenerator;
import ca.ubc.cs.beta.aclib.seedgenerator.RandomInstanceSeedGenerator;
import ca.ubc.cs.beta.aclib.state.StateDeserializer;
import ca.ubc.cs.beta.aclib.state.StateFactory;
import ca.ubc.cs.beta.aclib.state.StateSerializer;
import ca.ubc.cs.beta.aclib.state.legacy.LegacyStateFactory;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.CommandLineTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.targetalgorithmevaluator.ParamEchoExecutor;
import ca.ubc.cs.beta.targetalgorithmevaluator.ParamEchoExecutorWithGibberish;
import ec.util.MersenneTwister;

public class LegacyStateDeserializerTester {

	
	private static List<ProblemInstance> instances = new ArrayList<ProblemInstance>();
	 
	private static final List<ProblemInstance> emptyInstanceList = Collections.emptyList();
	
	private static ParamConfigurationSpace configSpace = new ParamConfigurationSpace(TestHelper.getTestFile("paramFiles/simpleParam.txt"));
	
	private static AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig("foo", "bar", configSpace, false, false, 300);
	
	
	@BeforeClass
	public static void init()
	{
		for(int i=0; i < 1000; i++)
		{
			
			
			instances.add(new ProblemInstance("Instance " + i,i));
		}
	
	}
	
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
	
	@Test(expected=StateSerializationException.class)
	public void unknownInstance()
	{
		File f = TestHelper.getTestFile("stateFiles");
		StateFactory sf = new LegacyStateFactory(null,f.getAbsolutePath());	
		
		
		sf.getStateDeserializer("unknown_instance", 4, configSpace, OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME, emptyInstanceList, execConfig);
	}
	
	@Test
	public void validInstance()
	{
		File f = TestHelper.getTestFile("stateFiles");
		StateFactory sf = new LegacyStateFactory(null,f.getAbsolutePath());	
		
		startOutputCapture();
		sf.getStateDeserializer("valid", 4, configSpace, OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME, instances, execConfig);
		String output = stopOutputCapture();
		assertFalse(output.contains("Cutoff time discrepancy"));
		
		
	}
	
	@Test
	/**
	 * Tests whether MATLAB Inf, -Inf are parsed properly, as well as some NaN values
	 */
	public void validInstanceWithInfAndPermittedNaNs()
	{
		File f = TestHelper.getTestFile("stateFiles");
		StateFactory sf = new LegacyStateFactory(null,f.getAbsolutePath());	
		
		
		sf.getStateDeserializer("inf", 4, configSpace, OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME, instances, execConfig);
	}
	
	@Test
	public void validInstanceLoweredCaptime()
	{
		
		File f = TestHelper.getTestFile("stateFiles");
		StateFactory sf = new LegacyStateFactory(null,f.getAbsolutePath());	
		
		 AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig("foo", "bar", configSpace, false, false, 1);
		 startOutputCapture();
		StateDeserializer sd = sf.getStateDeserializer("valid", 4, configSpace, OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME, instances, execConfig);
		String output = stopOutputCapture();
		assertTrue(output.contains("Cutoff time discrepancy"));
		assertFalse(output.contains("marking run as TIMEOUT and Censored"));
		assertTrue(output.contains("marking run as TIMEOUT with runtime 1.0"));
		for(RunData runData : sd.getRunHistory().getAlgorithmRunData())
		{
			if(runData.getRun().getRunResult().equals(RunResult.TIMEOUT))
			{
				if(runData.isCappedRun())
				{
					assertTrue(runData.getResponseValue() < 1);
				} else
				{
					assertTrue(runData.getResponseValue() == 1);
				}
			} else
			{
				
				assertTrue(runData.getResponseValue() < 1);
			}
		}
		
	}
	
	@Test
	public void validInstanceRaisedCaptime()
	{
		File f = TestHelper.getTestFile("stateFiles");
		StateFactory sf = new LegacyStateFactory(null,f.getAbsolutePath());	
		
		 AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig("foo", "bar", configSpace, false, false, 1000);
		

		 startOutputCapture();
		StateDeserializer sd = sf.getStateDeserializer("valid", 4, configSpace, OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME, instances, execConfig);
			String output = stopOutputCapture();
			assertTrue(output.contains("Cutoff time discrepancy"));
			assertTrue(output.contains("marking run as TIMEOUT and Censored"));
			assertFalse(output.contains("marking run as TIMEOUT with runtime"));
			
		for(RunData runData : sd.getRunHistory().getAlgorithmRunData())
		{
			if(runData.getRun().getRunResult().equals(RunResult.TIMEOUT))
			{
				if(runData.isCappedRun())
				{
					assertTrue(runData.getResponseValue() < 1000);
				} else
				{
					assertTrue(runData.getResponseValue() == 1000);
				}
			} else
			{
				assertTrue(runData.getResponseValue() < 1000);
			}
		}
			
	}
	
	/**
	 * Tests that a restored Run History is roughly the same as the other Run History
	 *
	 * This test does not preserve invariants that we would expect SMAC to preserve 
	 * (such as Incumbent always has the most number of runs or that Incumbent
	 * always has the same Problem INstance Seed Pairs as any challenger.)
	 * 
	 * At the time of writing it seems to make sense that RunHistory actually doesn't
	 * care about these invariants.
	 * 
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void stateSerializationRestoration() throws DuplicateRunException
	{
		
		  
				
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt");
		ParamConfigurationSpace configSpace;
		
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		
		long seed = System.currentTimeMillis();
		//seed = 1;
		System.out.println("Seed was:" + seed);
		Random r = new MersenneTwister(seed);
		
		configSpace = new ParamConfigurationSpace(paramFile,r);
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 5000);
		List<ProblemInstance> pis = new ArrayList<ProblemInstance>();
		for(int i=0; i < 10; i++)
		{
			pis.add(new ProblemInstance("TestInstance_" + i,i));
		}
		
		
		InstanceSeedGenerator isg = new RandomInstanceSeedGenerator(pis, 1);
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(1000);
		for(int i=0; i < 200; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration();
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				ProblemInstance pi = pis.get(r.nextInt(pis.size()));
				
				ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, isg.getNextSeed(pi));
				RunConfig rc = new RunConfig(pisp, 1001, config);
				runConfigs.add(rc);
			}
		}
		
		
		AutomaticConfiguratorFactory.setMaximumNumberOfThreads(10);
		
		TargetAlgorithmEvaluator tae = new CommandLineTargetAlgorithmEvaluator( execConfig, true);
		
		RunHistory runHistory = new NewRunHistory(isg, OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME);
		
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		PrintStream ps = System.out;
		List<AlgorithmRun> runs;
		
		//=== Suppress System output to speed up test execution
		System.out.println("Suppressing System output");
		System.setOut(null);
		
		try {
			runs = tae.evaluateRun(runConfigs);
		} finally
		{
			System.setOut(ps);
			System.out.println("Restoring System Output");
		}
		for(AlgorithmRun run : runs)
		{
			runHistory.append(run);
		}
		
		File f;
		try {
			f = File.createTempFile("smac", "junitTest");
			f.delete();
			if(!f.mkdirs())
			{
				throw new IllegalStateException("Couldn't create directory");
			}
			
		} catch (IOException e) {
			throw new IllegalStateException("Couldn't create directory");
		}
		StateFactory sf = new LegacyStateFactory(f.getAbsolutePath(),f.getAbsolutePath());
		
		StateSerializer stateS = sf.getStateSerializer("deleteMe-unitTest", 10);
		
		stateS.setRunHistory(runHistory);
		
		stateS.setIncumbent(runs.get(0).getRunConfig().getParamConfiguration());
		stateS.save();
		
		StateDeserializer stateD =  sf.getStateDeserializer("deleteMe-unitTest", 10, configSpace, OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME, pis, execConfig);
		
		
		RunHistory restoredRunHistory = stateD.getRunHistory();
		
		assertEquals(runHistory.getIteration(), restoredRunHistory.getIteration());
		
		Set<ProblemInstance> instanceSet = new HashSet<ProblemInstance>();
		instances.addAll(pis);
		
		for(int i=0; i < runs.size(); i++)
		{
			//==== We check equality on each member directly
			//==== So we can easily debug cases where it's broken
			
			AlgorithmRun run = runHistory.getAlgorithmRuns().get(i);
			
			AlgorithmRun restoredRun = restoredRunHistory.getAlgorithmRuns().get(i);
			
			assertDEquals(run.getQuality(), restoredRun.getQuality(),0.01);
			assertDEquals(run.getRuntime(),restoredRun.getRuntime(),0.01);
			assertEquals(run.getResultSeed(), restoredRun.getResultSeed());
			assertEquals(run.getRunResult(),restoredRun.getRunResult());
			assertDEquals(run.getRunLength(), restoredRun.getRunLength(), 0.01);
			assertEquals(run.getAdditionalRunData(), restoredRun.getAdditionalRunData());
			assertEquals("",run.getAdditionalRunData().trim() );
			
			
			
			//assertEquals(runHistory.getAlgorithmRuns().get(i), restoredRunHistory.getAlgorithmRuns().get(i));
			
			ParamConfiguration config = run.getRunConfig().getParamConfiguration();
			
			assertEquals(runHistory.getCensoredFlagForRuns()[i],restoredRunHistory.getCensoredFlagForRuns()[i]);
			double cost1 = runHistory.getEmpiricalCost(config, instanceSet, execConfig.getAlgorithmCutoffTime());
			double cost2 = restoredRunHistory.getEmpiricalCost(config, instanceSet, execConfig.getAlgorithmCutoffTime()); 
			assertDEquals(cost1,cost2,0.1);
			
			System.out.print(".");
			
			if(i % 40 == 0) System.out.println("");
			
			
			
			
			
			
			
			
			
		}
		
		
		
		
	}
	
	
	
	
	/**
	 * Tests that a restored Run History is roughly the same as the other Run History
	 *
	 * This test does not preserve invariants that we would expect SMAC to preserve 
	 * (such as Incumbent always has the most number of runs or that Incumbent
	 * always has the same Problem INstance Seed Pairs as any challenger.)
	 * 
	 * At the time of writing it seems to make sense that RunHistory actually doesn't
	 * care about these invariants.
	 *
	 * This test differs from the last in that we also check if AdditionalRunData is saved and restored is saved and restored
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void stateSerializationRestorationWithAdditionalData() throws DuplicateRunException
	{
		
		  
				
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt");
		ParamConfigurationSpace configSpace;
		
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutorWithGibberish.class.getCanonicalName());
		
		long seed = System.currentTimeMillis();
		//seed = 1;
		
		System.out.println("Seed was:" + seed);
		Random r = new MersenneTwister(seed);
		
		configSpace = new ParamConfigurationSpace(paramFile,r);
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 5000);
		List<ProblemInstance> pis = new ArrayList<ProblemInstance>();
		for(int i=0; i < 10; i++)
		{
			pis.add(new ProblemInstance("TestInstance_" + i,i));
		}
		
		
		InstanceSeedGenerator isg = new RandomInstanceSeedGenerator(pis, 1);
		
		
		
		List<RunConfig> runConfigs = new ArrayList<RunConfig>(1000);
		for(int i=0; i < 50; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration();
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				ProblemInstance pi = pis.get(r.nextInt(pis.size()));
				
				ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, isg.getNextSeed(pi));
				RunConfig rc = new RunConfig(pisp, 1001, config);
				runConfigs.add(rc);
			}
		}
		
		
		AutomaticConfiguratorFactory.setMaximumNumberOfThreads(10);
		
		TargetAlgorithmEvaluator tae = new CommandLineTargetAlgorithmEvaluator( execConfig, true);
		
		RunHistory runHistory = new NewRunHistory(isg, OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME);
		
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		PrintStream ps = System.out;
		List<AlgorithmRun> runs;
		
		//=== Suppress System output to speed up test execution
		System.out.println("Suppressing System output");
		System.setOut(null);
		
		try {
			runs = tae.evaluateRun(runConfigs);
		} finally
		{
			System.setOut(ps);
			System.out.println("Restoring System Output");
		}
		for(AlgorithmRun run : runs)
		{
			runHistory.append(run);
		}
		
		File f;
		try {
			f = File.createTempFile("smac-additionalRunData", "junitTest");
			f.delete();
			if(!f.mkdirs())
			{
				throw new IllegalStateException("Couldn't create directory");
			}
			
		} catch (IOException e) {
			throw new IllegalStateException("Couldn't create directory");
		}
		StateFactory sf = new LegacyStateFactory(f.getAbsolutePath(),f.getAbsolutePath());
		
		StateSerializer stateS = sf.getStateSerializer("deleteMe-unitTest-gibberish", 10);
		
		stateS.setRunHistory(runHistory);
		
		stateS.setIncumbent(runs.get(0).getRunConfig().getParamConfiguration());
		stateS.save();
		
		StateDeserializer stateD =  sf.getStateDeserializer("deleteMe-unitTest-gibberish", 10, configSpace, OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME, pis, execConfig);
		
		
		RunHistory restoredRunHistory = stateD.getRunHistory();
		
		assertEquals(runHistory.getIteration(), restoredRunHistory.getIteration());
		
		Set<ProblemInstance> instanceSet = new HashSet<ProblemInstance>();
		instances.addAll(pis);
		
		for(int i=0; i < runs.size(); i++)
		{
			//==== We check equality on each member directly
			//==== So we can easily debug cases where it's broken
			
			AlgorithmRun run = runHistory.getAlgorithmRuns().get(i);
			
			AlgorithmRun restoredRun = restoredRunHistory.getAlgorithmRuns().get(i);
			
			assertDEquals(run.getQuality(), restoredRun.getQuality(),0.01);
			assertDEquals(run.getRuntime(),restoredRun.getRuntime(),0.01);
			assertEquals(run.getResultSeed(), restoredRun.getResultSeed());
			assertEquals(run.getRunResult(),restoredRun.getRunResult());
			assertDEquals(run.getRunLength(), restoredRun.getRunLength(), 0.01);
			assertEquals(run.getAdditionalRunData(), restoredRun.getAdditionalRunData());
			
			//==== Test that two runs are 
			assertTrue(!"".equals(run.getAdditionalRunData()));
			
			//assertEquals(runHistory.getAlgorithmRuns().get(i), restoredRunHistory.getAlgorithmRuns().get(i));
			
			ParamConfiguration config = run.getRunConfig().getParamConfiguration();
			
			assertEquals(runHistory.getCensoredFlagForRuns()[i],restoredRunHistory.getCensoredFlagForRuns()[i]);
			double cost1 = runHistory.getEmpiricalCost(config, instanceSet, execConfig.getAlgorithmCutoffTime());
			double cost2 = restoredRunHistory.getEmpiricalCost(config, instanceSet, execConfig.getAlgorithmCutoffTime()); 
			assertDEquals(cost1,cost2,0.1);
			
			System.out.print(".");
			
			if(i % 40 == 0) System.out.println("");		
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
