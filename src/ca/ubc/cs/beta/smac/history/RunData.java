package ca.ubc.cs.beta.smac.history;

import ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun;
/**
 * Value object that holds useful book keeping information about a run
 * @author seramage
 */
public class RunData 
{
	

	private final int iteration;
	private final int thetaIdx;
	private final int instanceidx;
	private final AlgorithmRun run;
	private double responseValue;

	public RunData(int iteration, int thetaIdx, int instanceIdx, AlgorithmRun run, double responseValue)
	{
		this.iteration=iteration;
		this.thetaIdx = thetaIdx;
		this.instanceidx = instanceIdx;
		this.run = run;
		this.responseValue = responseValue;
	}
	
	public int getIteration() {
		return iteration;
	}

	public int getThetaIdx() {
		return thetaIdx;
	}

	public int getInstanceidx() {
		return instanceidx;
	}

	public AlgorithmRun getRun() {
		return run;
	}

	public double getResponseValue() {
		return responseValue;
	}
	
	public int hashCode()
	{
		return  run.hashCode();
	}
	public boolean equals(Object o)
	{
		if(o instanceof RunData)
		{
			return ((RunData) o).getRun().equals(run);
		} else
		{
			return false;
		}
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[").append("(").append(getIteration()).append("(");
		sb.append(getThetaIdx()).append(",").append(getInstanceidx()).append("=>").append(getResponseValue()).append("]");
		return sb.toString();
	}
}
