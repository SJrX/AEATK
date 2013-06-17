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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationStringFormatException;
import ca.ubc.cs.beta.aclib.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aclib.random.SeedableRandomPool;

import com.beust.jcommander.internal.Lists;

import ec.util.MersenneTwister;

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
	
	public static ParamConfigurationSpace getConfigSpaceForFile(String f)
	{
		URL url = ParamConfigurationSpace.class.getClassLoader().getResource(f);
		File file = new File(url.getPath());
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(file);
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
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(f);
		ParamConfiguration config = configSpace.getDefaultConfiguration();
		System.out.println(config.getFormattedParamString());
		//File is parsed correctly
		
	}
	
	/**
	 * See Bug #1274
	 */
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidArgumentParameter() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/invalidDefaultParam.txt");
		File f = new File(url.getPath());
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(f);
		try { 
		ParamConfiguration config = configSpace.getDefaultConfiguration();
		System.out.println(config.getFormattedParamString());
		} catch(IllegalArgumentException e)
		{
			fail("The Config Space should have thrown this exception");
			
		}
		
		
	}

	
	@Test
	public void testForbidden() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/forbiddenExampleParam.txt");
		File f = new File(url.getPath());
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(f);
		ParamConfiguration config = configSpace.getDefaultConfiguration();
		System.out.println(config.getFormattedParamString());
		
		assertFalse(config.isForbiddenParamConfiguration());
		config.put("a", "v2");
		config.put("b", "w2");
		assertTrue(config.isForbiddenParamConfiguration());
		
	}
	
	@Test
	public void testNameThenSquareBracket()
	{
		//name[ may fail
		ParamConfiguration config = getConfigSpaceForFile("paramFiles/continuousNameNoSpaceParam.txt").getDefaultConfiguration();
		
		double d = Double.valueOf(config.get("name"));
		
		if( d > 0.45 && d < 0.55)
		{
			
		} else
		{
			fail("Value should have been 0.5");
		}
		
		System.out.println("Result: " + config.getFormattedParamString());
		
	}
	
	public void testEmptyValue()
	{
		
	}
	

	/**
	 * Tests what happens when we specify a value for a parameter that is not in it's domain
	 */
	@Test(expected=ParamConfigurationStringFormatException.class)
	public void testParamNotInvalidValue()
	{
		
		
		ParamConfigurationSpace p = getConfigSpaceForFile("paramFiles/daisy-chain-param.txt");
		
		
		
		p.getConfigurationFromString("-Pa=99989", StringFormat.SURROGATE_EXECUTOR);
		
		
		fail();
		
		
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);
		
		 
	}
	
	/**
	 * Tests what happens if we specify a parameter that does not appear in the file
	 */
	@Test(expected=ParamConfigurationStringFormatException.class)
	public void testParamNotInParameterFile()
	{
		ParamConfigurationSpace p = getConfigSpaceForFile("paramFiles/daisy-chain-param.txt");
		
		
		
		p.getConfigurationFromString("-Pa=2 -Pf=3", StringFormat.SURROGATE_EXECUTOR);
		
		
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
		ParamConfigurationSpace p = getConfigSpaceForFile("paramFiles/daisy-chain-param.txt");
		
		
		
		ParamConfiguration config = p.getConfigurationFromString("-Pa=2 -Pb=1 -Pc=4 -Pd=2 -Pe=7", StringFormat.SURROGATE_EXECUTOR);
	
				

		
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
		
		
	

		
		ParamConfigurationSpace p = getConfigSpaceForFile("paramFiles/daisy-chain-param.txt");
		
		
		
		ParamConfiguration config = p.getConfigurationFromString("-Pa=2 -Pb=1 -Pc=4 -Pd=1 ", StringFormat.SURROGATE_EXECUTOR);
	
				

		
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
		
		ParamConfigurationSpace p = getConfigSpaceForFile("paramFiles/daisy-chain-param.txt");
		
		
		ParamConfiguration config = p.getConfigurationFromString("-Pa=1 ", StringFormat.SURROGATE_EXECUTOR);
	
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
		
		ParamConfigurationSpace p = getConfigSpaceForFile("paramFiles/diamond-param.txt");
		
		
		
		ParamConfiguration config = p.getConfigurationFromString("-Pa=1 -Pb=3 -Pc=4 -Pd=6 -Pe=2 ", StringFormat.SURROGATE_EXECUTOR);
	
				

		
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
		
		
	
		ParamConfigurationSpace p = getConfigSpaceForFile("paramFiles/diamond-param.txt");
		
		
		
		ParamConfiguration config = p.getConfigurationFromString("-Pa=1 -Pb=1 -Pd=6  ", StringFormat.SURROGATE_EXECUTOR);
	
				

		
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

		ParamConfigurationSpace p = getConfigSpaceForFile("paramFiles/diamond-param.txt");
		
		
		
		ParamConfiguration config = p.getConfigurationFromString("-Pa=1 -Pb=2 -Pc=2  ", StringFormat.SURROGATE_EXECUTOR);
	
				

		
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
		
		ParamConfigurationSpace p = getConfigSpaceForFile("paramFiles/diamond-param.txt");
		
		
		
		ParamConfiguration config = p.getConfigurationFromString("-Pa=1 -Pb=5 ", StringFormat.SURROGATE_EXECUTOR);
	
				

		
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
		
		ParamConfigurationSpace p = getConfigSpaceForFile("paramFiles/diamond-param.txt");
		
		
		
		ParamConfiguration config = p.getConfigurationFromString("-Pa=1 -Pb=3 -Pd=1 -Pc=2", StringFormat.SURROGATE_EXECUTOR);
	
				

		
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
		

		ParamConfigurationSpace p = getConfigSpaceForFile("paramFiles/multi-dependency-param.txt");
		
		
		
		ParamConfiguration config = p.getConfigurationFromString("-Pa=2 -Pb=2 -Pd=2 -Pc=2 -Pe=1", StringFormat.SURROGATE_EXECUTOR);
	
				

		
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

		ParamConfigurationSpace p = getConfigSpaceForFile("paramFiles/multi-dependency-param.txt");
		
		
		
		ParamConfiguration config = p.getConfigurationFromString("-Pa=3 -Pb=2 -Pd=2 -Pc=2", StringFormat.SURROGATE_EXECUTOR);
	
				

		
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		Collections.sort(paramNames);
		

		
		double[] valueArray = config.toValueArray();
		
		assertDEquals(valueArray[0], 3);
		assertDEquals(valueArray[1], 2);
		assertDEquals(valueArray[3], 2);
		assertDEquals(valueArray[2], 2);
		

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
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(paramFile);
		
		
		
		Set<ParamConfiguration> configs = new HashSet<ParamConfiguration>();
		
		
		
		configs.add(configSpace.getDefaultConfiguration());
		
		
		while(configs.size() < 100)
		{
			configs.add(configSpace.getRandomConfiguration(rand));
		}
		
		List<ParamConfiguration> configList = new ArrayList<ParamConfiguration>(100);
		configList.addAll(configs);
		Random rand = new SeedableRandomPool(System.currentTimeMillis()).getRandom(DebugUtil.getCurrentMethodName()); 
		
		
		
		for(int i=0; i < 1000; i++)
		{
			int nextConfig = rand.nextInt(100);
			System.out.println("Getting config: " + nextConfig);
			ParamConfiguration configToTest = configList.get(nextConfig);
			System.out.println(configToTest);
			
			int loggingID = configToTest.getFriendlyID();
			
			if(!configs.contains(configToTest))
			{
				fail("Config To Test should have been in the set");
			}
			
			configToTest = new ParamConfiguration(configToTest);
			
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
		 new ParamConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerUpperBound()
	{
		
		String file = "param [1, 10.5] [1]i";
		StringReader sr = new StringReader(file);
		new ParamConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerDefault()
	{
		
		String file = "param [1, 10] [1.5]i";
		StringReader sr = new StringReader(file);
		new ParamConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerLowerBoundLog()
	{
		
		String file = "param [1.1, 10] [2]il";
		StringReader sr = new StringReader(file);
		 new ParamConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerUpperBoundLog()
	{
		
		String file = "param [1, 10.5] [1]il";
		StringReader sr = new StringReader(file);
		new ParamConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerDefaultLog()
	{
		
		String file = "param [1, 10] [1.5]il";
		StringReader sr = new StringReader(file);
		new ParamConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnLogCategorial()
	{
		
		String file = "param {1,2,3} [1] il";
		StringReader sr = new StringReader(file);
		new ParamConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnIntegralCategorial()
	{
		
		String file = "param {1,2,3} [1] il";
		StringReader sr = new StringReader(file);
		new ParamConfigurationSpace(sr);
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalIntegralFlagsSetInIncorrectOrder()
	{
		
		String file = "-numPCA [1,10]i [1]";
		StringReader sr = new StringReader(file);
		new ParamConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalLogFlagSetInIncorrectOrder()
	{
		
		String file = "-numPCA [1,10]l [1]";
		StringReader sr = new StringReader(file);
		new ParamConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalBothFlagSetInIncorrectOrder()
	{
		
		String file = "-numPCA [1,10]li [1]";
		StringReader sr = new StringReader(file);
		new ParamConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalDependentValue()
	{
		StringReader sr = new StringReader(
				"foo { a, b, c, d } [a]\n" +
				"bar { 1,2,3,4} [1]\n" +
				"bar | foolar in { a,b }");
		new ParamConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalIndependentValue()
	{
		StringReader sr = new StringReader(
				"foo { a, b, c, d } [a]\n" +
				"bar { 1,2,3,4} [1]\n" +
				"barar | foo in { a,b }");
		new ParamConfigurationSpace(sr);
	}
	
	
	@Test
	public void testIntegralValue()
	{
		File paramFile = TestHelper.getTestFile("paramFiles/smac-param.txt");
		
		
		String file = "-numPCA [1,20] [7]i\n-numberSearch [1,10000000] [2000]i\n";
		StringReader sr = new StringReader(file);
		String exec = new ParamConfigurationSpace(sr).getDefaultConfiguration().getFormattedParamString(StringFormat.NODB_SYNTAX);
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
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr);
		ParamConfiguration defaultConfiguration = configSpace.getDefaultConfiguration();
		
		
		for(StringFormat f : StringFormat.values())
		{
			assertEquals(defaultConfiguration, configSpace.getConfigurationFromString("DEFAULT", f));
			assertEquals(defaultConfiguration, configSpace.getConfigurationFromString("<DEFAULT>", f));
		}
	}
	
	@Test
	public void testRandomFromSpecialString()
	{
		StringReader sr = new StringReader("-foo [1,100] [82.22]l");
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr);
		ParamConfiguration defaultConfiguration = configSpace.getDefaultConfiguration();
		
		
		for(StringFormat f : StringFormat.values())
		{
			assertFalse("Expected that two configurations that I generated would be different", configSpace.getConfigurationFromString("<RANDOM>", f,rand).equals(configSpace.getConfigurationFromString("<RANDOM>", f,rand)));
			assertFalse("Expected that two configurations that I generated would be different", configSpace.getConfigurationFromString("RANDOM", f, rand).equals(configSpace.getConfigurationFromString("RANDOM", f, rand)));
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
		
		
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr);
		
		int failures = 0;
		int attempts = 0;
		for(int i=0; i < 10000; i++)
		{
			ParamConfiguration config = configSpace.getRandomConfiguration(rand);
			for(StringFormat f : StringFormat.values())
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
				String stringVersion = config.getFormattedParamString(f);
				ParamConfiguration config2 = configSpace.getConfigurationFromString(stringVersion, f);				
				
				
				if(!config2.equals(config))
				{
					failures++;
					
					
				}
				try {
					assertEquals("Expected two configurations after transform via " + f+ " to be equal but not "+ config.toString() + " vs. " + config2.toString(), config, config2);
					assertEquals("Expected hash codes to be the same ", config.hashCode(), config2.hashCode());
					
				} catch(RuntimeException e)
				{
					System.out.println(config.getFormattedParamString());
					System.out.println(config2.getFormattedParamString());
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
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr);
		ParamConfiguration defaultConfiguration = configSpace.getDefaultConfiguration();
		for(StringFormat f : StringFormat.values())
		{
			assertFalse("Was able to get a DEFAULT as a string representation for StringFormat " + f.toString(), defaultConfiguration.getFormattedParamString(f).trim().toUpperCase().equals("DEFAULT"));
		}
		
		StringReader sr2 = new StringReader("<DEFAULT> {<DEFAULT>} [<DEFAULT>]");
		ParamConfigurationSpace configSpace2 = new ParamConfigurationSpace(sr2);
		ParamConfiguration defaultConfiguration2 = configSpace2.getDefaultConfiguration();
		for(StringFormat f : StringFormat.values())
		{
			assertFalse("Was able to get a <DEFAULT> as a string representation for StringFormat " + f.toString(), defaultConfiguration2.getFormattedParamString(f).trim().toUpperCase().equals("<DEFAULT>"));
		}
		
	}
	
	@Test
	public void testNoRandomFromConfiguration()
	{ //Tries to prevent a newly implemented StringFormat from creating a representation of a RANDOM configuration
	  
		StringReader sr = new StringReader("RANDOM {RANDOM} [RANDOM] ");
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr);
		ParamConfiguration defaultConfiguration = configSpace.getDefaultConfiguration();
		for(StringFormat f : StringFormat.values())
		{
			assertFalse("Was able to get a RANDOM as a string representation for StringFormat " + f.toString(), defaultConfiguration.getFormattedParamString(f).trim().toUpperCase().equals("RANDOM"));
		}
		
		StringReader sr2 = new StringReader("<RANDOM> {<RANDOM>} [<RANDOM>]");
		ParamConfigurationSpace configSpace2 = new ParamConfigurationSpace(sr2);
		ParamConfiguration defaultConfiguration2 = configSpace2.getDefaultConfiguration();
		for(StringFormat f : StringFormat.values())
		{
			assertFalse("Was able to get a <RANDOM> as a string representation for StringFormat " + f.toString(), defaultConfiguration2.getFormattedParamString(f).trim().toUpperCase().equals("<RANDOM>"));
		}
		
	}
	
	
	@Test
	public void testSubspaceDeclaration()
	{
		StringReader sr = new StringReader("foo { a, b, c, d } [a]");
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, Collections.singletonMap("foo", "a") );
		assertEquals("No neighbours", 0, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "a", configSpace.getDefaultConfiguration().get("foo"));
		/**
		 * Only one parameter possible so we should always get the same thing
		 */
		assertTrue(configSpace.getRandomConfiguration(rand).equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getRandomConfiguration(rand).equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getRandomConfiguration(rand).equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getDefaultConfiguration().equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getDefaultConfiguration().equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getDefaultConfiguration().equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());

	}
	
	@Test
	public void testSubspaceAndParentSpaceEquality()
	{
		//A Subspace and a parent space should NOT be equal
		fail("Test Not Implemented");
	}

	@Test
	public void testSubspaceDeclarationDefault()
	{
		StringReader sr = new StringReader("foo { a, b, c, d } [a]");
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, Collections.singletonMap("foo", "<DEFAULT>") );
		assertEquals("No neighbours", 0, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "a", configSpace.getDefaultConfiguration().get("foo"));
		
		assertTrue("Configuration should be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		/**
		 * Only one parameter possible so we should always get the same thing
		 */
		assertTrue(configSpace.getRandomConfiguration(rand).equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getRandomConfiguration(rand).equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getRandomConfiguration(rand).equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getDefaultConfiguration().equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getDefaultConfiguration().equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getDefaultConfiguration().equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());

		
	}

	
	@Test
	public void testSubspaceDeclarationContinuous()
	{
		StringReader sr = new StringReader("foo [0,1] [0.1]\n" +
				"bar { a, b, c } [a]");
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, Collections.singletonMap("foo", "0.1") );
		assertEquals("# neighbours", 2, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "0.1", configSpace.getDefaultConfiguration().get("foo"));
		assertTrue("Configuration should be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		/**
		 * Only one parameter possible so we should always get the same thing
		 */
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		
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
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, subspace );
		assertEquals("# neighbours", 8, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "0.1", configSpace.getDefaultConfiguration().get("foo"));
		assertTrue("Configuration should be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		
		for(int i=0; i < 100; i++)
		{
			assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
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
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, subspace );
		assertEquals("# neighbours", 16, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "0.1", configSpace.getDefaultConfiguration().get("foo"));
		assertFalse("Configuration shouldn't be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		
		for(int i=0; i < 100; i++)
		{
			assertTrue(configSpace.getRandomConfiguration(rand).isInSearchSubspace());
		}
				
	}
	
	
	@Test
	public void testSubspaceDeclarationContinuousNonDefault()
	{
		StringReader sr = new StringReader("foo [0,1] [0.2]\n" +
				"bar [0,1] [0.1]");
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, Collections.singletonMap("foo", "0.1") );
		assertEquals("# neighbours", 4, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "0.2", configSpace.getDefaultConfiguration().get("foo"));
		assertFalse("Configuration should be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		
	}
	

	
	
	
	@Test
	public void testSubspaceDeclarationNotDefault()
	{
		StringReader sr = new StringReader("foo { a, b, c, d } [a]");
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, Collections.singletonMap("foo", "d") );
		
		assertEquals("No neighbours", 0, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "a", configSpace.getDefaultConfiguration().get("foo"));
		assertFalse("Configuration shouldn't be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		
		/**
		 * Only one parameter possible so we should always get the same thing
		 */
		assertTrue(configSpace.getRandomConfiguration(rand).equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getRandomConfiguration(rand).equals(configSpace.getRandomConfiguration(rand)));
		assertTrue(configSpace.getRandomConfiguration(rand).equals(configSpace.getRandomConfiguration(rand)));
		
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSubspaceDeclarationSubspaceIsForbidden()
	{
		
		StringReader sr = new StringReader("foo { a, b, c, d } [d]\n {foo = a}");
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, Collections.singletonMap("foo", "a") );
		
		assertEquals("No neighbours", 0, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		configSpace.getRandomConfiguration(rand);
		
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
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, Collections.singletonMap("foo", "a"));
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
		
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, Collections.singletonMap("foo", "a"));
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
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, Collections.singletonMap("foo", "a"));
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
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, Collections.singletonMap("foo", "a"));
		assertEquals("Should have 3 neighbours", 3, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSubspaceValidation()
	{
		StringReader sr = new StringReader(
				"foo { a, b, c, d } [a]\n" +
				"bar { 1,2,3,4} [1]\n" +
				"bar | foo in { a,b }");
		new ParamConfigurationSpace(sr, Collections.singletonMap("foo", "ILLEGAL"));
	}
	
	@Test
	public void testSingletonSpaceEquality()
	{
		assertEquals("Expected that the two objects are the same", ParamConfigurationSpace.getSingletonConfigurationSpace(),ParamConfigurationSpace.getSingletonConfigurationSpace());
		assertEquals("Expected that the two objects hash the same", ParamConfigurationSpace.getSingletonConfigurationSpace().hashCode(),ParamConfigurationSpace.getSingletonConfigurationSpace().hashCode());
		
		assertEquals("Expected that the two objects are the same", ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration(),ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration());
		assertEquals("Expected that the two objects hash the same", ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration().hashCode(),ParamConfigurationSpace.getSingletonConfigurationSpace().getDefaultConfiguration().hashCode());
		
	}
	
	@Test
	public void testNeighbourCorrect()
	{
		for(int i=0; i < 25; i++)
		{
			StringReader sr = new StringReader("foo [0,10] [5]");
			ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, "<>", Collections.EMPTY_MAP);
			
			assertEquals(configSpace.getDefaultConfiguration().getNeighbourhood(rand,i).size(), i);
			
		}
		
		try {
			StringReader sr = new StringReader("foo [0,10] [5]");
			ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, "<>", Collections.EMPTY_MAP);
			
			assertEquals(configSpace.getDefaultConfiguration().getNeighbourhood(rand,-1).size(), -1);
			fail("Should have crashed with zero neighbours");
		} catch(IllegalArgumentException e)
		{
			
		}
		
		for(int i=0; i < 25; i++)
		{
			 
			StringReader sr = new StringReader("foo [0,10] [5]\nbar [0,10] [5]");
			ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, "<>", Collections.EMPTY_MAP);
			
			assertEquals(configSpace.getDefaultConfiguration().getNeighbourhood(rand,i).size(), 2*i);
			
		}
		
		
		
		
	}
	
	@Test
	/**
	 * Related to bug 1718
	 */
	public void testEmptyParamValue()
	{
		StringReader sr = new StringReader("foo {\"\",\"test\"} [\"\"]\n");
		
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(sr, "<>", Collections.EMPTY_MAP);
		ParamConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		
		ParamConfiguration otherConfig = configSpace.getDefaultConfiguration().getNeighbourhood(new MersenneTwister(), 1).get(0);
		
		System.out.println(defaultConfig.getFormattedParamString());
		System.out.println(otherConfig.getFormattedParamString());
		
		
		defaultConfig.put("foo", otherConfig.get("foo"));
		defaultConfig = configSpace.getDefaultConfiguration();
		otherConfig.put("foo", defaultConfig.get("foo"));
		System.out.println(otherConfig.getFormattedParamString());	
	}
	
	
	@After
	public void tearDown()
	{
		//System.out.println("Done");
	}
}
