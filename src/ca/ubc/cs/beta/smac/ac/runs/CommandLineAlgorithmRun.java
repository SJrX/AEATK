package ca.ubc.cs.beta.smac.ac.runs;

import java.io.File;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ca.ubc.cs.beta.ac.config.RunConfig;
import ca.ubc.cs.beta.ac.RunResult;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;

public class CommandLineAlgorithmRun extends AbstractAlgorithmRun {

	
	private static final long serialVersionUID = -70897405824987641L;
	
	public static final String AUTOMATIC_CONFIGURATOR_RESULT_REGEX = "(Final)?\\s*[Rr]esult\\s+(?:(for)|(of))\\s+(?:(HAL)|(ParamILS)|(SMAC)|(this wrapper)):";
	
	private static final Pattern pattern = Pattern.compile(AUTOMATIC_CONFIGURATOR_RESULT_REGEX);
	
	private static transient Logger log = LoggerFactory.getLogger(CommandLineAlgorithmRun.class);
	private static transient Marker execCommandMarker = MarkerFactory.getMarker("Execution Command"); 
	private static transient Marker fullProcessOutputMarker = MarkerFactory.getMarker("Full Process Output");
	
	static {
		log.warn("This version of SMAC hardcodes run length for calls to the target algorithm to {}.", Integer.MAX_VALUE);
	}
	
	
	public CommandLineAlgorithmRun(AlgorithmExecutionConfig execConfig, RunConfig instanceConfig) 
	{
		super(execConfig, instanceConfig);
		
		if(instanceConfig.getCutoffTime() <= 0)
		{
			log.info("Cap time is negative for {} setting run as timedout", instanceConfig);
			
			acResult =  RunResult.TIMEOUT;
			rawResultLine = "[DIDN'T BOTHER TO RUN ALGORITHM AS THE CAPTIME IS NEGATIVE]";
			int solved = acResult.getResultCode();
			String runtime = "0.0";
			String runLength = "0";
			String bestSolution = "0";
			String seed = String.valueOf(instanceConfig.getAlgorithmInstanceSeedPair().getSeed());
			resultLine = acResult.name() + ", " + runtime + ", " + runLength + ", " + bestSolution + ", " + seed;
			
			this.runLength = Double.valueOf(runLength);
			this.runtime = Double.valueOf(runtime);
			this.quality = Double.valueOf(bestSolution);
			this.resultSeed = Long.valueOf(seed);
			runResultWellFormed = true;
			
			synchronized(this) 
			{
				runCompleted = true;
			}
		}
	}


	@Override
	public synchronized void run() {
		
		if(runCompleted)
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
			String execCmd = getTargetAlgorithmExecutionCommand();
			log.error("Failed to execute command: {}", execCmd);
			throw new IllegalStateException(e1);
		}
		
		runCompleted = true;
		
		
	}
	

	/**
	 * Executes the command
	 * @return Process reference
	 * @throws IOException
	 */
	private  Process runProcess() throws IOException
	{
		String execCmd = getTargetAlgorithmExecutionCommand();
		log.info(execCommandMarker, execCmd);
		Process proc = Runtime.getRuntime().exec(execCmd,null, new File(execConfig.getAlgorithmExecutionDirectory()));
	
		
		return proc;
	}
	
	
	
	/**
	 *  Gets the execution command string
	 * @return string containing command
	 */
	private String getTargetAlgorithmExecutionCommand()
	{
		
		
		StringBuilder execString = new StringBuilder();
		execString.append(execConfig.getAlgorithmExecutable()).append(" ").append(instanceConfig.getAlgorithmInstanceSeedPair().getInstance().getInstanceName()).append(" ").append(instanceConfig.getAlgorithmInstanceSeedPair().getInstance().getInstanceSpecificInformation()).append(" ").append(instanceConfig.getCutoffTime()).append(" ").append(Integer.MAX_VALUE).append(" ").append(instanceConfig.getAlgorithmInstanceSeedPair().getSeed()).append(" ").append(instanceConfig.getParamConfiguration().getFormattedParamString());
		return execString.toString();
	}

	/**
	 * Proceses all the output of the target algorithm
	 * @param procIn - Scanner of processes output stream
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
	*	Process a single line of the output looking for a matching line (i.e. Result for ParamILS: ...)
	*	@param String line - line of program output
	*/
	public void processLine(String line)
	{
		Matcher matcher = pattern.matcher(line);
		rawResultLine = "[No Matching Output Found]";
		log.debug(fullProcessOutputMarker,line);
		
		//long time = System.currentTimeMillis();	
		if (matcher.find())
		{
			
			String acExecResultString = line.substring(matcher.end()).trim();
			
			String[] results = acExecResultString.split(",");
			for(int i=0; i < results.length; i++)
			{
				results[i] = results[i].trim();
			}
			

			
			acResult =  RunResult.getAutomaticConfiguratorResultForKey(results[0]);
			
			int solved = acResult.getResultCode();
			String runtime = results[1];
			String runLength = results[2];
			String bestSolution = results[3];
			String seed = results[4];
			
			resultLine = acResult.name() + ", " + runtime + ", " + runLength + ", " + bestSolution + ", " + seed;
			
			
			try
			{
				this.runLength = Double.valueOf(runLength);
				this.runtime = Double.valueOf(runtime);
				this.quality = Double.valueOf(bestSolution);
				this.resultSeed = Long.valueOf(seed);
				runResultWellFormed = true;
			} catch(NumberFormatException e)
			{
				
				
				//There was a problem with the output, we just set this flag
				this.runtime = 0;
				this.runLength = 0;
				this.quality = 0;
				this.resultSeed = -1;
				this.acResult = RunResult.CRASHED;
				
				runResultWellFormed = true;
				
			}	
			
			
		}
	}
	
	
	
	
	
	
}
