package ca.ubc.cs.beta.runhistory;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.algorithmrun.RunningAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillHandler;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.configspace.ParamFileHelper;
import ca.ubc.cs.beta.aclib.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;
import ca.ubc.cs.beta.aclib.probleminstance.InstanceListWithSeeds;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceHelper;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.random.SeedableRandomPool;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aclib.runhistory.RunHistory;
import ca.ubc.cs.beta.aclib.runhistory.RunHistoryHelper;
import ca.ubc.cs.beta.aclib.runhistory.ThreadSafeRunHistory;
import ca.ubc.cs.beta.aclib.runhistory.ThreadSafeRunHistoryWrapper;
import ca.ubc.cs.beta.aclib.seedgenerator.InstanceSeedGenerator;
import ca.ubc.cs.beta.aclib.seedgenerator.RandomInstanceSeedGenerator;
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
	
	private final ParamConfigurationSpace configSpace = ParamConfigurationTest.getConfigSpaceForFile("paramFiles/daisy-chain-param.txt");
	private final AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig("boo", "foo", configSpace, false, false, 500);
	
	
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
		
		ParamConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		ProblemInstanceSeedPair pisp = RunHistoryHelper.getRandomInstanceSeedWithFewestRunsFor(r, insc, defaultConfig, ilws.getInstances(), rand, false);
		
		RunConfig runConfig = new RunConfig(pisp, 1, defaultConfig,true);
		
		
		
		
		
		AlgorithmRun run = new ExistingAlgorithmRun(execConfig, runConfig, "0, 1 , 0 , 0, " + pisp.getSeed());
		
		
		
		try {
			r.append(run);

			runConfig = new RunConfig(pisp, 2, defaultConfig,true);
			
			assertEquals(1.0,r.getEmpiricalCost(defaultConfig, r.getProblemInstancesRan(defaultConfig), 500),0.01);
			
			
			
			run = new ExistingAlgorithmRun(execConfig, runConfig, "0, 2 , 0 , 0, " + pisp.getSeed());
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
	
	
		ParamConfigurationSpace space = ParamFileHelper.getParamFileFromString("a [0,9] [0]\nb [0,9] [0]\n");
				
		ParamConfiguration defaultConfig = space.getDefaultConfiguration();		
		
		ParamConfiguration otherConfig = space.getConfigurationFromString("-a '1' -b '1'", StringFormat.NODB_OR_STATEFILE_SYNTAX);
		
		ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(ilws.getInstances().get(0), insc.getNextSeed(ilws.getInstances().get(0)));
		RunConfig rc = new RunConfig(pisp, execConfig.getAlgorithmCutoffTime(), defaultConfig);
		try {
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.SAT, 0, 0, 0, pisp.getSeed()));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		//Get a duplicate
		try {
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.SAT, 0, 0, 0, pisp.getSeed()));
			fail("Expected duplicate run exception");
		} catch (DuplicateRunException e) {
			
			
		}
		
		//Duplicates should be thrown for the same runconfig even with different results
		try {
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.UNSAT, 1, 1, 1, pisp.getSeed()));
			fail("Expected duplicate run exception");
		} catch (DuplicateRunException e) {
			
			
		}
		
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		assertTrue("Should have no runs", runHistory.getProblemInstanceSeedPairsRan(otherConfig).equals(Collections.EMPTY_SET));
		
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
		ParamConfigurationSpace space = ParamFileHelper.getParamFileFromString("a [0,9] [0]\nb [0,9] [0]\n");
				
				
		RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(ilws.getInstances().get(0), insc.getNextSeed(ilws.getInstances().get(0))), execConfig.getAlgorithmCutoffTime(), space.getDefaultConfiguration()); 
	
		try {
			runHistory.append(new RunningAlgorithmRun(execConfig, rc, 0, 0, 0, 0, 0, new KillHandler() {

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
		
		
		ParamConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("x [0,10][5]\n");
		ParamConfiguration incumbent = configSpace.getDefaultConfiguration();
		
		
		
		
		InstanceSeedGenerator inscgen = new RandomInstanceSeedGenerator(pis, 0);
		pis.add(new ProblemInstance("Test1",1));
		appendRun(new ExistingAlgorithmRun(execConfig, new RunConfig(new ProblemInstanceSeedPair(pis.get(0), 0), cutoffTime, incumbent), RunResult.SAT, 5, 0, 0, 0));
		
		
		
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
					piCount.get(pisp.getInstance()).incrementAndGet();
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
		
		ParamConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		
		try {
			for(int i=0; i < 55; i++)
			{
				
				ProblemInstanceSeedPair pisp = RunHistoryHelper.getRandomInstanceSeedWithFewestRunsFor(r,insc,defaultConfig, ilws.getInstances(), rand, false);
				
				RunConfig runConfig = new RunConfig(pisp, 1, defaultConfig,true);
				AlgorithmRun run = new ExistingAlgorithmRun(execConfig, runConfig, "0, 1 , 0 , 0, " + pisp.getSeed());
				
				
				
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
		
		ParamConfiguration defaultConfig = configSpace.getDefaultConfiguration();	
		
		try {
			for(int i=0; i < 55; i++)
			{
				
				ProblemInstanceSeedPair pisp = RunHistoryHelper.getRandomInstanceSeedWithFewestRunsFor(r, insc,defaultConfig, ilws.getInstances(), rand, false);
				
				RunConfig runConfig = new RunConfig(pisp, 1, defaultConfig,true);
				AlgorithmRun run = new ExistingAlgorithmRun(execConfig, runConfig, "0, 1 , 0 , 0, " + pisp.getSeed());
				
				
				
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
	
	
		ParamConfigurationSpace space = ParamFileHelper.getParamFileFromString("a [0,9] [0]\nb [0,9] [0]\n");
				
		ParamConfiguration defaultConfig = space.getDefaultConfiguration();		
		
		ParamConfiguration otherConfig = space.getConfigurationFromString("-a '1' -b '1'", StringFormat.NODB_OR_STATEFILE_SYNTAX);
		
		ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(ilws.getInstances().get(0), insc.getNextSeed(ilws.getInstances().get(0)));
		RunConfig rc = new RunConfig(pisp, 2, defaultConfig, true);
		try {
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.TIMEOUT, 2, 0, 0, pisp.getSeed()));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		Set<ProblemInstanceSeedPair> earlyCensored = runHistory.getEarlyCensoredProblemInstanceSeedPairs(defaultConfig);
		
		assertEquals("Expected censored early runs to be 1", 1, earlyCensored.size());
		assertEquals("Expected cost to be ", runHistory.getEmpiricalCost(defaultConfig, Collections.singleton(ilws.getInstances().get(0)), execConfig.getAlgorithmCutoffTime()),2,0.01);
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		
		rc = new RunConfig(pisp, 4, defaultConfig, true);
		try {
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.TIMEOUT, 4, 0, 0, pisp.getSeed()));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		earlyCensored = runHistory.getEarlyCensoredProblemInstanceSeedPairs(defaultConfig);
		
		assertEquals("Expected censored early runs to be 1", 1, earlyCensored.size());
		assertEquals("Expected cost to be ", runHistory.getEmpiricalCost(defaultConfig, Collections.singleton(ilws.getInstances().get(0)), execConfig.getAlgorithmCutoffTime()),4,0.01);
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		
		
		rc = new RunConfig(pisp, 8, defaultConfig, true);
		try {
			runHistory.append(new ExistingAlgorithmRun(execConfig, rc, RunResult.SAT, 6, 0, 0, pisp.getSeed()));
		} catch (DuplicateRunException e) {
			e.printStackTrace();
			fail("Unexpected duplicated run exception");
		}
		
		earlyCensored = runHistory.getEarlyCensoredProblemInstanceSeedPairs(defaultConfig);
		
		
		assertEquals("Expected censored early runs to be 0", 0, earlyCensored.size());
		assertEquals("Expected cost to be ", runHistory.getEmpiricalCost(defaultConfig, Collections.singleton(ilws.getInstances().get(0)), execConfig.getAlgorithmCutoffTime()),6,0.01);
		
		
		assertTrue("Should only see an the one configuration", runHistory.getProblemInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		assertTrue("Should have no runs", runHistory.getProblemInstanceSeedPairsRan(otherConfig).equals(Collections.EMPTY_SET));
		assertEquals("Expect to see three runs", runHistory.getAlgorithmRunData(defaultConfig).size(), 3);
		
		
		
	
	}
	
}
