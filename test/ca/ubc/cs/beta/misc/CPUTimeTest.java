package ca.ubc.cs.beta.misc;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import ca.ubc.cs.beta.aclib.misc.cputime.CPUTime;

public class CPUTimeTest {

	private final AtomicBoolean shutdown = new AtomicBoolean(false);
	@Test
	public void testCPUTimeDoesntDecrease() throws InterruptedException
	{
		System.out.println(CPUTime.getCPUTime());
		
		Thread t = new Thread(new Runnable()
		{

			@Override
			public void run() {
				for(long i = 1; i < Long.MAX_VALUE; i++)
				{
					
					
					//System.out.println("Started " + i);
					boolean result;
					try {
						if(Thread.interrupted())
						{
							throw new InterruptedException();
						}
						result = ThreeNPlusONE(i, new HashSet<Long>());
					} catch (InterruptedException e) {
						return;
					}
					
					if(result == false)
					{
						//System.out.println("party");
					}
					
				}
				
				
				
				
				
			}
			
			//A stupid function that I'm pretty sure the JVM Can't optimize away
			public boolean ThreeNPlusONE(long n, Set<Long> seenValues) throws InterruptedException
			{
				
				if(n == 1)
				{
					//System.out.println("Ended with 1");
					return true;
				} else
				{
					
					if ((n % 2) == 0)
					{
						seenValues.add(n);
						return ThreeNPlusONE(n/2,seenValues); 
					}	else
					{
						if(seenValues.contains(n))
						{
							//System.out.println("Cycle:" + n);
							return false;
						}
						seenValues.add(n);
						return ThreeNPlusONE(3*n+1,seenValues);
					}
				}
			}
			
			
		});
		
		t.start();
		
		double cpuTime = CPUTime.getCPUTime();
		Thread.sleep(2000);
		double newCPUTime = CPUTime.getCPUTime(); 
		
		if(newCPUTime - cpuTime < 1.7)
		{
			fail("Expected cpuTime used to be greater than 5 seconds, not "  + (newCPUTime - cpuTime));
		} else
		{
			System.out.println("Time was :" + (newCPUTime - cpuTime));
		}
		
		Thread.sleep(500);
		t.interrupt();
		Thread.sleep(500);
		
		double newCPUTime2 = CPUTime.getCPUTime(); 
		
		
		if(newCPUTime2 - cpuTime <  newCPUTime - cpuTime)
		{
			fail("Expected cpuTime used to be greater than "+(newCPUTime - cpuTime)+ "5 seconds, not "  + (newCPUTime2 - cpuTime));
		} else
		{
			System.out.println("Time was :" + (newCPUTime2 - cpuTime));
		}
		
		
		
	}
}
