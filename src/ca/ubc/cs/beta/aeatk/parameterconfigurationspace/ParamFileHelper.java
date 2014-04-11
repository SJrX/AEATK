package ca.ubc.cs.beta.aeatk.parameterconfigurationspace;

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
	 * 
	 * @param 	filename				 string for the filename
	 * @return	ParamConfigurationSpace  the configuration space
	 * 
	 */
	public static ParameterConfigurationSpace getParamFileParser(String filename)
	{	if(filename.equals(ParameterConfigurationSpace.SINGLETON_ABSOLUTE_NAME))
		{
			return ParameterConfigurationSpace.getSingletonConfigurationSpace();
		} else if(filename.equals(ParameterConfigurationSpace.NULL_ABSOLUTE_NAME))
		{
			return ParameterConfigurationSpace.getNullConfigurationSpace();
		} else
		{
			return getParamFileParser(new File(filename));
		}
	}

	/**
	 * Returns a ParamConfigurationSpace via the filename and seeded with seed
	 * 
	 * @param file  					file with the param arguments
	 * @return ParamConfigurationSpace instance
	 */
	public static ParameterConfigurationSpace getParamFileParser(File file)
	{
		return new ParameterConfigurationSpace(file);
	}

	public static ParameterConfigurationSpace getParamFileFromString(String string) {
		return new ParameterConfigurationSpace(new StringReader(string));
	}
	
	//Non-initializable
	private ParamFileHelper()
	{
		
	}
}
