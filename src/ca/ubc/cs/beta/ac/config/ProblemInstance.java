package ca.ubc.cs.beta.ac.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Immutable Class that represents an problem instance for a target algorithm
 * @author seramage
 *
 * 
 */
public class ProblemInstance implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2077458754749675377L;

	private final String instanceName;
	
	private final int instanceId;

	private final double[] featuresDouble;

	private final String instanceSpecificInformation;
	
	/**
	 * Unmodifiable map containing our features
	 * This map MUST have a consistent iteration order
	 * 
	 */
	private final Map<String,Double> featuresMap;
	
	
	public ProblemInstance(String instanceName)
	{
		if (instanceName == null)
		{
			throw new IllegalArgumentException("Instance cannot be null");
		}
		this.instanceName = instanceName;
		this.instanceId = 0;
		this.featuresMap = Collections.emptyMap();
		this.featuresDouble = new double[0];
		this.instanceSpecificInformation = null;
	}
	
	public ProblemInstance(String instanceName, int id)
	{
		if (instanceName == null)
		{
			throw new IllegalArgumentException("Instance cannot be null");
		}
		this.instanceName = instanceName;
		this.instanceId = id;
		this.featuresMap = Collections.emptyMap();
		this.featuresDouble = new double[0];
		this.instanceSpecificInformation = null;
		
	}
	public ProblemInstance(String instanceName, int id, Map<String, Double> features)
	{
		if (instanceName == null)
		{
			throw new IllegalArgumentException("Instance cannot be null");
		}
		this.instanceName = instanceName;
		this.instanceId = id;
		
		this.featuresMap = features;
		this.featuresDouble = new double[features.size()];
	
		int i=0;
		for(Entry<String, Double> ent : features.entrySet())
		{
			featuresDouble[i++] = ent.getValue();
		}
		this.instanceSpecificInformation = null;
		
	}
	
	public ProblemInstance(String instanceName, int id, Map<String, Double> features, String instanceSpecificInformation)
	{
		if (instanceName == null)
		{
			throw new IllegalArgumentException("Instance cannot be null");
		}
		this.instanceName = instanceName;
		this.instanceId = id;
		
		this.featuresMap = features;
		this.featuresDouble = new double[features.size()];
	
		int i=0;
		for(Entry<String, Double> ent : features.entrySet())
		{
			featuresDouble[i++] = ent.getValue();
		}
		this.instanceSpecificInformation = instanceSpecificInformation;
		
	}
	
	public String getInstanceName()
	{
		return this.instanceName;
	}
	public boolean equals(Object o)
	{
		if(o instanceof ProblemInstance)
		{
			ProblemInstance ai = (ProblemInstance) o;
			return instanceName.equals(ai.instanceName);
		}
		return false;
	}
	
	public int hashCode()
	{
		return instanceName.hashCode();
	}
	
	public String toString()
	{
		if(instanceId != 0)
		{
			return "Instance(" + instanceId + "):"+instanceName;
		} else
		{
			return "Instance:"+instanceName;
		}
	}

	@Deprecated
	public int getInstanceID() {
		return instanceId;
	}

	
	public Map<String, Double> getFeatures()
	{
		return featuresMap;
	}
	
	
	public double[] getFeaturesDouble()
	{
		return featuresDouble.clone();
	}

	public String getInstanceSpecificInformation() {

		return instanceSpecificInformation;
	}
}
