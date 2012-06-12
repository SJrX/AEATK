package ca.ubc.cs.beta.probleminstance;

import static ca.ubc.cs.beta.TestHelper.getTestFile;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

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
	public void bug1294ErrorReporting()
	{
		File f = TestHelper.getTestFile("featureFiles/sugar-csc09-timeFeats-1.txt");
		
		try {
			ProblemInstanceHelper.getInstances(null, null,f.getAbsolutePath(), false);
		} catch(IOException e)
		{
			throw new RuntimeIOException(e);
		}
		
		
		
		
		getInstanceListWithSeeds("sugar-csc09-timeFeats-1.txt", false);
		
	}
}
