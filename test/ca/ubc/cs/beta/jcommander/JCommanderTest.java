package ca.ubc.cs.beta.jcommander;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import ca.ubc.cs.beta.aclib.misc.jcommander.JCommanderHelper;
import ca.ubc.cs.beta.aclib.options.scenario.ScenarioOptions;

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
	
}
