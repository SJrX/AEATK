package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.concurrent.threadfactory.SequentiallyNamedThreadFactory;

/**
 * Abstract TargetAlgorithmEvaluator that implements a basic form of asynchronous execution.
 * <br>
 * <b>Note:</b> Calls will just be made in a separate thread  
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
public abstract class AbstractSyncTargetAlgorithmEvaluator extends
		AbstractTargetAlgorithmEvaluator {

    
    private final ExecutorService execService;
	
	
    public AbstractSyncTargetAlgorithmEvaluator() {
		this(false);
		
		
	}
    
	public AbstractSyncTargetAlgorithmEvaluator(boolean unlimitedThreads) {
		super();
		if(unlimitedThreads)
		{
			execService = Executors.newCachedThreadPool(new SequentiallyNamedThreadFactory(this.getClass().getSimpleName() + " Abstract Blocking TAE Async Processing Thread (Cached)"));
		} else
		{
			execService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),(new SequentiallyNamedThreadFactory(this.getClass().getSimpleName() + " Abstract Blocking TAE Async Processing Thread (Available Processors " + Runtime.getRuntime().availableProcessors() + ")")));
		}
		
	}
	
	/**
	 * Construct an abstract synchronous target algorithm evaluator, limiting the number of threads ever created to given limit.
	 * @param aThreads - limit on threads ever created.
	 */
	public AbstractSyncTargetAlgorithmEvaluator(int aThreads)
	{
	    super();
	    execService = Executors.newFixedThreadPool(aThreads, new SequentiallyNamedThreadFactory(this.getClass().getSimpleName() + " Abstract Blocking TAE Async Processing Thread (Explicit " + aThreads + ")"));
	}

	@Override
	public  void evaluateRunsAsync(final List<AlgorithmRunConfiguration> runConfigs,
			final TargetAlgorithmEvaluatorCallback handler, final TargetAlgorithmEvaluatorRunObserver obs) {
		
		Runnable run = new Runnable()
		{

			@Override
			public void run() {
				
				try {
					List<AlgorithmRunResult> runs = AbstractSyncTargetAlgorithmEvaluator.this.evaluateRun(runConfigs, obs);
					
					handler.onSuccess(runs);
				} catch(RuntimeException e)
				{
					handler.onFailure(e);
				} catch(Throwable t)
				{
					
					handler.onFailure(new IllegalStateException("Unexpected throwable detected",t));
					
					if(t instanceof Error)
					{
						throw t;
					}
				}
			}
			
		};
		
		
		
		if(this.areRunsPersisted())
		{
			//Need to ensure that the runs get checked for being done.
			//I don't remember why this case is here and I don't think anything ever
			//returns true that implements this.
			run.run();
		} else
		{
			execService.execute(run);
		}

	}

	/**
	 * Template method for ensuring subtype gets notified. 
	 */
	protected abstract void subtypeShutdown();
	
	
	/**
	 * We must be notified of the shutdown, so we will prevent subtypes from overriding this method.
	 */
	@Override
	public final void notifyShutdown()
	{
		execService.shutdown();
		Logger log = LoggerFactory.getLogger(this.getClass());
		try 
		{
			
			try {
				boolean shutdown = execService.awaitTermination(120, TimeUnit.SECONDS);
				if(!shutdown)
				{
					log.warn("Outstanding evaluations on Target Algorithm Evaluator did not complete within 120 seconds, will try to interrupt currently executing tasks.");
				} 
			} catch (InterruptedException e) {
				
				Thread.currentThread().interrupt();
				log.warn("Interrupted while waiting for TAE shutdown");
			}
			
			execService.shutdownNow();
				
			boolean shutdown;
				
			try {
				shutdown = execService.awaitTermination(120, TimeUnit.SECONDS);
				if(!shutdown)
				{
					LoggerFactory.getLogger(this.getClass()).warn("Outstanding evaluations on Target Algorithm Evaluator did not complete within 120 seconds, even after interruption");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.warn("Interrupted while waiting for TAE shutdown after interruption");
			
			}
				
		} finally
		{
			this.subtypeShutdown();
		}
	}
}
