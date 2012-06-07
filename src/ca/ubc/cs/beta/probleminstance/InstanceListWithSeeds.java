package ca.ubc.cs.beta.probleminstance;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.ubc.cs.beta.ac.config.ProblemInstance;

public class InstanceListWithSeeds {
	private final InstanceSeedGenerator seedGen;
	
	private final List<ProblemInstance> instances;
	
	private final List<String> instanceNames;

	private final Map<String, String> instanceSpecificInfo;
	
	
	public InstanceListWithSeeds(InstanceSeedGenerator seedGen, List<ProblemInstance> instances)
	{
		this.seedGen = seedGen;
		this.instances = instances;
		this.instanceNames = Collections.emptyList();
		this.instanceSpecificInfo = Collections.emptyMap();
	}
	
	InstanceListWithSeeds(InstanceSeedGenerator seedGen, List<ProblemInstance> instances, List<String> instanceNames)
	{
		this.seedGen = seedGen;
		this.instanceNames = instanceNames;
		this.instances = ((instances == null) ? new LinkedList<ProblemInstance>() : instances);
		this.instanceSpecificInfo = Collections.emptyMap();
	}

	

	InstanceListWithSeeds(InstanceSeedGenerator seedGen, List<ProblemInstance> instances,
			List<String> instanceNames,
			Map<String, String> instanceSpecificInfo) {
		this.seedGen = seedGen;
		this.instanceNames = instanceNames;
		this.instances = ((instances == null) ? new LinkedList<ProblemInstance>() : instances);
		this.instanceSpecificInfo = instanceSpecificInfo;
	}

	public InstanceSeedGenerator getSeedGen() {
		return seedGen;
	}

	public List<ProblemInstance> getInstances() {
		return instances;
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
