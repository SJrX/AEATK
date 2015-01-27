package ca.ubc.cs.beta.aeatk.parameterconfigurationspace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.jcip.annotations.Immutable;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;
import ca.ubc.cs.beta.aeatk.json.JSONConverter;
import ca.ubc.cs.beta.aeatk.json.serializers.ParameterConfigurationSpaceJson;
import ca.ubc.cs.beta.aeatk.misc.java.io.FileReaderNoException.FileReaderNoExceptionThrown;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration.ParameterStringFormat;
import ec.util.MersenneTwisterFast;


/**
 * This parses the parameter specification file into a nice OO view for us, and stores the data in relevant data structures.
 * <p>
 * The aim of this file is more readability than simplicity as such some of the data structures like paramNames are redundant.
 * <p>
 * This object is effectively immutable and this should not change under any circumstances (for thread safety reasons)
 * <p>
 * <b>Historical Note:</b> This class originally was very Collection heavy, however as the ParamConfiguration objects 
 * are all backed by arrays it really would make more sense for this it to be backed by more arrays.
 * Some of the data structures in here are redundant. This class could use a clean up, but has far reaching consequences.
 * 
 * @author seramage
 */
@Immutable
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@JsonSerialize(using=ParameterConfigurationSpaceJson.ParamConfigurationSpaceSerializer.class)
@JsonDeserialize(using=ParameterConfigurationSpaceJson.ParamConfigurationSpaceDeserializer.class)
public class ParameterConfigurationSpace implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -11111155215218L;

	/**
	 * Stores a list of values for each parameter name
	 */
	private final Map<String,List<String>> values = new HashMap<String, List<String>>();
	
	/**
	 * Stores the default value for each parameter name
	 */
	private final Map<String,String> defaultValues = new HashMap<String, String>();
	
	/**
	 * For each parameter stores a boolean for whether or not the value is continuous
	 * @deprecated this should be replaced with the paramTypes array.
	 */
	private final Map<String, Boolean> isContinuous = new HashMap<String, Boolean>();
	
	/**
	 * types of parameters
	 */
	public enum ParameterType {
		CATEGORICAL, ORDINAL, INTEGER, REAL
	}
	
	/**
	 * For each parameter stores a the ParameterType
	 */
	private final Map<String, ParameterType> paramTypes = new HashMap<String, ParameterType>();
	
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

	public Map<String, Integer> getParamKeyIndexMap() {
		return Collections.unmodifiableMap(paramKeyIndexMap);
	}
	
	
	private final List<int[][]> forbiddenParameterValuesList = new ArrayList<int[][]>();
	/**
	 * Value to store in the categoricalSize array for continuous parameters
	 */
	public static final int INVALID_CATEGORICAL_SIZE = 0;
	
	/**
	 * Number of parameters 
	 */
	private int numberOfParameters;
	

	/**
	 * Flag variable that controls whether there exists a real PCS file
	 * NOTE: This field should not be changed after being set.
	 */
	private boolean hasRealParameterFile = true;
	
	/**
	 * Stores forbidden lines for later parsing
	 */
	private final List<String> forbiddenLines = new ArrayList<String>();


	
	/**
	 * Stores conditional lines for later parsing
	 */
	private final List<String> conditionalLines = new ArrayList<>(); 
	
	/**
	 * Gets the default configuration
	 */
	private final double[] defaultConfigurationValueArray;
	
	/**
	 * For each parameter stores the subspace value we are looking at.
	 */
	final double[] searchSubspaceValues;
	
	/**
	 * Stores whether we should only be looking at the subspace for the parameter.
	 */
	final boolean[]  searchSubspaceActive;
	
	
	/**
	 * Stores the names of parameters in order
	 * Synchronized because of lazy loading, this should be done better later
	 * 
	 * The authorative order is in fact something that should be specified in the constructor but for now sorting seems to work
	 * 
	 */
	private List<String> authorativeParameterNameOrder = new ArrayList<String>();
	
	private Set<String> sortedParameterNames = new TreeSet<String>();

	/**
	 * Array representation of the previous value to speed up searching
	 */
	private final String[] authorativeParameterOrderArray;
	
	/**
	 * Array representation of the ranges by index 
	 */
	private final NormalizedRange[] normalizedRangesByIndex;

	/**
	 *	operators in conditionals; EQ ==, NEQ !=, LE <, GR >, IN "in {...}" 
	 */
	
	
	
	
	
	/**
	 *	Tuple with parent ID, conditional values and ConditionalOperator to save individual conditions
	 */
	public class Conditional {
		public final int parent_ID;
		public final double[] values;
		public final ConditionalOperator op;
		
		public Conditional(int parent_ID, double[] values, ConditionalOperator op) {
			this.parent_ID = parent_ID;
			this.values = values;
			this.op = op;
		}
		
		public String toString()
		{
			return "parentID:" + parent_ID + " values: " + Arrays.toString(values) + " op: " + op;
		}
	}

	//TODO: the next four attributes should be final
	/**
	 * maps parameter (ID) to list of conditions (CNF: disjunctive list of clauses)
	 */
	private Map<Integer, ArrayList<ArrayList<Conditional>>> nameConditionsMap = new HashMap<Integer, ArrayList<ArrayList<Conditional>>>();
	
	private Map<String, HashSet<String>> parameterDependencies = new HashMap<String, HashSet<String>>(); 
	private List<Integer> activeCheckOrder = new ArrayList<Integer>();
	private List<String> activeCheckOrderString = new ArrayList<String>();
	
	/*
	 * maps variable index to disjunctions of conjunctions of parent variables
	 */
	private Map<Integer, int[][]> nameConditionsMapParentsArray;
	/*
	 * maps variable index to disjunctions of conjunctions of parent values in conditional
	 */
	private Map<Integer, double[][][]> nameConditionsMapParentsValues;
	/*
	 * maps variable index to disjunctions of conjunctions of conditional operator
	 */
	private Map<Integer, int[][]> nameConditionsMapOp;
	
	public  Map<Integer, int[][]> getNameConditionsMapParentsArray() {
		return nameConditionsMapParentsArray;
	}

	public Map<Integer, double[][][]> getNameConditionsMapParentsValues() {
		return nameConditionsMapParentsValues;
	}

	public Map<Integer, int[][]> getNameConditionsMapOp() {
		return nameConditionsMapOp;
	}
	
	protected List<String> getActiveCheckOrderString() {
		return activeCheckOrderString;
	}
	
	protected Map<Integer, ArrayList<ArrayList<Conditional>>> getNameConditionsMap(){
		return nameConditionsMap;
	}
	
	public Map<String, ParameterType> getParamTypes(){
		return paramTypes;
	}
	
	private final String pcsFile;
	
	private final Pattern catOrdPattern = Pattern.compile("^\\s*(?<name>\\p{Graph}+)\\s*(?<type>[co])\\s*\\{(?<values>.*)\\}\\s*\\[(?<default>\\p{Graph}+)\\]\\s*$");
	private final Pattern intReaPattern = Pattern.compile("^\\s*(?<name>\\p{Graph}+)\\s*(?<type>[ir])\\s*\\[\\s*(?<min>\\p{Graph}+)\\s*,\\s*(?<max>\\p{Graph}+)\\s*\\]\\s*\\[(?<default>\\p{Graph}+)\\]\\s*(?<log>(log)?)\\s*$");
	
	private final Map<String, String> searchSubspace;
	
	final List<Expression> cl = new ArrayList<Expression>();
	
	final List<ExpressionBuilder> bl = new ArrayList<>();
	
	final ThreadLocal<List<Expression>> tlExpressions = new ThreadLocal<>();
	
	final ThreadLocal<Map<String, Double>> map = new ThreadLocal<>();
	/**
	 * Creates a Param Configuration Space from the given file, no random object
	 * @param filename string storing the filename to parse
	 */
	public ParameterConfigurationSpace(String filename)
	{
		this(new File(filename));
	}
	
	
	/**
	 * 
	 * @param reader
	 * @param searchSubspace
	 */
	public ParameterConfigurationSpace(Reader reader, Map<String, String> searchSubspace)
	{
		this(reader, "ReaderOnly-"+System.currentTimeMillis() +"-" +(int) (Math.random() * 10000000.0), searchSubspace);
		hasRealParameterFile = false;
	}
	
	
	/**
	 * Creates a Param Configuration Space from the given file, no random object
	 * @param reader r
	 */
	public ParameterConfigurationSpace(Reader reader)
	{
 		this(reader, "ReaderOnly-"+System.currentTimeMillis() +"-" +(int) (Math.random() * 10000000.0));
		hasRealParameterFile = false;
	}
	
	
	/**
	 * Creates a Param Configuration Space from the given file
	 * @param file	 File containing the configuration space
	 */
	public ParameterConfigurationSpace(File file)
	{
		this(new FileReaderNoExceptionThrown(file), file.getAbsolutePath());
	}
	
	
	
	/**
	 * Creates a Param Configuration Space from the given reader
	 * @param file 			    A reader object that will allow us to read the configuration space
	 * @param absoluteFileName  A file name of the object (a unique string used for equality)
	 */
	@SuppressWarnings("unchecked")
	public ParameterConfigurationSpace(Reader file, String absoluteFileName)
	{
		this(file, absoluteFileName, Collections.EMPTY_MAP);
	}
	
	
	 
	
	/**
	 * Creates a Param Configuration Space from the given reader
	 * @param file 			    				A reader object that will allow us to read the configuration space
	 * @param absoluteFileName  				A file name of the object (a unique string used for equality)
	 * @param searchSubspace					A map that controls which parameters should be used to generate a subspace
	 */
	public ParameterConfigurationSpace(Reader file, String absoluteFileName, Map<String, String> searchSubspace)
	{
		//TODO: remove stderr outputs - use only errors
		//TODO: maybe try to parse each line independently with the old or new format
		
		/*
		 * Parse File and create configuration space
		 */
		this.absoluteFileName = absoluteFileName;
		
		if((absoluteFileName == null) || (absoluteFileName.trim().length() == 0))
		{
			throw new IllegalArgumentException("Absolute File Name must be non-empty:" + absoluteFileName);
		}
		
		
		try {
			
			StringBuilder pcs = new  StringBuilder();
			try(BufferedReader inputData = new BufferedReader(file))
			{
				String line;
				while((line = inputData.readLine()) != null)
				{ try {
					//System.out.println(line);
					pcs.append(line + "\n");
					parseAClibLine(transformOldFormat(line));
					} catch(RuntimeException e)
					{
						throw e;
					} 
				    
				}
			} 
			
			pcsFile = pcs.toString();
			
			
			
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		
		Collections.sort(this.authorativeParameterNameOrder);
		this.numberOfParameters = this.authorativeParameterNameOrder.size();
		
		for(int i=0; i < this.authorativeParameterNameOrder.size(); i++)
		{
			paramKeyIndexMap.put(this.authorativeParameterNameOrder.get(i), i);
		}
		
		
		
		processConditionalLines();
		
		
		computeCheckActiveOrder();
		
		
		transformConditionals2FastRFStructure();

		//convert authorativeParameterNameOrder and contNormalizedRanges to an Array (before List)
		this.authorativeParameterOrderArray = new String[this.authorativeParameterNameOrder.size()];
		this.normalizedRangesByIndex = new NormalizedRange[this.authorativeParameterNameOrder.size()];
		for (int i = 0; i < authorativeParameterNameOrder.size(); i++) {
			this.authorativeParameterOrderArray[i] = authorativeParameterNameOrder.get(i);
			this.normalizedRangesByIndex[i] = this.contNormalizedRanges.get(this.authorativeParameterOrderArray[i]);
		}

		parameterDomainContinuous = new boolean[numberOfParameters];
		categoricalSize = new int[numberOfParameters];
		int i = 0;
		for (String paramName : authorativeParameterOrderArray) {
			// saves the size of the categorical domains for each parameter
			parameterDomainContinuous[i] = getContinuousMap().get(paramName);
			if (parameterDomainContinuous[i] == false) {
				categoricalSize[i] = getValuesMap().get(paramName).size();
			} else {
				categoricalSize[i] = INVALID_CATEGORICAL_SIZE;
			}

			i++;
		}
		
		
		this.newForbiddenLinesPresent = parseForbiddenLines(forbiddenLines);
	
	
		this.defaultConfigurationValueArray = _getDefaultConfiguration().toValueArray();
		
		
	
		
		/*
		 * This will basically test that 
		 * the default configuration is actually valid
		 * This is a fail fast test in case the parameter values are invalid
		 */
		if(this.getDefaultConfiguration().isForbiddenParameterConfiguration())
		{
			throw new IllegalArgumentException("Default parameter setting cannot be a forbidden parameter setting:" + this.getDefaultConfiguration().getFormattedParameterString());
		}
	
		/*
		for(String forbiddenLine : forbiddenLines )
		{
			parseForbiddenLine(forbiddenLine);
			
		}
		forbiddenLines.clear();
		*/
		
		
	
		this.searchSubspaceValues = new double[this.defaultConfigurationValueArray.length];
		this.searchSubspaceActive = new boolean[this.defaultConfigurationValueArray.length];

		Map<String, String> searchSubspaceMap = new TreeMap<String, String>();
		for (Entry<String, String> subspaceProfile : searchSubspace.entrySet()) {
			String param = subspaceProfile.getKey();
			String value = subspaceProfile.getValue();

			if (value.equals("<DEFAULT>")) {
				value = this.getDefaultConfiguration().get(param);
			}

			searchSubspaceMap.put(param, value);
			int index = this.paramKeyIndexMap.get(param);

			setValueInArray(this.searchSubspaceValues, param, value);
			this.searchSubspaceActive[index] = true;
		}

		this.searchSubspace = Collections.unmodifiableMap(searchSubspaceMap);

	}

	/**
	 * Processes all conditional clauses found in the PCS file
	 */
	private final void processConditionalLines() {
		Map<String, List<String>> conditionalLinesMap = new LinkedHashMap<>();
		
		for(String conditionalLine : conditionalLines)
		{
			String name = getName(conditionalLine);
			if( conditionalLinesMap.get(name) == null)
			{
				conditionalLinesMap.put(name, new ArrayList<String>());
			}
			
			conditionalLinesMap.get(name).add(conditionalLine);
		}
		
		for(Entry<String, List<String>> ent : conditionalLinesMap.entrySet())
		{
			
			List<String> conditionalLines = ent.getValue();
			if(conditionalLines.size() ==  1)
			{
				parseConditional(conditionalLines.get(0));
			} else if( conditionalLines.size() > 1)
			{
				
				
				StringBuilder sb = new StringBuilder(ent.getKey());
				sb.append("|");
				Pattern p = Pattern.compile("^\\s*\\S+\\s*\\|\\s*(\\S+)\\s+in\\s+(\\{.+\\})\\s*$");
				for(String line : conditionalLines)
				{
					Matcher m = p.matcher(line);
					
					if(m.find())
					{
						 String name = m.group(1);
						 String values = m.group(2);
						 
						 
						sb.append(" ").append( name ).append(" in ").append(values).append(" &&");
						
					} else
					{
						throw new IllegalArgumentException("Duplicate conditional lines detected: " + conditionalLines + ". Only one conditional line allowed per variable in new format. This line doesn't seem to be of the old format: " + line);
					}
				}
				
				sb.setLength(sb.length()-2);
				
				
				try {
					//System.err.println("Parsing transformed conditional:" + sb.toString());
					parseConditional(sb.toString());
				} catch(IllegalArgumentException e)
				{
					throw new IllegalArgumentException("Problem occured during parsing conditionals, new PCS format requires all conditionals to be on the same line. This transformation has been done internally, and afterwards the following error occurred:" + e.getMessage());
				}
				
			} else
			{
				throw new IllegalArgumentException("Duplicate conditional lines detected: " + conditionalLines);
			}
			
			
		}
		
		conditionalLines.clear();
	}
	
	/**
	 * Returns the filename that we parsed in absolute form
	 * @return string representating the filename of the param file
	 */
	public String getParamFileName()
	{
		return absoluteFileName;
	}
	
	/**
	 * Parses a line from the pcs file with the AClib 2.0 format spec,
	 * populating the relevant data structures
	 * @param line line of the pcs file
	 */
	private void parseAClibLine(String line){
		
	
		
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
		
		if(line.trim().equals("Conditionals:")) return;
		if(line.trim().equals("Forbidden:")) return;
		
		
		// categorical or ordinal parameters
		Matcher catOrdMatcher = catOrdPattern.matcher(line);
		Matcher intReaMatcher = intReaPattern.matcher(line);
		
		if (catOrdMatcher.find()){
			//System.out.println("CatMatch");
			String name = catOrdMatcher.group("name");
			String type = catOrdMatcher.group("type");
			List<String> paramValues = Arrays.asList(catOrdMatcher.group("values").split(","));
			String defaultValue = catOrdMatcher.group("default").trim();

			
			
			if(this.sortedParameterNames.contains(name)) {
				throw new IllegalArgumentException("Parameter ("+name+") defined more than once.");
			}
			
			sortedParameterNames.add(name);
			//paramKeyIndexMap.put(name, numberOfParameters);
		
			authorativeParameterNameOrder.add(name);
			
			List<String> paramValues_trimed = new ArrayList<String>();
			
			Set<String> paramValuesSet = new HashSet<String>();
			for (String value: paramValues){
				paramValues_trimed.add(value.trim());
				if(!paramValuesSet.add(value.trim()))
				{
					throw new IllegalArgumentException("Duplicate value: " + value.trim() + " detected on line: " + line);
				}
			}
			
			values.put(name, paramValues_trimed);
			Map<String, Integer> valueMap = new LinkedHashMap<String, Integer>();
			int i=0;
			for(String value : paramValues_trimed) {
				valueMap.put(value, i);
				i++;
			}
			//TODO: check whether ordinal parameters are already handled correctly
			categoricalValueMap.put(name, valueMap);
			defaultValues.put(name, defaultValue);
			isContinuous.put(name,Boolean.FALSE);
			
			switch (type){
				case "o": 	paramTypes.put(name, ParameterType.ORDINAL);
							break;
				case "c":   paramTypes.put(name, ParameterType.CATEGORICAL);
							break;
				default:
					throw new IllegalStateException("Could not identify the type of the parameter: "+line);
			}
			return;
		} else if (intReaMatcher.find()){
			//integer or real valued parameters

			boolean intValuesOnly = false;
			boolean logScale = false;
			String name = intReaMatcher.group("name");
			String type = intReaMatcher.group("type");
			
			if (sortedParameterNames.contains(name)) {
				throw new IllegalArgumentException("Parameter ("+name+") defined more than once.");
			}
			
			sortedParameterNames.add(name);
			//paramKeyIndexMap.put(name, numberOfParameters);
			
			authorativeParameterNameOrder.add(name);
			
			if (type.equals("i")) {
				intValuesOnly = true;
			}
			Double min;
			Double max;
			Double defaultValue;
			try {
				min = Double.valueOf(intReaMatcher.group("min"));
				max = Double.valueOf(intReaMatcher.group("max"));
				
				
				if(min > max)
				{
					throw new IllegalArgumentException("Minimum value " + min + " must be less than the maximum value: " + max + " on line:" + line);
				}
				defaultValue = Double.valueOf(intReaMatcher.group("default"));
			} catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("This parameter has to consists of numbers:" + line);
			}
			
			
			if (!intReaMatcher.group("log").isEmpty()){
				logScale = true;
			}
			
			if(intValuesOnly)
			{
				try {
				if(!isIntegerDouble(Double.valueOf(min))) throw new IllegalArgumentException("This parameter is marked as integer, only integer values are permitted for the bounds and default on line:" + line); 
				if(!isIntegerDouble(Double.valueOf(max))) throw new IllegalArgumentException("This parameter is marked as integer, only integer values are permitted for the bounds and default on line:" + line);
				if(!isIntegerDouble(Double.valueOf(defaultValue))) throw new IllegalArgumentException("This parameter is marked as integer, only integer values are permitted for the bounds and default on line:" + line);
				
				
				if (defaultValue < min || defaultValue > max)
				{
					throw new IllegalArgumentException("Default value " + defaultValue +  " must fall in the interval [" + min + "," + max +"] on line: " + line);
				}
				} catch(NumberFormatException e)
				{
					throw new IllegalArgumentException("This parameter is marked as integer, only integer values are permitted for the bounds and default on line:" + line);
				}
			}
			
			isContinuous.put(name, Boolean.TRUE);
			values.put(name, Collections.<String> emptyList());
			switch (type){
				case "i": 	
						paramTypes.put(name, ParameterType.INTEGER);
						break;
				case "r":
						paramTypes.put(name, ParameterType.REAL);
						break;
				default:
					throw new IllegalStateException("Could not identify the type of the parameter: "+line);
			}
			defaultValues.put(name, intReaMatcher.group("default"));
			
			try {
				contNormalizedRanges.put(name, new NormalizedRange(min, max, logScale, intValuesOnly));
			} catch(IllegalArgumentException e)
			{
				throw new IllegalArgumentException(e.getMessage() + "; error occured while parsing line: " +line);
			}

			return;
		} else if(generalForbidden.matcher(line).find())
		{
			//System.out.println("Adding line: " + line);
			forbiddenLines.add(line);
			return;
		} else if (line.indexOf("|") >= 0) {
			
			conditionalLines.add(line);
			return;
		} 
		

		throw new IllegalArgumentException("Not sure how to parse this line: "+line);

	}
	
	/**
	 * parse conditional lines in AClib 2.0 format
	 * param | conditional_1 [operator] conditional_2 [operator] ...
	 * where [operator] is && or ||
	 * and conditionals are either
	 * param_x [==, !=, <, >] value
	 * or
	 * param_x in {value_1, value_2, ...}
	 * 
	 * no parentheses between the conditionals (makes parsing a lot easier)
	 * 
	 * Assumptions:
	 *   (i) parameters are only once in the head of a condition
	 *   (ii) conditions come after parameter definition
	 * 
	 * @param line line with conditional (as described above)
	 */
	private void parseConditional(String line){
		
		String param_name = getName(line);
		
		String conditions = line.substring(line.indexOf("|") + 1);
		
		// since we don't support parantheses, the conditionals are parsed as CNF 
		// and represented as two nested arrays
		
		// disjunction are less important than conjunctions -> split first the disjunctions
		String[] disjunctions = conditions.split("\\|\\|");
		
		ArrayList<ArrayList<Conditional>> conditionals = new ArrayList<ArrayList<Conditional>>();
		
		Integer param_id = paramKeyIndexMap.get(param_name);
		if (param_id == null) {
			throw new IllegalArgumentException("Conditioned parameter "+param_name+" not defined.");
		}
		
		if (nameConditionsMap.get(param_id) != null){
			throw new IllegalArgumentException("Each parameter can only be once in the head of a condition:" + param_name + " on line: " + line);
		}
		nameConditionsMap.put(param_id, conditionals);
		
		for(String dis : disjunctions){
			//split the conjunctions
			String[] conjunctions = dis.split("&&");
			
			//save triple for each conditional: (ID) parent, (ID / or float) values, type   
			
			ArrayList<Conditional> conj_conds = new ArrayList<Conditional>();
			conditionals.add(conj_conds);
			
			for(String con: conjunctions){
				String parent = ConditionalOperator.getParent(con);
				ConditionalOperator op = ConditionalOperator.getOperatorFromConditionalClause(con);
				String[] value = ConditionalOperator.getValues(con);
				
				
				
				
				if (!this.paramKeyIndexMap.keySet().contains(parent)) {
					throw new IllegalArgumentException("Unknown dependent parameter: " + parent	+ ", specified on conditional line: " + line);
				}
				
				List<Double> values_mapped = new ArrayList<Double>();
				for (String v : value) {
					Double vmapped; 
					if (paramTypes.get(parent) == ParameterType.CATEGORICAL || paramTypes.get(parent) == ParameterType.ORDINAL) { 
						Integer mapped_value = this.categoricalValueMap.get(parent).get(v);
						if (mapped_value == null) {
							throw new IllegalArgumentException("Illegal dependent parameter (" + (parent)+ ") specified on conditional line: " + line);
						}
						vmapped = (double) mapped_value;
					} else {
						try {
							vmapped = Double.parseDouble(v);
						} catch (NumberFormatException e){
							throw new IllegalArgumentException(parent + " is a of type i or o but is compared a non-double value in conditionals.");
						}
					}
					values_mapped.add(vmapped);
				}
				
				int parent_id = paramKeyIndexMap.get(parent); 
				
				double[] value_array = new double[values_mapped.size()];
				int i = 0;
				for (double v : values_mapped) {
					value_array[i] = v;
					i++;
				}
				
				Conditional cond_tuple = new Conditional(parent_id, value_array, op);
				
				conj_conds.add(cond_tuple);
				
				//remember all other parameter that it has conditions on
				if (parameterDependencies.get(param_name) == null){
					HashSet<String> deps = new HashSet<String>();
					deps.add(parent);
					parameterDependencies.put(param_name, deps);
				} else {
					parameterDependencies.get(param_name).add(parent);
				}
			}
			
		} 
	}
	
	/**
	 * computes the order in which the parameter should be checked whether they are active
	 */
	private void computeCheckActiveOrder(){
		Set<String> params_left = new HashSet<String>(authorativeParameterNameOrder);
		boolean changed = true;
		while(!params_left.isEmpty() && changed){
			changed = false;
			Set<String> to_remove = new HashSet<String>();
			for (String p: params_left){
				boolean ok = true;
				if (parameterDependencies.get(p) != null) {
					for(String d: parameterDependencies.get(p)){
						if (!activeCheckOrder.contains(paramKeyIndexMap.get(d))) {
							ok = false;
							break;
						}
					}
				}
				if (ok) {
					activeCheckOrder.add(paramKeyIndexMap.get(p));
					activeCheckOrderString.add(p);
					to_remove.add(p);
					changed = true;
				}
			}
			params_left.removeAll(to_remove);
		}
		if (!params_left.isEmpty()) {
			throw new IllegalArgumentException("Could not parse hierarchy of parameters. Probably cycles in there. ("+ Arrays.toString(params_left.toArray())+")");
		}
	}
	
	/**
	 * transforms <nameConditionsMap> into <nameConditionsMapParentsArray>, <nameConditionsMapParentsValues>, <nameConditionsMapOp>
	 * and <activeCheckOrder> into <activeCheckOrderArray>
	 */
	private void transformConditionals2FastRFStructure(){
		nameConditionsMapParentsArray = new HashMap<Integer, int[][]>();
		nameConditionsMapParentsValues = new HashMap<Integer, double[][][]>();
		nameConditionsMapOp = new HashMap<Integer, int[][]>();
		for (int p_idx: nameConditionsMap.keySet()) {
			int[][] parent_id = new int[nameConditionsMap.get(p_idx).size()][];
			double[][][] values = new double[nameConditionsMap.get(p_idx).size()][][];
			int[][] op = new int[nameConditionsMap.get(p_idx).size()][];
			
			int i = 0;
			for (List<Conditional> clause: nameConditionsMap.get(p_idx)){
				int j = 0;
				int[] parent_id_in = new int[clause.size()];
				double[][] values_in = new double[clause.size()][];
				int[] op_in = new int[clause.size()];
				for (Conditional cond: clause){
					parent_id_in[j] = cond.parent_ID;
					String parent_name = authorativeParameterNameOrder.get(cond.parent_ID);
					Boolean iscont = isContinuous.get(parent_name);
					//contNormalizedRanges: name-> normalizer
					if (iscont){
						//continuous parameters have to be normalized to range [0,1]
						int z = 0;
						values_in[j] = new double[cond.values.length];
						for (Double d : cond.values){
							//System.out.println(parent_name);
							//System.out.println(contNormalizedRanges.get(parent_name));
							//System.out.println(contNormalizedRanges.get(parent_name).normalizeValue(d));
							try 
							{
								values_in[j][z] = contNormalizedRanges.get(parent_name).normalizeValue(d);
							} catch(IllegalArgumentException e)
							{
								throw new IllegalArgumentException("Error parsing conditional statement with variable " + parent_name + " : " + e.getMessage() + ".");
							}
							z++;
						}
					}
					else {
						//categorical and ordinal values are already encoded as integer
						values_in[j] = cond.values;	
					}
					
					
					op_in[j] = cond.op.getOperatorCode();
					j++;
				}
				parent_id[i] = parent_id_in;
				values[i] = values_in;
				op[i] = op_in;
				i++;
			}
			nameConditionsMapParentsArray.put(p_idx, parent_id);
			nameConditionsMapParentsValues.put(p_idx, values);
			nameConditionsMapOp.put(p_idx, op);
		}
	} 
	
	
	
	private final Pattern classicForbidden = Pattern.compile("^\\s*\\{((\\s*\\S+\\s*=\\s*\\S+\\s*,?\\s*)+)\\}\\s*$");
	private final Pattern generalForbidden = Pattern.compile("^\\s*\\{\\s*(.+)\\s*\\}\\s*$");
	
	private boolean parseForbiddenLines(List<String> lines) {
		
		boolean newForbiddenStatements = false;
		for(String line : lines)
		{
			
			String originalLine = line;
			
			if(line.indexOf("#") >= 0)
			{
				line = line.substring(0, line.indexOf("#"));
			}
			
			
			Matcher classicForbiddenStatementMatcher = classicForbidden.matcher(line);
			Matcher generalForbiddenMatcher = generalForbidden.matcher(line);
			if(classicForbiddenStatementMatcher.matches())
			{
				String values = classicForbiddenStatementMatcher.group(1);
				String[] nameValuePairs = values.split(",");
				
				List<int[]> forbiddenIndexValuePairs = new ArrayList<int[]>();
						
				for(String nameValuePair : nameValuePairs)
				{
					String[] nvPairArr = nameValuePair.split("=");
					if(nvPairArr.length != 2)
					{
						throw new IllegalArgumentException("Line specifying forbidden parameters contained an name value pair that could not be parsed: "+ Arrays.toString(nvPairArr) + " in line: " + originalLine);
					}
					
					String name = nvPairArr[0].trim();
					Integer indexIntoValueArrays = paramKeyIndexMap.get(name);
					
					if(indexIntoValueArrays == null)
					{
						throw new IllegalArgumentException("Unknown parameter " + name + " in line: " + originalLine);
					}
					
					String value = nvPairArr[1].trim();
					
					if(paramTypes.get(name) == ParameterType.REAL || paramTypes.get(name) == ParameterType.INTEGER)
					{
						throw new IllegalArgumentException("Forbidden Parameter Declarations can only exclude combinations of categorical or ordinal parameters " + name + " is continuous; in line: " + line );
					}
					
					Integer valueIndex = categoricalValueMap.get(name).get(value);
					
					if(valueIndex == null)
					{
						throw new IllegalArgumentException("Invalid parameter value " + value + " for parameter " + name + " in line: " + line);
						
					}
					
					int[] nvPairArrayForm = new int[2];
					nvPairArrayForm[0] = indexIntoValueArrays;
					nvPairArrayForm[1] = valueIndex; 
					
					forbiddenIndexValuePairs.add(nvPairArrayForm);
				}
				
				
				forbiddenParameterValuesList.add(forbiddenIndexValuePairs.toArray(new int[0][0]));
				
			} else if(generalForbiddenMatcher.find())
			{
				newForbiddenStatements = true;
				
				/*
				line = line.replaceAll("!=", ":");
				
				line = line.replaceAll(">=", Matcher.quoteReplacement("$"));
				line  =line.replaceAll("<=", "#");
				line = line.replaceAll("==","|");
				*/
				
				ExpressionBuilder eb = new ExpressionBuilder(line);
				
				
				eb.variables(new HashSet<>(this.getParameterNames()));
				
				eb.operator(ForbiddenOperators.operators);
				
				bl.add(eb);
				
				Expression calc = eb.build();
				
				cl.add(calc);
			
			} else
			{
				throw new IllegalArgumentException("Unsure how to parse the following line, guess was that it was forbidden, line: " + originalLine);
			}
			
			
		}
		
		forbiddenLines.clear();
		return newForbiddenStatements;
		
		
		/*
		if(line.trim().indexOf("{",1) != -1) throw new IllegalArgumentException("Line specifying forbidden parameters contained more than one { in line: " + originalLine);
		
		line = line.replace("{", "");
		
		
		if(line.trim().indexOf("}") == -1) throw new IllegalArgumentException("Line specifying forbidden parameters contained no closing brace \"}\" in line: " + originalLine);
		
		line = line.replaceFirst("}","");
		
		if(line.trim().indexOf("}") != -1) throw new IllegalArgumentException("Line specifying forbidden parameters contained multiple closing braces \"}\" in line: " + originalLine);

		*/
		
		//Remove Comment
	
		
		
		
		
		
	}
	
	Pattern oldCategoricalFormatMatcher = Pattern.compile("^\\s*(\\S+)\\s*\\{(.+)\\}\\s*\\[\\s*(\\S+)\\s*\\]\\s*$");
	Pattern oldContinuousFormatMatcher = Pattern.compile("^\\s*(\\S+)\\s*\\[(.+)\\]\\s*\\[\\s*(\\S+)\\s*]\\s*([il]*[il]*)\\s*$");
	
	private String transformOldFormat(String s)
	{
		String line = s;
		int commentStart = line.indexOf("#");
		line = line.trim();
		if (commentStart >= 0)
		{
			line = line.substring(0, commentStart).trim();
		}
		
		//We are done if we trim away the rest of the line
		if (line.length() == 0)
		{
			return s;
		}
		
		if(line.trim().equals("Conditionals:")) return s;
		if(line.trim().equals("Forbidden:")) return s;
		
		Matcher match = oldCategoricalFormatMatcher.matcher(line);
		
		if(match.find())
		{
			String name = match.group(1);
			String values = match.group(2);
			String defaultValue = match.group(3);
			
			
			String newLine =  name + " c {" +values+"} [" + defaultValue + "]";
			
			//System.err.println("Transformation: " + s + "\t ====>" + newLine);
			return newLine;
		}
		
		
		match = oldContinuousFormatMatcher.matcher(line);
		
		
		if(match.find())
		{
			String name = match.group(1);
			String range = match.group(2);
			String defaultValue = match.group(3);
			String il = match.group(4);
			
			boolean integerOnly = false;
			boolean logTransform = false;
			
			
			
			if(il.contains("i"))
			{
				integerOnly = true;
			}
			
			if(il.contains("l"))
			{
				logTransform = true;
			}
			
			String newLine = name + " " + (integerOnly?"i":"r") + " [" + range + "] " + "[" + defaultValue +"]" + (logTransform?"log":"");
			
			//System.err.println("Transformation: " + s + "\t ====>" + newLine);
			return newLine;
		}
		
		return s;
		
		
		
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
		int firstSquare = line.indexOf("[");
		
		if (firstSpace == -1) firstSpace = Integer.MAX_VALUE;
		if (firstPipe == -1) firstPipe = Integer.MAX_VALUE;
		if (firstCurly == -1) firstCurly = Integer.MAX_VALUE;
		if (firstSquare == -1) firstSquare = Integer.MAX_VALUE;
		
		
		int nameBoundary = Math.min(firstSpace,firstPipe);
		
		nameBoundary = Math.min(nameBoundary, firstCurly);
		nameBoundary = Math.min(nameBoundary, firstSquare);
		String name =  line.substring(offset, nameBoundary).trim();
		if(name.length() == 0)
		{
			throw new IllegalArgumentException("Did Not Parse a Parameter Name in line: " + line);
		} else
		{
			return name;
		}
	}

	/**
	 * Gets the list of allowable values
	 * @param line
	 * @return
	 */
	private List<String> getValues(String line) {

		String oLine = line;
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
				
					if (value.trim().length() == 0)
					{
						throw new IllegalArgumentException("Value cannot be empty (consist only of whitespace) in line: " + oLine);
					} else
					{
						strings.add(value.trim());
					}
				
			}
		} catch (IOException e) {

			//System.err.println("Some random IOException occured?");
			e.printStackTrace();
			
			throw new IllegalStateException("An exception occured while reading values from (" + oLine + ") we mistakenly thought this would never happen, please contact developer", e);
		}
		
		Set<String> set = new HashSet<String>();
		set.addAll(strings);
		if(set.size() < strings.size())
		{
			throw new IllegalStateException("Duplicate Value detected in line: " +  oLine);
		}
		
		return strings;
	}


	

	public List<String> getParameterNames()
	{
		return Collections.unmodifiableList(authorativeParameterNameOrder);
	}
	
	public Map<String, Boolean> getContinuousMap()
	{
		return Collections.unmodifiableMap(isContinuous);
	}
	
	/**
	 * Returns a map of values for each parameter the list of string values
	 * 
	 * <b>WARN</b> While the map itself is immutable, messing with the internal data structures will corrupt the config space
	 * @return map of values
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
	 */
	public Map<String, Map<String, Integer>> getCategoricalValueMap()
	{
		return Collections.unmodifiableMap(categoricalValueMap);
	}
	
	
	public Map<String, NormalizedRange> getNormalizedRangeMap() {
		return Collections.unmodifiableMap(contNormalizedRanges);
	}
	
	
	/**
	 * Returns a listing of parameter names in <b>authorative</b> order
	 * @return list of strings
	 */
	public List<String> getParameterNamesInAuthorativeOrder()
	{
		return authorativeParameterNameOrder;
	}
	
	/**
	 * Absolute File Name is the basis for the hashCode
	 */
	public int hashCode()
	{
		return absoluteFileName.hashCode();
	}
	
	
	/**
	 * Determines whether two configuration spaces are 'compatible'
	 * <br/>
	 * <b>Note:</b> Compatible here at the very least means the valueArrays should be interchangable, in future it may be stronger.
	 * 
	 * @return <code>true</code> if the configuration spaces are compatible
	 */
	public boolean isCompatible(ParameterConfigurationSpace oSpace)
	{
		if(!Arrays.equals(this.categoricalSize, oSpace.categoricalSize))
		{
			return false;
		} else
		{
			return true;
		}
		
	}
	
	
	/**
	 * Two Entities are equal if they reference the same file
	 */
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if (o instanceof ParameterConfigurationSpace)
		{
			ParameterConfigurationSpace po = (ParameterConfigurationSpace) o;
			if(po.absoluteFileName.equals(absoluteFileName))
			{
				if(Arrays.equals(searchSubspaceActive, po.searchSubspaceActive))
				{
					if(Arrays.equals(this.searchSubspaceValues, po.searchSubspaceValues))
					{
						return true;
					}
				}
			}
			
			
		}
		return false;
	}
	
	public String toString()
	{
		return "ParamFile:" + absoluteFileName;
	}
	
	/**
	 * Generates a random configuration given the supplied random object 
	 * @param random object we will use to generate the configuration 
 	 * @return a random member of the configuration space (each parameter (ignoring the subspace) is sampled uniformly at random, and rejected if it's forbidden).
	 */
	public ParameterConfiguration getRandomParameterConfiguration(Random random)
	{
		
		return this.getRandomParameterConfiguration(random, false);
	}
	
	/**
	 * Returns a random instance for the configuration space
	 * @param random 	a random object we will use to generate the configurations
	 * @param allowForbiddenParameters  <code>true</code> if we can return parameters that are forbidden, <code>false</code> otherwise.
	 * @return	a random member of the configuration space (each parameter (ignoring the subspace) is sampled uniformly at random, and rejected if it's forbidden
	 */
	public ParameterConfiguration getRandomParameterConfiguration(Random random, boolean allowForbiddenParameters)
	{
		return getRandomParameterConfiguration(new RandomAdapter(random), allowForbiddenParameters);
	}
	
	/**
	 * Generates a random configuration given the supplied random object 
	 * @param random a fast random object we will use to generate the configuration 
 	 * @return a random member of the configuration space (each parameter (ignoring the subspace) is sampled uniformly at random, and rejected if it's forbidden).
	 */
	public ParameterConfiguration getRandomParameterConfiguration(MersenneTwisterFast random)
	{
		
		return this.getRandomParameterConfiguration(new RandomAdapter(random), false);
	}
	
	/**
	 * Returns a random instance for the configuration space
	 * @param random 	a fast random object we will use to generate the configurations
	 * @param allowForbiddenParameters  <code>true</code> if we can return parameters that are forbidden, <code>false</code> otherwise.
	 * @return	a random member of the configuration space (each parameter (ignoring the subspace) is sampled uniformly at random, and rejected if it's forbidden
	 */
	public ParameterConfiguration getRandomParameterConfiguration(MersenneTwisterFast random, boolean allowForbiddenParameters)
	{
		return this.getRandomParameterConfiguration(new RandomAdapter(random), allowForbiddenParameters);
	}
	
	
	/**
	 * Returns a random instance for the configuration space
	 * @param random 	a random object we will use to generate the configurations
	 * @param allowForbiddenParameters  <code>true</code> if we can return parameters that are forbidden, <code>false</code> otherwise.
	 * @return	a random member of the configuration space (each parameter (ignoring the subspace) is sampled uniformly at random, and rejected if it's forbidden
	 */
	private ParameterConfiguration getRandomParameterConfiguration(RandomAdapter random, boolean allowForbiddenParameters)
	{
		
		if(random == null) throw new IllegalArgumentException("Cannot supply null random object ");
		
		//fastRand.setSeed(random.nextLong());
		double[] valueArray = new double[numberOfParameters];
		
		for(int j=0; j < 1000000; j++)
		{
			
			for(int i=0; i < numberOfParameters; i++)
			{
				if(searchSubspaceActive[i])
				{		
					valueArray[i] = searchSubspaceValues[i];
				}	else if (parameterDomainContinuous[i])
				{
					NormalizedRange nr = this.normalizedRangesByIndex[i]; 
					valueArray[i] = nr.normalizeValue(nr.unnormalizeValue(random.nextDouble()));
				
				} else
				{
					valueArray[i] = random.nextInt(categoricalSize[i]) + 1;
				}
			}
			
			ParameterConfiguration p = new ParameterConfiguration(this, valueArray, categoricalSize, parameterDomainContinuous, paramKeyIndexMap);
			if(allowForbiddenParameters || !p.isForbiddenParameterConfiguration())
			{
				return p;
			} 
		}
		
		throw new IllegalArgumentException("After 1,000,000 attempts at generating a random configurations we have failed to generate even one that isn't forbidden. It is likely that your forbidden parameter settings are too restrictive. Try excluding smaller regions of the space.");
	}
	
	/**
	 * Returns the default configuration for the Configuration Space
	 * @return	paramconfiguration representing the default
	 */
	public ParameterConfiguration getDefaultConfiguration()
	{
		return getConfigurationFromValueArray(this.defaultConfigurationValueArray);
	}
	
	private ParameterConfiguration _getDefaultConfiguration()
	{
		
		ParameterConfiguration p = new ParameterConfiguration(this, new double[numberOfParameters], categoricalSize, parameterDomainContinuous, paramKeyIndexMap); 
		Map<String, String> defaultMap = getDefaultValuesMap();
		p.putAll(defaultMap);	
		return p;
	}
	
	/**
	 * Returns an upper bound on the size of the configuration space. 
	 * There are no guarantees how tight this upper bound might be, and in general it may get looser over time.
	 * You are only guaranteed that the number of actual configurations is LOWER than this upper bound.
	 * <b>NOTE:</b> Search Subspaces do NOT lower the size of the upper bound because you can leave the subspace 
	 * 
	 * @return an upper bound on the size of the search space
	 */
	public double getUpperBoundOnSize()
	{
		//Default cannot be forbidden so there is at least 1 configuration
		//We don't need to worry about the edge case
		double configSpaceSize = 1;
		
		for(int i=0; i < this.numberOfParameters; i++)
		{
			
		
			int catSize = this.categoricalSize[i];

			if(catSize != INVALID_CATEGORICAL_SIZE)
			{
				configSpaceSize *= catSize;
			} else
			{
				
				NormalizedRange nr = this.contNormalizedRanges.get(this.authorativeParameterNameOrder.get(i));
				
				if(nr.isIntegerOnly())
				{
					configSpaceSize *= (nr.unnormalizeValue(1) - nr.unnormalizeValue(0) + 1);
				} else
				{
					return Double.POSITIVE_INFINITY;
				}
				
				
			}
		}
		
		return configSpaceSize;
	}
	
	

	/**
	 * Returns an lower bound on the size of the configuration space. 
	 * There are no guarantees how tight this lower bound might be, and in general it may get looser over time.
	 * You are only guaranteed that the number of actual configurations is HIGHER than this lower bound.
	 * <b>NOTE:</b> Search Subspaces do NOT lower the size of the bound because you can leave the subspace 
	 * 
	 * @return an lower bound on the size of the search space
	 */
	public double getLowerBoundOnSize()
	{
		//Default cannot be forbidden so there is at least 1 configuration
		//We don't need to worry about the edge case
		double configSpaceSize = 1;
		if(this.forbiddenParameterValuesList.size() > 0)
		{
			return 1;
		}
		
		for(int i=0; i < this.numberOfParameters; i++)
		{
		
			int catSize = this.categoricalSize[i];

			//if(this.condParents[i].length > 0)
			if (this.parameterDependencies.get(this.authorativeParameterNameOrder.get(i)) != null)
			{
				//Conditionals are ignored
				continue;
			}
					
			if(catSize != INVALID_CATEGORICAL_SIZE)
			{
				
				configSpaceSize *= catSize;
			} else
			{
				
				NormalizedRange nr = this.contNormalizedRanges.get(this.authorativeParameterNameOrder.get(i));
				
				if(nr.isIntegerOnly())
				{
					configSpaceSize *= (nr.unnormalizeValue(1) - nr.unnormalizeValue(0) + 1);
				} else
				{
					return Double.POSITIVE_INFINITY;
				}
			}
		}
		
		return configSpaceSize;
	}
	
	
	
	/**
	 * Generates a configuration with the corresponding valueArray.
	 * 
	 * @param valueArray value array representation of configuration
	 * @return ParamConfiguration object that represents the valueArray
	 */
	private ParameterConfiguration getConfigurationFromValueArray(double[] valueArray)
	{
		if(valueArray.length != categoricalSize.length)
		{
			throw new IllegalArgumentException("Value Array Length is not the right size " + valueArray.length + " vs " + categoricalSize.length);
		}
		return new ParameterConfiguration(this, valueArray.clone(), categoricalSize, parameterDomainContinuous, paramKeyIndexMap);
	}
	
	public ParameterConfiguration getParameterConfigurationFromString( String paramString, ParameterStringFormat f)
	{
		return getParameterConfigurationFromString(paramString, f, null);
	}

	public ParameterConfiguration getParameterConfigurationFromString( String paramString, ParameterStringFormat f, Random rand)
	{
		try 
		{
			
			ParameterConfiguration config;
			
			String trySpecialParamString = paramString.trim().toUpperCase();
			//If the string is DEFAULT, <DEFAULT>,RANDOM,<RANDOM> we generate the default or a random as needed
			
			if((trySpecialParamString.equals("DEFAULT") || trySpecialParamString.equals("<DEFAULT>")))
			{
				return this.getDefaultConfiguration();
			}
			
			if((trySpecialParamString.equals("RANDOM") || trySpecialParamString.equals("<RANDOM>")))
			{
				if(rand == null) throw new IllegalStateException("Cannot generate random configurations unless a random object is passed with us");	
				return this.getRandomParameterConfiguration(rand);
			}
			
			double[] valueArray;
			
			Set<String> namesSpecified = new HashSet<String>();
			
			ParameterConfiguration defaultConfig = this.getDefaultConfiguration();
			
			switch(f)
			{
				case NODB_SYNTAX_WITH_INDEX:
					paramString = paramString.replaceFirst("\\A\\d+:", "");
					//NOW IT'S A REGULAR NODB STRING
				case NODB_SYNTAX:
					valueArray = new double[numberOfParameters];
					config = new ParameterConfiguration(this, valueArray,categoricalSize, parameterDomainContinuous, paramKeyIndexMap);
					String tmpParamString = " " + paramString;
					String[] params = tmpParamString.split("\\s-");
					
						
					for(String param : params)
					{
						
						if(param.trim().equals("")) continue;
						String[] paramSplit = param.trim().split(" ");
						if(!paramSplit[1].trim().equals("NaN"))
						{
							config.put(paramSplit[0].trim(),paramSplit[1].replaceAll("'","").trim());
							namesSpecified.add(paramSplit[0].trim());
						}
					
						
					}
					
					
					
					break;
				case STATEFILE_SYNTAX_WITH_INDEX:
					paramString = paramString.replaceFirst("\\A\\d+:", "");
				case STATEFILE_SYNTAX:
					valueArray = new double[numberOfParameters];
					config = new ParameterConfiguration(this, valueArray,categoricalSize, parameterDomainContinuous, paramKeyIndexMap);
					
					tmpParamString = " " + paramString.replaceAll("'","");
					params = tmpParamString.split(",");
					for(String param : params)
					{
						if(param.trim().equals("")) continue;
						String[] paramSplit = param.trim().split("=");
						if(!paramSplit[1].trim().equals("NaN"))
						{
							config.put(paramSplit[0].trim(),paramSplit[1].trim());
							namesSpecified.add(paramSplit[0].trim());
						}
					}
					
					break;
				case ARRAY_STRING_SYNTAX:
					valueArray = new double[numberOfParameters];
					
					tmpParamString = paramString;
					if(paramString.trim().length() == 0)
					{
						config = new ParameterConfiguration(this, valueArray,categoricalSize, parameterDomainContinuous, paramKeyIndexMap);
						break;
					}
					
					params = tmpParamString.split(",");
					if(params.length != valueArray.length)
					{
						throw new IllegalArgumentException("Param String Value Array expected to be: " + valueArray.length + " but got a string of length " + paramString.length());
						
						
					}
					
					
					for(int i=0; i < valueArray.length; i++)
					{
						valueArray[i] = Double.valueOf(params[i]);
					}
					config = new ParameterConfiguration(this, valueArray,categoricalSize, parameterDomainContinuous, paramKeyIndexMap);
					namesSpecified.addAll(config.keySet());
					break;

				case SURROGATE_EXECUTOR:
					valueArray = new double[numberOfParameters];
					config = new ParameterConfiguration(this, valueArray,categoricalSize, parameterDomainContinuous, paramKeyIndexMap);
					
					if(paramString.trim().length() == 0)
					{
						break;
					}
					tmpParamString = paramString.trim().replaceAll("-P", "");
					
					
					params = tmpParamString.trim().split(" "); 
					
					
					for(int i=0; i < params.length; i++)
					{
						String[] param = params[i].split("=");
						
						if(param.length != 2)
						{
							throw new IllegalArgumentException("Param String could not parse portion of string " + paramString + " error occured while seperating: (" + params[i] + ")") ;
						} else
						{
							namesSpecified.add(param[0].trim());
							config.put(param[0].trim(), param[1].trim());
						}
					}
					
					if(!namesSpecified.equals(config.getActiveParameters()))
					{
						Set<String> missingButRequired = new HashSet<String>();
						Set<String> specifiedButNotActive = new HashSet<String>();
						missingButRequired.addAll(config.getActiveParameters());
						missingButRequired.removeAll(namesSpecified);
						
						specifiedButNotActive.addAll(namesSpecified);
						specifiedButNotActive.removeAll(config.getActiveParameters());

						throw new IllegalArgumentException("Param String specified some combination of inactive parameters and/or missed active parameters. \nRequired Parameters: " + config.getActiveParameters().size() + "\nSpecified Parameters: " + namesSpecified.size() + "\nRequired But Missing: " + missingButRequired.toString() + "\nSpecified But Not Required" + specifiedButNotActive.toString());
					}
					
					break;
				case NODB_OR_STATEFILE_SYNTAX:
					try {
						return getParameterConfigurationFromString(paramString, ParameterStringFormat.STATEFILE_SYNTAX, rand);
					} catch(RuntimeException e)
					{
						return getParameterConfigurationFromString(paramString, ParameterStringFormat.NODB_SYNTAX, rand);
					}
					
					
				default:
					throw new IllegalArgumentException("Parsing not implemented for String Format: " + f);
					
				
				
			}
			
			
			/**
			 * For all inactive parameters, set them to the default value
			 */
			Set<String> allParameters = new HashSet<String>(this.getParameterNames());
			
			allParameters.removeAll(config.getActiveParameters());
			for(String inactiveParameter : allParameters)
			{
				if(config.get(inactiveParameter) == null)
				{
					config.put(inactiveParameter, defaultConfig.get(inactiveParameter));
				}
			}
			
			
			Set<String> unSetActiveParameters = new TreeSet<String>(config.getActiveParameters());
			unSetActiveParameters.removeAll(namesSpecified);
			
			if(unSetActiveParameters.size() > 0)
			{
				throw new ParameterConfigurationStringMissingParameterException("Error processing Parameter Configuration String \""+ paramString+ "\" in format: "+ f + ". The string specified is missing one or more required parameters: " + unSetActiveParameters.toString());
			}

			return config;
		} catch(IllegalStateException e)
		{
			throw e;
		} catch(ParameterConfigurationStringFormatException e)
		{
			throw e;
		
		} catch(RuntimeException e )
		{
			throw new ParameterConfigurationStringFormatException("Error processing Parameter Configuration String \""+ paramString+ "\" in format: "+ f + " please check the arguments (and nested exception) and try again", e);
		}
		
		
		
	}
	
	public int[] getCategoricalSize()
	{
		return categoricalSize.clone();
	}


	/**
	 * Checks the array representation of a configuration to see if it is forbidden
	 * @param valueArray
	 * @return <code>true</code> if the valueArray is ultimately forbidden, <code>false</code> otherwise.
	 */
	public boolean isForbiddenParameterConfiguration(double[] valueArray) {
		
		
		/*
		 * Each value is Nx2 where the first is an index into the array, and the second is the 
		 * index of the categorical value.
		 */
		for(int[][] forbiddenParamValues : forbiddenParameterValuesList)
		{
			
			boolean match = true;
			for(int[] forbiddenParamValue : forbiddenParamValues)
			{
				//Value arrays are indexed by 1, and forbidden parameters are 0 indexed
				if(valueArray[forbiddenParamValue[0]] != forbiddenParamValue[1] + 1)
				{
					match = false;
					break;
				}
				
			}
			if(match)
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean isIntegerDouble(double d)
	{
		return (d - Math.floor(d) == 0);
	}
	
	/**
	 * @return <code>true</code> if the source of this ParamConfigurationSpace is a real file on disk. 
	 */
	public boolean hasRealParameterFile()
	{
		return hasRealParameterFile;
	}
	
	public static final String SINGLETON_ABSOLUTE_NAME = "<--[SINGLETON SPACE]-->";
	public static final String NULL_ABSOLUTE_NAME ="<--[NULL SPACE]-->";
	/**
	 * Returns a configuration space with a single parameter and single value
	 * 
	 * @return a valid configuration space that is effectively empty
	 */
	public static ParameterConfigurationSpace getSingletonConfigurationSpace()
	{
		return new ParameterConfigurationSpace(new StringReader("singleton c { singleton } [singleton]"),SINGLETON_ABSOLUTE_NAME);
	}
	
	public static ParameterConfigurationSpace getNullConfigurationSpace() {
		return new ParameterConfigurationSpace(new StringReader(""),NULL_ABSOLUTE_NAME);
	}
	

	void setValueInArray(double[] valueArray, String key, String newValue) {
		
		/* We find the index into the valueArray from paramKeyIndexMap,
		 * then we find the new value to set from it's position in the getValuesMap() for the key. 
		 * NOTE: i = 1 since the valueArray numbers elements from 1
		 */
		
		
		Integer index = paramKeyIndexMap.get(key);
		if(index == null)
		{
			throw new IllegalArgumentException("This key does not exist in the Parameter Space: " + key);

		}
		
		if(newValue == null)
		{
			valueArray[index] = Double.NaN;
		}
		else if(parameterDomainContinuous[index])
		{
			NormalizedRange nr = getNormalizedRangeMap().get(key);
			valueArray[index] = nr.normalizeValue(nr.unnormalizeValue(nr.normalizeValue(Double.valueOf(newValue))));
			
		} else
		{
			List<String> inOrderValues = getValuesMap().get(key);
			int i=1;		
			boolean valueFound = false;
			
			
			for(String possibleValue : inOrderValues)
			{
				if (possibleValue.equals(newValue))
				{
					valueArray[index] = i;
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
		
	}
	
	
	private static class RandomAdapter
	{
		
		private MersenneTwisterFast fastRand;
		private Random rand;

		RandomAdapter(Random r)
		{
			this.rand = r;
			this.fastRand = null;
		}
		
		RandomAdapter(MersenneTwisterFast fastRand)
		{
			this.rand = null;
			this.fastRand =  fastRand;
		}
		
		public int nextInt(int n)
		{
			if(fastRand != null)
			{
				return fastRand.nextInt(n); 
			} else
			{
				return rand.nextInt(n);
			}
			
		}
		
		public double nextDouble()
		{
			if(fastRand != null)
			{
				return fastRand.nextDouble();
			} else
			{
				return rand.nextDouble();
			}
		}
	
	}

	/**
	 * Returns the contents of the PCS File that created this object
	 * @return
	 */
	public String getPCSFile() {
		return this.pcsFile;
	}

	/**
	 * Returns the search subspace of the PCS File
	 * @return
	 */
	public Map<String, String> getSearchSubspace()
	{
		return this.searchSubspace;
	}


	/**
	 * Returns true if there are new forbidden lines, false otherwise
	 * @return
	 */
	private final boolean newForbiddenLinesPresent;
	
	boolean hasNewForbidden() {
		// TODO Auto-generated method stub
		return newForbiddenLinesPresent;
	}

	
	private static final class PCSSerializationProxy implements Serializable
	{

		private final String pcsJSON;
		
		private static final long serialVersionUID = 21585218521L;
		
		public PCSSerializationProxy(String json)
		{
			this.pcsJSON = json;
		}
		
		
		private final Object readResolve() throws ObjectStreamException
		{
			JSONConverter<ParameterConfigurationSpace> json = new JSONConverter<ParameterConfigurationSpace>() {} ;
			
			//String jsonText = json.getJSON(pcsJSON);
			//System.out.println(jsonText);

			return json.getObject(pcsJSON);
		}
		
	}
	

	
	private final Object writeReplace() throws ObjectStreamException
	{
		JSONConverter<ParameterConfigurationSpace> json = new JSONConverter<ParameterConfigurationSpace>() {} ;
		
		String jsonText = json.getJSON(this);
		
		return new PCSSerializationProxy(jsonText);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		throw new InvalidObjectException("This object cannot be deserialized, should have used: " + PCSSerializationProxy.class.getCanonicalName());
	}

	
}

