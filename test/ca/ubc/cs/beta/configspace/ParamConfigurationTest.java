package ca.ubc.cs.beta.configspace;



import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration.StringFormat;

import com.beust.jcommander.internal.Lists;


public class ParamConfigurationTest {

	@Before
	public void setUp()
	{
		
	}
	
	
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
	@Test(expected=IllegalArgumentException.class)
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
	@Test(expected=IllegalArgumentException.class)
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
		

	@After
	public void tearDown()
	{
		System.out.println("Done");
	}
}
