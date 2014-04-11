package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.debug;

import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;

@ThreadSafe
/**
 * Logs every observation request
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class LogEveryObservationTargetAlgorithmEvaluatorDecorator extends	AbstractTargetAlgorithmEvaluatorDecorator {

	Logger log = LoggerFactory.getLogger(LogEveryObservationTargetAlgorithmEvaluatorDecorator.class);
	
	private final boolean logRCOnly;
	
	
	public LogEveryObservationTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae) {
		this(tae, false);
	}
	
	
	public LogEveryObservationTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae, boolean logRequestResponsesRCOnly) {
		super(tae);
		this.logRCOnly = logRequestResponsesRCOnly;
	}


	@Override
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		return tae.evaluateRun(runConfigs, new LoggingTargetAlgorithmEvaluatorObserver(obs));
	}

	@Override
	public final void evaluateRunsAsync(List<RunConfig> runConfigs,
			final TargetAlgorithmEvaluatorCallback oHandler, TargetAlgorithmEvaluatorRunObserver obs) {
		
		
		tae.evaluateRunsAsync(runConfigs, oHandler,  new LoggingTargetAlgorithmEvaluatorObserver(obs));

	}

	@Override
	protected void postDecorateeNotifyShutdown() {
		//No cleanup necessary
	}
	
	private class LoggingTargetAlgorithmEvaluatorObserver implements TargetAlgorithmEvaluatorRunObserver{

		private final TargetAlgorithmEvaluatorRunObserver obs;
		public LoggingTargetAlgorithmEvaluatorObserver(TargetAlgorithmEvaluatorRunObserver obs)
		{
			this.obs = obs;
		}
		@Override
		public void currentStatus(List<? extends AlgorithmRun> runs) {
			log.info("Observed runs : {}" , runs);
			if(obs != null)
			{
				obs.currentStatus(runs);
			}
		}
		
	}
}
