package ca.ubc.cs.beta.acquisitionfunction;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import org.junit.Test;

import ca.ubc.cs.beta.aeatk.acquisitionfunctions.AcquisitionFunctions;

public class AcquisitionFunctionTester {

	
	@Test
	/**
	 * This tests all things being equal lower means are better
	 */
	public void testReasonableMeanOrdering()
	{
		
		
		for(AcquisitionFunctions f : AcquisitionFunctions.values())
		{
			if(f.equals(AcquisitionFunctions.LCBEIRR)) continue;
			double f_min_samples = 1.0;
			double[] predmean = { 1.0, 10}; 
			double[] predvar = { 0.001, 0.001}; 
			double standardErrors = 1.0;
			double[] predictions = f.getFunction().computeAcquisitionFunctionValue(f_min_samples, predmean, predvar, standardErrors);

			System.out.println( f.name() + ":" + Arrays.toString(predictions));
			assertTrue("Expected that for expected improvment function " + f.name()+ " first value should be less than second", predictions[0] <= predictions[1]);
		}
		
	}
	

	@Test
	/**
	 * This tests that all things being equal higher variance is better
	 */
	public void testReasonableVarianceOrdering()
	{
		
		
		for(AcquisitionFunctions f : AcquisitionFunctions.values())
		{
			
			if(f.equals(AcquisitionFunctions.LCBEIRR)) continue;
			double f_min_samples = 1.0;
			double[] predmean = { 2.0, 2.0}; 
			double[] predvar = { 0.1, 0.001}; 
			double standardErrors = 1.0;
			double[] predictions = f.getFunction().computeAcquisitionFunctionValue(f_min_samples, predmean, predvar, standardErrors);

			System.out.println( f.name() + ":" + Arrays.toString(predictions));
			assertTrue("Expected that for expected improvment function " + f.name()+ " first value should be less than second", predictions[0] <= predictions[1]);
		}
		
	}
	
	@Test
	public void testKnownValues()
	{
		
		EnumMap<AcquisitionFunctions, List<Double>> solutions = new EnumMap<AcquisitionFunctions, List<Double>>(AcquisitionFunctions.class);
		
		
		solutions.put(AcquisitionFunctions.EXPONENTIAL, Arrays.asList(1.28111062544401, 8010.91730546862,40512.5386959303));
		solutions.put(AcquisitionFunctions.SIMPLE,Arrays.asList( -0.367879441171442, -0.00673794699908547, -4.53999297624848e-05));
		solutions.put(AcquisitionFunctions.LCB, Arrays.asList( 0.968377223398316,4.96837722339832, 9.96837722339832));
		solutions.put(AcquisitionFunctions.EI, Arrays.asList(4.37281617269574,  8014.05334762749, 40515.6750575457));
		for(AcquisitionFunctions f : solutions.keySet())
		{
			double f_min_samples = 1.0;
			double[] predmean = { 1.0, 5.0, 10}; 
			double[] predvar = { 0.001, 0.001, 0.001}; 
			double standardErrors = f_min_samples;
			double[] predictions = f.getFunction().computeAcquisitionFunctionValue(f_min_samples, predmean, predvar, standardErrors);

			System.out.println( f.name() + ":" + Arrays.toString(predictions));
			
			assertTrue("Expected that for expected improvment function " + f.name()+ " first value should be less than second", predictions[0] < predictions[1]);
			assertTrue("Expected that for expected improvment function " + f.name()+ " first value should be less than second", predictions[1] < predictions[2]);
			
			for(int i = 0; i < predmean.length; i++)
			{
				assertEquals("Expected that for expected improvement function " + f.name() + " the computed value and the response value are roughly equal" + predictions[i] + " vs " + solutions.get(f).get(i), solutions.get(f).get(i), predictions[i], 0.00000001);
			}
			
		}
	}
	
	@Test
	public void testLCB()
	{
		//This function explicitly checks LCB, it really doesn't exist to do anything other than 
		//Provide a way of easily seeing that it works correctly manually.
	
		double f_min_samples = 1.0;
		double[] predmean = { 1.0, 5.0, 10}; 
		double[] predvar = { 0.25, 0.25, 0.25};

		double[] errs = { 0.001, 0.01, 0.1, 0.25, 0.5, 1, 2, 3, 4, 5};
		for(double standardError : errs)
		{
			double[] predictions = AcquisitionFunctions.LCB.computeAcquisitionFunctionValue(f_min_samples, predmean, predvar, standardError);
			assertTrue("Expected that for expected improvment function " + AcquisitionFunctions.LCB+ " first value should be less than second", predictions[0] < predictions[1]);
			assertTrue("Expected that for expected improvment function " + AcquisitionFunctions.LCB+ " first value should be less than second", predictions[1] < predictions[2]);
			
			System.out.println(Arrays.toString(predictions));
		}
		
	}
}
