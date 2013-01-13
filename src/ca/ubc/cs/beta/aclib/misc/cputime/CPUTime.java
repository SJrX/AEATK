package ca.ubc.cs.beta.aclib.misc.cputime;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CPUTime {
	private static final Logger log = LoggerFactory.getLogger(CPUTime.class);
	
	/**
	 * Returns the total CPU Time for this JVM
	 * 
	 * @return cpu time for this jvm if enabled&supported 0 otherwise
	 */
	public static long getCPUTime()
	{
		try 
		{
			ThreadMXBean b = ManagementFactory.getThreadMXBean();
		
			long cpuTime = 0;
			for(long threadID : b.getAllThreadIds())
			{
				long threadTime =  b.getThreadCpuTime(threadID);
				if(threadTime == -1)
				{ //This JVM doesn't have CPU time enabled
			      //We check every iteration because some threads (the current thread may give us something other than -1)
					
					log.debug("JVM does not have CPU Time enabled");
					return 0; 
				}
				
				cpuTime += threadTime;
			}
			return cpuTime / 1000 / 1000 / 1000;
		} catch(UnsupportedOperationException e)
		{
			log.debug("JVM does not support CPU Time measurements");
			return 0;
		}
		
	}
}
