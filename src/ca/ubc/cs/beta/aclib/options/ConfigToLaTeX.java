package ca.ubc.cs.beta.aclib.options;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ca.ubc.cs.beta.aclib.misc.options.DomainDisplay;
import ca.ubc.cs.beta.aclib.misc.options.UsageSection;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

public class ConfigToLaTeX {

	public static void main(String[] args) throws Exception
	{
		
		
		SMACOptions config = new SMACOptions();
		
		List<UsageSection> sections = getParameters(config);
		
		//usage(sections);
		
		latex(sections);
		//bash(sections);
	}

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
				pw.format("========== %-20s ==========%n%n", sec.getSectionName());
				pw.format("\t%s %n %n ", sec.getSectionDescription());
				pw.format("\tArguments:%n");
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
	public static void getAllObjects(Object o, Set<Object> objectsToScan) throws IllegalArgumentException, IllegalAccessException
	{
		
		
		
		for(Field f : o.getClass().getFields())
		{
		
			if(f.isAnnotationPresent(ParametersDelegate.class))
			{
			
				objectsToScan.add(f.get(o));
				getAllObjects(f.get(o), objectsToScan);
		
				
			}
		}
		
	}
	public static List<UsageSection> getParameters(Object o) throws IllegalArgumentException, IllegalAccessException, InstantiationException
	{
		
	
		
		
		Set<Object> objectsToScan = new LinkedHashSet<Object>();
		
		objectsToScan.add(o);
		
		getAllObjects(o, objectsToScan);
				
		StringBuilder sb = new StringBuilder();
		
		List<UsageSection> sections = new ArrayList<UsageSection>();
		
		for(Object obj : objectsToScan)
		{
			
			
			
			String title = getTitleForObject(obj);
			String sectionDescription = getDescriptionForObject(obj);
			boolean isHidden = isHiddenSection(obj);
			
			UsageSection sec = new UsageSection(title, sectionDescription,isHidden);
			sections.add(sec);
			for(Field f : obj.getClass().getFields())
			{
				if(f.isAnnotationPresent(Parameter.class))
				{
					
					Parameter param = getParameterAnnotation(f);
					
					String name = getNameForField(f);
					String defaultValue = getDefaultValueForField(f,obj);
					String description = getDescriptionForField(f,obj);
					boolean required = getRequiredForField(f,obj);
					String aliases = getAliases(f, obj);
					String domain = getDomain(f,obj);
					boolean hidden = param.hidden();
					
					sec.addAttribute(name, description, defaultValue, required,domain, aliases , hidden);
					
				}
				
				
				
			}
			//System.out.println(sec);
			
		}
		
		
		
		return sections;
		
		
		
		
		
	}



	
	private static boolean getRequiredForField(Field f, Object o) {
		return getParameterAnnotation(f).required();
		
	}



	private static String getDescriptionForField(Field f, Object o) {
		// TODO Auto-generated method stub
		return getParameterAnnotation(f).description();
	}



	private static String getDefaultValueForField(Field f, Object o) throws IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		
		UsageTextField latexAnnotation = getLatexField(f);
		if((latexAnnotation == null) || latexAnnotation.defaultValues().equals("<NOT SET>"))
		{
			Object value = f.get(o);
			
			if(value != null)
			{
				return value.toString();
			} else
			{
				return "null";
			}
			
		} else
		{
			return latexAnnotation.defaultValues();
		}
		
		
		
	}



	private static String getNameForField(Field f) {

		return getParameterAnnotation(f).names()[0];
	}

	
	private static String getAliases(Field f, Object o) {

		return Arrays.toString(getParameterAnnotation(f).names()).replaceAll("\\[", "").replaceAll("\\]","");
	}

	private static String getDomain(Field f, Object o) throws InstantiationException, IllegalAccessException {

		
		UsageTextField latex = getLatexField(f);
		
		if(latex != null && !latex.domain().equals("<NOT SET>"))
		{
			return latex.domain();
		}
		
		if(DomainDisplay.class.isAssignableFrom(getParameterAnnotation(f).converter()))
		{
			return ((DomainDisplay) getParameterAnnotation(f).converter().newInstance()).getDomain();
		}
		
		if(DomainDisplay.class.isAssignableFrom(getParameterAnnotation(f).validateWith()))
		{
			return ((DomainDisplay) getParameterAnnotation(f).validateWith().newInstance()).getDomain();
		}
		
		Object value = f.get(o);
		if(value != null)
		{
			return getDomainForClass(value.getClass());
		} else
		{
			return getDomainForClass(f.getType());
		}
		
		
		
		
		
	}

	private static String getDomainForClass(Class<?> x)
	{
		if(x.equals(Boolean.class))
		{
			return "{true, false}";
		}
		
		if(Enum.class.isAssignableFrom(x))
		{
			//SortedSet<String> options = new TreeSet<String>();
			
			//for(x.getDeclaredFields())
			return Arrays.toString(x.getEnumConstants()).replaceAll("\\[", "{").replaceAll("\\]","}");
		}
		
		return "";
	}
	private static Parameter getParameterAnnotation(Field f)
	{
		Parameter param  = (Parameter)f.getAnnotation(Parameter.class);
		return param;
	}

	
	private static String getTitleForObject(Object obj) {

		UsageTextField f = getLatexField(obj);
		if(f == null) return "";
		return f.title();
		
		}
	
	private static String getDescriptionForObject(Object obj) {

		UsageTextField f = getLatexField(obj);
		if(f == null) return "";
		return f.description();
		
		}
	

	private static boolean isHiddenSection(Object obj) {

		UsageTextField f = getLatexField(obj);
		if(f == null) return false;
		return f.hiddenSection();
		
		}
	
	

	private static UsageTextField getLatexField(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof Field)
		{
			UsageTextField f = ((Field)obj).getAnnotation(UsageTextField.class);
			return f;
		} else if(obj.getClass().isAnnotationPresent(UsageTextField.class))
		{
			UsageTextField f = obj.getClass().getAnnotation(UsageTextField.class);
			
			return f;
		} else
		{
			return null;
		}
	}
		
		
}
