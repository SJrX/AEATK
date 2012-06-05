package ca.ubc.cs.beta.jcommander.converter;

import ca.ubc.cs.beta.smac.RunObjective;

import com.beust.jcommander.IStringConverter;

public class RunObjectiveConverter implements IStringConverter<RunObjective> {

	@Override
	public RunObjective convert(String arg0) {
		return RunObjective.valueOf(arg0.toUpperCase());
	}

}
