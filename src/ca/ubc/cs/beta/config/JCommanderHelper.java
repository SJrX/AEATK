package ca.ubc.cs.beta.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;

public class JCommanderHelper {

	
	/***
	 * Looks through the Object tree, for @ParameterFile annotations. If found parses those onto the existing object.
	 * 
	 * 
	 * @param jCommander
	 * @param args
	 */
	public static void parse(JCommander jCommander, String[] args)
	{

		
		try { 
			
			/**
			 * Not sure how to support multiple objects
			 */
			Object o = jCommander.getObjects().get(0);
			
			
			List<Object> objectsToScan = new LinkedList<Object>();
			objectsToScan.add(o);
			/**
			 * Scans the object tree for all @ParametersDelegates
			 */
			getAllObjects(o, objectsToScan);
			
			/**
			 * Load the object with what we have now.
			 * 
			 * 
			 * We do this without Validation because certain things may be specified in a config file.
			 */
			jCommander.parseWithoutValidation(args);
			
			//Set to true iff we read a new ParameterFile
			boolean isModified = true;
			Set<File> usedFiles = new HashSet<File>();
			while(isModified)
			{
				isModified = false;
				
				for(Object obj : objectsToScan)
				{
					for(Field f : obj.getClass().getFields())
					{
					
					
						if(f.isAnnotationPresent(ParameterFile.class))
						{
							if(!f.isAnnotationPresent(Parameter.class))
							{
								throw new IllegalStateException("ParameterFile annotation must be with a Parameter annotation");
							}
							
							if(!f.getType().equals(File.class))
							{
								throw new IllegalStateException("ParameterFile annotation must be set on a File type");
							}
							
							/**
							 * Read the file 
							 */
							File paramFile = (File) f.get(obj);
							Properties prop = new Properties();
							
							if(paramFile != null)
							{
								/**
								 * Don't reparse the same file
								 */
								if(usedFiles.contains(paramFile)) continue;
								
								usedFiles.add(paramFile);
								isModified = true;
								if(!paramFile.exists()) throw new ParameterException("Parameter File " + paramFile.getAbsolutePath() + " does not exist");
								if(!paramFile.canRead()) throw new ParameterException("Parameter File " + paramFile.getAbsolutePath() + " cannot be read");
								
								prop.load(new FileInputStream(paramFile));
								ArrayList<String> fileArgs = new ArrayList<String>(prop.keySet().size()*2);
								for(Entry<Object, Object> e : prop.entrySet())
								{
									/**
									 * Add --(key) to a String[] and then (value) to it
									 * Unless the value is true or false. 
									 */
									String val = e.getValue().toString();
									if(val.equals("true"))
									{ 
										fileArgs.add("--"+e.getKey().toString());
									} else if(val.equals("false"))
									{
										
									} else
									{
										fileArgs.add("--"+e.getKey().toString());
										fileArgs.add(e.getValue().toString());
									}
								}
								/**
								 * Reparse these arguments 
								 */
								jCommander.parseWithoutValidation(fileArgs.toArray(new String[0]));
								
							}
							
							
						}
						
					
						
					
					}
				}
			}
			
		
		
		
			
		
		
		} catch(IllegalAccessException e)
		{
			throw new RuntimeException(e);
		} catch (FileNotFoundException e) {
			throw new ParameterException("ParameterFile could not be found");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		//System.exit(1);
		return;	
	}
	
	public static void getAllObjects(Object o, List<Object> objs) throws IllegalAccessException
	{
		for(Field field : o.getClass().getFields())
		{
			if(field.isAnnotationPresent(ParametersDelegate.class))
			{
				objs.add(field.get(o));
				getAllObjects(field.get(o), objs);
			}
		}
		
	}
}
