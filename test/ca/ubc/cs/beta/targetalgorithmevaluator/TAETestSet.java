package ca.ubc.cs.beta.targetalgorithmevaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aeatk.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aeatk.exceptions.IllegalWrapperOutputException;
import ca.ubc.cs.beta.aeatk.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aeatk.misc.logback.MarkerFilter;
import ca.ubc.cs.beta.aeatk.misc.logging.LoggingMarker;
import ca.ubc.cs.beta.aeatk.misc.watch.AutoStartStopWatch;
import ca.ubc.cs.beta.aeatk.misc.watch.StopWatch;
import ca.ubc.cs.beta.aeatk.objectives.RunObjective;
import ca.ubc.cs.beta.aeatk.options.AbstractOptions;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParamFileHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;
import ca.ubc.cs.beta.aeatk.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.ThreadSafeRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.ThreadSafeRunHistoryWrapper;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.WaitableTAECallback;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.analytic.AnalyticFunctions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.analytic.AnalyticTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.analytic.AnalyticTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.blackhole.BlackHoleTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.blackhole.BlackHoleTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineAlgorithmRun;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc.IPCTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc.IPCTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc.IPCTargetAlgorithmEvaluatorOptions.EncodingMechanismOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc.IPCTargetAlgorithmEvaluatorOptions.IPCMechanism;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.ipc.reversetcpclient.IPCTAEClient;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.preloaded.PreloadedResponseTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.random.RandomResponseTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.random.RandomResponseTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.random.RandomResponseTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.debug.CheckForDuplicateRunConfigDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.debug.EqualTargetAlgorithmEvaluatorTester;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.functionality.OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.functionality.OutstandingEvaluationsWithAccessorTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.functionality.SimulatedDelayTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.functionality.TerminateAllRunsOnFileDeleteTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.functionality.portfolio.PortfolioRunKillingPolicy;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.functionality.portfolio.PortfolioTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.helpers.CompleteZeroSecondCutoffRunsTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.helpers.KillCaptimeExceedingRunsRunsTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.helpers.OutstandingRunLoggingTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.helpers.WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.prepostcommand.PrePostCommandErrorException;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.resource.BoundedTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.resource.NonBlockingAsyncTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.resource.PreemptingTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.resource.caching.runhistory.RunHistoryCachingTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.resource.forking.ForkingTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.resource.forking.ForkingTargetAlgorithmEvaluatorDecoratorPolicyOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.resource.forking.ForkingTargetAlgorithmEvaluatorDecoratorPolicyOptions.ForkingPolicy;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.safety.AbortOnCrashTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.safety.AbortOnFirstRunCrashTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.safety.CrashedSolutionQualityTransformingTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.safety.ResultOrderCorrectCheckerTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.safety.TimingCheckerTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.safety.VerifySATTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.exceptions.TargetAlgorithmAbortException;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.init.TargetAlgorithmEvaluatorBuilder;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.init.TargetAlgorithmEvaluatorLoader;
import ca.ubc.cs.beta.targetalgorithmevaluator.impl.SolQualSetTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.targetalgorithmevaluator.massiveoutput.MassiveOutputParamEchoExecutor;
import ca.ubc.cs.beta.targetalgorithmevaluator.targetalgos.CapitalForParamEchoExecutor;
import ca.ubc.cs.beta.targetalgorithmevaluator.targetalgos.DummyExecutor;
import ca.ubc.cs.beta.targetalgorithmevaluator.targetalgos.EnvironmentVariableEchoer;
import ca.ubc.cs.beta.targetalgorithmevaluator.targetalgos.FiveSecondSleepingParamEchoExecutor;
import ca.ubc.cs.beta.targetalgorithmevaluator.targetalgos.ParamEchoWalltimeExecutor;

@SuppressWarnings("unused")
public class TAETestSet {

	
	private static TargetAlgorithmEvaluator tae;
	
	private static AlgorithmExecutionConfiguration execConfig;
	
	private static ParameterConfigurationSpace configSpace;
	
	private static final int TARGET_RUNS_IN_LOOPS = 10;
	@BeforeClass
	public static void beforeClass()
	{
		//File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt");
		//configSpace = new ParamConfigurationSpace(paramFile);
	}
	
	PrintStream old;
	ByteArrayOutputStream bout;
	
	private static final SeedableRandomPool pool = new SeedableRandomPool(System.currentTimeMillis());
	
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
	
	@Before
	public void beforeTest()
	{
		System.out.flush();
		System.err.flush();
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt");
		configSpace = new ParameterConfigurationSpace(paramFile);
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
		

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
	}
	
