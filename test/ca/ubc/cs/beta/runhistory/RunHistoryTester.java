package ca.ubc.cs.beta.runhistory;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
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
import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.RunResult;
import ca.ubc.cs.beta.aeatk.algorithmrun.RunningAlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.kill.KillHandler;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
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
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;
import ca.ubc.cs.beta.aeatk.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistoryHelper;
import ca.ubc.cs.beta.aeatk.runhistory.ThreadSafeRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.ThreadSafeRunHistoryWrapper;
import ca.ubc.cs.beta.aeatk.seedgenerator.InstanceSeedGenerator;
import ca.ubc.cs.beta.aeatk.seedgenerator.RandomInstanceSeedGenerator;
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
	
	
	private static final SeedableRandomPool pool = new SeedableRandomPool(System.currentTimeMillis());
	
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
		
		
		
		
		
		AlgorithmRun run = ExistingAlgorithmRun.getRunFromString( runConfig, "0, 1 , 0 , 0, " + pisp.getSeed());
		
		
		
		try {
			r.append(run);

			runConfig = new AlgorithmRunConfiguration(pisp, 2, defaultConfig,execConfig);
			
			assertEquals(1.0,r.getEmpiricalCost(defaultConfig, r.getProblemInstancesRan(defaultConfig), 500),0.01);
			
			
			
			run = ExistingAlgorithmRun.getRunFromString( runConfig, "0, 2 , 0 , 0, " + pisp.getSeed());
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
			runHistory.append(new ExistingAlgorithmRun( rc, RunResult.SAT, 0, 0, 0, pisp.getSeed(),0));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		//Get a duplicate
		try {
			runHistory.append(new ExistingAlgorithmRun( rc, RunResult.SAT, 0, 0, 0, pisp.getSeed()));
			fail("Expected duplicate run exception");
		} catch (DuplicateRunException e) {
			
			
		}
		
		//Duplicates should be thrown for the same runconfig even with different results
		try {
			runHistory.append(new ExistingAlgorithmRun( rc, RunResult.UNSAT, 1, 1, 1, pisp.getSeed()));
			fail("Expected duplicate run exception");
		} catch (DuplicateRunException e) {
			
			
		}
		
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		assertTrue("Should have no runs", runHistory.getProblemInstanceSeedPairsRan(otherConfig).equals(Collections.EMPTY_SET));
		
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
			runHistory.append(new ExistingAlgorithmRun( rc, RunResult.SAT, 0, 0, 0, pisp.getSeed(),0));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		 execConfig = new AlgorithmExecutionConfiguration("boo2", "foo2", space, false, false, 500);
		 rc = new AlgorithmRunConfiguration(pisp, defaultConfig,execConfig);
		//Get a duplicate
		try {
			runHistory.append(new ExistingAlgorithmRun( rc, RunResult.SAT, 0, 0, 0, pisp.getSeed()));
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
			runHistory.append(new RunningAlgorithmRun( rc, 0, 0, 0, 0, 0, new KillHandler() {

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
	private List<AlgorithmRun> getInterestingRuns()
	{
		List<AlgorithmRun> runs = new ArrayList<AlgorithmRun>();
		//runs.add(new ExistingAlgorithmRun)
		return runs;
		
	}
	
	private final ThreadSafeRunHistory rh = new ThreadSafeRunHistoryWrapper(new NewRunHistory(OverallObjective.MEAN, OverallObjective.MEAN, RunObjective.RUNTIME));
	
	public void appendRun(AlgorithmRun run)
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
		appendRun(new ExistingAlgorithmRun(execConfig, new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pis.get(0), 0), cutoffTime, incumbent,execConfig), RunResult.SAT, 5, 0, 0, 0));
		
		
		
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
				
				piCount.get(pis.get(0)).incrementAndGet();
				
				List<ProblemInstanceSeedPair> pisps = RunHistoryHelper.getRandomInstanceSeedWithFewestRunsFor(rh, inscgen, incumbent, pis, rand, false,numberOfPispsToGenerate); 
				for(int i=0; i < numberOfPispsToGenerate; i++)
				{
					ProblemInstanceSeedPair pisp = pisps.get(i);
					piCount.get(pisp.getProblemInstance()).incrementAndGet();
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
				AlgorithmRun run = ExistingAlgorithmRun.getRunFromString( runConfig, "0, 1 , 0 , 0, " + pisp.getSeed());
				
				
				
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
				AlgorithmRun run = ExistingAlgorithmRun.getRunFromString( runConfig, "0, 1 , 0 , 0, " + pisp.getSeed());
				
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
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.TIMEOUT, 2, 0, 0, pisp.getSeed()));
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
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.TIMEOUT, 4, 0, 0, pisp.getSeed()));
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
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.SAT, 6, 0, 0, pisp.getSeed()));
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
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.TIMEOUT, 2, 0, 0, pisp.getSeed()));
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
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.TIMEOUT, 4, 0, 0, pisp.getSeed()));
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
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.KILLED, 1, 0, 0, pisp.getSeed()));
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
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.TIMEOUT, 2, 0, 0, pisp.getSeed()));
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
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.TIMEOUT, 4, 0, 0, pisp.getSeed()));
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
			runHistory.append(new ExistingAlgorithmRun( rc, RunResult.SAT, 3, 0, 0, pisp.getSeed()));
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
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.TIMEOUT, 2, 0, 0, pisp.getSeed()));
			
			ProblemInstance pi2 = ilws.getInstances().get(1);
			pisp = new ProblemInstanceSeedPair(pi2, insc.getNextSeed(pi2));
			rc = new AlgorithmRunConfiguration(pisp, cutoffTime, defaultConfig, execConfig);
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.SAT, 10, 0, 0, pisp.getSeed()));
			
			pisp = new ProblemInstanceSeedPair(pi2, insc.getNextSeed(pi2));
			rc = new AlgorithmRunConfiguration(pisp, 40, defaultConfig, execConfig);
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.SAT, 20, 0, 0, pisp.getSeed()));

			ProblemInstance pi3 = ilws.getInstances().get(2);
			pisp = new ProblemInstanceSeedPair(pi3, insc.getNextSeed(pi3));
			rc = new AlgorithmRunConfiguration(pisp, 10, defaultConfig, execConfig);
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.KILLED, 5, 0, 0, pisp.getSeed()));

		
			pisp = new ProblemInstanceSeedPair(pi3, insc.getNextSeed(pi3));
			rc = new AlgorithmRunConfiguration(pisp, 10, defaultConfig, execConfig);
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.TIMEOUT, 10, 0, 0, pisp.getSeed()));

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
	
	
	
	
}
