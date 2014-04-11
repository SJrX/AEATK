package ca.ubc.cs.beta.aeatk.initialization.classic;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aeatk.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aeatk.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aeatk.exceptions.OutOfTimeException;
import ca.ubc.cs.beta.aeatk.initialization.InitializationProcedure;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;
import ca.ubc.cs.beta.aeatk.runconfig.RunConfig;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistoryHelper;
import ca.ubc.cs.beta.aeatk.runhistory.ThreadSafeRunHistory;
import ca.ubc.cs.beta.aeatk.seedgenerator.InstanceSeedGenerator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.termination.TerminationCondition;

public class ClassicInitializationProcedure implements InitializationProcedure {

	private final ThreadSafeRunHistory runHistory;
	private final ParamConfiguration initialIncumbent;
	private final TargetAlgorithmEvaluator tae;
	private final ClassicInitializationProcedureOptions opts;
	private final Logger log = LoggerFactory.getLogger(ClassicInitializationProcedure.class);
	private final int maxIncumbentRuns;
	private final List<ProblemInstance> instances;
	private final InstanceSeedGenerator insc;
	private final ParamConfiguration incumbent;
	private final TerminationCondition termCond;
	private final double cutoffTime;
	private final SeedableRandomPool pool;
	private boolean deterministicInstanceOrdering;
	private final AlgorithmExecutionConfiguration algorithmExecutionConfig;
	

	public ClassicInitializationProcedure(ThreadSafeRunHistory runHistory, ParamConfiguration initialIncumbent, TargetAlgorithmEvaluator tae, ClassicInitializationProcedureOptions opts, InstanceSeedGenerator insc, List<ProblemInstance> instances,  int maxIncumbentRuns , TerminationCondition termCond, double cutoffTime, SeedableRandomPool pool, boolean deterministicInstanceOrdering, AlgorithmExecutionConfiguration execConfig)
	{
		this.runHistory =runHistory;
		this.initialIncumbent = initialIncumbent;
		this.tae = tae;
		this.opts = opts;
		this.instances = instances;
		this.maxIncumbentRuns = maxIncumbentRuns;
		this.insc = insc;
		this.incumbent = initialIncumbent;
		this.termCond = termCond;
		this.cutoffTime = cutoffTime;
		this.pool = pool;
		this.deterministicInstanceOrdering = deterministicInstanceOrdering;
		this.algorithmExecutionConfig = execConfig;
		
		
	}
	
	@Override
	public void run() {
		log.debug("Using Classic Initialization");
		ParamConfiguration incumbent = this.initialIncumbent;
		log.trace("Configuration Set as Incumbent: {}", incumbent);
		
		//iteration = 0;
		
		
		int N= opts.initialIncumbentRuns;
		
		N = Math.min(N, instances.size());
		N = Math.min(N, maxIncumbentRuns);
		log.debug("Scheduling default configuration for {} runs",N);
		for(int i=0; i <N; i++)
		{
			
			/**
			 * Evaluate Default Configuration
			 */
			
			ProblemInstanceSeedPair pisp = RunHistoryHelper.getRandomInstanceSeedWithFewestRunsFor(runHistory, insc, incumbent, instances, pool.getRandom("CLASSIC_INITIALIZATION"),deterministicInstanceOrdering);

			RunConfig incumbentRunConfig = new RunConfig(pisp, cutoffTime,incumbent, algorithmExecutionConfig);


			//Create initial row

			try { 
				evaluateRun(incumbentRunConfig);
			} catch(OutOfTimeException e)
			{
				log.warn("Ran out of time while evaluating the default configuration on the first run, this is most likely a configuration error");
				//Ignore this exception
				//Force the incumbent to be logged in RunHistory and then we will timeout next
				throw new IllegalStateException("Out of time on the first run");
				
			}
			
		}
		
	}

	@Override
	public ParamConfiguration getIncumbent() {
		return incumbent;
	}
	
	protected List<AlgorithmRun> evaluateRun(RunConfig runConfig)
	{
		return evaluateRun(Collections.singletonList(runConfig));
	}
	
	
	protected List<AlgorithmRun> evaluateRun(List<RunConfig> runConfigs)
	{
	
		if(termCond.haveToStop())
		{
			throw new OutOfTimeException();
		}
		log.debug("Initialization: Scheduling {} run(s):",  runConfigs.size());
		for(RunConfig rc : runConfigs)
		{
			Object[] args = {  runHistory.getThetaIdx(rc.getParamConfiguration())!=-1?" "+runHistory.getThetaIdx(rc.getParamConfiguration()):"", rc.getParamConfiguration(), rc.getProblemInstanceSeedPair().getProblemInstance().getInstanceID(),  rc.getProblemInstanceSeedPair().getSeed(), rc.getCutoffTime()};
			log.debug("Initialization: Scheduling run for config{} ({}) on instance {} with seed {} and captime {}", args);
		}
		
		List<AlgorithmRun> completedRuns = tae.evaluateRun(runConfigs);
		
		for(AlgorithmRun run : completedRuns)
		{
			RunConfig rc = run.getRunConfig();
			Object[] args = {  runHistory.getThetaIdx(rc.getParamConfiguration())!=-1?" "+runHistory.getThetaIdx(rc.getParamConfiguration()):"", rc.getParamConfiguration(), rc.getProblemInstanceSeedPair().getProblemInstance().getInstanceID(),  rc.getProblemInstanceSeedPair().getSeed(), rc.getCutoffTime(), run.getResultLine(),  run.getWallclockExecutionTime()};
			log.debug("Initialization: Completed run for config{} ({}) on instance {} with seed {} and captime {} => Result: {}, wallclock time: {} seconds", args);
		}
		
		
		
		updateRunHistory(completedRuns);
		return completedRuns;
	}
	
	/**
	 * 
	 * @return the input parameter (unmodified, simply for syntactic convience)
	 */
	protected List<AlgorithmRun> updateRunHistory(List<AlgorithmRun> runs)
	{
		for(AlgorithmRun run : runs)
		{
			try {
					runHistory.append(run);
			} catch (DuplicateRunException e) {
				//We are trying to log a duplicate run
				throw new IllegalStateException(e);
			}
		}
		return runs;
	}
	


}
