package ca.ubc.cs.beta.probleminstance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.beust.jcommander.ParameterException;

import ca.ubc.cs.beta.aeatk.probleminstance.InstanceListWithSeeds;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceHelper;
import ca.ubc.cs.beta.aeatk.probleminstance.seedgenerator.InstanceSeedGenerator;
import ca.ubc.cs.beta.aeatk.probleminstance.seedgenerator.RandomInstanceSeedGenerator;
import ca.ubc.cs.beta.aeatk.probleminstance.seedgenerator.SetInstanceSeedGenerator;
import static org.junit.Assert.*;
import static ca.ubc.cs.beta.TestHelper.*;

/**
 * Tests that Instance's are loaded correctly 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
//TODO: Check Features
//TODO: Check New File Format
@SuppressWarnings("unused")
public class ProblemInstanceHelperTester {

	
	
	/**
	 * If you modify either constant you need to make sure the filenames exist on disk 
	 */
	
	/**
	 * We expect files named instance(0 - NON_SPACE_INSTANCES) 
	 * i.e.
	 * instance0, instance1, instance2....
	 */
	public static final int NON_SPACE_INSTANCES = 10;

	/**
	 * We expect files named sp instance(0-SPACE_INSTANCES)
	 * i.e.
	 * sp instance0, sp instance1, sp instance2
	 */
	public static final int SPACE_INSTANCES = 5;
	
	
	
	
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
	
	@Before
	public void setUp()
	{
		
		ProblemInstanceHelper.clearCache();
	}
	
	public static InstanceListWithSeeds getInstanceListWithSeeds(String s, boolean checkOnDisk)
	{
		return getInstanceListWithSeeds(s,checkOnDisk, false, Integer.MAX_VALUE);
	}
	
	public static InstanceListWithSeeds getInstanceListWithSeeds(String s, boolean checkOnDisk, boolean determinstic)
	{
		return getInstanceListWithSeeds(s,checkOnDisk, false, Integer.MAX_VALUE);
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
		
		
		String instanceFilesRoot =  f.getParentFile().getParentFile().toString();
		/*System.out.println(instanceFilesRoot);*/

		
		
		try {
			return ProblemInstanceHelper.getInstances(f.getAbsolutePath(), instanceFilesRoot + File.separator + ((checkOnDisk) ? "instances/":"no-instances/"),null,  checkOnDisk, 0, limit, deterministic);
		} catch(IOException e)
		{
			throw new RuntimeIOException(e);
		}
		
	}
	
	private void validateClassicNonSpace(InstanceListWithSeeds ilws)
	{
		this.validateClassicNonSpace(ilws, 0);
	}
	
	private void validateClassicNonSpace(InstanceListWithSeeds ilws, int addlInstances)
	{

		if(!(ilws.getSeedGen() instanceof RandomInstanceSeedGenerator))
		{
			fail("Expected Random Instance Seed Generator");
		} else
		{
			assertEquals(ilws.getSeedGen().getProblemInstanceOrder(ilws.getInstances()).size(),NON_SPACE_INSTANCES+addlInstances);
		}
		
		assertTrue(ilws.getSeedGen().allInstancesHaveSameNumberOfSeeds());
		assertEquals(Integer.MAX_VALUE, ilws.getSeedGen().getInitialInstanceSeedCount());
		
		List<String> instanceNames = new ArrayList<String>();
		
		for(int i=0; i < NON_SPACE_INSTANCES+addlInstances; i++)
		{
			boolean match = false;
			for(ProblemInstance pi : ilws.getInstances())
			{
				if(pi.getInstanceName().endsWith("instance"+i))
				{
					match = true;
					break;
				}
			}
			
			assertTrue("Could not finding matching instance to instance" + i, match);
		}
		
		assertEquals(ilws.getInstances().size(),NON_SPACE_INSTANCES+addlInstances);
		
	}
	
	private void validateClassicNonSpaceInstanceSeed(InstanceListWithSeeds ilws)
	{
		this.validateClassicNonSpaceInstanceSeed(ilws, 0);
	}
	
	private void validateClassicNonSpaceInstanceSeed(InstanceListWithSeeds ilws, int addlInstances)
	{

		
		int expectedSeedCount = NON_SPACE_INSTANCES*(NON_SPACE_INSTANCES+1)/2;
		if(!(ilws.getSeedGen() instanceof SetInstanceSeedGenerator))
		{
			fail("Expected Set Instance Seed Generator");
		} else {
			//Relies on the way instance seed pairs are used.
			assertEquals(ilws.getSeedGen().getProblemInstanceOrder(ilws.getInstances()).size(),expectedSeedCount);
		}
		
		
		List<String> instanceNames = new ArrayList<String>();
		
		for(int i=0; i < NON_SPACE_INSTANCES+addlInstances; i++)
		{
			boolean match = false;
			for(ProblemInstance pi : ilws.getInstances())
			{
				if(pi.getInstanceName().endsWith("instance"+i))
				{
					match = true;
					break;
				}
			}
			
			assertTrue("Could not finding matching instance to instance" + i, match);
		}
		
		assertEquals(ilws.getSeedGen().getInitialInstanceSeedCount(),expectedSeedCount);
		assertFalse(ilws.getSeedGen().allInstancesHaveSameNumberOfSeeds());
		
		
		assertEquals(ilws.getInstances().size(),NON_SPACE_INSTANCES+addlInstances);
		
		InstanceSeedGenerator gen = ilws.getSeedGen();
		for(int i =0; i < NON_SPACE_INSTANCES+addlInstances; i++)
		{
			ProblemInstance pi = ilws.getInstances().get(i);
			
			/**
			 * Each instance has one more seed than it's index going from
			 * [0,index]
			 */
			for(int j=0; j <= i; j++)
			{
				assertEquals(gen.getNextSeed(pi),j);
			}
			
			assertFalse(gen.hasNextSeed(pi));
		}
		
	}
	
	private void validateClassicNonSpaceInstanceSpecific(InstanceListWithSeeds ilws)
	{
		validateClassicNonSpaceInstanceSpecific(ilws,0);
	}
			
	private void validateClassicNonSpaceInstanceSpecific(InstanceListWithSeeds ilws, int addlInstances)
	{

		
		if(!(ilws.getSeedGen() instanceof RandomInstanceSeedGenerator))
		{
			fail("Expected Random Instance Seed Generator");
		}
		
		assertTrue(ilws.getSeedGen().allInstancesHaveSameNumberOfSeeds());
		assertEquals(Integer.MAX_VALUE, ilws.getSeedGen().getInitialInstanceSeedCount());
		
		List<String> instanceNames = new ArrayList<String>();
		
		for(int i=0; i < NON_SPACE_INSTANCES+addlInstances; i++)
		{
			boolean match = false;
			for(ProblemInstance pi : ilws.getInstances())
			{
				if(pi.getInstanceName().endsWith("instance"+i))
				{
					match = true;
					break;
				}
			}
			
			assertTrue("Could not finding matching instance to instance" + i, match);
		}
		
		assertEquals(ilws.getInstances().size(),NON_SPACE_INSTANCES+addlInstances);
		
		InstanceSeedGenerator gen = ilws.getSeedGen();
		for(int i =0; i < NON_SPACE_INSTANCES+addlInstances; i++)
		{
			ProblemInstance pi = ilws.getInstances().get(i);
			assertNotNull("Expected Instance Specific Information", pi.getInstanceSpecificInformation());
			assertTrue(pi.getInstanceName().endsWith(pi.getInstanceSpecificInformation()));

		
		}
		
	}
	
	
	private void validateClassicNonSpaceInstanceSpecificSeed(InstanceListWithSeeds ilws)
	{
		validateClassicNonSpaceInstanceSpecificSeed(ilws,0);
	}
			
	private void validateClassicNonSpaceInstanceSpecificSeed(InstanceListWithSeeds ilws, int addlInstances)
	{

		
		if(!(ilws.getSeedGen() instanceof SetInstanceSeedGenerator))
		{
			fail("Expected Set Instance Seed Generator");
		} else {
			//Relies on the way instance seed pairs are used.
			assertEquals(NON_SPACE_INSTANCES*(NON_SPACE_INSTANCES+1)/2,ilws.getSeedGen().getProblemInstanceOrder(ilws.getInstances()).size());
		}
		
		
		
		List<String> instanceNames = new ArrayList<String>();
		
		for(int i=0; i < NON_SPACE_INSTANCES+addlInstances; i++)
		{
			boolean match = false;
			for(ProblemInstance pi : ilws.getInstances())
			{
				if(pi.getInstanceName().endsWith("instance"+i))
				{
					match = true;
					break;
				}
			}
			
			assertTrue("Could not finding matching instance to instance" + i, match);
		}
		assertEquals(NON_SPACE_INSTANCES*(NON_SPACE_INSTANCES+1)/2,ilws.getSeedGen().getInitialInstanceSeedCount());
		assertEquals(ilws.getInstances().size(),NON_SPACE_INSTANCES+addlInstances);
		
		assertFalse(ilws.getSeedGen().allInstancesHaveSameNumberOfSeeds());
		
		InstanceSeedGenerator gen = ilws.getSeedGen();
		for(int i =0; i < NON_SPACE_INSTANCES+addlInstances; i++)
		{
			ProblemInstance pi = ilws.getInstances().get(i);
			assertNotNull("Expected Instance Specific Information", pi.getInstanceSpecificInformation());
			assertTrue(pi.getInstanceName().endsWith(pi.getInstanceSpecificInformation()));
			
			/**
			 * Each instance has one more seed than it's index going from
			 * [0,index]
			 */
			for(int j=0; j <= i; j++)
			{
				assertEquals(gen.getNextSeed(pi),j);
			}
			
			assertFalse(gen.hasNextSeed(pi));

		}
		
		gen.reinit();
		List<ProblemInstance> pis =  gen.getProblemInstanceOrder(ilws.getInstances());
		
		for(ProblemInstance pi : pis)
		{
			System.out.println(pi.getInstanceName() + ":" + gen.getNextSeed(pi));
		}
		//TODO Check that instance seeds are returned in correct order
		
		
		
		
		
	}
	
	
	
	
	@Test
	public void testClassicInstanceListFileValid()
	{	
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatValid.txt", true);
		validateClassicNonSpace(ilws);
		
	}
	
	@Test
	public void testClassicInstanceListFileExtraLinesAtEndValid()
	{	
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatValidExtraLinesAtEnd.txt", true);
		validateClassicNonSpace(ilws);
	}
	
	
	@Test(expected=ParameterException.class)
	public void testClassicInstanceNonExistantFilesCheck()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatNonExistantFiles.txt", true);
		
	}
	
	@Test(expected=ParameterException.class)
	public void testClassicInstanceExtraLinesInvalid()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInvalidExtraLines.txt", true);
		
	}
	
	

	@Test
	public void testClassicInstanceNonExistantFilesNoCheck()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatNonExistantFiles.txt", false);
		validateClassicNonSpace(ilws,1);
	}
	
	@Test
	public void testClassicInstanceSeedFileValid()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSeedValid.txt", false);
		validateClassicNonSpaceInstanceSeed(ilws);
		
	}
	
	/**
	 * Verifies that when the instanceSeedFile has an equal number of seeds per instance the
	 * methods return the correct values  
	 * 
	 */
	@Test
	public void testClassicInstanceSeedFileValidEvenNumberOfSeeds()
	{
		
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSeedValidEvenNumberOfSeeds.txt", false);
		assertEquals(30, ilws.getSeedGen().getInitialInstanceSeedCount());
		assertTrue(ilws.getSeedGen().allInstancesHaveSameNumberOfSeeds());
	}
	
	
	@Test(expected=ParameterException.class)
	public void testClassicInstanceSeedMissingColumn()
	{
		
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSeedMissingColumn.txt", false);
	
	}
	
	@Test(expected=ParameterException.class)
	public void testClassicInstanceSeedInvalidNumber()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSeedMissingColumn.txt", false);
		
	}
	
	@Test(expected=ParameterException.class)
	public void testClassicInstanceSeedExtraColumn()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSeedExtraColumn.txt", false);
		
	}
	
	@Test
	public void testClassicInstanceSpecificFileValid()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSpecificValid.txt", false);
		validateClassicNonSpaceInstanceSpecific(ilws);
		
	}
	
	/**
	 * Should ensure that the rest is captured correctly
	 */
	@Test(expected=ParameterException.class)
	public void testClassicInstanceSpecificLotsOfExtraSpacesInRest()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSpecificExtraColumns.txt", false);
		
	}
	
	@Test(expected=ParameterException.class)
	public void testClassicInstanceSpecificMissingColumn()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSpecificMissingColumn.txt", false);
	}
	
	@Test
	public void testClassicInstanceSpecificSeedValid()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSeedSpecificValid.txt", false);
		validateClassicNonSpaceInstanceSpecificSeed(ilws);
	}
	
	@Test(expected=ParameterException.class)
	public void testClassicInstanceSpecificSeedMissingColumn()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSeedSpecificMissingColumn.txt", false);
		fail();
	}
	
	
	@Test(expected=ParameterException.class)
	public void testClassicInstanceSpecificSeedMissingColumn2()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSeedSpecificMissingColumn2.txt", false);
		fail();
	}
	
	
	@Test(expected=ParameterException.class)
	public void testClassicInstanceSpecificSeedMissingMiddleColumn()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSeedSpecificSpaceForMiddleColumn.txt", false);
		fail();
	}
	

	
	@Test(expected=ParameterException.class)
	public void testClassicInstanceSpecificSeedDiscrepancyInInstanceSpecificInfo()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSeedSpecificDiscrepancy.txt", false);
		fail();
	}
	
	@Test
	public void testClassicInstanceSeedCapped()
	{
		InstanceListWithSeeds ilws = getInstanceListWithSeeds("classicFormatInstanceSeedValid.txt", false, true, 2);
		
		assertEquals(19, ilws.getSeedGen().getInitialInstanceSeedCount());		
	}
	
	
	
}