	@AfterClass
	public static void afterClass()
	{
		pool.logUsage();
	}
	
	
	@After
	public void afterTest()
	{
		if(tae != null)
		{
			tae.notifyShutdown();
		} 
		tae = null;
	}
	@Test
	public void testPortfolioTargetAlgorithmEvaluatorDecorator()
	{
	    //Get analytical ADD tae.
	    TargetAlgorithmEvaluatorFactory factory = new AnalyticTargetAlgorithmEvaluatorFactory();
	    
	    AnalyticTargetAlgorithmEvaluatorOptions options = new AnalyticTargetAlgorithmEvaluatorOptions();
	    options.func = AnalyticFunctions.ADD;
	    
	    TargetAlgorithmEvaluator tae = factory.getTargetAlgorithmEvaluator(options);
	    
	    ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("x0 [0,10] [0]");
	    AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("", "", configSpace, false, true, 15);
	    
	    //Construct portfolio.
	    ParameterConfiguration config1 = configSpace.getDefaultConfiguration();
	    config1.put("x0", "2");
	    
	    ParameterConfiguration config2 = configSpace.getDefaultConfiguration();
        config2.put("x0", "5");
        
        ParameterConfiguration config3 = configSpace.getDefaultConfiguration();
        config3.put("x0", "8");
        
	    /**
	     * Test the addition of the default run.
	     */
        
        List<ParameterConfiguration> portfolio = Arrays.asList(config1,config2,config3);
        
        TargetAlgorithmEvaluator portfolioTAE = PortfolioTargetAlgorithmEvaluatorDecorator.constructParamConfigPortfolioTargetAlgorithmEvaluatorDecorator(tae, portfolio, RunObjective.RUNTIME, false, PortfolioRunKillingPolicy.SLOWERDIES);
        
	    AlgorithmRunConfiguration runConfig = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("test-instance"), 0), 15, configSpace.getDefaultConfiguration(), execConfig);
	    
	    List<AlgorithmRunResult> results = portfolioTAE.evaluateRun(runConfig);
	    AlgorithmRunResult result = results.get(0);
	    
	    System.out.println(results);
	    assertTrue(result.getRuntime() == Double.valueOf(config1.get("x0")));
	    
	    portfolioTAE = PortfolioTargetAlgorithmEvaluatorDecorator.constructParamConfigPortfolioTargetAlgorithmEvaluatorDecorator(tae, portfolio, RunObjective.RUNTIME, true, PortfolioRunKillingPolicy.SLOWERDIES);
        
        results = portfolioTAE.evaluateRun(runConfig);
        result = results.get(0);
        
        System.out.println(results);
        assertTrue(result.getRuntime() == Double.valueOf(configSpace.getDefaultValuesMap().get("x0")));
        
        /**
         * Test the breaking at duplicate runs.
         */
        
        ParameterConfiguration configDup3 = configSpace.getDefaultConfiguration();
        configDup3.put("x0", config3.get("x0"));
        
        ParameterConfiguration configDef = configSpace.getDefaultConfiguration();
        
        portfolio = Arrays.asList(config1,config2,config3,configDup3);        
        
        try
        {
            portfolioTAE = PortfolioTargetAlgorithmEvaluatorDecorator.constructParamConfigPortfolioTargetAlgorithmEvaluatorDecorator(tae, portfolio, RunObjective.RUNTIME, true, PortfolioRunKillingPolicy.SLOWERDIES);
        }
        catch(IllegalArgumentException e)
        {
            System.err.println(e);
        }
        
        
        portfolio = Arrays.asList(config1,config2,config3,configDef);
        portfolioTAE = PortfolioTargetAlgorithmEvaluatorDecorator.constructParamConfigPortfolioTargetAlgorithmEvaluatorDecorator(tae, portfolio, RunObjective.RUNTIME, true, PortfolioRunKillingPolicy.SLOWERDIES);
        try
        {
            results = portfolioTAE.evaluateRun(runConfig);
        }
        catch(IllegalStateException e)
        {
            System.err.println(e);
        }
	    
	}
	
	@Test
	public void testRunHistoryCachingDecorator()
	{
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("x0 [-5,10] [0]\n x1 [-0,15] [0]\n");
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 15);
		
		RandomResponseTargetAlgorithmEvaluatorFactory afact = new RandomResponseTargetAlgorithmEvaluatorFactory();
		
		TargetAlgorithmEvaluator tae = afact.getTargetAlgorithmEvaluator();
		
		ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);

		config.put("x0", "2.656650319997154");
		config.put("x1", "8.192989379593786");
		
		//config.put("x0", "3.1415");
		//config.put("x1", "2.275");
		
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i < 65535; i++)
		{
			sb.append(i);
			if(sb.length() > 25) 
			{
				break;
			}
		}
		String instanceName = sb.toString(); 
		
		List<AlgorithmRunConfiguration> rcs = new ArrayList<AlgorithmRunConfiguration>();
		
		for(int i=0; i <100; i++)
		{
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance(instanceName), r.nextInt(200000)), 15, configSpace.getRandomParameterConfiguration(r), execConfig);
			rcs.add(rc);
		}
		
		ThreadSafeRunHistory rh = new ThreadSafeRunHistoryWrapper(new NewRunHistory());
		
		StopWatch aWatch = new AutoStartStopWatch();
		
		tae = new RunHistoryCachingTargetAlgorithmEvaluatorDecorator(tae,rh);
		
		final List<AlgorithmRunResult> run1 = tae.evaluateRun(rcs);
		
		final List<AlgorithmRunResult> run2 = tae.evaluateRun(rcs);
		
		
		System.out.println(run1);
		try {
			rh.append(run1);
		} catch (DuplicateRunException e) {
		
		}
		
		final List<AlgorithmRunResult> run3 = tae.evaluateRun(rcs);
		
		
		for(int i=0; i < rcs.size(); i++)
		{
			assertEquals(run1.get(i).getRuntime(), run3.get(i).getRuntime(),0.0005);
		}
		

		for(int i=0; i <100; i++)
		{
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance(instanceName), 1L), 15, configSpace.getRandomParameterConfiguration(r), execConfig);
			rcs.add(rc);
		}
		
		final List<AlgorithmRunResult> run4 = tae.evaluateRun(rcs);
		
		final List<AlgorithmRunResult> run5 = tae.evaluateRun(rcs);
		
		boolean notEquals = false;
		
		for(int i=0; i < rcs.size(); i++)
		{
			if(i < 100)
			{
				assertEquals(run4.get(i).getRuntime(), run5.get(i).getRuntime(),0.0005);
				System.out.println(run4.get(i) +  " is the same as " + run5.get(i));
			} else
			{
				boolean myEquals = ((run4.get(i).getRuntime() - run5.get(i).getRuntime()) < 0.0005);
				//System.out.println("Test" + (run4.get(i).getRuntime() - run5.get(i).getRuntime() ) + " My Equals");
				if(!myEquals)
				{
					System.out.println(run4.get(i) +  " is different from " + run5.get(i));
				}
				notEquals |= myEquals;
			}
		}
		
		assertTrue("Expected some run is not equal: " , notEquals);
		System.out.println("Completed ");
		
		tae.notifyShutdown();
		
		
		
	}
	
	@Test
	public void testPreemptionTAE() throws InterruptedException
	{
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("x0 [-5,10] [0]\n x1 [-0,15] [0]\n");
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 15);
		
		RandomResponseTargetAlgorithmEvaluatorFactory afact = new RandomResponseTargetAlgorithmEvaluatorFactory();
		
		TargetAlgorithmEvaluator tae = afact.getTargetAlgorithmEvaluator();
		
		tae = new SimulatedDelayTargetAlgorithmEvaluatorDecorator(tae, 250, 2);
		
		
		tae = new BoundedTargetAlgorithmEvaluator(tae,20);
		
		tae = new PreemptingTargetAlgorithmEvaluator(tae);
		
		TargetAlgorithmEvaluator lowPriorityTAE = tae;
		
		lowPriorityTAE = ((PreemptingTargetAlgorithmEvaluator) tae).getLowPriorityTargetAlgorithmEvaluator();
		
		
		ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);

		config.put("x0", "2.656650319997154");
		config.put("x1", "8.192989379593786");
		
		//config.put("x0", "3.1415");
		//config.put("x1", "2.275");
		
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i < 65535; i++)
		{
			sb.append(i);
			if(sb.length() > 25) 
			{
				break;
			}
		}
		String instanceName = sb.toString(); 
		
		List<AlgorithmRunConfiguration> rcs = new ArrayList<AlgorithmRunConfiguration>();
		
		for(int i=0; i <100; i++)
		{
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance(instanceName), i), 15, configSpace.getRandomParameterConfiguration(r), execConfig);
			rcs.add(rc);
		}
		
		final StopWatch aWatch = new AutoStartStopWatch(); 
		
		synchronized(aWatch)
		{
			//Noop
		}
		
		TargetAlgorithmEvaluatorCallback taeCallback = new TargetAlgorithmEvaluatorCallback()
		{

			@Override
			public void onSuccess(List<AlgorithmRunResult> runs) {
				synchronized(aWatch)
				{
					System.out.println("LOW PRIORITY RUNS");
					System.out.println(runs);
					System.out.println("First batch of runs took: " + aWatch.stop() + "ms");
				}
			}

			@Override
			public void onFailure(RuntimeException e) {
				e.printStackTrace();
				
			}
			
		};
		
		
		
		lowPriorityTAE = new NonBlockingAsyncTargetAlgorithmEvaluatorDecorator(lowPriorityTAE,20);
		lowPriorityTAE = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(lowPriorityTAE);
		
		for(int i=0; i < rcs.size()/10; i++)
		{
			lowPriorityTAE.evaluateRunsAsync(rcs.subList(i, i+10), taeCallback);
		}
		
		Thread.sleep(1500);
		

		List<AlgorithmRunConfiguration> high = new ArrayList<AlgorithmRunConfiguration>();
		
		for(int i=0; i <1; i++)
		{
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance(instanceName), i+27), 15, configSpace.getRandomParameterConfiguration(r), execConfig);
			high.add(rc);
		}
		
		
		tae  = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		

		List<AlgorithmRunResult> runs = tae.evaluateRun(high);
		
		System.out.println("Completed High priority runs");
		System.out.println("Runs: " + runs);
		

		lowPriorityTAE.waitForOutstandingEvaluations();
		
		tae.close();
		
		lowPriorityTAE.close();
		
	}
	
	
	@Test
	public void testIPCClient()
	{
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("x0 [-5,10] [0]\n x1 [-0,15] [0]\n");
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 15);
		
		RandomResponseTargetAlgorithmEvaluatorFactory afact = new RandomResponseTargetAlgorithmEvaluatorFactory();
		
		TargetAlgorithmEvaluator tae = afact.getTargetAlgorithmEvaluator();
		
		ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);

		config.put("x0", "2.656650319997154");
		config.put("x1", "8.192989379593786");
		
		//config.put("x0", "3.1415");
		//config.put("x1", "2.275");
		
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i < 65535; i++)
		{
			sb.append(i);
			if(sb.length() > 25) 
			{
				break;
			}
		}
		String instanceName = sb.toString(); 
		
		List<AlgorithmRunConfiguration> rcs = new ArrayList<AlgorithmRunConfiguration>();
		
		for(int i=0; i <100; i++)
		{
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance(instanceName), 1L), 15, configSpace.getRandomParameterConfiguration(r), execConfig);
			rcs.add(rc);
		}
		
		StopWatch aWatch = new AutoStartStopWatch(); 
		List<AlgorithmRunResult> res = tae.evaluateRun(rcs);
		
		System.out.println(res);
		System.out.println("First batch of runs took: " + aWatch.stop() + "ms");
		
		tae.notifyShutdown();
		
		IPCTargetAlgorithmEvaluatorFactory tfact = new IPCTargetAlgorithmEvaluatorFactory();
		
		IPCTargetAlgorithmEvaluatorOptions opt = tfact.getOptionObject();
		
		opt.encodingMechanism = EncodingMechanismOptions.JAVA_SERIALIZATION;
		
		opt.ipcMechanism = IPCMechanism.REVERSE_TCP;
		
		opt.poolConnections = false;
		
		opt.execScriptOutput = false;
		StringBuilder bn = new StringBuilder();
		bn.append("java -cp ");
		bn.append(System.getProperty("java.class.path"));
		bn.append(" ");
		bn.append(IPCTAEClient.class.getCanonicalName());
		
		
		bn.append(" --log-level DEBUG --tae RANDOM --ipc-tae-client-port ");
		opt.execScript = bn.toString();
		//opt.execScriptOutput = true;
		TargetAlgorithmEvaluator itae = tfact.getTargetAlgorithmEvaluator(opt);

		itae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(itae);
		aWatch = new AutoStartStopWatch();
		System.out.println(itae.evaluateRun(rcs));
		System.out.println("Second batch of runs took: " + aWatch.stop() + "ms");
		
		itae.evaluateRunsAsync(rcs, new TargetAlgorithmEvaluatorCallback() {

			@Override
			public void onSuccess(List<AlgorithmRunResult> runs) {
				System.out.println("DONE");
				
			}

			@Override
			public void onFailure(RuntimeException e) {
				e.printStackTrace();
				
			}
			
		});
		
		itae.waitForOutstandingEvaluations();
		itae.notifyShutdown();
		
		
	}
	
	@Test
	/**
	 * This just tests to see if {@link ForkingTargetAlgorithmEvaluatorDecorator} does what it should.
	 */
	public void testForkWithQuickPolicy()
	{

		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("x0 [-5,10] [0]\n x1 [-0,15] [0]\n");
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 15);
		
		AnalyticTargetAlgorithmEvaluatorFactory ataef = new AnalyticTargetAlgorithmEvaluatorFactory();
		
		AnalyticTargetAlgorithmEvaluatorOptions options = ataef.getOptionObject();
		
		options.func = AnalyticFunctions.BRANINS;
		
		tae = ataef.getTargetAlgorithmEvaluator(options);
		
		tae = new SimulatedDelayTargetAlgorithmEvaluatorDecorator(tae, 1000, 3);
		
		options = ataef.getOptionObject();
		
		options.func = AnalyticFunctions.BRANINS;
		
		TargetAlgorithmEvaluator slaveTAE  = ataef.getTargetAlgorithmEvaluator(options);
		
		ForkingTargetAlgorithmEvaluatorDecoratorPolicyOptions fOptions = new ForkingTargetAlgorithmEvaluatorDecoratorPolicyOptions();
		fOptions.fPolicy = ForkingPolicy.DUPLICATE_ON_SLAVE_QUICK;
		tae = new ForkingTargetAlgorithmEvaluatorDecorator(tae,slaveTAE, fOptions);
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);

			config.put("x0", "2.656650319997154");
			config.put("x1", "8.192989379593786");
			
			//config.put("x0", "3.1415");
			//config.put("x1", "2.275");
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), 1L), 15, config, execConfig);
			runConfigs.add(rc);
			
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		AutoStartStopWatch watch = new AutoStartStopWatch();
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		watch.stop();
		
		assertTrue("Run should have taken less than 1 second", watch.time() < 1000);
		
		System.out.println("Runs: " + runs);
		for(AlgorithmRunResult run : runs)
		{
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			
			System.out.println(config.get("x0") + "," + config.get("x1") + "=>" + run.getRuntime());

		}
		
		
		 runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);

			config.put("x0", "2.756650319997154");
			config.put("x1", "8.192989379593786");
			
			//config.put("x0", "3.1415");
			//config.put("x1", "2.275");
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), 1L), 15, config, execConfig);
			runConfigs.add(rc);
			
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		watch = new AutoStartStopWatch();
		runs = tae.evaluateRun(runConfigs);
		
		watch.stop();
		
		assertTrue("Run should have taken more than 1 second", watch.time() > 1000);
		
		System.out.println("Runs: " + runs);
		for(AlgorithmRunResult run : runs)
		{
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			
			System.out.println(config.get("x0") + "," + config.get("x1") + "=>" + run.getRuntime());

		}
		
		tae.notifyShutdown();
		
	}
	
	/**
	 * This just tests to see if {@link ForkingTargetAlgorithmEvaluatorDecorator} does what it should.
	 */
	@Test
	public void testFork()
	{

		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		TargetAlgorithmEvaluator slaveTAE = new BlackHoleTargetAlgorithmEvaluator(new BlackHoleTargetAlgorithmEvaluatorOptions());
		
		ForkingTargetAlgorithmEvaluatorDecoratorPolicyOptions fOptions = new ForkingTargetAlgorithmEvaluatorDecoratorPolicyOptions();
		fOptions.fPolicy = ForkingPolicy.DUPLICATE_ON_SLAVE;
		tae = new ForkingTargetAlgorithmEvaluatorDecorator(slaveTAE, tae, fOptions);
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		
		for(AlgorithmRunResult run : runs)
		{
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunStatus().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());

		}
		
		tae.close();
	}
	
	
	@Test
	/**
	 * This just tests to see if {@link ForkingTargetAlgorithmEvaluatorDecorator} does what it should.
	 * 
	 * See bug #2055
	 * 
	 */
	public void testForkWithQuickPolicyBoundedThreads()
	{

		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("x0 [-5,10] [0]\n x1 [-0,15] [0]\n");
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 15);
		
		AnalyticTargetAlgorithmEvaluatorFactory ataef = new AnalyticTargetAlgorithmEvaluatorFactory();
		
		AnalyticTargetAlgorithmEvaluatorOptions options = ataef.getOptionObject();
		
		options.func = AnalyticFunctions.BRANINS;
		
		tae = ataef.getTargetAlgorithmEvaluator(options);
		
		options = ataef.getOptionObject();
		
		options.func = AnalyticFunctions.BRANINS;
		
		
		RandomResponseTargetAlgorithmEvaluatorFactory rFact = new RandomResponseTargetAlgorithmEvaluatorFactory();
		
		RandomResponseTargetAlgorithmEvaluatorOptions rOptions = rFact.getOptionObject();
		rOptions.sleepInternally = 500;
		TargetAlgorithmEvaluator slaveTAE  =  rFact.getTargetAlgorithmEvaluator(rOptions);
		
		
		//slaveTAE = new SimulatedDelayTargetAlgorithmEvaluatorDecorator(slaveTAE, 1000, 0.05);
		
		slaveTAE = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(slaveTAE);
		
		ForkingTargetAlgorithmEvaluatorDecoratorPolicyOptions fOptions = new ForkingTargetAlgorithmEvaluatorDecoratorPolicyOptions();
		fOptions.fPolicy = ForkingPolicy.DUPLICATE_ON_SLAVE_QUICK;
		tae = new ForkingTargetAlgorithmEvaluatorDecorator(tae,slaveTAE, fOptions);
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);

			config.put("x0", "2.656650319997154");
			config.put("x1", "8.192989379593786");
			
			//config.put("x0", "3.1415");
			//config.put("x1", "2.275");
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), 1L), 15, config, execConfig);
			runConfigs.add(rc);
			
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		AutoStartStopWatch watch = new AutoStartStopWatch();
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		watch.stop();
		
		assertTrue("Run should have taken less than 1 second", watch.time() < 1000);
		
		System.out.println("Runs: " + runs);
		for(AlgorithmRunResult run : runs)
		{
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			
			System.out.println(config.get("x0") + "," + config.get("x1") + "=>" + run.getRuntime());

		}
		
		
		 runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		final int NUM_RUNS_TO_DO = 100;
		for(int i=0; i < NUM_RUNS_TO_DO; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);

			//config.put("x0", "2.756650319997154");
			//config.put("x1", "8.192989379593786");
			
			//config.put("x0", "3.1415");
			//config.put("x1", "2.275");
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), 1L), 15, config, execConfig);
			runConfigs.add(rc);
			
		}
		
		System.out.println("Number of Threads: " + ManagementFactory.getThreadMXBean().getAllThreadIds().length);
		System.out.println("Performing " + runConfigs.size() + " runs");
		watch = new AutoStartStopWatch();
		for(int i=0; i < NUM_RUNS_TO_DO; i++)
		{
			tae.evaluateRunsAsync(runConfigs.subList(i, i+1), new TargetAlgorithmEvaluatorCallback() {

				@Override
				public void onSuccess(List<AlgorithmRunResult> runs) {
					EnumMap<RunStatus,AtomicInteger> results = new EnumMap<RunStatus, AtomicInteger>(RunStatus.class);
					
					for(RunStatus rs : RunStatus.values())
					{
						results.put(rs, new AtomicInteger(0));
					}
					
					for(AlgorithmRunResult run : runs)
					{
						results.get(run.getRunStatus()).incrementAndGet();
					}
					
					System.out.println(results);
					/*for(Entry<RunStatus, AtomicInteger> ent : results.entrySet())
					{
						
					}*/
				}

				@Override
				public void onFailure(RuntimeException e) {
					e.printStackTrace();
				}
				
			});
			
		}
		//runs = tae.evaluateRun(runConfigs);
		tae.waitForOutstandingEvaluations();

		System.out.println(tae.toString());
		System.out.println("Runs took : " + watch.time()  + " ms to execute");
		System.out.println("Number of Threads: " + ManagementFactory.getThreadMXBean().getAllThreadIds().length);
		//System.out.println(Arrays.deepToString(ManagementFactory.getThreadMXBean().getThreadInfo(ManagementFactory.getThreadMXBean().getAllThreadIds())));
		System.out.println("Slave TAE outstanding:" + slaveTAE.getNumberOfOutstandingRuns());
		int numberOfOutstanding = tae.getNumberOfOutstandingRuns();
		System.out.println("TAE outstanding:" + numberOfOutstanding );

		if(numberOfOutstanding > 0)
		{
			fail("Number of outstanding runs should have been zero, but instead got " + numberOfOutstanding);
		}
			
	
		
				
		
		//watch.stop();
		
		//assertTrue("Run should have taken more than 1 second", watch.time() > 1000);
		
		System.out.println("Runs: " + runs);
		/*
		for(AlgorithmRunResult run : runs)
		{
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			
			System.out.println(config.get("x0") + "," + config.get("x1") + "=>" + run.getRuntime());

		}*/
		
		
		slaveTAE.waitForOutstandingEvaluations();
		System.out.println("Slave TAE took total time: " + watch.stop() + " ms  to execute ");
		try 
		
		{
			if(watch.time() > 2000)
			{
				
				fail("Expected time should have been less than two seconds, but was " + watch.time() + " ms");
			}
		} finally
		{
			tae.notifyShutdown();
		}
		
		
	}
	
	
	/**
	 * This just tests to see if ParamEchoExecutor does what it should
	 */
	@Test
	public void testMirror()
	{

		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		
		for(AlgorithmRunResult run : runs)
		{
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunStatus().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());

		}
	}
	
	
	/**
	 * Tests whether warnings are generated for Algorithms exceeding there runtime
	 */
	@Test
	public void testTimingWarningGeneratorTAE()
	{
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(SleepyParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		tae = new EchoTargetAlgorithmEvaluator();
		
		((EchoTargetAlgorithmEvaluator) tae).wallClockTime = 50;
		
		
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 10; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("runtime", ""+(i));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 0.01, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		TargetAlgorithmEvaluator tae = new TimingCheckerTargetAlgorithmEvaluator( TAETestSet.tae);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		PrintStream out = System.out;
		System.setOut(new PrintStream(bout));
		
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		System.setOut(out);
		System.out.println(bout.toString());
		
		
		assertTrue(bout.toString().contains("Algorithm Run Result reported wallclock time of 50.0 (secs) that exceeded it's cutoff time of "));
		assertTrue(bout.toString().contains("exceeded it's cutoff time of 0.01 (secs) by 1.99 (secs)"));
		assertTrue(bout.toString().contains("exceeded it's cutoff time of 0.01 (secs) by 3.99 (secs)"));
		assertTrue(bout.toString().contains("exceeded it's cutoff time of 0.01 (secs) by 5.99 (secs)"));
		assertTrue(bout.toString().contains("exceeded it's cutoff time of 0.01 (secs) by 7.99 (secs)"));
		
		
		for(AlgorithmRunResult run : runs)
		{
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunStatus().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());

		}
		
		tae.notifyShutdown();
	}
	
	

	/**
	 * Tests that none of the values in the CSV are zero.
	 * This test could be made more advanced by looking that the runs dispatched never exceeds known bounds, but for now this will be good enough
	 */
	@Test
	
	public void testRunLoggingTAEDecorator()
	{
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoWalltimeExecutor.class.getCanonicalName());
		
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFileWalltime.txt");
		configSpace = new ParameterConfigurationSpace(paramFile);
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		CommandLineTargetAlgorithmEvaluatorOptions cliOpts = CommandLineTargetAlgorithmEvaluatorFactory.getCLIOPT();
		
		cliOpts.concurrentExecution = true;
		cliOpts.cores = 10;
		cliOpts.logAllCallStrings = true;
		cliOpts.logAllProcessOutput = true;
		TargetAlgorithmEvaluator tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE(cliOpts);
		
		
		
		
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 100; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("runtime", (1 + Math.random()) + "" );
			config.put("walltime", (2 + Math.random()) + "");
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration( new ProblemInstanceSeedPair(new ProblemInstance("TestInstance","SLEEP"), Long.valueOf(config.get("seed"))), 0.01, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		File f1;
		File f2;
		try {
			long s =  System.currentTimeMillis();
			f1 = File.createTempFile("junit_runlogging_" + s +"-", "dispatched.csv");
			f2 = File.createTempFile("junit_runlogging_" + s +"-", "outstanding.csv");
		} catch (IOException e) {
			throw new IllegalStateException("Argh");
		}
		
		tae = new OutstandingRunLoggingTargetAlgorithmEvaluatorDecorator(tae, f1.getAbsolutePath() , 0.01, "Dispatched");
		
		
		tae = new BoundedTargetAlgorithmEvaluator(tae,15);
		
		tae = new OutstandingRunLoggingTargetAlgorithmEvaluatorDecorator(tae, f2.getAbsolutePath(), 0.01, "Outstanding");
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		
		
		//PrintStream out = System.out;
		//System.setOut(new PrintStream(bout));
		
		for(int i=0; i < 10; i++)
		{
			
			System.err.println("Starting runs: " + 10);
			tae.evaluateRunsAsync(runConfigs.subList(10*i, 10*(i+1)), new TargetAlgorithmEvaluatorCallback() {

				@Override
				public void onSuccess(List<AlgorithmRunResult> runs) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onFailure(RuntimeException e) {
					e.printStackTrace();
					
				}
				
			});
		}
		tae.waitForOutstandingEvaluations();
		//System.setOut(out);
		//System.out.println(bout.toString());
		
		tae.notifyShutdown();
		
		verifyRunPerformanceCSV(f1);
		verifyRunPerformanceCSV(f2);
		
		
		
		
		System.out.println("Files deleted");
		f1.delete();
		f2.delete();
	}
	
	private void verifyRunPerformanceCSV(File f)
	{
		System.out.println( "CSV File is in : " + f.getAbsolutePath());
		try {
			BufferedReader fread = new BufferedReader(new FileReader(f));
			
			try {
				while(fread.ready())
				{
					String line = fread.readLine();
					
					
					String[] cells = line.split(",");
					
					for(String s : cells)
					{
						try {
							double d = Double.valueOf(s);
							
							
							if(d < 0)
							{
								fail("Line contains negative values this shouldn't happen");
							}
						} catch(NumberFormatException e)
						{
							//This is okay we are only concerned that there are no negative numbers
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {

			throw new IllegalStateException("This shouldn't happen");
		}
		return;
	}
	/**
	 * This just tests to see if EchoTargetAlgorithmEvaluator matches the CLI Version
	 */
	@Test
	public void testCLIAndDirectEquality()
	{
		
	
		
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		TargetAlgorithmEvaluator tae = new EqualTargetAlgorithmEvaluatorTester(TAETestSet.tae, new EchoTargetAlgorithmEvaluator());
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		
		for(AlgorithmRunResult run : runs)
		{
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunStatus().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());

		}
	}
	
	
	
	/**
	 * This just tests to see if ParamEchoExecutor does what it should
	 */
	@Test
	public void testMirrorWithAdditionalData()
	{
		
	
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutorWithGibberish.class.getCanonicalName());
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		int i=0; 
		for(AlgorithmRunResult run : runs)
		{
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunStatus().name());
			//This executor should not have any additional run data
			
			try {
				assertTrue(!run.getAdditionalRunData().equals("")); 
			} catch(AssertionError e)
			{
				System.out.println(run);
				throw e;
			}
			

		}
	}
	
	
	/**
	 * Tests that the runCount actually increments over time
	 */
	@Test
	public void testRunCountIncrement()
	{
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
			
			
			
			List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(2);
			for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
			{
				runConfigs.clear();
				ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
				if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
				{
					//Only want good configurations
					i--;
					continue;
				} else
				{
					AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
					runConfigs.add(rc);
				}
				tae.evaluateRun(runConfigs);
				
				
				assertEquals(i+1,tae.getRunCount());
				
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

	@Test
	public void testABORT()
	{
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved","ABORT");
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(AlgorithmRunConfiguration run : runConfigs)
		{
			
			try {
				tae.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 
				//This is what we wanted 
				continue;
			}
			fail("Should not have reached this point, algorithm should have thrown an error");

		}
	}
		
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalCaptimeException()
	{
		
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(100);
	
		ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
		
		AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), -1, config, execConfig);
		runConfigs.add(rc);
		
		System.out.println("Performing " + runConfigs.size() + " runs");
	}
	
	
	
	/**
	 * Checks to ensure that a misbehaiving wrapper has it's logical
	 * output corrected (so a SAT run should always have a time < cutoffTime)
	 * 
	 * This test fails currently
	 */
	@Ignore("Functionality not implemented yet, this test will fail. The feature that it is testing was pushed back")
	@Test
	public void testRuntimeGreaterThanCapTime()
	{
		
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		
		double cutoffTime = 300;
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("solved", "SAT");
			
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), cutoffTime, config, execConfig);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		
			
			
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		for(AlgorithmRunResult run : runs)
		{
			
			if(run.getRuntime() >= cutoffTime)
			{
				assertEquals(RunStatus.TIMEOUT, run.getRunStatus());
			} else
			{
				assertEquals(run.getRunStatus(), RunStatus.valueOf(run.getAlgorithmRunConfiguration().getParameterConfiguration().get("solved")));
				assertDEquals(run.getAlgorithmRunConfiguration().getParameterConfiguration().get("runtime"), run.getRuntime(),0.05);
			}
			
			
			
		}
			
			
	}
	
	/**
	 * Tests that an algorithm that reports crash actually triggers an abort exception
	 */
	@Test
	public void testAbortOnCrashTAE()
	{
		
		
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved","CRASHED");
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		
		for(AlgorithmRunConfiguration run : runConfigs)
		{
			
			try {
				 tae.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 
				fail("Should not have crashed here, unwrapped TAE should not be aborting");
			}
			
			continue;

		}
		
		
		TargetAlgorithmEvaluator abortOnCrash = new AbortOnCrashTargetAlgorithmEvaluator(tae);
		
		for(AlgorithmRunConfiguration run : runConfigs)
		{
			try {
				 abortOnCrash.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 
				//This is what we wanted
				
				continue;
			}
			
			fail("We should have gotten the a TargetAlgorithmAbortException");

		}
		
		
		
		
		
	}


	
	/**
	 * Tests that the FirstRunCrashTAE behaves as expected 
	 * (i.e. if the first run crashes it aborts, otherwise it treats as crashes)
	 * This test (unlike the next, has a first run actually crash)
	 * 
	 */
	@Test
	public void testAbortOnFirstRunCrashTAEfirstIsACrash()
	{
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved","CRASHED");
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
		}
		
		
		
		
		for(AlgorithmRunConfiguration run : runConfigs)
		{
			
			try {
					tae.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 
				fail("Should not have crashed here, unwrapped TAE should not be aborting");
			}
			
			continue;

		}
		
		
		TargetAlgorithmEvaluator abortOnCrash = new AbortOnFirstRunCrashTargetAlgorithmEvaluator(tae);
		
		boolean firstRun = true;
		for(AlgorithmRunConfiguration run : runConfigs)
		{
			try {
				abortOnCrash.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 

				if(firstRun) 
				{
					//This is what we wanted
					firstRun = false;
				} else
				{
					fail("Expected only the first run to trigger this");
				}
				continue;
			}
			
			
			if(firstRun)
			{
				fail("Expected the first to run trigget a target algorithm abort exception");
			}
		}
	}
	
	
	/**
	 * Tests that the FirstRunCrashTAE behaves as expected 
	 * (i.e. if the first run crashes it aborts, otherwise it treats as crashes)
	 * This tests has the first run not actually crash
	 * 
	 */
	@Test
	public void testAbortOnFirstRunCrashTAEfirstIsSAT()
	{
		
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(i == 0 )
			{
				config.put("solved","SAT");
			} else
			{
				config.put("solved","CRASHED");
			}
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
		}
		
		
		
		for(AlgorithmRunConfiguration run : runConfigs)
		{
			
			try {
				tae.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 
				fail("Should not have crashed here, unwrapped TAE should not be aborting");
			}
			
			continue;

		}
		
		TargetAlgorithmEvaluator abortOnCrash = new AbortOnFirstRunCrashTargetAlgorithmEvaluator(tae);

		for(AlgorithmRunConfiguration run : runConfigs)
		{
			try {
				abortOnCrash.evaluateRun(run);
			} catch(TargetAlgorithmAbortException e)
			{ 
				fail("Only the first run should be able to trigger this and it was a SAT");				
			}
		}
	}

	/**
	 * Tests to see if we runs with no matching output get treated as CRASHED and logged correctly
	 */
	@Test
	public void testCrashIfNoMatchingOutput()
	{
	
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(DoNothingExecutor.class.getCanonicalName());

		AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500); 
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		
		
		ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
		config.put("solved","SAT");
		AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
		runConfigs.add(rc);
		
		TargetAlgorithmEvaluator tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		for(AlgorithmRunResult run : runs)
		{
			assertEquals(RunStatus.CRASHED,run.getRunStatus());
		}
			
		
	}
	
	@Test
	public void testSatAliases()
	{
		AlgorithmExecutionConfiguration execConfig;
		
		ParameterConfigurationSpace configSpace;
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		File paramFile = TestHelper.getTestFile("paramFiles/paramAliasEchoParamFile.txt");
			configSpace = new ParameterConfigurationSpace(paramFile);
			
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamAliasEchoExecutor.class.getCanonicalName());
		
		CommandLineTargetAlgorithmEvaluatorFactory tfact = new CommandLineTargetAlgorithmEvaluatorFactory();
		
		CommandLineTargetAlgorithmEvaluatorOptions opts = tfact.getOptionObject();
		
		opts.logAllProcessOutput = true;
		
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
			
		tae = new AbortOnCrashTargetAlgorithmEvaluator(tfact.getTargetAlgorithmEvaluator(opts));
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
				
		
		
		
		for(String alias : RunStatus.SAT.getAliases())
		{
			
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", alias);
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
		}
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		
		for(AlgorithmRunResult run : runs)
		{
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(RunStatus.getAutomaticConfiguratorResultForKey(config.get("solved")), RunStatus.SAT);
			assertEquals("",run.getAdditionalRunData()); //No Additional Run Data Expected

		}
		
	}
	

	@Test
	public void testUnSatAliases()
	{
		AlgorithmExecutionConfiguration execConfig;
		
		ParameterConfigurationSpace configSpace;
		
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		File paramFile = TestHelper.getTestFile("paramFiles/paramAliasEchoParamFile.txt");
			configSpace = new ParameterConfigurationSpace(paramFile);
			
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamAliasEchoExecutor.class.getCanonicalName());
		
		
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
			
		tae = new AbortOnCrashTargetAlgorithmEvaluator(CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE());
			
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
				
		
		
		
		for(String alias : RunStatus.UNSAT.getAliases())
		{
			
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", alias);
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
		}
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		
		for(AlgorithmRunResult run : runs)
		{
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(RunStatus.getAutomaticConfiguratorResultForKey(config.get("solved")), RunStatus.UNSAT);
			
			assertEquals("",run.getAdditionalRunData()); //No Additional Run Data Expected

		}
		
	}
	
	
	@Test
	public void testExceptionWithDuplicateRunConfigs()
	{
		AlgorithmExecutionConfiguration execConfig;
		
		ParameterConfigurationSpace configSpace;
		
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		File paramFile = TestHelper.getTestFile("paramFiles/paramAliasEchoParamFile.txt");
			configSpace = new ParameterConfigurationSpace(paramFile);
			
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamAliasEchoExecutor.class.getCanonicalName());
		
		
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
			
		tae = new CheckForDuplicateRunConfigDecorator(new AbortOnCrashTargetAlgorithmEvaluator(CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE()), true);
			
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
				
		
		
		
		for(String alias : RunStatus.UNSAT.getAliases())
		{
			
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", alias);
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
		}

		List<AlgorithmRunResult> runs;
		System.out.println("Performing " + runConfigs.size() + " runs");
		try {
			runs = tae.evaluateRun(runConfigs);
		} catch(IllegalStateException e)
		{
			//Unexpected
			e.printStackTrace();
			
			throw e;
			
		}

		runConfigs.add(runConfigs.get(0));
		
		
		try {
			runs = tae.evaluateRun(runConfigs);
			fail("Expected Exception");
		} catch(IllegalStateException e)
		{
			System.out.println("Got exception which was expected: YAY:");
			e.printStackTrace();
			
		}
		
	
		
	}
	
	

	@Test
	/**
	 * This tests to ensure that the runs that don't have a Run Result are treated as crashed and the right error message appears
	 */
	public void testRegexMatchButNoRunResultOutput()
	{
		AlgorithmExecutionConfiguration execConfig;
		
		ParameterConfigurationSpace configSpace;
		
		
		File paramFile = TestHelper.getTestFile("paramFiles/paramAliasEchoParamFile.txt");
			configSpace = new ParameterConfigurationSpace(paramFile);
		
			
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(RegexMatchingButIncompleteOutputExecutor.class.getCanonicalName());
		
		
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
			
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
				
		
		
		
		for(String alias : RunStatus.UNSAT.getAliases())
		{
			
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", alias);
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
		}
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(bout);
		
		PrintStream oldOut = System.out;
		
		System.setOut(pw);
		List<AlgorithmRunResult> runs = new ArrayList<AlgorithmRunResult>();
		try {
		runs = tae.evaluateRun(runConfigs);
		} finally
		{
			System.setOut(oldOut);
			System.out.println(bout.toString());
			pw.close();
			
		}
		
		assertEquals(runs.size(), runConfigs.size());
		assertTrue(bout.toString().contains("Most likely the Algorithm did not report a result string as one of"));
		
		
		for(AlgorithmRunResult run : runs)
		{
			assertEquals( RunStatus.CRASHED, run.getRunStatus());
		}
		
	}
	

	@Test
	/**
	 * This tests to ensure that the runs that don't have a Run Result are treated as crashed and the right error message appears
	 */
	public void testRegexMatchButInvalidNumber()
	{
		AlgorithmExecutionConfiguration execConfig;
		
		ParameterConfigurationSpace configSpace;
		
		
		File paramFile = TestHelper.getTestFile("paramFiles/paramAliasEchoParamFile.txt");
			configSpace = new ParameterConfigurationSpace(paramFile);
			
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(RegexMatchingButInvalidNumberOutputExecutor.class.getCanonicalName());
		
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
			
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
		
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
				
		
		
		
		for(String alias : RunStatus.UNSAT.getAliases())
		{
			
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", alias);
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
		}
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(bout);
		
		PrintStream oldOut = System.out;
		
		System.setOut(pw);
		List<AlgorithmRunResult> runs = new ArrayList<AlgorithmRunResult>();
		try {
		runs = tae.evaluateRun(runConfigs);
		} finally
		{
			System.setOut(oldOut);
			System.out.println(bout.toString());
			pw.close();
			
		}
		
		assertEquals(runs.size(), runConfigs.size());
		
		
		
		for(AlgorithmRunResult run : runs)
		{
			assertEquals(RunStatus.CRASHED, run.getRunStatus());
		}
		
		assertTrue(bout.toString().contains("Most likely one of the values of runLength, runtime, quality could not be parsed as a Double, or the seed could not be parsed as a valid long"));
	}
	
	@Test
	/**
	 * This tests to ensure that the runs that don't have a Run Result are treated as crashed and the right error message appears
	 */
	public void testRegexMatchButMissingCommas()
	{
		AlgorithmExecutionConfiguration execConfig;
		
		ParameterConfigurationSpace configSpace;
		
		
		File paramFile = TestHelper.getTestFile("paramFiles/paramAliasEchoParamFile.txt");
			configSpace = new ParameterConfigurationSpace(paramFile);
			
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(RegexMatchingButMissingOutputExecutor.class.getCanonicalName());
		
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
			
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
				
		
		
		
		for(String alias : RunStatus.UNSAT.getAliases())
		{
			
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", alias);
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
		}
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(bout);
		
		PrintStream oldOut = System.out;
		
		System.setOut(pw);
		List<AlgorithmRunResult> runs = new ArrayList<AlgorithmRunResult>();
		try {
		runs = tae.evaluateRun(runConfigs);
		} finally
		{
			System.setOut(oldOut);
			System.out.println(bout.toString());
			pw.close();
			
		}
		
		assertEquals(runs.size(), runConfigs.size());
		
		for(AlgorithmRunResult run : runs)
		{
			assertEquals( RunStatus.CRASHED, run.getRunStatus());
		}
		
		assertTrue(bout.toString().contains("Most likely the algorithm did not specify all of the required outputs that is <solved>,<runtime>,<runlength>,<quality>,<seed>"));
	}
	
	
	
	/**
	 * This just tests to see if ParamEchoExecutor does what it should
	 * This tested for out of memory issues with the regular expression but
	 * didn't conclusively find anything.
	 * 
	 */
	@Test
	@Ignore
	public void testMassiveOutput()
	{
		
		MarkerFilter.deny(LoggingMarker.FULL_PROCESS_OUTPUT);
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(MassiveOutputParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(1);
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		System.out.println("Suppressing Output");
		PrintStream out = System.out;
		System.setOut(new PrintStream(new NullOutputStream()));
		List<AlgorithmRunResult> runs;
		try {
		 runs = tae.evaluateRun(runConfigs);
		} finally{
			System.setOut(out);
		}
		
		
		for(AlgorithmRunResult run : runs)
		{
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunStatus().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());

		}
	}
	
	
	/**
	 * This tests to see if the Random white space executor and various other supported
	 * output formats are matched correctly
	 */
	@Test
	public void testWhiteSpaceInExecutorStart()
	{
		
	
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(RandomWhitespaceParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
		
		
		for(AlgorithmRunResult run : runs)
		{
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunStatus().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());

		}
	}
	
	/**
	 * This tests to see if the Random white space executor and various other supported
	 * output formats are matched correctly
	 * This also tests for capitalization as in Issue 1842.
	 */
	@Test
	public void testCapitizalionOfFor()
	{
		
	
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(CapitalForParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		for(int i =0; i < this.TARGET_RUNS_IN_LOOPS; i++)
		{
			List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
			
			
			for(AlgorithmRunResult run : runs)
			{
				ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
				assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
				assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
				assertDEquals(config.get("quality"), run.getQuality(), 0.1);
				assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
				assertEquals(config.get("solved"), run.getRunStatus().name());
				//This executor should not have any additional run data
				assertEquals("",run.getAdditionalRunData());
			}
		}
	}
	
	
	/**
	 * This tests to make sure that VerifySATTargetAlgorithmEvaluator fires warnings when it is suppose to
	 * 
	 * 
	 * 
	 * 
	 */
	@Test
	public void testVerifySATTargetAlgorithmEvaluator()
	{
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", "SAT");
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("SAT",1,new HashMap<String, Double>(),"SAT"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
		
			config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", "SAT");
			rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("UNSAT",2,new HashMap<String, Double>(),"UNSAT"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
			
			config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", "UNSAT");
			rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("UNSAT",2,new HashMap<String, Double>(),"UNSAT"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
			
			
			config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", "UNSAT");
			rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("SAT",1,new HashMap<String, Double>(),"SAT"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
			
			
			
			
			config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", "SAT");
			rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("SATISFIABLE",3,new HashMap<String, Double>(),"SATISFIABLE"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
		
			config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", "SAT");
			rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("UNSATISFIABLE",4,new HashMap<String, Double>(),"UNSATISFIABLE"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
			
			config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", "UNSAT");
			rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("UNSATISFIABLE",4,new HashMap<String, Double>(),"UNSATISFIABLE"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
			
			
			config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", "UNSAT");
			rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("SATISFIABLE",3,new HashMap<String, Double>(),"SATISFIABLE"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
			
			
			
			
			
			
			
			
			config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", "TIMEOUT");
			rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("SAT",1,new HashMap<String, Double>(),"SAT"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
		
			config = configSpace.getRandomParameterConfiguration(r);
			config.put("solved", "SAT");
			rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("UNKNOWN",2,new HashMap<String, Double>(),"UNKNOWN"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
			runConfigs.add(rc);
			
		}	
			
			
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		TargetAlgorithmEvaluator tae =  new VerifySATTargetAlgorithmEvaluator(new EchoTargetAlgorithmEvaluator());
		
		
		
		for(AlgorithmRunConfiguration rc : runConfigs)
		{
			
			
			
			startOutputCapture();
			List<AlgorithmRunResult> runs = tae.evaluateRun(rc);
			String output = stopOutputCapture();
			//System.out.println("<<<<<\n" + output+"\n<<<<<<");
			
			
			AlgorithmRunResult run = runs.get(0);
			
			
			switch(run.getRunStatus())
			{
				case SAT:
					if(run.getAlgorithmRunConfiguration().getProblemInstanceSeedPair().getProblemInstance().getInstanceName().startsWith("SAT"))
					{
						assertFalse(output.contains("Mismatch occured between instance specific information"));
					} else
					{
						assertTrue(output.contains("Mismatch occured between instance specific information"));
					}
					
					break;
				case UNSAT:
					
					if(run.getAlgorithmRunConfiguration().getProblemInstanceSeedPair().getProblemInstance().getInstanceName().startsWith("UNSAT"))
					{
				
						assertFalse(output.contains("Mismatch occured between instance specific information"));
					} else
					{
						assertTrue(output.contains("Mismatch occured between instance specific information"));
					}
					
					break;
			
				default:
					assertFalse(output.contains("Mismatch occured between instance specific information"));	
			}
			ParameterConfiguration config  = run.getAlgorithmRunConfiguration().getParameterConfiguration();
			assertDEquals(config.get("runtime"), run.getRuntime(), 0.1);
			assertDEquals(config.get("runlength"), run.getRunLength(), 0.1);
			assertDEquals(config.get("quality"), run.getQuality(), 0.1);
			assertDEquals(config.get("seed"), run.getResultSeed(), 0.1);
			assertEquals(config.get("solved"), run.getRunStatus().name());
			//This executor should not have any additional run data
			assertEquals("",run.getAdditionalRunData());

		}
	}
	
	
	@Test
	/**
	 * Tests the SAT consistency checker
	 */
	public void testSATConsistencyChecker()
	{
	
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), ParameterConfigurationSpace.getSingletonConfigurationSpace(), false, false, 500);
		
		Map<String, AbstractOptions> taeOptionsMap = TargetAlgorithmEvaluatorLoader.getAvailableTargetAlgorithmEvaluators();
		
		TargetAlgorithmEvaluatorOptions opts = new TargetAlgorithmEvaluatorOptions();
		System.out.println(taeOptionsMap);
		opts.targetAlgorithmEvaluator = "PRELOADED";
		opts.checkSATConsistency = true;
		opts.checkSATConsistencyException = false;
		opts.boundRuns = false;
		opts.retryCount = 0;
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[TIMEOUT=4],[CRASHED=2],[TIMEOUT=3],[CRASHED=1],[SAT=1],[SAT=2],[UNSAT=2],[UNSAT=3],[TIMEOUT=4],[CRASHED=2],[TIMEOUT=3],[CRASHED=1],[SAT=1],[SAT=2],[UNSAT=2],[UNSAT=3]";
		TargetAlgorithmEvaluator tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts,  false, taeOptionsMap);
		
		ProblemInstance satPi = new ProblemInstance("SATInstance");
		ProblemInstance unsatPi = new ProblemInstance("UNSATInstance");
		
		
		ProblemInstanceSeedPair satPiOne = new ProblemInstanceSeedPair(satPi, 1);
		ProblemInstanceSeedPair satPiTwo = new ProblemInstanceSeedPair(satPi, 2);
		ProblemInstanceSeedPair satPiThree = new ProblemInstanceSeedPair(satPi, 3);
		ProblemInstanceSeedPair satPiFour = new ProblemInstanceSeedPair(satPi, 4);
		
		ProblemInstanceSeedPair unSatPiOne = new ProblemInstanceSeedPair(unsatPi, 1);
		ProblemInstanceSeedPair unSatPiTwo = new ProblemInstanceSeedPair(unsatPi, 2);
		ProblemInstanceSeedPair unSatPiThree = new ProblemInstanceSeedPair(unsatPi, 3);
		ProblemInstanceSeedPair unSatPiFour = new ProblemInstanceSeedPair(unsatPi, 4);
		
		AlgorithmRunConfiguration satPiOneRC = new AlgorithmRunConfiguration(satPiOne, 0.1, ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(), execConfig);
		AlgorithmRunConfiguration satPiTwoRC = new AlgorithmRunConfiguration(satPiTwo, 0.1, ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(),execConfig);
		AlgorithmRunConfiguration unSatPiOneRC = new AlgorithmRunConfiguration(unSatPiOne, 0.1, ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(),execConfig);
		AlgorithmRunConfiguration unSatPiTwoRC = new AlgorithmRunConfiguration(unSatPiTwo, 0.1, ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(),execConfig);
		
		
		AlgorithmRunConfiguration satPiThreeRC = new AlgorithmRunConfiguration(satPiThree, 0.1, ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(),execConfig);
		AlgorithmRunConfiguration satPiFourRC = new AlgorithmRunConfiguration(satPiFour, 0.1, ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(),execConfig);
		AlgorithmRunConfiguration unSatPiThreeRC = new AlgorithmRunConfiguration(unSatPiThree, 0.1, ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(),execConfig);
		AlgorithmRunConfiguration unSatPiFourRC = new AlgorithmRunConfiguration(unSatPiFour, 0.1, ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(),execConfig);
		
		
		
		List<AlgorithmRunConfiguration> rcs = Arrays.asList(satPiOneRC, satPiTwoRC, unSatPiOneRC, unSatPiTwoRC, satPiThreeRC, satPiFourRC, unSatPiThreeRC, unSatPiFourRC);
		
		tae.evaluateRun(rcs);
		
		
		rcs = Arrays.asList(satPiOneRC, satPiTwoRC, unSatPiOneRC, unSatPiTwoRC, satPiThreeRC,  unSatPiThreeRC, satPiFourRC, unSatPiFourRC);
		
		

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(bout);
		
		PrintStream oldOut = System.out;
		
		System.setOut(pw);
		
		try {
			tae.evaluateRun(rcs);
		} finally
		{
		   System.setOut(oldOut);
		   System.out.println(bout.toString());
		   pw.close();
		}
		
		//The actual string value isn't important, and if this test is failing because a log message changed you can simply update this string 
		assertTrue("Expected error message to appear",bout.toString().contains("SAT/UNSAT discrepancy detected on problem instance: Instance:UNSATInstance"));
		assertTrue("Expected error message to appear",bout.toString().contains("SAT/UNSAT discrepancy detected on problem instance: Instance:SATInstance"));
		
		
		
		tae.notifyShutdown();
		
		
		opts.checkSATConsistencyException = true;
		
		tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts,  false, taeOptionsMap);
		rcs = Arrays.asList(satPiOneRC, satPiTwoRC, unSatPiOneRC, unSatPiTwoRC, satPiThreeRC,  unSatPiThreeRC, satPiFourRC, unSatPiFourRC);
		boolean exception = false;
		try {
			tae.evaluateRun(rcs);
		} catch(TargetAlgorithmAbortException e)
		{
			exception = true;
		}
		assertTrue("Expected that a TargetAlgorithmAbortException would have occured", exception);
		tae.notifyShutdown();
	}
	
	@Test
	/**
	 * Tests the Pre and Post commands work
	 */
	public void testPrePostCommandTAE()
	{
	
		Map<String, AbstractOptions> taeOptionsMap = TargetAlgorithmEvaluatorLoader.getAvailableTargetAlgorithmEvaluators();
		
		TargetAlgorithmEvaluatorOptions opts = new TargetAlgorithmEvaluatorOptions();
		System.out.println(taeOptionsMap);
		opts.targetAlgorithmEvaluator = "PRELOADED";
		opts.checkSATConsistency = true;
		opts.checkSATConsistencyException = false;
		opts.boundRuns = false;
		opts.uncleanShutdownCheck = false;
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ReturnCodeTester.class.getCanonicalName()).append(" ");
		
		
		opts.prePostOptions.preCommand = b.toString() + "0";
		opts.prePostOptions.postCommand = b.toString() + "0";
		
		
		/***
		 * Normal Test
		 */
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream pw = new PrintStream(bout);		
		PrintStream oldOut = System.out;
		System.setOut(pw);
		TargetAlgorithmEvaluator tae;
		try {
			 tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts,  false, taeOptionsMap);
		} finally
		{
		   System.setOut(oldOut);
		   System.out.println(bout.toString());
		   pw.close();
		}
		
		assertTrue(bout.toString().contains("Command completed"));

		
		bout = new ByteArrayOutputStream();
		pw = new PrintStream(bout);		
		oldOut = System.out;
		System.setOut(pw);
		
		try {
			tae.notifyShutdown();
		} finally
		{
		   System.setOut(oldOut);
		   System.out.println(bout.toString());
		   pw.close();
		   
		}
		
		/**
		 * Error Test (no exception)
		 */
		opts.prePostOptions.preCommand = b.toString() + "227";
		opts.prePostOptions.postCommand = b.toString() + "228";
		
		bout = new ByteArrayOutputStream();
		pw = new PrintStream(bout);		
		oldOut = System.out;
		System.setOut(pw);
	
		try {
			 tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts,  false, taeOptionsMap);
		} finally
		{
		   System.setOut(oldOut);
		   System.out.println(bout.toString());
		   pw.close();
		   
		}
		
		assertTrue(bout.toString().contains("Got a non-zero return code from process: 227"));

		
		bout = new ByteArrayOutputStream();
		pw = new PrintStream(bout);		
		oldOut = System.out;
		System.setOut(pw);
		
		try {
			tae.notifyShutdown();
		} finally
		{
		   System.setOut(oldOut);
		   System.out.println(bout.toString());
		   pw.close();
		}
		
		assertTrue(bout.toString().contains("Got a non-zero return code from process: 228"));
		
		/**
		 * Error Test (exception on startup)
		 */
		
		
		opts.prePostOptions.preCommand = b.toString() + "2";
		opts.prePostOptions.exceptionOnError = true;
		bout = new ByteArrayOutputStream();
		pw = new PrintStream(bout);		
		oldOut = System.out;
		System.setOut(pw);
		boolean exceptionOccurred = true;
		try {
			try {
				 tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts,  false, taeOptionsMap);
			} finally
			{
			   System.setOut(oldOut);
			   System.out.println(bout.toString());
			   pw.close();
			   tae.notifyShutdown();
			}
		} catch(PrePostCommandErrorException e)
		{
			exceptionOccurred = true;
		}
		
		assertTrue("Expect exception", exceptionOccurred);
		
		
		
		
	}
	
	/**
	 * Test invalid wrapper outputs
	 */
	@Test
	public void testInvalidArguments()
	{
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		Map<String, AbstractOptions> taeOptionsMap = TargetAlgorithmEvaluatorLoader.getAvailableTargetAlgorithmEvaluators();
		
		TargetAlgorithmEvaluatorOptions opts = new TargetAlgorithmEvaluatorOptions();
		System.out.println(taeOptionsMap);
		opts.targetAlgorithmEvaluator = "PRELOADED";
		opts.checkSATConsistency = true;
		opts.checkSATConsistencyException = false;
		opts.boundRuns = false;
		
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[SAT=-1],";
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).quality = 0.0;
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).runLength = 0.0;
		
		TargetAlgorithmEvaluator tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts,  false, taeOptionsMap);
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		try {
			List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
			fail("Expected exception to be thrown: " + runs);
		} catch(IllegalWrapperOutputException e)
		{
			System.out.println(e.getMessage());
		}
		

		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[SAT=1],";
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).quality = Double.NaN;
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).runLength = 0.0;
		
		tae.notifyShutdown();
		tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts,  false, taeOptionsMap);
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		try {
			List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
			fail("Expected exception to be thrown: " + runs);
		} catch(IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
		}
		
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[SAT=1],";
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).quality = 0;
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).runLength = -2;
		
		tae.notifyShutdown();
		tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts,  false, taeOptionsMap);
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		try {
			List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
			fail("Expected exception to be thrown: " + runs);
		} catch(IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
		}
		tae.notifyShutdown();
		
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[SAT=1],";
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).quality = 0;
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).runLength = Double.NaN;
		
		tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts,  false, taeOptionsMap);
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		try {
			List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
			fail("Expected exception to be thrown: " + runs);
		} catch(IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
		}
			
		
		
		tae.notifyShutdown();
		
		

		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[SAT=1],";
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).quality = 0;
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).runLength = -1;
		
		tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts,  false, taeOptionsMap);
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		try {
			List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
			
		} catch(IllegalArgumentException e)
		{
			fail("Unexpected Exception Thrown: " + e);
		}
		tae.notifyShutdown();
		

		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).preloadedResponses="[SAT=" + Double.POSITIVE_INFINITY + "],";
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).quality = 0;
		((PreloadedResponseTargetAlgorithmEvaluatorOptions) taeOptionsMap.get("PRELOADED")).runLength = -1;

		tae = TargetAlgorithmEvaluatorBuilder.getTargetAlgorithmEvaluator(opts,  false, taeOptionsMap);
		
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		try {
			List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
			
		} catch(IllegalArgumentException e)
		{
			fail("Unexpected exception");
			
		}
		
		
		tae.notifyShutdown();
	}
	
	@Test
	public void testOrderCheckingDecorator()
	{
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < TARGET_RUNS_IN_LOOPS; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		
		//Test the TAE when we shuffle responses
		RandomResponseTargetAlgorithmEvaluatorOptions randOpts = new RandomResponseTargetAlgorithmEvaluatorOptions();
		long seed = System.currentTimeMillis();
		System.out.println("Order Checking Decorator used seed" + seed);
		randOpts.seed =seed;
		randOpts.shuffleResponses = true;
		
		TargetAlgorithmEvaluator tae = new RandomResponseTargetAlgorithmEvaluator(randOpts);
		
		tae = new ResultOrderCorrectCheckerTargetAlgorithmEvaluatorDecorator(tae);
		try {
			try {
				List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
				fail("Expected Exception to have occured");
			} catch(IllegalStateException e)
			{
				System.out.println("GOOD: " + e.getMessage());
			}
	
			
			final AtomicBoolean taeCompletedSuccessfully = new AtomicBoolean();
			TargetAlgorithmEvaluatorCallback callback = new TargetAlgorithmEvaluatorCallback()
			{
	
				@Override
				public void onSuccess(List<AlgorithmRunResult> runs) {
					taeCompletedSuccessfully.set(true);
				}
	
				@Override
				public void onFailure(RuntimeException t) {
					System.out.println("GOOD ASYNC: " + t.getMessage());	
				}
				
			};
			
			WaitableTAECallback wait = new WaitableTAECallback(callback);
				
			tae.evaluateRunsAsync(runConfigs, wait);
			
			wait.waitForCompletion();
			
			assertFalse("TAE Should not have completed successfully", taeCompletedSuccessfully.get());
		} finally
		{
			tae.notifyShutdown();
		}
		
		
		//Test the TAE when we don't shuffle
		try {
			randOpts = new RandomResponseTargetAlgorithmEvaluatorOptions();
			
			System.out.println("Order Checking Decorator used seed" + seed);
			randOpts.seed =seed;
			
			tae = new RandomResponseTargetAlgorithmEvaluator( randOpts);
			
			tae = new ResultOrderCorrectCheckerTargetAlgorithmEvaluatorDecorator(tae);
			
			try {
				List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
				System.out.println("GOOD: Completed");
			} catch(IllegalStateException e)
			{
				throw e;
			}
	
			
			final AtomicBoolean taeCompletedSuccessfully = new AtomicBoolean();
			TargetAlgorithmEvaluatorCallback callback = new TargetAlgorithmEvaluatorCallback()
			{
	
				@Override
				public void onSuccess(List<AlgorithmRunResult> runs) {
					taeCompletedSuccessfully.set(true);
				}
	
				@Override
				public void onFailure(RuntimeException t) {
					taeCompletedSuccessfully.set(false);
					t.printStackTrace();
				}
				
			};
			
			WaitableTAECallback wait = new WaitableTAECallback(callback);
				
			tae.evaluateRunsAsync(runConfigs, wait);
			
			wait.waitForCompletion();
			
			assertTrue("TAE Should not have completed successfully", taeCompletedSuccessfully.get());
			
		} finally
		{
			tae.notifyShutdown();
		}
		
		
	}
	
	
	@Test
	/**
	 * This tests that the CLI TAE Frequency is being respected more or less.
	 * 
	 * This is related to issue https://mantis.sjrx.net/view.php?id=2040
	 * 
	 * 
	 * 
	 */
	public void testObserverFrequency()
	{
	
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path").replaceAll("jar:.*", "jar:"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		final List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 100; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.concurrentExecution = true;
		options.cores = 100;
		options.logAllCallStrings = true;
		options.observerFrequency = 250;
		
		
		tae = fact.getTargetAlgorithmEvaluator( options);		
		tae = new BoundedTargetAlgorithmEvaluator(tae,100);
		

		final AtomicInteger obsCount = new AtomicInteger(0);
		TargetAlgorithmEvaluatorRunObserver tObs = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) {
				obsCount.incrementAndGet();
				
			}
			
		};
		
		StopWatch stopWatch = new AutoStartStopWatch();
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs,tObs);
		
		stopWatch.stop();
		System.out.println(obsCount.get() + " vs " + stopWatch.time() / 1000.0);
		
		long msPerObs = stopWatch.time() / obsCount.get();
		
		
		assertTrue("Expected milli-seconds per observation to be greater than set 250 got: " + msPerObs ,msPerObs>=250 );
		//obsCount.get() / stopWatch.time() 
		
	
	}
	
	@Test
	/**
	 * This tests for that the CLI will return eventually
	 * This is related to issue https://mantis.sjrx.net/view.php?id=1675
	 * 
	 * This test roughly tries to simulate a deadlock, if you look at the commit 0702803124e3513b8b5479b8ae5391d2df5ba38a the changes will show you where the deadlocks were
	 * occurring.
	 * 
	 */
	public void testDeadLockinCommandLineTargetAlgorithmEvaluatorSimpleRuns()
	{
	
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path").replaceAll("jar:.*", "jar:"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		final List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 100; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.concurrentExecution = true;
		options.cores = 4;
		options.logAllCallStrings = true;
		
		tae = fact.getTargetAlgorithmEvaluator( options);		
		tae = new BoundedTargetAlgorithmEvaluator(tae,4);
		
		
		
		
		final AtomicBoolean finishedRuns = new AtomicBoolean(false);
		Runnable run = new Runnable()
		{
			public void run()
			{
				for(int i=0; i < 10; i++)
				{
					List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
				}
			
				finishedRuns.set(true);
			}
		};
		
		
		Executors.newSingleThreadExecutor(new SequentiallyNamedThreadFactory("DeadLock JUnit Test")).submit(run);
		
		
		
		
			//This 45 second sleep is probably incredibly sensitive.

		for(int i=0; i < 600; i++)
		{
			try {
				Thread.sleep(1000);
				if(finishedRuns.get())
				{
					break;
				}
				
				if(i == 40)
				{
					System.err.println("IF THIS TEST IS STILL OUTPUTTING THEN THE 45 SECOND SLEEP IS TOO LITTLE");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
		assertTrue("Deadlock probably occured", finishedRuns.get());	
	}
	
	
	
	@Test
	/**
	 * This tests for that the CLI will return eventually
	 * This is related to issue https://mantis.sjrx.net/view.php?id=1675
	 * 
	 * This test roughly tries to simulate a deadlock, if you look at the commit 0702803124e3513b8b5479b8ae5391d2df5ba38a the changes will show you where the deadlocks were
	 * occurring.
	 * 
	 */
	public void testDeadLockinCommandLineTargetAlgorithmEvaluator()
	{
	
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path").replaceAll("jar:.*", "jar:"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		final List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 100; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		System.out.println("Performing " + runConfigs.size() + " runs");
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.concurrentExecution = true;
		options.cores = 100;
		options.logAllCallStrings = true;
		
		tae = fact.getTargetAlgorithmEvaluator( options);		
		tae = new BoundedTargetAlgorithmEvaluator(tae,100);
		
		
		
		
		final AtomicBoolean finishedRuns = new AtomicBoolean(false);
		Runnable run = new Runnable()
		{
			public void run()
			{
				for(int i=0; i < 10; i++)
				{
					List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs);
				}
			
				finishedRuns.set(true);
			}
		};
		
		
		Executors.newSingleThreadExecutor(new SequentiallyNamedThreadFactory("DeadLock JUnit Test")).submit(run);
		
		
		
		
			//This 45 second sleep is probably incredibly sensitive.

		for(int i=0; i < 600; i++)
		{
			try {
				Thread.sleep(1000);
				if(finishedRuns.get())
				{
					break;
				}
				
				if(i == 40)
				{
					System.err.println("IF THIS TEST IS STILL OUTPUTTING THEN THE 45 SECOND SLEEP IS TOO LITTLE");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
		assertTrue("Deadlock probably occured", finishedRuns.get());	
	}
	
	@Test
	/**
	 * This tests if a deadlock occurs when we try and resubmit runs in a onSuccess Method
	 */
	public void testBlockingTAEResubmitRunsHandler()
	{
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		RandomResponseTargetAlgorithmEvaluatorFactory fact = new RandomResponseTargetAlgorithmEvaluatorFactory();
		
		RandomResponseTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		options.persistent = true;
		TargetAlgorithmEvaluator tae = fact.getTargetAlgorithmEvaluator( options);
		
		
		tae = new BoundedTargetAlgorithmEvaluator(tae, 1);
		
		final List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		
		final TargetAlgorithmEvaluator tae2 = tae;
		
		final List<AlgorithmRunConfiguration> runConfigs2 = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs2.add(rc);
			}
		}
		
		
		
		
		
		final CountDownLatch latch = new CountDownLatch(1); 
		
		tae.evaluateRunsAsync(runConfigs, new TargetAlgorithmEvaluatorCallback()
		{

			@Override
			public void onSuccess(List<AlgorithmRunResult> runs) {

				System.out.println(runs);
				
				
				System.out.println(tae2.evaluateRun(runConfigs2));
				
				
				latch.countDown();
				
			}

			@Override
			public void onFailure(RuntimeException e) {
				e.printStackTrace();
				
			}
			
		});
		
		
		try {
			System.out.println("Deadlock if this is the last thing you see");
			latch.await();
		} catch (InterruptedException e1) {
			Thread.currentThread().interrupt();
			fail();
			return;
		}
		
	}
	
	
	@Test
	/**
	 * Schedules 100 runs against the TAE and very quick kills them it then measures how long it takes to submit.
	 * 
	 */
	public void testBoundedTargetAlgorithmEvaluatorKillingSpeed()
	{
		final Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 3000);
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 100;
		options.cores = 2;
		
		TargetAlgorithmEvaluator tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		tae = new BoundedTargetAlgorithmEvaluator(tae,10);
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(100);
		for(int i=0; i < 20; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime","2");
			

			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))),  config,execConfig);
				runConfigs.add(rc);
			}
		}
		
		
		
		TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
		{

			private boolean killedByDecorator = false;
			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) 
			{
				
				double sum = 0;
			
			
				for(AlgorithmRunResult run : runs)
				{
					if(run.getAlgorithmRunConfiguration().getProblemInstanceSeedPair().getSeed() % 100 % 19 != 0)
					{
						run.kill();
					}
					

					if(run.getRunStatus() == RunStatus.KILLED && !killedByDecorator)
					{
						if(run.getAdditionalRunData().equals(BoundedTargetAlgorithmEvaluator.KILLED_BY_DECORATOR_ADDL_RUN_INFO))
						{
							System.err.println(run.getResultLine());
							killedByDecorator = true;
						}
					}
					
				}
				

				
			}
			
		};
		
		StopWatch watch2 = new AutoStartStopWatch();
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs, obs);
		
		int killedCount = 0;
		for(AlgorithmRunResult run : runs)
		{
			
			if(run.getRunStatus().equals(RunStatus.KILLED))
			{
				killedCount++;
			}
			
			
			//System.out.println(run);
		}
		System.out.println(watch2.stop());
		
		System.out.println(killedCount);
		assertTrue("Expected time for Bounded to be less than 5 seconds", watch2.time() < 5000 );
		
	}
	
	
	@Test
	/**
	 * Schedules a set of runs in the form of 3,1,3,1 on a bound of 2x, if the Bounding is done properly this should be doable in under 5 seconds,
	 * if not then it might take about 8 seconds.
	 */
	public void testBoundedTAESubmissionSpeed()
	{
		//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 50;
		options.cores = 2;
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		tae = new BoundedTargetAlgorithmEvaluator(tae,2);
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		for(int i=0; i < 4; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if( i % 2 == 0)
			{
				config.put("runtime", "3");
			} else
			{
				config.put("runtime","1");
			}

			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 3000, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		
		
		StopWatch watch = new AutoStartStopWatch();
		cliTAE.evaluateRun(runConfigs);
		System.out.println(watch.stop());
		assertTrue("Expected time for CLI Direct to be less than 5 seconds", watch.time() < 5000 );
		
		StopWatch watch2 = new AutoStartStopWatch();
		tae.evaluateRun(runConfigs);
		System.out.println(watch2.stop());
		assertTrue("Expected time for Bounded to be less than 5 seconds", watch2.time() < 5000 );
		
	}
	
	
	@Test
	/**
	 * Schedules a set of runs in the form of {8,1,1,1}, {5}  on a bound of 4x, if the Bounding is done properly this should be doable in under 10 seconds 
	 * if not then it might take about 13 seconds.
	 */
	public void testBoundedTAESubmissionSpeedOnCompletion()
	{
		//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 50;
		options.cores = 4;
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		tae = new BoundedTargetAlgorithmEvaluator(tae,4);
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		for(int i=0; i < 5; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if( i == 0)
			{
				config.put("runtime", "8");
			} else if (i == 4)
			{
				config.put("runtime","5");
			} else
			{
				config.put("runtime","1");
			}

			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration( new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 3000, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		
		

		StopWatch watch2 = new AutoStartStopWatch();
		tae.evaluateRunsAsync(runConfigs.subList(0,4), new TargetAlgorithmEvaluatorCallback() {

			@Override
			public void onSuccess(List<AlgorithmRunResult> runs) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFailure(RuntimeException e) {
				e.printStackTrace();
				
			}
		
		});
		
		tae.evaluateRunsAsync(runConfigs.subList(4,5), new TargetAlgorithmEvaluatorCallback() {

			@Override
			public void onSuccess(List<AlgorithmRunResult> runs) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFailure(RuntimeException e) {
				e.printStackTrace();
				
			}
		
		});
		
		
		
		
		tae.waitForOutstandingEvaluations();
		
		System.out.println(watch2.stop());
		assertTrue("Expected time for Bounded to be less than 10 seconds", watch2.time() < 10000 );
		
	}
	
	
	@Test
	/**
	 * Schedules 4 runs with 2 seconds each on 2 cores. The CLI TAE should internally ensure that only two are executed at any given time, if it takes more than 5 seconds (suggesting only one core), 
	 * or less than 4 seconds suggesting more than 2, then we fail.
	 */
	public void testCLIInternallyBoundsRuns()
	{
		//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFileWalltime.txt");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(paramFile);
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 50;
		options.cores = 2;
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator( fact.getTargetAlgorithmEvaluator( options));	
		TargetAlgorithmEvaluator cliTAE = tae;
		//tae = new BoundedTargetAlgorithmEvaluator(tae,2,execConfig);
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		for(int i=0; i < 4; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime","2");
			

			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 3000, config,execConfig);
				runConfigs.add(rc);
			}
		}
		
		
		
		StopWatch watch = new AutoStartStopWatch();
		for(int i=0; i < 4; i++)
		{
			cliTAE.evaluateRunsAsync(runConfigs.subList(i,i+1), new TargetAlgorithmEvaluatorCallback()
			{

				@Override
				public void onSuccess(List<AlgorithmRunResult> runs) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onFailure(RuntimeException e) {
					e.printStackTrace();
					
				}
				
			});
		}
		//cliTAE.evaluateRun(runConfigs);
		cliTAE.waitForOutstandingEvaluations();
		
		System.out.println(watch.stop());
		assertTrue("Expected time for CLI Direct to be greater than 3 seconds", watch.time() > 4000 );
		
		assertTrue("Expected time for CLI Direct to be less than 5 seconds", watch.time() < 5000 );
		
		
	}
	

	@Test
	/**
	 * Wraps a bunch of TAEs together and checks to see if an Illegal State Exception happens
	 */
	public void testBoundedTAEOrderOfCallsObserverPreserved()
	{
		
	
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		
		try 
		{
			//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
			
			File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFileWalltime.txt");
			ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(paramFile);
			
			AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
			
			
			CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
			CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
			
			options.logAllCallStrings = true;
			options.logAllProcessOutput = true;
			options.concurrentExecution = true;
			options.observerFrequency = 50;
			options.cores = 100;
			
			tae = fact.getTargetAlgorithmEvaluator( options);	
			TargetAlgorithmEvaluator cliTAE = tae;
			
	
			tae = new BoundedTargetAlgorithmEvaluator(tae,100);
			tae = new BoundedTargetAlgorithmEvaluator(tae,100);
			tae = new BoundedTargetAlgorithmEvaluator(tae,100);
			tae = new BoundedTargetAlgorithmEvaluator(tae,100);
	
			
			List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
			for(int i=0; i < 100; i++)
			{
				ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
				
				config.put("runtime", String.valueOf(1 + (i%10)));
				
				if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
				{
					//Only want good configurations
					i--;
					continue;
				} else
				{
					AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 3000, config, execConfig);
					runConfigs.add(rc);
				}
			}
			
			
			
			StopWatch watch = new AutoStartStopWatch();
			System.out.println(watch.stop());
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			PrintStream pout = new PrintStream(bout);
			
			
			
			
			System.out.println("Turning off STDOUT");
			final PrintStream origOut = System.out;
			System.setOut(pout);
			
			tae.evaluateRun(runConfigs, new TargetAlgorithmEvaluatorRunObserver()
			{
	
				final long startTime = System.currentTimeMillis();
				int numCompleted = 0;
				int calls = 0;
				@Override
				public void currentStatus(List<? extends AlgorithmRunResult> runs) {
					//if(Math.random() > 0.95)
					//System.out.println("Called");
					calls++;
					int complete = 0;
					for(AlgorithmRunResult run : runs)
					{
						if(run.isRunCompleted())
						{
							complete++;
						}
					}
					if(numCompleted < complete)
					{
						numCompleted = complete;
						origOut.println("Status: " + numCompleted + " out of " + runs.size() + " calls: " + calls);
						
					}
					
					
				}
					
				
			}
			);
			
			System.setOut(origOut);
			System.out.println("Outputting Everything...");
			String output = bout.toString();
			System.out.println(output);
			
	
			System.out.println(watch.stop());
			if(output.contains("ERROR"))
			{
				fail("Output contained some error this is unexpected: " + output );
			}
		} finally
		{ 	//Tests need to be cleaned up, reset the execConfig object
			execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), TAETestSet.configSpace, false, false, 0.01);
		}
	}
	
	
	@Test
	/**
	 * Wraps a bunch of TAEs together and checks to see if an Illegal State Exception happens
	 */
	public void testBoundedTAEShutdownExceptionHandled() throws InterruptedException 
	{
	
		//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
		final Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 50;
		options.cores = 1;
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		

		tae = new BoundedTargetAlgorithmEvaluator(tae,1);
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		
		Runnable run = new Runnable()
		{
			public void run()
			{
				List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
				for(int i=0; i < 4; i++)
				{
					ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
					
					config.put("runtime", String.valueOf(1 + (i%10)));
					
					if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
					{
						//Only want good configurations
						i--;
						continue;
					} else
					{
						AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 3000, config, execConfig);
						runConfigs.add(rc);
					}
				}
				
				try {
				
				tae.evaluateRun(runConfigs);
				} catch(RuntimeException e)
				{
					System.out.println("Got Exception:");
					e.printStackTrace();
					System.out.println("This is okay:");
					tae.evaluateRun(runConfigs);
					
					if(Thread.interrupted())
					{
						System.out.println("Interrupted");
					} else
					{
						System.out.println("Not Interrupted");
					}
					//System.out.println("TEST");
				}
				
				
				
			}
		};
		
		
		

		Thread t = new Thread(run);
		t.start();
		
		Thread.sleep(2048);
		
		t.interrupt();
		Thread.sleep(1024);
		assertEquals("Number of outstanding requests should be zero",0, tae.getNumberOfOutstandingEvaluations());
		
	}
	
	

	@Test
	public void testKillFileDecorator()
	{
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(FiveSecondSleepingParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 50);
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.cores = 1;
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		
		TargetAlgorithmEvaluator taeUnity = new KillCaptimeExceedingRunsRunsTargetAlgorithmEvaluatorDecorator(tae, 1.1);
		
		
		
		File f;
		try {
			f = File.createTempFile("unitTest", "testKillFileDecorator");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("What happened?");
			return;
		}
		
		final File fDelete = f;
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Deleting File");
				fDelete.delete();
				
			}
			
		});
		t.start();
		taeUnity = new TerminateAllRunsOnFileDeleteTargetAlgorithmEvaluatorDecorator(taeUnity, f);
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		double runtime = 50;
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime",String.valueOf(runtime));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 50, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		long startTime = System.currentTimeMillis();
		taeUnity.evaluateRun(runConfigs);
		long endTime = System.currentTimeMillis();
		if(endTime - startTime > 5000)
		{
			fail("This test took too long to run");
		}
	}
	
	
	
	
	
	@Test
	public void testSimulateDelayScaling()
	{
		//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(ParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		options.cores = 2;
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		
		TargetAlgorithmEvaluator taeFourth = new SimulatedDelayTargetAlgorithmEvaluatorDecorator(cliTAE, 20, 0.25);
		TargetAlgorithmEvaluator taeHalf = new SimulatedDelayTargetAlgorithmEvaluatorDecorator(cliTAE, 20, 0.5);
		TargetAlgorithmEvaluator taeUnity = new SimulatedDelayTargetAlgorithmEvaluatorDecorator(cliTAE, 20, 1);
		TargetAlgorithmEvaluator taeDouble = new SimulatedDelayTargetAlgorithmEvaluatorDecorator(cliTAE, 20, 2);
		TargetAlgorithmEvaluator taeFour = new SimulatedDelayTargetAlgorithmEvaluatorDecorator(cliTAE, 20, 4);
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		double runtime = 2;
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime",String.valueOf(runtime));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 3000, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		
		
		checkExceptedObserverCount(taeFourth, 0.25, runConfigs, runtime, 20);
		checkExceptedObserverCount(taeHalf, 0.5, runConfigs, runtime, 20);
		checkExceptedObserverCount(taeUnity, 1.0, runConfigs, runtime, 20);
		checkExceptedObserverCount(taeDouble, 2, runConfigs, runtime, 20);
		checkExceptedObserverCount(taeFour, 4, runConfigs, runtime, 20);
		
		/*
		StopWatch watch2 = new AutoStartStopWatch();
		tae.evaluateRun(runConfigs);
		System.out.println(watch2.stop());
		assertTrue("Expected time for Bounded to be less than 5 seconds", watch2.time() < 5000 );
		*/
	}
	
	@Test
	public void testNullConfigurationSpace()
	{
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(DummyExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), ParameterConfigurationSpace.getNullConfigurationSpace(), false, false, 0.01);
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.cores = 1;
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		
		TargetAlgorithmEvaluator taeUnity = new KillCaptimeExceedingRunsRunsTargetAlgorithmEvaluatorDecorator(tae, 1.1);
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		double runtime = 50;
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = ParameterConfigurationSpace.getNullConfigurationSpace().getDefaultConfiguration();
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), 1), 1.5, config, execConfig);
			runConfigs.add(rc);
		}
		
		long startTime = System.currentTimeMillis();
		taeUnity.evaluateRun(runConfigs);
		long endTime = System.currentTimeMillis();
		if(endTime - startTime > 5000)
		{
			fail("This test took too long to run");
		}
	}
	
	
	
	@Test
	public void testKillingRunDecorator()
	{
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(FiveSecondSleepingParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.cores = 1;
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		
		TargetAlgorithmEvaluator taeUnity = new KillCaptimeExceedingRunsRunsTargetAlgorithmEvaluatorDecorator(tae, 1.1);
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		double runtime = 50;
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime",String.valueOf(runtime));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1.5, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		long startTime = System.currentTimeMillis();
		taeUnity.evaluateRun(runConfigs);
		long endTime = System.currentTimeMillis();
		if(endTime - startTime > 5000)
		{
			fail("This test took too long to run");
		}
	}
	
	
	@Test
	public void testKillingRunDecoratorRunsMarkedAsCrashed()
	{
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(SleepyParamEchoExecutorWithRealtime.class.getCanonicalName());
		
		
		
		
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFileWithRealTime.txt");
		
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(paramFile);
		
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 2);
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.cores = 1;
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 125;
		
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		
		final int CRASHED_SOLUTION_QUALITY = 2_000_000;
		try(TargetAlgorithmEvaluator taeUnity = new CrashedSolutionQualityTransformingTargetAlgorithmEvaluatorDecorator(new KillCaptimeExceedingRunsRunsTargetAlgorithmEvaluatorDecorator(tae, 1.1), CRASHED_SOLUTION_QUALITY ))
		{
			/**
			 * Test Synchronous Transformation
			 */
			List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
	
			for(int i=0; i < 2; i++)
			{
				ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
				
				System.out.println(config.getFormattedParameterString());
				config.put("runtime",String.valueOf("0"));
				if(i == 0)
				{
					config.put("realTime", String.valueOf(i*10+0));
				}
				
				if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
				{
					//Only want good configurations
					i--;
					continue;
				} else
				{
					config.put("solved","SAT");
					AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1.5, config, execConfig);
					runConfigs.add(rc);
				}
			}
			
			long startTime = System.currentTimeMillis();
			List<AlgorithmRunResult> runs = taeUnity.evaluateRun(runConfigs);
			long endTime = System.currentTimeMillis();
			
			
			assertEquals(runs.get(0).getRunStatus(), RunStatus.SAT);
			
			assertEquals(runs.get(1).getRunStatus(), RunStatus.CRASHED);
			assertTrue(runs.get(1).getRuntime() > 1.5);
			assertEquals(runs.get(1).getQuality(), CRASHED_SOLUTION_QUALITY, 1);
			
			
			System.out.println(runs);
			
			 runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
			
			for(int i=0; i < 2; i++)
			{
				ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
				
				System.out.println(config.getFormattedParameterString());
				config.put("runtime",String.valueOf("0"));
				if(i == 0)
				{
					config.put("realTime", String.valueOf(i*10+0));
				}
				
				if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
				{
					//Only want good configurations
					i--;
					continue;
				} else
				{
					config.put("solved","SAT");
					AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1.5, config, execConfig);
					runConfigs.add(rc);
				}
			}
			
			startTime = System.currentTimeMillis();
			
			
			final AtomicReference<List<AlgorithmRunResult>> listRef = new AtomicReference<>();
			final CountDownLatch latch = new CountDownLatch(1);
			
			
			taeUnity.evaluateRunsAsync(runConfigs, new TargetAlgorithmEvaluatorCallback(){

				@Override
				public void onSuccess(List<AlgorithmRunResult> runs) {
					listRef.set(runs);
					latch.countDown();
				}

				@Override
				public void onFailure(RuntimeException e) {
					e.printStackTrace();
					latch.countDown();
				}
				
			}
			);
			
			
			
			try {
				latch.await();
			} catch (InterruptedException e1) {
			
				fail("Interrupted while executing test");
			}
			
			
			System.out.println(listRef.get());
			runs = listRef.get();
			assertEquals(runs.get(0).getRunStatus(), RunStatus.SAT);
			
			assertEquals(runs.get(1).getRunStatus(), RunStatus.CRASHED);
			assertTrue(runs.get(1).getRuntime() > 1.5);
			assertEquals(runs.get(1).getQuality(), CRASHED_SOLUTION_QUALITY, 1);
			
			
			
			
			
			
			
			
			
			
		}
		
		
		
		
	}
	
	
	
	
	public void checkExceptedObserverCount(TargetAlgorithmEvaluator tae, double scale, List<AlgorithmRunConfiguration> runConfigs, double runtime, double observerFrequency)
	{

		final AtomicInteger count = new AtomicInteger(0);
		TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) {
				count.incrementAndGet();
				
			}
			
		};
		
		
		StopWatch watch = new AutoStartStopWatch();
		tae.evaluateRun(runConfigs, obs);
		watch.stop();
		
		
		double timeInSeconds = watch.time() / 1000.0;
		System.out.println("Runs completed in " + timeInSeconds + " observerCounts: " + count.get() + " expect:" + (( runtime / scale) - 1) / observerFrequency * 1000);
		assertTrue("Expected that the number of observer notifications " + count.get() + " > " + (( runtime / scale) - 1) / observerFrequency * 1000  , count.get() > (( runtime / scale) - 1) / observerFrequency * 1000);
		
		assertTrue("Expected that time taken " + timeInSeconds + " > " + ((runtime / scale) - 1) , timeInSeconds >  (runtime / scale) - 1 );
		assertTrue("Expected that time taken " + timeInSeconds + " < " + ((runtime / scale) + 1) , timeInSeconds <  (runtime / scale) + 1 );
	}
	
	@Test
	public void testDecoratorsApplyTheSameWay()
	{
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		RandomResponseTargetAlgorithmEvaluatorFactory fact = new RandomResponseTargetAlgorithmEvaluatorFactory();
		
		RandomResponseTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		options.persistent = true;
		options.cores = 10000;
		TargetAlgorithmEvaluator tae = fact.getTargetAlgorithmEvaluator( options);
		
		
		tae = new BoundedTargetAlgorithmEvaluator(tae, 1);
		
		final List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(TARGET_RUNS_IN_LOOPS);
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1001, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		TargetAlgorithmEvaluator tae10 = new SolQualSetTargetAlgorithmEvaluatorDecorator( new SolQualSetTargetAlgorithmEvaluatorDecorator(tae, 5), 10);
		TargetAlgorithmEvaluator tae5 = new SolQualSetTargetAlgorithmEvaluatorDecorator( new SolQualSetTargetAlgorithmEvaluatorDecorator(tae, 10), 5);
		
		
		
		
		List<AlgorithmRunResult> runs = tae10.evaluateRun(runConfigs);
		for(AlgorithmRunResult run : runs)
		{
			assertEquals("Expected quality to be 10", 10, run.getQuality(), 0.01);
		}
		
		
		runs = tae5.evaluateRun(runConfigs);
		for(AlgorithmRunResult run : runs)
		{
			assertEquals("Expected quality to be 5", 5, run.getQuality(), 0.01);
		}
		
		final Semaphore s = new Semaphore(0);
		final AtomicInteger solQual = new AtomicInteger(0);
		tae10.evaluateRunsAsync(runConfigs, new TargetAlgorithmEvaluatorCallback() {
			
			@Override
			public void onSuccess(List<AlgorithmRunResult> runs) {
				solQual.set((int) runs.get(0).getQuality());
				s.release();
				
			}
			
			@Override
			public void onFailure(RuntimeException e) {
				e.printStackTrace();
				
			}
		});

		s.acquireUninterruptibly();
		assertEquals("Expect SolQual to be 10", 10,  solQual.get());
		
		tae5.evaluateRunsAsync(runConfigs, new TargetAlgorithmEvaluatorCallback() {
			
			@Override
			public void onSuccess(List<AlgorithmRunResult> runs) {
				solQual.set((int) runs.get(0).getQuality());
				s.release();
				
			}
			
			@Override
			public void onFailure(RuntimeException e) {
				e.printStackTrace();
				
			}
		});

		s.acquireUninterruptibly();
		assertEquals("Expect SolQual to be 5", 5,  solQual.get());
		
		
		
	}
	
	
	@Test
	public void testSpacesAndQuotesCLI()
	{
		//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		StringBuilder b = new StringBuilder();
		b.append("\"test-files/testexecutor/file test.py\"");
		System.out.println(b);
		
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("Test { \"\\\" } [\"\\\"]\n");
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		options.cores = 2;
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		tae = new BoundedTargetAlgorithmEvaluator(tae,2);
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);

			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"),1), 3000, config, execConfig);
			runConfigs.add(rc);

		}
		
		
		
		StopWatch watch = new AutoStartStopWatch();
		cliTAE.evaluateRun(runConfigs).get(0).getAdditionalRunData().equals("'\"\"'");
		System.out.println(watch.stop());
		
	}
	
	@Test
	public void testBackslash()
	{
		//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		StringBuilder b = new StringBuilder();
		b.append("\"test-files/testexecutor/fi_letest.py\"");
		System.out.println(b);
		
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("Test { \"\\\" } [\"\\\"]\n");
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		options.cores = 2;
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		tae = new BoundedTargetAlgorithmEvaluator(tae,2);
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);

			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"),1), 3000, config, execConfig);
			runConfigs.add(rc);

		}
		
		
		
		StopWatch watch = new AutoStartStopWatch();
		cliTAE.evaluateRun(runConfigs).get(0).getAdditionalRunData().equals("'\"\"'");
		System.out.println(watch.stop());
		
	}

	@Test
	public void testEnvironmentVariableSet()
	{
		//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(EnvironmentVariableEchoer.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 0.01);
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.cores = 2;
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		
		
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		double runtime = 50;
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime",String.valueOf(runtime));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 1.5, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream pout = new PrintStream(bout);
		PrintStream oldOut = System.out;
		
		System.setOut(pout);
		
		tae.evaluateRun(runConfigs);
		
		
		System.setOut(oldOut);
		System.out.println(bout.toString());
		assertTrue(bout.toString().contains(CommandLineAlgorithmRun.PORT_ENVIRONMENT_VARIABLE));
		assertTrue(bout.toString().contains(CommandLineAlgorithmRun.FREQUENCY_ENVIRONMENT_VARIABLE));
		
		
		
	}
	
	@Test
	public void testCPUTimeUpdateWithRunsolver()
	{
		//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		
		b.append((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "runsolver" + File.separator + "runsolver -C 4000 " + (new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "runsolver" + File.separator + "/busy");
		//b.append((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "runsolver" + File.separator + "system ");
		
		System.out.println(b);
		

		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 1500);
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.cores = 2;
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		options.pgEnvKillCommand = (new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "testExecutionEnvironment" + File.separator + "killenv.sh";
		
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		//tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		
		
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		double runtime = 50;
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime",String.valueOf(runtime));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 20, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		
		
		TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) 
			{
				for(AlgorithmRunResult run : runs)
				{
					System.out.println("Runtime: " + run.getRuntime() + " walltime: " + run.getWallclockExecutionTime());
					
					if(run.getRuntime() > 4)
					{
						run.kill();
					}
					
					if(run.getWallclockExecutionTime() > 30)
					{
						System.err.println("This test has almost certainly failed and will never end");
					}
				}
			}
			
		};
		
		
		
		tae.evaluateRun(runConfigs,obs);
		
		
		
		
	}
	
	

	@Test
	public void testRaceConditionOfWrapper()
	{
		//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		
		b.append((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "rubywrapper" + File.separator + "rubywrapper.rb");
		//b.append((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "runsolver" + File.separator + "system ");
		
		System.out.println(b);
		
		
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 1500);
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.cores = 10;
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		
		
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		double runtime = 50;
		for(int i=0; i < 20; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime",String.valueOf(runtime));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 20, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		
		
		TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) 
			{
				AlgorithmRunResult run = runs.get(0);
				
					System.out.println("Runtime: " + run.getRuntime() + " walltime: " + run.getWallclockExecutionTime());
					
					if(run.getRuntime() > 4)
					{
						for(AlgorithmRunResult krun : runs)
						{
							krun.kill();
						}
					}
					
					if(run.getWallclockExecutionTime() > 30)
					{
						System.err.println("This test has almost certainly failed and will never end");
					}
				
			}
			
		};
		
		
		
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs,obs);
		
		
		
		
		for(AlgorithmRunResult run : runs)
		{
			System.out.println(run);
			if(run.getRunStatus().equals(RunStatus.CRASHED))
			{
				fail("Run shouldn't be crashed");
			}
		}
		
		
		
		
		
	}
	

	@Test(expected=TargetAlgorithmAbortException.class)
	public void testDoubleOutput()
	{
		//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		
		b.append((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "rubywrapper" + File.separator + "doublewrapper.rb");
		//b.append((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "runsolver" + File.separator + "system ");
		
		System.out.println(b);
		
		
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 1500);
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.cores = 10;
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		
		
		
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		double runtime = 50;
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime",String.valueOf(runtime));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 20, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		
		
		TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) 
			{
				AlgorithmRunResult run = runs.get(0);
				
					System.out.println("Runtime: " + run.getRuntime() + " walltime: " + run.getWallclockExecutionTime());
					
					if(run.getRuntime() > 4)
					{
						for(AlgorithmRunResult krun : runs)
						{
							krun.kill();
						}
					}
					
					if(run.getWallclockExecutionTime() > 30)
					{
						System.err.println("This test has almost certainly failed and will never end");
					}
				
			}
			
		};		
		
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs,obs);
	
		for(AlgorithmRunResult run : runs)
		{
			System.out.println(run);
			if(run.getRunStatus().equals(RunStatus.CRASHED))
			{
				fail("Run shouldn't be crashed");
			}
		}
		
	}
	
	
	
	
	
	
	
	@Test
	public void testProcessGroupKilled()
	{
		
		/**
		 * This tests to see if a wrapper (that sets the process group) => chained shutsdown correctly
		 */
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		
		b.append((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "runsolver" + File.separator + "pgrpset.py");
		//b.append((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "runsolver" + File.separator + "system ");
		
		System.out.println(b);
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 1500);
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.cores = 2;
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		double runtime = 50;
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime",String.valueOf(runtime));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 20, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		
		
		TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) 
			{
				for(AlgorithmRunResult run : runs)
				{
					System.out.println("Runtime: " + run.getRuntime() + " walltime: " + run.getWallclockExecutionTime());
					
					if(run.getRuntime() > 1)
					{
						run.kill();
					}
					
					if(run.getWallclockExecutionTime() > 30)
					{
						System.err.println("This test has almost certainly failed and will never end");
					}
				}
			}
			
		};
		
		
		
		tae.evaluateRun(runConfigs,obs);
		
		try {
			
			//This checks to see how many matching processes there are 
			//It outputs a line from the wc command and we expect all zeros.
			//If this test fails, then it may be because a previous run failed,
			//so check that shell script for the exact line and maybe killall the other processes
			Process proc = Runtime.getRuntime().exec((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "runsolver" + File.separator + "runsCounter.sh");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			
			assertTrue("Expected that there would be zero matching process", reader.readLine().matches("^\\s*0\\s*0\\s*0\\s*$"));
		} catch (IOException e) {

			e.printStackTrace();
			fail("Um what?");
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		/***
		 * This checks to see if a runsolver => chained shuts down correctly.
		 */

		b = new StringBuilder();
		
		b.append((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "runsolver" + File.separator + "runsolver -C 4000 " + (new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "runsolver" + File.separator + "/chained 5");
		
		System.out.println(b);
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 1500);
		tae = fact.getTargetAlgorithmEvaluator( options);	
		
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime",String.valueOf(runtime));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 20, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		tae.evaluateRun(runConfigs,obs);
		
		try {
			
			//This checks to see how many matching processes there are 
			//It outputs a line from the wc command and we expect all zeros.
			//If this test fails, then it may be because a previous run failed,
			//so check that shell script for the exact line and maybe killall the other processes
			Process proc = Runtime.getRuntime().exec((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "runsolver" + File.separator + "runsCounter.sh");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			
			assertTrue("Expected that there would be zero matching process", reader.readLine().matches("^\\s*0\\s*0\\s*0\\s*$"));
		} catch (IOException e) {

			e.printStackTrace();
			fail("Um what?");
		}
		
		
		
		
		
		
		
		
		/***
		 * This tests to see if a wrapper => runsolver => chained shutsdown correctly
		 */
		
		b = new StringBuilder();
		
		b.append((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "runsolver" + File.separator + "pgrpset-rs.py");
		
		System.out.println(b);
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 1500);
		tae = fact.getTargetAlgorithmEvaluator( options);	
		
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime",String.valueOf(runtime));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 20, config, execConfig);
				runConfigs.add(rc);
			}
		}
		
		tae.evaluateRun(runConfigs,obs);
		
		try {
			
			//This checks to see how many matching processes there are 
			//It outputs a line from the wc command and we expect all zeros.
			//If this test fails, then it may be because a previous run failed,
			//so check that shell script for the exact line and maybe killall the other processes
			Process proc = Runtime.getRuntime().exec((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "runsolver" + File.separator + "runsCounter.sh");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			
			assertTrue("Expected that there would be zero matching process", reader.readLine().matches("^\\s*0\\s*0\\s*0\\s*$"));
		} catch (IOException e) {

			e.printStackTrace();
			fail("Um what?");
		}
		
		
		
	}
	
	
	@Test
	public void testProcessEnvironmentKilled()
	{
		
		/**
		 * This tests to see if a wrapper (that sets the process group) => chained shutsdown correctly
		 */
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		
		b.append((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "testExecutionEnvironment" + File.separator + "execTree.py");
		
		System.out.println(b);
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 1500);
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.cores = 2;
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		options.pgEnvKillCommand = (new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "testExecutionEnvironment" + File.separator + "killenv.sh";
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
		
		tae = new WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(tae);
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		double runtime = 50;
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime",String.valueOf(runtime));
			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), 20, config,execConfig);
				runConfigs.add(rc);
			}
		}
		
		
		
		TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) 
			{
				for(AlgorithmRunResult run : runs)
				{
					System.out.println("Runtime: " + run.getRuntime() + " walltime: " + run.getWallclockExecutionTime());
					
					if(run.getRuntime() > 1)
					{
						run.kill();
					}
					
					if(run.getWallclockExecutionTime() > 30)
					{
						System.err.println("This test has almost certainly failed and will never end");
					}
				}
			}
			
		};
		
		
		
		tae.evaluateRun(runConfigs,obs);
		
		try {
			
			//This checks to see how many matching processes there are 
			//It outputs a line from the wc command and we expect all zeros.
			//If this test fails, then it may be because a previous run failed,
			//so check that shell script for the exact line and maybe killall the other processes
			Process proc = Runtime.getRuntime().exec((new File("")).getAbsolutePath() + File.separator + "test-files"+File.separator + "testExecutionEnvironment" + File.separator + "countenv.sh " + CommandLineAlgorithmRun.EXECUTION_UUID_ENVIRONMENT_VARIABLE_DEFAULT );
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			
			String line = reader.readLine();
			System.out.println(line);
			assertTrue("Expected that there would be zero matching process: got: " + line , line.matches("^\\s*0\\s*$"));
		} catch (IOException e) {

			e.printStackTrace();
			fail("Um what?");
		}
		
		
	}
	
	
	static class TestClass
	{
		public static void main(String[] args)
		{
			System.out.println("WHAT");
		}
	}
	
	
	/**
	 * Bug #2115
	 * 
	 * Walltime is incorrect when wrapper doesn't output anything.
	 */
	@Test
	public void testIncorrectWallTimeOnNoWrapperOutput()
	{
		
		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TestClass.class.getCanonicalName());
		
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false,3000);
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		options.cores = 16;
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
	
	
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		for(int i=0; i < 1; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);

			config.put("runtime", "0");
			if(config.get("solved").equals("ABORT") || config.get("solved").equals("INVALID"))
			{
				i--;
				continue;
			}
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"),1), 0, config, execConfig);
			runConfigs.add(rc);

		}
		
		StopWatch watch = new AutoStartStopWatch();
		
		List<AlgorithmRunResult> results = tae.evaluateRun(runConfigs);
		
		for(AlgorithmRunResult run : results)
		{
			System.out.println(run.getResultLine() + "===>" + run.getWallclockExecutionTime());
		}
		System.out.println(watch.stop());
		
		try
		{
			assertTrue("Run should report less time than we measured.", results.get(0).getWallclockExecutionTime() < watch.time() / 1000.0 );
		} finally
		{
			tae.notifyShutdown();
		}
		
		
	
	}
	
	@Test
	/**
	 * Related to bug #2116
	 * 
	 * The TAE gets a run with zero seconds cutoff time, users may not expect this.
	 * 
	 */
	public void testZeroSecondsCallNotSentToTAE()
	{

		Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(SleepyParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false,3000);
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		options.cores = 16;
		
		tae = fact.getTargetAlgorithmEvaluator( options);	
	
		
		tae = new BoundedTargetAlgorithmEvaluator(tae,2);
		tae = new CompleteZeroSecondCutoffRunsTargetAlgorithmEvaluatorDecorator(tae);
		
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(4);
		for(int i=0; i < 40; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);

			config.put("runtime", "0");
			if(config.get("solved").equals("ABORT") || config.get("solved").equals("INVALID"))
			{
				i--;
				continue;
			}
			AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"),1), 0, config, execConfig);
			runConfigs.add(rc);

		}

		ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
		config.put("solved","SAT");
		AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"),1), 3000, config, execConfig);
		runConfigs.set(5, rc);
		
		config = configSpace.getRandomParameterConfiguration(r);
		config.put("solved","SAT");
		rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"),1), 3000, config, execConfig);
		runConfigs.set(7, rc);
		
		config = configSpace.getRandomParameterConfiguration(r);
		config.put("solved","SAT");
		rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"),1), 3000, config, execConfig);
		runConfigs.set(11, rc);
		
		config = configSpace.getRandomParameterConfiguration(r);
		config.put("solved","SAT");
		rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"),1), 3000, config, execConfig);
		runConfigs.set(12, rc);
		
		
		
		
		
		
		
		
		TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
		{

			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) {
				for(AlgorithmRunResult run : runs)
				{
					System.out.println(run.getResultLine());
				}

				
			}
			
		};
		
		StopWatch watch = new AutoStartStopWatch();
		
		List<AlgorithmRunResult> results = tae.evaluateRun(runConfigs, obs);
		
		
		for(AlgorithmRunResult run : results)
		{
			System.out.println(run.getResultLine());
		}
		System.out.println(watch.stop());
		
		try
		{
			assertTrue("Expected time to execute runs was less than 15 seconds", watch.time() < 15000);
		} finally
		{
			tae.notifyShutdown();
		}
		
	}
	
	@Test
	/**
	 * Checks if a deadlock occurs with notifyShutdown()
	 * 
	 * See Issue #1949
	 */
	public void testDeadlockOnShutdownWithInterruption()
	{
		//Check that a submission of run 10 runs on a bound of <5 take 5,1,1,1,1, 5,1,1,1,1 takes 6 seconds and not 10.
		final Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, false,3000);
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		options.cores = 10;
		
		TargetAlgorithmEvaluator tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		tae = new BoundedTargetAlgorithmEvaluator(tae,10);
		
		OutstandingEvaluationsWithAccessorTargetAlgorithmEvaluator aTAE = new OutstandingEvaluationsWithAccessorTargetAlgorithmEvaluator(tae);
		
		tae = aTAE;
		List<AlgorithmRunConfiguration> runConfigs = new ArrayList<AlgorithmRunConfiguration>(100);
		for(int i=0; i < 100; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(r);
			
			config.put("runtime","2");
			

			if(config.get("solved").equals("INVALID") || config.get("solved").equals("ABORT") || config.get("solved").equals("CRASHED") || config.get("solved").equals("TIMEOUT"))
			{
				//Only want good configurations
				i--;
				continue;
			} else
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("TestInstance"), Long.valueOf(config.get("seed"))), config,execConfig);
				runConfigs.add(rc);
			}
		}
		
		
		
		TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
		{

			private boolean killedByDecorator = false;
			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) 
			{
				
				double sum = 0;
			
			
				for(AlgorithmRunResult run : runs)
				{
					if(run.getAlgorithmRunConfiguration().getProblemInstanceSeedPair().getSeed() % 100 % 11 != 0)
					{
						run.kill();
					}
					

					if(run.getRunStatus() == RunStatus.KILLED && !killedByDecorator)
					{
						if(run.getAdditionalRunData().equals(BoundedTargetAlgorithmEvaluator.KILLED_BY_DECORATOR_ADDL_RUN_INFO))
						{
							System.err.println(run.getResultLine());
							killedByDecorator = true;
						}
					}
					
				}
				

				
			}
			
		};
		
		
		tae.evaluateRunsAsync(runConfigs, new TargetAlgorithmEvaluatorCallback() {
			
			@Override
			public void onSuccess(List<AlgorithmRunResult> runs) {
				System.out.println("Completed: " + runs);
				
			}
			
			@Override
			public void onFailure(RuntimeException e) {
				e.printStackTrace();
				
			}
		}, obs);
		
		try {
			System.out.println("Outstanding: " + aTAE.getOutstandingRunConfigs());
			
			Thread.sleep(1000);
			System.out.println("Outstanding: " + aTAE.getOutstandingRunConfigs());
			
		} catch (InterruptedException e1) {
			Thread.currentThread().interrupt();
			return;
		}
		
		tae.notifyShutdown();
		System.out.println("Outstanding: " + aTAE.getOutstandingRunConfigs());
		
		
		
		int killedCount = 0;
	
		
		
		//assertTrue("Expected time for Bounded to be less than 5 seconds", watch2.time() < 5000 );
		
	}
	
	
	
}
