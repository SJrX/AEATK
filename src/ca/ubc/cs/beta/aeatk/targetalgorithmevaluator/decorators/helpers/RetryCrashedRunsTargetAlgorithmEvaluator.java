package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;

/**
 * Retries crashed runs some number of times
 * 
 * This should be transparent to the end user, so all runs must appear in order, and the run count should not show the retried runs.

 * @author Steve Ramage 
 *
 */
@ThreadSafe
public class RetryCrashedRunsTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	private AtomicInteger runCount = new AtomicInteger(0);
	private final int retryCount; 
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public RetryCrashedRunsTargetAlgorithmEvaluator(int retryCount, TargetAlgorithmEvaluator tae) {
		super(tae);
		if(retryCount < 0)
		{
			throw new IllegalArgumentException("Retry Count should be atleast 0");
		}
		this.retryCount = retryCount;
		
		if(tae.isRunFinal())
		{
			log.warn("Target Algorithm Evaluator {} issues final runs, retrying will be a waste of time", tae.getClass().getSimpleName());
		}
	}
	
	

	@Override
	public List<AlgorithmRunResult> evaluateRun(List<AlgorithmRunConfiguration> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		List<AlgorithmRunResult> runs = tae.evaluateRun(runConfigs, obs);
		
		runs = new ArrayList<AlgorithmRunResult>(runs);
		
		
		for(int i=1; i <= retryCount; i++)
		{
			Map<AlgorithmRunConfiguration, Integer> crashedRuns = new HashMap<AlgorithmRunConfiguration,Integer>();
			boolean crashedRunsExist = false;
			
			for(int j =0; j < runs.size(); j++)
			{
				AlgorithmRunResult run = runs.get(j); 
				if(run.getRunStatus().equals(RunStatus.CRASHED))
				{
					crashedRuns.put(run.getAlgorithmRunConfiguration(),j);
					crashedRunsExist = true;
				}
			}
			
			
			if(!crashedRunsExist)
			{
				log.trace("No crashed runs to retry");
				break;
			} else
			{
				log.debug("Retrying {} crashed runs (Attempt {})", crashedRuns.size(), i);
			}
			
			List<AlgorithmRunConfiguration> crashRCs = new ArrayList<AlgorithmRunConfiguration>(crashedRuns.keySet().size());
			crashRCs.addAll(crashedRuns.keySet());
			
			
			List<AlgorithmRunResult> retriedRuns = tae.evaluateRun(crashRCs, obs);
			
			
			for(AlgorithmRunResult run : retriedRuns)
			{
				runs.set(crashedRuns.get(run.getAlgorithmRunConfiguration()), run);
			}
		}	
		
		runCount.addAndGet(runs.size());
		return runs;
		
		
		
	}

	@Override
	public int getRunCount() {
		//Override this because internal TAE's have probably seen more runs
		return runCount.get();
	}

	@Override
	public void seek(List<AlgorithmRunResult> runs)
	{
		tae.seek(runs);
		runCount.addAndGet(runs.size());
	}


	@Override
	public void evaluateRunsAsync(List<AlgorithmRunConfiguration> runConfigs,
			TargetAlgorithmEvaluatorCallback handler, TargetAlgorithmEvaluatorRunObserver obs) {
		log.warn("Cannot retry runs that are asynchronous at the moment");
		tae.evaluateRunsAsync(runConfigs, handler, obs);
	}
	
	@Override
	protected void postDecorateeNotifyShutdown() {
		//No cleanup necessary
	}
}
