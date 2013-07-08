package ca.ubc.cs.beta.aclib.misc.jcommander;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.ant.execscript.ExecScriptCreatorOptions;
import ca.ubc.cs.beta.aclib.help.HelpOptions;
import ca.ubc.cs.beta.aclib.misc.returnvalues.ACLibReturnValues;
import ca.ubc.cs.beta.aclib.misc.spi.SPIClassLoaderHelper;
import ca.ubc.cs.beta.aclib.misc.version.VersionTracker;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.options.ConfigToLaTeX;
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

	
	public static void parseCheckingForHelpAndVersion(AbstractOptions options, String[] args)
	{
		checkForHelpAndVersion(args, options,Collections.<String, AbstractOptions> emptyMap());
	}
	
	
	public static void checkForHelpAndVersion(String[] args, AbstractOptions options, Map<String, AbstractOptions> taeOpts)
	{
		
		
		//=== The arguments that we are searching for come from this class here.
		@SuppressWarnings("unused")
		HelpOptions helpOption = null;
		//=== I do this just in case the class is moved, so that javadoc isn't left out of date.
		
		
		
		try {
			Set<String> possibleValues = new HashSet<String>(Arrays.asList(args));
			
			String[] hiddenNames = {"--showHiddenParameters"};
			for(String helpName : hiddenNames)
			{
				if(possibleValues.contains(helpName))
				{
					ConfigToLaTeX.usage(ConfigToLaTeX.getParameters(options, taeOpts), true);
					System.exit(ACLibReturnValues.SUCCESS);
				}
			}
			
			String[] helpNames =  {"--help","-?","/?","-h"};
			for(String helpName : helpNames)
			{
				if(possibleValues.contains(helpName))
				{
					ConfigToLaTeX.usage(ConfigToLaTeX.getParameters(options, taeOpts));
					System.exit(ACLibReturnValues.SUCCESS);
				}
			}
			
			String[] versionNames = {"-v","--version"};
			for(String helpName : versionNames)
			{
				if(possibleValues.contains(helpName))
				{
					//Turn off logging
					System.setProperty("logback.configurationFile", "logback-off.xml");
					VersionTracker.setClassLoader(SPIClassLoaderHelper.getClassLoader());
					System.out.println("**** Version Information ****");
					System.out.println(VersionTracker.getVersionInformation());
					
					
					System.exit(ACLibReturnValues.SUCCESS);
				}
			}
			
			
			
			
		} catch (Exception e) {
			
			throw new IllegalStateException(e);
		}
		
		
		
	}

	/**
	 * Returns a JCommander object after screening for parameters that are asking for help or version information 
	 *  
	 * 
	 * @param args
	 * @param mainOptions
	 * @param taeOptions
	 * 
	 * @return
	 */
	public static JCommander getJCommanderAndCheckForHelp(String[] args,AbstractOptions mainOptions) {
		JCommander jcom = getJCommander(mainOptions, Collections.<String, AbstractOptions> emptyMap());
		checkForHelpAndVersion(args, mainOptions, Collections.<String, AbstractOptions> emptyMap());
		return jcom;
		
		
	}
	
	
	/**
	 * Returns a JCommander object after screening for parameters that are asking for help or version information 
	 *  
	 * 
	 * @param args
	 * @param mainOptions
	 * @param taeOptions
	 * 
	 * @return
	 */
	public static JCommander getJCommanderAndCheckForHelp(String[] args,AbstractOptions mainOptions,Map<String, AbstractOptions> taeOptions) {
		JCommander jcom = getJCommander(mainOptions, taeOptions);
		
		checkForHelpAndVersion(args, mainOptions, taeOptions);
		
		
		
		return jcom;
		
		
	}
	
	public static void logCallString(String[] args, Class<?> c) {
		Logger log = LoggerFactory.getLogger(JCommanderHelper.class);
		StringBuilder sb = new StringBuilder("java -cp ");
		sb.append(System.getProperty("java.class.path")).append(" ");
		sb.append(c.getCanonicalName()).append(" ");
		for(String arg : args)
		{
			boolean escape = false;
			if(arg.contains(" "))
			{
				escape = true;
				arg = arg.replaceAll(" ", "\\ ");
			}
			
			
			if(escape) sb.append("\"");
			sb.append(arg);
			if(escape) 	sb.append("\"");
			sb.append(" ");
		}
		
		log.info("Call String:");
		log.info("{}", sb.toString());
	}

	
	
	
}