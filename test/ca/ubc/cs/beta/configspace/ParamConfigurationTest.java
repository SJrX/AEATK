package ca.ubc.cs.beta.configspace;



import static org.junit.Assert.*;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
public class ParamConfigurationTest {

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
	
	private static double EPSILON = Math.pow(10, -4);
	
	public static void assertDEquals(double d1, double d2)
	{
		//System.out.println(d1 + " = " + d2 + "?");
		if(Double.isNaN(d1) && Double.isNaN(d2))
		{
			return;
		}
		//System.out.println("delta: ~10^" + Math.round(Math.log10(d2-d1)));
		//System.out.flush();
		
		assertTrue(Math.abs(d1 - d2) < EPSILON);
	}	
	
	@Test
	public void testIntegerContinuousParameters() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/integerFormatParam.txt");
		File f = new File(url.getPath());
		System.out.println(f.getAbsolutePath());
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(f);
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		System.out.println(config.getFormattedParameterString());
		//File is parsed correctly
		
	}
	
	/**
	 * See Bug #1274
	 */
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidArgumentParameter() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/invalidDefaultParam.txt");
		File f = new File(url.getPath());
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(f);
		try { 
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		System.out.println(config.getFormattedParameterString());
		} catch(IllegalArgumentException e)
		{
			fail("The Config Space should have thrown this exception");
			
		}
		
		
	}

	
	@Test
	public void testForbidden() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/forbiddenExampleParam.txt");
		File f = new File(url.getPath());
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(f);
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		System.out.println(config.getFormattedParameterString());
		
		assertFalse(config.isForbiddenParameterConfiguration());
		config.put("a", "v2");
		config.put("b", "w2");
		assertTrue(config.isForbiddenParameterConfiguration());
		
	}
	
	@Test
	public void testNameThenSquareBracket()
	{
		//name[ may fail
		ParameterConfiguration config = getConfigSpaceForFile("paramFiles/continuousNameNoSpaceParam.txt").getDefaultConfiguration();
		
		double d = Double.valueOf(config.get("name"));
		
		if( d > 0.45 && d < 0.55)
		{
			
		} else
		{
			fail("Value should have been 0.5");
		}
		
		System.out.println("Result: " + config.getFormattedParameterString());
		
	}
	
	public void testEmptyValue()
	{
		
	}
	

	/**
	 * Tests what happens when we specify a value for a parameter that is not in it's domain
	 */
	@Test(expected=ParameterConfigurationStringFormatException.class)
	public void testParamNotInvalidValue()
	{
		
		
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/daisy-chain-param.txt");
		
		
		
		p.getParameterConfigurationFromString("-Pa=99989", ParameterStringFormat.SURROGATE_EXECUTOR);
		
		
		fail();
		
		
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);
		
		 
	}
	
	/**
	 * Tests what happens if we specify a parameter that does not appear in the file
	 */
	@Test(expected=ParameterConfigurationStringFormatException.class)
	public void testParamNotInParameterFile()
	{
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/daisy-chain-param.txt");
		
		
		
		p.getParameterConfigurationFromString("-Pa=2 -Pf=3", ParameterStringFormat.SURROGATE_EXECUTOR);
		
		
		fail();

	}
	
	
	
	
	/**
	 * All following tests are assuming the validation is correct, and are concerned
	 * only with the active parameters being correct
	 * 
	 *  Most Files end up with e as the most specific parameter to make active
	 */
	
	/**
	 * Tests the Daisy Chain Param File for all values being active
	 */
	@Test
	public void testDaisyChainParamEActive()
	{
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/daisy-chain-param.txt");
		
		
		
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=2 -Pb=1 -Pc=4 -Pd=2 -Pe=7", ParameterStringFormat.SURROGATE_EXECUTOR);
	
				

		
		//ParamConfigurationSpace p = new ParamConfigurationSpace(new File("./test_resources/daisy-chain-param.txt"));
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);
		
		//List<Double> result=  SurrogateExecutor.getConfigParameters(config.params, p,paramNames.toArray(new String[0]));
		
		double[] valueArray = config.toValueArray();
		
		assertDEquals(valueArray[0], 2);
		assertDEquals(valueArray[1], 1);
		assertDEquals(valueArray[2], 4);
		assertDEquals(valueArray[3], 2);
		assertDEquals(valueArray[4], 7);

	}

	
	/**
	 * Tests Daisy Chain Param File for E being invactive because D is not the correct value
	 */
	@Test
	public void testDaisyChanParamEInActiveBecauseOfD()
	{
		
		
	

		
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/daisy-chain-param.txt");
		
		
		
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=2 -Pb=1 -Pc=4 -Pd=1 ", ParameterStringFormat.SURROGATE_EXECUTOR);
	
				

		
		//ParamConfigurationSpace p = new ParamConfigurationSpace(new File("./test_resources/daisy-chain-param.txt"));
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);

		
		double[] valueArray = config.toValueArray();
		
		assertDEquals(valueArray[0], 2);
		assertDEquals(valueArray[1], 1);
		assertDEquals(valueArray[2], 4);
		assertDEquals(valueArray[3], 1);
		
	}
	
	/**
	 * Tests Daisy Chain Param file for only A being active
	 */
	@Test
	public void testDaisyChanParamAActiveOnly()
	{
		
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/daisy-chain-param.txt");
		
		
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=1 ", ParameterStringFormat.SURROGATE_EXECUTOR);
	
		//ParamConfigurationSpace p = new ParamConfigurationSpace(new File("./test_resources/daisy-chain-param.txt"));
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);

		
		double[] valueArray = config.toValueArray();
		
		assertDEquals(valueArray[0], 1);
		
	}
	
	/**
	 * Tests Diamond Param File for E active
	 */
	@Test
	public void testDiamondParamEActive()
	{
		
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/diamond-param.txt");
		
		
		
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=1 -Pb=3 -Pc=4 -Pd=6 -Pe=2 ", ParameterStringFormat.SURROGATE_EXECUTOR);
	
				

		
		//ParamConfigurationSpace p = new ParamConfigurationSpace(new File("./test_resources/daisy-chain-param.txt"));
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);
		
		//List<Double> result=  SurrogateExecutor.getConfigParameters(config.params, p,paramNames.toArray(new String[0]));
		
		double[] valueArray = config.toValueArray();
		
		assertDEquals(valueArray[0], 1);
		assertDEquals(valueArray[1], 3);
		assertDEquals(valueArray[2], 4);
		assertDEquals(valueArray[3], 6);
		assertDEquals(valueArray[4], 2);
	
	
		
	}
	
	/**
	 * Test Diamond Param for C being active, but D and E being inactive
	 */
	@Test
	public void testDiamondParamCInActive()
	{
		
		
	
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/diamond-param.txt");
		
		
		
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=1 -Pb=1 -Pd=6  ", ParameterStringFormat.SURROGATE_EXECUTOR);
	
				

		
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);
		

		
		double[] valueArray = config.toValueArray();
		
		assertDEquals(valueArray[0], 1);
		assertDEquals(valueArray[1], 1);
		assertDEquals(valueArray[3], 6);
	
	}
	
	/**
	 * Test Diamond Param for D being active, but C and E being inactive
	 */
	@Test
	public void testDiamondParamDInActive()
	{

		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/diamond-param.txt");
		
		
		
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=1 -Pb=2 -Pc=2  ", ParameterStringFormat.SURROGATE_EXECUTOR);
	
				

		
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);
		

		
		double[] valueArray = config.toValueArray();
		
		assertDEquals(valueArray[0], 1);
		assertDEquals(valueArray[1], 2);
		assertDEquals(valueArray[2], 2);
		
	}
	
	
	/**
	 * Tests Diamond Param for D and C being inactive
	 */
	@Test
	public void testDiamondParamDCInActive()
	{
		
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/diamond-param.txt");
		
		
		
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=1 -Pb=5 ", ParameterStringFormat.SURROGATE_EXECUTOR);
	
				

		
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);
		

		
		double[] valueArray = config.toValueArray();
		
		assertDEquals(valueArray[0], 1);
		assertDEquals(valueArray[1], 5);
		

	}
	
	/**
	 * Tests Diamond Param for D and C being active, but D making E inactive.
	 */
	@Test
	public void testDiamondParamEInActiveByDValue()
	{
		
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/diamond-param.txt");
		
		
		
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=1 -Pb=3 -Pd=1 -Pc=2", ParameterStringFormat.SURROGATE_EXECUTOR);
	
				

		
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);
		

		
		double[] valueArray = config.toValueArray();
		
		assertDEquals(valueArray[0], 1);
		assertDEquals(valueArray[1], 3);
		assertDEquals(valueArray[3], 1);
		assertDEquals(valueArray[2], 2);
	
		
	}
	
	/**
	 * Tests the multi dependency file for e being active
	 */
	@Test
	public void testMultiParamEActive()
	{
		

		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/multi-dependency-param.txt");
		
		
		
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=2 -Pb=2 -Pd=2 -Pc=2 -Pe=1", ParameterStringFormat.SURROGATE_EXECUTOR);
	
				

		
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);
		

		
		double[] valueArray = config.toValueArray();
		
		assertDEquals(valueArray[0], 2);
		assertDEquals(valueArray[1], 2);
		assertDEquals(valueArray[3], 2);
		assertDEquals(valueArray[2], 2);
		assertDEquals(valueArray[4], 1);
		
	
	}
	
	/**
	 * Tests the multi dependency file for E being inactive because of A
	 */
	@Test
	public void testMultiParamEInActive()
	{

		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/multi-dependency-param.txt");
		
		
		
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=3 -Pb=2 -Pd=2 -Pc=2", ParameterStringFormat.SURROGATE_EXECUTOR);
	
				

		
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);
		

		
		double[] valueArray = config.toValueArray();
		
		System.out.println(Arrays.toString(valueArray));
		System.out.println(config.getParameterConfigurationSpace().getParameterNamesInAuthorativeOrder());
		assertDEquals(valueArray[0], 3);
		assertDEquals(valueArray[1], 2);
		
		assertDEquals(valueArray[2], 2);
		
		assertDEquals(valueArray[3], 2);

		/*
		SurrogateExecutorParams config = getConfig("-Pa=3 -Pb=2 -Pd=2 -Pc=2 -f " + STANDARD_MATRIX_FILE + " -d --inst /ubc/cs/project/arrow/projects/Sat_Data/bench/SW-verification/HSAT/hsat_vc3492.cnf --seed 1234 ");
		
		
		ParamConfigurationSpace p = new ParamConfigurationSpace(new File("./test_resources/multi-dependency-param.txt"));
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);
		
		List<Double> result=  SurrogateExecutor.getConfigParameters(config.params, p,paramNames.toArray(new String[0]));
		
		assertDEquals(result.get(0), 3);
		assertDEquals(result.get(1), 2);
		assertDEquals(result.get(2), 2);
		assertDEquals(result.get(3), 2);
		assertDEquals(result.get(4), Double.NaN);*/
	}
	
	
	/**
	 * Tests that the copy constructor obeys the contracts for hashCode() and equals()
	 * 
	 * We test this by generating 100 random elements
	 * Throwing them in a hash set, then 1000 times select a random element
	 * seeing if it's in the set.
	 * Flipping a random value to something else, then checking again
	 * 
	 */
	@Test
	public void testCopyConstructorEquality()
	{
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(paramFile);
		
		
		
		Set<ParameterConfiguration> configs = new HashSet<ParameterConfiguration>();
		
		
		
		configs.add(configSpace.getDefaultConfiguration());
		
		
		while(configs.size() < 100)
		{
			configs.add(configSpace.getRandomParameterConfiguration(rand));
		}
		
		List<ParameterConfiguration> configList = new ArrayList<ParameterConfiguration>(100);
		configList.addAll(configs);
		Random rand = new SeedableRandomPool(System.currentTimeMillis()).getRandom(DebugUtil.getCurrentMethodName()); 
		
		
		
		for(int i=0; i < 1000; i++)
		{
			int nextConfig = rand.nextInt(100);
			System.out.println("Getting config: " + nextConfig);
			ParameterConfiguration configToTest = configList.get(nextConfig);
			System.out.println(configToTest);
			
			int loggingID = configToTest.getFriendlyID();
			
			if(!configs.contains(configToTest))
			{
				fail("Config To Test should have been in the set");
			}
			
			configToTest = new ParameterConfiguration(configToTest);
			
			if(!configs.contains(configToTest))
			{
				fail("Copy of config to test should have been in the set");
			}
			
			if(loggingID != configToTest.getFriendlyID())
			{
				fail("Logging ID should not change under copying");
			}

			String originalValue = configToTest.get("solved");
			
			configToTest.put("solved", "CRASHED");
			
			if(loggingID == configToTest.getFriendlyID())
			{
				fail("Logging ID should have changed when we modified the map");
			}
			
			configToTest.put("solved", originalValue);
			
			if(!configs.contains(configToTest))
			{
				fail("When we set the value back we should be able to find the original");
			}
			
		}
		
		
		
	}

	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerLowerBound()
	{
		
		String file = "param [0.1, 10] [1]i";
		StringReader sr = new StringReader(file);
		 new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerUpperBound()
	{
		
		String file = "param [1, 10.5] [1]i";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerDefault()
	{
		
		String file = "param [1, 10] [1.5]i";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerLowerBoundLog()
	{
		
		String file = "param [1.1, 10] [2]il";
		StringReader sr = new StringReader(file);
		 new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerUpperBoundLog()
	{
		
		String file = "param [1, 10.5] [1]il";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerDefaultLog()
	{
		
		String file = "param [1, 10] [1.5]il";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnLogCategorial()
	{
		
		String file = "param {1,2,3} [1] il";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnIntegralCategorial()
	{
		
		String file = "param {1,2,3} [1] il";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalIntegralFlagsSetInIncorrectOrder()
	{
		
		String file = "-numPCA [1,10]i [1]";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalLogFlagSetInIncorrectOrder()
	{
		
		String file = "-numPCA [1,10]l [1]";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalBothFlagSetInIncorrectOrder()
	{
		
		String file = "-numPCA [1,10]li [1]";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalDependentValue()
	{
		StringReader sr = new StringReader(
				"foo { a, b, c, d } [a]\n" +
				"bar { 1,2,3,4} [1]\n" +
				"bar | foolar in { a,b }");
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalIndependentValue()
	{
		StringReader sr = new StringReader(
				"foo { a, b, c, d } [a]\n" +
				"bar { 1,2,3,4} [1]\n" +
				"barar | foo in { a,b }");
		new ParameterConfigurationSpace(sr);
	}
	
	
	@Test
	public void testIntegralValue()
	{
		File paramFile = TestHelper.getTestFile("paramFiles/smac-param.txt");
		
		
		String file = "-numPCA [1,20] [7]i\n-numberSearch [1,10000000] [2000]i\n";
		StringReader sr = new StringReader(file);
		String exec = new ParameterConfigurationSpace(sr).getDefaultConfiguration().getFormattedParameterString(ParameterStringFormat.NODB_SYNTAX);
		System.out.println(exec);
		assertEquals( "Expected no decimal places (decimal point occured):",  exec.indexOf("."),-1);
		
		//System.out.println(new ParamConfigurationSpace(paramFile).getDefaultConfiguration().getFormattedParamString(StringFormat.NODB_SYNTAX));
		//System.out.println(new ParamConfigurationSpace(sr).getDefaultConfiguration().getFormattedParamString(StringFormat.NODB_SYNTAX));
		System.out.println("Test Hello");
		
		
		
	}
	
	@Test
	public void testDefaultFromSpecialString()
	{
		StringReader sr = new StringReader("-foo [1,100] [82.22]l");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr);
		ParameterConfiguration defaultConfiguration = configSpace.getDefaultConfiguration();
		
		
		for(ParameterStringFormat f : ParameterStringFormat.values())
		{
			assertEquals(defaultConfiguration, configSpace.getParameterConfigurationFromString("DEFAULT", f));
			assertEquals(defaultConfiguration, configSpace.getParameterConfigurationFromString("<DEFAULT>", f));
		}
	}
	
	@Test
	public void testRandomFromSpecialString()
	{
		StringReader sr = new StringReader("-foo [1,100] [82.22]l");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr);
		ParameterConfiguration defaultConfiguration = configSpace.getDefaultConfiguration();
		
		
		for(ParameterStringFormat f : ParameterStringFormat.values())
		{
			assertFalse("Expected that two configurations that I generated would be different", configSpace.getParameterConfigurationFromString("<RANDOM>", f,rand).equals(configSpace.getParameterConfigurationFromString("<RANDOM>", f,rand)));
			assertFalse("Expected that two configurations that I generated would be different", configSpace.getParameterConfigurationFromString("RANDOM", f, rand).equals(configSpace.getParameterConfigurationFromString("RANDOM", f, rand)));
		}
	}

	
	@Test
	public void testStringEncodingPreserving()
	{
		//Complex Parameter Space
		StringReader sr = new StringReader("test {a,b,c,d,e,f} [a]\n"
				+ "bar [0,1000] [1]\n"
				+ "bar2 [0,1000] [1]i\n"
				+ "bar3 [1,1000] [1]li\n"
				+ "bar4 [1,1000] [1]l\n" 
				+ "test2 { a,b,c,d,e,f} [b]\n"
				+ "test3 { a,b,c,d,e,f, Az,Bz, Cz, dZ,eZ} [c]\n"
				+ "test2 | test in { a,b,c} \n"
				+ "{test3 = f, test2 = f}\n");
		
		
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr);
		
		int failures = 0;
		int attempts = 0;
		for(int i=0; i < 1000; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(rand);
			for(ParameterStringFormat f : ParameterStringFormat.values())
			{
				
				switch(f)
				{
					case STATEFILE_SYNTAX_NO_INACTIVE:
					case ARRAY_STRING_MASK_INACTIVE_SYNTAX:
					case FIXED_WIDTH_ARRAY_STRING_SYNTAX:
					case FIXED_WIDTH_ARRAY_STRING_MASK_INACTIVE_SYNTAX:
						
						
						continue;
					default:
						
				}
				
				attempts++;
				String stringVersion = config.getFormattedParameterString(f);
				ParameterConfiguration config2 = configSpace.getParameterConfigurationFromString(stringVersion, f);				
				
				
				if(!config2.equals(config))
				{
					failures++;
					
					
				}
				try {
					assertEquals("Expected two configurations after transform via " + f+ " to be equal but not "+ config.toString() + " vs. " + config2.toString(), config, config2);
					assertEquals("Expected hash codes to be the same ", config.hashCode(), config2.hashCode());
					
				} catch(RuntimeException e)
				{
					System.out.println(config.getFormattedParameterString());
					System.out.println(config2.getFormattedParameterString());
				}
					
				
			}
			
		}
		
		
		System.out.println(failures + " out of " + (attempts));
		System.out.println( failures / (double) (attempts));
		
		
		
		
	}
	@Test
	public void testNoDefaultFromConfiguration()
	{ //Tries to prevent a newly implemented StringFormat from creating a representation of DEFAULT
	  
		StringReader sr = new StringReader("DEFAULT {DEFAULT} [DEFAULT] ");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr);
		ParameterConfiguration defaultConfiguration = configSpace.getDefaultConfiguration();
		for(ParameterStringFormat f : ParameterStringFormat.values())
		{
			assertFalse("Was able to get a DEFAULT as a string representation for StringFormat " + f.toString(), defaultConfiguration.getFormattedParameterString(f).trim().toUpperCase().equals("DEFAULT"));
		}
		
		StringReader sr2 = new StringReader("<DEFAULT> {<DEFAULT>} [<DEFAULT>]");
		ParameterConfigurationSpace configSpace2 = new ParameterConfigurationSpace(sr2);
		ParameterConfiguration defaultConfiguration2 = configSpace2.getDefaultConfiguration();
		for(ParameterStringFormat f : ParameterStringFormat.values())
		{
			assertFalse("Was able to get a <DEFAULT> as a string representation for StringFormat " + f.toString(), defaultConfiguration2.getFormattedParameterString(f).trim().toUpperCase().equals("<DEFAULT>"));
		}
		
	}
	
	@Test
	public void testNoRandomFromConfiguration()
	{ //Tries to prevent a newly implemented StringFormat from creating a representation of a RANDOM configuration
	  
		StringReader sr = new StringReader("RANDOM {RANDOM} [RANDOM] ");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr);
		ParameterConfiguration defaultConfiguration = configSpace.getDefaultConfiguration();
		for(ParameterStringFormat f : ParameterStringFormat.values())
		{
			assertFalse("Was able to get a RANDOM as a string representation for StringFormat " + f.toString(), defaultConfiguration.getFormattedParameterString(f).trim().toUpperCase().equals("RANDOM"));
		}
		
		StringReader sr2 = new StringReader("<RANDOM> {<RANDOM>} [<RANDOM>]");
		ParameterConfigurationSpace configSpace2 = new ParameterConfigurationSpace(sr2);
		ParameterConfiguration defaultConfiguration2 = configSpace2.getDefaultConfiguration();
		for(ParameterStringFormat f : ParameterStringFormat.values())
		{
			assertFalse("Was able to get a <RANDOM> as a string representation for StringFormat " + f.toString(), defaultConfiguration2.getFormattedParameterString(f).trim().toUpperCase().equals("<RANDOM>"));
		}
		
	}
	
	
	@Test
	public void testNeighbourFunction()
	{
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a { 0,1,2,3,4,5,6,7,8,9 } [0] \n b { 0,1,2,3,4,5,6,7,8,9 } [0] \n c { 0,1,2,3,4,5,6,7,8,9 } [0] \n d { 0, 1} [0] \n d | c in { 0 } ");
		
		
		ParameterConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		
		
		assertFalse(defaultConfig.isNeighbour(defaultConfig));
		
		for(ParameterConfiguration neighbour : defaultConfig.getNeighbourhood(rand, 4))
		{
			System.out.println(neighbour.getFormattedParameterString(ParameterStringFormat.NODB_SYNTAX));
			assertTrue("Neighbour and default should be neighbours ", defaultConfig.isNeighbour(neighbour));
			assertTrue("Neighbour and default should be neighbours ", neighbour.isNeighbour(defaultConfig));
		}
	
		ParameterConfiguration newValue = new ParameterConfiguration(defaultConfig);
		
		newValue.put("c", "1");
		
		assertTrue("Neighbour and new value be neighbours ", defaultConfig.isNeighbour(newValue));
		assertTrue("Neighbour and new value be neighbours ", newValue.isNeighbour(defaultConfig));
		
		
		newValue.put("d", "1");
		
		assertTrue("Neighbour and new value be neighbours ", defaultConfig.isNeighbour(newValue));
		assertTrue("Neighbour and new value be neighbours ", newValue.isNeighbour(defaultConfig));
		
		newValue.put("b", "1");
		
		assertFalse("Neighbour and new value should not be neighbours ", defaultConfig.isNeighbour(newValue));
		assertFalse("Neighbour and new value should not be neighbours ", newValue.isNeighbour(defaultConfig));
		
		
		
		
		
		
		
	}
	
	@Test
	public void testSubspaceDeclaration()
	{
		StringReader sr = new StringReader("foo { a, b, c, d } [a]");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, Collections.singletonMap("foo", "a") );
		assertEquals("No neighbours", 0, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "a", configSpace.getDefaultConfiguration().get("foo"));
		/**
		 * Only one parameter possible so we should always get the same thing
		 */
		assertTrue(configSpace.getRandomParameterConfiguration(rand).equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getRandomParameterConfiguration(rand).equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getRandomParameterConfiguration(rand).equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getDefaultConfiguration().equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getDefaultConfiguration().equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getDefaultConfiguration().equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());

	}
	
	@Test
	public void testSubspaceAndParentSpaceEquality()
	{
		//A Subspace and a parent space should NOT be equal
		
		String sf = "foo { a, b, c } [a] \n bar { d, e,f} [f]";
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(new StringReader(sf),"<>");
		
		System.out.println(configSpace.getDefaultConfiguration().getFormattedParameterString());
		
		ParameterConfigurationSpace configSpace2 = new ParameterConfigurationSpace(new StringReader(sf),"<>");
		
		System.out.println(configSpace2.getDefaultConfiguration().getFormattedParameterString());
		
		assertTrue("ParamConfigurationSpaces should be equal for the remainder of the test to be valid", configSpace.equals(configSpace2));
		
		
		
		ParameterConfigurationSpace configSubSpace = new ParameterConfigurationSpace(new StringReader(sf), "<>",Collections.singletonMap("foo", "b"));
		
		assertFalse("Config Space should not be equal to the subspace", configSpace.equals(configSubSpace));
		assertFalse("Subspace should not be equal to it's parent", configSubSpace.equals(configSpace));
		
		
		

		//fail("Test Not Implemented");
	}

	@Test
	public void testSubspaceDeclarationDefault()
	{
		StringReader sr = new StringReader("foo { a, b, c, d } [a]");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, Collections.singletonMap("foo", "<DEFAULT>") );
		assertEquals("No neighbours", 0, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "a", configSpace.getDefaultConfiguration().get("foo"));
		
		assertTrue("Configuration should be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		/**
		 * Only one parameter possible so we should always get the same thing
		 */
		assertTrue(configSpace.getRandomParameterConfiguration(rand).equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getRandomParameterConfiguration(rand).equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getRandomParameterConfiguration(rand).equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getDefaultConfiguration().equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getDefaultConfiguration().equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getDefaultConfiguration().equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());

		
	}

	
	@Test
	public void testSubspaceDeclarationContinuous()
	{
		StringReader sr = new StringReader("foo [0,1] [0.1]\n" +
				"bar { a, b, c } [a]");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, Collections.singletonMap("foo", "0.1") );
		assertEquals("# neighbours", 2, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "0.1", configSpace.getDefaultConfiguration().get("foo"));
		assertTrue("Configuration should be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		/**
		 * Only one parameter possible so we should always get the same thing
		 */
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		
	}
	
	@Test
	public void testRandomInSubspace()
	{
		StringReader sr = new StringReader("foo [0,1] [0.1]\n" +
				"bar [0,1] [0.1]\n" +
				"tar { a,b,c,d,e } [e]\n "+
				"gzi { a,b,c,d,e} [a]\n");
		
		
		Map<String,String> subspace = new HashMap<String, String>();
		
		subspace.put("foo", "0.1");
		subspace.put("gzi", "a");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, subspace );
		assertEquals("# neighbours", 8, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "0.1", configSpace.getDefaultConfiguration().get("foo"));
		assertTrue("Configuration should be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		
		for(int i=0; i < 100; i++)
		{
			assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		}
				
	}
	
	@Test
	public void testRandomInSubspaceWithConditionals()
	{
		StringReader sr = new StringReader("foo [0,1] [0.1]\n" +
				"bar [0,1] [0.1]\n" +
				"tar { a,b,c,d,e } [e]\n "+
				"gzi { a,b,c,d,e} [a]\n" +
				"bzi { a,b,c,d,e} [c]\n" + 
				"bzi | gzi in {a,b}");
		
		
		Map<String,String> subspace = new HashMap<String, String>();
		

		subspace.put("bzi", "a");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, subspace );
		assertEquals("# neighbours", 16, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "0.1", configSpace.getDefaultConfiguration().get("foo"));
		assertFalse("Configuration shouldn't be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		
		for(int i=0; i < 100; i++)
		{
			assertTrue(configSpace.getRandomParameterConfiguration(rand).isInSearchSubspace());
		}
				
	}
	
	
	@Test
	public void testSubspaceDeclarationContinuousNonDefault()
	{
		StringReader sr = new StringReader("foo [0,1] [0.2]\n" +
				"bar [0,1] [0.1]");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, Collections.singletonMap("foo", "0.1") );
		assertEquals("# neighbours", 4, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "0.2", configSpace.getDefaultConfiguration().get("foo"));
		assertFalse("Configuration should be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		
	}
	

	
	
	
	@Test
	public void testSubspaceDeclarationNotDefault()
	{
		StringReader sr = new StringReader("foo { a, b, c, d } [a]");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, Collections.singletonMap("foo", "d") );
		
		assertEquals("No neighbours", 0, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "a", configSpace.getDefaultConfiguration().get("foo"));
		assertFalse("Configuration shouldn't be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		
		/**
		 * Only one parameter possible so we should always get the same thing
		 */
		assertTrue(configSpace.getRandomParameterConfiguration(rand).equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getRandomParameterConfiguration(rand).equals(configSpace.getRandomParameterConfiguration(rand)));
		assertTrue(configSpace.getRandomParameterConfiguration(rand).equals(configSpace.getRandomParameterConfiguration(rand)));
		
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSubspaceDeclarationSubspaceIsForbidden()
	{
		
		StringReader sr = new StringReader("foo { a, b, c, d } [d]\n {foo = a}");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, Collections.singletonMap("foo", "a") );
		
		assertEquals("No neighbours", 0, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		configSpace.getRandomParameterConfiguration(rand);
		
	}
	
	@Test
	/**
	 * Tests to see that forbidden parameters are treated correctly when we subspace stuff
	 */
	public void testSubspaceDeclarationSubspaceAndForbidden()
	{
		StringReader sr = new StringReader(
				"foo { a, b, c, d } [a]\n" +
				"bar { 1,2,3,4} [1]\n" +
				"{foo = a, bar= 2}");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, Collections.singletonMap("foo", "a"));
		assertTrue("Configuration should be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		assertEquals("Should have 2 neighbours", 2, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
	}
	
	
	@Test
	/**
	 * Tests to see that forbidden parameters are treated correctly when we subspace stuff
	 */
	public void testSubspaceDeclarationSubspaceAndIrrelevantForbidden()
	{
		StringReader sr = new StringReader(
				"foo { a, b, c, d } [a]\n" +
				"bar { 1,2,3,4} [1]\n" +
				"{foo = b, bar= 2}");
		
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, Collections.singletonMap("foo", "a"));
		assertTrue("Configuration should be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		assertEquals("Should have 3 neighbours", 3, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
	}
	

	@Test
	/**
	 * Tests to see that forbidden parameters are treated correctly when we subspace stuff
	 */
	public void testSubspaceDeclarationSubspaceAndConditionalInactive()
	{
		StringReader sr = new StringReader(
				"foo { a, b, c, d } [d]\n" +
				"bar { 1,2,3,4} [1]\n" +
				"bar | foo in { c,b }");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, Collections.singletonMap("foo", "a"));
		assertFalse("Configuration shouldn't be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		
		assertEquals("Should have 0 neighbours", 0, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
	}
	
	@Test
	/**
	 * Tests to see that forbidden parameters are treated correctly when we subspace stuff
	 */
	public void testSubspaceDeclarationSubspaceAndConditional()
	{
		StringReader sr = new StringReader(
				"foo { a, b, c, d } [a]\n" +
				"bar { 1,2,3,4} [1]\n" +
				"bar | foo in { a,b }");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, Collections.singletonMap("foo", "a"));
		assertEquals("Should have 3 neighbours", 3, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSubspaceValidation()
	{
		StringReader sr = new StringReader(
				"foo { a, b, c, d } [a]\n" +
				"bar { 1,2,3,4} [1]\n" +
				"bar | foo in { a,b }");
		new ParameterConfigurationSpace(sr, Collections.singletonMap("foo", "ILLEGAL"));
	}
	
	@Test
	public void testSingletonSpaceEquality()
	{
		assertEquals("Expected that the two objects are the same", ParameterConfigurationSpace.getSingletonConfigurationSpace(),ParameterConfigurationSpace.getSingletonConfigurationSpace());
		assertEquals("Expected that the two objects hash the same", ParameterConfigurationSpace.getSingletonConfigurationSpace().hashCode(),ParameterConfigurationSpace.getSingletonConfigurationSpace().hashCode());
		
		assertEquals("Expected that the two objects are the same", ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(),ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration());
		assertEquals("Expected that the two objects hash the same", ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration().hashCode(),ParameterConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration().hashCode());
		
	}
	
	@Test
	public void testNeighbourCorrect()
	{
		for(int i=0; i < 25; i++)
		{
			StringReader sr = new StringReader("foo [0,10] [5]");
			ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, "<>", Collections.EMPTY_MAP);
			
			assertEquals(configSpace.getDefaultConfiguration().getNeighbourhood(rand,i).size(), i);
			
		}
		
		try {
			StringReader sr = new StringReader("foo [0,10] [5]");
			ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, "<>", Collections.EMPTY_MAP);
			
			assertEquals(configSpace.getDefaultConfiguration().getNeighbourhood(rand,-1).size(), -1);
			fail("Should have crashed with zero neighbours");
		} catch(IllegalArgumentException e)
		{
			
		}
		
		for(int i=0; i < 25; i++)
		{
			 
			StringReader sr = new StringReader("foo [0,10] [5]\nbar [0,10] [5]");
			ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, "<>", Collections.EMPTY_MAP);
			
			assertEquals(configSpace.getDefaultConfiguration().getNeighbourhood(rand,i).size(), 2*i);
			
		}
		
		
		
		
	}
	
	@Test
	public void testParameterSpaceUpperBounds()
	{
		
		System.out.println("Expect 1 : "+ParameterConfigurationSpace.getSingletonConfigurationSpace().getUpperBoundOnSize());
		assertTrue("Singleton space should have >= 1 configuration ", ParameterConfigurationSpace.getSingletonConfigurationSpace().getUpperBoundOnSize() >= 1);
		
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(new StringReader("foo { a,b,c} [a]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 3 : "+ configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 3", configSpace.getUpperBoundOnSize() >= 3);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo { a,b,c} [a]\n bar { d,e,f} [f]"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 9 : " +configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 9", configSpace.getUpperBoundOnSize() >= 9);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo { a,b,c} [a]\n bar { d,e,f} [f]\n bar | foo in {a}"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 9 : " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 9", configSpace.getUpperBoundOnSize() >= 9);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo { a,b,c} [a]\n bar { d,e,f} [f]\n"),"<>",Collections.singletonMap("foo", "a"));
		System.out.println("Expect 9 : " + configSpace.getUpperBoundOnSize());
		
		assertTrue("Size should be >= 9", configSpace.getUpperBoundOnSize() >= 9);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo { a,b,c} [a]\n bar { d,e,f} [f]\n"),"<>",Collections.singletonMap("foo", "b"));
		System.out.println("Expect 9 : " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 9", configSpace.getUpperBoundOnSize() >= 9);
		
		List<ParameterConfiguration> neighbours = configSpace.getDefaultConfiguration().getNeighbourhood(rand,4);
		neighbours.add(configSpace.getDefaultConfiguration());
		
		HashSet<ParameterConfiguration> newSet = new HashSet<ParameterConfiguration>(neighbours);
		
		
		for(ParameterConfiguration config : newSet)
		{
			System.out.println(config.getFormattedParameterString());
		}
		
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo { a,b,c} [a]\n bar { d,e,f} [f]\n {foo = a, bar = d}"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 9 :" + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 9", configSpace.getUpperBoundOnSize() >= 9);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo [0,1][1]\n bar { d,e,f} [f]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect Infinity: " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= Infinity", configSpace.getUpperBoundOnSize() >= Double.POSITIVE_INFINITY);
		
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo [0,1][1]i\n bar { d,e,f} [f]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 6: " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 6", configSpace.getUpperBoundOnSize() >= 6);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo [0,9][1]i\n bar { d,e,f} [f]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 30: " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 30", configSpace.getUpperBoundOnSize() >= 30);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo [0,9][1]i\n bar [0,9] [1]i\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 100: " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 100", configSpace.getUpperBoundOnSize() >= 100);
		
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo [0,9][1]i\n bar [0,9] [1]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect Infinity: " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= Infinity", configSpace.getUpperBoundOnSize() >= Double.POSITIVE_INFINITY);
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
	
	
	
	
	
	
	@Test
	public void testParameterSpaceLowerBounds()
	{
		
		System.out.println("Expect 1 : "+ParameterConfigurationSpace.getSingletonConfigurationSpace().getLowerBoundOnSize());
		assertTrue("Singleton space should have >= 1 configuration ", ParameterConfigurationSpace.getSingletonConfigurationSpace().getLowerBoundOnSize() >= 1);
		
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(new StringReader("foo { a,b,c} [a]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 3 : "+ configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 3", configSpace.getLowerBoundOnSize() <= 3);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo { a,b,c} [a]\n bar { d,e,f} [f]"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 9 : " +configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 9", configSpace.getLowerBoundOnSize() <= 9);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo { a,b,c} [a]\n bar { d,e,f} [f]\n bar | foo in {a}"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 3 : " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 5", configSpace.getLowerBoundOnSize() <= 5);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo { a,b,c} [a]\n bar { d,e,f} [f]\n"),"<>",Collections.singletonMap("foo", "a"));
		System.out.println("Expect 9 : " + configSpace.getLowerBoundOnSize());
		
		assertTrue("Size should be <= 9", configSpace.getLowerBoundOnSize() <= 9);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo { a,b,c} [a]\n bar { d,e,f} [f]\n"),"<>",Collections.singletonMap("foo", "b"));
		System.out.println("Expect 9 : " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 9", configSpace.getLowerBoundOnSize() <= 9);
		
		List<ParameterConfiguration> neighbours = configSpace.getDefaultConfiguration().getNeighbourhood(rand,4);
		neighbours.add(configSpace.getDefaultConfiguration());
		
		HashSet<ParameterConfiguration> newSet = new HashSet<ParameterConfiguration>(neighbours);
		
		
		for(ParameterConfiguration config : newSet)
		{
			System.out.println(config.getFormattedParameterString());
		}
		
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo { a,b,c} [a]\n bar { d,e,f} [f]\n {foo = a, bar = d}"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 1 :" + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 8", configSpace.getLowerBoundOnSize() <= 8);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo [0,1][1]\n bar { d,e,f} [f]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect Infinity: " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= Infinity", configSpace.getLowerBoundOnSize() <= Double.POSITIVE_INFINITY);
		
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo [0,1][1]i\n bar { d,e,f} [f]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 6: " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 6", configSpace.getLowerBoundOnSize() <= 6);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo [0,9][1]i\n bar { d,e,f} [f]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 30: " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 30", configSpace.getLowerBoundOnSize() <= 30);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo [0,9][1]i\n bar [0,9] [1]i\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 100: " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 100", configSpace.getLowerBoundOnSize() <= 100);
		
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo [0,9][1]i\n bar [0,9] [1]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect Infinity: " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= Infinity", configSpace.getLowerBoundOnSize() <= Double.POSITIVE_INFINITY);
		
		
	}
	
	
	
	
	
	
	
	@Test
	/**
	 * Related to bug 1718
	 */
	public void testEmptyParamValue()
	{
		StringReader sr = new StringReader("foo {\"\",\"test\"} [\"\"]\n");
		
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, "<>", Collections.EMPTY_MAP);
		ParameterConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		
		ParameterConfiguration otherConfig = configSpace.getDefaultConfiguration().getNeighbourhood(new MersenneTwister(), 1).get(0);
		
		System.out.println(defaultConfig.getFormattedParameterString());
		System.out.println(otherConfig.getFormattedParameterString());
		
		
		defaultConfig.put("foo", otherConfig.get("foo"));
		defaultConfig = configSpace.getDefaultConfiguration();
		otherConfig.put("foo", defaultConfig.get("foo"));
		System.out.println(otherConfig.getFormattedParameterString());	
	}
	
	@Test
	/**
	 * Related to bug 1720
	 */
	public void testDefaultConfigurationToAndFromString()
	{

		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("foo {a,b,c} [a]\nbar{e,d,f} [f]\nbar | foo in { c }");		
		ParameterConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		ParameterConfiguration duplicateConfig = configSpace.getParameterConfigurationFromString(defaultConfig.getFormattedParameterString(ParameterStringFormat.NODB_SYNTAX), ParameterStringFormat.NODB_SYNTAX);		
		
		assertEquals("Expected that the NANed version of the strings should be equal", defaultConfig.getFormattedParameterString(ParameterStringFormat.ARRAY_STRING_MASK_INACTIVE_SYNTAX), duplicateConfig.getFormattedParameterString(ParameterStringFormat.ARRAY_STRING_MASK_INACTIVE_SYNTAX)); 
		assertEquals("Expected that the version of the strings should be equal", defaultConfig.getFormattedParameterString(ParameterStringFormat.ARRAY_STRING_SYNTAX), duplicateConfig.getFormattedParameterString(ParameterStringFormat.ARRAY_STRING_SYNTAX));
	}

	/**
	 * Related to bug 1720 & 2009
	 */
	@Test
	public void testFromStringMissingActiveParam()
	{

		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("foo {a,b,c} [a]\nbar{e,d,f} [f]\n cat {2,3,4} [2] \nbar | foo in { c }");		
	

		ParameterConfiguration duplicateConfig = configSpace.getParameterConfigurationFromString("-foo 'a' -bar 'e' -cat 2", ParameterStringFormat.NODB_SYNTAX);
		duplicateConfig = configSpace.getParameterConfigurationFromString("-foo 'a' -bar 'e' -cat 3", ParameterStringFormat.NODB_SYNTAX);
		duplicateConfig = configSpace.getParameterConfigurationFromString("-foo 'a' -bar 'e' -cat 4", ParameterStringFormat.NODB_SYNTAX);
		
		//==== Parameter value for cat is missing, should tank
		try {
			 duplicateConfig = configSpace.getParameterConfigurationFromString("-foo 'a' -bar 'e'", ParameterStringFormat.NODB_SYNTAX);
			fail("Expected exception on corrupted string");
		} catch(ParameterConfigurationStringFormatException e)
		{
			//Good
		}

		duplicateConfig = configSpace.getParameterConfigurationFromString("foo='a',bar='e',cat='2'", ParameterStringFormat.STATEFILE_SYNTAX);
		duplicateConfig = configSpace.getParameterConfigurationFromString("foo='a',bar='e',cat='3'", ParameterStringFormat.STATEFILE_SYNTAX);
		duplicateConfig = configSpace.getParameterConfigurationFromString("foo='a',bar='e',cat='4'", ParameterStringFormat.STATEFILE_SYNTAX);
		
		
		try {
			duplicateConfig = configSpace.getParameterConfigurationFromString("foo='a',bar='e'", ParameterStringFormat.STATEFILE_SYNTAX);
			fail("Expected exception on corrupted string");
		} catch(ParameterConfigurationStringFormatException e)
		{
			//Good
		}
		
	}
	
	
	/**
	 * Related to bug 2009 & 1720
	 * 
	 */
	@Test
	public void testFromStringMissingActiveParamContinuous()
	{

		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("foo {a,b,c} [a]\nbar{e,d,f} [f]\n cat [2,4] [2] \nbar | foo in { c }");		
	
		ParameterConfiguration duplicateConfig = configSpace.getParameterConfigurationFromString("-foo 'a' -bar 'e' -cat 2", ParameterStringFormat.NODB_SYNTAX);
		duplicateConfig = configSpace.getParameterConfigurationFromString("-foo 'a' -bar 'e' -cat 3", ParameterStringFormat.NODB_SYNTAX);
		duplicateConfig = configSpace.getParameterConfigurationFromString("-foo 'a' -bar 'e' -cat 4", ParameterStringFormat.NODB_SYNTAX);
		
		//==== Parameter value for cat is missing, should tank
		try {
			 duplicateConfig = configSpace.getParameterConfigurationFromString("-foo 'a' -bar 'e'", ParameterStringFormat.NODB_SYNTAX);
			fail("Expected exception on corrupted string");
		} catch(ParameterConfigurationStringFormatException e)
		{
			//Good
		}

		duplicateConfig = configSpace.getParameterConfigurationFromString("foo='a',bar='e',cat='2'", ParameterStringFormat.STATEFILE_SYNTAX);
		duplicateConfig = configSpace.getParameterConfigurationFromString("foo='a',bar='e',cat='3'", ParameterStringFormat.STATEFILE_SYNTAX);
		duplicateConfig = configSpace.getParameterConfigurationFromString("foo='a',bar='e',cat='4'", ParameterStringFormat.STATEFILE_SYNTAX);
		
		
		try {
			duplicateConfig = configSpace.getParameterConfigurationFromString("foo='a',bar='e'", ParameterStringFormat.STATEFILE_SYNTAX);
			fail("Expected exception on corrupted string");
		} catch(ParameterConfigurationStringFormatException e)
		{
			//Good
		}
		
		
		
		
	}
	
	
	/**
	 * Related to bug 1720
	 */
	@Test
	public void testFromStringMissingInactive()
	{

		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("foo {a,b,c} [a]\nbar{e,d,f} [f]\n \nbar | foo in { a }");		
	
		//==== Parameter value for cat is missing, should tank
		ParameterConfiguration duplicateConfig = configSpace.getParameterConfigurationFromString("-foo 'b' ", ParameterStringFormat.NODB_SYNTAX);
		
		assertEquals("Expected Default value to be set", duplicateConfig.get("bar"), "f");
	
		duplicateConfig = configSpace.getParameterConfigurationFromString("-foo 'b' -bar 'd'", ParameterStringFormat.NODB_SYNTAX);
		
		assertEquals("Expected Default value to be set", duplicateConfig.get("bar"), "d");
	
		System.out.println(duplicateConfig.get("bar"));
		
		
	}
	
	
	
	/**
	 * Related to bug 1728
	 */
	@Test
	public void testGenerateForbidden()
	{

		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("foo {a,b,c} [a]\nbar{e,d,f} [f]\n cat { g,h,i } [h] \n bar | foo in { c } \n { foo=a,bar=d,cat=g} ");		
		ParameterConfiguration duplicateConfig = configSpace.getParameterConfigurationFromString("-foo 'a' -bar 'd' -cat 'g'", ParameterStringFormat.NODB_SYNTAX);
		
		assertTrue("Parameter should be forbidden", duplicateConfig.isForbiddenParameterConfiguration());
		
		/*assertEquals("Expected that the NANed version of the strings should be equal", defaultConfig.getFormattedParamString(StringFormat.ARRAY_STRING_MASK_INACTIVE_SYNTAX), duplicateConfig.getFormattedParamString(StringFormat.ARRAY_STRING_MASK_INACTIVE_SYNTAX)); 
		assertEquals("Expected that the version of the strings should be equal", defaultConfig.getFormattedParamString(StringFormat.ARRAY_STRING_SYNTAX), duplicateConfig.getFormattedParamString(StringFormat.ARRAY_STRING_SYNTAX));
		*/
	}
	
	@Test
	public void testNullSpace()
	{
		ParameterConfigurationSpace configSpace = ParameterConfigurationSpace.getNullConfigurationSpace();
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		
		System.out.println("Config String:" + config.getFormattedParameterString(ParameterStringFormat.NODB_SYNTAX));
		
		assertEquals(config.getNeighbourhood(new Random(0),5).size(), 0);
		
		assertEquals(config.getActiveParameters().size(), 0);
		assertEquals(configSpace.getLowerBoundOnSize(),1,0.0);
		assertEquals(configSpace.getUpperBoundOnSize(),1,0.0);
		assertEquals(configSpace.getRandomParameterConfiguration(new Random(0)), config);
		assertEquals(configSpace.getParameterNames().size(),0);
			
	}
	
	@Test
	@Ignore
	public void testSpeed()
	{
		//This really isn't a test
		//Just times it.
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a { 1,2} [1]\n b { 1,2} [1]\n c { 1,2} [1]\n d { 1,2} [1]\n e { 1,2} [1]\n f { 1,2} [1]\n g { 1,2} [1]\n h { 1,2} [1]\n i { 1,2} [1]\n");
		
		StopWatch t = new AutoStartStopWatch();
		
		HashSet<ParameterConfiguration> configs = new HashSet<ParameterConfiguration>();
		for(int i=0; i < 1000000; i++)
		{
			configs.add(configSpace.getDefaultConfiguration());
			
		}
		
		System.out.println(t.stop() / 1000.0);
	}
	
	@Test
	@Ignore
	public void testRandomSpeed()
	{
		 
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("x0 [-3,3] [3]\n"+
"x1 [-2, 2] [2]\n"+
"abs(3*X) [-4,4] [-4]\n"+
"-cos(X)+1 [0,6.28] [2]\n"+
"exp(X)-1 [0,10] [8]\n"+
"abs(2*X) [-4,4] [-4]\n"+
"abs(4*X) [-4,4] [-4]\n"+
"abs(X) [-4,4] [-4]\n");
		MersenneTwisterFast fast = new MersenneTwisterFast(rand.nextLong());
		
		AutoStartStopWatch watch = new AutoStartStopWatch();
		for(int i=0; i < 10; i++)
		{
			for(int j=0; j < 2000000; j++)
			{
				configSpace.getRandomParameterConfiguration(fast);
			}
			System.out.println(watch.laps()/1000.0 + " secs");
		}
		
		System.out.println("Average time " + watch.stop() / 10000.0 + " seconds");
		
		fail("This doesn't actually test anything");	
		
	}
	
	@Test
	public void testNullAndSingletonConfigurationRestore()
	{
		ParameterConfigurationSpace singletonConfigSpace = ParameterConfigurationSpace.getSingletonConfigurationSpace();
		
		testRestoration(singletonConfigSpace.getDefaultConfiguration());
		
		
		
		
		ParameterConfigurationSpace nullConfigSpace = ParameterConfigurationSpace.getNullConfigurationSpace();
		
		testRestoration(nullConfigSpace.getDefaultConfiguration());
		
		
		
		
	}
	
	
	public void testRestoration(ParameterConfiguration configuration)
	{
		for(ParameterStringFormat format : ParameterStringFormat.values())
		{
			switch(format)
			{
				case STATEFILE_SYNTAX_NO_INACTIVE:
				case ARRAY_STRING_MASK_INACTIVE_SYNTAX:
				case FIXED_WIDTH_ARRAY_STRING_SYNTAX:
				case FIXED_WIDTH_ARRAY_STRING_MASK_INACTIVE_SYNTAX:
					
					
					continue;
				default:
					
			}
			
			
			ParameterConfiguration restoredConfiguration = configuration.getParameterConfigurationSpace().getParameterConfigurationFromString(configuration.getFormattedParameterString(format), format);
			assertEquals("Parameter Configurations should be equal", configuration, restoredConfiguration);
		}
		
	}
	
	@Test
	public void testGeneration()
	{
		String pcsFile ="solved { SAT, UNSAT, TIMEOUT, CRASHED, ABORT, INVALID } [SAT]\n"+
						"runtime [0,1000] [0]\n"+
						"walltime [0,2000] [1]\n"+
						"runlength [0,1000000][0]\n"+
						"quality [0, 1000000] [0]\n"+
						"seed [ -1,4294967295][1]i";
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		for(int i=0; i < 1000; i++)
		{
			configSpace.getParameterConfigurationFromString(configSpace.getRandomParameterConfiguration(rand).getFormattedParameterString(), ParameterStringFormat.NODB_SYNTAX);
			
			
		}
	}
	
	@After
	public void tearDown()
	{
		//System.out.println("Done");
	}
}
