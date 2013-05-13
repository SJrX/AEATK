package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.loader;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.factory.TargetAlgorithmEvaluatorFactory;


/**
 * Loads Target Algorithm Evaluators
 * <p>
 * <b>Implementation Note:</b> This class cannot use logging as it will be accessed before options are parsed
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class TargetAlgorithmEvaluatorLoader {

private static final String defaultSearchPath;
	
	static{
		//==== This builds a giant string to search for other Target Algorithm Executors
		StringBuilder sb = new StringBuilder();
		String cwd = System.getProperty("user.dir");
		Set<String> files = new HashSet<String>();
		Set<String> directoriesToSearch = new TreeSet<String>();
		
		directoriesToSearch.add(cwd);
		
		String[] classpath = System.getProperty("java.class.path").split(File.pathSeparator);
		
		String pluginDirectory = System.getProperty("user.dir");
		for(String location : classpath)
		{
			if(location.endsWith("aclib.jar"))
			{
				File f = new File(location);
				
				pluginDirectory = f.getParentFile().getAbsolutePath();
				break;
			}
				
		}
		
		pluginDirectory =new File(pluginDirectory) + File.separator + "plugins" + File.separator;
		
		directoriesToSearch.add(pluginDirectory);
		
		File pluginDir = new File(pluginDirectory);
		boolean errorOccured = false;
		//We will look in the plugins directory and all sub directories, but not further
		if(pluginDir.exists())
		{
			//System.out.println(pluginDirectory);
			//System.exit(0);
			if(!pluginDir.canRead())
			{
				System.err.println("WARNING: The plugin directory (" + pluginDir.getAbsolutePath()+ ") is not readable, plugins may not be available");
				System.err.flush();
				System.out.println("WARNING: The plugin directory (" + pluginDir.getAbsolutePath()+ ") is not readable, plugins may not be available");
				System.out.flush();
				errorOccured = true;
			} else {
				
				for(File f : pluginDir.listFiles())
				{
					
					if(f.isDirectory())
					{
						directoriesToSearch.add(f.getAbsolutePath());
					}
				}
			}
		}
		
		directoriesToSearch.add(new File(cwd) + File.separator + "plugins" + File.separator);
		
		directoriesToSearch.add(System.getProperty("java.class.path"));
		for(String dirName : directoriesToSearch)
		{
			File dir = new File(dirName);
			sb.append(dirName);
			sb.append(File.pathSeparator);
			if(dir.exists())
			{
				if(dir.canRead())
				{
					if(dir.isDirectory())
					{
						for(String fileName : dir.list())
						{
							
							if(fileName.trim().endsWith(".jar"))
							{
								//System.out.println(fileName);
								File jarFile = new File(dir.getAbsolutePath() + File.separator + fileName);
								if(!jarFile.canRead())
								{
									System.err.println("WARNING: The jar file (" + jarFile.getAbsolutePath()+ ") is not readable, plugins may not be available");
									System.err.flush();
									System.out.println("WARNING: The jar file (" + jarFile.getAbsolutePath()+ ") is not readable, plugins may not be available");
									System.out.flush();
									errorOccured = true;
								}
								
								if(!files.contains(fileName))
								{
									sb.append(dir.getAbsolutePath());
									sb.append(File.separator);
									sb.append(fileName);
									sb.append(File.pathSeparator);
									
									//System.out.println("Adding " + fileName);
									files.add(fileName);
								}
							}
						}
					}	
				}
			}
		}
		
		defaultSearchPath = sb.toString();
		
		if(errorOccured)
		{
			System.out.println("Warnings have occured sleeping for 30 seconds");
		
			try {
				for(int i=0; i < 30; i++)
				{
					Thread.sleep(1000);
					System.out.print(".");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	/**
	 * Retrieves a Target Algorithm Evaluator configured with the correct options
	 * @param execConfig					configuration object for target algorithm execution
	 * @param name							the name of the Target Algorithm Evaluator to return
	 * @param loader						the class loader to use 
	 * @param options						The abstract options associated with this target algorithm evaluator
	 * @return
	 */
	public static TargetAlgorithmEvaluator getTargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig, String name, AbstractOptions options)
	{
		ClassLoader loader = getClassLoader();
		
		Iterator<TargetAlgorithmEvaluatorFactory> taeIt = ServiceLoader.load(TargetAlgorithmEvaluatorFactory.class, loader).iterator();
		
		while(taeIt.hasNext())
		{
			
			try { 
				TargetAlgorithmEvaluatorFactory tae= taeIt.next();
				//log.debug("Found Target Algorithm Evaluator {}", tae.getName());
				
				if(tae.getName().contains(" "))
				{
					System.err.println("Target Algorithm Evaluator has white space in it's name, this is a violation of the contract of "+ TargetAlgorithmEvaluatorFactory.class.getName());
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				if(tae.getName().trim().equals(name.trim()))
				{
					return tae.getTargetAlgorithmEvaluator(execConfig, options);
				}
			
			} catch(ServiceConfigurationError e)
			{
				
				System.err.println("Error occured while retrieving Target Algorithm Evaluator");
				e.printStackTrace();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e2) {
					Thread.currentThread().interrupt();
				}
			}
		}
			
		
		throw new IllegalArgumentException("No Target Algorithm Evalutor found for name: " + name);
	
	}


	/**
	 * Returns a Mapping of Target Algorithm Evaluators by their name and associated Option objects
	 * @return map contains taeName and their options
	 */
	public static Map<String, AbstractOptions> getAvailableTargetAlgorithmEvaluators()
	{
		return getAvailableTargetAlgorithmEvaluators(getClassLoader());
	}
	
	
	private static Map<String,AbstractOptions> getAvailableTargetAlgorithmEvaluators(ClassLoader loader)
	{
		
		//Whatever map you use here, it should support NULL values.
		Map<String, AbstractOptions> taeOptionsMap = new TreeMap<String,AbstractOptions>();
		
		Iterator<TargetAlgorithmEvaluatorFactory> taeIt = ServiceLoader.load(TargetAlgorithmEvaluatorFactory.class, loader).iterator();
		
		while(taeIt.hasNext())
		{
			
			try { 
				TargetAlgorithmEvaluatorFactory tae= taeIt.next();
				
				if(tae.getName().contains(" "))
				{
					System.err.println("Target Algorithm Evaluator has white space in it's name, this is a violation of the contract of "+ TargetAlgorithmEvaluatorFactory.class.getName());
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				try {
				String name = tae.getName();
				
				
				

				try {
					int modifiers = tae.getClass().getMethod("getOptionObject").getModifiers();
					if(Modifier.isAbstract(modifiers))
					{
						String msg = tae.getName() + " has an abstract method, it is most likely that this plugin needs updating to a current version. It cannot be loaded";
						System.err.println(msg);
						System.err.flush();
						System.out.println(msg);
						System.out.flush();
						try {
							Thread.sleep(2048);
						} catch (InterruptedException e) {
							Thread.currentThread();
						}
						
						continue;
					}
				} catch (SecurityException e) {
					//Just print this crap and continue
					e.printStackTrace();
					continue;
				} catch (NoSuchMethodException e) {
					//Just print this crap and continue
					e.printStackTrace();
					continue;
				}
				
				AbstractOptions options = tae.getOptionObject();
				taeOptionsMap.put(name, options);
				
				
				} catch(AbstractMethodError e)
				{
					System.err.println("Error occurred while processing " + tae.getName());
					throw e;
				}
				
			
			} catch(ServiceConfigurationError e)
			{
				System.err.println("Error occured while retrieving Target Algorithm Evaluator");
				e.printStackTrace();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e2) {
					Thread.currentThread().interrupt();
				}
			}
		}
			
		//Options can be modified in the map, but the map keys and values itself can't be
		return Collections.unmodifiableMap(taeOptionsMap);
	}
	
	/**
	 * Retrieves a modified class loader to do dynamically search for jars
	 * @return
	 */
	public static ClassLoader getClassLoader()
	{
		String pathtoSearch = defaultSearchPath;
		String[] paths = pathtoSearch.split(File.pathSeparator);
		
		ArrayList<URL> urls = new ArrayList<URL>(paths.length);
				
		for(String path : paths)
		{
			
			File f = new File(path);
			
			try {
				urls.add(f.toURI().toURL());
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
				System.err.println("Couldn't parse path " + path);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
				}
			}
			
			
		}
		
		
		URL[] urlsArr = urls.toArray(new URL[0]);
		
		
		URLClassLoader ucl = new URLClassLoader(urlsArr);
		
		return ucl;
		
		
		
	}
	
	
}
