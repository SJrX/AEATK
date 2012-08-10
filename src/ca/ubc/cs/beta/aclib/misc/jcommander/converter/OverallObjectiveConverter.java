package ca.ubc.cs.beta.aclib.misc.jcommander.converter;

import java.util.Arrays;

import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

public class OverallObjectiveConverter implements IStringConverter<OverallObjective> {

	@Override
	public OverallObjective convert(String arg0) {
		try { 
		return OverallObjective.valueOf(arg0.toUpperCase());
		} catch(IllegalArgumentException e)
		{
			throw new ParameterException("Illegal value specified for Overall Objective ("  + arg0 + "), allowed values are: " + Arrays.toString(OverallObjective.values()));
		}
	}

}
