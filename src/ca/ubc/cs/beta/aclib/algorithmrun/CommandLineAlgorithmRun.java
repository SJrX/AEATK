package ca.ubc.cs.beta.aclib.algorithmrun;

import java.io.File;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

/**
 * Executes a Target Algorithm Run via Command Line Execution
 * @author sjr
 *
 */
public class CommandLineAlgorithmRun extends AbstractAlgorithmRun {

	
	private static final long serialVersionUID = -70897405824987641L;
	
	/**
	 * Regex that we hope to match
	 */
	public static final String AUTOMATIC_CONFIGURATOR_RESULT_REGEX = "(Final)?\\s*[Rr]esult\\s+(?:(for)|(of))\\s+(?:(HAL)|(ParamILS)|(SMAC)|(this wrapper)):";
	
	/**
	 * Compiled REGEX
	 */
	private static final Pattern pattern = Pattern.compile(AUTOMATIC_CONFIGURATOR_RESULT_REGEX);
	
	private static transient Logger log = LoggerFactory.getLogger(CommandLineAlgorithmRun.class);
	
	/**
	 * Marker for logging
	 */
	private static transient Marker execCommandMarker = MarkerFactory.getMarker("Execution Command");
	/**
	 * Marker for logging
	 */
	private static transient Marker fullProcessOutputMarker = MarkerFactory.getMarker("Full Process Output");
	
	static {
		log.warn("This version of SMAC hardcodes run length for calls to the target algorithm to {}.", Integer.MAX_VALUE);
	}
	
	/**
	 * Default Constructor
	 * @param execConfig		execution configuration of the object
	 * @param runConfig			run configuration we are executing
	 */
	public CommandLineAlgorithmRun(AlgorithmExecutionConfig execConfig, RunConfig runConfig) 
	{
		super(execConfig, runConfig);
		
		if(runConfig.getCutoffTime() <= 0)
		{
			
			log.info("Cap time is negative for {} setting run as timedout", runConfig);
			String rawResultLine = "[DIDN'T BOTHER TO RUN ALGORITHM AS THE CAPTIME IS NOT POSITIVE NEGATIVE]";
			
			this.setResult(RunResult.TIMEOUT, 0, 0, 0, runConfig.getProblemInstanceSeedPair().getSeed(), rawResultLine);
		}
	}


	@Override
	public synchronized void run() {
		
		if(isRunCompleted())
		{
			return;
		}
		
		Process proc;
		
		try {
			proc = runProcess();
			Scanner procIn = new Scanner(proc.getInputStream());
		
			processRunLoop(procIn);
		
			procIn = new Scanner(proc.getErrorStream());
			
			while(procIn.hasNext())
			{
				log.warn(fullProcessOutputMarker,procIn.nextLine());
				
				
			}
			
			procIn.close();
			proc.destroy();
		} catch (IOException e1) {
			String execCmd = getTargetAlgorithmExecutionCommand(execConfig,runConfig);
			log.error("Failed to execute command: {}", execCmd);
			throw new IllegalStateException(e1);
		}
		
		
		
		
	}
	

	/**
	 * Starts the target algorithm
	 * @return Process reference to the executiong process
	 * @throws IOException
	 */
	private  Process runProcess() throws IOException
	{
		String execCmd = getTargetAlgorithmExecutionCommand(execConfig, runConfig);
		log.info(execCommandMarker, execCmd);
		Process proc = Runtime.getRuntime().exec(execCmd,null, new File(execConfig.getAlgorithmExecutionDirectory()));
	
		
		return proc;
	}
	
	
	
	/**
	 * Gets the execution command string
	 * @return string containing command
	 */
	public static String getTargetAlgorithmExecutionCommand(AlgorithmExecutionConfig execConfig, RunConfig runConfig)
	{

		StringBuilder execString = new StringBuilder();
		execString.append(execConfig.getAlgorithmExecutable()).append(" ").append(runConfig.getProblemInstanceSeedPair().getInstance().getInstanceName()).append(" ").append(runConfig.getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation()).append(" ").append(runConfig.getCutoffTime()).append(" ").append(Integer.MAX_VALUE).append(" ").append(runConfig.getProblemInstanceSeedPair().getSeed()).append(" ").append(runConfig.getParamConfiguration().getFormattedParamString(StringFormat.NODB_SYNTAX));
		return execString.toString();
	}

	/**
	 * Processes all the output of the target algorithm
	 * 
	 * Takes a line from the input and tries to parse it 
	 * 
	 * @param procIn Scanner of processes output stream
	 */
	public void processRunLoop(Scanner procIn)
	{
		while(procIn.hasNext())
		{
			String line = procIn.nextLine();
			processLine(line);
			
		}	
		procIn.close();
	}
	
	
	/**
	 *	Process a single line of the output looking for a matching line (e.g. Result for ParamILS: ...)
	 *	@param line of program output
	 */
	public void processLine(String line)
	{
		Matcher matcher = pattern.matcher(line);
		String rawResultLine = "[No Matching Output Found]";
		log.debug(fullProcessOutputMarker,line);
		

		if (matcher.find())
		{
			
			String acExecResultString = line.substring(matcher.end()).trim();
			
			String[] results = acExecResultString.split(",");
			for(int i=0; i < results.length; i++)
			{
				results[i] = results[i].trim();
			}
			
			rawResultLine = acExecResultString;
			
			RunResult acResult =  RunResult.getAutomaticConfiguratorResultForKey(results[0]);
			
			int solved = acResult.getResultCode();
			String runtime = results[1];
			String runLength = results[2];
			String bestSolution = results[3];
			String seed = results[4];

			try
			{
				double runLengthD = Double.valueOf(runLength);
				double runtimeD = Double.valueOf(runtime);
				double qualityD = Double.valueOf(bestSolution);
				long resultSeedD = Long.valueOf(seed);
				
				this.setResult(acResult, runtimeD, runLengthD, qualityD, resultSeedD, rawResultLine);
			} catch(NumberFormatException e)
			{
				this.setCrashResult(rawResultLine + "\n" + e.getMessage());
			}	
			
			
		}
	}
	
	
	
	
	
	
}
