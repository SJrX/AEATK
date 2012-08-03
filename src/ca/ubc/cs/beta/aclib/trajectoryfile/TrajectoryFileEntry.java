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
	
	/**
	 * Creates a new Trajectory File Entry
	 * @param config 					configuration to log
	 * @param tunerTime 				tuner time when incumbent selected
	 * @param empericalPerformance  	emperical performance of incumbent
	 * @param acOverhead 				overhead time of automatic configurator
	 */
	public TrajectoryFileEntry(ParamConfiguration config, double tunerTime, double empericalPerformance, double acOverhead)
	{
		this.config = config;
		this.empericalPerformance = empericalPerformance;
		this.acOverhead = acOverhead;
		
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

	@Override
	public int compareTo(TrajectoryFileEntry o) {
		if(tunerTime - o.tunerTime < 0)
		{
			return -1;
		} else if( tunerTime - o.tunerTime == 0)
		{
			return 0;
		} else
		{
			return 1;
		}
	}
	
	
}