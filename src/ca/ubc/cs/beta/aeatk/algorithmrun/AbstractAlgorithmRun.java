package ca.ubc.cs.beta.aeatk.algorithmrun;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ca.ubc.cs.beta.aeatk.exceptions.IllegalWrapperOutputException;
import ca.ubc.cs.beta.aeatk.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aeatk.json.serializers.AlgorithmRunJson;
import ca.ubc.cs.beta.aeatk.runconfig.RunConfig;

/**
 * This class represents a single run of the target algorithm given by the AlgorithmExecutionConfig object and the RunConfig object
 * 
 * @author seramage
 *
 */
@JsonSerialize(using=AlgorithmRunJson.AlgorithmRunSerializer.class)
public abstract class AbstractAlgorithmRun implements AlgorithmRun
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1860615761848618478L;
	
	protected final RunConfig runConfig;
	//protected final AlgorithmExecutionConfig execConfig;
	
	/*
	 * Values reported by the target algorithm
	 */
	private final RunResult acResult;
	
	private final double runtime;
	private final double runLength;
	private final double quality;
	 
	
	/**
	 * Raw result line reported by the target algorithm (potentially useful if the result line is corrupt)
	 */
	private final String rawResultLine;
		
	/**
	 * true if the result has been set,
	 * if this is false most methods will throw an IllegalStateException
	 */
	
	//private final boolean resultSet;
	/**
	 * True if the run was well formed
	 * Note: We may deprecate this in favor of using CRASHED
	 */
	//private final boolean runResultWellFormed;
	
	/**
	 * Wallclock Time to return
	 */
	private final double wallClockTime;
	
	/**
	 * Watch that can be used to time algorithm runs 
	 */
	//private	final StopWatch wallClockTimer = new StopWatch();
	
	/**
	 * Stores additional run data
	 */
	private final String additionalRunData;
	
	
	//private static final Logger log = LoggerFactory.getLogger(AbstractAlgorithmRun.class);
	
	/**
	 * Sets the values for this Algorithm Run
	 * @param acResult					The result of the Run
	 * @param runtime					Reported runtime of the run
	 * @param runLength					Reported runlength of the run
	 * @param quality					Reported quality of the run
	 * @param resultSeed				Reported seed of the run
	 * @param rawResultLine				The Raw result line we got
	 * @param additionalRunData			Additional Run Data
	 *
	protected synchronized void setResult(RunResult acResult, double runtime, double runLength, double quality, long resultSeed, String rawResultLine, String additionalRunData)
	{
		this.setResult(acResult, runtime, runLength, quality, resultSeed, rawResultLine, true, additionalRunData);
	}
	
	/**
	 * Marks this run as aborted
	 * @param rawResultLine  the raw output that might be relevant to this abort
	 *
	protected void setAbortResult(String rawResultLine)
	{
		this.setResult(RunResult.ABORT, runConfig.getCutoffTime(), 0, 0, runConfig.getProblemInstanceSeedPair().getSeed(), rawResultLine, "");
	}
	
	/**
	 * Marks this run as aborted
	 * @param rawResultLine  the raw output that might be relevant to this crash
	 *
	protected void setCrashResult(String rawResultLine)
	{
		this.setResult(RunResult.CRASHED, runConfig.getCutoffTime(), 0, 0, runConfig.getProblemInstanceSeedPair().getSeed(), rawResultLine,rawResultLine);
	}
	
	/*
	protected void startWallclockTimer()
	{
		wallClockTimer.start();
	}
	
	protected void stopWallclockTimer()
	{
		this.wallClockTime = wallClockTimer.stop() / 1000.0;
	}
	
	protected long getCurrentWallClockTime()
	{
		return this.wallClockTimer.time();
	}
	*/
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
	 *
	protected synchronized void setResult(RunResult acResult, double runtime, double runLength, double quality, long resultSeed , String rawResultLine, boolean runResultWellFormed, String additionalRunData)
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
		this.acResult = acResult;
		this.runtime = Math.min(runtime, Double.MAX_VALUE);
		this.runLength = Math.min(runLength, Double.MAX_VALUE);
		this.quality = quality;
		
		if(this.saveRawResultLine())
		{
			this.rawResultLine = rawResultLine;
		}
		
		this.runResultWellFormed = runResultWellFormed;

	
		
		if(this.additionalRunData == null)
		{
			throw new IllegalArgumentException("Additional Run Data cannot be NULL");
		} else
		{
			this.additionalRunData = additionalRunData.replace("\n","\\n").replace(',', ';');
		}
		
		this.resultSet = true;
		if(!(this instanceof KillableAlgorithmRun))
		{
			
			if(this.acResult.equals(RunResult.RUNNING))
			{
				throw new IllegalStateException("Only " + KillableAlgorithmRun.class.getSimpleName() + " may be set as " + RunResult.RUNNING);
			}
		}
		
		
	}*/
	
	
	/**
	 * Default Constructor
	 * @param execConfig		execution configuration of the object
	 * @param runConfig			run configuration we are executing
	 *
	public AbstractAlgorithmRun( RunConfig runConfig)
	{
		if(runConfig == null)
		{
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		
		this.runConfig = runConfig;

	}*/
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
	protected AbstractAlgorithmRun(RunConfig rc, RunResult acResult, double runtime, double runLength, double quality, long resultSeed, String rawResultLine, String additionalRunData, double wallClockTime)
	{
		this(rc, acResult, runtime, runLength, quality, resultSeed, rawResultLine, true, additionalRunData,wallClockTime);
	}
	
	/**
	 * Marks this run as aborted
	 * @param rawResultLine  the raw output that might be relevant to this abort
	 */
	/*
	protected void setAbortResult(String rawResultLine)
	{
		this.setResult(RunResult.ABORT, runConfig.getCutoffTime(), 0, 0, runConfig.getProblemInstanceSeedPair().getSeed(), rawResultLine, "");
	}
	
	/**
	 * Marks this run as aborted
	 * @param rawResultLine  the raw output that might be relevant to this crash
	 */
	/*
	protected static AlgorithmRun setCrashResult(String rawResultLine)
	{
		this.setResult(RunResult.CRASHED, runConfig.getCutoffTime(), 0, 0, runConfig.getProblemInstanceSeedPair().getSeed(), rawResultLine,rawResultLine);
	}
	
	/*
	protected void startWallclockTimer()
	{
		wallClockTimer.start();
	}
	
	protected void stopWallclockTimer()
	{
		this.wallClockTime = wallClockTimer.stop() / 1000.0;
	}
	
	protected long getCurrentWallClockTime()
	{
		return this.wallClockTimer.time();
	}
	*/
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
	public AbstractAlgorithmRun(RunConfig runConfig, RunResult acResult, double runtime, double runLength, double quality, long resultSeed , String rawResultLine, boolean runResultWellFormed, String additionalRunData, double wallClockTime)
	{
		
		if(acResult.equals(RunResult.TIMEOUT))
		{
			System.err.println("Hello");
		}
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
				
		this.acResult = acResult;
		this.runtime = Math.min(runtime, Double.MAX_VALUE);
		this.runLength = Math.min(runLength, Double.MAX_VALUE);
		this.quality = quality;
		
		if(this.saveRawResultLine())
		{
			this.rawResultLine = rawResultLine;
		} else
		{
			this.rawResultLine = "";
		}
		
		//this.runResultWellFormed = runResultWellFormed;

	
		
		if(additionalRunData == null)
		{
			this.additionalRunData ="";
		} else
		{
			this.additionalRunData = additionalRunData.replace("\n","\\n").replace(',', ';');
		}
		
		this.wallClockTime = wallClockTime;
		//this.resultSet = true;
		/*
		if(!(this instanceof KillableAlgorithmRun))
		{
			
			if(this.acResult.equals(RunResult.RUNNING))
			{
				throw new IllegalStateException("Only " + KillableAlgorithmRun.class.getSimpleName() + " may be set as " + RunResult.RUNNING);
			}
		}
		*/
		
	}
	
	
	
	
	
	
	
	@Override
	public final AlgorithmExecutionConfig getExecutionConfig()
	{
		return runConfig.getAlgorithmExecutionConfig();
	}
	
	@Override
	public final RunConfig getRunConfig()
	{
		return runConfig;
	}
	
	
	@Override
	public final RunResult getRunResult() {
		
		return acResult;
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
	public final long getResultSeed() {

		return this.getRunConfig().getProblemInstanceSeedPair().getSeed();
	}
	
	private final String _getResultLine()
	{
		return getResultLine(this);
	}
	
	public static final String getResultLine(AlgorithmRun run)
	{
		String resultLine = run.getRunResult().name() + ", " + run.getRuntime() + ", " + run.getRunLength() + ", " + run.getQuality() + ", " + run.getResultSeed();
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
		return !acResult.equals(RunResult.RUNNING);
	}

	
	@Override
	public final String rawResultLine()
	{
		
		if(saveRawResultLine())
		{
			return rawResultLine;
		} else
		{
			return "[Raw Result Line Not Saved]";
		}
		
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
		
		if(o instanceof AlgorithmRun)
		{
			AlgorithmRun aro = (AlgorithmRun) o;
			return aro.getRunConfig().equals(runConfig);
		} 
		return false;
	}
	
	@Override 
	public String toString()
	{
		return toString(this);
	}
	
	
	public static String toString(AlgorithmRun run)
	{
		return run.getRunConfig().toString() + " ==> <" + run.getResultLine()+ "> W:(" + run.getWallclockExecutionTime() + ")";
		
	}
	
	/**
	 * Sets the wallclock time for this target algorithm
	 * @param time time in seconds that the algorithm executed
	 */
	/*
	protected void setWallclockExecutionTime(double time)
	{
		if(time < 0) throw new IllegalArgumentException("Time must be positive");
		this.wallClockTime = time;
	}
	*/
	
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
		return ((getRunResult().equals(RunResult.TIMEOUT) && getRunConfig().hasCutoffLessThanMax()) ||  (getRunResult().equals(RunResult.KILLED) && getRuntime() < getRunConfig().getCutoffTime()));
	}
	

	private boolean saveRawResultLine()
	{
		return false;
	}

	
}
