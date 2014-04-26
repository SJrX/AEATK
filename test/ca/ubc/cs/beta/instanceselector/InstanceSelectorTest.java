package ca.ubc.cs.beta.instanceselector;

import static ca.ubc.cs.beta.TestHelper.getTestFile;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;

import ca.ubc.cs.beta.aeatk.probleminstance.InstanceListWithSeeds;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceHelper;


public class InstanceSelectorTest {

	@BeforeClass
	public static void beforeClass()
	{
		
	}
	
	public static InstanceListWithSeeds getInstanceListWithSeeds(String s, boolean checkOnDisk, boolean deterministic, int limit)
	{
		File f = null;
		try {
		 f = getTestFile("instanceFiles" + File.separator +s);
		} catch(NullPointerException e)
		{
			fail("File Does Not Exist: " + s);
		}
		
		
		String instanceFilesRoot =  f.getAbsoluteFile().getParentFile().getParentFile().toString();
		/*System.out.println(instanceFilesRoot);*/


		try {
			return ProblemInstanceHelper.getInstances(f.getAbsolutePath(), instanceFilesRoot + File.separator + ((checkOnDisk) ? "instances/":"no-instances/"),null,  checkOnDisk, 0, limit, deterministic);
		} catch(IOException e)
		{
			throw new RuntimeIOException(e);
		}
		
	}
	
	
	static class RuntimeIOException extends RuntimeException
	{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 416643478944307403L;
		

		RuntimeIOException(IOException e)
		{
			super(e);
			
		
		}
		

	}
	
}


