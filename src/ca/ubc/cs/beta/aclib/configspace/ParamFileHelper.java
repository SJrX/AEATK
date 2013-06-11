package ca.ubc.cs.beta.aclib.configspace;

import java.io.File;
import java.io.StringReader;

/**
 * Contains Factory Methods for getting ParamConfigurationSpaces
 * 
 * 
 */
public final class ParamFileHelper {

	/**
	 * Returns a ParamConfigurationSpace via the filename and seeded with seed
	 * @param 	filename					string for the filename
	 * @return 	ParamConfigurationSpace  the configuration space
	 * 
	 */
	public static ParamConfigurationSpace getParamFileParser(String filename)
	{
		return getParamFileParser(filename, ParamConfigurationSpace.DEFAULT_NEIGHBOURS_FOR_CONTINUOUS_PARAMETERS);
	}
	
	/**
	 * Returns a ParamConfigurationSpace via the filename and seeded with seed
	 * 
	 * @param 	filename				 string for the filename
	 * @param 	neighbours				 the number of neighbours numerical parameters should have
	 * @return	ParamConfigurationSpace  the configuration space
	 * 
	 */
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
	 * @param neighbours				the number of neighbours numerical parameters should have
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
