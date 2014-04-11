package ca.ubc.cs.beta.aeatk.algorithmrun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aeatk.runconfig.RunConfig;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineAlgorithmRun;
/**
 * Class that is used to take an existing algorithm run (from for instance a string), and create an AlgorithmRun object
 * @author seramage
 */
public class ExistingAlgorithmRun extends AbstractAlgorithmRun {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	private static transient Logger log = LoggerFactory.getLogger(ExistingAlgorithmRun.class);
	
	/**
	 * 
	 * @param runConfig			run configuration we are executing
	 * @param runResult			The RunResult to report
	 * @param runtime 			The Run Time
	 * @param runLength			The Run Length
	 * @param quality			The Run Quality
	 * @param resultSeed 		The Reported seed
	 * @param additionalRunData	The Additional Run Data
	 * @param wallclockTime		Wallclock time to report
	 */
	public ExistingAlgorithmRun(RunConfig runConfig, RunResult runResult, double runtime, double runLength, double quality, long resultSeed, String additionalRunData, double wallclockTime)
	{
		super( runConfig,runResult, runtime, runLength, quality, resultSeed, "<Existing Run>", additionalRunData, wallclockTime);
	}
	
	/**
	 * 
	 * @param runConfig			run configuration we are executing
	 * @param runResult			The RunResult to report
	 * @param runtime 			The Run Time
	 * @param runLength			The Run Length
	 * @param quality			The Run Quality
	 * @param resultSeed 		The Reported seed
	 * @param wallclockTime		Wallclock time to report
	 */

	public ExistingAlgorithmRun( RunConfig runConfig, RunResult runResult, double runtime, double runLength, double quality, long resultSeed,  double wallclockTime)
	{
		super(runConfig,runResult, runtime, runLength, quality, resultSeed, "<Existing Run>", "", wallclockTime);
	}
	
	
	

	/**
	 * 
	 * @param runConfig			run configuration we are executing
	 * @param runResult			The RunResult to report
	 * @param runtime 			The Run Time
	 * @param runlength			The Run Length
	 * @param quality			The Run Quality
	 * @param seed 				The Reported seed
	 * @param additionalRunData	The Additional Run Data
	 */

	public ExistingAlgorithmRun( RunConfig runConfig, RunResult runResult, double runtime, double runlength, double quality, long seed, String additionalRunData)
	{
		this( runConfig, runResult, runtime,runlength, quality, seed, additionalRunData, 0.0);
	}
	

	/**
	 * 
	 * @param runConfig			run configuration we are executing
	 * @param runResult			The RunResult to report
	 * @param runtime 			The Run Time
	 * @param runlength			The Run Length
	 * @param quality			The Run Quality
	 * @param seed 				The Reported seed
	 */
	public ExistingAlgorithmRun( RunConfig runConfig, RunResult runResult, double runtime, double runlength, double quality, long seed)
	{
		this( runConfig, runResult, runtime,runlength, quality, seed, "", 0.0);
	}
	
	
	
	
	
	/**
	 * 
	 * @param execConfig		execution configuration of the object
	 * @param runConfig			run configuration we are executing
	 * @param runResult			The RunResult to report
	 * @param runtime 			The Run Time
	 * @param runLength			The Run Length
	 * @param quality			The Run Quality
	 * @param resultSeed 		The Reported seed
	 * @param additionalRunData	The Additional Run Data
	 * @param wallclockTime		Wallclock time to report
	 */
	@Deprecated
	public ExistingAlgorithmRun(AlgorithmExecutionConfig execConfig, RunConfig runConfig, RunResult runResult, double runtime, double runLength, double quality, long resultSeed, String additionalRunData, double wallclockTime)
	{
		super(runConfig,runResult, runtime, runLength, quality, resultSeed, "<Existing Run>", additionalRunData, wallclockTime);
	}
	
	/**
	 * 
	 * @param execConfig		execution configuration of the object
	 * @param runConfig			run configuration we are executing
	 * @param runResult			The RunResult to report
	 * @param runtime 			The Run Time
	 * @param runLength			The Run Length
	 * @param quality			The Run Quality
	 * @param resultSeed 		The Reported seed
	 * @param wallclockTime		Wallclock time to report
	 */
	@Deprecated
	public ExistingAlgorithmRun(AlgorithmExecutionConfig execConfig, RunConfig runConfig, RunResult runResult, double runtime, double runLength, double quality, long resultSeed,  double wallclockTime)
	{
		
		super(runConfig,runResult, runtime, runLength, quality, resultSeed, "<Existing Run>", "", wallclockTime);
		
	}
	
	
	

