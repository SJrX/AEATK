package ca.ubc.cs.beta.aclib.options.docgen;

import java.io.PrintWriter;
import java.util.List;

import ca.ubc.cs.beta.aclib.misc.options.UsageSection;

public class OptionsToUsage {

	public static void usage(List<UsageSection> sections)
	{
		usage(sections, false);
	}
	
	public static void usage(List<UsageSection> sections, boolean showHidden)
	{
		PrintWriter pw = new PrintWriter(System.out);
		
		System.out.println("Usage:\n");
		for(UsageSection sec : sections)
		{
			if(!sec.isSectionHidden())
			{
				pw.format(sec.getSectionBanner(), sec.getSectionName());
				pw.format("\t%s %n %n", sec.getSectionDescription());
				
				if(sec.getNumberOfAttributes()!=0)
				{
					pw.format("\tArguments:%n");
				}
				
			}
			for(String name : sec)
			{
				
				String required = "    ";
				
				if(sec.isAttributeHidden(name) && showHidden)
				{
					required = "[H]";
				} else if(sec.isAttributeHidden(name))
				{
					continue;
				} else
				{
					required = "   ";
				}
				
				if(sec.isAttributeRequired(name))
				{
					required = "[R]";
				}
				
				
				pw.format("%-10s %s %n", required, sec.getAttributeAliases(name));
				if(sec.getAttributeDescription(name).trim().length() > 0)
				{
					pw.format("\t\t%s%n", sec.getAttributeDescription(name));
				} else
				{
					
					System.err.println(name + " has no DESCRIPTION");
					System.err.flush();
				}
				
				if(sec.getAttributeDomain(name).trim().length() > 0)
				{
					pw.format("\t\tDomain: %41s%n", sec.getAttributeDomain(name));
				}
				
				
				if(!sec.isAttributeRequired(name) && sec.getAttributeDefaultValues(name).trim().length() > 0)
				{
					pw.format("\t\tDefault: %40s%n", sec.getAttributeDefaultValues(name));
				}
				
				pw.format("%n");
			}
		}
		
		pw.flush();
		
		System.out.println("\t[R] denotes a parameter is required");
		
		if(showHidden)
		{
			System.out.println("\t[H] denotes a parameter that is hidden and not to be trifled with");
		}
		
	}
	
}
