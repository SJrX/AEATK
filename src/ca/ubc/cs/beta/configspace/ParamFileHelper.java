package ca.ubc.cs.beta.configspace;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;


import ec.util.MersenneTwister;

public class ParamFileHelper {

	
	
	
	public static ParamConfigurationSpace getParamFileParser(String filename)
	{
		return getParamFileParser(new File(filename));
	}
	

	private static ConcurrentHashMap<String, ParamConfigurationSpace> paramFiles = new ConcurrentHashMap<String, ParamConfigurationSpace>();
	
	public static ParamConfigurationSpace getParamFileParser(File f)
	{
		//TODO Fix Thread Safety of this code (I'm not sure if the double-check locking idiom works here)
		ParamConfigurationSpace param = paramFiles.get(f.getAbsolutePath());
		
		if(param == null)
		{ 
			
				System.err.println("Param Files Have Hard Coded Seed");
				param = new ParamConfigurationSpace(f, new MersenneTwister(456));
								
				 ParamConfigurationSpace p = paramFiles.putIfAbsent(f.getAbsolutePath(),param);
				 if(p == null)
				 {
					 return param; 
				 } else
				 {
					 return p;
				 }
		} else
		{
			return param;
		}
		
	}

	public static void clear() {
		paramFiles.clear();
		
	}
	
	
}