	/**
	 * 
	 * @param execConfig		execution configuration of the object
	 * @param runConfig			run configuration we are executing
	 * @param runResult			The RunResult to report
	 * @param runtime 			The Run Time
	 * @param runlength			The Run Length
	 * @param quality			The Run Quality
	 * @param seed 				The Reported seed
	 * @param additionalRunData	The Additional Run Data
	 */
	@Deprecated
	public ExistingAlgorithmRun(AlgorithmExecutionConfig execConfig, RunConfig runConfig, RunResult runResult, double runtime, double runlength, double quality, long seed, String additionalRunData)
	{
		this(execConfig, runConfig, runResult, runtime,runlength, quality, seed, additionalRunData, 0.0);
	}
	

	/**
	 * 
	 * @param execConfig		execution configuration of the object
	 * @param runConfig			run configuration we are executing
	 * @param runResult			The RunResult to report
	 * @param runtime 			The Run Time
	 * @param runlength			The Run Length
	 * @param quality			The Run Quality
	 * @param seed 				The Reported seed
	 */
	@Deprecated
	public ExistingAlgorithmRun(AlgorithmExecutionConfig execConfig, RunConfig runConfig, RunResult runResult, double runtime, double runlength, double quality, long seed)
	{
		this(execConfig, runConfig, runResult, runtime,runlength, quality, seed, "", 0.0);
	}
	
	@Deprecated
	public static ExistingAlgorithmRun getRunFromString( RunConfig runConfig, String result)
	{
		return getRunFromString(runConfig, result, 0);
	}
	
	/**
	 * Default Constructor
	 * @param execConfig		execution configuration of the object
	 * @param runConfig			run configuration we are executing
	 * @param result			result string to parse. The format of this is currently everything after the : in the result line of {@link CommandLineAlgorithmRun}. We support both the String for the RunResult, as well as the Status Code
	 * @deprecated  the constructor that doesn't take a result string is preferred. 
	 */
	@Deprecated
	public static ExistingAlgorithmRun getRunFromString( RunConfig runConfig, String result, double wallClockTime) {
		
		//this.rawResultLine = resultLine;
		//this.runCompleted = true;
		String[] resultLine = result.split(",");
		
		try
		{
			RunResult acResult;
			try {
				acResult = RunResult.getAutomaticConfiguratorResultForCode(Integer.valueOf(resultLine[0]));
			} catch(NumberFormatException e)
			{
				acResult = RunResult.getAutomaticConfiguratorResultForKey(resultLine[0]);
			}
			
			
			double runtime = Double.valueOf(resultLine[1].trim());
			double runLength = Double.valueOf(resultLine[2].trim());
			double quality = Double.valueOf(resultLine[3].trim());
			long resultSeed = Long.valueOf(resultLine[4].trim());
			String additionalRunData = "";
			if(resultLine.length == 6)
			{
				additionalRunData = resultLine[5].trim();
			}
			
			
			return new ExistingAlgorithmRun(runConfig, acResult, runtime, runLength, quality, resultSeed, additionalRunData,wallClockTime);
			
			
		} catch(ArrayIndexOutOfBoundsException e)
		{ 
			Object[] args = { runConfig, result} ;
			

			log.debug("Malformed Run Result for Execution (ArrayIndexOutOfBoundsException): {}, Instance: {}, Result: {}", args);
			log.debug("Exception:",e);

			
			
			return getAbortResult(runConfig, e.getMessage());
		}catch(NumberFormatException e)
		{
			//There was a problem with the output, we just set this flag
			log.debug("Malformed Run Result for Execution (NumberFormatException):  Instance: {}, Result: {}", runConfig, result);
			log.debug("Exception:",e);

			return getAbortResult(runConfig, e.getMessage());
			
			
		}
		
		

	}
	
	public static ExistingAlgorithmRun getAbortResult(RunConfig rc, String message)
	{
		return new ExistingAlgorithmRun(rc, RunResult.ABORT, 0, 0, 0, 0, "ERROR:" +message,0);
	}

	@Override
	public void kill() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRunResultWellFormed() {
		// TODO Auto-generated method stub
		return false;
	}


}
