package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

/**
 * Checks to see if the resulting SAT or UNSAT call matches what we expect
 * 
 * @author sjr
 *
 */
public class VerifySATTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	private static transient Logger log = LoggerFactory.getLogger(VerifySATTargetAlgorithmEvaluator.class);
	
	private static final Set<String> satResponses = new HashSet<String>();
	private static final Set<String> unsatResponses = new HashSet<String>();
	
	static
	{
		String[] mySatResponses = {"SAT", "SATISFIABLE"};
		String[] myUnsatResponses = {"UNSAT", "UNSATISFIABLE"};
		
		satResponses.addAll(Arrays.asList(mySatResponses));
		unsatResponses.addAll(Arrays.asList(myUnsatResponses));
	}
	
	 
	public VerifySATTargetAlgorithmEvaluator(TargetAlgorithmEvaluator tae) {
		super(tae);
	}
	
	@Override
	public List<AlgorithmRun> evaluateRun(RunConfig rc)
	{
		return this.evaluateRun(Collections.singletonList(rc));
	}
	@Override
	public List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs)
	{
		List<AlgorithmRun> runs = tae.evaluateRun(runConfigs);
		
		for(AlgorithmRun run : runs)
		{
			switch(run.getRunResult())
			{
				case SAT:
					if(unsatResponses.contains(run.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation()))
					{
						Object[] args = { run.getRunResult(), run.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation(), run};
						log.error("Mismatch occured between instance specific information and target algorithm for run (Saw: <{}>, Expected: <{}>): {} ", args);
					} else if(run.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation().equals("UNKNOWN"))
					{
						Object[] args = { run.getRunResult(), run.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation(), run};
						log.warn("Mismatch occured between instance specific information and target algorithm for run (Saw: <{}>, Expected: <{}>): {} ", args);
					}
					break;
					
				case UNSAT:
					if(satResponses.contains(run.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation()))
					{
						Object[] args = { run.getRunResult(), run.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation(), run};
						log.error("Mismatch occured between instance specific information and target algorithm for run (Saw: <{}>, Expected: <{}> ): {} ", args);
					}else if(run.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation().equals("UNKNOWN"))
					{
						Object[] args = { run.getRunResult(), run.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation(), run};
						log.warn("Mismatch occured between instance specific information and target algorithm for run (Saw: <{}>, Expected: <{}>): {} ", args);
					}
					break;
					
				default:
					
			}
		}
		
		
		return runs;
	}

	
}
