package ca.ubc.cs.beta;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.ubc.cs.beta.configspace.*;
import ca.ubc.cs.beta.probleminstance.*;
import ca.ubc.cs.beta.runhistory.RunHistoryTester;
import ca.ubc.cs.beta.state.legacy.LegacyStateDeserializerTester;
import ca.ubc.cs.beta.targetalgorithmevaluator.TAETestSet;
import ca.ubc.cs.beta.instancespecificinfo.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ParamConfigurationTest.class,
	RandomConfigurationTest.class,
	ProblemInstanceHelperTester.class,
	AlgoExecutionInstanceSpecificInfoTest.class,
	TAETestSet.class,
	BuggyFeatureFilesTester.class,
	LegacyStateDeserializerTester.class,
	RunHistoryTester.class
})

public class ACLibTestSuite {

}
