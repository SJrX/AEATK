package ca.ubc.cs.beta.aclib.misc.jcommander;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import ca.ubc.cs.beta.aclib.options.AbstractOptions;

import com.beust.jcommander.JCommander;

public final class JCommanderHelper
{

	public static JCommander getJCommander(AbstractOptions opts, Map<String, AbstractOptions> taeOpts)
	{

		ArrayList<Object> allOptions = new ArrayList<Object>();
		
		allOptions.add(opts);
		for(Entry<String, AbstractOptions> ent : taeOpts.entrySet())
		{
			if(ent.getValue() != null)
			{
				allOptions.add(ent.getValue());
			}
		}
		JCommander com = new JCommander(allOptions.toArray(), true, true);
		return com;
		
	}
	
}