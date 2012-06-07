package ca.ubc.cs.beta;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.ubc.cs.beta.configspace.*;
import ca.ubc.cs.beta.probleminstance.*;
import ca.ubc.cs.beta.instancespecificinfo.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ParamConfigurationTest.class,
	ProblemInstanceHelperTester.class,
	AlgoExecutionInstanceSpecificInfoTest.class
})

public class ACLibTestSuite {

}
