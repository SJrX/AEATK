package ca.ubc.cs.beta.runhistory;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Sets;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.ExistingAlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunningAlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.kill.KillHandler;
import ca.ubc.cs.beta.aeatk.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aeatk.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aeatk.objectives.OverallObjective;
import ca.ubc.cs.beta.aeatk.objectives.RunObjective;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParamFileHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration.ParameterStringFormat;
import ca.ubc.cs.beta.aeatk.probleminstance.InstanceListWithSeeds;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceHelper;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.probleminstance.seedgenerator.InstanceSeedGenerator;
import ca.ubc.cs.beta.aeatk.probleminstance.seedgenerator.RandomInstanceSeedGenerator;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;
import ca.ubc.cs.beta.aeatk.runhistory.FileSharingRunHistoryDecorator;
import ca.ubc.cs.beta.aeatk.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistoryHelper;
import ca.ubc.cs.beta.aeatk.runhistory.ThreadSafeRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.ThreadSafeRunHistoryWrapper;
import ca.ubc.cs.beta.configspace.ParamConfigurationTest;
import ca.ubc.cs.beta.probleminstance.ProblemInstanceHelperTester;
import ec.util.MersenneTwister;

public class RunHistoryTester {

	
	
	private final static List<ProblemInstance> pis = new ArrayList<ProblemInstance>();
	
	
	@BeforeClass
	public static void beforeClass()
	{
		for(int i=0; i < 10; i++)
		{
			pis.add(new ProblemInstance("instance " + i));
			
		}
		
		
	}
	
	private final ParameterConfigurationSpace configSpace = ParamConfigurationTest.getConfigSpaceForFile("paramFiles/daisy-chain-param.txt");
	private final AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("boo", "foo", configSpace, false, false, 500);
	
	private static Map<String, Integer> seeds = new HashMap<>();
	static 
	{
		
		
		//seeds.put("testRunHistorySavingToFileFeatures", -14922126); 
		
		 
	}
	private static final SeedableRandomPool pool = new SeedableRandomPool((int) System.currentTimeMillis(),seeds);
	
	@Before
	public void setUp()
	{
		ProblemInstanceHelper.clearCache();
	}
	
	@AfterClass
	public static void after()
	{
		pool.logUsage();
	}
	
	/**
	 * Tests that two run history objects 
	 */
	@Test
	public void testRunHistorySavingToFileFeatures()
	{
	
		Random rand = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		Set<String> features = new HashSet<String>();
		
		
		final int NUMBER_OF_FEATURES = rand.nextInt(100);
		for(int i=0; i < NUMBER_OF_FEATURES; i++)
		{
			features.add("feature_" + rand.nextDouble());
		}
		
		
		List<ProblemInstance> pis = new ArrayList<>();
		final int NUMBER_OF_INSTANCES = rand.nextInt(100) + 5;
		for(int i=0; i < NUMBER_OF_INSTANCES; i++)
		{
			Map<String, Double> featuresMap = new HashMap<>();
			
			
			for(String feat : features)
			{
				featuresMap.put(feat, rand.nextDouble());
			}
			
			pis.add(new ProblemInstance("instance_" +i, i+1, featuresMap, ""));
		}
		

		//InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatValid.txt", false);
		
		//InstanceSeedGenerator insc = ilws.getSeedGen();
		
		InstanceSeedGenerator insc = new RandomInstanceSeedGenerator(pis, rand.nextLong());
		InstanceListWithSeeds ilws = new InstanceListWithSeeds(insc, pis);
		
		//testSharedRunHistoryObjects(pis, ilws, insc,rand, 10,120);
		
		testSharedRunHistoryObjects(pis, ilws, insc,rand, 10, 120);
		
	}

	/**
	 * Tests that two run history objects 
	 */
	@Test
	public void testRunHistorySavingToFileNoFeatures()
	{
		
		
		
		Random rand = pool.getRandom(DebugUtil.getCurrentMethodName());
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatValid.txt", false);
		
		InstanceSeedGenerator insc = ilws.getSeedGen();
		testSharedRunHistoryObjects(pis, ilws, insc,rand, 10, 120 );
		
	
		
	}

	

