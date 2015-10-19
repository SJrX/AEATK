
package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import ca.ubc.cs.beta.aeatk.algorithmrunresult.*;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.factory.AlgorithmRunResultFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.callformatselector.AclibCallFormat;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.callformatselector.CallFormatSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.util.concurrent.AtomicDouble;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.kill.KillHandler;
import ca.ubc.cs.beta.aeatk.concurrent.threadfactory.SequentiallyNamedThreadFactory;
import ca.ubc.cs.beta.aeatk.misc.associatedvalue.Pair;
import ca.ubc.cs.beta.aeatk.misc.logging.LoggingMarker;
import ca.ubc.cs.beta.aeatk.misc.string.SplitQuotedString;
import ca.ubc.cs.beta.aeatk.misc.watch.StopWatch;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration.ParameterStringFormat;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.exceptions.TargetAlgorithmAbortException;

/**
 * Executes a Target Algorithm Run via Command Line Execution
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class CommandLineAlgorithmRun implements Callable<AlgorithmRunResult>{


	private static final long serialVersionUID = -70897405824987641L;
	
	/**
	 * Regex that we hope to match
	 */
	
	
	//maybe merge these one day
	/**
	 * @deprecated this format is deprecated and these constants have been moved to {@link AlgorithmRunResultFactory#AUTOMATIC_CONFIGURATOR_RESULT_REGEX}, also this format is getting replaced.
 	 */
	public static final String AUTOMATIC_CONFIGURATOR_RESULT_REGEX = AlgorithmRunResultFactory.AUTOMATIC_CONFIGURATOR_RESULT_REGEX;

	/**
	 * @deprecated this format is deprecated and these constants have been moved to {@link AlgorithmRunResultFactory#AUTOMATIC_CONFIGURATOR_RESULT_REGEX}, also this format is getting replaced.
	 */
	public static final String OLD_AUTOMATIC_CONFIGURATOR_RESULT_REGEX = AlgorithmRunResultFactory.OLD_LEGACY_AUTOMATIC_CONFIGURATOR_RESULT_REGEX;


	/**
	 * Exit code due to signal is 128 + signal number (according to http://tldp.org/LDP/abs/html/exitcodes.html)
	 */
	private static final int SIGINT_EXIT_VALUE = 130;
	private static final int SIGKILL_EXIT_VALUE = 137;
	private static final int SIGTERM_EXIT_VALUE = 143;

	private static transient Logger log = LoggerFactory.getLogger(CommandLineAlgorithmRun.class);

	private final ConcurrentMap<AlgorithmExecutionConfiguration, CallFormatSelector> selectorMap;

	private final CallFormatSelector selector;



	/**
	 * Stores the set of AlgorithmExecutionConfigurations for w
	 *
	 */
	//private final Set<AlgorithmExecutionConfiguration> classicCallAborted;


	private Queue<String> outputQueue = new ArrayDeque<String>(MAX_LINES_TO_SAVE * 2);

	/**
	 * Stores the observer for this run
	 */
	private transient TargetAlgorithmEvaluatorRunObserver runObserver;

	/**
	 * Stores the kill handler for this run
	 */
	private transient KillHandler killHandler;
	
	public static final String PORT_ENVIRONMENT_VARIABLE = "AEATK_PORT";
	public static final String FREQUENCY_ENVIRONMENT_VARIABLE = "AEATK_CPU_TIME_FREQUENCY";
	public static final String CONCURRENT_TASK_ID = "AEATK_CONCURRENT_TASK_ID";

	
	/**
	 * This variable is public only for unit test purposes,
	 * this is not guaranteed to be the actual environment variable of child processes
	 */
	public static final String EXECUTION_UUID_ENVIRONMENT_VARIABLE_DEFAULT = "AEATK_EXECUTION_UUID"; 
	
	
	/**
	 * Stores a unique UUID for the run, used in environment variables.
	 */
	private final UUID uuid = UUID.randomUUID();
	
	static 
	{
		if(!System.getenv().containsKey(EXECUTION_UUID_ENVIRONMENT_VARIABLE_DEFAULT))
		{
			envVariableForChildren = EXECUTION_UUID_ENVIRONMENT_VARIABLE_DEFAULT;
		} else
		{
			int i=0;
			while( System.getenv().containsKey(EXECUTION_UUID_ENVIRONMENT_VARIABLE_DEFAULT + "_SUB_" + (i++)));
			envVariableForChildren = EXECUTION_UUID_ENVIRONMENT_VARIABLE_DEFAULT + "_SUB_" + (i++);
		}
	}
	
	private static final String envVariableForChildren;
	
	
	/**
	 * Marker for logging
	 */
	private static transient Marker fullProcessOutputMarker = MarkerFactory.getMarker(LoggingMarker.FULL_PROCESS_OUTPUT.name());
	
	public final static String COMMAND_SEPERATOR;

	static {
		log.trace("This version of SMAC hardcodes run length for calls to the target algorithm to {}.", Integer.MAX_VALUE);
		
		if(System.getProperty("os.name").toLowerCase().contains("win"))
		{
			COMMAND_SEPERATOR = "&";
		} else
		{
			COMMAND_SEPERATOR = ";";
		}
		
	}
	
	//private transient
	
	private final int observerFrequency;
	
	private AtomicBoolean processEnded = new AtomicBoolean(false);
	
	private transient final BlockingQueue<Integer> executionIDs;
	
	
	/**
	 * This field is transient because we can't save this object when we serialize.
	 * 
	 * If after restoring serialization you need something from this object, you should
	 * save it as a separate field. (this seems unlikely) 
	 * 
	 */
	private final transient CommandLineTargetAlgorithmEvaluatorOptions options;

	private final AlgorithmRunConfiguration runConfig;
	
	private static transient final AtomicBoolean jvmShutdownDetected = new AtomicBoolean(false);
	
	
	/**
	 * Watch that can be used to time algorithm runs 
	 */
	private	final StopWatch wallClockTimer = new StopWatch();


	protected void startWallclockTimer()
	{
		wallClockTimer.start();
	}
	
	private AtomicDouble wallClockTime = new AtomicDouble();
	
	protected void stopWallclockTimer()
	{
		wallClockTime.set(wallClockTimer.stop() / 1000.0);
	}
	
	protected long getCurrentWallClockTime()
	{
		return this.wallClockTimer.time();
	}
	
	
	private static final Set<Pair<CommandLineAlgorithmRun, Process>> outstandingRuns  = Collections.newSetFromMap(new ConcurrentHashMap<Pair<CommandLineAlgorithmRun, Process>,Boolean>());
	static
	{
		Thread shutdownThread = new Thread(new Runnable()
		{

			@Override
			public void run() {
				Thread.currentThread().setName("CLI Shutdown Thread");
				jvmShutdownDetected.set(true);
				if(outstandingRuns.size() > 0)
				{
					log.debug("Terminating approximately {} outstanding algorithm runs", outstandingRuns.size());
				}
				log.trace("Further runs will be instantly terminated");
				for(Pair<CommandLineAlgorithmRun, Process> p : outstandingRuns)
				{
					p.getFirst().killProcess(p.getSecond());
				}
			}
			
		});
		
		log.trace("Shutdown hook to terminate all outstanding runs enabled");
		Runtime.getRuntime().addShutdownHook(shutdownThread);
	}
	
	
	
	private volatile AlgorithmRunResult completedAlgorithmRun;
	/**
	 * Default Constructor
	 * @param runConfig			run configuration we are executing
	 * @param obs
	 * @param handler
	 * @param executionID 		a queue of execution IDS.
	 * @param selectorMap		concurrent map that tells us which selector we should use, and we will modify if needed.
	 */ 
	public CommandLineAlgorithmRun( AlgorithmRunConfiguration runConfig, TargetAlgorithmEvaluatorRunObserver obs, KillHandler handler, CommandLineTargetAlgorithmEvaluatorOptions options, BlockingQueue<Integer> executionID, ConcurrentMap<AlgorithmExecutionConfiguration, CallFormatSelector> selectorMap)
	{

		
		this.runConfig = runConfig;
		this.runObserver = obs;
		this.killHandler = handler;
		this.observerFrequency = options.observerFrequency;
		
		if(observerFrequency < 25)
		{
			throw new IllegalArgumentException("Observer Frequency can't be less than 25 milliseconds");
		}
		
		this.options = options;

		this.executionIDs = executionID;

		this.selectorMap = selectorMap;
		this.selector = selectorMap.get(runConfig.getAlgorithmExecutionConfiguration());

	}

	/**
	 * Copy constructor which switches the call format selector
	 * @param commandLineAlgorithmRun
	 * @param callFormatSelector
	 */
	public CommandLineAlgorithmRun(CommandLineAlgorithmRun commandLineAlgorithmRun, CallFormatSelector callFormatSelector) {
		this.runConfig = commandLineAlgorithmRun.runConfig;
		this.runObserver = commandLineAlgorithmRun.runObserver;
		this.killHandler = commandLineAlgorithmRun.killHandler;
		this.observerFrequency = commandLineAlgorithmRun.observerFrequency;
		this.options = commandLineAlgorithmRun.options;
		this.executionIDs = commandLineAlgorithmRun.executionIDs;
		this.selector = callFormatSelector;
		this.selectorMap = commandLineAlgorithmRun.selectorMap;
	}

	
	private static final int MAX_LINES_TO_SAVE = 1000;

	private volatile boolean wasKilled = false;
	
	@Override
	public synchronized AlgorithmRunResult call() 
	{

		AlgorithmRunResult result;
		try {

			result = processCallRequest();
		} catch(TargetAlgorithmAbortException e)
		{
			if(selector.shouldSwitch())
			{
				log.info("Algorithm call signaled ABORT (via exception) with ({}) call format trying alternative ({}) call format", selector, selector.onAbortTry());

				selectorMap.put(runConfig.getAlgorithmExecutionConfiguration(),selector.onAbortTry());
				CommandLineAlgorithmRun arc = new CommandLineAlgorithmRun(this, selector.onAbortTry());

				return arc.call();
			} else
			{
				if(completedAlgorithmRun.getRunExecutionStatus().equals(RunExecutionStatus.ABORT))
				{
					// REPORT THE ABORT
					runObserver.currentStatus(Collections.singletonList(completedAlgorithmRun));
				}
				throw e;
			}
		}


		if(result.getRunExecutionStatus().equals(RunExecutionStatus.ABORT))
		{
			if(selector.shouldSwitch())
			{
				log.info("Algorithm call signalled ABORT (via result) ({}) call format trying alternative ({}) call format", selector, selector.onAbortTry());

				selectorMap.put(runConfig.getAlgorithmExecutionConfiguration(),selector.onAbortTry());
				CommandLineAlgorithmRun arc = new CommandLineAlgorithmRun(this, selector.onAbortTry());


				return arc.call();
			} else
			{
				if(completedAlgorithmRun.getRunExecutionStatus().equals(RunExecutionStatus.ABORT))
				{
					// REPORT THE ABORT
					runObserver.currentStatus(Collections.singletonList(completedAlgorithmRun));
				}

				return result;
			}
		} else
		{
			if(selector.shouldSwitch())
			{
				log.debug("Algorithm call signalled success with ({}) call format trying alternative ({}) call format", selector, selector.onSuccessUse());

				selectorMap.put(runConfig.getAlgorithmExecutionConfiguration(),selector.onSuccessUse());
			}

			return result;
		}
	}

	private AlgorithmRunResult processCallRequest() {
		Thread.currentThread().setName("CLI TAE (Master Thread - TBD)");
		if(killHandler.isKilled())
		{

			log.trace("Run has already been toggled as killed {}", runConfig);

			RunStatus rr = RunStatus.KILLED;

			AlgorithmRunResult run = new ExistingAlgorithmRunResult(runConfig, rr, 0, 0, 0, runConfig.getProblemInstanceSeedPair().getSeed(), "",0);
			try {
				runObserver.currentStatus(Collections.singletonList((AlgorithmRunResult) run));
			} catch(RuntimeException t)
			{
				log.error("Error occurred while notify observer ", t);
				throw t;
			}

			return run;

		}


		//Notify observer first to trigger kill handler
		runObserver.currentStatus(Collections.singletonList((AlgorithmRunResult) new RunningAlgorithmRunResult(runConfig,  0,  0,0, runConfig.getProblemInstanceSeedPair().getSeed(), 0, killHandler)));


		if(jvmShutdownDetected.get())
		{

			String rawResultLine = "JVM Shutdown Detected";

			AlgorithmRunResult run = new ExistingAlgorithmRunResult(runConfig, RunStatus.KILLED, 0, 0, 0, runConfig.getProblemInstanceSeedPair().getSeed(),"JVM Shutdown Detected, algorithm not executed",0);
			runObserver.currentStatus(Collections.singletonList(run));
			return run;
		}

		if(killHandler.isKilled())
		{

			log.trace("Run was killed", runConfig);
			String rawResultLine = "Kill detected before target algorithm invoked";

			AlgorithmRunResult run = new ExistingAlgorithmRunResult(runConfig,RunStatus.KILLED, 0, 0, 0, runConfig.getProblemInstanceSeedPair().getSeed(), "Kill detected before target algorithm invoked", 0);
			runObserver.currentStatus(Collections.singletonList(run));
			return run;
		}

		final Process proc;

		File execDir = new File(runConfig.getAlgorithmExecutionConfiguration().getAlgorithmExecutionDirectory());
		if(!execDir.exists())
		{
			throw new TargetAlgorithmAbortException("Algorithm Execution Directory: " + runConfig.getAlgorithmExecutionConfiguration().getAlgorithmExecutionDirectory() + " does not exist");
		}

		if(!execDir.isDirectory())
		{
			throw new TargetAlgorithmAbortException("Algorithm Execution Directory: " + runConfig.getAlgorithmExecutionConfiguration().getAlgorithmExecutionDirectory() + " is not a directory");
		}

		try
		{
			Integer token;
			try
			{
				token = executionIDs.take();
			} catch(InterruptedException e)
			{
				Thread.currentThread().interrupt();
				AlgorithmRunResult run = ExistingAlgorithmRunResult.getAbortResult(runConfig, "Target CLI Thread was Interrupted");
				return run;
			}

			final Integer myToken = token;
			Thread.currentThread().setName("CLI TAE (Master Thread - #" + myToken +")" );

			try
			{

				//Check kill handler again
				if(killHandler.isKilled())
				{
					log.trace("Run was killed", runConfig);

					AlgorithmRunResult run = new ExistingAlgorithmRunResult(runConfig, RunStatus.KILLED, 0, 0, 0, runConfig.getProblemInstanceSeedPair().getSeed(),"Kill detected before target algorithm invoked",0);
					runObserver.currentStatus(Collections.singletonList(run));;
					return run;
				}

				int port = 0;
				final DatagramSocket serverSocket;
				if(options.listenForUpdates)
				{
					serverSocket = new DatagramSocket();
					port = serverSocket.getLocalPort();
				} else
				{
					serverSocket = null;
				}

				final AtomicDouble currentRuntime = new AtomicDouble(0);

				Runnable socketThread = new Runnable()
				{
					@Override
					public void run()
					{

						Thread.currentThread().setName("CLI TAE (Socket Thread - #"+myToken+")" );

						byte[] receiveData = new byte[1024];

						while(true)
						{
							try
							{
								DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);


								serverSocket.receive(receivePacket);

								InetAddress IPAddress = receivePacket.getAddress();

								if (!InetAddress.getByName("localhost").equals(IPAddress))
								{
									log.warn("Received Request from Non-localhost, ignoring request from: {}", IPAddress.getHostAddress());
									continue;
								}

				               Double runtime = Double.valueOf(new String(receivePacket.getData()));

				               currentRuntime.set(runtime);

							} catch(RuntimeException e)
							{
								log.trace("Got some runtime exception while processing data packet", e);
							} catch(SocketException e)
							{
								//Don't log this since this socket exception is what we
								//we expect since most of the time we will be blocked on the socket
								//when we are shutdown and interrupted.
								return;
							} catch (IOException e) {
								log.warn("Unknown IOException occurred ", e);
							}

						}
					}


				};

				this.startWallclockTimer();
				proc = runProcess(port, token);

				try
				{
				outstandingRuns.add(new Pair<CommandLineAlgorithmRun, Process>(this, proc));

				final Process innerProcess = proc;


				final Semaphore stdErrorDone = new Semaphore(0);


				Runnable standardErrorReader = new Runnable()
				{

					@Override
					public void run() {

						Thread.currentThread().setName("CLI TAE (STDERR Thread - #" + myToken + ")");
						try {
							try {
								try (BufferedReader procIn = new BufferedReader(new InputStreamReader(innerProcess.getErrorStream())))
								{
									do{

										String line;
										boolean read = false;
										while(procIn.ready())
										{
											read = true;
											line = procIn.readLine();

											if(line == null)
											{

												return;
											}
											log.warn("[PROCESS-ERR]  {}", line);

										}


										if(!read)
										{
											Thread.sleep(50);
										}

									} while(!processEnded.get());


									StringBuilder sb = new StringBuilder();

									//In case something else has come in
									if(procIn.ready())
									{
										//Probably not the most efficient way to read
										char[] input = new char[10000];
										procIn.read(input);
										sb.append(String.valueOf(input));

									}

									if(sb.toString().trim().length() > 0)
									{
										log.warn("[PROCESS-ERR] {}", sb.toString().trim());
									}
								}
							} finally
							{

								stdErrorDone.release();
								log.trace("Standard Error Done");
							}
						} catch(InterruptedException e)
						{
							Thread.currentThread().interrupt();
							return;
						} catch(IOException e)
						{
							log.warn("Unexpected IOException occurred {}",e);
						}


					}

				};


				Runnable observerThread = new Runnable()
				{

					@Override
					public void run() {
						Thread.currentThread().setName("CLI TAE (Observer Thread - #" + myToken+ ")");

						while(true)
						{

							double currentTime = getCurrentWallClockTime() / 1000.0;

							runObserver.currentStatus(Collections.singletonList((AlgorithmRunResult) new RunningAlgorithmRunResult(runConfig,  Math.max(0,currentRuntime.get()),  0,0, runConfig.getProblemInstanceSeedPair().getSeed(), currentTime, killHandler)));
							try {



								//Sleep here so that maybe anything that wanted us dead will have gotten to the killHandler
								Thread.sleep(25);
								if(killHandler.isKilled())
								{
									wasKilled = true;
									log.trace("Trying to kill run: {} latest time: {} " , runConfig, currentRuntime.get());


									killProcess(proc);
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

				ExecutorService threadPoolExecutor = Executors.newCachedThreadPool(new SequentiallyNamedThreadFactory("Command Line Target Algorithm Evaluator Thread "));
				try
				{
					if(options.listenForUpdates)
					{
						threadPoolExecutor.execute(socketThread);
					}
					threadPoolExecutor.execute(observerThread);
					threadPoolExecutor.execute(standardErrorReader);
					BufferedReader read = new BufferedReader(new InputStreamReader(proc.getInputStream()));

					if(jvmShutdownDetected.get())
					{ //Possible that this run started after the shutdown call was flagged, but before we put it in the map.

						killProcess(proc);
					}
					try
					{
						processRunLoop(read,proc);
					} finally
					{
						killProcess(proc);
					}

					if(completedAlgorithmRun == null)
					{
						if(wasKilled)
						{
							double currentTime = Math.max(0,currentRuntime.get());
							completedAlgorithmRun = new ExistingAlgorithmRunResult(runConfig, RunStatus.KILLED, currentTime, 0,0, runConfig.getProblemInstanceSeedPair().getSeed(), "Killed Manually", this.getCurrentWallClockTime() / 1000.0 );

						} else if(jvmShutdownDetected.get())
						{
							double currentTime = Math.max(0,currentRuntime.get());
							completedAlgorithmRun = new ExistingAlgorithmRunResult(runConfig,  RunStatus.KILLED, currentTime, 0,0, runConfig.getProblemInstanceSeedPair().getSeed(), "JVM Shutdown Detected", this.getCurrentWallClockTime()  / 1000.0);
						} else
						{
							double currentTime = Math.max(0,currentRuntime.get());
							completedAlgorithmRun = new ExistingAlgorithmRunResult(runConfig, RunStatus.CRASHED, currentTime, 0,0, runConfig.getProblemInstanceSeedPair().getSeed(), "ERROR: Wrapper did not output anything that matched the expected output (\"Result of algorithm run:...\"). Please try executing the wrapper directly", this.getCurrentWallClockTime() / 1000.0);
						}
					}


					switch(completedAlgorithmRun.getRunStatus())
					{
						case ABORT:
						case CRASHED:

								log.error("The following algorithm call failed: cd \"{}\" " + COMMAND_SEPERATOR + "  {} ",new File(runConfig.getAlgorithmExecutionConfiguration().getAlgorithmExecutionDirectory()).getAbsolutePath(), getTargetAlgorithmExecutionCommandAsString( runConfig));

								if(outputQueue.size() > 0)
								{
									log.error("The last {} lines of output we saw were:", outputQueue.size());

									for(String s : outputQueue)
									{
										log.error("> "+s);
									}
								} else
								{
									log.debug("No output on standard out detected");
								}



						default:
							//Doesn't matter

					}

					outputQueue.clear();

					read.close();

					stdErrorDone.acquireUninterruptibly();
				} finally
				{
					//Close the listening socket
					if(serverSocket != null)
					{
						serverSocket.close();
					}
					threadPoolExecutor.shutdownNow();
					try {
						threadPoolExecutor.awaitTermination(24, TimeUnit.HOURS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}

				} finally
				{
					if(proc != null)
					{
						proc.destroy();
					}

				}

				if(!completedAlgorithmRun.getRunExecutionStatus().equals(RunExecutionStatus.ABORT))
				{
					//DO NOT REPORT AN ABORT HERE, IN CASE WE ARE GOING TO RETRY THE CALL
					runObserver.currentStatus(Collections.singletonList(completedAlgorithmRun));
				}

				log.debug("Run {} is completed", completedAlgorithmRun);

			} finally
			{
				if(!executionIDs.offer(token))
				{
					log.error("Developer Error: Couldn't offer run token back to pool, which violates an invariant. We will essentially block until it is accepted.");
					try {
						executionIDs.put(token);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}


			}

			return completedAlgorithmRun;

		} catch (IOException e1)
		{

			//String execCmd = getTargetAlgorithmExecutionCommandAsString(execConfig,runConfig);
			log.error( "The following algorithm call failed: cd \"{}\" " + COMMAND_SEPERATOR + "  {} ",new File(runConfig.getAlgorithmExecutionConfiguration().getAlgorithmExecutionDirectory()).getAbsolutePath(), getTargetAlgorithmExecutionCommandAsString( runConfig));

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
	public void processRunLoop(BufferedReader procIn, Process p)
	{
		
		int i=0; 
			try {
				boolean matchFound = false;
				try
				{
outerloop:		
					do{

						String line;
						boolean read = false;
						//TODO This ready call doesn't guarantee we can read a line
						
						while(procIn.ready())
						{
							read = true;
							line = procIn.readLine();
							
							if(line == null)
							{
								log.trace("Process has ended");
								processEnded.set(true);
								break outerloop;
							}
							outputQueue.add(line);
							if (outputQueue.size() > MAX_LINES_TO_SAVE)
							{
								outputQueue.poll();
							}
							

							if(wasKilled)
							{
								continue;
							}
							boolean matched = processLine(line);

							if(matched && matchFound)
							{
								log.error("Second output of matching line detected, there is a problem with your wrapper. You can try turning with log all process output enabled to debug: {} ", line);
								completedAlgorithmRun = ExistingAlgorithmRunResult.getAbortResult(runConfig, "duplicate lines matched");
								continue;
							}
							matchFound = matchFound | matched; 
						}
						
						if(completedAlgorithmRun != null && wasKilled)
						{
							if(completedAlgorithmRun.getWallclockExecutionTime() > 1)
							{
								//This could easily occur with short runs, so lets only do this if the wallclock time is greater than 1.
								log.warn("Run was killed but we somehow completed this might be a race condition but our result is: {}. This is a warning because some wrappers don't properly handle termination and erroneously report successful results, it is up to you to judge whether this is correct or not." ,completedAlgorithmRun.getResultLine());
							}
						}
						
						if(!procIn.ready() && exited(p))
						{
							//I assume that if the stream isn't ready and the process has exited that 
							//we have processed everything
							processEnded.set(true);
							break;
						}
						
						if(!read)
						{
							if(++i % 12000 == 0)
							{
								log.trace("Slept for 5 minutes waiting for pid {}  &&  (matching line found?: {} ) " ,getPID(p), matchFound);
							}
							Thread.sleep(25);
						}
						
					} while(!processEnded.get());
				} finally
				{
					procIn.close();
				} 
			} catch (IOException e) {
				
				if(!processEnded.get())
				{
					log.trace("IO Exception occurred while processing runs");
				}
				
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			
			}
		
		
	}
	
	

	/**
	 * Starts the target algorithm
	 * @param token 
	 * @return Process reference to the executiong process
	 * @throws IOException
	 */
	private  Process runProcess(int port, Integer token) throws IOException
	{
		String[] execCmdArray = getTargetAlgorithmExecutionCommand(runConfig);
		
		
		if(options.logAllCallStrings())
		{
			StringBuilder sb = new StringBuilder();

			for(String s : execCmdArray)
			{
				sb.append(s).append(" ");
			}

			log.info( "Call (with token {}) : cd \"{}\" " + COMMAND_SEPERATOR + "  {} ", token, new File(runConfig.getAlgorithmExecutionConfiguration().getAlgorithmExecutionDirectory()).getAbsolutePath(), sb.toString());


		}
		
		
		ArrayList<String> envpList = new ArrayList<String>(System.getenv().size());
		for(Entry<String, String> ent : System.getenv().entrySet())
		{
			envpList.add(ent.getKey() + "=" + ent.getValue());
		}
		
		if(options.listenForUpdates)
		{
			envpList.add(PORT_ENVIRONMENT_VARIABLE  + "=" + port);
			envpList.add(FREQUENCY_ENVIRONMENT_VARIABLE + "=" + (this.observerFrequency / 2000.0));
			
		}
		
		envpList.add(CONCURRENT_TASK_ID + "=" + token);
		envpList.add(envVariableForChildren + "=" + uuid.toString());  
		String[] envp = envpList.toArray(new String[0]);

		Process proc = Runtime.getRuntime().exec(execCmdArray,envp, new File(runConfig.getAlgorithmExecutionConfiguration().getAlgorithmExecutionDirectory()));

		log.debug("Process for {} started with pid: {} (Environment Variable: {})", this.runConfig, getPID(proc), uuid);

		return proc;
	}
	
	
	/**
	 * Gets the execution command string
	 * @return string containing command
	 */

	private String[] getTargetAlgorithmExecutionCommand( AlgorithmRunConfiguration runConfig)
	{
		return selector.getCallString(runConfig, options.paramArgumentsContainQuotes);
	}
	
	/**
	 * Gets the execution command string
	 *
	 * @return string containing command
	 */
	public static String getTargetAlgorithmExecutionCommandAsString( AlgorithmRunConfiguration runConfig)
	{

		String[] list = AclibCallFormat.getInstance().getCallString(runConfig, true);
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

		return sb.toString();
	}

	/**
	 *	Process a single line of the output looking for a matching line (e.g. Result of algorithm run: ...)
	 *	@param line of program output
	 */
	public boolean processLine(String line)
	{

		if(options.logAllProcessOutput)
		{
			log.info("[PROCESS] {}" ,line);
		}

		AlgorithmRunResult resultRun = selector.getAlgorithmRunResult(line, runConfig, this.getCurrentWallClockTime() / 1000.0);

		if(resultRun == null)
		{
			return false;
		}

		// We got a result so the line must have matched something.
		if(options.logAllCallResults() && !options.logAllProcessOutput)
		{
			log.info("[PROCESS] {}", line);
		}

		if(this.completedAlgorithmRun == null)
		{
			//Only save the last output.
			this.completedAlgorithmRun = resultRun;
		}

		return true;
	}

	
	private static AtomicBoolean getPidFailureReported = new AtomicBoolean(false);
	public static int getPID(Process p)
	{
		int pid = 0;

		try {
			Field f = p.getClass().getDeclaredField("pid");
			
			f.setAccessible(true);
			pid = Integer.valueOf(f.get(p).toString());
			f.setAccessible(false);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			if(getPidFailureReported.compareAndSet(false, true))
			{
				log.error("Unable to determine pid of process, this means we may not be able to terminate the process properly, if it misbehaves. Please notify the developer and provide your log as well as your OS and Java versions.", e);
			}
		} catch (NoSuchFieldException e) {
			return -1;
		}
	
		if(pid > 0)
		{
			return pid;
		} else
		{
			return -1;
		}
	}
	
	public static boolean exited(Process p)
	{
		try 
		{
			p.exitValue();
			return true;
		} catch(IllegalThreadStateException e)
		{
			return false;
		}
	}
	
	private String replacePid(String input, int pid)
	{
		return input.replaceAll("%pid", String.valueOf(pid));
	}
	
	AtomicBoolean killPreviouslyCalled = new AtomicBoolean(false);
	private void killProcess(Process p)
	{
	
		if(killPreviouslyCalled.getAndSet(true))
		{
			return;
		} else
		{
			outstandingRuns.remove(new Pair<>(this, p));
		}
		
		
		try 
		{
			int pid = getPID(p);
			if(options.pgEnvKillCommand != null && options.pgEnvKillCommand.trim().length() > 1)
			{
				try {
					
					String killEnvCmd = options.pgEnvKillCommand + " " + envVariableForChildren + " " + uuid.toString() + " " + pid; 
					ProcessBuilder pb = new ProcessBuilder();
					
					pb.redirectErrorStream(true);
					pb.command(SplitQuotedString.splitQuotedString(killEnvCmd));
					
					Process p2 = pb.start();
					
					try 
					{
						try(BufferedReader read = new BufferedReader(new InputStreamReader(p2.getInputStream())))
						{
							String line = null;
							
							while((line = read.readLine()) != null)
							{
								log.trace("Kill environment {} output> {}", uuid.toString(), line);
							}
						}
					} finally
					{
						p2.destroy();
						
						try 
						{
							if(p2.waitFor() > 0)
							{
								log.warn("Kill script execution returned non-zero exit status: {} ", p2.exitValue());
							}
						} catch(InterruptedException e)
						{
							Thread.currentThread().interrupt();
							//Continue;
						}
							
					}
				} catch(IOException e)
				{
					
					log.error("Error while executing {} execute Kill Environment Command",e);
					
				}
			} else
			{
				try 
				{
					if(pid > 0)
					{
						String command = replacePid(options.pgNiceKillCommand,pid);
						log.trace("Trying to send SIGTERM to process group id: {} with command \"{}\"", pid,command);
						try {
							
							
							int retValPGroup = executeKillCommand(command);
							
							if(retValPGroup > 0)
							{
								log.trace("SIGTERM to process group failed with error code {}", retValPGroup);
								
								
								int retVal = executeKillCommand(replacePid(options.procNiceKillCommand,pid));
								
								if(retVal > 0)
								{
									Object[] args = {  pid,retVal};
									log.trace("SIGTERM to process id: {} attempted failed with return code {}",args);
								} else
								{
									log.trace("SIGTERM delivered successfully to process id: {}", pid, pid);
								}
							} else
							{
								log.trace("SIGTERM delivered successfully to process group id: {} ", pid);
							}
						} catch (IOException e) {
							log.error("Couldn't SIGTERM process or process group ", e);
						}
						
					
						
						
						int totalSleepTime = 0;
						int currSleepTime = 25;
						while(true)
						{
							
							if(exited(p))
							{
								return;
							}
							
							Thread.sleep(currSleepTime);
							totalSleepTime += currSleepTime;
							currSleepTime *=1.5;
							if(totalSleepTime > 3000)
							{
								break;
							}
							
						}
												
						log.trace("Trying to send SIGKILL to process group id: {}", pid);
						try {
							
							int retVal = executeKillCommand(replacePid(options.pgForceKillCommand,pid));
							
							if(retVal > 0)
							{
								log.trace("SIGKILL to pid: {} attempted failed with return code {}",pid, retVal);
								
								int retVal3 = executeKillCommand(replacePid(options.procForceKillCommand,pid));
								
								if(retVal3 > 0)
								{
									Object[] args = {  pid,retVal};
									log.trace("SIGKILL to process id: {} attempted failed with return code {}",args);
								} else
								{
									log.trace("SIGKILL delivered successfully to process id: {}", pid, pid);
								}
							} else
							{
								log.trace("SIGKILL delivered successfully to pid: {} ", pid);
							}
						} catch (IOException e) {
							log.error("Couldn't SIGKILL process or process group ", e);
							
						}
					
						
						
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
				
			}
			
			
			p.destroy();
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.error("This shouldn't be possible", e);
				
			}
			
			if(p.exitValue() > 0)
			{
				if(p.exitValue() != SIGTERM_EXIT_VALUE && p.exitValue() != SIGKILL_EXIT_VALUE &&  p.exitValue() != SIGINT_EXIT_VALUE) //
				{ 	/**
				 	* The above exit values are for linux and likely mean that the process was killed either by us or something else, we consider
				 	* this normal and do not log it.
				 	*
				 	*
				 	* On Windows, BSD, MAC no one has reported or complained about this but presumably we will need better checks, if they do.
					*/

					log.debug("Process with pid {} and {} signaled non-zero exit status: {}", pid, uuid.toString(), p.exitValue() );
				}
			}
		
			
		} finally
		{
			this.processEnded.set(true);
			this.stopWallclockTimer();
			
		}
		
	}
	
	private int executeKillCommand(String command) throws IOException, InterruptedException
	{
		log.trace("Executing termination command: {}");
		ProcessBuilder pb = new ProcessBuilder();
		pb.redirectErrorStream(true);
		pb.command(SplitQuotedString.splitQuotedString(command));
		Process p2 = pb.start();
		
		
		try (BufferedReader read = new BufferedReader(new InputStreamReader(p2.getInputStream())))
		{
			String line = null;
			
			while((line = read.readLine()) != null)
			{
				log.trace("Kill For environment {}: command \"{}\" output> {}", uuid.toString(), command, line);
			}
		
		}
		
		try 
		{
			return p2.waitFor();
		} finally
		{
			p2.destroy();
		}
		
	}
}
