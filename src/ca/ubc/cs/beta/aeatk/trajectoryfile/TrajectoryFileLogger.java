package ca.ubc.cs.beta.aeatk.trajectoryfile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aeatk.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aeatk.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.aeatk.eventsystem.EventHandler;
import ca.ubc.cs.beta.aeatk.eventsystem.events.AutomaticConfiguratorEvent;
import ca.ubc.cs.beta.aeatk.eventsystem.events.ac.AutomaticConfigurationEnd;
import ca.ubc.cs.beta.aeatk.eventsystem.events.ac.IncumbentPerformanceChangeEvent;
import ca.ubc.cs.beta.aeatk.misc.cputime.CPUTime;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistory;
import ca.ubc.cs.beta.aeatk.termination.TerminationCondition;

public class TrajectoryFileLogger implements EventHandler<AutomaticConfiguratorEvent>{

	
	private double lastEmpericalPerformance = 0;
	private ParamConfiguration lastIncumbent;
	
	private final RunHistory runHistory;
	private final TerminationCondition terminationCondition;
	
	private final FileWriter trajectoryFileWriter;
	private final FileWriter trajectoryFileWriterCSV;
	
	private final String fileNamePrefix;
	
	private static final Logger log = LoggerFactory.getLogger(TrajectoryFileLogger.class);
	
	private final CPUTime cpuTime;
	
	boolean closed = false;
	
	public TrajectoryFileLogger(RunHistory runHistory, TerminationCondition terminationCondition, String fileNamePrefix, ParamConfiguration initialIncumbent, CPUTime cpuTime)
	{
		this.fileNamePrefix = fileNamePrefix;
		
		this.runHistory = runHistory;
		this.terminationCondition = terminationCondition;
		this.cpuTime = cpuTime;
		try {
			trajectoryFileWriter = new FileWriter(fileNamePrefix + ".txt");
			
			File f = new File(fileNamePrefix);
			
			trajectoryFileWriterCSV = new FileWriter(f.getParentFile().getAbsolutePath() + File.separator + "detailed-"+ f.getName() + ".csv");
			
			trajectoryFileWriter.append("\"CPU Time Used\",\"Estimated Training Performance\",\"Wallclock Time\",\"Incumbent ID\",\"Automatic Configurator (CPU) Time\",\"Configuration...\"\n");
			trajectoryFileWriterCSV.append("\"CPU Time Used\",\"Estimated Training Performance\",\"Wallclock Time\",\"Incumbent ID\",\"Automatic Configurator (CPU) Time\",\"Full Configuration\"\n");
			writeIncumbent(0,Double.MAX_VALUE,0,initialIncumbent,0);
		} catch (IOException e) {
			throw new IllegalStateException("Error occured creating files",e);
		}		
	}
	
	IncumbentPerformanceChangeEvent lastIevent;
	@Override
	public synchronized void handleEvent(AutomaticConfiguratorEvent event) {
		
		if(event instanceof IncumbentPerformanceChangeEvent)
		{
			if(closed)
			{
				log.error("Got Another Event After shutdown:{} ", event.getClass().getCanonicalName() );
			}
			IncumbentPerformanceChangeEvent ievent = (IncumbentPerformanceChangeEvent) event;
			this.lastIevent = ievent;
			writeIncumbent(ievent.getTunerTime(), ievent.getEmpiricalPerformance(), ievent.getWallTime(), ievent.getIncumbent(), ievent.getAutomaticConfiguratorCPUTime());
		} else if(event instanceof AutomaticConfigurationEnd)
		{
			
			log.debug("Writing trajectory file to {}",  (new File(fileNamePrefix)).getAbsolutePath());
			
			if(lastIevent != null)
			{ //Can't write this guy because the other threads have probably terminated
				writeIncumbent( terminationCondition.getTunerTime() , lastIevent.getEmpiricalPerformance(), terminationCondition.getWallTime(), lastIevent.getIncumbent(), cpuTime.getCPUTime());
			}
			try {
				trajectoryFileWriter.close();
				trajectoryFileWriterCSV.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			closed = true;
		} else
		{
			log.error("Got an event I wasn't expecting: {}", event.getClass().getCanonicalName());
		}
		
	}
	/*
	@Override
	public void handleEvent(IncumbentPerformanceChangeEvent event) {
		
	}*/
	
	private final List<TrajectoryFileEntry> tfes = new ArrayList<TrajectoryFileEntry>();

	/**
	 * Writes the incumbent to the trajectory files
	 * 
	 * @param tunerTime 			tuner time of the incumbent
	 * @param empiricalPerformance 	empirical performance of the incumbent
	 * @param wallclockTime 		wallclock time that
	 * @param incumbent				incumbent 
	 * @param acTime				automatic configurator time (tunerTime - Sum of runs)
	 */
	private synchronized void writeIncumbent(double tunerTime, double empiricalPerformance, double wallclockTime, ParamConfiguration incumbent, double acTime)
	{
	
		
		this.tfes.add(new TrajectoryFileEntry(incumbent, tunerTime, wallclockTime,   empiricalPerformance, acTime));
		
		boolean outOfTime = terminationCondition.haveToStop();
		if(incumbent.equals(lastIncumbent) && lastEmpericalPerformance == empiricalPerformance && !outOfTime)
		{
			log.trace("No change in performance");
			return;
		} else
		{
			log.trace("Incumbent Performance changed");
			lastEmpericalPerformance = empiricalPerformance;
			lastIncumbent = incumbent;
		}
		
		int thetaIdxInc = runHistory.getThetaIdx(incumbent);
		
		if(thetaIdxInc == -1)
		{
			thetaIdxInc = 1;
		}
		//-1 should be the variance but is allegedly the sqrt in compareChallengersagainstIncumbents.m and then is just set to -1.
		double wallClockTime = wallclockTime;
		
		String paramString = incumbent.getFormattedParamString(StringFormat.STATEFILE_SYNTAX);
		
		String escapedParamString = paramString.replaceAll(",","\\,");
		
		String outLine = tunerTime + ", " + empiricalPerformance + ", " + wallClockTime + ", " + thetaIdxInc + ", " + acTime + ", " + paramString +"\n";
		String escapedOutLine = "\""+tunerTime + "\",\" " + empiricalPerformance + "\",\"" + wallClockTime + "\",\"" + thetaIdxInc + "\",\"" + acTime + "\", \"" + escapedParamString +"\"\n";
		log.trace("Logging incumbent: (Runs {}): {}", ((this.lastIevent != null) ? this.lastIevent.getIncumbentRunCount() : "?"), outLine.trim());
		try 
		{
			trajectoryFileWriter.write(outLine);
			trajectoryFileWriter.flush();
			trajectoryFileWriterCSV.write(escapedOutLine);
			trajectoryFileWriterCSV.flush();
		} catch(IOException e)
		{
			throw new IllegalStateException("Could not update trajectory file", e);
		}

	}

	public synchronized final List<TrajectoryFileEntry> getTrajectoryFileEntries()
	{
		return Collections.unmodifiableList(tfes);
	}

	

}
