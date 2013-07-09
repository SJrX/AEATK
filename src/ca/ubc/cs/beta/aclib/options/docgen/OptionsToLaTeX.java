package ca.ubc.cs.beta.aclib.options.docgen;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import ca.ubc.cs.beta.aclib.misc.bashcompletion.BashCompletionOptions;
import ca.ubc.cs.beta.aclib.misc.options.DomainDisplay;
import ca.ubc.cs.beta.aclib.misc.options.UsageSection;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.smac.SMACOptions;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

public class OptionsToLaTeX {

	public static void main(String[] args) throws Exception
	{		
		
		Object obj = Class.forName(args[0]);
		List<UsageSection> sections = UsageSectionGenerator.getUsageSections(obj);
		
		latex(sections);
	}

	
	
	
	public static void latex(List<UsageSection> sections)
	{
		StringWriter s = new StringWriter();
		PrintWriter pw = new PrintWriter(s);
		//pw.append("\\documentclass[a4paper,12pt]{article}\n");
		pw.append("\\documentclass[manual.tex]{subfiles}");
		pw.append("\\begin{document}\n");
		for(UsageSection sec : sections)
		{
			
			boolean isHiddenSection = sec.isSectionHidden();

			if(!isHiddenSection)
			{
				pw.append("\t\\subsubsection{").append(sec.getSectionName()).append("}\n\n");
				pw.append(sec.getSectionDescription()).append("\n");
				
			}
			pw.append("\t\\begin{description}");
			for(String name : sec)
			{
				
				if(sec.isAttributeHidden(name)) continue;
				String printedName = name.replaceAll("-", "-~\\$\\\\!\\$");
				pw.append("\t\t\\item[").append(printedName).append("]");
				String description = sec.getAttributeDescription(name);
				//== Escape some special characters
				description = description.replaceAll("\\_", "\\\\_");
				description = description.replaceAll(">=","\\$\\\\geq\\$");
				description = description.replaceAll("<","\\$<\\$");
				description = description.replaceAll(">","\\$>\\$");
				description = description.replaceAll("\\*", "\\$\\\\times\\$");
				description = description.replaceAll("--", "-~\\$\\\\!\\$-");
				description = description.replaceAll("&", "\\\\&");
				pw.append(" ").append(description).append("\n\n");
				
				pw.append("\t\t\\begin{description}\n");
				
				
				
				if(sec.isAttributeRequired(name))
				{
					pw.append("\t\t\t\\item[REQUIRED]\n");
				}
				pw.format("\t\t\t\\item[Aliases:] %s %n", sec.getAttributeAliases(name).replaceAll("\\_", "\\\\_").replaceAll("--", "-~\\$\\\\!\\$-"));
				if(sec.getAttributeDefaultValues(name).length() > 0)
				{
					String defaultValue = sec.getAttributeDefaultValues(name);
					defaultValue = defaultValue.replaceAll("<","\\$<\\$");
					defaultValue = defaultValue.replaceAll(">","\\$>\\$");
					pw.format("\t\t\t\\item[Default Value:] %s %n", defaultValue);
				}
				
				if(sec.getAttributeDomain(name).length() > 0)
				{
					String domain = sec.getAttributeDomain(name);
					domain = domain.replaceAll("\\{", "\\$\\\\{");
					domain = domain.replaceAll("\\}", "\\\\}\\$");
					domain = domain.replaceAll("Infinity","\\$\\\\infty\\$");
					domain = domain.replaceAll(" U ", " \\$\\\\bigcup\\$ ");
					
					
					pw.format("\t\t\t\\item[Domain:] %s %n", domain);
				}
				
				pw.append("\t\t\\end{description}\n");
				
				
			}
			
			
			pw.append("\t\\end{description}\n\n");
			
			
		}
		
		pw.append("\\end{document}");
		
		pw.flush();
		System.out.println(s.toString());
		
	}
	
	
		
}
