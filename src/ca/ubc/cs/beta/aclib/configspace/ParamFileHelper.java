package ca.ubc.cs.beta.aclib.configspace;

import java.io.File;
import java.io.StringReader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains Factory Methods for getting ParamConfigurationSpaces
 * 
 * 
 */
public final class ParamFileHelper {

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
	{
		return getParamFileParser(filename, ParamConfigurationSpace.DEFAULT_NEIGHBOURS_FOR_CONTINUOUS_PARAMETERS);
	}
	
	public static ParamConfigurationSpace getParamFileParser(String filename, int neighbours)
	{	if(filename.equals(ParamConfigurationSpace.SINGLETON_ABSOLUTE_NAME))
		{
			return ParamConfigurationSpace.getSingletonConfigurationSpace();
		} else
		{
			return getParamFileParser(new File(filename), neighbours);
		}
	}

	/**
	 * Returns a ParamConfigurationSpace via the filename and seeded with seed
	 * 
	 * @param file  					file with the param arguments
	 * @param seedForRandomSampling		seed for prng
	 * @return ParamConfigurationSpace instance
	 */
	public static ParamConfigurationSpace getParamFileParser(File file)
	{
		return new ParamConfigurationSpace(file);
	}

	/**
	 * Returns a ParamConfigurationSpace via the filename and seeded with seed
	 * 
	 * @param file  					file with the param arguments
	 * @param seedForRandomSampling		seed for prng
	 * @return ParamConfigurationSpace instance
	 */
	public static ParamConfigurationSpace getParamFileParser(File file, int neighbours)
	{
		return new ParamConfigurationSpace(file, neighbours);
	}
	

	public static ParamConfigurationSpace getParamFileFromString(String string) {
		return new ParamConfigurationSpace(new StringReader(string));
	}
	
	//Non-initializable
	private ParamFileHelper()
	{
		
	}
}
