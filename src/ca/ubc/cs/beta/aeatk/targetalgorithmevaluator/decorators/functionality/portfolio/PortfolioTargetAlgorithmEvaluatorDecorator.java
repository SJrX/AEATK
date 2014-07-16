package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.functionality.portfolio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.ExistingAlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunningAlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.kill.KillHandler;
import ca.ubc.cs.beta.aeatk.exceptions.DeveloperMadeABooBooException;
import ca.ubc.cs.beta.aeatk.misc.associatedvalue.Pair;
import ca.ubc.cs.beta.aeatk.objectives.RunObjective;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorHelper;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.AbstractTargetAlgorithmEvaluatorDecorator;

import com.beust.jcommander.ParameterException;


/**
 * Tries to solve the target algorithm using a portfolio of other solvers & configurations.
 * 
 * For every run that comes in, a new set of runs is created with the associated parameter solvers & configurations 
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class PortfolioTargetAlgorithmEvaluatorDecorator extends	AbstractTargetAlgorithmEvaluatorDecorator {

	//private final Map<ProblemInstance, AlgorithmRun> portfolioMaps;
	private final RunObjective runObj;

	private final Logger log = LoggerFactory.getLogger(getClass());
	private List<ParameterConfiguration> portfolioConfigurations;

	private double kappaMax;
	
	public PortfolioTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae, List<Pair<AlgorithmExecutionConfiguration, ParameterConfiguration>> portfolio, RunObjective runObj, double kappaMax, String s)
	{
		//TODO Support pairs of exec configuration options.
		this(tae, Collections.EMPTY_LIST,runObj,kappaMax);
		
	}
	
	
	public PortfolioTargetAlgorithmEvaluatorDecorator(TargetAlgorithmEvaluator tae, List<ParameterConfiguration> portfolio, RunObjective runObj, double kappaMax)
	{
		super(tae);
	
		if(portfolio.size() == 0)
		{
			throw new IllegalArgumentException("Portfolio must contain at least one configuration");
		}
		
		
		
		this.portfolioConfigurations = Collections.unmodifiableList(new  ArrayList<ParameterConfiguration>(portfolio));
		
		if(new HashSet<>(portfolioConfigurations).size() != portfolioConfigurations.size())
		{
			throw new IllegalArgumentException("Portfolio must be constructed with distinct configurations, duplicates detected");
		}
			
		this.runObj = runObj;
		this.kappaMax = kappaMax;
		
		if(!runObj.equals(RunObjective.RUNTIME))
		{
			throw new ParameterException("Hydra supports only Runtime portfolio construction at this point");
		}
	}	
	
	private final ConcurrentHashMap<AlgorithmRunConfiguration, AlgorithmRunConfiguration> inverseTranslationMap = new ConcurrentHashMap<AlgorithmRunConfiguration, AlgorithmRunConfiguration>();
	
	@Override
	public final List<AlgorithmRunResult> evaluateRun(List<AlgorithmRunConfiguration> runConfigs, TargetAlgorithmEvaluatorRunObserver obs) {
		return TargetAlgorithmEvaluatorHelper.evaluateRunSyncToAsync(runConfigs, this, obs);
	}

	@Override
	public final void evaluateRunsAsync(List<AlgorithmRunConfiguration> runConfigs,	final TargetAlgorithmEvaluatorCallback oHandler, final TargetAlgorithmEvaluatorRunObserver obs) {
		
		
		/** WHAT DOES THIS COMMENT MEAN? **/
		//We need to make sure wrapped versions are called in the same order
		//as there unwrapped versions.
	
		
		if(runConfigs.isEmpty())
		{
			oHandler.onSuccess(Collections.EMPTY_LIST);
			return;
		}
		
		final int requestedRuns = runConfigs.size();
		
		
		//If this current configuration is in the portfolio we will remove it-
		//Assuming that all runConfigs share the same configuration 
		//This removal will ensure that we don't the same configuration twice
		//This is not the perfect test (what if configurations are different).
		final List<ParameterConfiguration> localPortfolio = new ArrayList<ParameterConfiguration>(portfolioConfigurations);
		localPortfolio.remove(runConfigs.get(0).getParameterConfiguration());
		
		
		final int portfolioSize = localPortfolio.size();
		
		
		
		/**
		 * Note that for every run we need to do say runs [1,2,3,4] we are submitting them in the following order
		 * [1, 1A, 1B, 1C, 1D, 1E, 2, 2A, 2B, 2C, 2D, 2E, ...]
		 * 
		 */
		//TODO: This class should have an option and should work in all cases correctly when you do and do not
		//want the original run to be submted.
		final List<AlgorithmRunConfiguration> runsToSubmitToTAE = new ArrayList<AlgorithmRunConfiguration>();
		
		for(AlgorithmRunConfiguration rc : runConfigs)
		{
			runsToSubmitToTAE.add(rc);
			
			for(ParameterConfiguration pConfig : localPortfolio)
			{
				runsToSubmitToTAE.add(new AlgorithmRunConfiguration(rc.getProblemInstanceSeedPair(),rc.getCutoffTime(),pConfig, rc.getAlgorithmExecutionConfiguration()));
			}
		}
		
		
		TargetAlgorithmEvaluatorCallback myHandler = new TargetAlgorithmEvaluatorCallback()
		{
			private final TargetAlgorithmEvaluatorCallback handler = oHandler;

			@Override
			public void onSuccess(List<AlgorithmRunResult> runs) 
			{	
				
				List<AlgorithmRunResult> listToReturn = new ArrayList<>();
						
				for(int i=0; i < runs.size(); i += portfolioSize + 1)
				{
					/**
					* We assume that our returned runs for this batch are in [i, i+portfolioSize+1)
					* For instance if we are asked for 4 runs [1,2,3,4] , then we will submit 16 in order of [1A, 1B, 1C, 1D, 2A, 2B, 2C 2D,...]-
					* So to determine the result of the portfolio we should look at runs nA, nB, nC, nD,...
					* 
					* getAlgorithmRunForCallback handles the logic for merging the runs
					**/
					listToReturn.add(getAlgorithmRunForCallback(runs.subList(i, i+portfolioSize+1)));
				}
				handler.onSuccess(listToReturn);
			}

			private AlgorithmRunResult getAlgorithmRunForCallback(final List<AlgorithmRunResult> runs)
			{
				
			
				final AlgorithmRunResult realRun = runs.get(0);
				
				//Run with lowest objective
				AlgorithmRunResult bestRun = runs.get(0);
				
				//Run with lowest objective that didn't time out.
				AlgorithmRunResult bestSolvedRun = null;
				
				
				for(AlgorithmRunResult run : runs)
				{
					if(runObj.getObjective(run) < runObj.getObjective(bestRun))
					{
						bestRun = run;
					}
					
					
					if(!run.isCensoredEarly())
					{
						if(bestSolvedRun == null)
						{
							bestSolvedRun = run;
						} else
						{
							if(runObj.getObjective(run) < runObj.getObjective(bestSolvedRun))
							{
								bestSolvedRun = run;
							}
						}
					}
				}
				
				
				if(bestSolvedRun != null && !bestSolvedRun.equals(bestRun))
				{
					log.error("Best solved run {} doesn't equal best run {} in responses: {} ", bestSolvedRun, bestRun, runs );
					throw new IllegalStateException("Values that are killed or timeout are better than those that are solved");
				}
				
				log.trace("Best run is {} out of {}", bestRun, runs);
				
				if(bestRun.equals(realRun))
				{
					
					return bestRun;
				} else
				{
					return new ExistingAlgorithmRunResult(realRun.getAlgorithmRunConfiguration(),bestRun.getRunStatus(), bestRun.getRuntime(), bestRun.getRunLength(), bestRun.getQuality(), bestRun.getResultSeed(), "Returned from from portfolio, configuration: " + bestRun.getAlgorithmRunConfiguration().getParameterConfiguration().getFriendlyID() + " my performance: ",bestRun.getWallclockExecutionTime());
					
					
				}
			}
			@Override
			public void onFailure(RuntimeException t) {
					handler.onFailure(t);
			}

		
		};
		
		
		TargetAlgorithmEvaluatorRunObserver myObs = new TargetAlgorithmEvaluatorRunObserver()
		{

			
			Set<AlgorithmRunResult> killedRuns = Collections.newSetFromMap(new ConcurrentHashMap<AlgorithmRunResult,Boolean>()); 
			@Override
			public void currentStatus(final List<? extends AlgorithmRunResult> runs) {
				
				List<AlgorithmRunResult> kRunsToClient = new ArrayList<AlgorithmRunResult>();
								
				for(int i=0; i < runs.size(); i += portfolioSize + 1)
				{
					/**
					 * See similar line in callback, we basically split the runs into all the runs for the requested run config.
					 */
					kRunsToClient.add(getAlgorithmRunForPortfolioObserver(runs.subList(i, i+portfolioSize+1)));	
				}
				
				
				if(obs != null)
				{
					obs.currentStatus(kRunsToClient);
				}
				
			}
			
			
			public AlgorithmRunResult getAlgorithmRunForPortfolioObserver(final List<? extends AlgorithmRunResult> runs)
			{
				AlgorithmRunResult originalRun = runs.get(0);
				
				boolean atleastOneRunStillRunning = false;

				//Best run that is solved
				AlgorithmRunResult bestSolvedRun = null;
				
				//Run with lowest run objective
				AlgorithmRunResult bestRun = runs.get(0);
				
				for(AlgorithmRunResult krun : runs)
				{
					if(krun.isRunCompleted())
					{
						if( (bestSolvedRun == null) || (runObj.getObjective(krun) < runObj.getObjective(bestSolvedRun)))
						{
							bestSolvedRun = krun;
						}
					} else
					{
						atleastOneRunStillRunning = true;
					}
					
					if(runObj.getObjective(krun) < runObj.getObjective(bestRun))
					{
						bestRun = krun;
					}
					
				}
					
				if(bestSolvedRun != null)
				{
					for(AlgorithmRunResult krun : runs)
					{
						if(krun.getRunStatus().equals(RunStatus.RUNNING))
						{
							if(runObj.getObjective(krun) > runObj.getObjective(bestSolvedRun))
							{
								if(killedRuns.add(krun))
								{
									log.trace("Run {} seems to be dominated by run {}, killing...", krun, bestSolvedRun);
								}
								
								krun.kill();
							}
						}
					}
				}
				

				
				if(atleastOneRunStillRunning)
				{
					KillHandler kh = new KillHandler()
					{
						AtomicBoolean bKill = new AtomicBoolean(false);

						@Override
						public void kill() {
							bKill.getAndSet(true);
							
							for(AlgorithmRunResult run : runs)
							{
								run.kill();
							}
						}

						@Override
						public boolean isKilled() {
							return bKill.get();
							
						}
						
					};
					return new RunningAlgorithmRunResult(originalRun.getAlgorithmRunConfiguration(), bestRun.getRuntime(), bestRun.getRunLength(), bestRun.getQuality(), bestRun.getResultSeed(), bestRun.getWallclockExecutionTime(), kh);
				} else
				{
					return new ExistingAlgorithmRunResult(originalRun.getAlgorithmRunConfiguration(), bestRun.getRunStatus(), bestRun.getRuntime(), bestRun.getRunLength(), bestRun.getQuality(), bestRun.getResultSeed(), bestRun.getWallclockExecutionTime());
				}
			}


		
			
		};
	
		
		
		log.info("Portfolio request translated runs {} to {} ", runConfigs.size(), runsToSubmitToTAE.size() );
		tae.evaluateRunsAsync(runsToSubmitToTAE, myHandler, myObs);

	}
	
	
	
		
	@Override
	protected void postDecorateeNotifyShutdown() {
		// TODO Auto-generated method stub
		
	}	

}
