package ca.ubc.cs.beta.state.legacy;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.ExistingAlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.exceptions.*;
import ca.ubc.cs.beta.aeatk.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aeatk.objectives.OverallObjective;
import ca.ubc.cs.beta.aeatk.objectives.RunObjective;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParamFileHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.probleminstance.seedgenerator.InstanceSeedGenerator;
import ca.ubc.cs.beta.aeatk.probleminstance.seedgenerator.RandomInstanceSeedGenerator;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;
import ca.ubc.cs.beta.aeatk.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.RunData;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistoryHelper;
import ca.ubc.cs.beta.aeatk.state.StateDeserializer;
import ca.ubc.cs.beta.aeatk.state.StateFactory;
import ca.ubc.cs.beta.aeatk.state.StateSerializer;
import ca.ubc.cs.beta.aeatk.state.legacy.LegacyStateFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.targetalgorithmevaluator.EchoTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.targetalgorithmevaluator.ParamEchoExecutor;
import ca.ubc.cs.beta.targetalgorithmevaluator.ParamEchoExecutorWithGibberish;
import ec.util.MersenneTwister;

public class LegacyStateDeserializerTester {

	
	private static List<ProblemInstance> instances = new ArrayList<ProblemInstance>();
	 
	private static final List<ProblemInstance> emptyInstanceList = Collections.emptyList();
	
	private static ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(TestHelper.getTestFile("paramFiles/simpleParam.txt"));
	
	private static AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("foo", "bar", configSpace, false, false, 300);
	
	private static SeedableRandomPool pool = new SeedableRandomPool(System.currentTimeMillis());
	
	@BeforeClass
	public static void init()
	{
		for(int i=0; i < 1000; i++)
		{
			
			
			instances.add(new ProblemInstance("Instance " + i,i));
		}
	
	}
	
	@Before
	public void before()
	{
			configSpace = new ParameterConfigurationSpace(TestHelper.getTestFile("paramFiles/simpleParam.txt"));
		
			execConfig = new AlgorithmExecutionConfiguration("foo", "bar", configSpace, false, false, 300);
		
		
	}
	@AfterClass
	public static void afterClass()
	{
		pool.logUsage();
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
		
		
		sf.getStateDeserializer("unknown_instance", 4, configSpace, emptyInstanceList, execConfig, new NewRunHistory( OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME));
	}
	
	public static File createTempDirectory()
		    throws IOException
		{
		    final File temp;

		    temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

		    if(!(temp.delete()))
		    {
		        throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
		    }

		    if(!(temp.mkdir()))
		    {
		        throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
		    }

		    return (temp);
		}
	
	@Test
	public void saveRestoreFidelityTest() throws IOException, DuplicateRunException
	{
		
		
		Random rand = pool.getRandom(DebugUtil.getCurrentMethodName());
		for(int i=0; i < 10; i++)
		{
			
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a [1,1000] [1]i \n b { on, off } [off] \n a | b in { on } ");
			AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("foo", "foo", configSpace, false, false, 0);
			
			File tempDir = createTempDirectory();
			
			
			
			StateFactory sf = new LegacyStateFactory(tempDir.getAbsolutePath(), tempDir.getAbsolutePath());
			
			
			StateSerializer ss = sf.getStateSerializer("it", 1);
			
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(rand);
			
			ProblemInstance pi = new ProblemInstance("test");
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pi,1), 0, config,execConfig);
			AlgorithmRunResult run = new ExistingAlgorithmRunResult(execConfig, rc,RunStatus.SAT, 0,0,0,1);
			//(InstanceSeedGenerator instanceSeedGenerator, OverallObjective intraInstanceObjective,  OverallObjective interInstanceObjective, RunObjective runObj)
			
