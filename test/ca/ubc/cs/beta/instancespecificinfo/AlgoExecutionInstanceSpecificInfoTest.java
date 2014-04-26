package ca.ubc.cs.beta.instancespecificinfo;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;









import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.InstanceListWithSeeds;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceHelper;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceOptions;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.probleminstance.seedgenerator.InstanceSeedGenerator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.probleminstance.ProblemInstanceHelperTester;

public class AlgoExecutionInstanceSpecificInfoTest {

	@Test
	public void testInstanceSpecificInfoPassed()
	{

		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(InstanceSpecificInfoTestExecutor.class.getCanonicalName());
		
		
		
		
		File paramFile = TestHelper.getTestFile("testInfoSpecificParamExecution/testParam.txt");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(paramFile);
		
		AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, true, 500);
		
		TargetAlgorithmEvaluator tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
		
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatInstanceSeedSpecificValid.txt", false);
		
		ParameterConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		
		for(int i=0; i < ProblemInstanceHelperTester.NON_SPACE_INSTANCES; i++)
		{
			ProblemInstance pi = ilws.getInstances().get(i);
			
			InstanceSeedGenerator inst = ilws.getSeedGen();
			
			while(inst.hasNextSeed(pi))
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pi, inst.getNextSeed(pi)), 300, defaultConfig,execConfig);
				AlgorithmRunResult run = tae.evaluateRun(rc).get(0);
				
				try {
				assertEquals(pi.getInstanceName().hashCode() + 37*pi.getInstanceSpecificInformation().hashCode(), (long) run.getRunLength());
				} catch(AssertionError e)
				{
					System.out.println(run.getResultLine());
					System.out.println(run.toString());
					throw e;
				}
				
				
				
			}
			
		}
		
		tae.notifyShutdown();
	}
	
	@Test
	public void testInstanceSpecificInfoManju()
	{

		StringBuilder b = new StringBuilder();
		b.append("java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		b.append(InstanceSpecificInfoTestExecutor.class.getCanonicalName());
		
		
		
		
		File paramFile = TestHelper.getTestFile("testInfoSpecificParamExecution/testParam.txt");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(paramFile);
		
		AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration(b.toString(), System.getProperty("user.dir"), configSpace, false, true, 500);
		TargetAlgorithmEvaluator tae = CommandLineTargetAlgorithmEvaluatorFactory.getCLITAE();
		
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("manju.txt", false);
		
		ParameterConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		
		for(int i=0; i < ProblemInstanceHelperTester.NON_SPACE_INSTANCES; i++)
		{
			ProblemInstance pi = ilws.getInstances().get(i);
			
			InstanceSeedGenerator inst = ilws.getSeedGen();
			
			while(inst.hasNextSeed(pi))
			{
				AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pi, inst.getNextSeed(pi)), 300, defaultConfig,execConfig);
				AlgorithmRunResult run = tae.evaluateRun(rc).get(0);
				
				try {
				assertEquals(Math.abs((long) pi.getInstanceName().hashCode() + 37*pi.getInstanceSpecificInformation().hashCode()), (long) run.getRunLength());
				} catch(AssertionError e)
				{
					System.out.println(run.getResultLine());
					System.out.println(run.toString());
					throw e;
				}
				
				
				
			}
			
		}
		
		
	}
	
	@Test
	public void testInstanceSpecificInfoWithCheckFiles() throws IOException
	{
		ProblemInstanceHelper.clearCache();
		
		
		
		ProblemInstanceOptions opts = new ProblemInstanceOptions();
		
		
		opts.checkInstanceFilesExist = false;
		opts.instanceFile = "classicFormatInstanceSeedSpecificValid.txt";
		
		InstanceListWithSeeds ilws = opts.getTrainingProblemInstances("test-files/instanceSpecificCheck/", 2, false, false, false);
		
		for(ProblemInstance pi : ilws.getInstances())
		{
			assertEquals(pi.getInstanceName(),pi.getInstanceSpecificInformation());
		}
		ProblemInstanceHelper.clearCache();
		
		
		
		opts.checkInstanceFilesExist = true;
		//opts.instanceFile = "test-files/instanceFiles/classicFormatInstanceSeedSpecificValid.txt";
		
		ilws = opts.getTrainingProblemInstances("test-files/instanceSpecificCheck/", 2, false, false, false);
		
		for(ProblemInstance pi : ilws.getInstances())
		{
			assertEquals(new File(pi.getInstanceName()).getName(),pi.getInstanceSpecificInformation());
		}
		
		
	}
}
