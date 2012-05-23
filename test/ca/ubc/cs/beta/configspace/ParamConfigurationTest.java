package ca.ubc.cs.beta.configspace;

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

	@After
	public void tearDown()
	{
		System.out.println("Done");
	}
}
