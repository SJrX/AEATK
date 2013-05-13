package ca.ubc.cs.beta.aclib.algorithmrun;

import java.io.File;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillHandler;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableWrappedAlgorithmRun;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.logback.MarkerFilter;
import ca.ubc.cs.beta.aclib.misc.logging.LoggingMarker;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.cli.CommandLineTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.currentstatus.CurrentRunStatusObserver;

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
	public static final String AUTOMATIC_CONFIGURATOR_RESULT_REGEX = "^\\s*(Final)?\\s*[Rr]esult\\s+(?:(for)|(of))\\s+(?:(HAL)|(ParamILS)|(SMAC)|(this wrapper)):";
	
	/**
	 * Compiled REGEX
	 */
	private static final Pattern pattern = Pattern.compile(AUTOMATIC_CONFIGURATOR_RESULT_REGEX);
	
	private static transient Logger log = LoggerFactory.getLogger(CommandLineAlgorithmRun.class);
	
	
	private Queue<String> outputQueue = new ArrayDeque<String>(MAX_LINES_TO_SAVE * 2);

	/**
	 * Stores the observer for this run
	 */
	private CurrentRunStatusObserver runObserver;

	/**
	 * Stores the kill handler for this run
	 */
	private KillHandler killHandler;
	
	
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
	
	private static final double WALLCLOCK_TIMING_SLACK = 0.001;
	
	private ExecutorService threadPoolExecutor = Executors.newCachedThreadPool(); 
	
	private final int observerFrequency;
		
	
	/**
	 * This field is transient because we can't save this object when we serialize.
	 * 
	 * If after restoring serialization you need something from this object, you should
	 * save it as a separate field. (this seems unlikely) 
	 * 
	 */
	private final transient CommandLineTargetAlgorithmEvaluatorOptions options;
	/**
	 * Default Constructor
	 * @param execConfig		execution configuration of the object
	 * @param runConfig			run configuration we are executing
	 */
	public CommandLineAlgorithmRun(AlgorithmExecutionConfig execConfig, RunConfig runConfig, CurrentRunStatusObserver obs, KillHandler handler, CommandLineTargetAlgorithmEvaluatorOptions options) 
	{
		super(execConfig, runConfig);
		//TODO Test
		if(runConfig.getCutoffTime() <= 0 || handler.isKilled())
		{
			
			log.info("Cap time is negative for {} setting run as timeout", runConfig);
			String rawResultLine = "[DIDN'T BOTHER TO RUN ALGORITHM AS THE CAPTIME IS NOT POSITIVE]";
			
			this.setResult(RunResult.TIMEOUT, 0, 0, 0, runConfig.getProblemInstanceSeedPair().getSeed(), rawResultLine,"");
		}
		
		this.runObserver = obs;
		this.killHandler = handler;
		this.observerFrequency = options.observerFrequency;
		
		if(observerFrequency < 25)
		{
			throw new IllegalArgumentException("Observer Frequency can't be less than 25 milliseconds");
		}
		
		this.options = options;
	}
	
	private static final int MAX_LINES_TO_SAVE = 1000;

	private volatile boolean wasKilled = false;
	
	@Override
	public synchronized void run() {
		
		if(this.isRunCompleted())
		{
			return;
		}
		
		if(killHandler.isKilled())
		{
			
			log.debug("Run was killed", runConfig);
			String rawResultLine = "Killed Manually";
			
			this.setResult(RunResult.TIMEOUT, 0, 0, 0, runConfig.getProblemInstanceSeedPair().getSeed(), rawResultLine,"");
		}
		
		final Process proc;
		
		
		try {
			this.startWallclockTimer();
			
			proc = runProcess();
			
			final Process innerProcess = proc; 
			
			final Semaphore stdErrorDone = new Semaphore(0);
			
			Runnable standardErrorReader = new Runnable()
			{

				@Override
				public void run() {
					
					Thread.currentThread().setName("cli-tae-std-err-" + getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceID() + "-" + getRunConfig().getProblemInstanceSeedPair().getSeed());
					try { 
					Scanner procIn = new Scanner(innerProcess.getErrorStream());
					
					while(procIn.hasNext())
					{	
						log.warn("[PROCESS]  {}", procIn.nextLine());
					}
					
					procIn.close();
					} finally
					{
						stdErrorDone.release();
					}
					
				}
				
			};
			
			Runnable observerThread = new Runnable()
			{

				@Override
				public void run() {
					Thread.currentThread().setName("cli-tae-obs-" + getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceID() + "-" + getRunConfig().getProblemInstanceSeedPair().getSeed());
					while(true)
					{
						
						double currentTime = getCurrentWallClockTime() / 1000.0;
						
						runObserver.currentStatus(Collections.singletonList((KillableAlgorithmRun) new RunningAlgorithmRun(execConfig, getRunConfig(), "RUNNING," + Math.max(0,(currentTime - WALLCLOCK_TIMING_SLACK)) + ",0,0," + getRunConfig().getProblemInstanceSeedPair().getSeed(), killHandler)));
						try {
							
							
							
							//Sleep here so that maybe anything that wanted us dead will have gotten to the killHandler
							Thread.sleep(25);
							if(killHandler.isKilled())
							{
								wasKilled = true;
								log.debug("Trying to kill");
								proc.destroy();
								proc.waitFor();
								return;
							}
							Thread.sleep(observerFrequency - 25);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							break;
						}
						
					}
					
					
				}
				
			};
			
			threadPoolExecutor.execute(observerThread);
			threadPoolExecutor.execute(standardErrorReader);
			Scanner procIn = new Scanner(proc.getInputStream());
		
			processRunLoop(procIn);
			
			
			if(!this.isRunCompleted())
			{
				if(wasKilled)
				{
					double currentTime = Math.max(0,(this.getCurrentWallClockTime()/1000.0 - WALLCLOCK_TIMING_SLACK));
					this.setResult(RunResult.TIMEOUT, currentTime, 0,0, getRunConfig().getProblemInstanceSeedPair().getSeed(), "Killed Manually", "" );
					
				} else {
					this.setCrashResult("We did not successfully read anything from the wrapper");
					log.error("We did not find anything in our target algorithm run output that matched our regex (i.e. We found nothing that looked like \"Result For ParamILS: x,x,x,x,x\", specifically the regex we were matching is: {} ", AUTOMATIC_CONFIGURATOR_RESULT_REGEX );
				}
			}
			
			
			switch(this.getRunResult())
			{
			
			
			case ABORT:
			case CRASHED:
				
					
					log.info( "Failed Run Detected Call: cd {} ;  {} ",new File(execConfig.getAlgorithmExecutionDirectory()).getAbsolutePath(), getTargetAlgorithmExecutionCommand(execConfig, runConfig));
				
					log.info("Failed Run Detected output last {} lines", outputQueue.size());
					for(String s : outputQueue)
					{
						log.info(s);
					}
					log.info("Output complete");
					
				
			default:
				
			}
			
			outputQueue.clear();
			
			
			procIn.close();
			
			stdErrorDone.acquireUninterruptibly();
			threadPoolExecutor.shutdownNow();
			
			proc.destroy();
			this.stopWallclockTimer();
			runObserver.currentStatus(Collections.singletonList(new KillableWrappedAlgorithmRun(this)));
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
	
	//See:http://stackoverflow.com/questions/7804335/split-string-on-spaces-except-if-between-quotes-i-e-treat-hello-world-as
	Pattern p = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");

	/**
	 * Starts the target algorithm
	 * @return Process reference to the executiong process
	 * @throws IOException
	 */
	private  Process runProcess() throws IOException
	{
		String execCmd = getTargetAlgorithmExecutionCommand(execConfig, runConfig);
		
		if(options.logAllCallStrings)
		{
			log.info( "Call: cd {} ;  {} ", new File(execConfig.getAlgorithmExecutionDirectory()).getAbsolutePath(), execCmd);
		}
		
		ArrayList<String> args = new ArrayList<String>();

		//See:http://stackoverflow.com/questions/7804335/split-string-on-spaces-except-if-between-quotes-i-e-treat-hello-world-as
		Matcher m = p.matcher(execCmd);
		while(m.find())
		{
			args.add(m.group(1).replace("\"", ""));
		}
		
		String[] execCmdArray = args.toArray(new String[0]);
		Process proc = Runtime.getRuntime().exec(execCmdArray,null, new File(execConfig.getAlgorithmExecutionDirectory()));

		return proc;
	}
	
	
	
	/**
	 * Gets the execution command string
	 * @return string containing command
	 */
	public static String getTargetAlgorithmExecutionCommand(AlgorithmExecutionConfig execConfig, RunConfig runConfig)
	{

		StringBuilder execString = new StringBuilder();
		
		String cmd = execConfig.getAlgorithmExecutable();
		cmd = cmd.replace(AlgorithmExecutionConfig.MAGIC_VALUE_ALGORITHM_EXECUTABLE_PREFIX,"");
		
		execString.append(cmd).append(" ").append(runConfig.getProblemInstanceSeedPair().getInstance().getInstanceName()).append(" ").append(runConfig.getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation()).append(" ").append(runConfig.getCutoffTime()).append(" ").append(Integer.MAX_VALUE).append(" ").append(runConfig.getProblemInstanceSeedPair().getSeed()).append(" ").append(runConfig.getParamConfiguration().getFormattedParamString(StringFormat.NODB_SYNTAX));
		
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
		
		if(options.logAllProcessOutput)
		{
			log.debug("[PROCESS]  {}" ,line);
		}
		

		if (matcher.find())
		{
			
			String fullLine = line.trim();
			String additionalRunData = "";
			try
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
				String runtime = results[1].trim();
				String runLength = results[2].trim();
				String bestSolution = results[3].trim();
				String seed = results[4].trim();
				if(results.length <= 5)
				{ //This is a good case

				} else if(results.length == 6)
				{
					additionalRunData = results[5].trim();
				} else
				{
					log.warn("Too many fields were encounted (expected 5 or 6) when parsing line (Additional Run Data cannot have commas): {}\n ",line);
				}
				
				double runLengthD = Double.valueOf(runLength);
				double runtimeD = Double.valueOf(runtime);
				double qualityD = Double.valueOf(bestSolution);
				long resultSeedD = Long.valueOf(seed);
				if(!MarkerFilter.log(fullProcessOutputMarker.getName()))
				{
					log.info("Algorithm Reported: {}" , line);
				}
				
				this.setResult(acResult, runtimeD, runLengthD, qualityD, resultSeedD, rawResultLine, additionalRunData);
			} catch(NumberFormatException e)
			{	 //Numeric value is probably at fault
				this.setCrashResult("Output:" + fullLine + "\n Exception Message: " + e.getMessage() + "\n Name:" + e.getClass().getCanonicalName());
				Object[] args = { getTargetAlgorithmExecutionCommand(execConfig, runConfig), fullLine};
				log.error("Target Algorithm Call failed:{}\nResponse:{}\nComment: Most likely one of the values of runLength, runtime, quality could not be parsed as a Double, or the seed could not be parsed as a valid long", args);
				log.error("Exception that occured trying to parse result was: ", e);
				log.error("Run will be counted as {}", RunResult.CRASHED);
					
			} catch(IllegalArgumentException e)
			{ 	//The RunResult probably doesn't match anything
				this.setCrashResult("Output:" + fullLine + "\n Exception Message: " + e.getMessage() + "\n Name:" + e.getClass().getCanonicalName());
				
				
				ArrayList<String> validValues = new ArrayList<String>();
				for(RunResult r : RunResult.values())
				{
					validValues.addAll(r.getAliases());
				}
				Collections.sort(validValues);
				
				String[] validArgs = validValues.toArray(new String[0]);
				
				
				Object[] args = { getTargetAlgorithmExecutionCommand(execConfig, runConfig), fullLine, Arrays.toString(validArgs)};
				log.error("Target Algorithm Call failed:{}\nResponse:{}\nComment: Most likely the Algorithm did not report a result string as one of: {}", args);
				log.error("Exception that occured trying to parse result was: ", e);
				log.error("Run will be counted as {}", RunResult.CRASHED);
				
			} catch(ArrayIndexOutOfBoundsException e)
			{	//There aren't enough commas in the output
				this.setCrashResult("Output:" + fullLine + "\n Exception Message: " + e.getMessage() + "\n Name:" + e.getClass().getCanonicalName());
				Object[] args = { getTargetAlgorithmExecutionCommand(execConfig, runConfig), fullLine};
				log.error("Target Algorithm Call failed:{}\nResponse:{}\nComment: Most likely the algorithm did not specify all of the required outputs that is <solved>,<runtime>,<runlength>,<quality>,<seed>", args);
				log.error("Exception that occured trying to parse result was: ", e);
				log.error("Run will be counted as {}", RunResult.CRASHED);
			}
			
			
		}
	}

	
	
	
	
	
	
}
