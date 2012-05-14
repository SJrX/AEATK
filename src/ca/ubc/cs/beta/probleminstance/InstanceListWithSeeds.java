package ca.ubc.cs.beta.probleminstance;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ca.ubc.cs.beta.ac.config.ProblemInstance;

public class InstanceListWithSeeds {
	private final InstanceSeedGenerator seedGen;
	
	private final List<ProblemInstance> instances;
	
	private final List<String> instanceNames;
	
	
	public InstanceListWithSeeds(InstanceSeedGenerator seedGen, List<ProblemInstance> instances)
	{
		this.seedGen = seedGen;
		this.instances = instances;
		this.instanceNames = Collections.emptyList();
	}
	
	public InstanceListWithSeeds(InstanceSeedGenerator seedGen, List<ProblemInstance> instances, List<String> instanceNames)
	{
		this.seedGen = seedGen;
		this.instanceNames = instanceNames;
		this.instances = ((instances == null) ? new LinkedList<ProblemInstance>() : instances);
	}

	

	public InstanceSeedGenerator getSeedGen() {
		return seedGen;
	}

	public List<ProblemInstance> getInstances() {
		return instances;
	}
	
	public List<String> getInstancesByName()
	{
		return instanceNames;
	}
	
	
}
