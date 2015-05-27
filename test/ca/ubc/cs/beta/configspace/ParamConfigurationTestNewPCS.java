package ca.ubc.cs.beta.configspace;



import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aeatk.exceptions.ParameterConfigurationLockedException;
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
	public void testAClibFormat() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/aclib2.txt");
		File f = new File(url.getPath());
		System.out.println(f.getAbsolutePath());
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(f);
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		System.out.println(config.getFormattedParameterString());
		//File is parsed correctly
	}
	
	@Test
	public void testAClibFormatInactives() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/aclib2.txt");
		File f = new File(url.getPath());
		System.out.println(f.getAbsolutePath());
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(f);
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		configSpace.getParameterConfigurationFromString("-Pcat=d -Pcat2=8.0 -Pord=z -Prealog=1.0", ParameterStringFormat.SURROGATE_EXECUTOR);
		System.out.println(config.getFormattedParameterString());
		//File is parsed correctly
	}
	
	@Test
	public void testIntegerContinuousParameters() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/aclib2integerFormatParam.txt");
		File f = new File(url.getPath());
		System.out.println(f.getAbsolutePath());
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(f);
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		System.out.println(config.getFormattedParameterString());
		//File is parsed correctly
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidArgumentParameter() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/aclib2invalidDefaultParam.txt");
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
		URL url = this.getClass().getClassLoader().getResource("paramFiles/aclib2forbiddenExampleParam.txt");
		File f = new File(url.getPath());
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(f);
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		System.out.println(config.getFormattedParameterString());
		
		assertFalse(config.isForbiddenParameterConfiguration());
		config.put("a", "v2");
		config.put("b", "w2");
		assertTrue(config.isForbiddenParameterConfiguration());
	}
	
	
	public void testOldFormat() {
		URL url = this.getClass().getClassLoader().getResource("paramFiles/aclib2oldFormat.txt");
		File f = new File(url.getPath());
    	ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(f);
	}
		
	@Test
	public void testNameThenSquareBracket()
	{
		ParameterConfiguration config = getConfigSpaceForFile("paramFiles/aclib2continuousNameNoSpaceParam.txt").getDefaultConfiguration();
		double d = Double.valueOf(config.get("name"));
		if(!( d > 0.45 && d < 0.55))
		{
			fail("Value should have been 0.5");
		}
	}
	
	/**
	 * Tests what happens when we specify a value for a parameter that is not in it's domain
	 */
	@Test(expected=ParameterConfigurationStringFormatException.class)
	public void testParamNotInvalidValue()
	{
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2daisy-chain-param.txt");
		p.getParameterConfigurationFromString("-Pa=99989", ParameterStringFormat.SURROGATE_EXECUTOR);
		fail("testParamNotInvalidValue: Should have fired a ParameterConfigurationStringFormatException");
	}
	
	/**
	 * Tests what happens if we specify a parameter that does not appear in the file
	 */
	@Test(expected=ParameterConfigurationStringFormatException.class)
	public void testParamNotInParameterFile()
	{
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2daisy-chain-param.txt");
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
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2daisy-chain-param.txt");
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=2 -Pb=1 -Pc=4 -Pd=2 -Pe=7", ParameterStringFormat.SURROGATE_EXECUTOR);
		

		assertTrue("Parameter e should be active",config.getActiveParameters().contains("e"));
		
	}
	
	/**
	 * Tests Daisy Chain Param File for E being invactive because D is not the correct value
	 */
	@Test
	public void testDaisyChanParamEInActiveBecauseOfD()
	{
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2daisy-chain-param.txt");
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=2 -Pb=1 -Pc=4 -Pd=1 ", ParameterStringFormat.SURROGATE_EXECUTOR);
		
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		
		double[] valueArray = config.toValueArray();
		assertDEquals(valueArray[0], 2);
		assertDEquals(valueArray[1], 1);
		assertDEquals(valueArray[2], 4);
		assertDEquals(valueArray[4], 1);
	}
	
	/**
	 * Tests Daisy Chain Param file for only A being active
	 */
	@Test
	public void testDaisyChanParamAActiveOnly()
	{
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2daisy-chain-param.txt");
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=1 ", ParameterStringFormat.SURROGATE_EXECUTOR);
	
		List<String> paramNames = Lists.newLinkedList();
		paramNames.addAll(p.getParameterNames());
		
		double[] valueArray = config.toValueArray();
		assertDEquals(valueArray[0], 1);
	}
	
	/**
	 * Test retrieval of immediate parent parameters for conditional parameters
	 */
	@Test
	public void testDaisyChainImmediateParents()
	{
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2daisy-chain-param.txt");
				
		Set<String> aParents = p.getImmediateParentParameters("a");
		assertNull(aParents);
		
		Set<String> bParents = p.getImmediateParentParameters("b");
		assertEquals(1, bParents.size());
		assertTrue(bParents.contains("a"));
		
		Set<String> cParents = p.getImmediateParentParameters("c");
		assertEquals(1, cParents.size());
		assertTrue(cParents.contains("b"));
		
		Set<String> dParents = p.getImmediateParentParameters("d");
		assertEquals(1, dParents.size());
		assertTrue(dParents.contains("c"));
		
		Set<String> eParents = p.getImmediateParentParameters("e");
		assertEquals(1, eParents.size());
		assertTrue(eParents.contains("d"));
	}
	
	/**
	 * Test retrieval of all parent parameters for conditional parameters
	 */
	@Test
	public void testDaisyChainAllParents()
	{
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2daisy-chain-param.txt");
		
		Set<String> aParents = p.getAllParentParameters("a");
		assertNull(aParents);
		
		Set<String> bParents = p.getAllParentParameters("b");
		assertEquals(1, bParents.size());
		assertTrue(bParents.contains("a"));
		
		Set<String> cParents = p.getAllParentParameters("c");
		assertEquals(2, cParents.size());
		assertTrue(cParents.contains("a"));
		assertTrue(cParents.contains("b"));
		
		Set<String> dParents = p.getAllParentParameters("d");
		assertEquals(3, dParents.size());
		assertTrue(dParents.contains("a"));
		assertTrue(dParents.contains("b"));
		assertTrue(dParents.contains("c"));
		
		Set<String> eParents = p.getAllParentParameters("e");
		assertEquals(4, eParents.size());
		assertTrue(eParents.contains("a"));
		assertTrue(eParents.contains("b"));
		assertTrue(eParents.contains("c"));
		assertTrue(eParents.contains("d"));
	}
	
	/**
	 * Test immediate and all parents in the single multi-dependency case
	 */
	@Test
	public void testMultipleDependencyParents() {
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2multi-dependency-param.txt");
		
		Set<String> eParents = p.getImmediateParentParameters("e");
		assertEquals(4, eParents.size());
		assertTrue(eParents.contains("a"));
		assertTrue(eParents.contains("b"));
		assertTrue(eParents.contains("c"));
		assertTrue(eParents.contains("d"));
		
		eParents = p.getAllParentParameters("e");
		assertEquals(4, eParents.size());
		assertTrue(eParents.contains("a"));
		assertTrue(eParents.contains("b"));
		assertTrue(eParents.contains("c"));
		assertTrue(eParents.contains("d"));
	}
	
	/**
	 * Test immediate parents in the diamond PCS
	 */
	@Test
	public void testDiamondParents() {
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2diamond-param.txt");
		
		Set<String> eImmediateParents = p.getImmediateParentParameters("e");
		assertEquals(2, eImmediateParents.size());
		assertTrue(eImmediateParents.contains("d"));
		assertTrue(eImmediateParents.contains("c"));
		
		Set<String> eAllParents = p.getAllParentParameters("e");
		assertEquals(3, eAllParents.size());
		assertTrue(eAllParents.contains("d"));
		assertTrue(eAllParents.contains("c"));
		assertTrue(eAllParents.contains("b"));
	}
	
	/**
	 * Tests Diamond Param File for E active
	 */
	@Test
	public void testDiamondParamEActive()
	{
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2diamond-param.txt");
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=1 -Pb=3 -Pc=4 -Pd=6 -Pe=2 ", ParameterStringFormat.SURROGATE_EXECUTOR);
		
		assertTrue("Parameter E should be active",config.getActiveParameters().contains("e"));
		
	}
	
	/**
	 * Test Diamond Param for C being active, but D and E being inactive
	 */
	@Test
	public void testDiamondParamCInActive()
	{
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2diamond-param.txt");
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=1 -Pb=1 -Pd=6  ", ParameterStringFormat.SURROGATE_EXECUTOR);
		
		List<String> paramNames = Lists.newLinkedList();
		
		paramNames.addAll(p.getParameterNames());
		double[] valueArray = config.toValueArray();
		
		assertFalse("Parameter e should be active",config.getActiveParameters().contains("e"));
		assertFalse("Parameter c should be active",config.getActiveParameters().contains("c"));
		
		assertTrue("Parameter d should be active",config.getActiveParameters().contains("d"));
	}
	
	/**
	 * Test Diamond Param for D being active, but C and E being inactive
	 */
	@Test
	public void testDiamondParamDInActive()
	{
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2diamond-param.txt");
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=1 -Pb=2 -Pc=2  ", ParameterStringFormat.SURROGATE_EXECUTOR);
	
		List<String> paramNames = Lists.newLinkedList();
		paramNames.addAll(p.getParameterNames());
		
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
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2diamond-param.txt");
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=1 -Pb=5 ", ParameterStringFormat.SURROGATE_EXECUTOR);
		
		List<String> paramNames = Lists.newLinkedList();
		paramNames.addAll(p.getParameterNames());
		
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
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2diamond-param.txt");
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=1 -Pb=3 -Pd=1 -Pc=2", ParameterStringFormat.SURROGATE_EXECUTOR);
	
		List<String> paramNames = Lists.newLinkedList();
		paramNames.addAll(p.getParameterNames());

		double[] valueArray = config.toValueArray();
		assertDEquals(valueArray[0], 1);
		assertDEquals(valueArray[1], 3);
		assertDEquals(valueArray[4], 1);
		assertDEquals(valueArray[2], 2);
	}
	
	/**
	 * Tests the multi dependency file for e being active
	 */
	@Test
	public void testMultiParamEActive()
	{
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2multi-dependency-param.txt");
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=2 -Pb=2 -Pd=2 -Pc=2 -Pe=1", ParameterStringFormat.SURROGATE_EXECUTOR);
	
		List<String> paramNames = Lists.newLinkedList();
		paramNames.addAll(p.getParameterNames());
		
		double[] valueArray = config.toValueArray();

		assertTrue("Parameter E should be active",config.getActiveParameters().contains("e"));
	}
	
	/**
	 * Tests the multi dependency file for E being inactive because of A
	 */
	@Test
	public void testMultiParamEInActive()
	{
		ParameterConfigurationSpace p = getConfigSpaceForFile("paramFiles/aclib2multi-dependency-param.txt");
		ParameterConfiguration config = p.getParameterConfigurationFromString("-Pa=2 -Pb=2 -Pd=2 -Pc=2 -Pe=2", ParameterStringFormat.SURROGATE_EXECUTOR);
	
		List<String> paramNames = Lists.newLinkedList();
		paramNames.addAll(p.getParameterNames());

		double[] valueArray = config.toValueArray();
		System.out.println(Arrays.toString(valueArray));
		
		
		assertTrue("Parameter E should be active",config.getActiveParameters().contains("e"));
		
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
		File paramFile = TestHelper.getTestFile("paramFiles/aclib2paramEchoParamFile.txt");
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
			//System.out.println("Getting config: " + nextConfig);
			ParameterConfiguration configToTest = configList.get(nextConfig);
			//System.out.println(configToTest);
			
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
	public void testMisplacedType()
	{
		System.out.println("misplaced test");
		String file = "param integer [1, 10] [1]i";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerLowerBound()
	{
		String file = "param integer [0.1, 10] [1]";
		StringReader sr = new StringReader(file);
		 new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerUpperBound()
	{
		String file = "param integer [1, 10.5] [1]";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerDefault()
	{
		String file = "param integer [1, 10] [1.5]";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerLowerBoundLog()
	{
		String file = "param integer [1.1, 10] [2] log";
		StringReader sr = new StringReader(file);
		 new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerUpperBoundLog()
	{
		String file = "param integer [1, 10.5] [1] log";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnNonIntegerDefaultLog()
	{
		
		String file = "param integer [1, 10] [1.5] log";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnLogCategorial()
	{
		
		String file = "param i {1,2,3} [1]";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentOnIntegralCategorial()
	{
		
		String file = "param {1,2,3} [1] log";
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
		
		String file = "-numPCA [1,10] log [1]";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalBothFlagSetInIncorrectOrder()
	{
		
		String file = "-numPCA [1,10]log integer [1]";
		StringReader sr = new StringReader(file);
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalDependentValue()
	{
		StringReader sr = new StringReader(
				"foo categorical { a, b, c, d } [a]\n" +
				"bar categorical { 1,2,3,4} [1]\n" +
				"bar | foolar in { a,b }");
		new ParameterConfigurationSpace(sr);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalIndependentValue()
	{
		StringReader sr = new StringReader(
				"foo categorical { a, b, c, d } [a]\n" +
				"bar categorical { 1,2,3,4} [1]\n" +
				"barar | foo in { a,b }");
		new ParameterConfigurationSpace(sr);
	}
	
	@Test
	public void testIntegralValue()
	{
		String file = "-numPCA integer [1,20] [7]\n-numberSearch integer [1,10000000] [2000]\n";
		StringReader sr = new StringReader(file);
		String exec = new ParameterConfigurationSpace(sr).getDefaultConfiguration().getFormattedParameterString(ParameterStringFormat.NODB_SYNTAX);
		System.out.println(exec);
		assertEquals( "Expected no decimal places (decimal point occured):",  exec.indexOf("."),-1);
	}
	
	@Test
	public void testDefaultFromSpecialString()
	{
		StringReader sr = new StringReader("-foo real [1,100] [82.22]log");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr);
		ParameterConfiguration defaultConfiguration = configSpace.getDefaultConfiguration();
		
		for(ParameterStringFormat f : ParameterStringFormat.values())
		{
			assertEquals(defaultConfiguration, configSpace.getParameterConfigurationFromString("DEFAULT", f));
			assertEquals(defaultConfiguration, configSpace.getParameterConfigurationFromString("<DEFAULT>", f));
		}
		System.out.println(defaultConfiguration.getFormattedParameterString());
	}
	
	@Test
	public void testRandomFromSpecialString()
	{
		StringReader sr = new StringReader("-foo real [1,100] [82.22]log");
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
		StringReader sr = new StringReader("test categorical {a,b,c,d,e,f} [a]\n"
				+ "bar real [0,1000] [1]\n"
				+ "bar2 integer [0,1000] [1]\n"
				+ "bar3 integer [1,1000] [1] log\n"
				+ "bar4 real [1,1000] [1]log\n" 
				+ "test2 categorical { a,b,c,d,e,f} [b]\n"
				+ "test3 ordinal { a,b,c,d,e,f, Az,Bz, Cz, dZ,eZ} [c]\n"
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
	  
		StringReader sr = new StringReader("DEFAULT categorical {DEFAULT} [DEFAULT] ");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr);
		ParameterConfiguration defaultConfiguration = configSpace.getDefaultConfiguration();
		for(ParameterStringFormat f : ParameterStringFormat.values())
		{
			assertFalse("Was able to get a DEFAULT as a string representation for StringFormat " + f.toString(), defaultConfiguration.getFormattedParameterString(f).trim().toUpperCase().equals("DEFAULT"));
		}
		
		StringReader sr2 = new StringReader("<DEFAULT> categorical {<DEFAULT>} [<DEFAULT>]");
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
	  
		StringReader sr = new StringReader("RANDOM categorical {RANDOM} [RANDOM] ");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr);
		ParameterConfiguration defaultConfiguration = configSpace.getDefaultConfiguration();
		for(ParameterStringFormat f : ParameterStringFormat.values())
		{
			assertFalse("Was able to get a RANDOM as a string representation for StringFormat " + f.toString(), defaultConfiguration.getFormattedParameterString(f).trim().toUpperCase().equals("RANDOM"));
		}
		
		StringReader sr2 = new StringReader("<RANDOM> categorical {<RANDOM>} [<RANDOM>]");
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
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a categorical { 0,1,2,3,4,5,6,7,8,9 } [0] \n b categorical { 0,1,2,3,4,5,6,7,8,9 } [0] \n c categorical { 0,1,2,3,4,5,6,7,8,9 } [0] \n d categorical { 0, 1} [0] \n d | c in { 0 } ");
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
		StringReader sr = new StringReader("foo categorical { a, b, c, d } [a]");
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
		
		String sf = "foo categorical { a, b, c } [a] \n bar categorical { d, e,f} [f]";
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
		StringReader sr = new StringReader("foo   categorical { a, b, c, d } [a]");
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
		StringReader sr = new StringReader("foo real [0,1] [0.1]\n" +
				"bar categorical { a, b, c } [a]");
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
		StringReader sr = new StringReader("foo real [0,1] [0.1]\n" +
				"bar real [0,1] [0.1]\n" +
				"tar categorical { a,b,c,d,e } [e]\n "+
				"gzi categorical { a,b,c,d,e} [a]\n");
		
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
		StringReader sr = new StringReader("foo real [0,1] [0.1]\n" +
				"bar real [0,1] [0.1]\n" +
				"tar categorical { a,b,c,d,e } [e]\n "+
				"gzi categorical { a,b,c,d,e} [a]\n" +
				"bzi categorical { a,b,c,d,e} [c]\n" + 
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
		StringReader sr = new StringReader("foo real [0,1] [0.2]\n" +
				"bar real [0,1] [0.1]");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, Collections.singletonMap("foo", "0.1") );
		assertEquals("# neighbours", 4, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
		assertEquals("Default correct", "0.2", configSpace.getDefaultConfiguration().get("foo"));
		assertFalse("Configuration should be in Subspace", configSpace.getDefaultConfiguration().isInSearchSubspace());
		
	}
	
	@Test
	public void testSubspaceDeclarationNotDefault()
	{
		StringReader sr = new StringReader("foo categorical { a, b, c, d } [a]");
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
		
		StringReader sr = new StringReader("foo categorical { a, b, c, d } [d]\n {foo = a}");
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
				"foo categorical { a, b, c, d } [a]\n" +
				"bar categorical { 1,2,3,4} [1]\n" +
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
				"foo categorical { a, b, c, d } [a]\n" +
				"bar categorical { 1,2,3,4} [1]\n" +
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
				"foo categorical { a, b, c, d } [d]\n" +
				"bar categorical { 1,2,3,4} [1]\n" +
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
				"foo categorical { a, b, c, d } [a]\n" +
				"bar categorical { 1,2,3,4} [1]\n" +
				"bar | foo in { a,b }");
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, Collections.singletonMap("foo", "a"));
		assertEquals("Should have 3 neighbours", 3, configSpace.getDefaultConfiguration().getNeighbourhood(rand,NUMBER_OF_NEIGHBOURS).size());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSubspaceValidation()
	{
		StringReader sr = new StringReader(
				"foo categorical { a, b, c, d } [a]\n" +
				"bar categorical { 1,2,3,4} [1]\n" +
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
			StringReader sr = new StringReader("foo real [0,10] [5]");
			ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, "<>", Collections.EMPTY_MAP);
			assertEquals(configSpace.getDefaultConfiguration().getNeighbourhood(rand,i).size(), i);
		}
		
		try {
			StringReader sr = new StringReader("foo real [0,10] [5]");
			ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, "<>", Collections.EMPTY_MAP);
			
			assertEquals(configSpace.getDefaultConfiguration().getNeighbourhood(rand,-1).size(), -1);
			fail("Should have crashed with zero neighbours");
		} catch(IllegalArgumentException e)
		{
			
		}
		
		for(int i=0; i < 25; i++)
		{
			StringReader sr = new StringReader("foo real [0,10] [5]\nbar [0,10] [5]");
			ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(sr, "<>", Collections.EMPTY_MAP);
			
			assertEquals(configSpace.getDefaultConfiguration().getNeighbourhood(rand,i).size(), 2*i);
		}
	}
	
	@Test
	public void testParameterSpaceUpperBounds()
	{
		
		System.out.println("Expect 1 : "+ParameterConfigurationSpace.getSingletonConfigurationSpace().getUpperBoundOnSize());
		assertTrue("Singleton space should have >= 1 configuration ", ParameterConfigurationSpace.getSingletonConfigurationSpace().getUpperBoundOnSize() >= 1);
		
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(new StringReader("foo categorical { a,b,c} [a]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 3 : "+ configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 3", configSpace.getUpperBoundOnSize() >= 3);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo categorical { a,b,c} [a]\n bar categorical { d,e,f} [f]"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 9 : " +configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 9", configSpace.getUpperBoundOnSize() >= 9);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo categorical { a,b,c} [a]\n bar categorical { d,e,f} [f]\n bar | foo in {a}"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 9 : " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 9", configSpace.getUpperBoundOnSize() >= 9);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo categorical { a,b,c} [a]\n bar categorical { d,e,f} [f]\n"),"<>",Collections.singletonMap("foo", "a"));
		System.out.println("Expect 9 : " + configSpace.getUpperBoundOnSize());
		
		assertTrue("Size should be >= 9", configSpace.getUpperBoundOnSize() >= 9);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo categorical { a,b,c} [a]\n bar categorical { d,e,f} [f]\n"),"<>",Collections.singletonMap("foo", "b"));
		System.out.println("Expect 9 : " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 9", configSpace.getUpperBoundOnSize() >= 9);
		
		List<ParameterConfiguration> neighbours = configSpace.getDefaultConfiguration().getNeighbourhood(rand,4);
		neighbours.add(configSpace.getDefaultConfiguration());
		
		HashSet<ParameterConfiguration> newSet = new HashSet<ParameterConfiguration>(neighbours);
		
		
		for(ParameterConfiguration config : newSet)
		{
			System.out.println(config.getFormattedParameterString());
		}
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo categorical { a,b,c} [a]\n bar categorical { d,e,f} [f]\n {foo = a, bar = d}"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 9 :" + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 9", configSpace.getUpperBoundOnSize() >= 9);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo real [0,1][1]\n bar categorical { d,e,f} [f]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect Infinity: " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= Infinity", configSpace.getUpperBoundOnSize() >= Double.POSITIVE_INFINITY);
		
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo integer [0,1][1]\n bar categorical { d,e,f} [f]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 6: " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 6", configSpace.getUpperBoundOnSize() >= 6);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo integer [0,9][1]\n bar categorical { d,e,f} [f]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 30: " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 30", configSpace.getUpperBoundOnSize() >= 30);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo integer [0,9][1]\n bar integer [0,9] [1]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 100: " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= 100", configSpace.getUpperBoundOnSize() >= 100);
		
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo integer [0,9][1]\n bar real [0,9] [1]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect Infinity: " + configSpace.getUpperBoundOnSize());
		assertTrue("Size should be >= Infinity", configSpace.getUpperBoundOnSize() >= Double.POSITIVE_INFINITY);
		
	}
	
	@Test
	public void testParameterSpaceLowerBounds()
	{
		
		System.out.println("Expect 1 : "+ParameterConfigurationSpace.getSingletonConfigurationSpace().getLowerBoundOnSize());
		assertTrue("Singleton space should have >= 1 configuration ", ParameterConfigurationSpace.getSingletonConfigurationSpace().getLowerBoundOnSize() >= 1);
		
		ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(new StringReader("foo categorical { a,b,c} [a]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 3 : "+ configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 3", configSpace.getLowerBoundOnSize() <= 3);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo categorical { a,b,c} [a]\n bar categorical { d,e,f} [f]"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 9 : " +configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 9", configSpace.getLowerBoundOnSize() <= 9);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo categorical { a,b,c} [a]\n bar categorical { d,e,f} [f]\n bar | foo in {a}"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 3 : " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 5", configSpace.getLowerBoundOnSize() <= 5);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo categorical { a,b,c} [a]\n bar categorical { d,e,f} [f]\n"),"<>",Collections.singletonMap("foo", "a"));
		System.out.println("Expect 9 : " + configSpace.getLowerBoundOnSize());
		
		assertTrue("Size should be <= 9", configSpace.getLowerBoundOnSize() <= 9);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo categorical { a,b,c} [a]\n bar categorical { d,e,f} [f]\n"),"<>",Collections.singletonMap("foo", "b"));
		System.out.println("Expect 9 : " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 9", configSpace.getLowerBoundOnSize() <= 9);
		
		List<ParameterConfiguration> neighbours = configSpace.getDefaultConfiguration().getNeighbourhood(rand,4);
		neighbours.add(configSpace.getDefaultConfiguration());
		
		HashSet<ParameterConfiguration> newSet = new HashSet<ParameterConfiguration>(neighbours);
		
		
		for(ParameterConfiguration config : newSet)
		{
			System.out.println(config.getFormattedParameterString());
		}
		
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo categorical { a,b,c} [a]\n bar categorical { d,e,f} [f]\n {foo = a, bar = d}"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 1 :" + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 8", configSpace.getLowerBoundOnSize() <= 8);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo real [0,1][1]\n bar categorical { d,e,f} [f]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect Infinity: " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= Infinity", configSpace.getLowerBoundOnSize() <= Double.POSITIVE_INFINITY);
		
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo integer [0,1][1]\n bar categorical { d,e,f} [f]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 6: " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 6", configSpace.getLowerBoundOnSize() <= 6);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo integer [0,9][1]\n bar categorical { d,e,f} [f]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 30: " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 30", configSpace.getLowerBoundOnSize() <= 30);
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo integer [0,9][1]\n bar integer [0,9] [1]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect 100: " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= 100", configSpace.getLowerBoundOnSize() <= 100);
		
		
		configSpace = new ParameterConfigurationSpace(new StringReader("foo integer [0,9][1]\n bar [0,9] [1]\n"),"<>",Collections.EMPTY_MAP);
		System.out.println("Expect Infinity: " + configSpace.getLowerBoundOnSize());
		assertTrue("Size should be <= Infinity", configSpace.getLowerBoundOnSize() <= Double.POSITIVE_INFINITY);
	}
	
	@Test
	/**
	 * Related to bug 1718
	 */
	public void testEmptyParamValue()
	{
		StringReader sr = new StringReader("foo categorical {\"\",\"test\"} [\"\"]\n");
		
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

		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("foo categorical {a,b,c} [a]\nbar categorical {e,d,f} [f]\nbar | foo in { c }");		
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

		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("foo categorical {a,b,c} [a]\nbar categorical{e,d,f} [f]\n cat categorical {2,3,4} [2] \nbar | foo in { c }");		
	

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

		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("foo categorical {a,b,c} [a]\nbar categorical {e,d,f} [f]\n cat real [2,4] [2] \nbar | foo in { c }");		
	
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
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("foo categorical {a,b,c} [a]\nbar categorical{e,d,f} [f]\n \nbar | foo in { a }");		
	
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

		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("foo categorical {a,b,c} [a]\nbar categorical{e,d,f} [f]\n cat categorical { g,h,i } [h] \n bar | foo in { c } \n { foo=a,bar=d,cat=g} ");		
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
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a categorical { 1,2} [1]\n b categorical { 1,2} [1]\n c categorical { 1,2} [1]\n d categorical { 1,2} [1]\n e categorical { 1,2} [1]\n f categorical { 1,2} [1]\n g cat{ 1,2} [1]\n h categorical { 1,2} [1]\n i categorical { 1,2} [1]\n");
		
		StopWatch t = new AutoStartStopWatch();
		
		HashSet<ParameterConfiguration> configs = new HashSet<ParameterConfiguration>();
		for(int i=0; i < 1000000; i++)
		{
			configs.add(configSpace.getDefaultConfiguration());
			
		}
		
		System.err.println("Speedtest Time: "+t.stop() / 1000.0);
	}
	
	@Test
	@Ignore
	public void testRandomSpeed()
	{
		 
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("x0 real [-3,3] [3]\n"+
"x1 real [-2, 2] [2]\n"+
"abs(3*X) real [-4,4] [-4]\n"+
"-cos(X)+1 real [0,6.28] [2]\n"+
"exp(X)-1 real [0,10] [8]\n"+
"abs(2*X) real [-4,4] [-4]\n"+
"abs(4*X) real [-4,4] [-4]\n"+
"abs(X) real [-4,4] [-4]\n"+
"x1 | exp(X)-1>2 || abs(X) < 0");
		MersenneTwisterFast fast = new MersenneTwisterFast(rand.nextLong());
		
		AutoStartStopWatch watch = new AutoStartStopWatch();
		for(int i=0; i < 10; i++)
		{
			for(int j=0; j < 2000000; j++)
			{
				configSpace.getRandomParameterConfiguration(fast);
			}
			System.err.println("Lap: "+watch.laps()/1000.0 + " secs");
		}
		
		System.err.println("Average time " + watch.stop() / 10000.0 + " seconds");
		
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
	public void testWeirdNames()
	{
		
		String weirdPCS = "weirdN>ame categorical { true, false} [true]\n"+
						  "second categorical { true, false} [true]\n" + 
						  "second | weirdN>ame in { true}\n";
				
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(weirdPCS);
		
		System.out.println(configSpace.getRandomParameterConfiguration(rand).getFormattedParameterString());
	}
	
	@Test
	public void testConditionalPrecedence()
	{
		String precendencePCS = "a categorical { true, false} [true]\n"+
								"b categorical { true, false} [true]\n"+
								"c categorical { true, false} [true]\n"+
								"d categorical { true, false} [true]\n"+
								"e categorical { true, false} [true]\n"+
								"f categorical { true, false} [true]\n"+
								"g categorical { active} [active]\n"+
								"g | c in {true} && b == true || a == true && d in {true} && e in {true} || f == true";
				
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(precendencePCS);
		
		Map<String, Boolean> parameters = new TreeMap<String,Boolean>();
		
		for(int i=0; i < 2500; i++)
		{ //Try and make sure every configuration is generated
			ParameterConfiguration configuration = configSpace.getRandomParameterConfiguration(rand);
			
			boolean a = Boolean.valueOf(configuration.get("a"));
			boolean b = Boolean.valueOf(configuration.get("b"));
			boolean c = Boolean.valueOf(configuration.get("c"));
			boolean d = Boolean.valueOf(configuration.get("d"));
			boolean e = Boolean.valueOf(configuration.get("e"));
			boolean f = Boolean.valueOf(configuration.get("f"));
			
			boolean gActive =   c && b || a && d && e || f;
			
			
			
			assertEquals("Prediction of active and parameter configuration space active differ for:" + configuration.getFormattedParameterString() , gActive,configuration.getFormattedParameterString().contains("-g 'active'"));
			parameters.put(configuration.getFormattedParameterString(),gActive);;
					
		}
		
		for(Entry<String,Boolean> s : parameters.entrySet())
		{
			System.out.println(s.getKey() + "=>" + s.getValue());
		}
		
	}
	
	@Test
	public void testConditionalOperatorsSimple()
	{
		
		String pcsFile = "";
		ParameterConfigurationSpace configSpace = null;
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "B | A == 5";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "B | A != 5";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "B | A == 5";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "B | A != 5";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "B | A in { 5 } ";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "B | A in { 6 }";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "B | A in { 5 } ";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "B | A in { 6 }";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "B | A > 4 ";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "B | A > 5";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "B | A < 6 ";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "B | A < 5";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		
		
		
		
		
		
		//System.out.println(configSpace.getDefaultConfiguration().getFormattedParameterString());
				
	}
	
	
	
	@Test
	public void testSyntaxErrors()
	{
		
		try {
			
		
			String pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 5 | C == true";
		
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
		}
		try {
			
			
			String pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 5 \n"
				+ "B | C == 6\n";
		
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
		}
		
		
		try {
			
			
			String pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 5 & C == true";
		
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
		}
		
		
		
		try {	
			String pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 5 5";
		
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
		}
		
	
		try {	
			String pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B  A == 5 ";
		
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
		}
	
		

		try {	
			String pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "C categorical {a,b,c} [a] ";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
		}
	
		try {	
			String pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "C | A > -1";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
			
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
		
		}
	
		try {	
			String pcsFile = "A integer [0,10] [-1]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
			
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
			
		}
	
		try {	
			String pcsFile = "A integer [0,10] [0.5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
			
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
			
		}
	

		try {	
			String pcsFile = "A integer [0,10] [2]\n"
				+ "B integer [100,10] [5]\n"
				+ "C categorical {true, false} [true]\n";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
			
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
			
		}
	
	

		try {	
			String pcsFile = "A integer [0,10] [2]\n"
				+ "B integer [-1,10] [5] log\n"
				+ "C categorical {true, false} [true]\n";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
			
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
			
		}
	
	
		
		
		
		
		try {	
			String pcsFile = "A  categorical {} []\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
			
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
			
		}
	
		
		
		try {	
			String pcsFile = "A categorical {a,b,   a    } [b]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
			
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
			
		}
		
		
		try {	
			String pcsFile = "A categorical {a,b } [b]\n"
				+ "B integer [0,10.5] [5]\n"
				+ "C categorical {true, false} [true]\n";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
			
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
			
		}
		
	
		
		
		try {	
			String pcsFile = "A categorical {a,b,} [a]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "C | A in {a,a,b} && B > 2";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
			
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
			
		}

		
		
		try {	
			String pcsFile = "A categorical {a,b,} [a]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "C | A in {a,b} && B > 2\n"
				+ "B | C in {true}";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			fail("Expected Exception");
			
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
			
		}

		
		
		
		try {
			String pcsFile = "A {a,b,} [a]\n"
				+ "B [0,10] [5]i\n"
				+ "C categorical {true, false} [true]\n"
				+ "C | A in {a,b} || A in {a}";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			//System.out.println(configSpace.getPCSFile());
			
			
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
			
		}
		
		
		try {
			String pcsFile = "A {a,b} [a]\n"
				+ "B [0,10] [5]i\n"
				+ "{A > 3}{B < 4}";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			//System.out.println(configSpace.getPCSFile());
			
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			//Good
			//e.printStackTrace();
			//System.err.println( e.getMessage());
			
		}
		

		try {
			String pcsFile = "A categorical {a,b} [a]\n"
				+ "B ordinal { 0,1,2,3,5,10} [5]\n"
				+ "A | B > 6";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			//System.out.println(configSpace.getPCSFile());
			
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			//Good
			//e.printStackTrace();
			//System.err.println( e.getMessage());
			
		}
		
		
		try {
			String pcsFile = "A categorical {a,b} [a]\n"
				+ "B ordinal { 0,1,2,3,5,10} [5]\n"
				+ "A | B in {0,1,2,3,5,7,10}";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			//System.out.println(configSpace.getPCSFile());
			
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			//Good
			//e.printStackTrace();
			//System.err.println( e.getMessage());
			
		}
		
		
		try {
			String pcsFile = "A categorical {a,b} [a]\n"
			    + " C real [0.01,1] [0.5] log\n"
				+ "B ordinal { 0,1,2,3,5,10} [5]\n"
				+ "A | C < 0.9 && B in {0,1,2,3,5,7,10}";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			//System.out.println(configSpace.getPCSFile());
			
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			//Good
			//e.printStackTrace();
			//System.err.println( e.getMessage());
			
		}
		
		try {
			String pcsFile = "A categorical {a,b} [a]\n"
			    + " C real [0.01,1] [0.5] log\n"
				+ "B ordinal { 0,1,2,3,5,10} [5]\n"
				+ "A | C < 1.1";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			//System.out.println(configSpace.getPCSFile());
			
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			//Good
			//e.printStackTrace();
			//System.err.println( e.getMessage());
			
		}
		
		
		
		

		try {
			String pcsFile = "A categorical {a,b} [a]\n"
			    + " C real [0.01,1] [0.5] log\n"
				+ "B categorical { 0,1,2,3,5,10, abuetnh} [5]\n"
				+ "A | B < abuetnh";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			//System.out.println(configSpace.getPCSFile());
			
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			//Good
			//e.printStackTrace();
			//System.err.println( e.getMessage());
			
		}
		
		try {
			String pcsFile = "sp-test real [0,10][0] \n"
					+ "ab real [0,10] [0]\n"
					+ "_de real [0,10] [0]\n"
					+ "noeth categorical { 24, 59, dg, af, unth-oeuh } [24]\n"
					+ "{ (sp-test-3)(sp-test+3)(sp-test+1) > 0 }";
			    
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			//System.out.println(configSpace.getPCSFile());
			
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			//Good
			//e.printStackTrace();
			//System.err.println( e.getMessage());
			
		}
		
		
		try {
			String pcsFile ="abc categorical { off, 1, on, 2, yes, 3, no } [on]\n"
					+		"def ordinal { off, 1, on, 2, yes, 3, no } [on]\n"
					+		"on categorical { 1, 2 } [1]\n"
					+ "maybe categorical { 1,2} [2] \n"
					+ "off categorical { 1 , 2} [1]\n"
					+ "{abc == off && on < 2}";
					//+		"heur_order categorical { heur1then2, heur2then1 } [heur1then2]";
					//+		"heur_order | heur1 == on && heur2 == on";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			//System.out.println(configSpace.getPCSFile());
			
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			//Good
			//e.printStackTrace();
			//System.err.println( e.getMessage());
			
		}
		
		
		
		
		
		
		
		
		
		try {
			String pcsFile ="heur1 categorical { off, on } [on]"
					+		"heur2 categorical { off, on } [on]";
					//+		"heur_order categorical { heur1then2, heur2then1 } [heur1then2]";
					//+		"heur_order | heur1 == on && heur2 == on";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			//System.out.println(configSpace.getPCSFile());
			
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			//Good
			//e.printStackTrace();
			//System.err.println( e.getMessage());
			
		}
		
		try {
			String pcsFile ="heur1 ordinal { 10, 1 , 0} [1]\n {heur1 > 1}";
					//+		"heur_order categorical { heur1then2, heur2then1 } [heur1then2]";
					//+		"heur_order | heur1 == on && heur2 == on";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			//System.out.println(configSpace.getPCSFile());
			System.out.println(configSpace.getForbiddenOrdinalAndCategoricalValues());
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			//Good
			//e.printStackTrace();
			//System.err.println( e.getMessage());
			
		}
		
		try {
			String pcsFile ="heur1 ordinal { a, b} [a]\n"
					+ "heur2 ordinal { b, c,d, e } [b]\n"
					+ "heur3 categorical { c, d,d1,e,f,z0,a0} [c]\n"
					+ "heur4 ordinal {d, e} [d]\n"
					+ "heur5 ordinal {d0, d} [d]\n"
					+ "heur6 ordinal {e, f} [e]\n"
					+ "heur7 ordinal {d, a} [a]\n"
					+ "\n {heur1 > b}";
					//+		"heur_order categorical { heur1then2, heur2then1 } [heur1then2]";
					//+		"heur_order | heur1 == on && heur2 == on";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			//System.out.println(configSpace.getPCSFile());
			System.out.println(configSpace.getForbiddenOrdinalAndCategoricalValues());
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			//Good
			//e.printStackTrace();
			//System.err.println( e.getMessage());
			
		}
		
		
		try {
			String pcsFile ="heur1 ordinal { a, 1} [a]\n"
					+ "heur2 ordinal { 2, b } [b]\n"
					+ "heur3 ordinal { b, 3 } [b]\n"
					+ "heur4 ordinal { 4, c } [c]\n"
					+ "heur5 ordinal { c, a } [c]\n"
					
					+ "\n {heur1 > 5}";
					//+		"heur_order categorical { heur1then2, heur2then1 } [heur1then2]";
					//+		"heur_order | heur1 == on && heur2 == on";
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			//System.out.println(configSpace.getPCSFile());
			System.out.println(configSpace.getForbiddenOrdinalAndCategoricalValues());
			fail("Expected Exception");
		} catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			//Good
			//e.printStackTrace();
			//System.err.println( e.getMessage());
			
		}
		
		
		
	
		/*
		try {	
			String pcsFile = "A ordinal { LOW, MEDIUM, HIGH} [LOW]\n";
				
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			
			System.out.println(Arrays.toString(configSpace.getDefaultConfiguration().toValueArray()));
			fail("Expected Exception");
			
		} catch(IllegalArgumentException e)
		{
			//Good
			System.err.println(e.getMessage());
			
		}
		*/
			
	}
	
	@Test
	public void testConditionalOperatorsAdvanced()
	{
		
		
		/**
		 * Integer == , != Tests
		 */
		String pcsFile = "";
		ParameterConfigurationSpace configSpace = null;
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 5 && C == true";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 5 || C == true";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 6 || C == true";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 5 || C == false";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A != 5 && C == true";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 5 && C == false";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 5 && C != true";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		/**
		 *  Real Tests ==, != Tests
		 **/
		
		
		
		
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 5 && C == true";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 5 || C == true";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 6 || C == true";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 5 || C == false";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
	
		
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A != 5 && C == true";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
	

		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A == 5 && C == false";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
	
		
		

		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A != 5 || C != true";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
	
		
		

		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A != 5 || C == false";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
	
		
		
		
		
		
		
		
		
		
		
		/**
		 * Integer Set tests
		 */
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | C == true && A in { 5 } ";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | C == false || A in { 5 } ";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | C == true || A in { 6 } ";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A in { 6 } || C != false";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		
	
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A in { 6 } && C == true";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
	

		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A in { 5 } && C == false";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		

		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A in { 6 } || C == false";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		

		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | C != true || A in { 6 }";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		
		
		/**
		 * Real set tests
		 */

		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | C == true && A in { 5 }";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		

		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | C == false || A in { 5 }";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		

		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | C == true || A in { 6 }";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		

		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A in { 6 } || C != false";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A in { 6 } && C == true";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A in { 6 } || C == false";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A in { 6 } || C != true";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A in { 6 } && C != false";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A > 4 && C == true ";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A > 3.5 || C == false ";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A > 5 && C == false";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		
		
		pcsFile = "A integer [0,10] [5]\n"
				+ "B integer [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A > 6 && C == true";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A < 6 && C == true ";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A < 5.5 || C == false ";
				
		assertTrue("Expected that B would be active", checkWhetherBIsActive(pcsFile));
		
		
		
		
		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A < 5 && C == false";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		
		

		
		pcsFile = "A real [0,10] [5]\n"
				+ "B real [0,10] [5]\n"
				+ "C categorical {true, false} [true]\n"
				+ "B | A < 4 || C in { false} ";
				
		assertFalse("Expected that B would NOT be active", checkWhetherBIsActive(pcsFile));
		//System.out.println(configSpace.getDefaultConfiguration().getFormattedParameterString());
				
	}
	
	
	
	/**
	 * @param pcsFile
	 * @return
	 */
	public boolean checkWhetherBIsActive(String pcsFile) {
		ParameterConfigurationSpace configSpace =  ParamFileHelper.getParamFileFromString(pcsFile);
		ParameterConfiguration  config =configSpace.getDefaultConfiguration();
		Set<String> activeParameters = config.getActiveParameters();
		return activeParameters.contains("B");
	}
	
	@Test
	public void checkNewForbiddenClausesRadius()
	{
		
		String pcsFile = "x real [-1,1] [0]\n"
				+ "y real [-1,1] [0]\n"
				+ "{ x^2+y^2 > 1 }";
				
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		
		TreeSet<String> output = new TreeSet<String>();
		
		for(int i=0; i < 10000; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(rand);
			double r = Math.sqrt( Math.pow(Double.valueOf(config.get("x")),2) + Math.pow(Double.valueOf(config.get("y")),2));
			
			assertTrue("Expected distance should be less than one", r < 1);
			//output.add(/*r + "=>" +*/ config.getFormattedParameterString() + "=> " + r);
		}
		
		for(String line : output)
		{
			//System.out.println(line);
		}
	}
	
	
	@Test
	@Ignore
	public void checkNewForbiddenClausesSpeedMT() throws InterruptedException
	{
		
		String pcsFile = "x real [-1,1] [0]\n"
				+ "y real [-1,1] [0]\n"
				+ "{ x^2+y^2 > 1 }";
				
		final ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		
		TreeSet<String> output = new TreeSet<String>();
		
		ExecutorService execService = Executors.newFixedThreadPool(4);
		
		for(int j=0; j < 45; j++)
		{
			final CountDownLatch latch = new CountDownLatch(1);
			final CountDownLatch complete = new CountDownLatch(100);
			Runnable run = new Runnable()
			{
				
				@Override
				public void run() {
					try {
						latch.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					for(int i=0; i < 10000; i++)
					{
						ParameterConfiguration config = configSpace.getRandomParameterConfiguration(rand);
						//double r = Math.sqrt( Math.pow(Double.valueOf(config.get("x")),2) + Math.pow(Double.valueOf(config.get("y")),2));
						
						//assertTrue("Expected distance should be less than one", r < 1);
						//output.add(/*r + "=>" +*/ config.getFormattedParameterString() + "=> " + r);
					}
					complete.countDown();
				}
			};
			
			for(int i=0; i < 100; i++)
			{
				execService.submit(run);
			}
			
			AutoStartStopWatch watch = new AutoStartStopWatch();
			latch.countDown();
			
			complete.await();
			watch.stop();
			
			if(j > 4)
			{
				System.out.println(/*j-4 + ","*/ + watch.time() / 1000.0);
			}
			System.gc();
		}
		
	}
	

	@Test
	public void checkNewForbiddenClausesOrder()
	{
		
		String pcsFile = "x real [0,1] [0.5]\n"
				+ "y real [0,1] [0.5]\n"
				+ "{ x > y }";
		/*	+ "z real [0,1] [0.5]\n"
		+ "{ x > y || y > z }";
	*/				
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		
		TreeSet<String> output = new TreeSet<String>();
		
		for(int i=0; i < 10000; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(rand);
			
			double x = Double.valueOf(config.get("x"));
			double y = Double.valueOf(config.get("y"));
			
			//double r = Math.sqrt( Math.pow(Double.valueOf(config.get("x")),2) + Math.pow(Double.valueOf(config.get("y")),2));
			
			
			assertTrue("Expected x: " + x + "<= y:" + y + " for configuration: " + config.getFormattedParameterString() , x <= y);
			output.add(/*r + "=>" +*/ config.getFormattedParameterString());
		}
		
		for(String line : output)
		{
			System.out.println(line);
		}
		
		
		
		pcsFile = "x real [0,1] [0.5]\n"
				+ "y real [0,1] [0.5]\n"
				+ "z real [0,1] [0.5]\n"
				+ "{ x > y }\n"
				+ "{ y > z }";
					
		configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		
		System.err.println("Second");
		System.out.println("Second");
		
		
		output = new TreeSet<String>();
		
		for(int i=0; i < 10000; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(rand);
			
			double x = Double.valueOf(config.get("x"));
			double y = Double.valueOf(config.get("y"));
			double z = Double.valueOf(config.get("z"));
			
			//double r = Math.sqrt( Math.pow(Double.valueOf(config.get("x")),2) + Math.pow(Double.valueOf(config.get("y")),2));
			
			
			try 
			{
				assertTrue("Expected x: " + x + "<= y:" + y + " for configuration: " + config.getFormattedParameterString() , x <= y);
				assertTrue("Expected y: " + x + "<= z:" + y + " for configuration: " + config.getFormattedParameterString() , y <= z);
			} catch(AssertionError e)
			{
				System.out.println(config.isForbiddenParameterConfiguration());
				System.out.println(config.isForbiddenParameterConfiguration());
				throw e;
			}
			output.add(/*r + "=>" +*/ config.getFormattedParameterString());
		}
		
		for(String line : output)
		{
			System.out.println(line);
		}
		
		
		
		pcsFile = "x real [0,1] [0.5]\n"
				+ "y real [0,1] [0.5]\n"
				+ "z real [0,1] [0.5]\n"
				+ "{ (x > y) || (y > z) }\n";
					
		configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		
		System.err.println("Third");
		System.out.println("Third");
		output = new TreeSet<String>();
		
		for(int i=0; i < 10000; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(rand);
			
			double x = Double.valueOf(config.get("x"));
			double y = Double.valueOf(config.get("y"));
			double z = Double.valueOf(config.get("z"));
			
			//double r = Math.sqrt( Math.pow(Double.valueOf(config.get("x")),2) + Math.pow(Double.valueOf(config.get("y")),2));
			
		
			try 
			{
				assertTrue("Expected x: " + x + "<= y:" + y + " for configuration: " + config.getFormattedParameterString() , x <= y);
				assertTrue("Expected y: " + x + "<= z:" + y + " for configuration: " + config.getFormattedParameterString() , y <= z);
				
			} catch(AssertionError e)
			{
				System.out.println(config.isForbiddenParameterConfiguration());
				System.out.println(config.isForbiddenParameterConfiguration());
				throw e;
			}
			output.add(/*r + "=>" +*/ config.getFormattedParameterString());
		}
		
		for(String line : output)
		{
			System.out.println(line);
		}
		
		
		
		assertFalse( 0.2 > 0.3 || 0.3 > 0.4);
		
		
		assertFalse( 0.2 > 0.3 && 0.3 > 0.4);
		
		
		assertTrue( 0.2 > 0.1 || 0.1 > 0.4);
		
		assertFalse( 0.2 > 0.1 && 0.1 > 0.4);
		
		
		assertTrue( 0.2 > 0.1 || 0.1 > 0.01);
		
		assertTrue( 0.2 > 0.1 && 0.1 > 0.01);
		
		
		
		pcsFile = "x real [0,1] [0.5]\n"
				+ "y real [0,1] [0.5]\n"
				+ "z real [0,1] [0.5]\n"
				+ "{ x > y || y > z }\n";
					
		configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		
		System.err.println("Fourth");
		System.out.println("Fourth");
		output = new TreeSet<String>();
		
		for(int i=0; i < 10000; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(rand);
			
			double x = Double.valueOf(config.get("x"));
			double y = Double.valueOf(config.get("y"));
			double z = Double.valueOf(config.get("z"));
			
			//double r = Math.sqrt( Math.pow(Double.valueOf(config.get("x")),2) + Math.pow(Double.valueOf(config.get("y")),2));
			
		
			try 
			{
				assertTrue("Expected x: " + x + "<= y:" + y + " for configuration: " + config.getFormattedParameterString() , x <= y);
				assertTrue("Expected y: " + x + "<= z:" + y + " for configuration: " + config.getFormattedParameterString() , y <= z);
				
			} catch(AssertionError e)
			{
				System.out.flush();
				System.err.flush();
				System.err.println(config.getFormattedParameterString());
				System.err.println(config.isForbiddenParameterConfiguration());
				System.out.println(config.isForbiddenParameterConfiguration());
				System.out.println(config.isForbiddenParameterConfiguration());
				throw e;
			}
			output.add(/*r + "=>" +*/ config.getFormattedParameterString());
		}
		
		for(String line : output)
		{
			System.out.println(line);
		}
		
		
		
	}
	
	@Test
	@Ignore
	public void testSpeedOfRandomWithRadius()
	{
		
		
		for( int i=0; i < 10; i++)
		{
			for (double k = 2; k > 0.15; k -= 0.1)
			{
				String pcsFile = "a real [-1,1] [0]\n b real [-1,1] [0]\n";
					
					
				if (k <= 1.95)
				{
					pcsFile += "{ a^2 + b^2>" + k + "^2}\n";
				}
				ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
				AutoStartStopWatch watch = new AutoStartStopWatch();
				for(int j=0; j < 1_000_000; j++)
				{
					configSpace.getRandomParameterConfiguration(rand);
				}
				
				watch.stop();
				
				System.out.println("Time for " + k + " is " + watch.stop() / 1000.0  + " s");
			}
			
		}
		 
	}
	
	
	@Test
	@Ignore
	public void testSpeedOfRandomWithRedundantClauses()
	{
		
		
		for( int i=0; i < 20; i++)
		{
			for (int k = 0 ; k < 10; k++)
			{
				String pcsFile = "a real [-1,1] [0]\n b real [-1,1] [0]\n";
					
				for(int j =0; j < k; j++)
				{
					pcsFile += "{ a^2 + b^2>2^2 }\n";
				}
				
				ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
				AutoStartStopWatch watch = new AutoStartStopWatch();
				for(int j=0; j < 1_000_000; j++)
				{
					configSpace.getRandomParameterConfiguration(rand);
				}
				
				watch.stop();
				
				System.out.println("Time for " + k + " is " + watch.stop() / 1000.0  + " s");
			}
			
		}
		 
	}
	
	
	@Test
	@Ignore
	public void testSpeedOfRandomWithRedundantSingleClauses()
	{
		
		
		for( int i=0; i < 20; i++)
		{
			for (int k = 0 ; k < 10; k++)
			{
				String pcsFile = "a real [-1,1] [0]\n b real [-1,1] [0]\n";
				
				if(k > 0)
				{
					pcsFile += "{ ";
					for(int j =1; j < k; j++)
					{
						pcsFile += "(a^2 + b^2>2^2) && ";
					}
				
					pcsFile += " (a^2 + b^2>2^2) }\n";
				}
				ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
				AutoStartStopWatch watch = new AutoStartStopWatch();
				for(int j=0; j < 1_000_000; j++)
				{
					configSpace.getRandomParameterConfiguration(rand);
				}
				
				watch.stop();
				
				System.out.println("Time for " + k + " is " + watch.stop() / 1000.0  + " s");
			}
			
		}
		 
	}
	
	@Test
	
	public void testThreadSafeRandomGeneration() throws InterruptedException
	{
		String pcsFile = "a [-1,1] [0]\n b [-1,1] [0]\n { a^2 + b^2 > 0.75^2 }";
		final ParameterConfigurationSpace configSpace =  ParamFileHelper.getParamFileFromString(pcsFile);
		
		
		final AtomicInteger forbiddenConfigurations = new AtomicInteger(0);
		Runnable run = new Runnable()
		{

			@Override
			public void run() {
				// TODO Auto-generated method stub
				MersenneTwisterFast mtf = new MersenneTwisterFast(System.nanoTime());
				for(int i=0; i < 2500; i++)
				{
					
					//synchronized(configSpace)
					{
						ParameterConfiguration config = configSpace.getRandomParameterConfiguration(mtf);
						if(config.isForbiddenParameterConfiguration())
						{
							System.out.println(config.getFormattedParameterString());
							forbiddenConfigurations.incrementAndGet();
							
						}
					}
					
				}
				System.out.print(".");
			}
	
		};
		
		
		ExecutorService execService = Executors.newFixedThreadPool(8);
		
		for(int i=0; i < 50; i++)
		{
			execService.submit(run);
		}
		
		
		execService.shutdown();
		execService.awaitTermination(24, TimeUnit.DAYS);
		
		assertEquals("Expected number of forbidden configurations should be zero", 0, forbiddenConfigurations.get());
	}
	
	@Test
	public void testComments() throws IOException
	{
		
		String pcsFile = "a [1, 10] [1] i\n"
					   + "b [1, 10] [1] l\n"
					   + "c [1, 10] [1] il\n"
					   + "d [1, 10] [1] \n"
					   + "e {1,2,3,4,5,6,7,8,9,10} [1]\n"
					   + "e2 {1,2,3,4,5,6,7,8,9,10} [1]\n"
					   + "f integer [1,10] [1] \n"
					   + "g integer [1 ,10] [1] log \n"
					   + "h real [1, 10] [1] \n"
					   + "i real [1 ,10] [1] log\n"
					   + "j real [1 ,10] [1]log\n"
					   + "k integer [1 ,10] [1]log\n"
					   + "l categorical {1,2,3,4,5,6,7,8,9,10} [1] \n"
					   + "m ordinal { 1,2,3, 4, 5, 6 ,7 ,8, 9, 10} [1]\n"
					   + "\n"
					   +"\n"
					   + "\n"
					   + "l | e in { 1,2 ,3 ,4}\n"
					   + "l | e2 in { 1,2 ,3 ,4}\n"
					   + "m | a > 5 && b < 5 || c == 5 && d != 10"
					   + "\n"
					   + "{a + b + c + d + e + e2 + f + g + h + i + j + k + l + m > 70}";
					  
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		for(int i=0; i < 100; i++)
		{
			BufferedReader sr = new BufferedReader(new StringReader(pcsFile));
			
			String line = null;
			StringBuilder sb = new StringBuilder();
			while((line = sr.readLine()) != null)
			{
				
				if(rand.nextInt(10) > 8)
				{
					line = line.replaceAll("\\s", "    ");
				}

				if(rand.nextInt(10) > 7)
				{
					line = line.replaceAll(",10", " ,    10 ");
				}
				
				while(rand.nextInt(10) > 6)
				{
					line = line.replaceAll(",", " , ");
				}
				
				while (rand.nextInt(10) > 7)
				{
					line = line.replaceAll("\\[", "   [");
				}
				sb.append(line);
				if(rand.nextDouble() < 0.25)
				{
					while(rand.nextInt(10) > 7){
						sb.append(" ");
					}
					sb.append("# Comment of some kind");
				}
				
				
				sb.append("\n");
			}
			
			//System.out.println("***********\n" + sb.toString() + "*********");
			
			ParameterConfigurationSpace newConfigurationSpace = ParamFileHelper.getParamFileFromString(sb.toString());
			
			assertTrue(configSpace.getParameterConfigurationFromString(newConfigurationSpace.getDefaultConfiguration().getFormattedParameterString(), ParameterStringFormat.NODB_OR_STATEFILE_SYNTAX).equals(configSpace.getDefaultConfiguration()));
						
			for(int j=0; j < 100; j++)
			{
				ParameterConfiguration newConfig = (newConfigurationSpace.getRandomParameterConfiguration(rand));
				ParameterConfiguration config = configSpace.getParameterConfigurationFromString(newConfig.getFormattedParameterString(), ParameterStringFormat.NODB_OR_STATEFILE_SYNTAX);

				assertEquals(newConfig.getFormattedParameterString(), config.getFormattedParameterString());
			}
			
		}
	}
	
	@Test
	public void testForbiddenClassic()
	{
		String pcsFile = "a real [0,100] [50]\n"
				+ "b integer [0,100] [50]\n"
				+ "c ordinal { 0, 25 , 50, 75, 100} [50]\n"
				+ "d categorical { 0, 25 , 50, 75, 100} [50]\n"
				+ "{a = 25}\n"
				+ "{b = 25}\n"
				+ "{c = 25}\n"
				+ "{d = 25}\n"
				+ "{a = 75, b = 75}\n"
				+ "{c = 75, d = 75}\n"
				+ "{a = 75, c = 75}\n"
				+ "{a = 75, d = 75}\n"
				+ "{b = 75, c = 75}\n"
				+ "{b = 75, d = 75}\n";
				
		
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);		
		
		assertEquals("Lower Bound should be 1.0", 1, configSpace.getLowerBoundOnSize(), 0.5);
		System.out.println(configSpace.getDefaultConfiguration().getFormattedParameterString());
		
		
		for(String name : configSpace.getParameterNames())
		{
			System.out.println("Starting: " + name);
			ParameterConfiguration config = configSpace.getDefaultConfiguration();
			
			
			config.put(name, "25");
			assertTrue("Configuration " + config.getFormattedParameterString() + " should be verboten", config.isForbiddenParameterConfiguration());
			
			
			
			for(String aName : configSpace.getParameterNames())
			{
				if(name.equals(aName))
				{
					continue;
				}
				
				ParameterConfiguration newConfig = configSpace.getDefaultConfiguration();
				
				newConfig.put(name, "75");
				assertFalse(newConfig.isForbiddenParameterConfiguration()); 
				newConfig.put(aName, "75");
				assertTrue("Configuration should be forbidden:" + newConfig.getFormattedParameterString() , newConfig.isForbiddenParameterConfiguration());
				System.out.println(newConfig.getFormattedParameterString());
				
				newConfig.put(aName, "25");
				assertTrue(newConfig.isForbiddenParameterConfiguration());
				
			}
		}
		
		
		for(int i=0; i < 1000000; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(rand);
			
			assertFalse("Expected configuration to be allowed", config.isForbiddenParameterConfiguration());
		}
	}
	
	@Test
	public void testForbiddenNew()
	{
		String pcsFile = "a real [0,100] [50]\n"
				+ "b integer [0,100] [50]\n"
				+ "c ordinal { 0, 25 , 50, 75, 100} [50]\n"
				+ "d categorical { 0, 25 , 50, 75, 100} [50]\n"
				+ "{a == 25}\n"
				+ "{b == 25}\n"
				+ "{c == 25}\n"
				+ "{d == 25}\n"
				+ "{a == 75 && b == 75}\n"
				+ "{c == 75 && d == 75}\n"
				+ "{a == 75 && c == 75}\n"
				+ "{a == 75 && d == 75}\n"
				+ "{b == 75 && c == 75}\n"
				+ "{b == 75 && d == 75}\n";
				
		
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);		
		
		System.out.println(configSpace.getDefaultConfiguration().getFormattedParameterString());
		
		assertEquals("Lower Bound should be 1.0", 1, configSpace.getLowerBoundOnSize(), 0.5);
		
		for(String name : configSpace.getParameterNames())
		{
			System.out.println("Starting: " + name);
			ParameterConfiguration config = configSpace.getDefaultConfiguration();
			
			
			config.put(name, "25");
			assertTrue("Configuration " + config.getFormattedParameterString() + " should be verboten", config.isForbiddenParameterConfiguration());
			
			
			
			for(String aName : configSpace.getParameterNames())
			{
				if(name.equals(aName))
				{
					continue;
				}
				
				ParameterConfiguration newConfig = configSpace.getDefaultConfiguration();
			
				
				newConfig.put(name, "75");
				assertFalse(newConfig.isForbiddenParameterConfiguration()); 
				newConfig.put(aName, "75");
				
				System.out.println(newConfig.getFormattedParameterString());
				assertTrue(newConfig.isForbiddenParameterConfiguration());
				
				
				newConfig.put(aName, "25");
				assertTrue(newConfig.isForbiddenParameterConfiguration());
				
			}
		}
		
		
		for(int i=0; i < 10000; i++)
		{
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(rand);
			
			assertFalse("Expected configuration to be allowed", config.isForbiddenParameterConfiguration());
		}
	}
	
	
	
	@Test
	public void testOrdinalValues()
	{
		String pcsFile = "priority ordinal { LOW, MEDIUM, HIGH } [MEDIUM]\n" +
				"temp ordinal { COLD, COOL, MILD, WARM, HOT} [HOT]\n" 
				+ "temp | priority == MEDIUM\n" 
				+ "{priority = LOW}";
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		System.out.println(configSpace.getDefaultConfiguration().getFormattedParameterString());
		System.out.println(configSpace.getRandomParameterConfiguration(rand).getFormattedParameterString());
		
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
	
		for(int i=0; i <1000; i++)
		{
			config = configSpace.getRandomParameterConfiguration(rand);
			
			if(config.get("priority").equals("MEDIUM"))
			{
				assertTrue(config.getActiveParameters().contains("temp"));
			} else
			{
				assertFalse(config.getActiveParameters().contains("temp"));
			}
			
			if(config.get("priority").equals("LOW"))
			{
				assertTrue(config.isForbiddenParameterConfiguration());
			}else
			{
				assertFalse(config.isForbiddenParameterConfiguration());
			}
		}
		
		
		pcsFile = "priority ordinal { LOW, MEDIUM, HIGH } [MEDIUM]\n" +
					"temp ordinal { COLD, COOL, MILD, WARM, HOT} [HOT]\n" 
					+ "temp | priority > LOW\n";
			
		configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		for(int i=0; i <1000; i++)
		{
			config = configSpace.getRandomParameterConfiguration(rand);
			
			if(config.get("priority").equals("MEDIUM") || config.get("priority").equals("HIGH") )
			{
				assertTrue(config.getActiveParameters().contains("temp"));
				
			} else
			{
				assertFalse(config.getActiveParameters().contains("temp"));
			}
			
			System.out.println(config.getFormattedParameterString());
			
		}
		
		//pcsFile = "temp ordinal { ABSOLUTE_ZERO, COLD, COOL, MILD, WARM, HOT, BOILING } [MILD]";
		pcsFile = "temp integer [1,7] [4]";
		
		configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		for(ParameterConfiguration config2 : configSpace.getDefaultConfiguration().getNeighbourhood(rand, 4))
		{
			System.out.println(config2.getFormattedParameterString());
		}
		
		
		pcsFile = "priority ordinal { LOW, MEDIUM, HIGH } [MEDIUM]\n" +
				  "temp ordinal { COLD, COOL, MILD, WARM, HOT} [HOT]\n" 
				+ "{ priority > MEDIUM && temp <= WARM }";
		
		configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		for(int i=0; i <1000; i++)
		{
			config = configSpace.getRandomParameterConfiguration(rand);
						
			
			if(config.get("priority").equals("HIGH") && !config.get("temp").equals("HOT"))
			{
				fail("This parameter setting should have been forbidden:" + config.getFormattedParameterString());
			}

			System.out.println(config.getFormattedParameterString());
			
		}

		
		pcsFile = "x ordinal { 1,4, 16, 64, 256, 1024, INFINITY} [4]\n"
				+ "y ordinal { 0, 1, 10, 100, 1000, INFINITY } [10]\n"
				+ "z categorical { 1, 4, 10 , 16, 64, INFINITY } [4] \n"
				+ "priority ordinal {0, LOW, MEDIUM, HIGH,INFINITY } [MEDIUM]\n "
				+ "boundary real [1,100] [10] log\n"
				+ "{ x >= y }\n"
				+ "{ z == INFINITY && y == INFINITY && x > 128 }\n"
				+ "{ z = 1, y = 10 , x = 4 }\n"
				+ "{ priority == LOW && yx > 40 }\n"
				+ "{ y-x == 99 }\n"
				+ "{ (x < boundary && y > boundary) }\n"
				+ "\n";
				
		
		configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		
		System.out.println(configSpace.getForbiddenOrdinalAndCategoricalValues());
		System.out.println(configSpace.getForbiddenOrdinalAndCategoricalVariableValues());
		for(int i=0; i <1000000; i++)
		{
			config = configSpace.getRandomParameterConfiguration(rand);
						
			/*
			if(config.get("priority").equals("HIGH") && !config.get("temp").equals("HOT"))
			{
				fail("This parameter setting should have been forbidden:" + config.getFormattedParameterString());
			}*/
			
			
			if(config.get("z").equals("INFINITY") && config.get("y").equals("INFINITY"))
			{
				assertTrue("Expected x to be less than or equal to 128: " + config.getFormattedParameterString(), Double.valueOf(config.get("x")) <= 128);
			}
			
			if(config.get("y").equals("100") && config.get("x").equals("1"))
			{
				fail("Expected that y-x should not be 99: " + config.getFormattedParameterString());
			}
			
			if(config.get("z").equals("1") && config.get("y").equals("10") && config.get("x").equals("4"))
			{
				fail("This parameter should be forbidden" + config.getFormattedParameterString());
			} 
			
			if(config.get("x").equals("INFINITY"))
			{
				fail("This parameter should be forbidden by {x >= y}:" + config.getFormattedParameterString());
			}
			
			double x = Double.valueOf(config.get("x"));
			
			//x is not infinity so can be cast to double
			
			double boundary = Double.valueOf(config.get("boundary"));
			
			if(!config.get("y").equals("INFINITY"))
			{
				assertTrue("Expected x to be less than y" + config.getFormattedParameterString(),x < Double.valueOf(config.get("y")));
			}
			
			if(config.get("priority").equals("LOW"))
			{
				if(config.get("y").equals("INFINITY"))
				{
					fail("This parameter setting should have been forbidden (since xy > 40)" + config.getFormattedParameterString());
				} else
				{
					double y = Double.valueOf(config.get("y"));
					
					assertTrue("Expected xy <= 40 in "+ config.getFormattedParameterString(), x*y <= 40);
				}
			}
			//System.out.println(config.getFormattedParameterString());
			
			if(x > boundary)
			{
				try
				{
					double d = Double.valueOf(config.get("y"));
					
					if(d < boundary)
					{
						fail("Configuration forbidden (x > boundary && y < boundary) " + config.getFormattedParameterString());
					}
				} catch(RuntimeException e)
				{
					
					//It's okay 
				}
			} else if (x < boundary)
			{
				
				try {
					double d = Double.valueOf(config.get("y"));
					
					if(d > boundary)
					{
						fail("Configuration forbidden (x < boundary && y > boundary) " + config.getFormattedParameterString());
					}
					
				} catch(RuntimeException e)
				{
					
						fail("Configuration forbidden (x < boundary && y > boundary) " + config.getFormattedParameterString());
					
				}
			}
				
				
			
			
			
		}

		
				
	}
	
	@Test
	/**
	 * This just tests that all the examples in the PCS file are valid
	 */
	public void testPCSFileExample()
	{
		
		
		String[] pcsExamples = {"@1:loops categorical { common, distinct, shared, no} [no]\n",
								"DS categorical {TinyDataStructure, FastDataStructure}[TinyDataStructure]\n",
								"random-variable-frequency categorical {0, 0.05, 0.1, 0.2} [0.05]",
								"annealing-temperature ordinal {cold, cool, medium, warm, hot} [medium]",
								"alpha-heuristic ordinal { 1, 10, 100, 1000, INFINITE } [1]",
								"sp-rand-var-dec-scaling real [0.3, 1.1] [1]",
								"mult-factor integer [2, 15] [5]",
								"DLSc real [0.00001, 0.1] [0.01] log",
								"first-restart integer [10, 1000] [100] log",
								"heur1 categorical { off, on } [on]\n"
								+		"heur2 categorical { off, on } [on]\n"
								+		"heur_order categorical { heur1then2, heur2then1 } [heur1then2]\n"
								+		"heur_order | heur1 == on && heur2 == on",
								"temperature real [-273.15, 100] [10]\n"
								+"rain real [0, 200] [0]\n"
								+"gloves ordinal { none, yarn, leather, gortex } [none]\n"
								+"gloves | rain > 0 || temperature < 5"
		};
		
		
		
		for(String pcsFile : pcsExamples)
		{
		
			ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
			
			System.out.println(configSpace.getDefaultConfiguration().getFormattedParameterString());
		}
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("first-restart integer [10, 1000] [100] log");
		
		int matches = 0;
		
		for(int i=0; i < 1000;i++)
		{
			if(Integer.valueOf(configSpace.getRandomParameterConfiguration(rand).get("first-restart")) > 100)
			{
				matches++;
			}
		}
		
		System.out.println(matches);
		
	}
	
	
	@Test(expected=ParameterConfigurationLockedException.class)
	public void testLock()
	{
		String pcsFile = "test categorical { a, b, d } [b]\n";
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		ParameterConfiguration test = configSpace.getRandomParameterConfiguration(rand);
		
		try
		{
			test.put("test", "b");
		} catch(Exception e)
		{
			fail("Should have been successful");
		}
		
		test.lock();
		
		
		try
		{
			test.put("test", "b");
		} catch(Exception e)
		{
			fail("Should have been successful");
		}
		
		
		
		test.put("test", "a");
		 
		
		
		
		
			
	}
	
	@Test
	public void testConcurrentModificationException()
	{
		String pcsFile = "a { on, off } [on]\nb { on, off } [on]\n a | b in { on} ";
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		
		ParameterConfiguration defaultConfig = configSpace.getDefaultConfiguration();
		
		
		for(String s : defaultConfig.getActiveParameters())
		{
			System.out.println(s);
		}
		
		
		try 
		{
			for(String s : defaultConfig.getActiveParameters())
			{
				defaultConfig.put("a", "off");
				System.out.println(s);
			}
			
			fail();
		} catch(ConcurrentModificationException e)
		{
			//Good
		}
		

		for(String s : defaultConfig.values())
		{
			System.out.println(s);
		}
		
		
		try 
		{
			for(String s : defaultConfig.values())
			{
				defaultConfig.put("a", "off");
				System.out.println(s);
			}
			
			fail("Expected exception");
		} catch(ConcurrentModificationException e)
		{
			//Good
		}

		

		for(Entry<String,String> s : defaultConfig.entrySet())
		{
			System.out.println(s);
		}
		
		
		try 
		{
			for(Entry<String,String> s : defaultConfig.entrySet())
			{
				defaultConfig.put("a", "off");
				System.out.println(s);	
			}
			
			fail();
		} catch(ConcurrentModificationException e)
		{
			//Good
		}

		
		
	}
	
	@Test
	/**
	 * Bug #2101
	 */
	public void testDefaultInactiveValueParsingFromString()
	{
		String pcs = "@1:max-solver [1,100][3]il\nTest {a,b} [a]\n @1:max-solver | Test == b";
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcs);
		
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		
		assertEquals("3",config.get("@1:max-solver"));
		assertEquals("a", config.get("Test"));
		
		
		
		System.out.println(config.getFormattedParameterString());
		config = configSpace.getParameterConfigurationFromString("-Test 'a' ",ParameterStringFormat.NODB_SYNTAX);

		assertEquals("3",config.get("@1:max-solver"));
		assertEquals("a", config.get("Test"));

		
		config = configSpace.getParameterConfigurationFromString("-Test 'a' -@1:max-solver '6'",ParameterStringFormat.NODB_SYNTAX);
		
		assertEquals("6",config.get("@1:max-solver"));
		assertEquals("a", config.get("Test"));
		
		
		pcs = "@1:max-solver {1,2,3,4,5,6,7,8,9,100}[3]\nTest {a,b} [a]\n @1:max-solver | Test == b";
		
		configSpace = ParamFileHelper.getParamFileFromString(pcs);
		
		config = configSpace.getDefaultConfiguration();
		
		assertEquals("3",config.get("@1:max-solver"));
		assertEquals("a", config.get("Test"));
		
		System.out.println(config.getFormattedParameterString());
		config = configSpace.getParameterConfigurationFromString("-Test 'a' ",ParameterStringFormat.NODB_SYNTAX);
		
		assertEquals("3",config.get("@1:max-solver"));
		assertEquals("a", config.get("Test"));
		
		
		config = configSpace.getParameterConfigurationFromString("-Test 'a' -@1:max-solver '6'",ParameterStringFormat.NODB_SYNTAX);
		
		assertEquals("6",config.get("@1:max-solver"));
		assertEquals("a", config.get("Test"));
		
		
	}
	
	/**
	 * Test for Bug #2104
	 */
	@Test
	public void testConfigurationLimits()
	{
		String pcsFile = "a integer [1,5][1]\n"
				+ "b integer [1,5][1]\n"
				+ "c integer [1,5][1]\n";
		
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString(pcsFile);
		
		Set<ParameterConfiguration> generatedConfigurations = new HashSet<>();
		
		
		Set<String> configs = new TreeSet<>();
		
		System.out.println(configSpace.getLowerBoundOnSize());
		
		while(generatedConfigurations.size() < configSpace.getLowerBoundOnSize())
		{
			System.out.println(generatedConfigurations.size()  + "," + configSpace.getLowerBoundOnSize()); 
			ParameterConfiguration config = configSpace.getRandomParameterConfiguration(rand);
			generatedConfigurations.add(config);
			configs.add(config.getFormattedParameterString());
		}
		
		assertEquals(configs.size(), generatedConfigurations.size());
		
		
		System.out.println("Done");
		
		
		generatedConfigurations = new HashSet<>();
		
		
		configs = new TreeSet<>();
		
		
		while(generatedConfigurations.size() < configSpace.getLowerBoundOnSize())
		{
			ParameterConfiguration newConfig = configSpace.getRandomParameterConfiguration(rand);
			
			System.out.println(".");
			
			for(ParameterConfiguration config : newConfig.getNeighbourhood(rand, 4))
			{
				generatedConfigurations.add(config);
				configs.add(config.getFormattedParameterString());
			}
		}
		for(ParameterConfiguration config : generatedConfigurations)
		{
			System.out.println("Parameters: " + config.getFormattedParameterString() + " => " + Arrays.toString(config.toValueArray()));
		}
		assertEquals(configs.size(), generatedConfigurations.size());
		
		
		System.out.println("Done");
		
	}
	
	@After
	public void tearDown()
	{
		System.out.println("Done");
	}
	
	
}
