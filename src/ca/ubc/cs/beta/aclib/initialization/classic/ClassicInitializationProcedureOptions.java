package ca.ubc.cs.beta.aclib.initialization.classic;

import com.beust.jcommander.Parameter;

import ca.ubc.cs.beta.aclib.misc.jcommander.validator.FixedPositiveInteger;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;

@UsageTextField(hiddenSection = true)
public class ClassicInitializationProcedureOptions extends AbstractOptions{

	@Parameter(names={"--initial-incumbent-runs","--initialIncumbentRuns","--defaultConfigRuns"}, description="initial amount of runs to schedule against for the default configuration", validateWith=FixedPositiveInteger.class)
	public int initialIncumbentRuns = 1;

}
