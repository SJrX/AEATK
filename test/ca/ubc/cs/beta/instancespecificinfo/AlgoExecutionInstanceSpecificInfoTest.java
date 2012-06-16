package ca.ubc.cs.beta.instancespecificinfo;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;



import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.ac.config.ProblemInstance;
import ca.ubc.cs.beta.ac.config.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.ac.config.RunConfig;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.configspace.ParamConfiguration;
import ca.ubc.cs.beta.configspace.ParamConfigurationSpace;

import ca.ubc.cs.beta.probleminstance.InstanceListWithSeeds;
import ca.ubc.cs.beta.probleminstance.ProblemInstanceHelperTester;
import ca.ubc.cs.beta.seedgenerator.InstanceSeedGenerator;
import ca.ubc.cs.beta.smac.ac.runners.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun;

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
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(paramFile);
		
		AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false);
		TargetAlgorithmEvaluator tae = new TargetAlgorithmEvaluator( execConfig, false);
		
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("classicFormatInstanceSeedSpecificValid.txt", false);
		
		ParamConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		
		for(int i=0; i < ProblemInstanceHelperTester.NON_SPACE_INSTANCES; i++)
		{
			ProblemInstance pi = ilws.getInstances().get(i);
			
			InstanceSeedGenerator inst = ilws.getSeedGen();
			
			while(inst.hasNextSeed(pi))
			{
				RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(pi, inst.getNextSeed(pi)), 300, defaultConfig);
				AlgorithmRun run = tae.evaluateRun(rc).get(0);
				
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
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(paramFile);
		
		AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig(b.toString(), System.getProperty("user.dir"), configSpace, false);
		TargetAlgorithmEvaluator tae = new TargetAlgorithmEvaluator( execConfig, false);
		
		InstanceListWithSeeds ilws = ProblemInstanceHelperTester.getInstanceListWithSeeds("manju.txt", false);
		
		ParamConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		
		for(int i=0; i < ProblemInstanceHelperTester.NON_SPACE_INSTANCES; i++)
		{
			ProblemInstance pi = ilws.getInstances().get(i);
			
			InstanceSeedGenerator inst = ilws.getSeedGen();
			
			while(inst.hasNextSeed(pi))
			{
				RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(pi, inst.getNextSeed(pi)), 300, defaultConfig);
				AlgorithmRun run = tae.evaluateRun(rc).get(0);
				
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
		
		
	}
}