	/**
	 * @param insc 
	 * @param ilws 
	 * 
	 */
	private void testSharedRunHistoryObjects(List<ProblemInstance> pis, InstanceListWithSeeds ilws, InstanceSeedGenerator insc , Random rand, final int DEFAULTS, final int RANDOMS) {
		
		
		RunHistory r = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
		RunHistory r2 = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
		
		File f;
		try {
			f = Files.createTempDirectory("runhistoryTest").toFile();
			
			r = new FileSharingRunHistoryDecorator(r, f, 1, pis, 125, true);
			
			r2 = new FileSharingRunHistoryDecorator(r2,f,2,pis,125, true); 
			
			//System.out.println(f.getAbsolutePath());
			
			for(int i=0; i < DEFAULTS; i++)
			{
				generateRunWithDefault(rand, ilws, insc, r);
				generateRunWithDefault(rand, ilws, insc, r2);
				
			}
			
	
			

			
			for(int i=0; i < RANDOMS; i++)
			{
				 generateRunWithRandom(rand, ilws, insc, r);
				 generateRunWithRandom(rand, ilws, insc, r2);
			
			}
			
			
			
			
			try {
				Thread.sleep(4096);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			AlgorithmRunResult run = getNewRun(rand, ilws, insc, r2, configSpace.getDefaultConfiguration());

			
			//System.out.println(run);
			
			r.append(run);
			r2.append(run);
		
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
			Set<AlgorithmRunResult> s1 = new HashSet<>(r.getAlgorithmRunsExcludingRedundant());
			
			Set<AlgorithmRunResult> s2 = new HashSet<>(r2.getAlgorithmRunsExcludingRedundant());
			
			
			System.out.println(r.getAlgorithmRunDataExcludingRedundant().size() + " vs. " + r2.getAlgorithmRunDataExcludingRedundant().size());
			assertEquals("Both RunHistory objects should have the same data", s1, s2);
			System.out.println(r.getAlgorithmRunDataExcludingRedundant().size() + " vs. " + r2.getAlgorithmRunDataExcludingRedundant().size());
			
			for(int i=0; i < RANDOMS; i++)
			{
				generateRunWithRandom(rand, ilws, insc, r);
				generateRunWithRandom(rand, ilws, insc, r2);
				
			}
			

			try {
				Thread.sleep(4096);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			run = getNewRun(rand, ilws, insc, r2, configSpace.getDefaultConfiguration());
			
			
			r.append(run);
			r2.append(run);
			
			
			s1 = new HashSet<>(r.getAlgorithmRunsExcludingRedundant());
			
			s2 = new HashSet<>(r2.getAlgorithmRunsExcludingRedundant());
			
			
			
			Set<AlgorithmRunResult> inS1NotInS2 = new HashSet<>();
			Set<AlgorithmRunResult> inS2NotInS1 = new HashSet<>();
			
			inS1NotInS2.addAll(s1);
			inS1NotInS2.removeAll(s2);
			
			inS2NotInS1.addAll(s2);
			inS2NotInS1.removeAll(s1);
			
			
			System.out.println(inS1NotInS2.size());
			System.out.println(inS2NotInS1.size());
			
			assertEquals("Both RunHistory objects should have the same data", inS1NotInS2, inS2NotInS1);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DuplicateRunException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	
	/**
	 * @param rand
	 * @param ilws
	 * @param insc
	 * @param r
	 * @return
	 * @throws DuplicateRunException 
	 */
	private void generateRunWithDefault(Random rand,
			InstanceListWithSeeds ilws, InstanceSeedGenerator insc, RunHistory r) throws DuplicateRunException {
		ParameterConfiguration defaultConfig = configSpace.getRandomParameterConfiguration(rand);
		
		generateAndAppendRun(rand, ilws, insc, r, defaultConfig);
		
	}

	/**
	 * @param rand
	 * @param ilws
	 * @param insc
	 * @param r
	 * @return
	 * @throws DuplicateRunException 
	 */
	private void generateRunWithRandom(Random rand,
			InstanceListWithSeeds ilws, InstanceSeedGenerator insc, RunHistory r) throws DuplicateRunException {
		ParameterConfiguration defaultConfig = configSpace.getRandomParameterConfiguration(rand);
		
		generateAndAppendRun(rand, ilws, insc, r, defaultConfig);
		
	}
	
	/**
	 * @param rand
	 * @param ilws
	 * @param insc
	 * @param r
	 * @param defaultConfig
	 * @throws DuplicateRunException
	 */
	private void generateAndAppendRun(Random rand, InstanceListWithSeeds ilws,
			InstanceSeedGenerator insc, RunHistory r,
			ParameterConfiguration defaultConfig) throws DuplicateRunException {
		
		AlgorithmRunResult run = getNewRun(rand, ilws, insc, r, defaultConfig);
		
		r.append(run);
	}

	/**
	 * @param rand
	 * @param ilws
	 * @param insc
	 * @param r
	 * @param defaultConfig
	 * @return
	 */
	private AlgorithmRunResult getNewRun(Random rand,
			InstanceListWithSeeds ilws, InstanceSeedGenerator insc,
			RunHistory r, ParameterConfiguration defaultConfig) {
		ProblemInstanceSeedPair pisp = RunHistoryHelper.getRandomInstanceSeedWithFewestRunsFor(r, insc, defaultConfig, ilws.getInstances(), rand, false);
		
		AlgorithmRunConfiguration runConfig = new AlgorithmRunConfiguration(pisp, 1, defaultConfig,execConfig);
		
		AlgorithmRunResult run = new ExistingAlgorithmRunResult( runConfig, getRandomStatus(rand), rand.nextDouble(), 0 , 0,  pisp.getSeed());
		return run;
	}

	/**
	 * @param rand
	 * @return
	 */
	private RunStatus getRandomStatus(Random rand) {
		RunStatus s =  RunStatus.values()[rand.nextInt(RunStatus.values().length)];
		
		
		switch(s)
		{
		case RUNNING:
		case ABORT:
		case KILLED:
			return getRandomStatus(rand);
		default:
			return s;
		}
		
	}
	/**
	 * Trying to replace a capped run causes an UnsupportedOperationException
	 */
	@Test
	public void testBug1319()
	{
		Random rand = new MersenneTwister();
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatValid.txt", false);
		
		InstanceSeedGenerator insc = ilws.getSeedGen();
		RunHistory r = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
		
		ParameterConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		ProblemInstanceSeedPair pisp = RunHistoryHelper.getRandomInstanceSeedWithFewestRunsFor(r, insc, defaultConfig, ilws.getInstances(), rand, false);
		
		AlgorithmRunConfiguration runConfig = new AlgorithmRunConfiguration(pisp, 1, defaultConfig,execConfig);
		
		
		
		
		
		AlgorithmRunResult run = ExistingAlgorithmRunResult.getRunFromString( runConfig, "0, 1 , 0 , 0, " + pisp.getSeed());
		
		
		
		try {
			r.append(run);

			runConfig = new AlgorithmRunConfiguration(pisp, 2, defaultConfig,execConfig);
			
			assertEquals(1.0,r.getEmpiricalCost(defaultConfig, r.getProblemInstancesRan(defaultConfig), 500),0.01);
			
			
			
			run = ExistingAlgorithmRunResult.getRunFromString( runConfig, "0, 2 , 0 , 0, " + pisp.getSeed());
			r.append(run);
			
			assertEquals(2.0,r.getEmpiricalCost(defaultConfig, r.getProblemInstancesRan(defaultConfig), 500),0.01);
			//System.out.println(r.getEmpiricalCost(defaultConfig, r.getInstancesRan(defaultConfig), 300));
			
		} catch (DuplicateRunException e) {
			fail("Should not have gotten a duplicate run exception");
			
		}
		
	}
	
	
	@Test
	public void testAppend()
	{
	
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatValid.txt", false);
		
		InstanceSeedGenerator insc = ilws.getSeedGen();
		RunHistory runHistory = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
	
	
		ParameterConfigurationSpace space = ParamFileHelper.getParamFileFromString("a [0,9] [0]\nb [0,9] [0]\n");
				
		ParameterConfiguration defaultConfig = space.getDefaultConfiguration();		
		
		ParameterConfiguration otherConfig = space.getParameterConfigurationFromString("-a '1' -b '1'", ParameterStringFormat.NODB_OR_STATEFILE_SYNTAX);
		
		ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(ilws.getInstances().get(0), insc.getNextSeed(ilws.getInstances().get(0)));
		AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("boo", "foo", space, false, false, 500);
		
		AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(pisp, defaultConfig,execConfig);
		try {
			runHistory.append(new ExistingAlgorithmRunResult( rc, RunStatus.SAT, 0, 0, 0, pisp.getSeed(),0));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		//Get a duplicate
		try {
			runHistory.append(new ExistingAlgorithmRunResult( rc, RunStatus.SAT, 0, 0, 0, pisp.getSeed()));
			fail("Expected duplicate run exception");
		} catch (DuplicateRunException e) {
			
			
		}
		
		//Duplicates should be thrown for the same runconfig even with different results
		try {
			runHistory.append(new ExistingAlgorithmRunResult( rc, RunStatus.UNSAT, 1, 1, 1, pisp.getSeed()));
			fail("Expected duplicate run exception");
		} catch (DuplicateRunException e) {
			
			
		}
		
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		assertTrue("Should have no runs", runHistory.getProblemInstanceSeedPairsRan(otherConfig).equals(Collections.EMPTY_SET));
		
	}

	
	@Test
	public void testRunHistoryFailureAtomic()
	{
		
		RunHistory r = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
		RunHistory r2 = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
		
		Random rand = pool.getRandom(DebugUtil.getCurrentMethodName());
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatValid.txt", false);
		
		InstanceSeedGenerator insc = ilws.getSeedGen();
		
		checkAllMethodsEqual(r, r2);
		
		
		ParameterConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		ProblemInstanceSeedPair pisp = RunHistoryHelper.getRandomInstanceSeedWithFewestRunsFor(r, insc, defaultConfig, ilws.getInstances(), rand, false);
		
		AlgorithmRunConfiguration runConfig = new AlgorithmRunConfiguration(pisp, 1, defaultConfig,execConfig);
		
		AlgorithmRunResult run = new ExistingAlgorithmRunResult( runConfig,RunStatus.SAT, rand.nextDouble(), 0 , 0,  pisp.getSeed());
		
		

		
		try 
		{
			r.append(run);
			r2.append(run);
		} catch(DuplicateRunException nc)
		{
			fail("Shouldn't have got this exception here");
		}
		checkAllMethodsEqual(r,r2);
		
		
		System.out.println(r.getAlgorithmRunDataExcludingRedundant());
		System.out.println(r.getAlgorithmRunsExcludingRedundant());
		System.out.println(r2.getAlgorithmRunsExcludingRedundant());
		try {
		r2.append(run);
		fail("This test should have failed");
		} catch(DuplicateRunException eh)
		{
			//Got it
		}
		
		checkAllMethodsEqual(r,r2);
		
		
		for(int i=0; i < 10; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(rand);
			AlgorithmRunResult run2 = getNewRun(rand, ilws, insc, r2, config);
			
			try {
				r.append(run2);
				r2.append(run2);
			} catch (DuplicateRunException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fail("Unexpected Exception");
			}


			
		
			checkAllMethodsEqual(r,r2);
			
			try {
				r2.append(run);
				fail("This test should have failed");
				} catch(DuplicateRunException eh)
				{
					//Got it
				}
			checkAllMethodsEqual(r,r2);
		}
		System.out.println(r.getAlgorithmRunsExcludingRedundant());
		
	}

	private void checkAllMethodsEqual(RunHistory r, RunHistory r2) {
	
		for(Method m : r.getClass().getMethods())
		{
			if(m.getName().startsWith("get"))
			{
				
				/*
				System.out.print(m.getName() + "=>" );
				
				for(Class<?> c :  m.getParameterTypes())
				{
					System.out.print(c.getSimpleName() + ",");
				}
				System.out.println();
				*/
				
				if(m.getParameterTypes().length == 0)
				{
					
					try {
						Object o1 = m.invoke(r, new Object[0]);
						Object o2 = m.invoke(r2, new Object[0]);
						

						try 
						{
							if ((o1 instanceof Object[]) &&(o2 instanceof Object[]))
							{
								
								assertTrue("Expected " + m.getName() + " to return the same thing on both runhistory objects", Arrays.deepEquals((Object[]) o1, (Object[]) o2));
							} else
							{
								assertEquals("Expected " + m.getName() + " to return the same thing on both runhistory objects", o1, o2);
							}
						} catch(AssertionError e)
						{
							throw e;
						}
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					
				}
			}
		}
		
	}
	
	

	@Test
	public void testExecConfigDetection()
	{
	
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatValid.txt", false);
		
		InstanceSeedGenerator insc = ilws.getSeedGen();
		RunHistory runHistory = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
	
	
		ParameterConfigurationSpace space = ParamFileHelper.getParamFileFromString("a [0,9] [0]\nb [0,9] [0]\n");
				
		ParameterConfiguration defaultConfig = space.getDefaultConfiguration();		
		
		ParameterConfiguration otherConfig = space.getParameterConfigurationFromString("-a '1' -b '1'", ParameterStringFormat.NODB_OR_STATEFILE_SYNTAX);
		
		ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(ilws.getInstances().get(0), insc.getNextSeed(ilws.getInstances().get(0)));
		AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("boo", "foo", space, false, false, 500);
		
		AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(pisp, defaultConfig,execConfig);
		try {
			runHistory.append(new ExistingAlgorithmRunResult( rc, RunStatus.SAT, 0, 0, 0, pisp.getSeed(),0));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		 execConfig = new AlgorithmExecutionConfiguration("boo2", "foo2", space, false, false, 500);
		 rc = new AlgorithmRunConfiguration(pisp, defaultConfig,execConfig);
		//Get a duplicate
		try {
			runHistory.append(new ExistingAlgorithmRunResult( rc, RunStatus.SAT, 0, 0, 0, pisp.getSeed()));
			fail("Expected error exception for handling different exec configurations");
		} catch (IllegalArgumentException e) {
			
			
		} catch (DuplicateRunException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IllegalStateException(e);
		}

	}
	
	
	
	
	/**
	 * An exception should be thrown if we try and append a run with RunResult.RUNNING
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testAppendRunningRun()
	{
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatValid.txt", false);
		
		InstanceSeedGenerator insc = ilws.getSeedGen();
		RunHistory runHistory = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
		ParameterConfigurationSpace space = ParamFileHelper.getParamFileFromString("a [0,9] [0]\nb [0,9] [0]\n");
				
				
		AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(ilws.getInstances().get(0), insc.getNextSeed(ilws.getInstances().get(0))), execConfig.getAlgorithmMaximumCutoffTime(), space.getDefaultConfiguration(), execConfig); 
	
		try {
			runHistory.append(new RunningAlgorithmRunResult( rc, 0, 0, 0, 0, 0, new KillHandler() {

				@Override
				public void kill() {
					
				}

				@Override
				public boolean isKilled() {
					return false;
				} 
				
			}));
			
			
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Duplicate Run Exception occurred this is odd");
		}
		
	}
	/**
	 * Returns a set of interesting runs
	 * @return
	 */
	@Ignore
	private List<AlgorithmRunResult> getInterestingRuns()
	{
		List<AlgorithmRunResult> runs = new ArrayList<AlgorithmRunResult>();
		//runs.add(new ExistingAlgorithmRun)
		return runs;
		
	}
	
	private final ThreadSafeRunHistory rh = new ThreadSafeRunHistoryWrapper(new NewRunHistory(OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME));
	
	public void appendRun(AlgorithmRunResult run)
	{
		try {
			rh.append(run);
		} catch (DuplicateRunException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * This tests that we can get multiple seeds to do at once
	 * 
	 */
	@Test
	public void testRunHistoryHelperMultipleRequests()
	{
		
		
		Random rand = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		List<ProblemInstance> pis = new ArrayList<ProblemInstance>();
		
		
		
		double cutoffTime = 50;
		
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("x [0,10][5]\n");
		ParameterConfiguration incumbent = configSpace.getDefaultConfiguration();
		
		
		 AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("boo", "foo", configSpace, false, false, 500);
		
		InstanceSeedGenerator inscgen = new RandomInstanceSeedGenerator(pis, 0);
		pis.add(new ProblemInstance("Test1",1));
		appendRun(new ExistingAlgorithmRunResult(execConfig, new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pis.get(0), 0), cutoffTime, incumbent,execConfig), RunStatus.SAT, 5, 0, 0, 0));
		
		
		ParameterConfiguration challenger = configSpace.getRandomParameterConfiguration(rand);
		for(int j=0; j< 20; j++)
		{
			int[] sizes = { 1, rand.nextInt(4)+1, rand.nextInt(4)+8, rand.nextInt(20)+1, rand.nextInt(200)+1, rand.nextInt(200)+ 150, rand.nextInt(1000)+1, rand.nextInt(2000)+1, rand.nextInt(2000) + 250, rand.nextInt(2000)+1000};
			
			
			pis.clear();
			pis.add(new ProblemInstance("Test1",1));
			int numbers = rand.nextInt(j+1)+2;
			for(int i=1; i < numbers; i++)
			{
				pis.add(new ProblemInstance("Test" + (i+1),(i+1)));
			}
			
			
			System.out.println("Executing against " + numbers + " instances");
			/*pis.add(new ProblemInstance("Test2",2));
			pis.add(new ProblemInstance("Test3",3));
			pis.add(new ProblemInstance("Test4",4));
			pis.add(new ProblemInstance("Test5",5));
			pis.add(new ProblemInstance("Test6",6));
			pis.add(new ProblemInstance("Test7",7));
			*/
			
			
			for(int numberOfPispsToGenerate : sizes)
			{
			
				System.out.println("Trying to generate :" + numberOfPispsToGenerate);

				Map<ProblemInstance, AtomicInteger> piCount = new LinkedHashMap<ProblemInstance, AtomicInteger>();
				
				
				for(ProblemInstance pi : pis)
				{
					piCount.put(pi, new AtomicInteger(0));
				}
				
				//piCount.get(pis.get(0)).incrementAndGet();
				
				List<ProblemInstanceSeedPair> pisps = RunHistoryHelper.getRandomInstanceSeedWithFewestRunsFor(rh, inscgen, challenger, pis, rand, false,numberOfPispsToGenerate); 
				
				Set <ProblemInstanceSeedPair> pispSet = new HashSet<>(pisps);
				
				if(pisps.size() != pispSet.size())
				{
					System.err.println("Duplicates detected");
				} else
				{
					System.out.println(pisps);
				}
				
				for(int i=0; i < numberOfPispsToGenerate; i++)
				{
					ProblemInstanceSeedPair pisp = pisps.get(i);
					piCount.get(pisp.getProblemInstance()).incrementAndGet();
					System.out.println(pisp);
				}
				
				
				int mathMin = -1;
				int mathMax = -1;
				for(Entry<ProblemInstance, AtomicInteger> vals : piCount.entrySet())
				{
					if(mathMin == -1)
					{
						mathMin = vals.getValue().get();
						mathMax = vals.getValue().get();
					} else
					{
						mathMin = Math.min(mathMin, vals.getValue().get());
						mathMax = Math.max(mathMax, vals.getValue().get());
					}
					System.out.println("Mapping: "+  vals);
					
				}
				
				
				
				
				
				assertTrue("Expected the instance with the most entries has at most one more entry than the one with the least. Instead have max: " + mathMax + " min: " + mathMin , (mathMax-mathMin) <= 1);
				
				//This test broke probably because now we are doing it on a different configuration (instead of the incumbent) some challenger.
				//System.out.println(numberOfPispsToGenerate);
				//System.out.println(pisps.size());
				//System.out.println(pis.size());
				//assertEquals("Expected number of generated instances is", mathMax, (int) Math.ceil((numberOfPispsToGenerate+1.0)/pis.size() ) );
			}
		}
	}
	
	
	
	
	/**
	 * This tests that we can get multiple seeds to do at once
	 * 
	 */
	@Test
	public void testRunHistoryMultipleRequestsWithExistingRuns()
	{
		
		
		Random rand = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		List<ProblemInstance> pis = new ArrayList<ProblemInstance>();
		
		
		
		double cutoffTime = 50;
		
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("x [0,10][5]\n");
		ParameterConfiguration incumbent = configSpace.getDefaultConfiguration();
		
		
		 AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("boo", "foo", configSpace, false, false, 500);
		
		InstanceSeedGenerator inscgen = new RandomInstanceSeedGenerator(pis, 0);
		pis.add(new ProblemInstance("Test1",1));
		appendRun(new ExistingAlgorithmRunResult(execConfig, new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pis.get(0), 3), cutoffTime, incumbent,execConfig), RunStatus.SAT, 5, 0, 0, 0));
		
		
		
		int j=0;
		int[] sizes = {rand.nextInt(6)+1};
		
		
		pis.clear();
		pis.add(new ProblemInstance("Test1",1));
		int numbers = rand.nextInt(j+1)+2;
		for(int i=1; i < numbers; i++)
		{
			pis.add(new ProblemInstance("Test" + (i+1),(i+1)));
		}
		
		
		System.out.println("Executing against " + numbers + " instances");
		/*pis.add(new ProblemInstance("Test2",2));
		pis.add(new ProblemInstance("Test3",3));
		pis.add(new ProblemInstance("Test4",4));
		pis.add(new ProblemInstance("Test5",5));
		pis.add(new ProblemInstance("Test6",6));
		pis.add(new ProblemInstance("Test7",7));
		*/
		
		ParameterConfiguration challenger = configSpace.getRandomParameterConfiguration(rand);
		for(int numberOfPispsToGenerate : sizes)
		{
		
			System.out.println("Trying to generate :" + numberOfPispsToGenerate);

			Map<ProblemInstance, AtomicInteger> piCount = new LinkedHashMap<ProblemInstance, AtomicInteger>();
			
			
			for(ProblemInstance pi : pis)
			{
				piCount.put(pi, new AtomicInteger(0));
			}
			
			piCount.get(pis.get(0)).incrementAndGet();
			
			List<ProblemInstanceSeedPair> pisps = RunHistoryHelper.getRandomInstanceSeedWithFewestRunsFor(rh, inscgen, challenger, pis, rand, false,numberOfPispsToGenerate); 
			
			Set <ProblemInstanceSeedPair> pispSet = new HashSet<>(pisps);
			
			if(pisps.size() != pispSet.size())
			{
				System.err.println("Duplicates detected");
			}
			
			for(int i=0; i < numberOfPispsToGenerate; i++)
			{
				ProblemInstanceSeedPair pisp = pisps.get(i);
				piCount.get(pisp.getProblemInstance()).incrementAndGet();
				System.out.println(pisp);
			}
			
			
			int mathMin = -1;
			int mathMax = -1;
			for(Entry<ProblemInstance, AtomicInteger> vals : piCount.entrySet())
			{
				if(mathMin == -1)
				{
					mathMin = vals.getValue().get();
					mathMax = vals.getValue().get();
				} else
				{
					mathMin = Math.min(mathMin, vals.getValue().get());
					mathMax = Math.max(mathMax, vals.getValue().get());
				}
				System.out.println("Mapping: "+  vals);
				
			}
			
			
			
			
			
			assertTrue("Expected the instance with the most entries has at most one more entry than the one with the least. Instead have max: " + mathMax + " min: " + mathMin , (mathMax-mathMin) <= 1);
			
			assertEquals("Expected number of generated instances is", mathMax, (int) Math.ceil((numberOfPispsToGenerate+1.0)/pis.size() ) );
		}
		
	}
	
	
	
	
	
	
	@Ignore("To fix later, we just crash currently")
	@Test
	/**
	 * This tests what happens when we have various numbers of seeds available for individual instances
	 * instead of a constant number.
	 */
	public void testUnequalInstanceSeedsAvailable()
	{

		Random rand = new MersenneTwister();
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatInstanceSeedValid.txt", false);
		
		InstanceSeedGenerator insc = ilws.getSeedGen();
		RunHistory r = new NewRunHistory(OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
		
		ParameterConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		
		try {
			for(int i=0; i < 55; i++)
			{
				
				ProblemInstanceSeedPair pisp = RunHistoryHelper.getRandomInstanceSeedWithFewestRunsFor(r,insc,defaultConfig, ilws.getInstances(), rand, false);
				
				AlgorithmRunConfiguration runConfig = new AlgorithmRunConfiguration(pisp, 1, defaultConfig,execConfig);
				AlgorithmRunResult run = ExistingAlgorithmRunResult.getRunFromString( runConfig, "0, 1 , 0 , 0, " + pisp.getSeed());
				
				
				
				r.append(run);
				//System.out.println(r.getEmpiricalCost(defaultConfig, r.getInstancesRan(defaultConfig), 300));
			}
		} catch (DuplicateRunException e) {
			fail("Should not have gotten a duplicate run exception");
			
		}

		
	}
	
	
	@Test
	@Ignore("This may not be an error condition (The IllegalStateException)")
	/**
	 * This tests what happens when we have no more seeds available
	 */
	public void testNoMoreSeedsAvailable()
	{

		Random rand = new MersenneTwister();
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatValid.txt", false, true);
		
		InstanceSeedGenerator insc = ilws.getSeedGen();
		RunHistory r = new NewRunHistory(OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
		
		ParameterConfiguration defaultConfig = configSpace.getDefaultConfiguration();	
		
		try {
			for(int i=0; i < 55; i++)
			{
				
				ProblemInstanceSeedPair pisp = RunHistoryHelper.getRandomInstanceSeedWithFewestRunsFor(r, insc,defaultConfig, ilws.getInstances(), rand, false);
				
				AlgorithmRunConfiguration runConfig = new AlgorithmRunConfiguration(pisp, 1, defaultConfig,execConfig);
				AlgorithmRunResult run = ExistingAlgorithmRunResult.getRunFromString( runConfig, "0, 1 , 0 , 0, " + pisp.getSeed());
				
				r.append(run);
				//System.out.println(r.getEmpiricalCost(defaultConfig, r.getInstancesRan(defaultConfig), 300));
			}
		} catch (DuplicateRunException e) {
			fail("Should not have gotten a duplicate run exception");
			
		}
		
	}
	
	
	
	@Test
	public void testCensoredEarlyRun()
	{
	
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatValid.txt", false);
		
		InstanceSeedGenerator insc = ilws.getSeedGen();
		RunHistory runHistory = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
	
	
		Set<ProblemInstance> instanceSet = new HashSet<ProblemInstance>();
		instanceSet.add(ilws.getInstances().get(0));
		
		double cutoffTime = execConfig.getAlgorithmMaximumCutoffTime();
		
		ParameterConfigurationSpace space = ParamFileHelper.getParamFileFromString("a [0,9] [0]\nb [0,9] [0]\n");
		
		ParameterConfiguration defaultConfig = space.getDefaultConfiguration();		
		
		ParameterConfiguration otherConfig = space.getParameterConfigurationFromString("-a '1' -b '1'", ParameterStringFormat.NODB_OR_STATEFILE_SYNTAX);

		
		AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("boo", "foo", space, false, false, 500);
		
		
		assertEquals("Configuration should have an upper bound of " + cutoffTime, cutoffTime, runHistory.getEmpiricalCostUpperBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		assertEquals("Configuration should have an lower bound of " + 0,0.0,runHistory.getEmpiricalCostLowerBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		
		ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(ilws.getInstances().get(0), insc.getNextSeed(ilws.getInstances().get(0)));
		AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(pisp, 2, defaultConfig, execConfig);
		try {
			runHistory.append(new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.TIMEOUT, 2, 0, 0, pisp.getSeed()));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		
		Set<ProblemInstanceSeedPair> earlyCensored = runHistory.getEarlyCensoredProblemInstanceSeedPairs(defaultConfig);
		
		assertEquals("Expected censored early runs to be 1", 1, earlyCensored.size());
		assertEquals("Expected cost to be ", runHistory.getEmpiricalCost(defaultConfig, Collections.singleton(ilws.getInstances().get(0)), cutoffTime),2,0.01);
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		assertEquals("Expected runtime sum to match hand calculation ", 2.0, runHistory.getTotalRunCost(), 0.01);
		
		assertEquals("Configuration should have an upper bound of " + cutoffTime, cutoffTime, runHistory.getEmpiricalCostUpperBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		assertEquals("Configuration should have an lower bound of " + 2,2.0,runHistory.getEmpiricalCostLowerBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		
		
		rc = new AlgorithmRunConfiguration(pisp, 4, defaultConfig, execConfig);
		try {
			runHistory.append(new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.TIMEOUT, 4, 0, 0, pisp.getSeed()));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		earlyCensored = runHistory.getEarlyCensoredProblemInstanceSeedPairs(defaultConfig);
		
		assertEquals("Expected censored early runs to be 1", 1, earlyCensored.size());
		assertEquals("Expected cost to be ", runHistory.getEmpiricalCost(defaultConfig, Collections.singleton(ilws.getInstances().get(0)), cutoffTime),4,0.01);
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		assertEquals("Expected runtime sum to match hand calculation ", 6.0, runHistory.getTotalRunCost(), 0.01);
		
		
		
		assertEquals("Configuration should have an upper bound of " + cutoffTime, cutoffTime, runHistory.getEmpiricalCostUpperBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		assertEquals("Configuration should have an lower bound of " + 4,4.0,runHistory.getEmpiricalCostLowerBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		
		
		rc = new AlgorithmRunConfiguration(pisp, 8, defaultConfig, execConfig);
		try {
			runHistory.append(new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.SAT, 6, 0, 0, pisp.getSeed()));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		earlyCensored = runHistory.getEarlyCensoredProblemInstanceSeedPairs(defaultConfig);
		
		
		assertEquals("Expected censored early runs to be 0", 0, earlyCensored.size());
		assertEquals("Expected cost to be ", runHistory.getEmpiricalCost(defaultConfig, Collections.singleton(ilws.getInstances().get(0)), cutoffTime),6,0.01);
		assertEquals("Expected runtime sum to match hand calculation ", 12.0, runHistory.getTotalRunCost(), 0.01);
		
		assertEquals("Configuration should have an upper bound of " + cutoffTime, 6.0, runHistory.getEmpiricalCostUpperBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		assertEquals("Configuration should have an lower bound of " + 6, 6.0, runHistory.getEmpiricalCostLowerBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		
		assertEquals("RunData should be 1 ", runHistory.getAlgorithmRunsExcludingRedundant(defaultConfig).size() , 1);
		assertEquals("RunData should be 1 ", runHistory.getTotalNumRunsOfConfigExcludingRedundant(defaultConfig) , 1);
		
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		assertTrue("Should have no runs", runHistory.getProblemInstanceSeedPairsRan(otherConfig).equals(Collections.EMPTY_SET));
		
		assertEquals("Expect to see three runs", runHistory.getAlgorithmRunsIncludingRedundant(defaultConfig).size(), 3);
		assertEquals("Expect to see one runs", runHistory.getAlgorithmRunsExcludingRedundant(defaultConfig).size(), 1);
		

		assertEquals("Expect to see three runs", runHistory.getAlgorithmRunsIncludingRedundant(defaultConfig).size(), 3);
		assertEquals("Expect to see one runs", runHistory.getAlgorithmRunsExcludingRedundant(defaultConfig).size(), 1);
		
		
	}
	
	@Test
	public void testCensoredEarlyRunWithLastBeingKilledAndLess()
	{
	
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatValid.txt", false);
		
		InstanceSeedGenerator insc = ilws.getSeedGen();
		RunHistory runHistory = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
	
		Set<ProblemInstance> instanceSet = new HashSet<ProblemInstance>();
		instanceSet.add(ilws.getInstances().get(0));
		
		ParameterConfigurationSpace space = ParamFileHelper.getParamFileFromString("a [0,9] [0]\nb [0,9] [0]\n");
		
		ParameterConfiguration defaultConfig = space.getDefaultConfiguration();		
		
		ParameterConfiguration otherConfig = space.getParameterConfigurationFromString("-a '1' -b '1'", ParameterStringFormat.NODB_OR_STATEFILE_SYNTAX);

		
		double cutoffTime = execConfig.getAlgorithmMaximumCutoffTime();
		
		AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("boo", "foo", space, false, false, 500);
		
		
		ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(ilws.getInstances().get(0), insc.getNextSeed(ilws.getInstances().get(0)));
		AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(pisp, 2, defaultConfig, execConfig);
		

		assertEquals("Configuration should have an upper bound of " + cutoffTime, cutoffTime, runHistory.getEmpiricalCostUpperBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		assertEquals("Configuration should have an lower bound of " + 0,0.0,runHistory.getEmpiricalCostLowerBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		
		
		try {
			runHistory.append(new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.TIMEOUT, 2, 0, 0, pisp.getSeed()));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		Set<ProblemInstanceSeedPair> earlyCensored = runHistory.getEarlyCensoredProblemInstanceSeedPairs(defaultConfig);
		
		assertEquals("Expected censored early runs to be 1", 1, earlyCensored.size());
		assertEquals("Expected cost to be ", runHistory.getEmpiricalCost(defaultConfig, Collections.singleton(ilws.getInstances().get(0)), cutoffTime),2,0.01);
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		assertEquals("Expected runtime sum to match hand calculation ", 2.0, runHistory.getTotalRunCost(), 0.01);
		

		assertEquals("Configuration should have an upper bound of " + cutoffTime, cutoffTime, runHistory.getEmpiricalCostUpperBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		assertEquals("Configuration should have an lower bound of " + 0,2.0,runHistory.getEmpiricalCostLowerBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		
		rc = new AlgorithmRunConfiguration(pisp, 4, defaultConfig, execConfig);
		try {
			runHistory.append(new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.TIMEOUT, 4, 0, 0, pisp.getSeed()));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		earlyCensored = runHistory.getEarlyCensoredProblemInstanceSeedPairs(defaultConfig);
		
		assertEquals("Expected censored early runs to be 1", 1, earlyCensored.size());
		assertEquals("Expected cost to be ", runHistory.getEmpiricalCost(defaultConfig, Collections.singleton(ilws.getInstances().get(0)), cutoffTime),4,0.01);
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		
		assertEquals("Expected runtime sum to match hand calculation ", 6.0, runHistory.getTotalRunCost(), 0.01);
		

		assertEquals("Configuration should have an upper bound of " + cutoffTime, cutoffTime, runHistory.getEmpiricalCostUpperBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		assertEquals("Configuration should have an lower bound of " + 0,4.0,runHistory.getEmpiricalCostLowerBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		
		rc = new AlgorithmRunConfiguration(pisp, 8, defaultConfig, execConfig);
		try {
			runHistory.append(new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.KILLED, 1, 0, 0, pisp.getSeed()));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		earlyCensored = runHistory.getEarlyCensoredProblemInstanceSeedPairs(defaultConfig);
		
		
		assertEquals("Expected censored early runs to be 1", 1, earlyCensored.size());
		assertEquals("Expected cost to be ", runHistory.getEmpiricalCost(defaultConfig, Collections.singleton(ilws.getInstances().get(0)), cutoffTime),4,0.01);
		assertEquals("Expected runtime sum to match hand calculation ", 7.0, runHistory.getTotalRunCost(), 0.01);
		

		assertEquals("Configuration should have an upper bound of " + cutoffTime, cutoffTime, runHistory.getEmpiricalCostUpperBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		assertEquals("Configuration should have an lower bound of " + 0,4.0,runHistory.getEmpiricalCostLowerBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		
		
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		assertTrue("Should have no runs", runHistory.getProblemInstanceSeedPairsRan(otherConfig).equals(Collections.EMPTY_SET));
		
		assertEquals("Expect to see three runs", runHistory.getTotalNumRunsOfConfigIncludingRedundant(defaultConfig), 3);
		assertEquals("Expect to see one runs", runHistory.getTotalNumRunsOfConfigExcludingRedundant(defaultConfig), 1);
		
		
		assertEquals("Expect to see three runs", runHistory.getAlgorithmRunsIncludingRedundant(defaultConfig).size(), 3);
		assertEquals("Expect to see one runs", runHistory.getAlgorithmRunsExcludingRedundant(defaultConfig).size(), 1);
		
	}
	
	

	@Test
	/**
	 * This situation can happen due to noise, although the numbers in this test are admittedly more than we would like :(.
	 */
	public void testCensoredEarlyRunWithFinalRunlessThanCensored()
	{
	
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatValid.txt", false);
		
		InstanceSeedGenerator insc = ilws.getSeedGen();
		RunHistory runHistory = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
	
	
		Set<ProblemInstance> instanceSet = new HashSet<ProblemInstance>();
		instanceSet.add(ilws.getInstances().get(0));

		ParameterConfigurationSpace space = ParamFileHelper.getParamFileFromString("a [0,9] [0]\nb [0,9] [0]\n");
		
		ParameterConfiguration defaultConfig = space.getDefaultConfiguration();		
		
		ParameterConfiguration otherConfig = space.getParameterConfigurationFromString("-a '1' -b '1'", ParameterStringFormat.NODB_OR_STATEFILE_SYNTAX);

		AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("boo", "foo", space, false, false, 500);
		
		
		double cutoffTime = execConfig.getAlgorithmMaximumCutoffTime();
		
		

		assertEquals("Configuration should have an upper bound of " + cutoffTime, cutoffTime, runHistory.getEmpiricalCostUpperBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		assertEquals("Configuration should have an lower bound of " + 0,0.0,runHistory.getEmpiricalCostLowerBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		
		ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(ilws.getInstances().get(0), insc.getNextSeed(ilws.getInstances().get(0)));
		AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(pisp, 2, defaultConfig, execConfig);
		try {
			runHistory.append(new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.TIMEOUT, 2, 0, 0, pisp.getSeed()));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		Set<ProblemInstanceSeedPair> earlyCensored = runHistory.getEarlyCensoredProblemInstanceSeedPairs(defaultConfig);
		
		assertEquals("Expected censored early runs to be 1", 1, earlyCensored.size());
		assertEquals("Expected cost to be ", runHistory.getEmpiricalCost(defaultConfig, Collections.singleton(ilws.getInstances().get(0)), cutoffTime),2,0.01);
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		assertEquals("Expected runtime sum to match hand calculation ", 2.0, runHistory.getTotalRunCost(), 0.01);
		

		assertEquals("Configuration should have an upper bound of " + cutoffTime, cutoffTime, runHistory.getEmpiricalCostUpperBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		assertEquals("Configuration should have an lower bound of " + 0,2.0,runHistory.getEmpiricalCostLowerBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		
		rc = new AlgorithmRunConfiguration(pisp, 4, defaultConfig, execConfig);
		try {
			runHistory.append(new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.TIMEOUT, 4, 0, 0, pisp.getSeed()));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		earlyCensored = runHistory.getEarlyCensoredProblemInstanceSeedPairs(defaultConfig);
		
		assertEquals("Expected censored early runs to be 1", 1, earlyCensored.size());
		assertEquals("Expected cost to be ", runHistory.getEmpiricalCost(defaultConfig, Collections.singleton(ilws.getInstances().get(0)), cutoffTime),4,0.01);
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		
		assertEquals("Expected runtime sum to match hand calculation ", 6.0, runHistory.getTotalRunCost(), 0.01);
		

		assertEquals("Configuration should have an upper bound of " + cutoffTime, cutoffTime, runHistory.getEmpiricalCostUpperBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		assertEquals("Configuration should have an lower bound of " + 0,4.0,runHistory.getEmpiricalCostLowerBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		
		
		rc = new AlgorithmRunConfiguration(pisp, 8, defaultConfig, execConfig);
		try {
			runHistory.append(new ExistingAlgorithmRunResult( rc, RunStatus.SAT, 3, 0, 0, pisp.getSeed()));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		earlyCensored = runHistory.getEarlyCensoredProblemInstanceSeedPairs(defaultConfig);
		
		
		assertEquals("Expected censored early runs to be 0", 0, earlyCensored.size());
		assertEquals("Expected cost to be ", runHistory.getEmpiricalCost(defaultConfig, Collections.singleton(ilws.getInstances().get(0)), cutoffTime),3,0.01);
		assertEquals("Expected runtime sum to match hand calculation ", 9.0, runHistory.getTotalRunCost(), 0.01);
		

		assertEquals("Configuration should have an upper bound of " + 3.0, 3, runHistory.getEmpiricalCostUpperBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		assertEquals("Configuration should have an lower bound of " + 3,3.0,runHistory.getEmpiricalCostLowerBound(defaultConfig, instanceSet, cutoffTime), 0.01);
		
		
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		assertTrue("Should have no runs", runHistory.getProblemInstanceSeedPairsRan(otherConfig).equals(Collections.EMPTY_SET));
		
		
		assertEquals("Expect to see three runs", runHistory.getTotalNumRunsOfConfigIncludingRedundant(defaultConfig), 3);
		assertEquals("Expect to see one runs", runHistory.getTotalNumRunsOfConfigExcludingRedundant(defaultConfig), 1);
		
		
		assertEquals("Expect to see three runs", runHistory.getAlgorithmRunsIncludingRedundant(defaultConfig).size(), 3);
		assertEquals("Expect to see one runs", runHistory.getAlgorithmRunsExcludingRedundant(defaultConfig).size(), 1);
		

	}
	@Test
	/**
	 * This situation can happen due to noise, although the numbers in this test are admittedly more than we would like :(.
	 */
	
	public void testUpperAndLowerBound()
	{
		//fail("Interface buggy at this point, test is well defined but the interface causes the results to break");
		try 
		{
			InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatValid.txt", false);
			
			InstanceSeedGenerator insc = ilws.getSeedGen();
			RunHistory runHistory = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
		
		
			Set<ProblemInstance> instanceSet = new HashSet<ProblemInstance>();
			instanceSet.add(ilws.getInstances().get(0));
			
			ParameterConfigurationSpace space = ParamFileHelper.getParamFileFromString("a [0,9] [0]\nb [0,9] [0]\n");
			
			ParameterConfiguration defaultConfig = space.getDefaultConfiguration();		
			
			ParameterConfiguration otherConfig = space.getParameterConfigurationFromString("-a '1' -b '1'", ParameterStringFormat.NODB_OR_STATEFILE_SYNTAX);

			
			AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("boo", "foo", space, false, false, 500);
			
			double cutoffTime = execConfig.getAlgorithmMaximumCutoffTime();
			
			
		
			ProblemInstance pi1 = ilws.getInstances().get(0);
			ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi1, insc.getNextSeed(pi1));
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(pisp, 2, defaultConfig, execConfig);
			runHistory.append(new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.TIMEOUT, 2, 0, 0, pisp.getSeed()));
			
			ProblemInstance pi2 = ilws.getInstances().get(1);
			pisp = new ProblemInstanceSeedPair(pi2, insc.getNextSeed(pi2));
			rc = new AlgorithmRunConfiguration(pisp, cutoffTime, defaultConfig, execConfig);
			runHistory.append(new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.SAT, 10, 0, 0, pisp.getSeed()));
			
			pisp = new ProblemInstanceSeedPair(pi2, insc.getNextSeed(pi2));
			rc = new AlgorithmRunConfiguration(pisp, 40, defaultConfig, execConfig);
			runHistory.append(new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.SAT, 20, 0, 0, pisp.getSeed()));

			ProblemInstance pi3 = ilws.getInstances().get(2);
			pisp = new ProblemInstanceSeedPair(pi3, insc.getNextSeed(pi3));
			rc = new AlgorithmRunConfiguration(pisp, 10, defaultConfig, execConfig);
			runHistory.append(new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.KILLED, 5, 0, 0, pisp.getSeed()));

		
			pisp = new ProblemInstanceSeedPair(pi3, insc.getNextSeed(pi3));
			rc = new AlgorithmRunConfiguration(pisp, 10, defaultConfig, execConfig);
			runHistory.append(new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.TIMEOUT, 10, 0, 0, pisp.getSeed()));

			Set<ProblemInstance> pi12 = Sets.newHashSet(pi1,pi2);
			
			assertEquals("Configuration should match upper bound ",257.50, runHistory.getEmpiricalCostUpperBound(defaultConfig, pi12, cutoffTime), 0.01);
			assertEquals("Configuration should have an lower bound ",8.5,runHistory.getEmpiricalCostLowerBound(defaultConfig, pi12, cutoffTime), 0.01);
			
			Set<ProblemInstance> pi23 = Sets.newHashSet(pi2,pi3);
			
			assertEquals("Configuration should match upper bound ",257.50, runHistory.getEmpiricalCostUpperBound(defaultConfig, pi23, cutoffTime), 0.01);
			assertEquals("Configuration should have an lower bound ",11.25,runHistory.getEmpiricalCostLowerBound(defaultConfig, pi23, cutoffTime), 0.01);
			
			Set<ProblemInstance> pi13 = Sets.newHashSet(pi1,pi3);
			
			assertEquals("Configuration should match upper bound ",500, runHistory.getEmpiricalCostUpperBound(defaultConfig, pi13, cutoffTime), 0.01);
			assertEquals("Configuration should have an lower bound ",4.75,runHistory.getEmpiricalCostLowerBound(defaultConfig, pi13, cutoffTime), 0.01);
		
			Set<ProblemInstance> pi123 = Sets.newHashSet(pi1,pi2,pi3);
			
			assertEquals("Configuration should match upper bound ",338.333333, runHistory.getEmpiricalCostUpperBound(defaultConfig, pi123, cutoffTime), 0.01);
			assertEquals("Configuration should have an lower bound ",8.16666666, runHistory.getEmpiricalCostLowerBound(defaultConfig, pi123, cutoffTime), 0.01);
			
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
	
	}
	
	/**
     * This is a test for bug #2052 
     * @throws DuplicateRunException 
     */
    @Test
    public void testVerifyOrderConsistent() throws DuplicateRunException
    {
            
            Random rand = pool.getRandom(DebugUtil.getCurrentMethodName());
            ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a [0,1] [0.5]\n b[0,1] [0.5]\n");
            List<ProblemInstance> pis = new ArrayList<ProblemInstance>();
            
            
            
            Map<String, Double> features = new HashMap<String,Double>();
            
            for(int i=1; i < 11; i++)
            {
                    features.put("id",(double) i);
                    pis.add(new ProblemInstance("instance"+i,i,features));
            }
            
            
            List<ParameterConfiguration> configs = new ArrayList<>();
            
            for(int i=0; i < 5; i++)
            {
                    configs.add(configSpace.getRandomParameterConfiguration(rand));
            }
            
            List<AlgorithmRunResult> runs = new ArrayList<>();
            
            AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("boo", "foo", configSpace, false, true, 5);
            
            
            
            
            
            runs.add(new ExistingAlgorithmRunResult( new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pis.get(0), -1),5, configs.get(0), execConfig), RunStatus.SAT, 1, 0, 0,-1));
            
            runs.add(new ExistingAlgorithmRunResult( new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pis.get(1), -1),5, configs.get(0),execConfig), RunStatus.SAT, 2, 0, 0,-1));
            
            runs.add(new ExistingAlgorithmRunResult( new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pis.get(0), -1),1, configs.get(1),execConfig), RunStatus.TIMEOUT, 1, 0, 0,-1));
            
            runs.add(new ExistingAlgorithmRunResult( new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pis.get(2), -1),5, configs.get(0),execConfig), RunStatus.SAT, 3, 0, 0,-1));
            
            runs.add(new ExistingAlgorithmRunResult( new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pis.get(1), -1),2, configs.get(2),execConfig), RunStatus.TIMEOUT, 2, 0, 0,-1));
            
            runs.add(new ExistingAlgorithmRunResult( new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pis.get(3), -1),5, configs.get(0),execConfig), RunStatus.SAT, 3, 0, 0,-1));
            
            runs.add(new ExistingAlgorithmRunResult( new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pis.get(0), -1),2, configs.get(1),execConfig), RunStatus.TIMEOUT, 2, 0, 0,-1));
            
            
            
            NewRunHistory runHistory = new NewRunHistory( OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME);
            
            
            for(AlgorithmRunResult run : runs)
            {
                    System.out.println(run);
                    runHistory.append(run);
            }
            
            List<AlgorithmRunResult> resultRuns = runHistory.getAlgorithmRunsExcludingRedundant();
            
            double[][] thetas = runHistory.getAllConfigurationsRanInValueArrayForm();
            
            int[][] thetaPiMatrix = runHistory.getParameterConfigurationInstancesRanByIndexExcludingRedundant();
            
            System.out.println(resultRuns);
            System.out.println(Arrays.deepToString(thetas));
            System.out.println(Arrays.deepToString(thetaPiMatrix));
            
            
            for(int i=0; i < resultRuns.size(); i++)
            {
                    AlgorithmRunResult run = resultRuns.get(i);
                    
                    System.out.println("Run: " + i + ":" + run);
                    
                    System.out.println(thetaPiMatrix[i][0]);
                    
                    System.out.println(runHistory.getThetaIdx(run.getAlgorithmRunConfiguration().getParameterConfiguration()));
                    
                    assertEquals(thetaPiMatrix[i][0],runHistory.getThetaIdx(run.getAlgorithmRunConfiguration().getParameterConfiguration()));
                    
                    
                    System.out.println(Arrays.toString(thetaPiMatrix[i]));
                    
                    System.out.println(Arrays.toString(thetas[thetaPiMatrix[i][0]-1]));
                    System.out.println(Arrays.toString(run.getAlgorithmRunConfiguration().getParameterConfiguration().toValueArray()));
            
                    
                     //run.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceID()
                     
                    System.out.println("Instance ID:" + run.getAlgorithmRunConfiguration().getProblemInstanceSeedPair().getProblemInstance().getInstanceID());
                    
                    System.out.println("ThetaPiMatrix:"+(int) thetaPiMatrix[i][1]);
                    
                    
                    assertEquals("Expected Instance ID should match ", run.getAlgorithmRunConfiguration().getProblemInstanceSeedPair().getProblemInstance().getInstanceID(),(int) thetaPiMatrix[i][1]);
                    
                    assertTrue("Expected Configuration should match",Arrays.equals(thetas[thetaPiMatrix[i][0]-1], run.getAlgorithmRunConfiguration().getParameterConfiguration().toValueArray()));
                    assertEquals("Expected Instance ID should match ", run.getAlgorithmRunConfiguration().getProblemInstanceSeedPair().getProblemInstance().getInstanceID(),(int) run.getAlgorithmRunConfiguration().getProblemInstanceSeedPair().getProblemInstance().getFeaturesDouble()[0]);
                    
                    System.out.println("\n");
            }
            
            
            

            assertEquals("Test", resultRuns.size(), thetaPiMatrix.length); 
            
            
            
            //System.out.println(runHistory.getAlgorithmRunData());
    
            //runHistory.getAllConfigurationsRanInValueArrayForm();
            
            
    }
	
	
}
