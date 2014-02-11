package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.debug;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.AbstractForEachRunTargetAlgorithmEvaluatorDecorator;

@ThreadSafe
public class LogEveryTargetAlgorithmEvaluatorDecorator extends
		AbstractForEachRunTargetAlgorithmEvaluatorDecorator {

	Logger log = LoggerFactory.getLogger(LogEveryTargetAlgorithmEvaluatorDecorator.class);
	
	private final boolean logRCOnly;
	
	private final String context;
	
	public LogEveryTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae) {
		this(tae, false);
	}
	public LogEveryTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae, String context) {
		this(tae, context, false);
	}
	
	public LogEveryTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae, boolean logRequestResponsesRCOnly) {
		super(tae);
		this.context = "";
		this.logRCOnly = logRequestResponsesRCOnly;
	}

	public LogEveryTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae, String context, boolean logRequestResponsesRCOnly) {
		super(tae);
		this.logRCOnly = logRequestResponsesRCOnly;
		this.context = (context != null) ? "(" +context+")" : "";
	}

	
	protected synchronized AlgorithmRun processRun(AlgorithmRun run)
	{
		if(logRCOnly)
		{
			log.debug("Run {} Completed: {} ", context, run.getRunConfig());
		} else
		{
			log.debug("Run {} Completed: {} : {} ", context, run, run.getAdditionalRunData());
		}
		return run;
	}
	
	protected RunConfig processRun(RunConfig rc)
	{
		log.debug("Run {} Scheduled: {} ", context, rc);
		return rc;
	}

	@Override
	protected void postDecorateeNotifyShutdown() {
		//No cleanup necessary
	}
}
