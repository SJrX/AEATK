package ca.ubc.cs.beta;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.ubc.cs.beta.acquisitionfunction.AcquisitionFunctionTester;
import ca.ubc.cs.beta.aeatk.algorithmrun.TestExistingAlgorithmRun;
import ca.ubc.cs.beta.configspace.ForbiddenOperatorsTest;
import ca.ubc.cs.beta.configspace.ParamConfigurationTest;
import ca.ubc.cs.beta.configspace.ParamConfigurationTestNewPCS;
import ca.ubc.cs.beta.configspace.RandomConfigurationTest;
import ca.ubc.cs.beta.eventsystem.EventManagerTester;
import ca.ubc.cs.beta.misc.CPUTimeTest;
import ca.ubc.cs.beta.objectives.RunObjectiveTester;
import ca.ubc.cs.beta.probleminstance.BuggyFeatureFilesTester;
import ca.ubc.cs.beta.probleminstance.ProblemInstanceHelperTester;
import ca.ubc.cs.beta.runhistory.RunHistoryTester;
import ca.ubc.cs.beta.state.legacy.LegacyStateDeserializerTester;
import ca.ubc.cs.beta.targetalgorithmevaluator.CachingTAETestSet;
import ca.ubc.cs.beta.targetalgorithmevaluator.DynamicCappingTestSet;
import ca.ubc.cs.beta.targetalgorithmevaluator.RetryCrashedTAETester;
import ca.ubc.cs.beta.targetalgorithmevaluator.TAETestSet;
import ca.ubc.cs.beta.instancespecificinfo.*;
import ca.ubc.cs.beta.jcommander.JCommanderTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	JCommanderTest.class,
	TestExistingAlgorithmRun.class,
	AcquisitionFunctionTester.class,
	EventManagerTester.class,
	DynamicCappingTestSet.class,
	ForbiddenOperatorsTest.class,
	ParamConfigurationTest.class,
	ParamConfigurationTestNewPCS.class,
	RandomConfigurationTest.class,
	ProblemInstanceHelperTester.class,
	AlgoExecutionInstanceSpecificInfoTest.class,
	TAETestSet.class,
	BuggyFeatureFilesTester.class,
	LegacyStateDeserializerTester.class,
	RunHistoryTester.class, 
	RetryCrashedTAETester.class,
	RunObjectiveTester.class,
	CPUTimeTest.class,
	CachingTAETestSet.class
})

public class AEATKTestSuite {

}
