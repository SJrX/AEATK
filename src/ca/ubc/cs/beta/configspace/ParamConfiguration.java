package ca.ubc.cs.beta.configspace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * This class represents an element in the search space, and provides a natural Map like interface for accessing it's members, but also uses an effective and fast 
 * storage mechanism for this. Not all Map methods are supported.
 * 
 * This is not a general purpose Map implementation, specifically the key and value space are all fixed for each parameter, and every Map for a given
 * parser file, will have a constant number of keys in it (some values may be null).
 * 
 * You cannot remove keys, nor can you add keys that don't exist. Effectively the only mutable operation is to replace keys.
 * 
 * The fastest way to iterate over this map is through the keySet, which each iteration taking O(1) expected time [depending on the keyIndexMap hashing], a perhaps 
 * faster way would be to implement getting the EntrySet 
 * 
 * @author seramage
 *
 */
public class ParamConfiguration implements Map<String, String>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 879997991870028528L;

	/**
	 *  Stores a map of paramKey to index into the backing arrays
	 */
	private final Map<String, Integer> paramKeyToValueArrayIndexMap;
	
	/**
	 * For each numerical index in the backing array, is it a continuous parameter
	 * NOTE: DO NOT WRITE TO THIS ARRAY
	 */
	private final boolean[] parameterDomainContinuous;
	
	/**
	 * For each numerical index in the backing array, if categorical what is the size of the domain.
	 * NOTE: DO NOT WRITE TO THIS ARRAY
	 */
	private final int[] categoricalSize;
	

	/**
	 * Configuration space we are from
	 */
	private final ParamConfigurationSpace configSpace;
	
	/**
	 * The array that actually stores our values. We store categorical values as their index in
	 * the configSpace.getValues( key )
	 * 
	 * e.g. if the List contains three values "foo", "bar" and "dog", and we want to store "bar"
	 * we would store 2.0. 
	 *  
	 * For continuous parameters we just store there raw value.
	 * 
	 * For non active parameters we store a NaN
	 */
	private final double[] valueArray;
	
	
	/**
	 * Stores whether the parameter in the array is active or not
	 * 
	 */
	private final boolean[] activeParams;
	
	
	/**
	 * Stores whether the map has been changed since the previous read
	 */
	private boolean isDirty; 
	
	
	/**
	 * Value array used in comparisons
	 */
	private final double[] valueArrayForComparsion; 
	
	/**
	 * Objects should only be constructed via the ParamConfigurationSpace
	 * 
	 * NOTE: We recieve the categoricalSize and paramDomainContinuous from the configSpace, we should not touch them
	 * @param parser
	 */
	ParamConfiguration(ParamConfigurationSpace configSpace ,double[] valueArray, int[] categoricalSize, boolean[] parameterDomainContinuous, Map<String, Integer> paramKeyToValueArrayIndexMap )
	{
		/**
		 * Note other constructors should populate the maps in the same order so that everyone gets the same references
		 */
		this.configSpace = configSpace;
		this.valueArray = valueArray;
		
		this.categoricalSize = categoricalSize;
		this.parameterDomainContinuous = parameterDomainContinuous;
		this.paramKeyToValueArrayIndexMap = paramKeyToValueArrayIndexMap;
		this.myID = idPool.incrementAndGet();
		this.activeParams = new boolean[valueArray.length];
		isDirty = true;
		this.valueArrayForComparsion = new double[valueArray.length];
	}
		
	
	
	/**
	 * Copy constructor
	 * @param m - configuration to copy
	 */
	public ParamConfiguration(ParamConfiguration m)
	{
		this.configSpace = m.configSpace;
		this.valueArray = m.valueArray.clone();
		this.categoricalSize = m.categoricalSize;
		this.parameterDomainContinuous = m.parameterDomainContinuous;
		this.paramKeyToValueArrayIndexMap = m.paramKeyToValueArrayIndexMap;
		this.myID = m.myID;
		this.activeParams = new boolean[valueArray.length];
		isDirty = true;
		this.valueArrayForComparsion = new double[valueArray.length];
	}
	
	/**
	 * Initializes a new / empty map
	 * 
	 * @param configSpace
	 * @param categoricalSize
	 * @param parameterDomainContinuous
	 * @param paramKeyIndexMap
	 */
	ParamConfiguration(ParamConfigurationSpace configSpace, int[] categoricalSize, boolean[] parameterDomainContinuous, Map<String, Integer> paramKeyIndexMap)
	{
		this(configSpace, new double[categoricalSize.length], categoricalSize, parameterDomainContinuous, paramKeyIndexMap);
	}

	
	@Override
	public int size() {
		return valueArray.length;
	}

	@Override
	public boolean isEmpty() {
		return (valueArray.length == 0);
	}

	@Override
	public boolean containsKey(Object key) {			
		return ((paramKeyToValueArrayIndexMap.get(key) != null) && (valueArray[paramKeyToValueArrayIndexMap.get(key)] != 0));
	}

	@Override
	/**
	 * This is a SUPER EXPENSIVE operation, and probably has no purpose for us
	 */
	public boolean containsValue(Object value) {
		if (value instanceof String)
		{
			/*
			String val = (String) value
			this.parser.getValuesMap()
			*/			
			throw new UnsupportedOperationException();
		} else
		{
			return false;
		}
	}

	

	@Override
	public String get(Object key) {
		
		
		
		Integer index = paramKeyToValueArrayIndexMap.get(key);
		if(index == null)
		{
			return null;
		}
		
		
		
		double value = valueArray[index];
		
		if(Double.isNaN(value))
		{
			return null;
		}
		
		if(parameterDomainContinuous[index])
		{
			NormalizedRange range = configSpace.getNormalizedRangeMap().get(key);
			if(range.isIntegerOnly())
			{
				return String.valueOf((long) Math.round(range.unnormalizeValue(value)));
			} else
			{
				return String.valueOf(range.unnormalizeValue(value));
			}
			
		} else
		{
			if(value == 0)
			{
				return null;
			} else
			{		
				return configSpace.getValuesMap().get(key).get((int) value - 1);
			}
		}
	}


	@Override
	/**
	 * Replaces a value in the map
	 * 
	 * NOTE: This operation is fairly slow, and could be sped up if the parser file had a Map<String, Integer> mapping Strings to there integer equivilants.
	 * @param key
	 * @param value
	 * @return old Value
	 */
	public String put(String key, String newValue) 
	{
		/* We find the index into the valueArray from paramKeyIndexMap,
		 * then we find the new value to set from it's position in the getValuesMap() for the key. 
		 * NOTE: i = 1 since the valueArray numbers elements from 1
		 */
		
		isDirty = true;

		Integer index = paramKeyToValueArrayIndexMap.get(key);
		if(index == null)
		{
			throw new IllegalArgumentException("This key does not exist in the Parameter Space: " + key);

		}
		
		String oldValue = get(key);
		
		if(newValue == null)
		{
			valueArray[index] = Double.NaN;
		}
		else if(parameterDomainContinuous[index])
		{
			valueArray[index] = configSpace.getNormalizedRangeMap().get(key).normalizeValue(Double.valueOf(newValue));
			
		} else
		{
			List<String> inOrderValues = configSpace.getValuesMap().get(key);
			int i=1;		
			boolean valueFound = false;
			
			
			for(String possibleValue : inOrderValues)
			{
				if (possibleValue.equals(newValue))
				{
					this.valueArray[index] = i;
					valueFound = true;
					break;
				} 
				i++;
			}
			
			if(valueFound == false)
			{
				throw new IllegalArgumentException("Value is not legal for this parameter: " + key + " Value:" + newValue);
			}
			
			
		}
		
	
		
		if(parameterDomainContinuous[index] && newValue != null)
		{
			double d1 = Double.valueOf(get(key));
			double d2 = Double.valueOf(newValue);
			
			if(Math.abs(d1/d2 - 1) >  Math.pow(10, -12))
			{
				System.out.println("Warning got the following value back from map " + get(key) + " put " + newValue + " in");
			}
				//throw new IllegalStateException("Not Sure Why this happened: " + get(key) + " vs. " + newValue);
		} else
		{
			if(get(key) == null)
			{
				if(newValue != null)
				{
					throw new IllegalStateException("Not Sure Why this happened: " + get(key) + " vs. " + newValue);	
				}
			} else if(!get(key).equals(newValue))
			{
				throw new IllegalStateException("Not Sure Why this happened: " + get(key) + " vs. " + newValue);
			}
		}
		return oldValue;
	}


	@Override
	public String remove(Object key) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public void putAll(Map<? extends String, ? extends String> m) {

		
		for(Entry<? extends String, ? extends  String> ent : m.entrySet())
		{
			this.put(ent.getKey(), ent.getValue());
		}
	
		
		
	}


	@Override
	public void clear() {
		throw new UnsupportedOperationException();
		
	}


	@Override
	/**
	 * Returns a Set that will iterate in the order
	 */
	public Set<String> keySet() {
		LinkedHashSet<String> keys = new LinkedHashSet<String>();
		for(String s : paramKeyToValueArrayIndexMap.keySet())
		{
			keys.add(s);
		}
		
		return keys;
	}


	@Override
	public Collection<String> values() {
		throw new UnsupportedOperationException();
	}


	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		throw new UnsupportedOperationException();
	}
	
	public double[] toValueArray()
	{
		return valueArray.clone();
	}
	
	
	
	public String toString()
	{
		return getFriendlyID() + Arrays.toString(valueArray);
	}
	
	public boolean equals(Object o)
	{
		if (o instanceof ParamConfiguration)
		{
			ParamConfiguration opc = (ParamConfiguration )o;
			if(isDirty) cleanUp();
			if(opc.isDirty) opc.cleanUp();
			
			return configSpace.equals(opc.configSpace) && Arrays.equals(valueArrayForComparsion, opc.valueArrayForComparsion);
		} else
		{
			return false;
		}
	}
	
	
	public int hashCode()	
	{ 
		if(isDirty) cleanUp();
		
		return configSpace.hashCode() ^ Arrays.hashCode(valueArrayForComparsion);
	}
	
	
	/**
	 * Builds a formatted string consisting of the active parameters 
	 * 
	 * @param preKey - String to appear before the key name
	 * @param keyValSeperator - String to appear between the key and value
	 * @param valueDelimiter - String to appear on either side of the value
	 * @param glue - String to placed in between various key value pairs
	 * @return
	 */
	@Deprecated
	public String getFormattedParamString(String preKey, String keyValSeperator,String valueDelimiter,String glue)
	{
		//Should use the String Format method
		return _getFormattedParamString(preKey, keyValSeperator, valueDelimiter, glue, true);
	
		
	}
	
	protected String _getFormattedParamString(String preKey, String keyValSeperator,String valueDelimiter,String glue, boolean hideInactiveParameters)
	{
		Set<String> activeParams = getActiveParameters();
		StringBuilder sb = new StringBuilder();
		boolean isFirstParameterInString = true;
		
		for(String key : keySet())
		{
			if(get(key) == null) continue;
			if((!activeParams.contains(key)) && hideInactiveParameters) continue;
			if(!isFirstParameterInString)
			{
				sb.append(glue);
			}
			isFirstParameterInString = false;
			sb.append(preKey).append(key).append(keyValSeperator).append(valueDelimiter).append(get(key)).append(valueDelimiter);
		}
		return sb.toString();
	}
	
	//TODO Support using StringFormat to generate these
	public String getFormattedParamString()
	{
		return _getFormattedParamString("-", " ","'"," ",true);
	}
	
	public String getFormattedParamString(StringFormat f)
	{
		
		return _getFormattedParamString(f.getPreKey(), f.getKeyValueSeperator(), f.getValueDelimeter(), f.getGlue(), f.hideInactiveParameters());
	}
	/**
	 * This is a hacky way of dealing with the numerous representations
	 * of param strings we have
	 * 
	 */
	public enum StringFormat
	{
		NODB_SYNTAX("-"," ", "'", " ", true), //Parameters are prefixed with a -(name) '(value)'
			NODB_SYNTAX_WITH_INDEX("-"," ", "'", " ", true), //Same as previous except each line starts with (n): where (n) is an integer
			STATEFILE_SYNTAX(" ","=","'",",",false), 
			STATEFILE_SYNTAX_WITH_INDEX(" ", "=","'",",", false),
			SURROGATE_EXECUTOR("-P","=",""," ",true);

		private final String preKey;
		private final String keyValSeperator;
		private final String valDelimiter;
		private final String glue;
		private final boolean hideInactive;

		private StringFormat(String preKey, String keyValSeperator, String valDelimeter, String glue, boolean hideInactive)
		{
			this.preKey = preKey;
			this.keyValSeperator = keyValSeperator;
			this.valDelimiter = valDelimeter;
			this.glue = glue;
			this.hideInactive = hideInactive;
		}

		public boolean hideInactiveParameters() {
			// TODO Auto-generated method stub
			return hideInactive;
		}

		public String getPreKey() {
			return preKey;
		}
		
		public String getGlue()
		{
			return glue;
		}
		
		public String getValueDelimeter()
		{
			return valDelimiter;
		}
		
		public String getKeyValueSeperator()
		{
			return keyValSeperator;
		}
	
	}
	
	
	
	/**
	 * Returns a list of configurations in the neighbourhood of this one (forbidden ones are excluded)
	 * @return
	 */
	public List<ParamConfiguration> getNeighbourhood()
	{
		List<ParamConfiguration> neighbours = new ArrayList<ParamConfiguration>(numberOfNeighbours());
		Set<String> activeParams = getActiveParameters();
		/**
		 * i is the number of parameters
		 * j is the number of neighbours
		 */
		for(int i=0; i < configSpace.getParameterNamesInAuthorativeOrder().size(); i++)
		{
			double[] newValueArray = valueArray.clone();
			
			for(int j=1; j <= numberOfNeighboursForParam(i,activeParams.contains(configSpace.getParameterNamesInAuthorativeOrder().get(i))); j++)
			{
				newValueArray[i] = getNeighbourForParam(i,j);
				
				if(configSpace.isForbiddenParamConfiguration(newValueArray)) continue;
				
				neighbours.add(new ParamConfiguration(configSpace, newValueArray.clone(), categoricalSize, parameterDomainContinuous, paramKeyToValueArrayIndexMap));
			}
		}
		
		
		if(neighbours.size() != numberOfNeighbours()) throw new IllegalStateException("Expected " + numberOfNeighbours() + " neighbours but got " + neighbours.size());
		return neighbours;
		
		
	}
	
	/**
	 * Returns the number of neighbours for this configuration
	 * @return
	 */
	private int numberOfNeighbours()
	{
		int neighbours = 0;
		
		Set<String> activeParams = getActiveParameters();
		
		for(int i=0; i < configSpace.getParameterNamesInAuthorativeOrder().size(); i++)
		{
			
			neighbours += numberOfNeighboursForParam(i, activeParams.contains(configSpace.getParameterNamesInAuthorativeOrder().get(i)));
		}
		/*
		for(int i=0; i < categoricalSize.length; i++)
		{
		
			neighbours += numberOfNeighboursForParam(i);
		}*/
		return neighbours;
		
	}
	
	/**
	 * Returns the number of Neighbours for the specific index into the param Array
	 * @param i
	 * @param b 
	 * @return
	 */
	private int numberOfNeighboursForParam(int i, boolean b)
	{
		if(b == false) return 0;

		if(parameterDomainContinuous[i])
		{
		  return ParamConfigurationSpace.NEIGHBOURS_FOR_CONTINUOUS;
		} else
		{
		  return categoricalSize[i] - 1;
		}
	}
	
	/**
	 * Returns a new neighbour for the param
	 * @param i - index to the value
	 * @param j - number of Neighbour 
	 * @return
	 */
	private double getNeighbourForParam(int i, int j)
	{
		if(parameterDomainContinuous[i])
		{
			
			
			/**
			 * Rejection sampling of N(valueArray[i], 0.2)
			 */
			double mean = valueArray[i];
			
			Random r = configSpace.getPRNG();
			
			while(true)
			{
				double randValue = 0.2*r.nextGaussian() + mean;
				
				if(randValue >= 0 && randValue <= 1)
				{
					return randValue;
				}
			}
		}  else
		{
			
			if(j < valueArray[i])
			{
				return j;
			} else
			{
				return j+1;
			}
		}
	}
	
	/**
	 * Recomputes the Active Parameters 
	 */
	
	public void cleanUp()
	{

	
		Set<String> activeParams = getActiveParameters();
		
		for(Entry<String, Integer> keyVal : this.paramKeyToValueArrayIndexMap.entrySet())
		{
			
			
			this.activeParams[keyVal.getValue()] = activeParams.contains(keyVal.getKey()); 
			
			
			if(this.activeParams[keyVal.getValue()])
			{
				this.valueArrayForComparsion[keyVal.getValue()] = valueArray[keyVal.getValue()];
			} else
			{
				this.valueArrayForComparsion[keyVal.getValue()] = Double.NaN;
			}
		}
	
		isDirty = false;
	}
	
	public Set<String> getActiveParameters()
	{
		boolean activeSetChanged = false;
		Set<String> activeParams= new HashSet<String>();
		
		/**
		 * This code is will loop in worse case ~(n^2) times, the data structures may not be very 
		 * good either, so gut feeling is probably Omega(n^3) in worse case.
		 * 
		 *  This algorithm basically does the following:
		 *  1) Adds all independent clauses to the active set
		 *  2) For every dependent value:
		 *  	- checks if each dependee parameter is active
		 *  	- if all dependee parameters have an acceptible value, adds it to the active set.
		 *  3) Terminates when there are no changes to the active set. (controlled by the changed flag) 
		 */
		do {
			/**
			 * Loop through every parameter to see if it should be added to the activeParams set.
			 */
			activeSetChanged = false;
			List<String> paramNames = this.configSpace.getParameterNames();
			
			for(String candidateParam : paramNames)
			{	
				
				if(activeParams.contains(candidateParam))
				{ //We already know the Param is active
					continue;
				}
				
				//System.out.print("\n[INFO]: " + candidateParam);
				/**
				 * Check if this parameter is conditional (if not add it to the activeParam set), if it is check if it's conditions are all satisified. 
				 */	
				Map<String,List<String>> dependentOn;
				if(( dependentOn = configSpace.getDependentValuesMap().get(candidateParam)) != null)
				{
					//System.out.print(" is dependent ");
					
					
					boolean dependentValuesSatified = true; 
					for(String dependentParamName : dependentOn.keySet())
					{
						if(activeParams.contains(dependentParamName))
						{
							if(dependentOn.get(dependentParamName).contains(get(dependentParamName)))
							{	
								//System.out.print("[+]:" +  dependentParamName +  " is " + params.get(dependentParamName)); 
							} else
							{	
								//System.out.print("[-]:" + dependentParamName +  " is " + params.get(dependentParamName));
								dependentValuesSatified = false;
								break;
							}
								
						} else
						{
							dependentValuesSatified = false;
							break;		
						}
					}
					
					if(dependentValuesSatified == true)
					{
						//System.out.print(" & added.");
						activeSetChanged = true;
						activeParams.add(candidateParam);
					} else
					{
						//System.out.print(" & not added");
					}
					
					
					
				} else
				{ //Not Dependent
					//System.out.print(" is independent ");
					
					if(activeParams.add(candidateParam))
					{
						activeSetChanged = true;
						//System.out.print(" & added.");
					}
					
					
				}
				
				
				
			}

			
		} while(activeSetChanged == true);
		
		
		return activeParams;
	}
	
	
	
	/**
	 * This is just an easy way to refer to the instance
	 * THIS IS VERY HACKY AND FEEL FREE TO REMOVE
	 * @return
	 */
	
	private static final AtomicInteger idPool = new AtomicInteger(0);
	private final int myID;
	
	public int getFriendlyID() {
		return myID;
	}

	
	public boolean isForbiddenParamConfiguration()
	{
		return configSpace.isForbiddenParamConfiguration(valueArray);
		
	}

}
