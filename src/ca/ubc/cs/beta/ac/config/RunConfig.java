package ca.ubc.cs.beta.ac.config;

import java.io.Serializable;

import ca.ubc.cs.beta.configspace.ParamConfiguration;
import ca.ubc.cs.beta.probleminstance.ProblemInstanceSeedPair;

/**
 * Immutable Class that stores informations for a specific algorithm run
 * @author seramage
 *
 */
public class RunConfig implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7749039021874017859L;
	private final ProblemInstanceSeedPair aisp;
	private final double cutoffTime;
	private final ParamConfiguration params;
	private final boolean cutoffLessThanMax;

	
	public RunConfig(ProblemInstanceSeedPair aisp, double cutoffTime, ParamConfiguration params)
	{
		if(aisp == null)
		{
			throw new IllegalArgumentException("AlgorithmInstanceSeedPair cannot be null");
		}
		
		if(params == null)
		{
			throw new IllegalArgumentException("Params cannot be null");
		}
		
		if(cutoffTime  < 0)
		{
			throw new IllegalArgumentException("Cutoff time must be non-negative positive");
		}
		this.aisp = aisp;
		this.cutoffTime = cutoffTime;
		this.params = params;
		this.cutoffLessThanMax = false;
		
		
		
	}
	
	public RunConfig(ProblemInstanceSeedPair aisp, double cutoffTime, ParamConfiguration params, boolean cutoffLessThanMax)
	{
		if(aisp == null)
		{
			throw new IllegalArgumentException("AlgorithmInstanceSeedPair Name cannot be null");
		}
		
		if(cutoffTime  < 0)
		{
			throw new IllegalArgumentException("Cutoff time must be non-negative positive");
		}

		if(params == null)
		{
			throw new IllegalArgumentException("ParamString cannot be null");
		}
		
		this.aisp = aisp;
		this.cutoffTime = cutoffTime;
		this.params = params;
		this.cutoffLessThanMax = cutoffLessThanMax;
	}
	


	
	public ProblemInstanceSeedPair getAlgorithmInstanceSeedPair()
	{
		return aisp;
	}

	public double getCutoffTime() {
		return cutoffTime;
	}

	public ParamConfiguration getParamConfiguration()
	{
		
		return new ParamConfiguration(params);
	}
	
	public boolean hasCutoffLessThanMax()
	{
		return cutoffLessThanMax;
	}
	
	public boolean equals(Object o)
	{
		if (o instanceof RunConfig)
		{
			RunConfig oar = (RunConfig) o;
			return (aisp.equals(oar.aisp)) && cutoffTime == oar.cutoffTime && params.equals(oar.params);
		} else
		{
			return false;
		}
	} 
	
	
	public int hashCode()
	{
		//System.out.println("AISP:" + aisp.hashCode());
		//System.out.println("Params:" + params.hashCode());
		
		/**
		 * Due to adaptive Capping and floating point issues, we don't consider the cutofftime as part of the hashcode.
		 * Theoretically this may cause certain performance issues in hash based collections
		 * however it is hoped that the number of re-runs with increasing cap times is small.
		 */
		return (int) ( (aisp.hashCode())^ params.hashCode());
		
		//System.out.println("CutOff Time:" + (Double.doubleToLongBits(cutoffTime) >>> 32));
		//return (int) ( (aisp.hashCode())^ (int) (Double.doubleToLongBits(cutoffTime) >>> 32) ^ ((int) Double.doubleToLongBits(cutoffTime))^ params.hashCode());
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Instance&Seed:\n").append(aisp.toString()).append("\nCutoffTime: ").append(cutoffTime).append("\nParamString: ").append(params.toString());
		return sb.toString();
		
	}
	/**
	 * Returns a nice friendly string with numbers telling you what instance, config, seed were called
	 * @return
	 */
	public String getFriendlyRunInfo()
	{
		int instID = this.getAlgorithmInstanceSeedPair().getInstance().getInstanceID();
		long seed = this.getAlgorithmInstanceSeedPair().getSeed();
		int confID = this.params.getFriendlyID();
		return "Run for Instance (" + instID + ") Config (" +confID + ") Seed: (" + seed +")";   
	}
	
	
	

}
