package ca.ubc.cs.beta.aeatk.algorithmrun;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.ExistingAlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;

public class TestExistingAlgorithmRun {

	@Test
	public void testAdditionalRunWeirdCharacters()
	{
		AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("", "", ParameterConfigurationSpace.getSingletonConfigurationSpace(), false, false, 0);
		
		AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("test"),2), 25.0, ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(), execConfig );
		
		AlgorithmRunResult run = new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.SAT, 10, 10, 10, 2, "Test", 0);
		
		assertEquals(run.getAdditionalRunData(),"Test");
		
		
		run = new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.SAT, 10, 10, 10, 2, "Test\n\nTwo", 0);
		
		assertEquals("Test\\n\\nTwo",run.getAdditionalRunData());

		run = new ExistingAlgorithmRunResult(execConfig, rc, RunStatus.SAT, 10, 10, 10, 2, "Test,Two", 0);
		
		assertEquals("Test;Two",run.getAdditionalRunData());
		
	}
}
