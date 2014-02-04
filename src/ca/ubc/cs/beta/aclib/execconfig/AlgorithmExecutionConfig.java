package ca.ubc.cs.beta.aclib.execconfig;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;

/**
 * Immutable Object contains all the information related to executing a target algorithm run
 * @author seramage
 *
 */
public class AlgorithmExecutionConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1009816326679512474L;
	
	private final String algorithmExecutable;
	private final String algorithmExecutionDirectory;
	private final ParamConfigurationSpace paramFile;
	private final boolean executeOnCluster;
	private final boolean deterministicAlgorithm;

	private final double cutoffTime; 

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Map<String, String> taeContext;
	
	public AlgorithmExecutionConfig(String algorithmExecutable, String algorithmExecutionDirectory,
			ParamConfigurationSpace paramFile, boolean executeOnCluster, boolean deterministicAlgorithm, double cutoffTime) {
		this(algorithmExecutable, algorithmExecutionDirectory, paramFile,executeOnCluster, deterministicAlgorithm, cutoffTime, Collections.EMPTY_MAP);
		
	}
	

	public AlgorithmExecutionConfig(String algorithmExecutable, String algorithmExecutionDirectory,	ParamConfigurationSpace paramFile, boolean executeOnCluster, boolean deterministicAlgorithm, double cutoffTime, Map<String, String> taeContext) {
		this.algorithmExecutable = algorithmExecutable;
		this.algorithmExecutionDirectory = algorithmExecutionDirectory;
		this.paramFile = paramFile;
		this.executeOnCluster = executeOnCluster;
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

	public String getAlgorithmExecutable() {
		return algorithmExecutable;
	}

	public String getAlgorithmExecutionDirectory() {
		return algorithmExecutionDirectory;
	}

	public ParamConfigurationSpace getParamFile() {
		return paramFile;
	}

	@Deprecated
	/**
	 * @deprecated this really never did anything and will be removed at some point
	 */
	public boolean isExecuteOnCluster() {
		return executeOnCluster;
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
		return algorithmExecutable.hashCode() ^ algorithmExecutionDirectory.hashCode() ^ paramFile.hashCode() ^ (executeOnCluster ? 0 : 1) ^ (deterministicAlgorithm ? 0 : 1);
	}
	
	public String toString()
	{
		return "algoExec:" + algorithmExecutable + "\nAlgorithmExecutionDirectory:" + algorithmExecutionDirectory + "\n"+paramFile + "\n Cluster:"+executeOnCluster+ "\nDetermininstic:" + deterministicAlgorithm + "\nID:" + myID;
	}
	
	public boolean equals(Object o)
	{ 
		if(this == o) return true;
		if (o instanceof AlgorithmExecutionConfig)
		{
			AlgorithmExecutionConfig co = (AlgorithmExecutionConfig) o;
			return (co.algorithmExecutable.equals(algorithmExecutable) && co.algorithmExecutionDirectory.equals(algorithmExecutionDirectory) && (co.executeOnCluster == executeOnCluster) && co.paramFile.equals(paramFile)) && co.deterministicAlgorithm == deterministicAlgorithm ;
		} 
		return false;
	}
	
	/**
	 * Returns the maximum cutoff time
	 * @return maximum cutoff time for the algorithm
	 */
	public double getAlgorithmCutoffTime() {
		return cutoffTime;
	}
	
	
	public final static String MAGIC_VALUE_ALGORITHM_EXECUTABLE_PREFIX = "Who am I, Alan Turing?...also from X-Men?";
	
	
	
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
