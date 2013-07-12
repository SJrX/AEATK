package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli;

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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AbstractAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.RunResult;
import ca.ubc.cs.beta.aclib.algorithmrun.RunningAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillHandler;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableAlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.kill.KillableWrappedAlgorithmRun;
import ca.ubc.cs.beta.aclib.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.logback.MarkerFilter;
import ca.ubc.cs.beta.aclib.misc.logging.LoggingMarker;
import ca.ubc.cs.beta.aclib.misc.string.SplitQuotedString;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.exceptions.TargetAlgorithmAbortException;

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
	private TargetAlgorithmEvaluatorRunObserver runObserver;

	/**
	 * Stores the kill handler for this run
	 */
	private KillHandler killHandler;
	
	

	/**
	 * Marker for logging
	 */
	private static transient Marker fullProcessOutputMarker = MarkerFactory.getMarker(LoggingMarker.FULL_PROCESS_OUTPUT.name());
	
	static {
		log.warn("This version of SMAC hardcodes run length for calls to the target algorithm to {}.", Integer.MAX_VALUE);
	}
	
	private static final double WALLCLOCK_TIMING_SLACK = 0.001;
	
	private ExecutorService threadPoolExecutor = Executors.newCachedThreadPool(new SequentiallyNamedThreadFactory("Command Line Target Algorithm Evaluator Thread ")); 
	
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
	public CommandLineAlgorithmRun(AlgorithmExecutionConfig execConfig, RunConfig runConfig, TargetAlgorithmEvaluatorRunObserver obs, KillHandler handler, CommandLineTargetAlgorithmEvaluatorOptions options) 
	{
		super(execConfig, runConfig);
		//TODO Test
		if(runConfig.getCutoffTime() <= 0 || handler.isKilled())
		{
			
			log.info("Cap time is less than or equal to zero for {} setting run as timeout", runConfig);
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
			
			this.setResult(RunResult.KILLED, 0, 0, 0, runConfig.getProblemInstanceSeedPair().getSeed(), rawResultLine,"");
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
					
					Thread.currentThread().setName("Command Line Target Algorithm Evaluator Thread (Standard Error Processor)" + getRunConfig() );
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
					Thread.currentThread().setName("Command Line Target Algorithm Evaluator Thread (Observer)" + getRunConfig());
					while(true)
					{
						
						double currentTime = getCurrentWallClockTime() / 1000.0;
						
						runObserver.currentStatus(Collections.singletonList((KillableAlgorithmRun) new RunningAlgorithmRun(execConfig, getRunConfig(),  Math.max(0,(currentTime - WALLCLOCK_TIMING_SLACK)),  0,0, getRunConfig().getProblemInstanceSeedPair().getSeed(), killHandler)));
						try {
							
							
							
							//Sleep here so that maybe anything that wanted us dead will have gotten to the killHandler
							Thread.sleep(25);
							if(killHandler.isKilled())
							{
								wasKilled = true;
								log.debug("Trying to kill");
								proc.destroy();
								log.debug("Process destroy() called now waiting for completion");
								proc.waitFor();
								log.debug("Process has exited");
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
					this.setResult(RunResult.KILLED, currentTime, 0,0, getRunConfig().getProblemInstanceSeedPair().getSeed(), "Killed Manually", "" );
					
				} else {
					this.setCrashResult("Wrapper did not output anything that matched our regex please see the manual for more information. Please try executing the wrapper directly and ensuring that it matches the following regex: " + AUTOMATIC_CONFIGURATOR_RESULT_REGEX );
				}
			}
			
			
			switch(this.getRunResult())
			{
			
			
			case ABORT:
			case CRASHED:
				
					
					log.error( "Failed Run Detected Call: cd {} ;  {} ",new File(execConfig.getAlgorithmExecutionDirectory()).getAbsolutePath(), getTargetAlgorithmExecutionCommand(execConfig, runConfig));
				
					log.error("Failed Run Detected output last {} lines", outputQueue.size());
					
					
					for(String s : outputQueue)
					{
						log.error("> "+s);
					}
					log.error("Output complete");
					
				
			default:
				
			}
			
			outputQueue.clear();
			
			
			procIn.close();
			proc.destroy();
			this.stopWallclockTimer();
			
			stdErrorDone.acquireUninterruptibly();
			threadPoolExecutor.shutdownNow();
			
			try {
				threadPoolExecutor.awaitTermination(24, TimeUnit.HOURS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			
			
			runObserver.currentStatus(Collections.singletonList(new KillableWrappedAlgorithmRun(this)));
		} catch (IOException e1) {
			String execCmd = getTargetAlgorithmExecutionCommandAsString(execConfig,runConfig);
			log.error("Failed to execute command: {}", execCmd);
			throw new TargetAlgorithmAbortException(e1);
			//throw new IllegalStateException(e1);
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
		String[] execCmdArray = getTargetAlgorithmExecutionCommand(execConfig, runConfig);
		
		
		if(options.logAllCallStrings)
		{
			log.info( "Call: cd \"{}\" ;  {} ", new File(execConfig.getAlgorithmExecutionDirectory()).getAbsolutePath(), getTargetAlgorithmExecutionCommandAsString(execConfig, runConfig));
		}
		
		Process proc = Runtime.getRuntime().exec(execCmdArray,null, new File(execConfig.getAlgorithmExecutionDirectory()));

		return proc;
	}
	
	
	
	/**
	 * Gets the execution command string
	 * @return string containing command
	 */
	private static String[] getTargetAlgorithmExecutionCommand(AlgorithmExecutionConfig execConfig, RunConfig runConfig)
	{

				
		String cmd = execConfig.getAlgorithmExecutable();
		cmd = cmd.replace(AlgorithmExecutionConfig.MAGIC_VALUE_ALGORITHM_EXECUTABLE_PREFIX,"");
		
		
		String[] execCmdArray = SplitQuotedString.splitQuotedString(cmd);
		
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(execCmdArray));
		list.add(runConfig.getProblemInstanceSeedPair().getInstance().getInstanceName());
		list.add(runConfig.getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation());
		list.add(String.valueOf(runConfig.getCutoffTime()));
		list.add(String.valueOf(Integer.MAX_VALUE));
		list.add(String.valueOf(runConfig.getProblemInstanceSeedPair().getSeed()));
		
		StringFormat f = StringFormat.NODB_SYNTAX;
		
		for(String key : runConfig.getParamConfiguration().getActiveParameters() )
		{
			
			
			if(!f.getKeyValueSeperator().equals(" ") || !f.getGlue().equals(" "))
			{
				throw new IllegalStateException("Key Value seperator or glue is not a space, and this means the way we handle this logic won't work currently");
			}
			list.add(f.getPreKey() + key);
			list.add(f.getValueDelimeter() + runConfig.getParamConfiguration().get(key)  + f.getValueDelimeter());	
			
		}
		
		
		//execString.append(cmd).append(" ").append().append(" ").append().append(" ").append().append(" ").append().append(" ").append().append(" ").append();
		
		return list.toArray(new String[0]);
	}
	
	/**
	 * Gets the execution command string
	 * @return string containing command
	 */
	public static String getTargetAlgorithmExecutionCommandAsString(AlgorithmExecutionConfig execConfig, RunConfig runConfig)
	{

				
		String cmd = execConfig.getAlgorithmExecutable();
		cmd = cmd.replace(AlgorithmExecutionConfig.MAGIC_VALUE_ALGORITHM_EXECUTABLE_PREFIX,"");
		
		
		String[] execCmdArray = SplitQuotedString.splitQuotedString(cmd);
		
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(execCmdArray));
		list.add(runConfig.getProblemInstanceSeedPair().getInstance().getInstanceName());
		list.add(runConfig.getProblemInstanceSeedPair().getInstance().getInstanceSpecificInformation());
		list.add(String.valueOf(runConfig.getCutoffTime()));
		list.add(String.valueOf(Integer.MAX_VALUE));
		list.add(String.valueOf(runConfig.getProblemInstanceSeedPair().getSeed()));
		
		StringFormat f = StringFormat.NODB_SYNTAX;
		for(String key : runConfig.getParamConfiguration().getActiveParameters()  )
		{
			
			
			if(!f.getKeyValueSeperator().equals(" ") || !f.getGlue().equals(" "))
			{
				throw new IllegalStateException("Key Value seperator or glue is not a space, and this means the way we handle this logic won't work currently");
			}
			list.add(f.getPreKey() + key);
			list.add(f.getValueDelimeter() + runConfig.getParamConfiguration().get(key)  + f.getValueDelimeter());	
			
		}
		
		
		StringBuilder sb = new StringBuilder();
		for(String s : list)
		{
			if(s.matches(".*\\s+.*"))
			{
				sb.append("\""+s + "\"");
			} else
			{
				sb.append(s);
			}
			sb.append(" ");
		}
		
		
		//execString.append(cmd).append(" ").append().append(" ").append().append(" ").append().append(" ").append().append(" ").append().append(" ").append();
		
		return sb.toString();
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
				
				if(!acResult.permittedByWrappers())
				{
					throw new IllegalArgumentException(" The Run Result reported is NOT permitted to be output by a wrapper and is for internal SMAC use only.");
				}
				
					
					
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
					if(r.permittedByWrappers())
					{
						validValues.addAll(r.getAliases());
					}
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
