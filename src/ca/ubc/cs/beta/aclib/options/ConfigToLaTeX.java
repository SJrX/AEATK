package ca.ubc.cs.beta.aclib.options;

import java.lang.reflect.Field;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

public class ConfigToLaTeX {

	public static void main(String[] args) throws Exception
	{
		
		
		SMACOptions config = new SMACOptions();
		
		printParameters(config);
	}

	
	public static void printParameters(Object o) throws IllegalArgumentException, IllegalAccessException
	{
		
	
		
		for(Field f : o.getClass().getFields())
		{
			
		
			
			if(f.isAnnotationPresent(Parameter.class))
			{
				Parameter param  = (Parameter)f.getAnnotation(Parameter.class);
				//System.out.println(param.description());
			
				String paramName = param.names()[0];
				
				if(param.hidden()) continue;
				for(int i=0; i < param.names().length; i++)
				{
					if(param.names()[i].startsWith("--"))
					{
						paramName = param.names()[i];
						break;
					}
				}
				
				
				String paramDescription = param.description();
				boolean paramRequired = param.required();
				String paramDefault;
				try {
					 paramDefault = f.get(o).toString();
				} catch(NullPointerException e)
				{
					paramDefault = "";
				}
				
				String paramLegalValues = "";
				
				StringBuilder row = new StringBuilder();
				row.append("\\begin_layout Description\n");
				
				row.append(paramName);
				if(paramName.equals("--runID"))
				{
					paramDefault = "Run-YYYY-MM-DD--HH-mm-ss-xxx (where xxx is milli-seconds)";
				}
				row.append(" ");
				row.append(paramDescription);
				/**
				 *

 Default: RUNTIME
\begin_inset Newline newline
\end_inset

 Values: RUNTIME, RUNLENGTH, APPROX, SPEEDUP, QUALITY
				 */
				row.append("\n\\begin_inset Newline newline\n\\end_inset");
				
				paramDefault = paramDefault.replaceFirst(System.getProperty("user.dir"), "<current working directory>");
				
				if(paramRequired)
				{
					row.append("\n Required ");
				}
				if(!paramDefault.trim().equals(""))
				{
					row.append("\n Default: " + paramDefault);
				} 
					
					
				
				if(f.get(o) instanceof String)
				{
					
				} else if(f.get(o) instanceof Enum)
				{
					row.append("\n\\begin_inset Newline newline\n\\end_inset");
					row.append("\n Values: ");
					Enum<?> e = (Enum<?>) f.get(o);
					Enum<?>[] constants = e.getClass().getEnumConstants();
					for(int i=0; i < constants.length; i++)
					{
						if(i != 0) row.append(", ");
						Enum<?> constant = constants[i];
						row.append(constant.name());
						
					}
					
					
					
				}
				
				
				
				row.append("\n\\end_layout\n");
						
						//paramName + " " + paramDescription;
				
			
			
				/*
				String row = "<row>\n"+
				"<cell alignment=\"center\" valignment=\"top\" topline=\"true\" leftline=\"true\" usebox=\"none\">\n"+
				"\\begin_inset Text\n" + 

				"\\begin_layout Plain Layout\n"+

				"\\series bold\n"+
				paramName+
				"\n\\end_layout\n"+

				"\\end_inset\n"+
				"</cell>\n"+
				"<cell alignment=\"center\" valignment=\"top\" topline=\"true\" leftline=\"true\" usebox=\"none\">\n"+
				"\\begin_inset Text\n"+

				"\\begin_layout Plain Layout\n"+
				paramDescription+
				"\n\\end_layout\n"+

				"\\end_inset\n"+
				"</cell>\n"+
				"<cell alignment=\"center\" valignment=\"top\" topline=\"true\" leftline=\"true\" usebox=\"none\">\n"+
				"\\begin_inset Text\n"+

				"\\begin_layout Plain Layout\n"+
				(paramRequired ? "YES" : "NO") +
				"\n\\end_layout\n"+

				"\\end_inset\n"+
				"</cell>\n"+
				"<cell alignment=\"center\" valignment=\"top\" topline=\"true\" leftline=\"true\" usebox=\"none\">\n"+
				"\\begin_inset Text\n"+

				"\\begin_layout Plain Layout\n"+
				"\\begin_inset Quotes eld\n"+
				"\\end_inset\n"+

				paramDefault+
				"\n\\begin_inset Quotes erd\n"+
				"\\end_inset\n"+


				"\\end_layout\n"+

				"\\end_inset\n"+
				"</cell>\n"+
				"<cell alignment=\"center\" valignment=\"top\" topline=\"true\" leftline=\"true\" rightline=\"true\" usebox=\"none\">\n"+
				"\\begin_inset Text\n"+

				"\\begin_layout Plain Layout\n"+
				paramLegalValues+
				"\n\\end_layout\n"+

				"\\end_inset\n"+
				"</cell>\n"+
				"</row>\n";
				*/
				System.out.println(row);
				
			}
			
			if(f.isAnnotationPresent(ParametersDelegate.class))
			{
				printParameters(f.get(o));
			}
		}
	}
		
		
}
