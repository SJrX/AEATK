package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.helpers;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.algorithmrun.AbstractAlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.algorithmrun.RunResult;
import ca.ubc.cs.beta.aeatk.execconfig.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.runconfig.RunConfig;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;

public class WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator extends
		AbstractTargetAlgorithmEvaluatorDecorator {

	
	private final double wallclockMultScaleFactor;
	private final double startAt;
	
	public WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae) {
		super(tae);
		wallclockMultScaleFactor = 0.95;
		startAt = 0.05;
	}
	
	public WalltimeAsRuntimeTargetAlgorithmEvaluatorDecorator(
			TargetAlgorithmEvaluator tae, double scaleFactor, double startAt) {
		super(tae);
		wallclockMultScaleFactor = scaleFactor;
		this.startAt = startAt;
	}
	
	
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	/**
	 * Stores runs we have already killed in a weak map so that they can be garbage collected if need be.
	 * The synchronization here is for memory visibility only, it doesn't
	 *
	 */
	

	@Override
	public final List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		return processRuns(tae.evaluateRun(runConfigs, new WalltimeAsRuntimeTargetAlgorithmEvaluatorObserver(obs)));
	}
	
	
	
	
	
	
	public List<AlgorithmRun> processRuns(List<AlgorithmRun> runs)
	{
		List<AlgorithmRun> myRuns = new ArrayList<AlgorithmRun>(runs.size());
		for(AlgorithmRun run : runs)
		{
			myRuns.add(processRun(run));
		}
		return myRuns;
	}
	
	public AlgorithmRun processRun(AlgorithmRun run )
	{
		if(run.getRunResult().equals(RunResult.KILLED))
		{
			if(run.getRuntime() == 0 && run.getWallclockExecutionTime() > startAt)
			{
		
				return new WalltimeAsRuntimeAlgorithmRun(run);
			}
		}
		return run;
		
	}
	
	@Override
	public final void evaluateRunsAsync(List<RunConfig> runConfigs,
			final TargetAlgorithmEvaluatorCallback oHandler, TargetAlgorithmEvaluatorRunObserver obs) {
		
		//We need to make sure wrapped versions are called in the same order
		//as there unwrapped versions.
	
		TargetAlgorithmEvaluatorCallback myHandler = new TargetAlgorithmEvaluatorCallback()
		{
			private final TargetAlgorithmEvaluatorCallback handler = oHandler;

			@Override
			public void onSuccess(List<AlgorithmRun> runs) {
					runs = processRuns(runs);			
					handler.onSuccess(runs);
			}

			@Override
			public void onFailure(RuntimeException t) {
					handler.onFailure(t);
			}
		};
		
		tae.evaluateRunsAsync(runConfigs, myHandler, new WalltimeAsRuntimeTargetAlgorithmEvaluatorObserver(obs));

	}

	


	private class WalltimeAsRuntimeTargetAlgorithmEvaluatorObserver implements TargetAlgorithmEvaluatorRunObserver
	{

		private TargetAlgorithmEvaluatorRunObserver obs;
		WalltimeAsRuntimeTargetAlgorithmEvaluatorObserver(TargetAlgorithmEvaluatorRunObserver obs)
		{
			this.obs = obs;
		}
		
		@Override
		public void currentStatus(List<? extends AlgorithmRun> runs) 
		{
			
			List<AlgorithmRun> myRuns = new ArrayList<AlgorithmRun>(runs.size());
			
			for(AlgorithmRun run : runs)
			{
				
				if(run.getRunResult().equals(RunResult.RUNNING))
				{
					if(run.getRuntime() == 0 && run.getWallclockExecutionTime() > startAt)
					{
				
						myRuns.add(new WalltimeAsRuntimeAlgorithmRun(run));
						
					} else
					{
						myRuns.add(run);
					}
				} else
				{
					myRuns.add(run);
				}
			}
			if(obs != null)
			{
				obs.currentStatus(myRuns);
			}
		}
		
		
	}
	
	private class WalltimeAsRuntimeAlgorithmRun implements AlgorithmRun
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 9082975671200245863L;
		
		AlgorithmRun wrappedRun;
		AlgorithmRun wrappedKillableRun;  
		public WalltimeAsRuntimeAlgorithmRun(AlgorithmRun r)
		{
			if(r instanceof AlgorithmRun)
			{
				wrappedKillableRun = (AlgorithmRun) r;
			}
			this.wrappedRun = r;
		}
		
		

		@Override
		public AlgorithmExecutionConfiguration getExecutionConfig() {
			return wrappedRun.getExecutionConfig();
		}

		@Override
		public RunConfig getRunConfig() {
			return wrappedRun.getRunConfig();
		}

		@Override
		public RunResult getRunResult() {
			return wrappedRun.getRunResult();
		}

		@Override
		public double getRuntime() {
			return Math.max(wrappedRun.getWallclockExecutionTime() * wallclockMultScaleFactor,0);
		}

		@Override
		public double getRunLength() {
			return wrappedRun.getRunLength();
		}

		@Override
		public double getQuality() {
			return wrappedRun.getQuality();
		}

		@Override
		public long getResultSeed() {
			return wrappedRun.getResultSeed();
		}

		@Override
		public String getResultLine() {
			return AbstractAlgorithmRun.getResultLine(this);
		}

		@Override
		public String getAdditionalRunData() {
			return wrappedRun.getAdditionalRunData();
		}

		@Override
		public boolean isRunCompleted() {
			return wrappedRun.isRunCompleted();
		}

		@Override
		public boolean isRunResultWellFormed() {
			return wrappedRun.isRunResultWellFormed();
		}

		@Override
		public String rawResultLine() {
			return "[Probably not accurate:]" + wrappedRun.rawResultLine();
		}

		@Override
		public double getWallclockExecutionTime() {
			return wrappedRun.getWallclockExecutionTime();
		}
		
		@Override
		public boolean isCensoredEarly() {
			return ((getRunResult().equals(RunResult.TIMEOUT) && getRunConfig().hasCutoffLessThanMax()) ||  (getRunResult().equals(RunResult.KILLED) && getRuntime() < getRunConfig().getCutoffTime()));
			
			
		}
		
		@Override
		public void kill() {
			if(wrappedKillableRun != null)
			{
				wrappedKillableRun.kill();
				
			}			
		}
		
		public String toString()
		{
			return AbstractAlgorithmRun.toString(this);
		}
		
	}

	@Override
	protected void postDecorateeNotifyShutdown() {
		//No cleanup necessary
	}
}
