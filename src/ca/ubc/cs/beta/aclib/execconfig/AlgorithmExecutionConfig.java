package ca.ubc.cs.beta.aclib.execconfig;

import java.io.Serializable;

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
	
	
	public AlgorithmExecutionConfig(String algorithmExecutable, String algorithmExecutionDirectory,
			ParamConfigurationSpace paramFile, boolean executeOnCluster, boolean deterministicAlgorithm) {
		this.algorithmExecutable = algorithmExecutable;
		this.algorithmExecutionDirectory = algorithmExecutionDirectory;
		this.paramFile = paramFile;
		this.executeOnCluster = executeOnCluster;
		this.deterministicAlgorithm = deterministicAlgorithm;
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

	public boolean isExecuteOnCluster() {
		return executeOnCluster;
	}
	
	public boolean isDeterministicAlgorithm()
	{
		return deterministicAlgorithm;
	}
	
	public int hashCode()
	{
		return algorithmExecutable.hashCode() ^ algorithmExecutionDirectory.hashCode() ^ paramFile.hashCode() ^ (executeOnCluster ? 0 : 1);
	}
	
	public String toString()
	{
		return "algoExec:" + algorithmExecutable + "\nAlgorithmExecutionDirectory:" + algorithmExecutionDirectory + "\n"+paramFile + "\n"+executeOnCluster+"\n";
	}
	
	public boolean equals(Object o)
	{ 
		
		if (o instanceof AlgorithmExecutionConfig)
		{
			AlgorithmExecutionConfig co = (AlgorithmExecutionConfig) o;
			return (co.algorithmExecutable.equals(algorithmExecutable) && co.algorithmExecutionDirectory.equals(algorithmExecutionDirectory) && (co.executeOnCluster == executeOnCluster) && co.paramFile.equals(paramFile));
		} 
		return false;
	}
	
	
	

}
