package ca.ubc.cs.beta.runhistory;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;
import ca.ubc.cs.beta.aclib.probleminstance.InstanceListWithSeeds;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceHelper;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aclib.runhistory.RunHistory;
import ca.ubc.cs.beta.aclib.runhistory.RunHistoryHelper;
import ca.ubc.cs.beta.aclib.seedgenerator.InstanceSeedGenerator;
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
	
	@Before
	public void setUp()
	{
		ProblemInstanceHelper.clearCache();
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
			
			assertEquals(1.0,r.getEmpiricalCost(defaultConfig, r.getInstancesRan(defaultConfig), 500),0.01);
			
			
			
			run = new ExistingAlgorithmRun(execConfig, runConfig, "0, 2 , 0 , 0, " + pisp.getSeed());
			r.append(run);
			
			assertEquals(2.0,r.getEmpiricalCost(defaultConfig, r.getInstancesRan(defaultConfig), 500),0.01);
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
		
		assertTrue("Should only see an the one configuration", runHistory.getAlgorithmInstanceSeedPairsRan(defaultConfig).equals(Collections.singleton(pisp)));
		assertTrue("Should have no runs", runHistory.getAlgorithmInstanceSeedPairsRan(otherConfig).equals(Collections.EMPTY_SET));
		
		
		
		
		
		
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
			runHistory.append(new RunningAlgorithmRun(execConfig, rc, 0, 0, 0, 0, new KillHandler() {

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
	
	
}
