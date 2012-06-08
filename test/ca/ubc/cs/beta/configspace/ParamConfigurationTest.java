package ca.ubc.cs.beta.configspace;

import static org.junit.Assert.*;
import java.io.File;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ParamConfigurationTest {

	@Before
	public void setUp()
	{
		
	}
	
	
	private ParamConfigurationSpace getConfigSpaceForFile(String f)
	{
		URL url = this.getClass().getClassLoader().getResource(f);
		File file = new File(url.getPath());
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(file);
		return configSpace;
	}
	
	@Test
	public void testIntegerContinuousParameters() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/integerFormatParam.txt");
		File f = new File(url.getPath());
		System.out.println(f.getAbsolutePath());
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(f);
		ParamConfiguration config = configSpace.getDefaultConfiguration();
		System.out.println(config.getFormattedParamString());
		
	}
	
	/**
	 * See Bug #1274
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidArgumentParameter() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/invalidDefaultParam.txt");
		File f = new File(url.getPath());
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(f);
		try { 
		ParamConfiguration config = configSpace.getDefaultConfiguration();
		System.out.println(config.getFormattedParamString());
		} catch(IllegalArgumentException e)
		{
			fail("The Config Space should have thrown this exception");
			
		}
		
		
	}

	
	@Test
	public void testForbidden() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/forbiddenExampleParam.txt");
		File f = new File(url.getPath());
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(f);
		ParamConfiguration config = configSpace.getDefaultConfiguration();
		System.out.println(config.getFormattedParamString());
		
		assertFalse(config.isForbiddenParamConfiguration());
		config.put("a", "v2");
		config.put("b", "w2");
		assertTrue(config.isForbiddenParamConfiguration());
		
	}
	
	@Test
	public void testNameThenSquareBracket()
	{
		//name[ may fail
		ParamConfiguration config = getConfigSpaceForFile("paramFiles/continuousNameNoSpaceParam.txt").getDefaultConfiguration();
		
		double d = Double.valueOf(config.get("name"));
		
		if( d > 0.45 && d < 0.55)
		{
			
		} else
		{
			fail("Value should have been 0.5");
		}
		
		System.out.println("Result: " + config.getFormattedParamString());
		
	}
	
	public void testEmptyValue()
	{
		
	}
	

	@After
	public void tearDown()
	{
		System.out.println("Done");
	}
}
