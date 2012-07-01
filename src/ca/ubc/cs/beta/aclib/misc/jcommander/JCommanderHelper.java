package ca.ubc.cs.beta.aclib.misc.jcommander;

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

import ca.ubc.cs.beta.aclib.options.ParameterFile;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;

/**
 * Helper class for supporting files with JCommander
 * <p>
 * <b>NOTE:</b> The convention in this file is to refer to options as Parameters, as this is what JCommander does.
 * 
 */
public class JCommanderHelper {

	/***
	 * Looks through the Object tree, for @ParameterFile annotations. If found
	 * parse those files and modify the configuration object accordingly
	 * 
	 * 
	 * @param jCommander  jcommander object to parse 
	 * @param args string array that contains the arguments to parse
	 * @throws ParameterException if there is an error parsing the configuration options
	 */
	public static void parse(JCommander jCommander, String[] args) {

		try {

			/*
			 * JCommander supports multiple configuration objects (not delegates)
			 * I'm not sure how to support this cleanly, so we only use the first one.
			 */
			Object o = jCommander.getObjects().get(0);

			
			if(jCommander.getObjects().size() != 1)
			{
				throw new IllegalStateException("Not sure how to parse this object");
			}
			
			List<Object> objectsToScan = new LinkedList<Object>();
			objectsToScan.add(o);

			getAllOptionObjectDelegates(o, objectsToScan); //updates objectsToScan with all @ParameterDelegates

			
			/**
			 * We parse without validation the arguments first
			 * Then we look through all the objects and see if the @ParameterFile is 
			 * not null, if so we repeat process until we scan through all objects 
			 * and have no modifications
			 */
			jCommander.parseWithoutValidation(args);

			// Set to true iff we read a new ParameterFile
			boolean isModified = true;
			Set<File> usedFiles = new HashSet<File>();
			while (isModified) {
				isModified = false;

				for (Object obj : objectsToScan)
				{
					for (Field f : obj.getClass().getFields())
					{
						if (f.isAnnotationPresent(ParameterFile.class))
						{
							if (!f.isAnnotationPresent(Parameter.class))
							{
								throw new IllegalStateException("ParameterFile annotation must be with a Parameter annotation");
							}

							if (!f.getType().equals(File.class))
							{
								throw new IllegalStateException("ParameterFile annotation must be set on a File type");
							}

							//Read the file
							File paramFile = (File) f.get(obj);
							Properties prop = new Properties();

							if (paramFile != null)
							{	
								if (usedFiles.contains(paramFile)) continue;

								usedFiles.add(paramFile);
								isModified = true;
								if (!paramFile.exists())
								{
									throw new ParameterException( "Parameter File "	+ paramFile.getAbsolutePath()+ " does not exist");
								}
								if (!paramFile.canRead())
								{
									throw new ParameterException("Parameter File "+ paramFile.getAbsolutePath() + " cannot be read");
								}
									

								prop.load(new FileInputStream(paramFile));
								ArrayList<String> fileArgs = new ArrayList<String>(prop.keySet().size() * 2);
								for (Entry<Object, Object> e : prop.entrySet()) {
									/*
									 * Add --(key) to a String[] and then
									 * (value) to it unless the value is true or
									 * false.
									 */
									String val = e.getValue().toString();
									if (val.equals("true")) {
										fileArgs.add("--"+ e.getKey().toString());
									} else if (val.equals("false")) {
										//If false we add nothing (as per convention at the moment)
										//This will break when we start having the default be something other than false
									} else {
										fileArgs.add("--"+ e.getKey().toString());
										fileArgs.add(e.getValue().toString());
									}
								}
								/*
								 * Reparse these arguments
								 */
								jCommander.parseWithoutValidation(fileArgs.toArray(new String[0]));

							}

						}

					}
				}
			}

		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (FileNotFoundException e) {
			throw new ParameterException("ParameterFile could not be found");
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return;
	}

	/**
	 * Scans an object for delegates and recursively adds it to the list of objects found
	 * 
	 * @param optionObjectToScan object to scan for <code>@ParameterDelegates</code>
	 * @param optionObjectsFound a list that has all the objects found
	 * @throws IllegalAccessException if we cannot access a field (all option objects should have public fields)
	 */
	public static void getAllOptionObjectDelegates(Object optionObjectToScan, List<Object> optionObjectsFound)
			throws IllegalAccessException {
		for (Field field : optionObjectToScan.getClass().getFields()) {
			if (field.isAnnotationPresent(ParametersDelegate.class)) {
				optionObjectsFound.add(field.get(optionObjectToScan));
				getAllOptionObjectDelegates(field.get(optionObjectToScan), optionObjectsFound);
			}
		}

	}
}