			RunHistory rh = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN ,RunObjective.RUNTIME);
			
			
			rh.append(run);
			ss.setRunHistory(rh);
			ss.save();
			System.out.println(Arrays.toString(config.toValueArray()));
			
			StateDeserializer sd = sf.getStateDeserializer("it",1, configSpace, Collections.singletonList(pi), execConfig,new NewRunHistory( OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME));
			System.out.println(Arrays.toString(config.toValueArray()));
			ParameterConfiguration restoredConfig = sd.getRunHistory().getAlgorithmRunsExcludingRedundant().get(0).getAlgorithmRunConfiguration().getParameterConfiguration();
			System.out.println(Arrays.toString(restoredConfig.toValueArray()));
			assertTrue("Testing for equality ",config.equals(restoredConfig));
			assertTrue("Testing for Array equality", Arrays.equals(config.toValueArray(), restoredConfig.toValueArray()));
		}
		
		
	}
	
	@Test
	public void validInstance()
	{
		File f = TestHelper.getTestFile("stateFiles");
		StateFactory sf = new LegacyStateFactory(null,f.getAbsolutePath());	
		
		startOutputCapture();
		sf.getStateDeserializer("valid", 4, configSpace, instances, execConfig,new NewRunHistory( OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME));
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
		
		
		sf.getStateDeserializer("inf", 4, configSpace, instances, execConfig,new NewRunHistory( OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME));
	}
	
	@Test
	public void validInstanceLoweredCaptime()
	{
		
		File f = TestHelper.getTestFile("stateFiles");
		StateFactory sf = new LegacyStateFactory(null,f.getAbsolutePath());	
		
		 AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("foo", "bar", configSpace, false, false, 1);
		startOutputCapture();
		StateDeserializer sd = sf.getStateDeserializer("valid", 4, configSpace, instances, execConfig,new NewRunHistory( OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME));
		String output = stopOutputCapture();
		assertTrue(output.contains("Cutoff time discrepancy"));
		assertFalse(output.contains("marking run as TIMEOUT and Censored"));
		assertTrue(output.contains("marking run as TIMEOUT with runtime 1.0"));
		for(RunData runData : sd.getRunHistory().getAlgorithmRunDataIncludingRedundant())
		{
			if(runData.getRun().getRunStatus().equals(RunStatus.TIMEOUT))
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
		
		 AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("foo", "bar", configSpace, false, false, 1000);
		

		 startOutputCapture();
		StateDeserializer sd = sf.getStateDeserializer("valid", 4, configSpace, instances, execConfig,new NewRunHistory( OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME));
			String output = stopOutputCapture();
			assertTrue(output.contains("Cutoff time discrepancy"));
			assertTrue(output.contains("marking run as TIMEOUT and Censored"));
			assertFalse(output.contains("marking run as TIMEOUT with runtime"));
			
		for(RunData runData : sd.getRunHistory().getAlgorithmRunDataIncludingRedundant())
		{
			if(runData.getRun().getRunStatus().equals(RunStatus.TIMEOUT))
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
		
		  
				
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFileWithKilled.txt");
		ParameterConfigurationSpace configSpace;
		
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		
		long seed = System.currentTimeMillis();
		//seed = 1;
		System.out.println("Seed was:" + seed);
		Random r = new MersenneTwister(seed);
		
		configSpace = new ParameterConfigurationSpace(paramFile);
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 5000);
		List<ProblemInstance> pis = new ArrayList<ProblemInstance>();
		for(int i=0; i < 10; i++)
		{
			pis.add(new ProblemInstance("TestInstance_" + i,i));
		}
		
		
		InstanceSeedGenerator isg = new RandomInstanceSeedGenerator(pis, 1);
		
		
		
		List<AlgorithmRunConfiguration> runConfigs = getValidRunConfigurations(pis, r, isg, configSpace);
		
	
		TargetAlgorithmEvaluator tae = new EchoTargetAlgorithmEvaluator();
		
		RunHistory runHistory = new NewRunHistory(OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME);
		for(int i=0; i < 10; i++)
		{
			runHistory.incrementIteration();
		}
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		//PrintStream ps = System.out;
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		
		for(AlgorithmRunResult run : runs)
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
		
		stateS.setIncumbent(runs.get(0).getAlgorithmRunConfiguration().getParameterConfiguration());
		stateS.save();
		
		StateDeserializer stateD =  sf.getStateDeserializer("deleteMe-unitTest", 10, configSpace, pis, execConfig,new NewRunHistory( OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME));
		
		
		compareRestoredStateWithOriginalRunHistory(stateD, runHistory, pis, runs.size());
		
		
		
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
		ParameterConfigurationSpace configSpace;
		
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutorWithGibberish.class.getCanonicalName());
		
		long seed = System.currentTimeMillis();
		//seed = 1;
		
		System.out.println("Seed was:" + seed);
		Random r = new MersenneTwister(seed);
		
		configSpace = new ParameterConfigurationSpace(paramFile);
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 5000);
		List<ProblemInstance> pis = new ArrayList<ProblemInstance>();
		for(int i=0; i < 10; i++)
		{
			pis.add(new ProblemInstance("TestInstance_" + i,i));
		}
				
		InstanceSeedGenerator isg = new RandomInstanceSeedGenerator(pis, 1);

		List<AlgorithmRunConfiguration> runConfigs = getValidRunConfigurations(pis, r, isg, configSpace);
		
		TargetAlgorithmEvaluator tae = new EchoTargetAlgorithmEvaluator();
		RunHistory runHistory = new NewRunHistory( OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME);
				
		System.out.println("Performing " + runConfigs.size() + " runs");
		//=== Suppress System output to speed up test execution

		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		for(AlgorithmRunResult run : runs)
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
		
		stateS.setIncumbent(runs.get(0).getAlgorithmRunConfiguration().getParameterConfiguration());
		stateS.save();
		
		StateDeserializer stateD =  sf.getStateDeserializer("deleteMe-unitTest-gibberish", 10, configSpace, pis, execConfig,new NewRunHistory( OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME));
		
		compareRestoredStateWithOriginalRunHistory(stateD, runHistory, pis, runs.size());
		
	}
	
	
	/**
	 * Tests that before a purge we could restore some number of states, and after a perge we can only restore the last two.
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
	public void stateSerializationPurge() throws DuplicateRunException
	{
		
		  
				
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt");
		ParameterConfigurationSpace configSpace;
		
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		
		long seed = System.currentTimeMillis();
		//seed = 1;
		System.out.println("Seed was:" + seed);
		Random r = new MersenneTwister(seed);
		
		configSpace = new ParameterConfigurationSpace(paramFile);
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 5000);
		List<ProblemInstance> pis = new ArrayList<ProblemInstance>();
		for(int i=0; i < 10; i++)
		{
			pis.add(new ProblemInstance("TestInstance_" + i,i));
		}

		InstanceSeedGenerator isg = new RandomInstanceSeedGenerator(pis, 1);
		
		File randomDirectory;
		try {
			randomDirectory = File.createTempFile("smac", "junitTest");
			randomDirectory.delete();
			if(!randomDirectory.mkdirs())
			{
				throw new IllegalStateException("Couldn't create directory");
			}
			
		} catch (IOException e) {
			throw new IllegalStateException("Couldn't create directory");
		}
		
		TargetAlgorithmEvaluator tae =  new EchoTargetAlgorithmEvaluator();
		
		RunHistory runHistory = new NewRunHistory(OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME);
		
		
		for(int i=0; i < 10; i++)
		{
			runHistory.incrementIteration();
		}
		
		
		
		StateFactory sf = new LegacyStateFactory(randomDirectory.getAbsolutePath(),randomDirectory.getAbsolutePath());
		
		List<AlgorithmRunResult> allRuns = new ArrayList<AlgorithmRunResult>();
		for(int i=0; i < 20; i++)
		{
			List<AlgorithmRunConfiguration> runConfigs = getValidRunConfigurations(pis, r, isg, configSpace);
			
			System.out.println("Performing " + runConfigs.size() + " runs");
		
			List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs,null);
			allRuns.addAll(runs);
			for(AlgorithmRunResult run : runs)
			{
				runHistory.append(run);
			}
			
			
			StateSerializer stateS = sf.getStateSerializer("deleteMe-unitTest", 10+i);
			
			if(i%5 == 0) stateS.setRunHistory(runHistory); //Test quick restore
			 
			stateS.setIncumbent(runs.get(0).getAlgorithmRunConfiguration().getParameterConfiguration());
			stateS.save();
			
			if(i%5 == 0 )
			{ //Only try to restore every 5th one because we need a runhistory later to even test this
				StateDeserializer stateD =  sf.getStateDeserializer("deleteMe-unitTest", 10+i, configSpace, pis, execConfig,new NewRunHistory( OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME));
				compareRestoredStateWithOriginalRunHistory(stateD, runHistory, pis, allRuns.size());
			}
			
				runHistory.incrementIteration();
		}
		
		StateSerializer stateS = sf.getStateSerializer("deleteMe-unitTest", 100);
		stateS.setRunHistory(runHistory); //Test quick restore
		stateS.setIncumbent(allRuns.get(0).getAlgorithmRunConfiguration().getParameterConfiguration());
		stateS.save();
		
		for(int i=0; i < 20; i++)
		{
			StateDeserializer stateD =  sf.getStateDeserializer("deleteMe-unitTest", 10+i, configSpace, pis, execConfig,new NewRunHistory( OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME));
			assertEquals(stateD.getIteration(),(10+i));
			assertEquals(stateD.getRunHistory().getAlgorithmRunsIncludingRedundant().size() , 200*(i+1));
			
			
		}
	
		sf.purgePreviousStates();
	
		for(int i=0; i < 20; i++)
		{
			StateDeserializer stateD =  sf.getStateDeserializer("deleteMe-unitTest", 10+i, configSpace, pis, execConfig,new NewRunHistory( OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME));
			assertEquals(stateD.getIteration(),(10+i));
			assertEquals(stateD.getRunHistory().getAlgorithmRunsIncludingRedundant().size() , 200*(i+1));	
		}
	
	
		
	
		
		
	
	
		
		
		
		
		
		
		
		
		
	}
	
	
	public List<AlgorithmRunConfiguration> getValidRunConfigurations(List<ProblemInstance> pis, Random r, InstanceSeedGenerator isg, ParameterConfigurationSpace configSpace)
	{
		return getValidRunConfigurations(pis, r, isg, configSpace, 200);
	}
	
	public List<AlgorithmRunConfiguration> getValidRunConfigurations(List<ProblemInstance> pis, Random r, InstanceSeedGenerator isg, ParameterConfigurationSpace configSpace, int number)
	{
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(number+2);
		for(int i=0; i < number; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("TIMEOUT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				ProblemInstance pi = pis.get(r.nextInt(pis.size()));
				
				ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, isg.getNextSeed(pi));
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(pisp, 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		return runConfigs;
	}
	
	
	public void compareRestoredStateWithOriginalRunHistory(StateDeserializer stateD, RunHistory runHistory,List<ProblemInstance> pis, int runsSize )
	{
		RunHistory restoredRunHistory = stateD.getRunHistory();
		assertEquals(runHistory.getIteration(), restoredRunHistory.getIteration());
		Set<ProblemInstance> instanceSet = new HashSet<ProblemInstance>();
		instances.addAll(pis);
		
		boolean[] originalCensored = RunHistoryHelper.getCensoredEarlyFlagForRuns(runHistory.getAlgorithmRunsExcludingRedundant());
		boolean[] restoredCensored = RunHistoryHelper.getCensoredEarlyFlagForRuns(restoredRunHistory.getAlgorithmRunsExcludingRedundant());
		
		for(int i=0; i < runsSize; i++)
		{
			//==== We check equality on each member directly
			//==== So we can easily debug cases where it's broken
			
			AlgorithmRunResult run = runHistory.getAlgorithmRunsExcludingRedundant().get(i);
			
			AlgorithmRunResult restoredRun = restoredRunHistory.getAlgorithmRunsExcludingRedundant().get(i);
			
			assertDEquals(run.getQuality(), restoredRun.getQuality(),0.01);
			assertDEquals(run.getRuntime(),restoredRun.getRuntime(),0.01);
			assertEquals(run.getResultSeed(), restoredRun.getResultSeed());
			assertEquals(run.getRunStatus(),restoredRun.getRunStatus());
			assertDEquals(run.getRunLength(), restoredRun.getRunLength(), 0.01);
			assertEquals(run.getAdditionalRunData(), restoredRun.getAdditionalRunData());
			
			//=== Checks that each run has no additional run data
			
			assertEquals("",run.getAdditionalRunData().trim() );
			assertEquals(run.getWallclockExecutionTime(), restoredRun.getWallclockExecutionTime(), 0.001);
			
			

			
			ParameterConfiguration config = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			
		
			assertEquals(originalCensored[i],restoredCensored[i]);
			double cost1 = runHistory.getEmpiricalCost(config, instanceSet, execConfig.getAlgorithmMaximumCutoffTime());
			double cost2 = restoredRunHistory.getEmpiricalCost(config, instanceSet, execConfig.getAlgorithmMaximumCutoffTime()); 
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

	
	private void outputMem()
	{
		System.out.println("DATE: " + (new SimpleDateFormat()).format(new Date()));
		System.out.println("MAX: " + Runtime.getRuntime().maxMemory() / 1024.0 / 1024 + "MB");
		System.out.println("TOTAL: " + Runtime.getRuntime().totalMemory() / 1024.0 / 1024 + " MB");
		System.out.println("FREE: " + Runtime.getRuntime().freeMemory() / 1024.0 / 1024 + " MB");
		
	}
	/**
	 * This test is related to Task 
	 * 
	 */
	@SuppressWarnings("deprecation")
	@Test
	@Ignore 
	public void stateSerializationMemHeavy() throws DuplicateRunException
	{
	
		if(Runtime.getRuntime().maxMemory() / 1024.0 / 1024  > 80)
		{
			fail("Too much memory this test will not make any sense");
		}
		  
				
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt");
		ParameterConfigurationSpace configSpace;
		
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		
		long seed = System.currentTimeMillis();
		//seed = 1;
		System.out.println("Seed was:" + seed);
		Random r = new MersenneTwister(seed);
		
		configSpace = new ParameterConfigurationSpace(paramFile);
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 5000);
		List<ProblemInstance> pis = new ArrayList<ProblemInstance>();
		for(int i=0; i < 2000; i++)
		{
			pis.add(new ProblemInstance("TestInstance_" + i,i));
		}
		
		
		InstanceSeedGenerator isg = new RandomInstanceSeedGenerator(pis, 1);
		
		
		outputMem();
		
		List<AlgorithmRunConfiguration> runConfigs = getValidRunConfigurations(pis, r, isg, configSpace, 25000 );
		outputMem();
		
	
		
		TargetAlgorithmEvaluator tae = new EchoTargetAlgorithmEvaluator();
		
		RunHistory runHistory = new NewRunHistory( OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME);
		for(int i=0; i < 10; i++)
		{
			runHistory.incrementIteration();
		}
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		//PrintStream ps = System.out;
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		
		for(AlgorithmRunResult run : runs)
		{
			runHistory.append(run);
		}
		outputMem();
		
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
		outputMem();
		
		StateFactory sf = new LegacyStateFactory(f.getAbsolutePath(),f.getAbsolutePath());
		outputMem();
		StateSerializer stateS = sf.getStateSerializer("deleteMe-unitTest", 10);
		
		stateS.setRunHistory(runHistory);
		
		stateS.setIncumbent(runs.get(0).getAlgorithmRunConfiguration().getParameterConfiguration());
		stateS.save();
		
				
		
		
	}
	
	
	
}
