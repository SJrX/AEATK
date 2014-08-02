package ca.ubc.cs.beta.bootstrap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.ExistingAlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aeatk.misc.bootstrap.ConfigurationDifference;
import ca.ubc.cs.beta.aeatk.misc.bootstrap.ConfigurationDifference.ChallengeResult;
import ca.ubc.cs.beta.aeatk.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aeatk.objectives.ObjectiveHelper;
import ca.ubc.cs.beta.aeatk.objectives.OverallObjective;
import ca.ubc.cs.beta.aeatk.objectives.RunObjective;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParamFileHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;
import ca.ubc.cs.beta.aeatk.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.ThreadSafeRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.ThreadSafeRunHistoryWrapper;

public class BootstrapTester {

	
	private List<ProblemInstance> pis;
	
	private AlgorithmExecutionConfiguration execConfig;
	
	private ParameterConfigurationSpace configSpace;
	
	private static SeedableRandomPool pool = new SeedableRandomPool(System.currentTimeMillis());
	
	@Before
	public void beforeTest()
	{
		pis = new ArrayList<ProblemInstance>();
		for(int i=0; i < 10; i++)
		{
			pis.add(new ProblemInstance("instance" + i));
		}
		
		configSpace = ParamFileHelper.getParamFileFromString("a [0,1] [0.5]\nb [0,1] [0.5]\n c [0,1] [0.5]\n"); 
		
		
		execConfig = new AlgorithmExecutionConfiguration("TEST", "TEST2", configSpace, false, false, 0);
		
		
		
	}
	
	@Test
	public void bootstrapTest() throws DuplicateRunException
	{
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		
		ParameterConfiguration randomConfig = configSpace.getRandomParameterConfiguration(r);
		
		int RUNS_TO_CREATE = 1;
		
		int SAMPLES  = 100;
		
		System.out.print("Actual Value,");
		for(ChallengeResult res : ChallengeResult.values())
		{
			System.out.print(res + ",");
		
		}
		
		
		System.out.println();
		
		final Map<Double, Map<ChallengeResult, AtomicInteger>>  resMap = new TreeMap<>();
		for(double kappa = 0.0; kappa<= 1.0; kappa+= 0.01)
		{
		
			/*
			EnumMap<ChallengeResult, AtomicInteger> resCount = new EnumMap<>(ChallengeResult.class);
			
		
			for(ChallengeResult res : ChallengeResult.values())
			{
		
				resCount.put(res, new AtomicInteger(0));
			}
			*/
			
			for(int j=0; j < SAMPLES; j++)
			{
				List<AlgorithmRunResult> runs = new ArrayList<>();
				
				ThreadSafeRunHistory rh = new ThreadSafeRunHistoryWrapper(new NewRunHistory());
				
				
				for(int i=0; i < RUNS_TO_CREATE; i++)
				{
					ProblemInstance pi = pis.get(r.nextInt(pis.size()));
					
					ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, i++);
					
					AlgorithmRunConfiguration incRC = new AlgorithmRunConfiguration(pisp,5, config,execConfig);
					AlgorithmRunConfiguration chalRC = new AlgorithmRunConfiguration(pisp,5, randomConfig,execConfig);
					
					AlgorithmRunResult incRun = new ExistingAlgorithmRunResult(incRC, RunStatus.SAT, 1, 0, 0 ,1);
					AlgorithmRunResult chalRun = new ExistingAlgorithmRunResult(chalRC, RunStatus.SAT, Math.random()+kappa, 0, 0 ,1);
					
					rh.append(incRun);
					rh.append(chalRun);
				}
	
			
				ObjectiveHelper helper = new ObjectiveHelper(RunObjective.RUNTIME, OverallObjective.MEAN10, OverallObjective.MEAN, 5); 
			
				
				double objective = helper.computeObjective(rh.getAlgorithmRunsExcludingRedundant(randomConfig));
				
				
				if(objective < 0.4 || objective > 1.4)
				{
					j--;
					continue;
				}
				
				double key = (((int) (objective*50)))/50.0;
				
				Map<ChallengeResult, AtomicInteger> result = resMap.get(key);
				if(result == null)
				{
					result = new EnumMap<ChallengeResult, AtomicInteger>(ChallengeResult.class);
					
					for(ChallengeResult c : ChallengeResult.values())
					{
						result.put(c, new AtomicInteger(0));
					}
					
					resMap.put(key, result);
				}
				
				
				
				result.get(ConfigurationDifference.compareChallengerWithIncumbent(rh, randomConfig, config, r, helper, 5)).incrementAndGet();

		
			}
			
			
			
		}
		
		//System.out.println(resMap);
		
		for(Entry<Double, Map<ChallengeResult, AtomicInteger>> en : resMap.entrySet())
		{
			System.out.print(en.getKey()+ "," );
			for(Entry<ChallengeResult, AtomicInteger> ent : en.getValue().entrySet())
			{
				System.out.print( ent.getValue() + ",");
			}
			System.out.println();
		}
		
		
		
	}
	
	@AfterClass
	public static void afterClass()
	{
		pool.logUsage();
	}
}
