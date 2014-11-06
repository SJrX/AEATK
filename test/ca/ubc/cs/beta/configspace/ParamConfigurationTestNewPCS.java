package ca.ubc.cs.beta.configspace;



import static org.junit.Assert.*;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aeatk.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aeatk.misc.watch.AutoStartStopWatch;
import ca.ubc.cs.beta.aeatk.misc.watch.StopWatch;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationStringFormatException;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParamFileHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration.ParameterStringFormat;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;

import com.beust.jcommander.internal.Lists;

import ec.util.MersenneTwister;
import ec.util.MersenneTwisterFast;

@SuppressWarnings({"unused", "deprecation","unchecked"})
public class ParamConfigurationTestNewPCS {

	private final int NUMBER_OF_NEIGHBOURS = 4;
	@BeforeClass
	public static void setUpClass()
	{
		rand = new MersenneTwister();
		
	}
	@Before
	public void setUp()
	{
		long time = System.currentTimeMillis();
		
	}
	
	private static Random rand;
	
	public static ParameterConfigurationSpace getConfigSpaceForFile(String f)
	{
		URL url = ParameterConfigurationSpace.class.getClassLoader().getResource(f);
		File file = new File(url.getPath());
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(file);
		return configSpace;
	}
	
	
	@Test
	public void testAClibFormat() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/aclib2.txt");
		File f = new File(url.getPath());
		System.out.println(f.getAbsolutePath());
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(f);
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		System.out.println(config.getFormattedParameterString());
		//File is parsed correctly
	}
	
/*	
	@Test
	public void testRegexRange() {
		System.out.println("STart");
		String in = "int i [1,10][3]";
		String pat ="[ ]*(?<name>\\p{Alnum}+)[ ]*(?<type>[ir]+)[ ]*\\[[ ]*(?<min>\\p{Graph}+)[ ]*,[ ]*(?<max>\\p{Graph}+)[ ]*\\][ ]*\\[(?<default>\\p{Graph}+)\\][ ]*(?<log>(log)?)[ ]*"; 
		pat = "\\s*(?<name>\\p{Alnum}+)\\s*(?<type>[ir])\\s*\\[\\s*(?<min>\\p{Graph}+)\\s*,\\s*(?<max>\\p{Graph}+)\\s*\\]\\s*\\[(?<default>\\p{Graph}+)\\]\\s*(?<log>(log)?)\\s*";
		Pattern catOrdPattern = Pattern.compile(pat);		
		Matcher catOrdMatcher = catOrdPattern.matcher(in);
		System.out.println(catOrdMatcher);
		while (catOrdMatcher.find())
		{
			System.out.println("int/real");
			System.out.println(catOrdMatcher.group("name"));
			System.out.println(catOrdMatcher.group("type"));
			System.out.println(catOrdMatcher.group("min"));
			System.out.println(catOrdMatcher.group("max"));
			System.out.println(catOrdMatcher.group("default"));
			System.out.println(catOrdMatcher.group("log"));
		}
	}
	
	@Test
	public void testRegexCat() {
		System.out.println("STart");
		String in = "cat c {a,b,c}[a]";
		String pat ="\\s*(?<name>\\p{Alnum}+)\\s*(?<type>[co])\\s*\\{(?<values>.*)\\}\\s*\\[(?<default>\\p{Graph}+)\\]\\s*"; 
		Pattern catOrdPattern = Pattern.compile(pat);		
		Matcher catOrdMatcher = catOrdPattern.matcher(in);
		System.out.println(catOrdMatcher);
		while (catOrdMatcher.find())
		{
			System.out.println("int/real");
			System.out.println(catOrdMatcher.group("name"));
			System.out.println(catOrdMatcher.group("type"));
			System.out.println(catOrdMatcher.group("values"));
			System.out.println(catOrdMatcher.group("default"));
		}
	}
	*/	
		
	@After
	public void tearDown()
	{
		System.out.println("Done");
	}
}
