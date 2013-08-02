package ca.ubc.cs.beta.hal.startup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import ca.ubc.cs.beta.aclib.misc.options.UsageSection;
import ca.ubc.cs.beta.aclib.misc.returnvalues.ACLibReturnValues;
import ca.ubc.cs.beta.aclib.options.docgen.OptionsToUsage;
import ca.ubc.cs.beta.aclib.options.docgen.UsageSectionGenerator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class JsonWriter {

	private static File json = new File("./hal.json");
	private static String fileContent;
	
	public static void main(String[] args)
	{
		JsonOptions mainOptions = new JsonOptions();
		JCommander com = new JCommander(mainOptions, true, true);
		
		try {	
			//parse the JCommander options
			com.parse(args);
			
			//If no database argument was provided
			//and no arguments were provided at all and a JSON file exists, run with the current settings
			//the database argument is required to write or overwrite the JSON file
			if(mainOptions.database==null)
			{
				if(!json.exists())
					throw new ParameterException("No JSON file found. Must supply argument --db to generate JSON file");	
				else if(args.length!=0)
					throw new ParameterException("Must supply argument --db in order to overwrite JSON file.");
			}
			//Prepare and write the JSON file
			else
			{
				fileContent = "{\n  \"database\": \""+mainOptions.getJDBCString()+"\",\n  \"serverport\": "+mainOptions.serverport+",\n  \"reloadImportsOnStartup\": "+mainOptions.reload+"\n}";
				
				if(!json.exists())
				{
					json.createNewFile();
				}
				
				FileWriter fwrite = new FileWriter(json.getAbsoluteFile());
				BufferedWriter bwrite = new BufferedWriter(fwrite);
				bwrite.write(fileContent);
				bwrite.close();
			}
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(ACLibReturnValues.OTHER_EXCEPTION);
		}
		catch(IllegalStateException e){
			e.printStackTrace();
			System.exit(ACLibReturnValues.OTHER_EXCEPTION);
		}
		catch(ParameterException e){	
			//Converts the actual option objects into objects "UsageSection"s that are easy to manipulate
			
			List<UsageSection> sections = UsageSectionGenerator.getUsageSections(mainOptions);
			
			boolean showHiddenParameters = false;
			
			//A much nicer usage screen than JCommander's 
			//OptionsToUsage.usage(sections, showHiddenParameters);
			e.printStackTrace();
			System.exit(ACLibReturnValues.PARAMETER_EXCEPTION);
		}
		System.exit(ACLibReturnValues.SUCCESS);
	}
}
