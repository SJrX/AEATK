package ca.ubc.cs.beta.configspace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import ca.ubc.cs.beta.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.random.SeedableRandomSingleton;

enum LineType
{
	CATEGORICAL,
	CONTINUOUS,
	CONDITIONAL, 
	OTHER
	
}



/**
 * This parses the parameter specification file into a nice OO view for us, and stores the data in relevant data structures.
 * 
 * The aim of this file is more readability than simplicity as such some of the data structures like paramNames are redundant.
 * 
 * This object is effectively immutable (sans random objects)
 * 
 * Historical Note: This class originally was very Collection heavy, however as the ParamConfiguration objects 
 * are all backed by arrays it really would make more sense for this it to be backed by more arrays.
 * Some of the data structures in here are redundant. This class could use a clean up, but has far reaching consequences.
 * 
 * @author seramage
 */
public class ParamConfigurationSpace implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9137375058810057209L;

	/**
	 * Stores a list of values for each parameter name
	 */
	private final Map<String,List<String>> values = new HashMap<String, List<String>>();
	
	/**
	 * Stores the default value for each parameter name
	 */
	private final Map<String,String> defaultValues = new HashMap<String, String>();
	
	/**
	 * For each parameter name stores the name of the parameters and the required values of those parameters, for this parameter to be active. 
	 */
	private final Map<String, Map<String, List<String>>> dependentValues = new HashMap<String, Map<String,List<String>>>();
	
	/**
	 * For each parameter stores a boolean for whether or not the value is continuous
	 */
	private final Map<String, Boolean> isContinuous = new HashMap<String, Boolean>();
	
	/**
	 * Stores a list of parameter names
	 * @deprecated the paramKeyIndexMap stores the same information, and will iterate the same way
	 * with a slightly more cumbersome interface unfortunately.
	 */
	@Deprecated
	private final List<String> paramNames = new LinkedList<String>();
	
	/**
	 * For each Categorical Parameter, maps the value of input, into the value the random forest expects
	 */
	private final Map<String, Map<String, Integer>> categoricalValueMap = new HashMap<String, Map<String, Integer>>();
	
	
	/**
	 * Used as the basis of hashCode calculations
	 * Two ParamFileParsers are equal iff the file they reference are equal.
	 */
	private final String absoluteFileName;
	
	
	private final Map<String, NormalizedRange> contNormalizedRanges = new HashMap<String, NormalizedRange>();
	
	/**
	 * Stores a mapping of Parameter Names to index into the representative arrays.
	 * The iteration order of this is guaranteed to be authorative (that is you will
	 * always get the first index first, then the second, then the third)...
	 */
	private final Map<String, Integer> paramKeyIndexMap = new LinkedHashMap<String, Integer>();
	private final boolean[] parameterDomainContinuous;
	private final int[] categoricalSize;

	/**
	 * Value to store in the categoricalSize array for continuous parameters
	 */
	static final int INVALID_CATEGORICAL_SIZE = 0;
	
	/**
	 * Number of Neighbours a continuous value has
	 */
	static final int NEIGHBOURS_FOR_CONTINUOUS = 4;
	
	/**
	 * Number of parameters 
	 */
	private final int numberOfParameters;
	
	
	private final int[][] condParents;

	private final int[][][] condParentVals;

	private Random random;
	
	
	
	public ParamConfigurationSpace(String filename, Random random)
	{
		this(new File(filename), random);
	}
	public ParamConfigurationSpace(String filename)
	{
		this(new File(filename));
	}
	
	public ParamConfigurationSpace(File f)
	{
		this(f,SeedableRandomSingleton.getRandom());
	}
	
	public ParamConfigurationSpace(File f, Random random)
	{
		/**
		 * Parse File and create configuration space
		 */
		this.random = random;
		absoluteFileName = f.getAbsolutePath();
		try {
			BufferedReader inputData = null;
			
			try{
			
			inputData = new BufferedReader(new  FileReader(f));
			String line;
			while((line = inputData.readLine()) != null)
			{
				parseLine(line);
			}
			} finally
			{
				Collections.sort(paramNames);
				if (inputData != null)
				{
					inputData.close();
				}
			}
			
			
		} catch (FileNotFoundException e) {

			System.err.println("Parameter File not Found");			
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Some random IO Exception Occured");
			e.printStackTrace();
		}
		
		
		
		/**
		 * Create data structures necessary for ParamConfiguration objects
		 * 
		 * Alot of this is redundant with what's above as this code was added
		 * as part of refactoring. This class could use a clean up
		 */		
		  parameterDomainContinuous = new boolean[paramNames.size()];
		  categoricalSize = new int[paramNames.size()];
		  this.numberOfParameters = paramNames.size();
		  int i=0;
		  for(String paramName : getParameterNamesInAuthorativeOrder())
		  {  
			paramKeyIndexMap.put(paramName,i);
			
			parameterDomainContinuous[i] = getContinuousMap().get(paramName);
			if(parameterDomainContinuous[i] == false)
			{
				categoricalSize[i] = getValuesMap().get(paramName).size();
			} else
			{
				categoricalSize[i] = INVALID_CATEGORICAL_SIZE;
			}
			
			i++;
		  }
		  
		  
		  
		  condParents = new int[numberOfParameters][];
		  condParentVals = new int[numberOfParameters][][];
		  
			Map<String, Map<String, List<String>>> depValueMap = getDependentValuesMap();
			for(i=numberOfParameters; i < numberOfParameters;i++)
			{
				condParents[i] = new int[0];
				condParentVals[i] = new int[0][];
			}
			
			for( i=0; i < numberOfParameters; i++)
			{
				
				String key = getParameterNames().get(i);
				//System.out.println("key => " + key);

				Map<String, List<String>> depValues = depValueMap.get(key); 
				if((depValues == null) || (depValues.size() == 0))
				{
					condParents[i] = new int[0];
					condParentVals[i] = new int[0][];
					continue;
				}
				
				condParents[i] = new int[depValues.size()];
				condParentVals[i] = new int[depValues.size()][];
				
				int j=0;
				for(Entry<String, List<String>> e : depValues.entrySet())
				{
					
					
					condParents[i][j] = paramKeyIndexMap.get(e.getKey()) ;
					condParentVals[i][j] = new int[e.getValue().size()]; 
					for(int k=0; k < e.getValue().size(); k++)
					{

						condParentVals[i][j][k] = getCategoricalValueMap().get(e.getKey()).get(e.getValue().get(k));
						
						condParentVals[i][j][k]++;
						
					}
					j++;	
				}
				
			}

		  		
	
	}
	
	public String getParamFileName()
	{
		return absoluteFileName;
	}
	
	/**
	 * Parses a line from the configuration file, populating the relevant data structures.
	 * @param line
	 */
	private void parseLine(String line)
	{
		//Default to a LineType of other 
		LineType type = LineType.OTHER;
		
		//Removes Comment
		int commentStart = line.indexOf("#");
		line = line.trim();
		if (commentStart >= 0)
		{
			line = line.substring(0, commentStart).trim();
		}
		
		
		//We are done if we trim away the rest of the line
		if (line.length() == 0)
		{
			return;
		}
		
		/** 
		 * Perhaps not the most robust but the logic here is as follows
		 * if we see a "|" it's a conditional line
		 * otherwise we expect to see a "[" (if we don't we have no idea) what it is. 
		 * If we see two (Or more) "[" then allegedly it's continous (one specifies the range the other the default).
		 * Otherwise we must be categorical
		 * 
		 * This is hardly robust and easily tricked.
		 */
		if (line.indexOf("|") >= 0)
		{
			type = LineType.CONDITIONAL;
		} else if(line.indexOf("[") < 0)
		{
			type = LineType.OTHER;
			if(line.trim().equals("Conditionals:")) return;
			throw new IllegalArgumentException("Cannot parse the following line:" + line);
		} else if (line.indexOf("[") != line.lastIndexOf("["))
		{
			type = LineType.CONTINUOUS;
		} else
		{
			type = LineType.CATEGORICAL;
		}
		
		switch(type)
		{
			case CONDITIONAL:
				parseConditionalLine(line);
				break;
			case CATEGORICAL:
				parseCategoricalLine(line);
				break;
			case CONTINUOUS:
				parseContinuousLine(line);
				break;
			default:
				throw new IllegalStateException("Not sure how I can be parsing some other type");
		}
		
		
	}
	
	private void parseContinuousLine(String line) 
	{
		
		String name = getName(line);
		int firstBracket = line.indexOf("[");
		int secondBracket = line.indexOf("]");
		String domainValues = line.substring(firstBracket+1, secondBracket);
		String[] contValues  = domainValues.split(",");
		if(contValues.length != 2)
		{
			throw new IllegalArgumentException ("Expected two parameter values (or one comma between the first brackets) from line \""+ line + "\" but received " + contValues.length);
		}
		
		double min = Double.valueOf(contValues[0]);
		double max = Double.valueOf(contValues[1]);
		
		String defaultValue = getDefault(secondBracket, line);
		
		paramNames.add(name);
		isContinuous.put(name, Boolean.TRUE);
		values.put(name, Collections.<String> emptyList());
		
		this.defaultValues.put(name, defaultValue);
		
		
		String scale = line.substring(line.indexOf("]",secondBracket+1)+1);
		if((scale.length() > 0) && (scale.trim().substring(0, 1).equals("l")))
		{
			contNormalizedRanges.put(name, new NormalizedRange(min, max, true)); 
		} else
		{
			contNormalizedRanges.put(name, new NormalizedRange(min, max, false));
		}

		//throw new UnsupportedOperationException("Do not support continuous lines");
		
	}

	/**
	 * Categorical Lines consist of:
	 *   
	 * <name><w*>{<values>}<w*>[<default>]<*w>#Comment
	 * where:
	 * <name> - name of parameter.
	 * <values> - comma seperated list of values (i.e. a,b,c,d...,z)
	 * <default> - default value enclosed in braces.
	 * <w*> - zero or more whitespace characters
	 * 
	 * 
	 * @param line
	 */
	private void parseCategoricalLine(String line) 
	{
		String name = getName(line);
		List<String> paramValues = getValues(line);
		String defaultValue = getDefault(0,line);
		
		paramNames.add(name);
		values.put(name, paramValues);
		
		//LinkedHashMap just makes it easier to debug
		//Map is all that is necessary
		Map<String, Integer> valueMap = new LinkedHashMap<String, Integer>();
		
		
		int i=0;
		for(String value : paramValues)
		{
			valueMap.put(value, i);
			i++;
			
		}
		categoricalValueMap.put(name, valueMap);
		
		defaultValues.put(name, defaultValue);
		isContinuous.put(name,Boolean.FALSE);
		
		
	}
	
	/**
	 * Conditional Lines consist of:
	 * <name1><w*>|<name2><w+><op><w+>{<values>} #Comment
	 * <name1> - dependent parameter name
	 * <name2> - the independent parameter name
	 * <op> - probably only in at this time.
	 * <w*> - zero or more whitespace characters.
	 * <w+> - one or more whitespace characters
	 * <values> - values 
	 * 
	 * Note: <op> only supports in at this time. (and the public interface only assumes in).
	 * @param line
	 */
	private void parseConditionalLine(String line) {
		String name1 = getName(line);
		String name2 = getName(line.substring(line.indexOf("|") + 1));
		//System.out.println(name1 + " depends on " + name2);
		if (line.indexOf(" in ") < 0)
		{
			throw new IllegalStateException("Unknown operator");
		}
		List<String> condValues = getValues(line);
		
		Map<String, List<String>> dependencies = dependentValues.get(name1);
		if( dependencies == null)
		{
			dependencies = new HashMap<String,List<String>>();
			dependentValues.put(name1, dependencies );
		}
	
		//If multiple lines appear for more than one clause I overwrite the previous value
		dependencies.put(name2, condValues);
		
		
	}
	
	
	/**
	 * Returns the name assuming it starts at the beginning of a line.
	 * @param line
	 * @return
	 */
	private String getName(String line)
	{
		return getName(line,0);
	}
	
	/**
	 * Returns the string from offset until the first " ", "|", "{" 
	 * @param offset
	 * @return
	 */
	private String getName(String line, int offset)
	{
		line = line.trim();
		/**
		 * We are looking for the stuff before the first " ", "|", or "{"
		 */
		int firstSpace = line.indexOf(" ");
		int firstPipe = line.indexOf("|");
		int firstCurly = line.indexOf("{");
		
		if (firstSpace == -1) firstSpace = Integer.MAX_VALUE;
		if (firstPipe == -1) firstPipe = Integer.MAX_VALUE;
		if (firstCurly == -1) firstCurly = Integer.MAX_VALUE;
		
		
		int nameBoundary = Math.min(firstSpace,firstPipe);
		
		nameBoundary = Math.min(nameBoundary, firstCurly);
		return line.substring(offset, nameBoundary).trim();
	}
	
	
	/**
	 *  Returns the default value (takes an offset from where to search)
	 * @param offset
	 * @param line
	 * @return
	 */
	private String getDefault(int offset, String line) {
		return line.substring(line.indexOf("[",offset+1)+1, line.indexOf("]",offset+1)).trim();
	}


	/**
	 * Gets the list of allowable values
	 * @param line
	 * @return
	 */
	private List<String> getValues(String line) {

		int start = line.indexOf("{");
		int end = line.indexOf("}");
		
		line = line.substring(start+1,end);
		line = line.replace(',', '\n');
		
		BufferedReader r = new BufferedReader( new StringReader(line));
		
		List<String> strings = new LinkedList<String>();
		
		String value; 
		
		try {
			while ((value = r.readLine()) != null)
			{
				
					strings.add(value.trim());
				
			}
		} catch (IOException e) {

			System.err.println("Some random IOException occured?");
			e.printStackTrace();
		}
		
		return strings;
	}


	

	public List<String> getParameterNames()
	{
		return Collections.unmodifiableList(paramNames);
	}
	
	public Map<String, Boolean> getContinuousMap()
	{
		return Collections.unmodifiableMap(isContinuous);
	}
	
	/**
	 * This R/O protection isn't robust
	 * @return
	 */
	public Map<String,List<String>> getValuesMap()
	{
		return Collections.unmodifiableMap(values);
	}
	
	
	public Map<String, String> getDefaultValuesMap()
	{
		return Collections.unmodifiableMap(defaultValues);
		
	}
	
	/**
	 * This R/O protection isn't robust
	 * 
	 */
	public Map<String, Map<String, List<String>>> getDependentValuesMap()
	{
		return Collections.unmodifiableMap(dependentValues);
	}
	
	/**
	 * This R/O protection isn't robust
	 */
	public Map<String, Map<String, Integer>> getCategoricalValueMap()
	{
		return Collections.unmodifiableMap(categoricalValueMap);
	}
	
	
	public Map<String, NormalizedRange> getNormalizedRangeMap() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableMap(contNormalizedRanges);
	}
	
	/**
	 * Stores the names of parameters in order
	 * Synchronized because of lazy loading, this should be done better later
	 * 
	 * The authorative order is in fact something that should be specified in the constructor but for now sorting seems to work
	 * 
	 * @return
	 */
	List<String> authorativeParameterNameOrder = null;


	
	public synchronized List<String> getParameterNamesInAuthorativeOrder()
	{
		if(authorativeParameterNameOrder == null)
		{
			authorativeParameterNameOrder = new ArrayList<String>(paramNames.size());
			authorativeParameterNameOrder.addAll(paramNames);
			Collections.sort(authorativeParameterNameOrder);
		}
		return Collections.unmodifiableList(authorativeParameterNameOrder);
	}
	
	/**
	 * Absolute File Name is the basis for the hashCode
	 */
	public int hashCode()
	{
		return absoluteFileName.hashCode();
	}
	
	/**
	 * Two Entities are equal if they reference the same file
	 */
	public boolean equals(Object o)
	{
		if (o instanceof ParamConfigurationSpace)
		{
			ParamConfigurationSpace po = (ParamConfigurationSpace) o;
			return po.absoluteFileName.equals(absoluteFileName);
			
		}
		return false;
	}
	
	public String toString()
	{
		return "ParamFile:" + absoluteFileName;
	}
	
	
	
	/**
	 * Returns a random instance for the configuration space
	 * @param configSpace
	 * @return
	 */
	public ParamConfiguration getRandomConfiguration()
	{

		double[] valueArray = new double[numberOfParameters];
		for(int i=0; i < numberOfParameters; i++)
		{
			if (parameterDomainContinuous[i])
			{
				valueArray[i] = random.nextDouble();
			} else
			{
				//array values = 0 have invalid values, so we take one less of the categorical size and then + 1
				valueArray[i] = random.nextInt(categoricalSize[i]) + 1;
				
			}
		}
		
		ParamConfiguration p = new ParamConfiguration(this, valueArray, categoricalSize, parameterDomainContinuous, paramKeyIndexMap);
		return p;
	}
	
	/**
	 * Returns the default configuration for the Configuration Space
	 * @param configSpace
	 * @return
	 */
	public ParamConfiguration getDefaultConfiguration()
	{

		ParamConfiguration p = new ParamConfiguration(this, categoricalSize, parameterDomainContinuous, paramKeyIndexMap);
		Map<String, String> defaultMap = getDefaultValuesMap();
		p.putAll(defaultMap);	
		return p;
	}
	
	public ParamConfiguration getEmptyConfiguration()
	{
		return new ParamConfiguration(this, categoricalSize, parameterDomainContinuous, paramKeyIndexMap);
	}
	

	public ParamConfiguration getConfigurationFromString( String paramString, StringFormat f)
	{
		try {
		switch(f)
		{
			case NODB_SYNTAX_WITH_INDEX:
				paramString = paramString.replaceFirst("\\A\\d+:", "");
				//NOW IT'S A REGULAR NODB STRING
			case NODB_SYNTAX:
				
				ParamConfiguration config = new ParamConfiguration(this, categoricalSize, parameterDomainContinuous, paramKeyIndexMap);
				String tmpParamString = " " + paramString.replaceAll("'","");
				String[] params = tmpParamString.split("\\s-");
				for(String param : params)
				{
					if(param.equals("")) continue;
					String[] paramSplit = param.trim().split(" ");
					if(!paramSplit[1].trim().equals("NaN"))
					{
						config.put(paramSplit[0].trim(),paramSplit[1].trim());
					}
				}
				
				return config;
				
			default:
				throw new IllegalArgumentException("Parsing not implemented for String Format");
			
		}
		} catch(IllegalArgumentException e )
		{
			throw new IllegalArgumentException(e.getMessage() + " String: " + paramString + " Format: " + f);
		}
		
	}
	
	public int[] getCategoricalSize()
	{
		return categoricalSize.clone();
	}

	public int[][] getCondParentsArray() {

		return condParents.clone();
	}

	public int[][][] getCondParentValsArray() {

		return condParentVals.clone();
	}
	
	
	/**
	 * 
	 * The actual PRNG may change and so clients should always get the latest one from here, as opposed to saving an instance.
	 * 
	 * @return
	 */
	public Random getPRNG()
	{
		return random;
	}
	
	
	public void setPRNG(Random r)
	{
		this.random = r;
	}
	
}

