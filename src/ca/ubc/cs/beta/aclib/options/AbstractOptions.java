package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

/**
 * Parent Class for most Options objects to allow there settings to be serialized into strings 
 * 
 */
public abstract class AbstractOptions {

	public String toString()
	{
		return this.toString(0);
	}
	public String toString(final int initialTabs)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("[").append(this.getClass().getSimpleName()).append("]").append("\n");
		try {
		for(Field f : this.getClass().getFields())
		{
			StringBuilder line = new StringBuilder();
		
			if(f.getAnnotation(Parameter.class) != null || f.getAnnotation(ParametersDelegate.class) != null)
			{
				boolean isAbstractOption = false;
				for(int i=0; i < initialTabs; i++)
				{
					sb.append("\t");
				}
				
				line.append(f.getName());
				line.append(" = ");
				
				Class<?> o = f.getType();
				if(o.isPrimitive())
				{
					line.append(f.get(this).toString());
				} else
				{
					Object obj = f.get(this);
					if(obj == null)
					{
						line.append("null");
					} else if(obj instanceof File)
					{
						line.append(((File) obj).getAbsolutePath());
					} else if (obj instanceof String)
					{
						line.append(obj);
					} else if (obj instanceof Long)
					{
						line.append(obj.toString());
					} else if(obj instanceof Integer)
					{
						line.append(obj.toString());
					} else if (obj instanceof Enum)
					{
						line.append(((Enum<?>) obj).name());
					} else if (obj instanceof AbstractOptions)
					{
						isAbstractOption = true;
						line.append(((AbstractOptions) obj).toString(initialTabs+2));
					}  else if( obj instanceof List)
					{
						line.append(Arrays.toString(((List<?>) obj).toArray()));
					} else if(obj instanceof Map)
					{
						line.append(obj.toString());
					}
					else {
						/*
						 * We take a cautious approach here, we want every object to have a MEANINGFUL toString() method
						 * so we only add types for things we know provide this
						 */
						throw new IllegalArgumentException("Failed to convert type configuration option to a string " + f.getName() + "=" +  obj + " type: " + o) ;
					}
				}
				if(!isAbstractOption == true)
				{
					sb.append(" ");
				}
				sb.append(line).append("\n");
			}
		}
		return sb.toString();
		} catch(RuntimeException e)
		{
			throw e;
			
		} catch(Exception e)
		{
			throw new RuntimeException(e); 
		}
		
	}
			

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
					
					if(o instanceof AbstractOptions)
					{
						list.addAll( ((AbstractOptions) o).configToString());
						sb.append(" ").append(((AbstractOptions) o).configToString());
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
						sb.append(((Enum<?>) o).name());
						list.add(ant.names()[0]);
						list.add(((Enum<?>) o).name());
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
