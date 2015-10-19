package ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import ca.ubc.cs.beta.aeatk.misc.string.SplitQuotedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ca.ubc.cs.beta.aeatk.json.serializers.AlgorithmExecutionConfigurationJson;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
/**
 * Immutable Object contains all the information related to executing a target algorithm run
 * @author seramage
 *
 */
@SuppressWarnings("unused")
@JsonSerialize(using=AlgorithmExecutionConfigurationJson.AlgorithmExecutionConfigSerializer.class)
@JsonDeserialize(using=AlgorithmExecutionConfigurationJson.AlgorithmExecutionConfigDeserializer.class)
public class AlgorithmExecutionConfiguration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3L;
	

	private final String algorithmExecutionDirectory;
	private final ParameterConfigurationSpace paramFile;
		private final boolean deterministicAlgorithm;

	private final double cutoffTime; 

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Map<String, String> taeContext;

	/**
	 * Stores the executable in the first element, and the remaining arguments
	 * in subsequent arguments.
	 *
	 * This may seem odd but this is the format Runtime.exec() takes
	 *
	 * @see java.lang.Runtime#exec(String[])
	 */
	private final List<String> executableAndArguments;


	@Deprecated
	/**
	 * @deprecated Please pass executionString as a list
	 * @see AlgorithmExecutionConfiguration#AlgorithmExecutionConfiguration(List, String, ParameterConfigurationSpace, boolean, double)
	 *
	 */
	public AlgorithmExecutionConfiguration(String algorithmExecutionString, String algorithmExecutionDirectory,
			ParameterConfigurationSpace paramFile, boolean executeOnCluster, boolean deterministicAlgorithm, double cutoffTime) {
		this(algorithmExecutionString, algorithmExecutionDirectory, paramFile, deterministicAlgorithm, cutoffTime, Collections.EMPTY_MAP);
		
	}


	/**
	 * @deprecated Please pass executionString as a list
	 * @see AlgorithmExecutionConfiguration#AlgorithmExecutionConfiguration(List, String, ParameterConfigurationSpace, boolean, double, Map)
	 */
	@Deprecated
	public AlgorithmExecutionConfiguration(String algorithmExecutionString , String algorithmExecutionDirectory, ParameterConfigurationSpace paramFile, boolean deterministicAlgorithm, double cutoffTime, Map<String, String> taeContext) {

		this.executableAndArguments = Arrays.asList(SplitQuotedString.splitQuotedString(algorithmExecutionString));
		this.algorithmExecutionDirectory = algorithmExecutionDirectory;
		this.paramFile = paramFile;
		
		this.deterministicAlgorithm = deterministicAlgorithm;
		if(cutoffTime < 0)
		{
			throw new IllegalArgumentException("Cutoff time must be greater than zero");
		}
		
		if(cutoffTime == 0)
		{
			log.warn("Cutoff time should be greater than zero");
		}
		this.cutoffTime = cutoffTime;
		
		this.taeContext = new TreeMap<String, String>(taeContext);
	}


	public AlgorithmExecutionConfiguration(List<String> arguments, String algorithmExecutionDirectory, ParameterConfigurationSpace paramFile, boolean deterministicAlgorithm, double cutoffTime)
	{
		this(arguments.get(0), arguments.subList(1,arguments.size()), algorithmExecutionDirectory, paramFile,deterministicAlgorithm,cutoffTime,Collections.EMPTY_MAP);
	}


	public AlgorithmExecutionConfiguration(List<String> arguments, String algorithmExecutionDirectory, ParameterConfigurationSpace paramFile, boolean deterministicAlgorithm, double cutoffTime, Map<String, String> taeContext)
	{
		this(arguments.get(0), arguments.subList(1,arguments.size()), algorithmExecutionDirectory, paramFile,deterministicAlgorithm,cutoffTime,taeContext);
	}

	public AlgorithmExecutionConfiguration(String executable, List<String> arguments, String algorithmExecutionDirectory, ParameterConfigurationSpace paramFile, boolean deterministicAlgorithm, double cutoffTime)
	{
		this(executable,arguments,algorithmExecutionDirectory,paramFile,deterministicAlgorithm,cutoffTime,Collections.EMPTY_MAP);
	}


	public AlgorithmExecutionConfiguration(String executable, List<String> arguments, String algorithmExecutionDirectory, ParameterConfigurationSpace paramFile, boolean deterministicAlgorithm, double cutoffTime, Map<String, String> taeContext)
	{
		this.algorithmExecutionDirectory = algorithmExecutionDirectory;
		this.paramFile = paramFile;

		this.deterministicAlgorithm = deterministicAlgorithm;
		if(cutoffTime < 0)
		{
			throw new IllegalArgumentException("Cutoff time must be greater than zero");
		}

		if(cutoffTime == 0)
		{
			log.warn("Cutoff time should be greater than zero");
		}
		this.cutoffTime = cutoffTime;

		this.taeContext = new TreeMap<String, String>(taeContext);

		List<String> executableAndArguments = new ArrayList<>(arguments.size()+1);
		executableAndArguments.add(executable);
		executableAndArguments.addAll(arguments);

		this.executableAndArguments = Collections.unmodifiableList(executableAndArguments);
	}



	private final AtomicReference<String> executableString = new AtomicReference<>();
	/**
	 * @deprecated this method is poorly named and really returns the execution string of the executable and all it's arguments. This method will be replaced eventually
	 */
	@Deprecated
	public String getAlgorithmExecutable() {

		String executableString = this.executableString.get();

		if(executableString != null)
		{
			return executableString;
		}

		StringBuilder sb = new StringBuilder();

		for(String s : this.executableAndArguments)
		{
			sb.append(s).append(" ");
		}

		String result = sb.toString().trim();

		this.executableString.set(result);
		return result;
	}

	/**
	 * @return the executable
	 */
	public String getExecutable() {
		return executableAndArguments.get(0);
	}

	public List<String> getArguments()
	{
		return executableAndArguments.subList(0,executableAndArguments.size());
	}

	public List<String> getExecutableAndArguments()
	{
		return executableAndArguments;
	}

	public String getAlgorithmExecutionDirectory() {
		return algorithmExecutionDirectory;
	}

	public ParameterConfigurationSpace getParameterConfigurationSpace() {
		return paramFile;
	}

	public boolean isDeterministicAlgorithm()
	{
		return deterministicAlgorithm;
	}
	
	/**
	 * Additional context information necessary to execute runs, this is TAE dependent.
	 * @return
	 */
	public Map<String, String> getTargetAlgorithmExecutionContext()
	{
		return Collections.unmodifiableMap(this.taeContext);
	}
	
	public int hashCode()
	{
		return executableAndArguments.hashCode() ^ algorithmExecutionDirectory.hashCode() ^ paramFile.hashCode() ^ (deterministicAlgorithm ? 0 : 1) ^ taeContext.hashCode();
	}
	
	public String toString()
	{
		return "algoExec:" + this.getAlgorithmExecutable() + "\nAlgorithmExecutionDirectory:" + algorithmExecutionDirectory + "\n"+paramFile +  "\nDetermininstic:" + deterministicAlgorithm + "\nID:" + myID + " MapSize:" + taeContext.size();
	}
	
	public boolean equals(Object o)
	{ 
		if(this == o) return true;
		if (o instanceof AlgorithmExecutionConfiguration)
		{
			AlgorithmExecutionConfiguration co = (AlgorithmExecutionConfiguration) o;
			return (co.executableAndArguments.equals(executableAndArguments) && co.algorithmExecutionDirectory.equals(algorithmExecutionDirectory) && co.paramFile.equals(paramFile)) && co.deterministicAlgorithm == deterministicAlgorithm  && co.taeContext.equals(taeContext);
		} 
		return false;
	}
	
	/**
	 * Returns the maximum cutoff time
	 * @return maximum cutoff time for the algorithm
	 */
	public double getAlgorithmMaximumCutoffTime() {
		return cutoffTime;
	}
	

	private static final AtomicInteger idPool = new AtomicInteger(0);

	private int myID = idPool.incrementAndGet();	
	
	/**
	 * Friendly IDs are just unique numbers that identify this configuration for logging purposes
	 * you should <b>NEVER</b> rely on this for programatic purposes.
	 * 
	 * @return unique id for this object
	 */
	public int getFriendlyID() {
		return myID;
	}

	public String getFriendlyIDHex()
	{
		String hex = Integer.toHexString(getFriendlyID());
		
		StringBuilder sb = new StringBuilder("0x");
		while(hex.length() + sb.length() < 6)
		{
			sb.append("0");
		}
		sb.append(hex.toUpperCase());
		return sb.toString();
	}

}
