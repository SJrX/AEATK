package ca.ubc.cs.beta.aclib.misc.jcommander;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ca.ubc.cs.beta.aclib.example.tae.TargetAlgorithmEvaluatorRunnerOptions;
import ca.ubc.cs.beta.aclib.help.HelpOptions;
import ca.ubc.cs.beta.aclib.misc.options.UsageSection;
import ca.ubc.cs.beta.aclib.misc.returnvalues.ACLibReturnValues;
import ca.ubc.cs.beta.aclib.misc.spi.SPIClassLoaderHelper;
import ca.ubc.cs.beta.aclib.misc.version.VersionTracker;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.options.ConfigToLaTeX;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.init.TargetAlgorithmEvaluatorLoader;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

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
	public static JCommander getJCommanderAndCheckForHelp(String[] args,AbstractOptions mainOptions,Map<String, AbstractOptions> taeOptions) {
		JCommander jcom = getJCommander(mainOptions, taeOptions);
		
		checkForHelpAndVersion(args, mainOptions, taeOptions);
		
		
		
		return jcom;
		
		
	}
	
	
	
}