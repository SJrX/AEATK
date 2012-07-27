package ca.ubc.cs.beta.aclib.objectives;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
/**
 * Enumeration listing the various run objectives (converts an {@link AlgorithmRun} into a response value)
 * @author sjr
 *
 */
public enum RunObjective {
	/**
	 * Uses the runtime
	 */
	RUNTIME,
	/**
	 * Use the runlength
	 */
	RUNLENGTH,
	/**
	 * Not sure what this does (the manual does say) [NOT IMPLEMENTED]
	 */
	APPROX,
	/**
	 * Use the speedup (NOT IMPLEMENTED)
	 */
	SPEEDUP,
	
	/**
	 * Use the quality
	 */
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
			if(!r.getRunConfig().hasCutoffLessThanMax())
			{
				switch(r.getRunResult())
				{
					case CRASHED:
					case TIMEOUT:
					
						return r.getExecutionConfig().getAlgorithmCutoffTime();
					default:
					 
				}
			}
			
			return (r.getRuntime());
			
		case RUNLENGTH:
			return r.getRunLength();
		case APPROX:
			
			instanceInfo = r.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation();
			double optimalQuality = Double.parseDouble(instanceInfo);
			return 1-(optimalQuality / r.getQuality());
			
		case SPEEDUP:
			instanceInfo = r.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation();
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
