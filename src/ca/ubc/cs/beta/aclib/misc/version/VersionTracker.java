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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
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


	private static SortedMap<String, String> init()
	{
			
			Iterator<VersionInfo> versionInfo = ServiceLoader.load(VersionInfo.class).iterator();
			SortedMap<String, String> versionMap = new TreeMap<String, String>();
			
			while(versionInfo.hasNext())
			{
				try { 
				VersionInfo info = versionInfo.next();
				versionMap.put(info.getProductName(),info.getVersion());
				} catch(ServiceConfigurationError e)
				{
					log.warn("Error occured while loading version Information", e);
				}
			}
				
			
		return versionMap;
	}
	
	public static void main(String[] args)
	{
		logVersions();
	}

	
	
	/**
	 * Gets a map of all product versions
	 * @return map that will have an iterator in alphabetical order
	 */
	public static Map<String, String> getVersionMap()
	{
		return init();
	}

	
	
	/**
	 * Gets a string representation of all registered product versions 
	 * @return string of all versions
	 */
	public static String getVersionInformation()
	{
		SortedMap<String, String> versionMap = init();
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
		SortedMap<String, String> versionMap = init();
		for(Entry<String, String> ent : versionMap.entrySet())
		{
			log.info("Version of {} is {} ", ent.getKey(), ent.getValue());
		}
	}
}
