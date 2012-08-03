package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.util.List;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

/**
 * Debugging class that verifies two evaluators output the same value.
 * @author Steve Ramage 
 *
 */
public class DebugTargetAlgorithmEvaluator implements TargetAlgorithmEvaluator {

	
	private TargetAlgorithmEvaluator tae1;
	private TargetAlgorithmEvaluator tae2;

	public DebugTargetAlgorithmEvaluator(TargetAlgorithmEvaluator tae1, TargetAlgorithmEvaluator tae2)
	{
		this.tae1 = tae1;
		this.tae2 = tae2;
	}
	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig run) {
		List<AlgorithmRun> listA = tae1.evaluateRun(run);
		List<AlgorithmRun> listB = tae2.evaluateRun(run);
		
		
		assertEquals(listA.size(), listB.size());
		for(int i=0; i < listA.size(); i++)
		{
			assertEquals(listA.get(i).getRunConfig(), listB.get(i).getRunConfig());
			assertEquals(listA.get(i).getRuntime(), listB.get(i).getRuntime());
			assertEquals(listA.get(i).getRunResult(), listB.get(i).getRunResult());
		}
		return listA;
	}

	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs) {
		List<AlgorithmRun> listA = tae1.evaluateRun(runConfigs);
		List<AlgorithmRun> listB = tae2.evaluateRun(runConfigs);
		
		
		assertEquals(listA.size(), listB.size());
		for(int i=0; i < listA.size(); i++)
		{
			assertEquals(listA.get(i).getRunConfig(), listB.get(i).getRunConfig());
			assertEquals(listA.get(i).getRuntime(), listB.get(i).getRuntime());
			assertEquals(listA.get(i).getRunResult(), listB.get(i).getRunResult());
		}
		return listA;
	}
	

	@Override
	public int getRunCount() {
		return assertEquals(tae1.getRunCount(),tae2.getRunCount());
	}

	@Override
	public int getRunHash() {

		return assertEquals(tae2.getRunHash(), tae2.getRunHash());
	}
	
	public Object assertEquals(Object a, Object b)
	{
		if(!a.equals(b))
		{
			throw new IllegalArgumentException("Not Equals");
		}
		return a;
		
	}

	public int assertEquals(int a, int b)
	{
		if(a != b)
		{
			throw new IllegalArgumentException("Not Equals");
		}
		
		return a;
	}
	
	public double assertEquals(double a, double b)
	{
		if( a != b ) 
		{
			throw new IllegalArgumentException("Not Equals");
		}
		return a;
	}
	@Override
	public void seek(List<AlgorithmRun> runs) {
		tae1.seek(runs);
		tae2.seek(runs);
	}
	
	
	@Override
	public String getManualCallString(RunConfig runConfig) {
		String callString = tae2.getManualCallString(runConfig);
		if(tae1.getManualCallString(runConfig).equals(callString))
		{
			return callString;
		} else
		{
			throw new IllegalArgumentException("Not Equals");
		}
	}
	@Override
	public void notifyShutdown() {
		tae1.notifyShutdown();
		tae2.notifyShutdown();	
	}

}
