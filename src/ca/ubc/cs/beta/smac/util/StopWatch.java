package ca.ubc.cs.beta.smac.util;

public class StopWatch {
	
	private long startTime = -1;
	private long endTime = Long.MAX_VALUE;
	
	public StopWatch()
	{
		
	}
	/**
	 * Starts the watch
	 * @return stop time in ms
	 */
	public long start()
	{
		if(startTime >= 0)
		{
			throw new IllegalStateException("Watch already started");
		}
		
		startTime = System.currentTimeMillis();
		return startTime;
	}
	
	/**
	 * Stops the watch
	 * @return duration in ms
	 */
	public long stop()
	{
		if (startTime < 0)
		{
			throw new IllegalStateException("Watch hasn't been started");
		} 
			
		endTime = System.currentTimeMillis();
		return endTime - startTime;
	}
	
	/**
	 * Gets the time reading from the watch (either since start, if not stopped, or till stopped
	 * @return duration in ms
	 */
	public long time()
	{
		if(startTime < 0)
		{
			throw new IllegalStateException("Watch hasn't been started");
		}
		return Math.min(endTime, System.currentTimeMillis()) - startTime;
	}
	 
	
	
	
}
