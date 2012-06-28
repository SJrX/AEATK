package ca.ubc.cs.beta.state.legacy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.smac.OverallObjective;
import ca.ubc.cs.beta.smac.RunObjective;
import ca.ubc.cs.beta.smac.state.StateFactory;
import ca.ubc.cs.beta.smac.state.legacy.LegacyStateFactory;
import ca.ubc.cs.beta.smac.exceptions.*;

public class LegacyStateDeserializerTester {

	
	private static List<ProblemInstance> instances = new ArrayList<ProblemInstance>();
	 
	private static final List<ProblemInstance> emptyInstanceList = Collections.emptyList();
	
	private static ParamConfigurationSpace configSpace = new ParamConfigurationSpace(TestHelper.getTestFile("paramFiles/simpleParam.txt"));
	
	private static AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig("foo", "bar", configSpace, false);
	
	
	@BeforeClass
	public static void init()
	{
		for(int i=0; i < 1000; i++)
		{
			
			
			instances.add(new ProblemInstance("Instance " + i,i));
		}
	
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
		
		
		sf.getStateDeserializer("valid", 4, configSpace, OverallObjective.MEAN10, OverallObjective.MEAN, RunObjective.RUNTIME, instances, execConfig);
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
	
}
