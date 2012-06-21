package ca.ubc.cs.beta.smac;

import ca.ubc.cs.beta.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun;

public enum RunObjective {
	RUNTIME,
	RUNLENGTH,
	APPROX,
	SPEEDUP,
	QUALITY;
	
	public double getObjective(AlgorithmRun r)
	{
		String instanceInfo;
		
		switch(this)
		{
		case RUNTIME:
			//We always count a runtime as taking atleast 0.1 seconds
			//System.err.println("RunObjective I am not rounding up to 0.1 seconds");
			//Allegedly this use to occur on dorun.m line 25
			return (r.getRuntime());
			
		case RUNLENGTH:
			return r.getRunLength();
		case APPROX:
			
			instanceInfo = r.getInstanceRunConfig().getAlgorithmInstanceSeedPair().getInstance().getInstanceSpecificInformation();
			double optimalQuality = Double.parseDouble(instanceInfo);
			return 1-(optimalQuality / r.getQuality());
			
		case SPEEDUP:
			instanceInfo = r.getInstanceRunConfig().getAlgorithmInstanceSeedPair().getInstance().getInstanceSpecificInformation();
			double originalSpeed = Double.parseDouble(instanceInfo);
			return originalSpeed / r.getRuntime();

		case QUALITY:
			return r.getQuality();
			
		default:
			throw new UnsupportedOperationException(this.toString() + " Run Objective Not Implemented");

		}
	}
	
	public void validateInstanceSpecificInformation(ProblemInstance pi)
	{
		switch(this)
		{
		case RUNTIME:
		case QUALITY:
		case RUNLENGTH:
			return;
			
		case APPROX:
		case SPEEDUP:
			String instanceSpecificInformation = pi.getInstanceSpecificInformation();
			try {
				Double.parseDouble(instanceSpecificInformation);
			} catch(NumberFormatException e)
			{
				throw new IllegalArgumentException(pi.getInstanceName() + " has an invalid instance specific information (expected integer)");
			}
			return;
			
		default:
			throw new UnsupportedOperationException(this.toString() + " Run Objective Not Implemented");
		}
		
	}
}
