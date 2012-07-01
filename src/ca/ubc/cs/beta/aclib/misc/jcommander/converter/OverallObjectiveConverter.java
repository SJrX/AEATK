package ca.ubc.cs.beta.aclib.misc.jcommander.converter;

import ca.ubc.cs.beta.aclib.objectives.OverallObjective;

import com.beust.jcommander.IStringConverter;

public class OverallObjectiveConverter implements IStringConverter<OverallObjective> {

	@Override
	public OverallObjective convert(String arg0) {
		return OverallObjective.valueOf(arg0.toUpperCase());
	}

}
