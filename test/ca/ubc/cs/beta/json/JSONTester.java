package ca.ubc.cs.beta.json;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.configspace.ParamFileHelper;
import ca.ubc.cs.beta.aclib.json.JSONConverter;

public class JSONTester {

	
	@Test
	public void testJSONParamConfiguration()
	{
		ParamConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a { 0,1,2,3,4,5} [0]\n"
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
		
		
		JSONConverter<ParamConfigurationSpace> json = new JSONConverter<ParamConfigurationSpace>() {} ;
		
		String jsonText = json.getJSON(configSpace);
		System.out.println(jsonText);
		
		Object o = json.getObject(jsonText);
		
		assertEquals("Expected Representations to be equal",o , configSpace);
	}
	
	
	@Test
	public void testJSONParamConfigurationList()
	{
		ParamConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a { 0,1,2,3,4,5} [0]\n"
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
		
		
		
		ParamConfigurationSpace configSpace2 = ParamFileHelper.getParamFileFromString("a { 0,1 } [0]");
		
		List<ParamConfigurationSpace> pcs = new ArrayList<>();
		
		
		pcs.add(configSpace);
		pcs.add(configSpace2);
		JSONConverter<List<ParamConfigurationSpace>> json = new JSONConverter<List<ParamConfigurationSpace>>() {} ;
		
		String jsonText = json.getJSON(pcs);
		System.out.println(jsonText);
		
		Object o = json.getObject(jsonText);
		
		assertEquals("Expected Representations to be equal",o , pcs);
	}

	@Test
	@Ignore
	public void testJSONParamConfigurationMap()
	{
		ParamConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a { 0,1,2,3,4,5} [0]\n"
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
		
		
		
		ParamConfigurationSpace configSpace2 = ParamFileHelper.getParamFileFromString("a { 0,1 } [0]");
		
		Map<ParamConfigurationSpace,Integer> pcs = new HashMap<>();
		
		
		pcs.put(configSpace,1);
		pcs.put(configSpace2,2);
		JSONConverter<Map<ParamConfigurationSpace,Integer>> json = new JSONConverter<Map<ParamConfigurationSpace,Integer>>() {} ;
		
		String jsonText = json.getJSON(pcs);
		System.out.println(jsonText);
		
		Object o = json.getObject(jsonText,new TypeReference<Map<ParamConfigurationSpace,Integer>>() { });
		
		assertEquals("Expected Representations to be equal",o , pcs);
	}
	
	
}
