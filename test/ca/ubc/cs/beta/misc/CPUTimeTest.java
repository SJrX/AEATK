package ca.ubc.cs.beta.misc;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import ca.ubc.cs.beta.aeatk.misc.cputime.CPUTime;

public class CPUTimeTest {

	
	@Test
	public void testCPUTimeDoesntDecrease() throws InterruptedException
	{
		
		
		CPUTime cpuTime = new CPUTime();
		
		System.out.println(cpuTime.getCPUTime());
		
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
		
		double cpuTimeObs = cpuTime.getCPUTime();
		Thread.sleep(2000);
		double newCPUTime = cpuTime.getCPUTime(); 
		
		if(newCPUTime - cpuTimeObs < 1.7)
		{
			fail("Expected cpuTime used to be greater than 5 seconds, not "  + (newCPUTime - cpuTimeObs));
		} else
		{
			System.out.println("Time was :" + (newCPUTime - cpuTimeObs));
		}
		
		Thread.sleep(500);
		t.interrupt();
		Thread.sleep(500);
		
		double newCPUTime2 = cpuTime.getCPUTime(); 
		
		
		if(newCPUTime2 - cpuTimeObs <  newCPUTime - cpuTimeObs)
		{
			fail("Expected cpuTime used to be greater than "+(newCPUTime - cpuTimeObs)+ "5 seconds, not "  + (newCPUTime2 - cpuTimeObs));
		} else
		{
			System.out.println("Time was :" + (newCPUTime2 - cpuTimeObs));
		}

	}
	
	@Test
	@Ignore
	/**
	 * I have no idea how to actually make a test for this work (need something that has lots of CPU time but not
	 * alot of User Time.
	 * @throws InterruptedException
	 */
	public void testUserTimeDoesntDecrease() throws InterruptedException
	{
		CPUTime cpuTime = new CPUTime();
		System.out.println("CPU Time :" +  cpuTime.getCPUTime() + " User Time: "+ cpuTime.getUserTime());
		
		Thread t = new Thread(new Runnable()
		{

			@Override
			public void run() {
				for(long i = 1; i < Long.MAX_VALUE; i++)
				{
					
					
					
					try {
						if(Thread.interrupted())
						{
							throw new InterruptedException();
						}
						
						
						StringBuilder sb = new StringBuilder("");
						for(int j=0; j < 10000; j++)
						{
							sb.append("LOTS OF I/O... LOTS OF I/O...");
						}
						
						for(int j = 0; j < 10000; j++)
						{
							System.out.println(sb.toString());
						}

					} catch (InterruptedException e) {
						return;
					}
					
				}
				
				
				
				
				
			}

			/*private boolean scanFileSystem(File file) {
				for(File f : file.listFiles())
				{
					f.isDirectory();
					f.exists();
					f.
				}
			}*/
		
			
			
		});
		
		t.start();
		
		double userTime = cpuTime.getUserTime();
		System.out.println("CPU Time :" +  cpuTime.getCPUTime() + " User Time: "+ cpuTime.getUserTime());
		Thread.sleep(2000);
		double newUserTime = cpuTime.getUserTime(); 
		System.out.println("CPU Time :" +  cpuTime.getCPUTime() + " User Time: "+ cpuTime.getUserTime());
		
		if(newUserTime - userTime < 1.7)
		{
			
			fail("Expected userTime used to be greater than 5 seconds, not "  + (newUserTime - userTime));
		} else
		{
			System.out.println("Time was :" + (newUserTime - userTime));
		}
		
		System.out.println("CPU Time :" +  cpuTime.getCPUTime() + " User Time: "+ cpuTime.getUserTime());
		Thread.sleep(500);
		t.interrupt();
		Thread.sleep(500);
		System.out.println("CPU Time :" +  cpuTime.getCPUTime() + " User Time: "+ cpuTime.getUserTime());
		
		
		double newUserTime2 = cpuTime.getUserTime(); 
		
		
		if(newUserTime2 - userTime <  newUserTime - userTime)
		{
			fail("Expected userTime used to be greater than "+(newUserTime - userTime)+ "5 seconds, not "  + (newUserTime2 - userTime));
		} else
		{
			System.out.println("Time was :" + (newUserTime2 - userTime));
		}
		
		
		
	}
	
}
