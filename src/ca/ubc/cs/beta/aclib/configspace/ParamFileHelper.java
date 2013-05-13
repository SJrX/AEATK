package ca.ubc.cs.beta.aclib.configspace;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains Factory Methods for getting ParamConfigurationSpaces
 * 
 * 
 */
public class ParamFileHelper {

	/**
	 * Returns a ParamConfigurationSpace via the filename and seeded with seed
	 * 
	 *  This method will return the same instance for subsequent file names, and can only been seeded once
	 * @param filename					string for the filename
	 * @param seedForRandomSampling		seed for prng
	 * @return ParamConfigurationSpace instance
	 * 
	 */
	public static ParamConfigurationSpace getParamFileParser(String filename)
	{	if(filename.equals(ParamConfigurationSpace.SINGLETON_ABSOLUTE_NAME))
		{
			return ParamConfigurationSpace.getSingletonConfigurationSpace();
		} else
		{
			return getParamFileParser(new File(filename));
		}
	}
	

	private static ConcurrentHashMap<String, ParamConfigurationSpace> paramFiles = new ConcurrentHashMap<String, ParamConfigurationSpace>();
	
	/**
	 * Returns a ParamConfigurationSpace via the filename and seeded with seed
	 * 
	 *  This method will return the same instance for subsequent file names, and can only been seeded once
	 * @param file  					file with the param arguments
	 * @param seedForRandomSampling		seed for prng
	 * @return ParamConfigurationSpace instance
	 */
	public static ParamConfigurationSpace getParamFileParser(File file)
	{
		//TODO Fix Thread Safety of this code (I'm not sure if the double-check locking idiom works here)
		ParamConfigurationSpace param = paramFiles.get(file.getAbsolutePath());
		
		if(param == null)
		{ 
				param = new ParamConfigurationSpace(file);
								
				 ParamConfigurationSpace p = paramFiles.putIfAbsent(file.getAbsolutePath(),param);
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

	/**
	 * Clears the cache so that param files can be re-seeded
	 */
	public static void clear() {
		paramFiles.clear();
		
	}
	
	
}
