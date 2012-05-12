package ca.ubc.cs.beta.ac.config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;



public abstract class AbstractConfigToString {


	public List<String> configToString()
	{
		
		StringBuilder sb = new StringBuilder();
		ArrayList<String> list = new ArrayList<String>();
		for(Field f : this.getClass().getFields())
		{
			if(!f.isAnnotationPresent(ParametersDelegate.class)) continue;
			
			
			
			ParametersDelegate ant = f.getAnnotation(ParametersDelegate.class);
			
			if(ant != null)
			{
				try {
					
					
				
					
					Object o = f.get(this);
					if(o == null) continue;
					
					if(o instanceof AbstractConfigToString)
					{
						list.addAll( ((AbstractConfigToString) o).configToString());
						sb.append(" ").append(((AbstractConfigToString) o).configToString());
					}
					sb.append(" ");
					
				} catch (Exception e) {


				} 
				
			}
		}
		
		for(Field f : this.getClass().getFields())
		{
			if(!f.isAnnotationPresent(Parameter.class)) continue;
			
			
			
			Parameter ant = f.getAnnotation(Parameter.class);
			
			if(ant != null)
			{
				try {
					
					
				
					
					Object o = f.get(this);
					if(o == null) continue;
					
					if(o instanceof Boolean)
					{
						boolean b = (Boolean) o;
								
						if(b)
						{
							list.add(ant.names()[0]);
							sb.append(ant.names()[0]).append(" ");
						}
					} else if(o instanceof File)
					{
						list.add(ant.names()[0]);
						list.add(((File) o ).getAbsolutePath());
						
						sb.append(ant.names()[0]).append(" ");
						sb.append( ((File) o).getAbsolutePath() );
					} else if (o instanceof Integer)
					{
						list.add(ant.names()[0]);
						list.add(o.toString());
						
						sb.append(ant.names()[0]).append(" ");
						sb.append(o);
					}else if (o instanceof Long)
					{
						list.add(ant.names()[0]);
						list.add(o.toString());
						sb.append(ant.names()[0]).append(" ");
						sb.append(o);
					} else if (o instanceof String)
					{
						sb.append(ant.names()[0]).append(" ");
						sb.append("\""+o+"\"");
						list.add(ant.names()[0]);
						list.add(o.toString());
					}  
					else if (o instanceof Double)
					{
						sb.append(ant.names()[0]).append(" ");
						sb.append(o);
						list.add(ant.names()[0]);
						list.add(o.toString());
					} else if(o instanceof Enum)
					{
						sb.append(ant.names()[0]).append(" ");
						sb.append(((Enum) o).name());
						list.add(ant.names()[0]);
						list.add(((Enum) o).name());
					} else 
					{
						System.err.println("No idea what o is " + o.getClass()  +" value:" + o + " name " + ant.names()[0]);
					}
					sb.append(" ");
					
				} catch (Exception e) {


				} 
				
			}
		}
		return list;
	}
}
