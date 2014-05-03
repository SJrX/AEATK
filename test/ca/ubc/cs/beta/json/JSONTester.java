package ca.ubc.cs.beta.json;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.json.JSONConverter;
import ca.ubc.cs.beta.aeatk.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParamFileHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;

public class JSONTester {

	
	private static SeedableRandomPool pool = new SeedableRandomPool(System.currentTimeMillis());
	
	
	
	@AfterClass
	public static void afterClass()
	{
		pool.logUsage();
	}
	@Test
	public void testJSONParamConfiguration()
	{
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a { 0,1,2,3,4,5} [0]\n"
				+ " b [0,10] [0.5]\n"
				+ "  c { on, off} [on] \n"
				+ " d { yay, nay} [yay] \n"
				+ " e [0,100] [5]i\n"
				+ " f [1,10] [2]l\n"
				+ " g [1,10] [2]il\n"
				+ " h { one, two, three } [one] \n"
				+ " c | a in { 0, 1,2} \n"
				+ " d | c in { on} \n "
				+ "d | a in { 1,2}\n "
				+ "{a=5, h=two}");
		
		
		JSONConverter<ParameterConfigurationSpace> json = new JSONConverter<ParameterConfigurationSpace>() {} ;
		
		String jsonText = json.getJSON(configSpace);
		System.out.println(jsonText);
		
		Object o = json.getObject(jsonText);
		
		assertEquals("Expected Representations to be equal",o , configSpace);
	}
	
	
	@Test
	public void testJSONParamConfigurationList()
	{
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a { 0,1,2,3,4,5} [0]\n"
				+ " b [0,10] [0.5]\n"
				+ "  c { on, off} [on] \n"
				+ " d { yay, nay} [yay] \n"
				+ " e [0,100] [5]i\n"
				+ " f [1,10] [2]l\n"
				+ " g [1,10] [2]il\n"
				+ " h { one, two, three } [one] \n"
				+ " c | a in { 0, 1,2} \n"
				+ " d | c in { on} \n "
				+ "d | a in { 1,2}\n "
				+ "{a=5, h=two}");
		
		
		
		ParameterConfigurationSpace configSpace2 = ParamFileHelper.getParamFileFromString("a { 0,1 } [0]");
		
		List<ParameterConfigurationSpace> pcs = new ArrayList<>();
		
		
		pcs.add(configSpace);
		pcs.add(configSpace2);
		JSONConverter<List<ParameterConfigurationSpace>> json = new JSONConverter<List<ParameterConfigurationSpace>>() {} ;
		
		String jsonText = json.getJSON(pcs);
		System.out.println(jsonText);
		
		Object o = json.getObject(jsonText);
		
		assertEquals("Expected Representations to be equal",o , pcs);
	}
	
	@Test
	public void testJSONAlgorithmExecutionConfiguration()
	{
		Random rand = pool.getRandom(DebugUtil.getCurrentMethodName());
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a { 0,1,2,3,4,5} [0]\n"
				+ " b [0,10] [0.5]\n"
				+ "  c { on, off} [on] \n"
				+ " d { yay, nay} [yay] \n"
				+ " e [0,100] [5]i\n"
				+ " f [1,10] [2]l\n"
				+ " g [1,10] [2]il\n"
				+ " h { one, two, three } [one] \n"
				+ " c | a in { 0, 1,2} \n"
				+ " d | c in { on} \n "
				+ "d | a in { 1,2}\n "
				+ "{a=5, h=two}");
		
		
		AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("Some string", "Some directory", configSpace, rand.nextBoolean(), Math.abs(rand.nextDouble())*10000, Collections.singletonMap("Test", "Value"));
		
		JSONConverter<AlgorithmExecutionConfiguration> json = new JSONConverter<AlgorithmExecutionConfiguration>() {} ;
		
		String jsonText = json.getJSON(execConfig);
		System.out.println(jsonText);
		
		Object o = json.getObject(jsonText);
		
		assertEquals("Expected Representations to be equal",o , execConfig);
		
	}
	
	
	
	
	@Test
	@Ignore
	public void testJSONParamConfigurationMap()
	{
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a { 0,1,2,3,4,5} [0]\n"
				+ " b [0,10] [0.5]\n"
				+ "  c { on, off} [on] \n"
				+ " d { yay, nay} [yay] \n"
				+ " e [0,100] [5]i\n"
				+ " f [1,10] [2]l\n"
				+ " g [1,10] [2]il\n"
				+ " h { one, two, three } [one] \n"
				+ " c | a in { 0, 1,2} \n"
				+ " d | c in { on} \n "
				+ "d | a in { 1,2}\n "
				+ "{a=5, h=two}");
		
		
		
		ParameterConfigurationSpace configSpace2 = ParamFileHelper.getParamFileFromString("a { 0,1 } [0]");
		
		Map<ParameterConfigurationSpace,Integer> pcs = new HashMap<>();
		
		
		pcs.put(configSpace,1);
		pcs.put(configSpace2,2);
		JSONConverter<Map<ParameterConfigurationSpace,Integer>> json = new JSONConverter<Map<ParameterConfigurationSpace,Integer>>() {} ;
		
		String jsonText = json.getJSON(pcs);
		System.out.println(jsonText);
		
		Object o = json.getObject(jsonText,new TypeReference<Map<ParameterConfigurationSpace,Integer>>() { });
		
		assertEquals("Expected Representations to be equal",o , pcs);
	}
	
	
}
