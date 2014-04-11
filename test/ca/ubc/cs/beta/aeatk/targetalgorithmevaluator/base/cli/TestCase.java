package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
/*****
 *  
    system.c:
	#include <unistd.h>
	#include <stdlib.h>
	#include <stdio.h>
	
	int main()
	{
	  system("/home/sjr/git/AutomaticConfiguratorLibrary/test-files/runsolver/sleepy");
	}


    sleepy.c:
    
    #include <unistd.h>

	int main()
	{
	
	  int i,j;
	  for(i=0; i < 20000000; i++)
	  {
	    usleep(1500);
	    for(j = 0; j < 500000; j++);
	  }
	}
    
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class TestCase {

	

		public static void main(String[] args) throws Exception, InterruptedException
		{
		
			final Process p = Runtime.getRuntime().exec("/home/sjr/git/AutomaticConfiguratorLibrary/test-files/runsolver/system");
	
			final Semaphore stdErrorDone = new Semaphore(0);
			
			final CountDownLatch stdErrorStarted = new CountDownLatch(0);
			stdErrorStarted.countDown();
			
			Runnable standardErrorReader = new Runnable()
			{

				@Override
				public void run() {
					
					try { 
						stdErrorStarted.countDown();
					final Scanner procErr = new Scanner(p.getErrorStream());
					while(procErr.hasNext())
					{	
						System.out.println("[PROCESS-ERR] "+ procErr.nextLine());
					}
					
					procErr.close();
					} finally
					{
						stdErrorDone.release();
					}
					
				}
				
			};
			
			ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
			ExecutorService se = Executors.newCachedThreadPool();
			final AtomicBoolean shutdown = new AtomicBoolean(false);
			Runnable run = new Runnable()
			{
				public void run()
				{
					try {
						stdErrorStarted.await();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					}
					System.out.println("Calling destroy");
					p.destroy();
					shutdown.set(true);
					System.out.println("Process destroyed return value "+  p.exitValue());
					//try {
						//p.getInputStream().close();
						//p.getOutputStream().close();
						//p.getErrorStream().close();
					//} catch (IOException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					//}
					
					System.out.println("Streams closed");
					
					
				}
			};
			
			
			
			//Scanner procIn = new Scanner(p.getInputStream());
			
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			se.submit(standardErrorReader);
			
			ses.schedule(run, 4, TimeUnit.SECONDS);
			
			System.out.println("Starting input reading ");
			try {
				
			String line;
			
			while((line = in.readLine()) != null)
			{
				System.out.println(line);
			}
			while(!shutdown.get())
			{
				if(in.ready())
				{
					line = in.readLine();
					System.out.println(line);
				} else
				{
					Thread.sleep(50);
				}
			}	
			} catch(Exception e)
			{
				e.printStackTrace();
				throw e;
			}
			in.readLine();
			System.out.println("Done reading input");
		
			System.out.println("Standard Error Closed");
			
			se.shutdownNow();
			ses.shutdownNow();
			
			se.awaitTermination(24, TimeUnit.HOURS);
			System.out.println("DONE");
			
			
		

	}

}
