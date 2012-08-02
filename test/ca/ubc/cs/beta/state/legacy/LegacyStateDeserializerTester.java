package ca.ubc.cs.beta.state.legacy;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.exceptions.*;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.runhistory.RunData;
import ca.ubc.cs.beta.aclib.state.StateDeserializer;
import ca.ubc.cs.beta.aclib.state.StateFactory;
import ca.ubc.cs.beta.aclib.state.legacy.LegacyStateFactory;

public class LegacyStateDeserializerTester {

	
	private static List<ProblemInstance> instances = new ArrayList<ProblemInstance>();
	 
	private static final List<ProblemInstance> emptyInstanceList = Collections.emptyList();
	
	private static ParamConfigurationSpace configSpace = new ParamConfigurationSpace(TestHelper.getTestFile("paramFiles/simpleParam.txt"));
	
	private static AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig("foo", "bar", configSpace, false, false, 300);
	
	
	@BeforeClass
	public static void init()
	{
		for(int i=0; i < 1000; i++)
		{
			
			
			instances.add(new ProblemInstance("Instance " + i,i));
		}
	
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
		
		
		sf.getStateDeserializer("unknown_instance", 4, configSpace, OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME, emptyInstanceList, execConfig);
	}
	
	@Test
	public void validInstance()
	{
		File f = TestHelper.getTestFile("stateFiles");
		StateFactory sf = new LegacyStateFactory(null,f.getAbsolutePath());	
		
		startOutputCapture();
		sf.getStateDeserializer("valid", 4, configSpace, OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME, instances, execConfig);
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
		
		
		sf.getStateDeserializer("inf", 4, configSpace, OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME, instances, execConfig);
	}
	
	@Test
	public void validInstanceLoweredCaptime()
	{
		
		File f = TestHelper.getTestFile("stateFiles");
		StateFactory sf = new LegacyStateFactory(null,f.getAbsolutePath());	
		
		 AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig("foo", "bar", configSpace, false, false, 1);
		 startOutputCapture();
		StateDeserializer sd = sf.getStateDeserializer("valid", 4, configSpace, OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME, instances, execConfig);
		String output = stopOutputCapture();
		assertTrue(output.contains("Cutoff time discrepancy"));
		assertFalse(output.contains("marking run as TIMEOUT and Censored"));
		assertTrue(output.contains("marking run as TIMEOUT with runtime 1.0"));
		for(RunData runData : sd.getRunHistory().getAlgorithmRunData())
		{
			if(runData.getRun().getRunResult().equals(RunResult.TIMEOUT))
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
		
		 AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig("foo", "bar", configSpace, false, false, 1000);
		

		 startOutputCapture();
		StateDeserializer sd = sf.getStateDeserializer("valid", 4, configSpace, OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME, instances, execConfig);
			String output = stopOutputCapture();
			assertTrue(output.contains("Cutoff time discrepancy"));
			assertTrue(output.contains("marking run as TIMEOUT and Censored"));
			assertFalse(output.contains("marking run as TIMEOUT with runtime"));
			
		for(RunData runData : sd.getRunHistory().getAlgorithmRunData())
		{
			if(runData.getRun().getRunResult().equals(RunResult.TIMEOUT))
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
	
}
