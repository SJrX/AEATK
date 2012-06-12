package ca.ubc.cs.beta.probleminstance;

import static ca.ubc.cs.beta.TestHelper.getTestFile;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;

import ca.ubc.cs.beta.TestHelper;

/**
 * Test class for various example feature files that we have 
 * had problems with.
 * @author seramage
 *
 */
public class BuggyFeatureFilesTester {

	
	static class RuntimeIOException extends RuntimeException
	{
		
		private IOException e;

		RuntimeIOException(IOException e)
		{
			super(e);
			
		
		}
		

	}
	
	@Before
	public void setUp()
	{
		
		ProblemInstanceHelper.clearCache();
	}
	
	public static InstanceListWithSeeds getInstanceListWithSeeds(String s, boolean checkOnDisk)
	{
		File f = null;
		try {
		 f = getTestFile("featureFiles" + File.separator +s);
		} catch(NullPointerException e)
		{
			fail("File Does Not Exist: " + s);
		}
		
		
		String instanceFilesRoot =  f.getParentFile().getParentFile().toString();
		/*System.out.println(instanceFilesRoot);*/

		
		
		try {
			return ProblemInstanceHelper.getInstances(f.getAbsolutePath(), instanceFilesRoot + File.separator + ((checkOnDisk) ? "instances/":"no-instances/"), checkOnDisk);
		} catch(IOException e)
		{
			throw new RuntimeIOException(e);
		}
		
	}
	
	@Test
	/**
	 * Tests that no errors are thrown when training and test set instances aren't disjoint.
	 * 
	 * This is a copy and paste of the code that loads instances in AutomaticConfigurator
	 */
	public void bug1303ErrorReporting()
	{
		
		String feature = TestHelper.getTestFile("featureFiles/sugar-csc09-timeFeats-1.txt").getAbsolutePath();
		String f = TestHelper.getTestFile("featureFiles/sugar-csc09.csv").getAbsolutePath();
		String instanceFilesRoot =  TestHelper.getTestFile("featureFiles/sugar-csc09.csv").getParentFile().getParentFile().toString();
		
		boolean checkOnDisk = true;
		try {
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			
			PrintStream old = System.out;
			System.setOut(new PrintStream(bout));
			
			
			
			
		InstanceListWithSeeds ilws = ProblemInstanceHelper.getInstances(f,instanceFilesRoot + File.separator + ((checkOnDisk) ? "instances/":"no-instances/"), feature, !checkOnDisk);
		
		
		
		
		ilws = ProblemInstanceHelper.getInstances(f, instanceFilesRoot + File.separator + ((checkOnDisk) ? "instances/":"no-instances/"), !checkOnDisk);
		
		String output = bout.toString();
		System.setOut(old);
		System.out.println(output);
		
		
		if(output.contains("but the instance Features don't match"))
		{
			fail("Instances didn't match up");
		} else if(output.contains("ERROR"))
		{
			fail("Error detected");
			
		} else if(output.contains("Matched Features for file name"))
		{
			//No matching output
		} else
		{
			fail("No matching output");
		}
		
			//ProblemInstanceHelper.getInstances(null, null,f.getAbsolutePath(), false);
		} catch(IOException e)
		{
			throw new RuntimeIOException(e);
		}
		
		
		
		
	
		
	}
}
