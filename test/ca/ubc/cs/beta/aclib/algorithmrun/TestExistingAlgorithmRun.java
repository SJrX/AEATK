package ca.ubc.cs.beta.aclib.algorithmrun;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

public class TestExistingAlgorithmRun {

	@Test
	public void testAdditionalRunWeirdCharacters()
	{
		AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig("", "", ParamConfigurationSpace.getSingletonConfigurationSpace(), false, false, 0);
		
		RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("test"),2), 25.0, ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(), execConfig );
		
		AlgorithmRun run = new ExistingAlgorithmRun(execConfig, rc, RunResult.SAT, 10, 10, 10, 2, "Test", 0);
		
		assertEquals(run.getAdditionalRunData(),"Test");
		
		
		run = new ExistingAlgorithmRun(execConfig, rc, RunResult.SAT, 10, 10, 10, 2, "Test\n\nTwo", 0);
		
		assertEquals("Test\\n\\nTwo",run.getAdditionalRunData());

		run = new ExistingAlgorithmRun(execConfig, rc, RunResult.SAT, 10, 10, 10, 2, "Test,Two", 0);
		
		assertEquals("Test;Two",run.getAdditionalRunData());
	}
}
