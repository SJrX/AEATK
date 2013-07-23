package ca.ubc.cs.beta.misc;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.Ignore;
import org.junit.Test;

import ca.ubc.cs.beta.aclib.misc.cputime.CPUTime;

public class CPUTimeTest {

	
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
	
	@Test
	@Ignore
	/**
	 * I have no idea how to actually make a test for this work (need something that has lots of CPU time but not
	 * alot of User Time.
	 * @throws InterruptedException
	 */
	public void testUserTimeDoesntDecrease() throws InterruptedException
	{
		System.out.println("CPU Time :" +  CPUTime.getCPUTime() + " User Time: "+ CPUTime.getUserTime());
		
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
		
		double userTime = CPUTime.getUserTime();
		System.out.println("CPU Time :" +  CPUTime.getCPUTime() + " User Time: "+ CPUTime.getUserTime());
		Thread.sleep(2000);
		double newUserTime = CPUTime.getUserTime(); 
		System.out.println("CPU Time :" +  CPUTime.getCPUTime() + " User Time: "+ CPUTime.getUserTime());
		
		if(newUserTime - userTime < 1.7)
		{
			
			fail("Expected userTime used to be greater than 5 seconds, not "  + (newUserTime - userTime));
		} else
		{
			System.out.println("Time was :" + (newUserTime - userTime));
		}
		
		System.out.println("CPU Time :" +  CPUTime.getCPUTime() + " User Time: "+ CPUTime.getUserTime());
		Thread.sleep(500);
		t.interrupt();
		Thread.sleep(500);
		System.out.println("CPU Time :" +  CPUTime.getCPUTime() + " User Time: "+ CPUTime.getUserTime());
		
		
		double newUserTime2 = CPUTime.getUserTime(); 
		
		
		if(newUserTime2 - userTime <  newUserTime - userTime)
		{
			fail("Expected userTime used to be greater than "+(newUserTime - userTime)+ "5 seconds, not "  + (newUserTime2 - userTime));
		} else
		{
			System.out.println("Time was :" + (newUserTime2 - userTime));
		}
		
		
		
	}
	
}
