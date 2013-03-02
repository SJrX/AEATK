package ca.ubc.cs.beta.aclib.options;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ca.ubc.cs.beta.aclib.misc.jcommander.validator.ReadableFileConverter;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;

import com.beust.jcommander.Parameter;

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

}
