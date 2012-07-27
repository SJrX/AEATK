package ca.ubc.cs.beta.aclib.algorithmrun;

import java.io.File;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.logback.MarkerFilter;
import ca.ubc.cs.beta.aclib.misc.logging.LoggingMarker;
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
	
	
	private Queue<String> outputQueue = new ArrayDeque<String>(MAX_LINES_TO_SAVE * 2);
	
	/**
	 * Marker for logging
	 */
	private static transient Marker execCommandMarker = MarkerFactory.getMarker(LoggingMarker.COMMAND_LINE_CALL.name());
	/**
	 * Marker for logging
	 */
	private static transient Marker fullProcessOutputMarker = MarkerFactory.getMarker(LoggingMarker.FULL_PROCESS_OUTPUT.name());
	
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
	
	private static final int MAX_LINES_TO_SAVE = 1000;


	@Override
	public synchronized void run() {
		
		if(isRunCompleted())
		{
			return;
		}
		
		Process proc;
		
		
		
		try {
			this.startWallclockTimer();
			proc = runProcess();
			Scanner procIn = new Scanner(proc.getInputStream());
		
			processRunLoop(procIn);
			
			switch(this.getRunResult())
			{
			case ABORT:
			case CRASHED:
				
				if(!MarkerFilter.log(execCommandMarker.getName()))
				{
					
					log.info( "Failed Run Detected Call: " + getTargetAlgorithmExecutionCommand(execConfig, runConfig));
				}
				if(!MarkerFilter.log(fullProcessOutputMarker.getName()))
				{
				
					
					
					log.info("Failed Run Detected output last {} lines", outputQueue.size());
					for(String s : outputQueue)
					{
						log.info(s);
					}
					log.info("Output complete");
					
				}
			default:
				
			}
			
			outputQueue.clear();
			
			
			
			procIn = new Scanner(proc.getErrorStream());
	
			while(procIn.hasNext())
			{	
				
				log.warn(procIn.nextLine());
				
			}
			
			
			
			procIn.close();
			proc.destroy();
			this.stopWallclockTimer();
		} catch (IOException e1) {
			String execCmd = getTargetAlgorithmExecutionCommand(execConfig,runConfig);
			log.error("Failed to execute command: {}", execCmd);
			throw new IllegalStateException(e1);
		}
		
		
		
		
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
			outputQueue.add(line);
			if (outputQueue.size() > MAX_LINES_TO_SAVE)
			{
				outputQueue.poll();
			}
			
			processLine(line);
			
		}	
		procIn.close();
	}
	
	

	/**
	 * Starts the target algorithm
	 * @return Process reference to the executiong process
	 * @throws IOException
	 */
	private  Process runProcess() throws IOException
	{
		String execCmd = getTargetAlgorithmExecutionCommand(execConfig, runConfig);
		
		if(MarkerFilter.log(execCommandMarker.getName()))
		{
			log.info( "Calling: " + execCmd);
		}
		
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
	 *	Process a single line of the output looking for a matching line (e.g. Result for ParamILS: ...)
	 *	@param line of program output
	 */
	public void processLine(String line)
	{
		Matcher matcher = pattern.matcher(line);
		String rawResultLine = "[No Matching Output Found]";
		if(MarkerFilter.log(fullProcessOutputMarker.getName()))
		{
			log.debug(line);
		}
		

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
			
			
			
			try
			{
				int solved = acResult.getResultCode();
				String runtime = results[1];
				String runLength = results[2];
				String bestSolution = results[3];
				String seed = results[4];

				
				double runLengthD = Double.valueOf(runLength);
				double runtimeD = Double.valueOf(runtime);
				double qualityD = Double.valueOf(bestSolution);
				long resultSeedD = Long.valueOf(seed);
				if(!MarkerFilter.log(fullProcessOutputMarker.getName()))
				{
					log.info("Algorithm Reported: {}" , line);
				}
				
				this.setResult(acResult, runtimeD, runLengthD, qualityD, resultSeedD, rawResultLine);
			} catch(NumberFormatException e)
			{
				this.setCrashResult(rawResultLine + "\n" + e.getMessage());
			} catch(ArrayIndexOutOfBoundsException e)
			{
				this.setCrashResult(rawResultLine + "\n Could not parse result " + e.getMessage());
			}
			
			
		}
	}
	
	
	
	
	
	
}
