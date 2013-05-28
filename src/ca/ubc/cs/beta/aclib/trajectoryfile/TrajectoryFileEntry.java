package ca.ubc.cs.beta.aclib.trajectoryfile;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;

/**
 * Value object corresponding roughly to the significant rows in a trajectory file entry;
 * @author Steve Ramage 
 *
 */
public class TrajectoryFileEntry implements Comparable<TrajectoryFileEntry>
{
	private final ParamConfiguration config;
	private final double empericalPerformance;
	private final double acOverhead;
	private final double tunerTime;
	private final double wallTime;
	
	/**
	 * Creates a new Trajectory File Entry
	 * @param config 					configuration to log
	 * @param tunerTime 				tuner time when incumbent selected
	 * @param walltime					Wallclock time of entry
	 * @param empericalPerformance  	emperical performance of incumbent
	 * @param acOverhead 				overhead time of automatic configurator
	 */
	public TrajectoryFileEntry(ParamConfiguration config, double tunerTime, double walltime , double empericalPerformance, double acOverhead)
	{
		this.config = config;
		this.empericalPerformance = empericalPerformance;
		this.acOverhead = acOverhead;
		this.wallTime = walltime;
		this.tunerTime = tunerTime;
	}
	

	public ParamConfiguration getConfiguration()
	{
		return config;
	}
	
	public double getEmpericalPerformance()
	{
		return empericalPerformance;
	}
	
	public double getACOverhead()
	{
		return acOverhead;
	}
	
	public double getTunerTime()
	{
		return tunerTime;
	}

	public double getWallTime()
	{
		return wallTime;
	}
	@Override
	public int compareTo(TrajectoryFileEntry o) {
		if(tunerTime - o.tunerTime < 0)
		{
			return -1;
		} else if( tunerTime - o.tunerTime == 0)
		{
			
			if(config.equals(o.config)) return 0;
			
			return (config.getFriendlyID() - o.config.getFriendlyID());
		} else
		{
			return 1;
		}
	}
	
	public String toString()
	{
		return "<"+getTunerTime() +","+ getEmpericalPerformance() +","+ getWallTime() + ">"; 
	}
	
}