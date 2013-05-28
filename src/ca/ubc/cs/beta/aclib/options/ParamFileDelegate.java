package ca.ubc.cs.beta.aclib.options;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.configspace.ParamFileHelper;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.ReadableFileConverter;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * Delegate for ParamConfigurationSpace objects
 * 
 */
@UsageTextField(hiddenSection = true)
public class ParamFileDelegate extends AbstractOptions{
	
	@Parameter(names={"-p", "--paramFile","--paramfile"}, description="File containing algorithm parameter space information (see Algorithm Parameter File in the Manual)", required=true)
	public String paramFile;

	@Parameter(names="--searchSubspace", description="Only generate random and neighbouring configurations with these values. Specified in a \"name=value,name=value,...\" format (Overrides those set in file)", required=false)
	public String searchSubspace;
	
	@Parameter(names="--searchSubspaceFile", description="Only generate random and neighbouring configurations with these values. Specified each parameter on each own line with individual value", required=false, converter=ReadableFileConverter.class)
	public File searchSubspaceFile;
	
	public Map<String,String> getSubspaceMap()
	{
		Map<String, String> map = new HashMap<String, String>();
		if(searchSubspaceFile != null)
		{
			BufferedReader reader = null;
			try {
				try {
					reader = new BufferedReader(new FileReader(searchSubspaceFile));
					
					String line;
					while((line = reader.readLine()) != null)
					{
						if(line.trim().length() == 0) continue;
						
						String[] args = line.split("=");
						
						if(args.length != 2)
						{
							throw new IllegalArgumentException("Invalid line specified in subspace file (--searchSubspaceFile), expected to have a name=value format: " + line);
						}
						
						String name = args[0].trim();
						String value = args[1].trim();
						
						map.put(name, value);
					}
					
					
					
				} catch (IOException e) {
					throw new IllegalStateException("Couldn't open search subspace file, even though it validated",e);
				}
			} finally
			{
				if(reader != null)
					try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			
		
		
		}
		
		
		if(searchSubspace != null)
		{
			String mysubSpace = searchSubspace.trim();
			String[] spaces = mysubSpace.split(",");
			
			for(String space : spaces)
			{
				if(space.trim().length() == 0) continue;
				String[] args = space.split("=");
				if(args.length != 2)
				{
					throw new IllegalArgumentException("Invalid parameter space declaration (--searchSubspace), something around here was the problem: " + space);
				}
				
				String name = args[0].trim();
				String value = args[1].trim();
				
				map.put(name,value);
			}
			
		}
		return map;
		
	}
	
	/**
	 * Creates a ParamConfigurationSpace based on the setting of the options in this object.
	 * @return ParamConfigurationSpace object
	 */
	public ParamConfigurationSpace getParamConfigurationSpace()
	{
		return getParamConfigurationSpace(Collections.EMPTY_LIST);
	}
	
	/**
	 * Creates a ParamConfigurationSpace object based on the setting of the options in this object, searching for the file in the directories given in the list.
	 * 
	 * @param searchDirectories  directories to search for path (you do not need to include the current one)
	 * @return ParamConfigurationSpace object
	 */
	public ParamConfigurationSpace getParamConfigurationSpace(List<String> searchDirectories)
	{
		Logger log = LoggerFactory.getLogger(this.getClass());
		List<String> searchPaths = new ArrayList<String>(searchDirectories);
		
		//==Th
		searchPaths.add("");
		
		
		ParamConfigurationSpace configSpace = null;
		
		
		
		for(String path : searchPaths)
		{
			try {
				
				if(path.trim().length() > 0)
				{
					path = path + File.separator;
				} 
				
				path += this.paramFile;
				
		
				configSpace = ParamFileHelper.getParamFileParser(path);
				log.debug("Configuration space found in " + path);
			} catch(IllegalStateException e)
			{ 
				if(e.getCause() instanceof FileNotFoundException)
				{
					//We don't care about this because we will just toss an exception if we don't find it
				} else
				{
					log.warn("Error occured while trying to parse {} is {}", path ,  e.getMessage() );
				}
				

			}
		}
			
		
		if(configSpace == null)
		{
			throw new ParameterException("Could not find a valid parameter file named " + this.paramFile  +  "  in any of the following locations: (" + searchPaths.toString()+ ") please check the file exists or for a previous error");
		} else
		{
			return configSpace;
		}
		
	}

}
