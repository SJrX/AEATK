package ca.ubc.cs.beta.configspace;

import static org.junit.Assert.fail;

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
	
	
	@Test
	public void testIntegerContinuousParameters() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/integerFormatParam.txt");
		File f = new File(url.getPath());
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

	

	@After
	public void tearDown()
	{
		System.out.println("Done");
	}
}
