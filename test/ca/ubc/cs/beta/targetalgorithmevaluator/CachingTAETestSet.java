package ca.ubc.cs.beta.targetalgorithmevaluator;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.RunResult;
import ca.ubc.cs.beta.aeatk.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aeatk.configspace.ParamFileHelper;
import ca.ubc.cs.beta.aeatk.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aeatk.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aeatk.misc.watch.AutoStartStopWatch;
import ca.ubc.cs.beta.aeatk.misc.watch.StopWatch;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;
import ca.ubc.cs.beta.aeatk.runconfig.RunConfig;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.random.RandomResponseTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.random.RandomResponseTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.functionality.OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.helpers.CallObserverBeforeCompletionTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.resource.caching.CachingTargetAlgorithmEvaluatorDecorator;

public class CachingTAETestSet {
private static TargetAlgorithmEvaluator tae;
	
	private static AlgorithmExecutionConfig execConfig;
	
	private static ParamConfigurationSpace configSpace;
	
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
		configSpace = new ParamConfigurationSpace(paramFile);
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false, 500);
		
		tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
		

		
	}
	
	@AfterClass
	public static void afterClass()
	{
		pool.logUsage();
	}
	
	@Test
	public void cachingTAETester()
	{
		nestingTestLevels(1);
		
	}

	@Test
	@Ignore
	public void nestingCachingTAETester()
	{
		for(int j=0; j < 1; j++)
		{
			for(int i=1; i < 10; i++)
			{
				PrintStream out = System.out;
				System.setOut(new PrintStream(new NullOutputStream()));
				StopWatch watch;
				try {
				
					watch = new AutoStartStopWatch();
					nestingTestLevels(i);
				} finally
				{
					System.setOut(out);
				}
				System.out.println("Time taken was "+i+ ":"  + (watch.stop() / 1000.0));
				System.gc();
			}
		}
	}
	public void nestingTestLevels(int levels)
	{
		final Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		
		ParamConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a [0,100] [0]i\nb [0,9][0]i \n");
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false,3000);
		
		
		RandomResponseTargetAlgorithmEvaluatorFactory rfact = new RandomResponseTargetAlgorithmEvaluatorFactory();
		
		
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		options.cores = 10;
		
		TargetAlgorithmEvaluator tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		
		RandomResponseTargetAlgorithmEvaluatorOptions opts = rfact.getOptionObject();
		opts.cores = 20000;
		opts.simulateDelay = true;
		
		opts.maxResponse = 4;
		tae = rfact.getTargetAlgorithmEvaluator(opts);
		
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		final TargetAlgorithmEvaluator insideTAE = tae;
		
		for(int i=0; i < levels; i++)
		{
			tae = new CachingTargetAlgorithmEvaluatorDecorator(tae);
		}
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		ExponentialDistribution dist = new ExponentialDistribution(30);
		
		
		List<ProblemInstance> pis = new ArrayList<ProblemInstance>(4);
		
		for(int i=0; i < 4; i++)
		{
			pis.add(new ProblemInstance("inst" + i));
		}
		
		
		final AtomicInteger failures = new AtomicInteger(0);
		
		final TargetAlgorithmEvaluator fTAE = tae;
		ExecutorService execService = Executors.newFixedThreadPool(64);
		
		final AtomicInteger submits = new AtomicInteger(0);
		final AtomicInteger passes = new AtomicInteger(0);
		for(int i=0; i < 8000; i++)
		{
			int numberOfValuesToSubmit = (int) dist.inverseCumulativeProbability(r.nextDouble());
			
			final Set<RunConfig> rcs = new LinkedHashSet<RunConfig>();
			for(int j=0; j < numberOfValuesToSubmit; j++)
			{
				rcs.add(new RunConfig(new ProblemInstanceSeedPair(pis.get(r.nextInt(4)),r.nextInt(2)), configSpace.getRandomConfiguration(r),execConfig));
			}
			
			final List<RunConfig> submitList = new ArrayList<RunConfig>(rcs);
			submits.incrementAndGet();
			Runnable runner = new Runnable()
			{
				@Override
				public void run()
				{
					/*
					try {
						Thread.sleep((long) (r.nextDouble() * 10));
					} catch (InterruptedException e1) {
						Thread.currentThread().interrupt();
						return;
					}
					*/
					if(r.nextBoolean())
					{
						Thread.yield();
					}
					fTAE.evaluateRunsAsync(submitList, new TargetAlgorithmEvaluatorCallback(){

						AtomicBoolean bool = new AtomicBoolean();
						@Override
						public void onSuccess(List<AlgorithmRun> runs) {
				
							if(!bool.compareAndSet(false, true))
							{
								throw new IllegalStateException("Callback was called twice: " + this);
							}
							
							
							
							if(rcs.size() != runs.size())
							{
								throw new IllegalStateException("Expected that the number of submitted run configs: " + rcs.size() + " would equal runs completed " + runs.size());
							}
							int i=0;
							for(RunConfig rc : submitList)
							{
								if(!rc.equals(runs.get(i).getRunConfig()))
								{
									throw new IllegalStateException("Runs are coming back out of order: " + rcs + " runs: " + runs);
								}
								i++;
							}
							
							passes.incrementAndGet();
						}

						@Override
						public void onFailure(RuntimeException e) {
							e.printStackTrace();
							failures.incrementAndGet();					
						}
						
					});
				}
				
			};
			execService.execute(runner);
			
			//RunConfig rc = new RunConfig(pis.get(index));
		
			
		}
		
		try {
			execService.shutdown();
			execService.awaitTermination(10, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		tae.waitForOutstandingEvaluations();
		
		tae.notifyShutdown();
		System.out.println(insideTAE.getRunCount());
	
		System.out.println("Configuration space size: [" +  configSpace.getLowerBoundOnSize() + "," + configSpace.getUpperBoundOnSize() + "] * " + pis.size() + " * 2");
		assertEquals("Expected no failures to be detected", 0, failures.get());
		assertEquals("Expected passes to be equal to submits", passes.get(), submits.get());
		
		
	}
	
	
	@Test
	public void testSameCallbackMultipleRequests()
	{
		
		final Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		
		ParamConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a [0,100] [0]i\nb [0,9][0]i \n");
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false,3000);
		
		
		RandomResponseTargetAlgorithmEvaluatorFactory rfact = new RandomResponseTargetAlgorithmEvaluatorFactory();
		
		
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		options.cores = 10;
		
		TargetAlgorithmEvaluator tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		
		RandomResponseTargetAlgorithmEvaluatorOptions opts = rfact.getOptionObject();
		opts.cores = 10240;
		opts.simulateDelay = true;
		
		opts.maxResponse = 4;
		tae = rfact.getTargetAlgorithmEvaluator(opts);
		
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		final TargetAlgorithmEvaluator insideTAE = tae;
		
		for(int i=0; i < 1; i++)
		{
			tae = new CachingTargetAlgorithmEvaluatorDecorator(tae);
		}
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		ExponentialDistribution dist = new ExponentialDistribution(30);
		
		
		List<ProblemInstance> pis = new ArrayList<ProblemInstance>(4);
		
		for(int i=0; i < 4; i++)
		{
			pis.add(new ProblemInstance("inst" + i));
		}
		
		
		final AtomicInteger failures = new AtomicInteger(0);
		
		final TargetAlgorithmEvaluator fTAE = tae;
		ExecutorService execService = Executors.newFixedThreadPool(64);
		
		final AtomicInteger submits = new AtomicInteger(0);
		final AtomicInteger passes = new AtomicInteger(0);
		
		
		for(int i=0; i < 8000; i++)
		{
			int numberOfValuesToSubmit = (int) dist.inverseCumulativeProbability(r.nextDouble());
			
			final Set<RunConfig> rcs = new LinkedHashSet<RunConfig>();
			for(int j=0; j < numberOfValuesToSubmit; j++)
			{
				rcs.add(new RunConfig(new ProblemInstanceSeedPair(pis.get(r.nextInt(4)),r.nextInt(2)), configSpace.getRandomConfiguration(r),execConfig));
			}
			
			final List<RunConfig> submitList = new ArrayList<RunConfig>(rcs);
			submits.incrementAndGet();
			
			
			
			final TargetAlgorithmEvaluatorCallback cb = new TargetAlgorithmEvaluatorCallback()
			{

				@Override
				public void onSuccess(List<AlgorithmRun> runs) {
					
					//We don't check the results because we don't know what matches it
					//Other tests will check the result better 
					
					passes.incrementAndGet();
				}

				@Override
				public void onFailure(RuntimeException e) {
					e.printStackTrace();
					failures.incrementAndGet();	
				}
				
			};
			
			
			Runnable runner = new Runnable()
			{
				@Override
				public void run()
				{
					
					try {
						Thread.sleep((long) (Math.max(r.nextGaussian() * 50,1)));
					} catch (InterruptedException e1) {
						Thread.currentThread().interrupt();
						return;
					}
					
					if(r.nextBoolean())
					{
						Thread.yield();
					}
					fTAE.evaluateRunsAsync(submitList, cb);
				}
				
			};
			execService.execute(runner);
			
			//RunConfig rc = new RunConfig(pis.get(index));
		
			
		}
		try {
			execService.shutdown();
			execService.awaitTermination(10, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		tae.waitForOutstandingEvaluations();
		
		tae.notifyShutdown();
		System.out.println(insideTAE.getRunCount());
	
		System.out.println("Configuration space size: [" +  configSpace.getLowerBoundOnSize() + "," + configSpace.getUpperBoundOnSize() + "] * " + pis.size() + " * 2");
		assertEquals("Expected no failures to be detected", 0, failures.get());
		assertEquals("Expected passes to be equal to submits", passes.get(), submits.get());
		
			
		
	}
	
	@Test
	public void testCachingObserver()
	{
		final Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		
		ParamConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a [0,10] [0]i\n");
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false,3000);
		
		
		RandomResponseTargetAlgorithmEvaluatorFactory rfact = new RandomResponseTargetAlgorithmEvaluatorFactory();
		
		
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		options.cores = 10;
		
		TargetAlgorithmEvaluator tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		
		RandomResponseTargetAlgorithmEvaluatorOptions opts = rfact.getOptionObject();
		opts.cores = 4;
		opts.simulateDelay = true;
		
		
		opts.maxResponse = 10;
		tae = rfact.getTargetAlgorithmEvaluator(opts);
		
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		final TargetAlgorithmEvaluator insideTAE = tae;
		
		for(int i=0; i < 1; i++)
		{
			tae = new CachingTargetAlgorithmEvaluatorDecorator(tae);
		}
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		ExponentialDistribution dist = new ExponentialDistribution(4);
		
		
		List<ProblemInstance> pis = new ArrayList<ProblemInstance>(4);
		
		for(int i=0; i < 1; i++)
		{
			pis.add(new ProblemInstance("inst" + i));
		}
		
		
		final AtomicInteger failures = new AtomicInteger(0);
		
		final TargetAlgorithmEvaluator fTAE = tae;
		ExecutorService execService = Executors.newFixedThreadPool(64);
		
		final AtomicInteger submits = new AtomicInteger(0);
		final AtomicInteger passes = new AtomicInteger(0);
		
		final RunConfig firstRunConfig = new RunConfig(new ProblemInstanceSeedPair(pis.get(r.nextInt(1)),0), configSpace.getRandomConfiguration(r),execConfig);
		for(int i=0; i < 4; i++)
		{
			int numberOfValuesToSubmit = 5;
			
			final Set<RunConfig> rcs = new LinkedHashSet<RunConfig>();
			for(int j=0; j < numberOfValuesToSubmit; j++)
			{
				if(j == 0)
				{
					rcs.add(firstRunConfig);
				} else
				{
					rcs.add(new RunConfig(new ProblemInstanceSeedPair(pis.get(r.nextInt(1)),0), configSpace.getRandomConfiguration(r),execConfig));
				}
			}
			
			final List<RunConfig> submitList = new ArrayList<RunConfig>(rcs);
			submits.incrementAndGet();
			
			
			
			final TargetAlgorithmEvaluatorCallback cb = new TargetAlgorithmEvaluatorCallback()
			{

				@Override
				public void onSuccess(List<AlgorithmRun> runs) {
					
					//We don't check the results because we don't know what matches it
					//Other tests will check the result better 
					
					System.out.println("DONE:" + runs);
					passes.incrementAndGet();
				}

				@Override
				public void onFailure(RuntimeException e) {
					e.printStackTrace();
					failures.incrementAndGet();	
				}
				
			};
			
			
			final int id = i;
			TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
			{

				@Override
				public void currentStatus(
						List<? extends AlgorithmRun> runs) {
					System.out.println("Observer: "+ id + ":" + runs);
					
				}
				
			};
			
	
			fTAE.evaluateRunsAsync(submitList, cb,obs);
			
			//RunConfig rc = new RunConfig(pis.get(index));
		
			
		}
		
		tae.waitForOutstandingEvaluations();
		
		tae.notifyShutdown();
		System.out.println(insideTAE.getRunCount());
	
		System.out.println("Configuration space size: [" +  configSpace.getLowerBoundOnSize() + "," + configSpace.getUpperBoundOnSize() + "] * " + pis.size() + " * 2");
		assertEquals("Expected no failures to be detected", 0, failures.get());
		assertEquals("Expected passes to be equal to submits", passes.get(), submits.get());
	}
	
	@Test
	public void testSimpleKillingCacheObserver()
	{
		final Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		
		ParamConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a [0,10] [0]i\n");
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false,3000);
		
		
		RandomResponseTargetAlgorithmEvaluatorFactory rfact = new RandomResponseTargetAlgorithmEvaluatorFactory();
		
		
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		options.cores = 4;
		
		TargetAlgorithmEvaluator tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		
		RandomResponseTargetAlgorithmEvaluatorOptions opts = rfact.getOptionObject();
		opts.cores = 2;
		opts.simulateDelay = true;
		
		
		opts.maxResponse = 10;
		tae = rfact.getTargetAlgorithmEvaluator(opts);
		
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		final TargetAlgorithmEvaluator insideTAE = tae;
		
		for(int i=0; i < 1; i++)
		{
			tae = new CachingTargetAlgorithmEvaluatorDecorator(tae);
		}
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		ExponentialDistribution dist = new ExponentialDistribution(4);
		
		
		List<ProblemInstance> pis = new ArrayList<ProblemInstance>(4);
		
		for(int i=0; i < 1; i++)
		{
			pis.add(new ProblemInstance("inst" + i));
		}
		
		
		final AtomicInteger failures = new AtomicInteger(0);
		
		final TargetAlgorithmEvaluator fTAE = tae;
		ExecutorService execService = Executors.newFixedThreadPool(64);
		
		final AtomicInteger submits = new AtomicInteger(0);
		final AtomicInteger passes = new AtomicInteger(0);
		
		final RunConfig firstRunConfig = new RunConfig(new ProblemInstanceSeedPair(pis.get(r.nextInt(1)),0), configSpace.getRandomConfiguration(r),execConfig);
		for(int i=0; i < 2; i++)
		{
			int numberOfValuesToSubmit = 5;
			
			final Set<RunConfig> rcs = new LinkedHashSet<RunConfig>();
			for(int j=0; j < numberOfValuesToSubmit; j++)
			{
				if(j == 0)
				{
					rcs.add(firstRunConfig);
				} else
				{
					rcs.add(new RunConfig(new ProblemInstanceSeedPair(pis.get(r.nextInt(1)),0), configSpace.getRandomConfiguration(r),execConfig));
				}
			}
			
			final List<RunConfig> submitList = new ArrayList<RunConfig>(rcs);
			submits.incrementAndGet();
			
			final int id = i;
			
			final TargetAlgorithmEvaluatorCallback cb = new TargetAlgorithmEvaluatorCallback()
			{

				@Override
				public void onSuccess(List<AlgorithmRun> runs) {
					
					//We don't check the results because we don't know what matches it
					//Other tests will check the result better 
					
					for(AlgorithmRun run : runs)
					{
						if(id==1)
						{
							if(run.getRunResult().equals(RunResult.KILLED))
							{
								fail("Unexpected kill");
							}
						}
					}
					System.out.println("DONE:" + runs);
					passes.incrementAndGet();
				}

				@Override
				public void onFailure(RuntimeException e) {
					e.printStackTrace();
					failures.incrementAndGet();	
				}
				
			};
			
			
			
			

			TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
			{

				@Override
				public void currentStatus(
						List<? extends AlgorithmRun> runs) {
					System.out.println("Observer: "+ id + ":" + runs);
					
					if(id==0)
					{
						for(AlgorithmRun run : runs)
						{
							run.kill();
						}
					}
					
				}
				
			};
			
	
			fTAE.evaluateRunsAsync(submitList, cb,obs);
			
			//RunConfig rc = new RunConfig(pis.get(index));
		
			
		}
		
		tae.waitForOutstandingEvaluations();
		
		tae.notifyShutdown();
		System.out.println(insideTAE.getRunCount());
	
		System.out.println("Configuration space size: [" +  configSpace.getLowerBoundOnSize() + "," + configSpace.getUpperBoundOnSize() + "] * " + pis.size() + " * 2");
		assertEquals("Expected no failures to be detected", 0, failures.get());
		assertEquals("Expected passes to be equal to submits", passes.get(), submits.get());
	}
	

	@Test
	public void testAdvancedKillingObserver()
	{
		final Random r = pool.getRandom(DebugUtil.getCurrentMethodName());
		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(TrueSleepyParamEchoExecutor.class.getCanonicalName());
		
		ParamConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a [0,10] [0]i\n b [0,10] [0]i\n");
		
		execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false, false,3000);
		
		
		RandomResponseTargetAlgorithmEvaluatorFactory rfact = new RandomResponseTargetAlgorithmEvaluatorFactory();
		
		
		CommandLineTargetAlgorithmEvaluatorFactory fact = new CommandLineTargetAlgorithmEvaluatorFactory();
		CommandLineTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.logAllCallStrings = true;
		options.logAllProcessOutput = true;
		options.concurrentExecution = true;
		options.observerFrequency = 2000;
		options.cores = 4;
		
		TargetAlgorithmEvaluator tae = fact.getTargetAlgorithmEvaluator( options);	
		TargetAlgorithmEvaluator cliTAE = tae;
		
		RandomResponseTargetAlgorithmEvaluatorOptions opts = rfact.getOptionObject();
		opts.cores = 1024;
		opts.simulateDelay = true;
		
		
		opts.maxResponse = 4;
		tae = rfact.getTargetAlgorithmEvaluator(opts);
		
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		final TargetAlgorithmEvaluator insideTAE = tae;
		
		tae = new CallObserverBeforeCompletionTargetAlgorithmEvaluatorDecorator(tae);
		for(int i=0; i < 1; i++)
		{
			tae = new CachingTargetAlgorithmEvaluatorDecorator(tae);
		}
		
		tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(tae);
		ExponentialDistribution dist = new ExponentialDistribution(4);
		
		
		List<ProblemInstance> pis = new ArrayList<ProblemInstance>(4);
		
		for(int i=0; i < 4; i++)
		{
			pis.add(new ProblemInstance("inst" + i));
		}
		
		
		final AtomicInteger failures = new AtomicInteger(0);
		
		final TargetAlgorithmEvaluator fTAE = tae;
		ExecutorService execService = Executors.newFixedThreadPool(64);
		
		final AtomicInteger submits = new AtomicInteger(0);
		final AtomicInteger passes = new AtomicInteger(0);
		
		final RunConfig firstRunConfig = new RunConfig(new ProblemInstanceSeedPair(pis.get(r.nextInt(1)),0), configSpace.getRandomConfiguration(r),execConfig);
		for(int i=0; i < 8000; i++)
		{
			int numberOfValuesToSubmit = (int) dist.inverseCumulativeProbability(r.nextDouble());
			
			final Set<RunConfig> rcs = new LinkedHashSet<RunConfig>();
			for(int j=0; j < numberOfValuesToSubmit; j++)
			{
				if(j == 0)
				{
					rcs.add(firstRunConfig);
				} else
				{
					rcs.add(new RunConfig(new ProblemInstanceSeedPair(pis.get(r.nextInt(4)),r.nextInt(2)), configSpace.getRandomConfiguration(r),execConfig));
				}
			}
			
			final List<RunConfig> submitList = new ArrayList<RunConfig>(rcs);
			submits.incrementAndGet();
			
			final int id = i;
			
			final Set<AlgorithmRun> killedRuns = Collections.newSetFromMap(new ConcurrentHashMap<AlgorithmRun, Boolean>());

			final TargetAlgorithmEvaluatorCallback cb = new TargetAlgorithmEvaluatorCallback()
			{

				@Override
				public void onSuccess(List<AlgorithmRun> runs) {
					
					//We don't check the results because we don't know what matches it
					//Other tests will check the result better 
					
					for(AlgorithmRun run : runs)
					{
						if(id % 7 == 0)
						{
							if(run.getRunResult().equals(RunResult.KILLED))
							{
								fail("Unexpected kill " + id + ":" + run);
							}
							
							
						} else
						{
							if(run.getRunResult().equals(RunResult.KILLED))
							{
								if (!killedRuns.contains(run))
								{
									fail("Unexpected kill " + id + ":" + run);
								}
							}
						}
					}
					System.out.println("DONE: " + id + " " + runs);
					passes.incrementAndGet();
				}

				@Override
				public void onFailure(RuntimeException e) {
					e.printStackTrace();
					failures.incrementAndGet();	
				}
				
			};
			
			
			
			
			TargetAlgorithmEvaluatorRunObserver obs = new TargetAlgorithmEvaluatorRunObserver()
			{

				AtomicBoolean created = new AtomicBoolean(false);
				@Override
				public synchronized void currentStatus(	List<? extends AlgorithmRun> runs) {
					
					
					
					//System.out.println("Observer: "+ id + ":" + runs);
					
					
					if(id % 7 != 0)
					{

						if(created.compareAndSet(false, true))
						{
							for(AlgorithmRun run : runs)
							{
								if(r.nextDouble() > 0.5)
								{
									killedRuns.add(run);
								}
							}
						}
						
						for(AlgorithmRun run : runs)
						{
							if(killedRuns.contains(run))
							{
								run.kill();
							}
						}
					}
					
					
					
					for(AlgorithmRun run : runs)
					{
						if(run.getRunResult().equals(RunResult.KILLED))
						{
							if(!killedRuns.contains(run))
							{
								System.err.println("Saw a killed value I wasn't expecting : " + runs + " my killset is : " + killedRuns);
								failures.incrementAndGet();
								
							}
						}
					}
						
					
					
				}
				
			};
			
	
			fTAE.evaluateRunsAsync(submitList, cb,obs);
			
			//RunConfig rc = new RunConfig(pis.get(index));
		
			
		}
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				while(true)
				{
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					
						e.printStackTrace();
						return;
					}
					System.err.println(fTAE.getNumberOfOutstandingBatches() + " with runs: " + fTAE.getNumberOfOutstandingRuns());
				}
			}
			
		}).start();
		tae.waitForOutstandingEvaluations();
		
		tae.notifyShutdown();
		System.out.println(insideTAE.getRunCount());
	
		System.out.println("Configuration space size: [" +  configSpace.getLowerBoundOnSize() + "," + configSpace.getUpperBoundOnSize() + "] * " + pis.size() + " * 2");
		assertEquals("Expected no failures to be detected", 0, failures.get());
		assertEquals("Expected passes to be equal to submits", passes.get(), submits.get());
	}
	
	
}
