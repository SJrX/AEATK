package ca.ubc.cs.beta.aclib.targetalgorithmevaluator;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.ThreadSafe;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.CommandLineAlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.helpers.OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator;

/**
 * Abstract Target Algorithm Evalutar
 * <p>
 * This class implements the default noop operation
 * 
 * @author Steve Ramage 
 */
@ThreadSafe
public abstract class AbstractTargetAlgorithmEvaluator implements TargetAlgorithmEvaluator {
	 /*
	 * Execution configuration of the target algorithm
	 */
	protected final AlgorithmExecutionConfig execConfig;
	
	protected final AtomicInteger runCount = new AtomicInteger(0);

	/**
	 * Default Constructor
	 * @param execConfig	execution configuration of the target algorithm
	 */
	public AbstractTargetAlgorithmEvaluator(AlgorithmExecutionConfig execConfig)
	{
		this.execConfig = execConfig;
	}
	
	/**
	 * Evaluate a sequence of run configurations
	 * @param runConfigs a list containing zero or more run configurations to evaluate
	 * @param obs 		 observer that will be notified of the current run status
	 * @return	list of the exact same size as input containing the <code>AlgorithmRun</code> objects in the same order as runConfigs
	 * @throws TargetAlgorithmAbortException
	 */
	public abstract List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver runStatusObserver);
	
	/**
	 * Evaluates the given configuration, and when complete the handler is invoked
	 * <p>
	 * <b>Note:</b>You are guaranteed that when this method returns your runs have been 'delivered'
	 * to the eventual processor. In other words if the runs are dispatched to some external
	 * processing system, you can safely shutdown after this method call completes and know that they have been
	 * delivered. Additionally if the runs are already complete (for persistent TAEs), the call back is guaranteed to fire to completion <i>before</i> 
	 * this method is returned.
	 * 
	 * @param runConfigs list of zero or more run configuration to evaluate
	 * @param handler    handler to invoke on completion or failure
	 * @param obs 		 observer that will be notified of the current run status
	 */
	public abstract void evaluateRunsAsync(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorCallback taeCallback, TargetAlgorithmEvaluatorRunObserver runStatusObserver);
	
	
	@Override
	public final List<AlgorithmRun> evaluateRun(RunConfig run) 
	{
		return evaluateRun(Collections.singletonList(run), null);
	}
	
	@Override
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs)
	{
		return evaluateRun(runConfigs, null);
	}
	
	@Override
	public final void evaluateRunsAsync(RunConfig runConfig, TargetAlgorithmEvaluatorCallback handler) {
		evaluateRunsAsync(Collections.singletonList(runConfig), handler);
	}

	
	@Override
	public final void evaluateRunsAsync(List<RunConfig> runConfigs,
			TargetAlgorithmEvaluatorCallback handler) {
				evaluateRunsAsync(runConfigs, handler, null);
			}

	
	@Override
	public int getRunCount()
	{
		return runCount.get();
	}
	

	@Override
	public int getRunHash()
	{
		return 0;
	}

	@Override
	public void seek(List<AlgorithmRun> runs) 
	{
		runCount.set(runs.size());	
	}

	protected void addRuns(List<AlgorithmRun> runs)
	{
		runCount.addAndGet(runs.size());
	}

	@Override
	public String getManualCallString(RunConfig runConfig) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("cd ").append(execConfig.getAlgorithmExecutionDirectory()).append("; ");
		sb.append(CommandLineAlgorithmRun.getTargetAlgorithmExecutionCommand(execConfig, runConfig));
		sb.append("");
		
		return sb.toString();
	}
	
	/**
	 * Blocks waiting for all runs that have been invoked via evaluateRun or evaluateRunAsync to complete
	 * @throws UnsupportedOperationException - if the TAE does not support this operation 
	 */
	@Override
	public void waitForOutstandingEvaluations()
	{
		throw new UnsupportedOperationException(this.getClass().getCanonicalName() + " does NOT support waiting or observing the number of outstanding evaluations, you should probably wrap this TargetAlgorithmEvaluator with an instance of " + OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator.class );
	}
	
	/**
	 * Returns the total number of outstanding evaluations, that is the number of calls to evaluateRun or evaluateRunAsync to complete
	 * <b>NOTE:</b> This is NOT the number of runConfigs to be evaluated but the number of requests
	 * 
	 * @return number of outstanding evaluations
	 * @throws UnsupportedOperationException - if the TAE does not support this operation 
	 */
	@Override
	public int getNumberOfOutstandingEvaluations()
	{
		throw new UnsupportedOperationException(this.getClass().getCanonicalName() + " does NOT support waiting or observing the number of outstanding evaluations, you should probably wrap this TargetAlgorithmEvaluator with an instance of " + OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator.class );
	}
	


}