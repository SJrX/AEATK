package ca.ubc.cs.beta.configspace;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import ec.util.MersenneTwister;

public class ParamFileHelper {

	
	
	
	public static ParamConfigurationSpace getParamFileParser(String filename, long seedForRandomSampling)
	{
		return getParamFileParser(new File(filename),seedForRandomSampling);
	}
	

	private static ConcurrentHashMap<String, ParamConfigurationSpace> paramFiles = new ConcurrentHashMap<String, ParamConfigurationSpace>();
	
	public static ParamConfigurationSpace getParamFileParser(File f, long seedForRandomSampling)
	{
		//TODO Fix Thread Safety of this code (I'm not sure if the double-check locking idiom works here)
		ParamConfigurationSpace param = paramFiles.get(f.getAbsolutePath());
		
		if(param == null)
		{ 
				param = new ParamConfigurationSpace(f, new MersenneTwister(seedForRandomSampling));
								
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
