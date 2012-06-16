package ca.ubc.cs.beta.probleminstance;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.ubc.cs.beta.ac.config.ProblemInstance;
import ca.ubc.cs.beta.seedgenerator.InstanceSeedGenerator;

public class InstanceListWithSeeds {
	private final InstanceSeedGenerator seedGen;
	
	private final List<ProblemInstance> instances;
	
	private final List<String> instanceNames;

	private final Map<String, String> instanceSpecificInfo;

	private final List<ProblemInstance> instancesFromFeatures;
	
	
	public InstanceListWithSeeds(InstanceSeedGenerator seedGen, List<ProblemInstance> instances)
	{
		this.seedGen = seedGen;
		this.instances = instances;
		this.instanceNames = Collections.emptyList();
		this.instanceSpecificInfo = Collections.emptyMap();
		this.instancesFromFeatures = Collections.emptyList();
	}
	
	public InstanceListWithSeeds(InstanceSeedGenerator seedGen, List<ProblemInstance> instances, List<ProblemInstance> instancesByFeatures)
	{
		this.seedGen = seedGen;
		this.instances = instances;
		this.instanceNames = Collections.emptyList();
		this.instanceSpecificInfo = Collections.emptyMap();
		this.instancesFromFeatures = instancesByFeatures;
	}
	/*
	InstanceListWithSeeds(InstanceSeedGenerator seedGen, List<ProblemInstance> instances, List<String> instanceNames)
	{
		this.seedGen = seedGen;
		this.instanceNames = instanceNames;
		this.instances = ((instances == null) ? new LinkedList<ProblemInstance>() : instances);
		this.instanceSpecificInfo = Collections.emptyMap();
	}*/

	

	InstanceListWithSeeds(InstanceSeedGenerator seedGen, List<ProblemInstance> instances,
			List<String> instanceNames,
			Map<String, String> instanceSpecificInfo) {
		this.seedGen = seedGen;
		this.instanceNames = instanceNames;
		this.instances = ((instances == null) ? new LinkedList<ProblemInstance>() : instances);
		this.instanceSpecificInfo = instanceSpecificInfo;
		this.instancesFromFeatures = Collections.emptyList();
	}
	
	

	public InstanceSeedGenerator getSeedGen() {
		return seedGen;
	}

	public List<ProblemInstance> getInstances() {
		return instances;
	}
	
	/**
	 * Returns a list of Problem instances that was determined from the feature file.
	 * 
	 * NOTE: If the feature file lists both testing and training instances this will have all of those instances
	 * 
	 * @return a list of Problem instances that was determined from the feature file.
	 */
	public List<ProblemInstance> getInstancesFromFeatures()
	{
		return instancesFromFeatures;
	}
	
	List<String> getInstancesByName()
	{
		return instanceNames;
	}
	
	Map<String,String> getInstanceSpecificInfo()
	{
		return instanceSpecificInfo;
	}
	
}
