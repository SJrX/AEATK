package ca.ubc.cs.beta.jcommander;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.beust.jcommander.Parameter;

import ca.ubc.cs.beta.aeatk.misc.jcommander.JCommanderHelper;
import ca.ubc.cs.beta.aeatk.options.AbstractOptions;
import ca.ubc.cs.beta.aeatk.options.scenario.ScenarioOptions;

public class JCommanderTest {

	@Test
	public void testScenarioFile()
	{
	
		ScenarioOptions scen = new ScenarioOptions();
		
	
		String[] args = { "--scenarioFile", System.getProperty("user.dir") + File.separator + "test-files" + File.separator + "scenarioFile" + File.separator + "scen.txt"}; 
		JCommanderHelper.parseCheckingForHelpAndVersion(args, scen);
		System.out.println(scen.toString());
		assertTrue(scen.algoExecOptions.algoExec.contains("C:\\"));
		
	}
	
	enum TestValue
	{
		UPPER,
		Mixed,
		lower;
	}

	
	public class TestOptions extends AbstractOptions
	{
		@Parameter(names={"--test"}, description="Test")
		TestValue v = TestValue.UPPER;
	}
	
	/**
	 * This actually tests the Jcommander JAR and is related to bug 1960 (which was patched in JCommander).
	 */
	@Test
	public void testLowerCaseEnum()
	{
		
		for (TestValue v : TestValue.values())
		{
			String[] args = { "--test", v.toString()};
			TestOptions to = new TestOptions();
			JCommanderHelper.parseCheckingForHelpAndVersion(args, to);
			System.out.println(args);
		}
		
	}
	
}
