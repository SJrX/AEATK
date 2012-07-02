package ca.ubc.cs.beta.aclib.misc.version;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that allows various related projects to log their versions and have them reported
 * @author sjr
 *
 */
public class VersionTracker {

	private static final Logger log = LoggerFactory.getLogger(VersionTracker.class);
	/**
	 * Contains a mapping from product name to versions
	 */
	private static final SortedMap<String, String> versionMap = new TreeMap<String, String>();
	
	/**
	 * Given a name of a project, and a file in the classpath read it in as the version for the project
	 * @param name 					name of the project
	 * @param fileInClassPath		file in classpath
	 */
	public static void loadVersionFromClassPath(String name, String fileInClassPath)
	{
		try {
			
			InputStream inputStream = VersionTracker.class.getResourceAsStream("/"+fileInClassPath);
			BufferedReader reader =  new BufferedReader(new InputStreamReader(inputStream));
			String version = reader.readLine();
			registerVersion(name,version);
			inputStream.close();
		} catch (Throwable t) {
			System.out.println(t);
			t.printStackTrace();
			log.debug("Could not retrieve version information",t);
			registerVersion(name, "No Version Information Found");
			
		}
	}
	
	static 
	{
		loadVersionFromClassPath("Automatic Configurator Library", "aclib-version.txt");
		loadVersionFromClassPath("Random Forest Library", "fastrf-version.txt");
	}
	
	public static void main(String[] args)
	{
		logVersions();
	}
	
	/**
	 * Registers a new version 
	 * @param productName 	product to register a version for
	 * @param version     	version of the product
	 */
	public static void registerVersion(String productName, String version)
	{
		versionMap.put(productName, version);
	}
	
	
	/**
	 * Gets a map of all product versions
	 * @return map that will have an iterator in alphabetical order
	 */
	public static Map<String, String> getVersionMap()
	{
		return Collections.unmodifiableMap(versionMap);
		
	}
	
	/**
	 * Gets a string representation of all registered product versions 
	 * @return string of all versions
	 */
	public static String getVersionInformation()
	{
		StringBuilder sb = new StringBuilder();
		
		for(Entry<String, String> ent : versionMap.entrySet())
		{
			sb.append(ent.getKey()).append(" ==> ").append(ent.getValue()).append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * Logs the version number of all registered products
	 */
	public static void logVersions()
	{
		for(Entry<String, String> ent : versionMap.entrySet())
		{
			log.info("Version of {} is {} ", ent.getKey(), ent.getValue());
		}
	}
}
