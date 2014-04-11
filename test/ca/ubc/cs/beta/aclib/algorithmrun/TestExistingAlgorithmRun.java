package ca.ubc.cs.beta.aclib.algorithmrun;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.RunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;

public class TestExistingAlgorithmRun {

	@Test
	public void testAdditionalRunWeirdCharacters()
	{
		AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("", "", ParamConfigurationSpace.getSingletonConfigurationSpace(), false, false, 0);
		
		AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(new ProblemInstance("test"),2), 25.0, ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(), execConfig );
		
		AlgorithmRun run = new ExistingAlgorithmRun(execConfig, rc, RunResult.SAT, 10, 10, 10, 2, "Test", 0);
		
		assertEquals(run.getAdditionalRunData(),"Test");
		
		
		run = new ExistingAlgorithmRun(execConfig, rc, RunResult.SAT, 10, 10, 10, 2, "Test\n\nTwo", 0);
		
		assertEquals("Test\\n\\nTwo",run.getAdditionalRunData());

		run = new ExistingAlgorithmRun(execConfig, rc, RunResult.SAT, 10, 10, 10, 2, "Test,Two", 0);
		
		assertEquals("Test;Two",run.getAdditionalRunData());
	}
}
