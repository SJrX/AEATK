package ca.ubc.cs.beta.aeatk.initialization.doubleracing;

import com.beust.jcommander.Parameter;

import ca.ubc.cs.beta.aeatk.misc.jcommander.validator.FixedPositiveInteger;
import ca.ubc.cs.beta.aeatk.misc.options.OptionLevel;
import ca.ubc.cs.beta.aeatk.misc.options.UsageTextField;
import ca.ubc.cs.beta.aeatk.options.AbstractOptions;

@UsageTextField(hiddenSection = true)
public class DoubleRacingInitializationProcedureOptions extends AbstractOptions{

	@UsageTextField(level=OptionLevel.INTERMEDIATE)
	@Parameter(names={"--incumbent-runs-in-final-init-round","--incumbent-runs-final-init"}, description="number of runs that will be run for the incumbent in the final round of the double racing initialization", validateWith=FixedPositiveInteger.class)
	public int incumbentRunsInFinalInitRound = 8;

}
