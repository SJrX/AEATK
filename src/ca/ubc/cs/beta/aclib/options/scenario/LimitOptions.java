package ca.ubc.cs.beta.aclib.options.scenario;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

import ca.ubc.cs.beta.aclib.misc.jcommander.validator.FixedPositiveInteger;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.FixedPositiveLong;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.NonNegativeInteger;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.termination.CompositeTerminationCondition;
import ca.ubc.cs.beta.aclib.termination.TerminationCondition;
import ca.ubc.cs.beta.aclib.termination.standard.AlgorithmRunLimitCondition;
import ca.ubc.cs.beta.aclib.termination.standard.CPULimitCondition;
import ca.ubc.cs.beta.aclib.termination.standard.ModelBuiltTerminationCondition;
import ca.ubc.cs.beta.aclib.termination.standard.WallClockLimitCondition;

@UsageTextField(hiddenSection=true)
public class LimitOptions extends AbstractOptions {

	@Parameter(names="--tunerTimeout", description="limits the total cpu time allowed between SMAC and the target algorithm runs during the automatic configuration phase", validateWith=NonNegativeInteger.class)
	public int tunerTimeout = Integer.MAX_VALUE;
	
	@Parameter(names={"--numIterations","--numberOfIterations"}, description = "limits the number of iterations allowed during automatic configuration phase", validateWith=FixedPositiveInteger.class)
	public int numIteratations = Integer.MAX_VALUE;
	
	@Parameter(names={"--runtimeLimit", "--wallClockLimit"}, description = "limits the total wall-clock time allowed during the automatic configuration phase", validateWith=FixedPositiveInteger.class)
	public int runtimeLimit = Integer.MAX_VALUE;
	
	@Parameter(names={"--totalNumRunsLimit","--numRunsLimit","--numberOfRunsLimit"} , description = "limits the total number of target algorithm runs allowed during the automatic configuration phase", validateWith=FixedPositiveLong.class)
	public long totalNumRunsLimit = Long.MAX_VALUE;

	@Parameter(names="--countSMACTimeAsTunerTime", description="include the CPU Time of SMAC as part of the tunerTimeout")
	public boolean countSMACTimeAsTunerTime = true;
	
	public TerminationCondition getTerminationConditions()
	{
		List<TerminationCondition> termConds = new ArrayList<TerminationCondition>();
		
		termConds.add(new CPULimitCondition(tunerTimeout, countSMACTimeAsTunerTime));
		termConds.add(new WallClockLimitCondition(System.currentTimeMillis(),runtimeLimit));
		termConds.add(new AlgorithmRunLimitCondition(totalNumRunsLimit));
		termConds.add(new ModelBuiltTerminationCondition(this.numIteratations));
		
		return new CompositeTerminationCondition(termConds);
	}
}
