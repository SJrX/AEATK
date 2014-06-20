package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.resource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.AbstractAsyncTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.functionality.OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator;

/**
 * Forks the asynchronous evaluation of runs between a master TAE and a slave TAE (not blocking on the slave TAE).
 * Kill run executions in either TAEs if one of them terminates the runs first.
 * 
 * @author Alexandre Fréchette <afrechet@cs.ubc.ca>, Steve Ramage <seramage@cs.ubc.ca>
 * 
 */
public class ForkingTargetAlgorithmEvaluatorDecorator extends AbstractAsyncTargetAlgorithmEvaluatorDecorator {
	
	private final static Logger log = LoggerFactory.getLogger(ForkingTargetAlgorithmEvaluatorDecorator.class);
	
	private final ExecutorService fSlaveSubmitterThreadPool = Executors.newFixedThreadPool(3,new SequentiallyNamedThreadFactory("Forking TAE Slave Submitter",true));
	
	private final TargetAlgorithmEvaluator fSlaveTAE;
	
	public ForkingTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator aMasterTAE, TargetAlgorithmEvaluator aSlaveTAE) {
		super(aMasterTAE);
		fSlaveTAE = aSlaveTAE;
	}
	
	public void evaluateRunsAsync(final List<AlgorithmRunConfiguration> runConfigs, final TargetAlgorithmEvaluatorCallback callback, final TargetAlgorithmEvaluatorRunObserver observer)
	{
		final AtomicBoolean fForkCompletionFlag = new AtomicBoolean(false);
		
		final TargetAlgorithmEvaluatorCallback forkCallback = new TargetAlgorithmEvaluatorCallback() {
			
			@Override
			public synchronized void onSuccess(List<AlgorithmRunResult> runs) {
				if(fForkCompletionFlag.compareAndSet(false, true))
				{
					callback.onSuccess(runs);
				}
			}
			
			@Override
			public synchronized void onFailure(RuntimeException e) {
				if(fForkCompletionFlag.compareAndSet(false, true))
				{
					callback.onFailure(e);
				}
				else
				{
					log.error("Received run failures after callback already notified.",e);
				}
			}
		};
		
		
		final TargetAlgorithmEvaluatorRunObserver forkObserver = new TargetAlgorithmEvaluatorRunObserver() {
			
			@Override
			public void currentStatus(List<? extends AlgorithmRunResult> runs) {
				
				if(fForkCompletionFlag.get())
				{
					for(AlgorithmRunResult run : runs)
					{
						run.kill();
					}
				}
				//TODO Perform observation.
			}
		};
		
		//Submit the job to the forked slave TAE in another thread to avoid any kind of blocking.
		fSlaveSubmitterThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				fSlaveTAE.evaluateRunsAsync(runConfigs, forkCallback, forkObserver);
			}
		});
		
		
		//Do this here and not in another thread to honor any (blocking) contracts the master TAE has.
		this.tae.evaluateRunsAsync(runConfigs, forkCallback, forkObserver);
	}

	
	/**
	 * Both TAEs runs must be final for their forked runs to be final.
	 */
	@Override
	public boolean isRunFinal()
	{
		//Return AND of both TAEs.
		return this.tae.isRunFinal() && fSlaveTAE.isRunFinal();
	}
	
	/**
	 * Both TAEs runs must be persisted for their forked runs to be persisted.
	 */
	@Override
	public boolean areRunsPersisted()
	{
		//Return AND of both TAEs.
		return this.tae.areRunsPersisted() && fSlaveTAE.areRunsPersisted();
	}
	
	@Override
	//TODO Support observation.
	public boolean areRunsObservable()
	{
		return false;
	}
	
	@Override
	protected void postDecorateeNotifyShutdown() {
		//Shutdown the forked slave TAE submitter.
		fSlaveSubmitterThreadPool.shutdownNow();
		try {
			if(!fSlaveSubmitterThreadPool.awaitTermination(60, TimeUnit.MINUTES))
			{
				log.error("Slave TAE submitter thread pool did not terminate in 60 minutes.");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		//Shutdown the forked slave TAE.
		fSlaveTAE.notifyShutdown();
	}
	
	/*
	 * {@see AbstractRunReschedulingTargetAlgorithmEvaluatorDecorator}
	 */
	
	public void throwException()
	{
		 throw new UnsupportedOperationException(this.getClass().getCanonicalName() + " does NOT support waiting/observing/reporting the number of outstanding evaluations. This is because this Target Algorithm Evaluator may schedule runs internally that should not be "
				+ "apparent to outside observers. You should rewrap this class with an instance of " + OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator.class.getCanonicalName() );
		
	}
	/**
	 * We need to throw this now because even if the lower level supplies it, we may break it.
	 * @throws UnsupportedOperationException - if the TAE does not support this operation 
	 */
	@Override
	public void waitForOutstandingEvaluations()
	{
		throwException();
	}
	
	/**
	 * We need to throw this now because even if the lower level supplies it, we may break it.
	 */
	@Override
	public int getNumberOfOutstandingEvaluations()
	{
		throwException();
		return -1;
	}
	
	@Override
	public int getNumberOfOutstandingRuns()
	{
		throwException();
		return -1;
	}
	

	
	@Override
	public int getNumberOfOutstandingBatches() {
		throwException();
		return -1;
	}

	

	@Override
	public int getRunCount() {
		throwException();
		return -1;
	}
	
	/*
	 * Thanks Ramage ;)
	 */
	
	
}
