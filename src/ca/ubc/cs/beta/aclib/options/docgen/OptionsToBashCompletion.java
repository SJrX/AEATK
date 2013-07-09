package ca.ubc.cs.beta.aclib.options.docgen;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import ca.ubc.cs.beta.aclib.misc.options.UsageSection;

public class OptionsToBashCompletion {

	public static void bash(List<UsageSection> sections)
	{
		StringWriter s = new StringWriter();
		PrintWriter pw = new PrintWriter(s);
		
		
		SortedSet<String> sorted = new TreeSet<String>();
		
		for(UsageSection sec : sections)
		{
			for(String attr : sec)
			{
				//pw.append(attr);
				sorted.addAll(Arrays.asList(sec.getAttributeAliases(attr).replaceAll(","," ").split(" ")));
			}
		}
		

		for(String key : sorted)
		{
			if(key.trim().startsWith("--"))
			{
				pw.append(key);
				pw.append(" ");
			}
			
		}
		System.out.println(s.toString());
	}
	
}
