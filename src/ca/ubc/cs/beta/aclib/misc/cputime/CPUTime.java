package ca.ubc.cs.beta.aclib.misc.cputime;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.concurrent.threadfactory.SequentiallyNamedThreadFactory;

/**
 * Class that tracks CPUTime usage since object creation
 * <br>
 * Previously this class used static methods to just give you the time, but this was encapsulated as an object so that you could essentially re-zero the CPU Time measurement,
 * as otherwise you could only accrue CPU time. This would make it difficult to run other meta-algorithmic procedures in the same instance of the JVM.
 *
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public final class CPUTime {
	
	public static final CPUTime getCPUTimeTracker()
	{
		return new CPUTime();
	}
	private final double startCPUTime;
	private final double startUserTime;
	
	public CPUTime()
	{
		startCPUTime = CPUTimeCalculator._getCPUTime();
		startUserTime = CPUTimeCalculator._getUserTime();
	}
	
	public double getCPUTime()
	{
		return CPUTimeCalculator._getCPUTime() - startCPUTime;
	}
	
	public double getUserTime()
	{
		return CPUTimeCalculator._getUserTime() - startUserTime;
	}
	
	public static double getCPUTimeSinceJVMStart()
	{
		return CPUTimeCalculator._getCPUTime();
	}
	
	public static double getUserTimeSinceJVMStart()
	{
		return CPUTimeCalculator._getUserTime();
	}
}
