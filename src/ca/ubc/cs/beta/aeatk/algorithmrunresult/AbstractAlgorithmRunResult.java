package ca.ubc.cs.beta.aeatk.algorithmrunresult;

import ca.ubc.cs.beta.aeatk.algorithmrunresult.factory.AlgorithmRunResultFactory;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.exceptions.IllegalWrapperOutputException;
import ca.ubc.cs.beta.aeatk.json.serializers.AlgorithmRunResultJson;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class represents a single run of the target algorithm given by the AlgorithmExecutionConfig object and the RunConfig object
 * 
 * @author seramage
 *
 */
@JsonSerialize(using=AlgorithmRunResultJson.AlgorithmRunSerializer.class)
public abstract class AbstractAlgorithmRunResult implements AlgorithmRunResult
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1860615761848618478L;
	
	protected final AlgorithmRunConfiguration runConfig;

	private final RunExecutionStatus runExecutionStatus;

	private final Satisfiability satisfiability;

	private final double runtime;
	private final double runLength;
	private final double quality;
		
	/**
	 * true if the result has been set,
	 * if this is false most methods will throw an IllegalStateException
	 */

	/**
	 * Wallclock Time to return
	 */
	private final double wallClockTime;
	

	/**
	 * Stores additional run data
	 */
	private final String additionalRunData;


    private final Map<String, Object> resultMap;

	/**
	 * Sets the values for this Algorithm Run
	 * @param acResult					The result of the Run
	 * @param runtime					Reported runtime of the run
	 * @param runLength					Reported runlength of the run
	 * @param quality					Reported quality of the run
	 * @param resultSeed				Reported seed of the run
	 * @param rawResultLine				The Raw result line we got
	 * @param additionalRunData			Additional Run Data
	 */
	protected AbstractAlgorithmRunResult(AlgorithmRunConfiguration rc, RunStatus acResult, double runtime, double runLength, double quality, long resultSeed, String rawResultLine, String additionalRunData, double wallClockTime)
	{
		this(rc, acResult, runtime, runLength, quality, resultSeed, rawResultLine, true, additionalRunData,wallClockTime);
	}


	/**
	 * Sets the values for this Algorithm Run
	 * @param acResult					RunResult for this run
	 * @param runtime					runtime measured
	 * @param runLength					runlength measured
	 * @param quality					quality measured
	 * @param resultSeed				resultSeed 
	 * @param rawResultLine				raw result line
	 * @param runResultWellFormed		whether this run has well formed output
	 * @param additionalRunData			additional run data from this run
	 */
	public AbstractAlgorithmRunResult(AlgorithmRunConfiguration runConfig, Map<String, Object> resultMap, double wallClockTime)
	{

		double runtime;


		if(resultMap.containsKey(AlgorithmRunResultFactory.RUNTIME_KEY))
		{
			runtime =  (double) resultMap.get(AlgorithmRunResultFactory.RUNTIME_KEY);
		} else
		{
			runtime = wallClockTime;
		}

		if(Double.isNaN(runtime) || runtime < 0)
		{
			throw new IllegalWrapperOutputException("Runtime is NaN or negative", resultMap);
		}

		double runLength = 0;

		if(resultMap.containsKey(AlgorithmRunResultFactory.RUNLENGTH_KEY))
		{
			runLength = (double) resultMap.get(AlgorithmRunResultFactory.RUNLENGTH_KEY);
		}
			
		if ( Double.isNaN(runLength) || ((runLength < 0) && (runLength != -1.0)))
		{
			throw new IllegalWrapperOutputException("RunLength (" + runLength + ") is NaN or negative (and not -1)", resultMap);
		}

		double cost = 0;

        if(resultMap.containsKey(AlgorithmRunResultFactory.COST_KEY))
        {
            cost = (double) resultMap.get(AlgorithmRunResultFactory.COST_KEY);
        }

        double quality = cost;

		if(Double.isNaN(quality))
		{
			throw new IllegalWrapperOutputException("Cost needs to be a number", resultMap);
		}

        this.runExecutionStatus = RunExecutionStatus.valueOf(String.valueOf(resultMap.get(AlgorithmRunResultFactory.STATUS_KEY)));

		if(runExecutionStatus == null)
		{
			throw new IllegalWrapperOutputException("Run Result cannot be null", resultMap);
		}


        this.satisfiability = Satisfiability.fromString( (String) resultMap.get(AlgorithmRunResultFactory.SATISFIABILITY_KEY));
		if(runConfig == null)
		{
			throw new IllegalArgumentException("No RunConfig specified");
		}
		
		
		if(wallClockTime < 0 )
		{
			throw new IllegalArgumentException("WallClock time must be greater than or equal to zero");
		}

		this.runConfig = runConfig;

		this.runtime = Math.min(runtime, Double.MAX_VALUE);
		this.runLength = Math.min(runLength, Double.MAX_VALUE);
		this.quality = quality;

        String additionalRunData = (String) resultMap.get(AlgorithmRunResultFactory.ADDITIONAL_RUN_DATA_KEY);

		if(additionalRunData == null)
		{
			this.additionalRunData ="";
		} else
		{
			this.additionalRunData = additionalRunData.replace("\n","\\n").replace(',', ';');
		}
		
		this.wallClockTime = wallClockTime;

        Map<String, Object> myMap = new TreeMap<>(AlgorithmRunResultKeyComparator.getInstance());
        myMap.putAll(resultMap);
        this.resultMap = Collections.unmodifiableMap(myMap);
	}

	/**
	 * Sets the values for this Algorithm Run
	 * @param acResult					RunResult for this run
	 * @param runtime					runtime measured
	 * @param runLength					runlength measured
	 * @param quality					quality measured
	 * @param resultSeed				resultSeed
	 * @param rawResultLine				raw result line
	 * @param runResultWellFormed		whether this run has well formed output
	 * @param additionalRunData			additional run data from this run
	 */
	public AbstractAlgorithmRunResult(AlgorithmRunConfiguration runConfig, RunStatus acResult, double runtime, double runLength, double quality, long resultSeed , String rawResultLine, boolean runResultWellFormed, String additionalRunData, double wallClockTime)
	{

		if(Double.isNaN(runtime) || runtime < 0)
		{
			throw new IllegalWrapperOutputException("Runtime is NaN or negative", rawResultLine);
		}

		if ( Double.isNaN(runLength) || ((runLength < 0) && (runLength != -1.0)))
		{
			throw new IllegalWrapperOutputException("RunLength (" + runLength + ") is NaN or negative (and not -1)", rawResultLine);
		}

		if(Double.isNaN(quality))
		{
			throw new IllegalWrapperOutputException("Quality needs to be a number", rawResultLine);
		}

		if(acResult == null)
		{
			throw new IllegalStateException("Run Result cannot be null");
		}

		if(runConfig == null)
		{
			throw new IllegalArgumentException("No RunConfig specified");
		}


		if(wallClockTime < 0 )
		{
			throw new IllegalArgumentException("Wallclock time must be greater than or equal to zero");
		}
		this.runConfig = runConfig;

		this.runtime = Math.min(runtime, Double.MAX_VALUE);
		this.runLength = Math.min(runLength, Double.MAX_VALUE);
		this.quality = quality;

		if(additionalRunData == null)
		{
			this.additionalRunData ="";
		} else
		{
			this.additionalRunData = additionalRunData.replace("\n","\\n").replace(',', ';');
		}


        this.runExecutionStatus = acResult.getRunExecutionStatus();
        this.satisfiability = acResult.getSatisfiability();
		this.wallClockTime = wallClockTime;

        Map<String, Object> resultMap = new TreeMap<>(new AlgorithmRunResultKeyComparator());

        resultMap.put(AlgorithmRunResultFactory.RUNTIME_KEY,runtime);
        resultMap.put(AlgorithmRunResultFactory.COST_KEY, quality);
        resultMap.put(AlgorithmRunResultFactory.STATUS_KEY, runExecutionStatus.toString());
        resultMap.put(AlgorithmRunResultFactory.SATISFIABILITY_KEY, runExecutionStatus.toString());

        this.resultMap = Collections.unmodifiableMap(resultMap);
	}

	
	@Override
	public final AlgorithmRunConfiguration getAlgorithmRunConfiguration()
	{
		return runConfig;
	}
	

    @Override
    public final Satisfiability getSatisfiability()
    {
        return this.satisfiability;
    }

    @Override
    public final RunExecutionStatus getRunExecutionStatus()
    {
        return this.runExecutionStatus;
    }

	@Override
	public final RunStatus getRunStatus() {
		
		switch(this.getRunExecutionStatus())
        {
            case CRASHED:
                return RunStatus.CRASHED;
            case KILLED:
                return RunStatus.KILLED;
            case TIMEOUT:
                return RunStatus.TIMEOUT;
            case ABORT:
                return RunStatus.ABORT;
            case RUNNING:
                return RunStatus.RUNNING;
            case SUCCESS:
                switch(getSatisfiability())
                {
                    case SATISFIABILE:
                    case UNKNOWN:
                        return RunStatus.SAT;
                    case UNSATISFIABLE:
                        return RunStatus.UNSAT;
                }
        }

        throw new IllegalStateException("Should not have been able to get here, unexpected RunExecutionStatus " + getRunExecutionStatus() + " or satisfiability: " + getSatisfiability());
	}

	@Override
	public final double getRuntime() {
		
		return runtime;
	}

	@Override
	public final double getRunLength() {

		return runLength;
	}

	@Override
	public final double getQuality() {

		return quality;
	}

    @Override
    public final double getCost()
    {
        return quality;
    }

	@Override
	public final long getResultSeed() {

		return this.getAlgorithmRunConfiguration().getProblemInstanceSeedPair().getSeed();
	}
	
	private final String _getResultLine()
	{
		return getResultLine(this);
	}
	
	public static final String getResultLine(AlgorithmRunResult run)
	{
		String resultLine = run.getRunStatus().name() + ", " + run.getRuntime() + ", " + run.getRunLength() + ", " + run.getQuality() + ", " + run.getResultSeed();
		if(run.getAdditionalRunData().trim().length() > 0)
		{
			resultLine += "," + run.getAdditionalRunData();
		}
		return resultLine;
	}
	
	@Override
	public final String getResultLine() {
		return _getResultLine();
	}

	@Override
	public final boolean isRunCompleted() {
		return !runExecutionStatus.equals(RunExecutionStatus.RUNNING);
	}

	
	@Override
	public final String rawResultLine()
	{
        return "[Raw Result Line Not Saved]";
	}
	
	@Override
	public final int hashCode()
	{
		//I believe that just instanceConfig and not execConfig hashCodes should be good enough
		//Since it's rare for two different execConfigs to have identical instanceConfigs
		return runConfig.hashCode();
	}
	
	/**
	 * Two AlgorithmRuns are considered equal if they have same runConfig and execConfig
	 */
	@Override
	public final boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null) return false;
		
		if(o instanceof AlgorithmRunResult)
		{
			AlgorithmRunResult aro = (AlgorithmRunResult) o;
			return aro.getAlgorithmRunConfiguration().equals(runConfig);
		} 
		return false;
	}
	
	@Override 
	public String toString()
	{
		return toString(this);
	}
	
	
	public static String toString(AlgorithmRunResult run)
	{
		return run.getAlgorithmRunConfiguration().toString() + " ==> <" + run.getResultLine()+ "> W:(" + run.getWallclockExecutionTime() + ")";
		
	}

	@Override
	public double getWallclockExecutionTime() {
		return wallClockTime;
	}
	
	@Override
	public String getAdditionalRunData() {
	
		return additionalRunData;
	}
	
	@Override
	public boolean isCensoredEarly()
	{
		return ((getRunStatus().equals(RunStatus.TIMEOUT) && getAlgorithmRunConfiguration().hasCutoffLessThanMax()) ||  (getRunStatus().equals(RunStatus.KILLED) && getRuntime() < getAlgorithmRunConfiguration().getCutoffTime()));
	}

	@Override
	public ParameterConfiguration getParameterConfiguration() {
		
		return getAlgorithmRunConfiguration().getParameterConfiguration();
	}


	@Override
	public AlgorithmExecutionConfiguration getAlgorithmExecutionConfiguration() {
		
		return getAlgorithmRunConfiguration().getAlgorithmExecutionConfiguration();
	}


	@Override
	public ProblemInstanceSeedPair getProblemInstanceSeedPair() {

		return getAlgorithmRunConfiguration().getProblemInstanceSeedPair();
	}


	@Override
	public ProblemInstance getProblemInstance() {
		return getAlgorithmRunConfiguration().getProblemInstanceSeedPair().getProblemInstance();
	}
	
	

}
