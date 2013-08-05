package ca.ubc.cs.beta.aclib.example.statemerge;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun;
import ca.ubc.cs.beta.aclib.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.MapList;
import ca.ubc.cs.beta.aclib.misc.jcommander.JCommanderHelper;
import ca.ubc.cs.beta.aclib.options.scenario.ScenarioOptions;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aclib.runhistory.RunData;
import ca.ubc.cs.beta.aclib.runhistory.ThreadSafeRunHistory;
import ca.ubc.cs.beta.aclib.runhistory.ThreadSafeRunHistoryWrapper;
import ca.ubc.cs.beta.aclib.state.StateFactoryOptions;
import ca.ubc.cs.beta.aclib.state.StateSerializer;
import ca.ubc.cs.beta.aclib.state.legacy.LegacyStateFactory;

public class StateMergeExecutor {

	private static  Logger log = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		StateMergeOptions smo = new StateMergeOptions();

		try 
		{
			JCommander jcom;
			try {
			 jcom = JCommanderHelper.parseCheckingForHelpAndVersion(args, smo);
			} finally
			{
				log = LoggerFactory.getLogger(StateMergeExecutor.class);
			}
			
			
			for(String file : jcom.getParameterFilesToRead())
			{
				log.info("Reading options from default file {} " , file);
			}
			
			
			Set<String> directoriesWithState = new HashSet<String>();
			log.info("Beginning Directory Scan");
			for(String dir: smo.directories)
			{
				 directoriesWithState.addAll(scanDirectories(dir));
			}
			
			log.info("Determining Scenario Options");
			
			List<ProblemInstance> pis = smo.scenOpts.getTrainingAndTestProblemInstances(".", 0, 0, true, false, false, false).getTrainingInstances().getInstances();;
			AlgorithmExecutionConfig execConfig = smo.scenOpts.getAlgorithmExecutionConfigSkipExecDirCheck(".");
			
			MapList<Integer, AlgorithmRun> runsPerIteration = new MapList<Integer, AlgorithmRun>(new LinkedHashMap<Integer, List<AlgorithmRun>>());
			
			for(String dir: directoriesWithState)
			{
				ThreadSafeRunHistory rh = new ThreadSafeRunHistoryWrapper(new NewRunHistory(smo.scenOpts.intraInstanceObj, smo.scenOpts.interInstanceObj, smo.scenOpts.runObj));
				restoreState(dir, smo.scenOpts, pis, execConfig, rh);
				
				for(RunData rd : rh.getAlgorithmRunData())
				{
					runsPerIteration.addToList(rd.getIteration(), rd.getRun());
				}
			}
			
			
			
			MapList<Integer, AlgorithmRun> repairedRuns = new MapList<Integer, AlgorithmRun>(new HashMap<Integer, List<AlgorithmRun>>());
			
			Map<String, ProblemInstance> fixedPi = new LinkedHashMap<String, ProblemInstance>();
			
			int instanceId = 1;
			Set<String> featureKeys = new HashSet<String>();
			ProblemInstance firstPi = null;
			for(Entry<Integer,List<AlgorithmRun>> runsForIt : runsPerIteration.entrySet())
			{
				for(AlgorithmRun run: runsForIt.getValue())
				{
					ProblemInstance pi =  run.getRunConfig().getProblemInstanceSeedPair().getInstance();
					
					
					
					
					ProblemInstance repairedPi;
					if(fixedPi.containsKey(pi.getInstanceName()))
					{
						repairedPi = fixedPi.get(pi.getInstanceName());
					} else
					{
						repairedPi = new ProblemInstance(pi.getInstanceName(), instanceId, pi.getFeatures(), pi.getInstanceSpecificInformation());
						fixedPi.put(pi.getInstanceName(), repairedPi);
						
						if(featureKeys.isEmpty())
						{
							featureKeys.addAll(pi.getFeatures().keySet());
							firstPi = pi;
						} else
						{
							if(!featureKeys.equals(pi.getFeatures().keySet()))
							{
								
								String prevMinusCurr = "";
								{
									Set<String> previousFeatures = new HashSet<String>(featureKeys);
									Set<String> currentFeatures = new HashSet<String>(pi.getFeatures().keySet());
									previousFeatures.removeAll(currentFeatures);
									prevMinusCurr = previousFeatures.toString();
								}
								
								String currMinusPrev = "";
								{
									Set<String> previousFeatures = new HashSet<String>(featureKeys);
									Set<String> currentFeatures = new HashSet<String>(pi.getFeatures().keySet());
									currentFeatures.removeAll(previousFeatures);
									currMinusPrev = currentFeatures.toString();
								}
							throw new ParameterException("Feature mismatch exception, features the current instance " + pi.getInstanceName() + " has but we previously on instance "+ firstPi.getInstanceName() +"  didn't find: " + currMinusPrev + " . Features the previous instance has but current instance doesn't: " + prevMinusCurr);
							}
						}
						
						instanceId++;
					}
					
					ProblemInstanceSeedPair newPisp = new ProblemInstanceSeedPair(repairedPi, run.getRunConfig().getProblemInstanceSeedPair().getSeed());
					RunConfig rc = new RunConfig(newPisp, run.getRunConfig().getCutoffTime(), run.getRunConfig().getParamConfiguration());
					
					ExistingAlgorithmRun repairedRun = new ExistingAlgorithmRun(run.getExecutionConfig(), rc, run.getRunResult(), run.getRuntime(), run.getRunLength(), run.getQuality(), run.getResultSeed(), run.getAdditionalRunData(), run.getWallclockExecutionTime());

					Object[] args2 = { runsForIt.getKey(), run.getRunConfig().getProblemInstanceSeedPair().getInstance(), run, repairedPi, repairedRun };
					log.debug("Run Restored on iteration {} : {} => {} repaired: {} => {}",args2);
					repairedRuns.addToList(runsForIt.getKey(), repairedRun);
					
				}
				
				
				
			}
			
			
			log.info("Processing Runs");
			
			ThreadSafeRunHistory rhToSave = new ThreadSafeRunHistoryWrapper(new NewRunHistory(smo.scenOpts.intraInstanceObj, smo.scenOpts.interInstanceObj, smo.scenOpts.runObj));
			
			//NOTE: I assume that the map will 
			for(Entry<Integer, List<AlgorithmRun>> itToRun :repairedRuns.entrySet())
			{
				
				for(AlgorithmRun run : itToRun.getValue())
				{
					try {
						
						rhToSave.append(run);
					} catch (DuplicateRunException e) {
					
						e.printStackTrace();
					}
				}
				rhToSave.incrementIteration();
			}
			
			for(RunData rd : rhToSave.getAlgorithmRunData())
			{
				log.info("Restored Data Iteration {} => {} ", rd.getIteration(), rd.getRun());
				
			}
			
			
			
			List<ProblemInstance> pisToSave = new ArrayList<ProblemInstance>();
			for(Entry<String, ProblemInstance> ent : fixedPi.entrySet())
			{
				pisToSave.add(ent.getValue());
			}
		
			saveState(smo.scenOpts.outputDirectory, rhToSave, pisToSave, execConfig.getParamFile().getParamFileName(), execConfig, smo.scenOpts);
			
		} catch(ParameterException e)
		{
			
			log.info("Error {}", e.getMessage());
			log.debug("Exception ", e);
		} catch(RuntimeException e)
		{
			log.error("Unknown Runtime Exception ",e);
			
		} catch (IOException e) {
			log.error("IO Exception occurred", e);
		}
	}

	
	private static void saveState(String dir, ThreadSafeRunHistory rh, List<ProblemInstance> pis, String configSpaceFileName, AlgorithmExecutionConfig execConfig, ScenarioOptions scenOpts) throws IOException 
	{
		
		//StateFactoryOptions sfo = new StateFactoryOptions();
		
		log.debug("Saving directory {}", dir);
		
		LegacyStateFactory lsf = new LegacyStateFactory(dir,null);
		
	
		StateSerializer ss = lsf.getStateSerializer("it", rh.getIteration());
		
		ss.setRunHistory(rh);
		
		StringBuilder scen = new StringBuilder();
		
		
		
		
		scen.append("# Automatically generated by State Merge Utility").append("\n");
		scen.append("algo="+execConfig.getAlgorithmExecutable()).append("\n");
		scen.append("execdir="+execConfig.getAlgorithmExecutionDirectory()).append("\n");
		scen.append("deterministic=" + scenOpts.algoExecOptions.deterministic).append("\n");
		scen.append("run_obj=" + scenOpts.runObj.toString().toLowerCase()).append("\n");
		scen.append("#outdir = (Outdir is not recommended in a scenario file anymore)").append("\n");
		scen.append("overall_obj=" + scenOpts.intraInstanceObj.toString().toLowerCase()).append("\n");
		scen.append("cutoff_time=" + execConfig.getAlgorithmCutoffTime()).append("\n");
		scen.append("tunerTimeout=" + scenOpts.limitOptions.tunerTimeout).append("\n");
		scen.append("paramfile=" + LegacyStateFactory.PARAM_FILE).append("\n");
		scen.append("instance_file=" + LegacyStateFactory.INSTANCE_FILE).append("\n");
		if(pis.get(0).getFeatures().size() > 0)
		{
			scen.append("feature_file=" + LegacyStateFactory.FEATURE_FILE).append("\n");
		}
		
		
		lsf.copyFileToStateDir(LegacyStateFactory.SCENARIO_FILE, new ReaderInputStream(new StringReader(scen.toString()),"UTF-8"));
		
		
		
		if(pis.get(0).getFeatures().size() > 0)
		{
		
			StringBuilder features = new StringBuilder();
			features.append(",");
			for(String key : pis.get(0).getFeatures().keySet())
			{
				features.append(key).append(",");
			}
			features.setCharAt(features.length()-1, '\n');
			
			for(ProblemInstance pi : pis)
			{
				features.append(pi.getInstanceName()).append(",");
				for(Entry<String, Double> ent : pi.getFeatures().entrySet())
				{
					features.append(ent.getValue()).append(",");
				}
				features.setCharAt(features.length()-1, '\n');
			}
			lsf.copyFileToStateDir(LegacyStateFactory.FEATURE_FILE, new ReaderInputStream(new StringReader(features.toString()),"UTF-8"));
		}
		
		StringBuilder piTxt = new StringBuilder();
		
		for(ProblemInstance pi : pis)
		{
			piTxt.append(pi.getInstanceName()).append("\n");
		}
		
		lsf.copyFileToStateDir(LegacyStateFactory.INSTANCE_FILE, new ReaderInputStream(new StringReader(piTxt.toString()),"UTF-8"));
		lsf.copyFileToStateDir(LegacyStateFactory.PARAM_FILE, new File(configSpaceFileName));
		
		
		ss.save();
	}

	private static void restoreState(String dir, ScenarioOptions scenOpts, List<ProblemInstance> pis, AlgorithmExecutionConfig execConfig, ThreadSafeRunHistory rh) throws IOException {
		
		StateFactoryOptions sfo = new StateFactoryOptions();
		
		log.debug("Restoring directory {}", dir);
		
		LegacyStateFactory lsf = new LegacyStateFactory(null, dir);
		
	
		for(File f : new File(dir).listFiles())
		{
			if(f.getName().equals(LegacyStateFactory.SCENARIO_FILE))
			{
				log.debug("Using built in scenario options for directory {}", dir);
				scenOpts = new ScenarioOptions();
				
				JCommander jcom = new JCommander(scenOpts);
				
				ArrayList<String> args = new ArrayList<String>();
				args.add("--scenario-file");
				args.add(f.getAbsolutePath());
				
				if(new File(dir + File.separator + LegacyStateFactory.INSTANCE_FILE).exists())
				{
					args.add("--instance-file");
					args.add(dir + File.separator + LegacyStateFactory.INSTANCE_FILE);
				}
				
				if(new File(dir + File.separator +LegacyStateFactory.FEATURE_FILE).exists())
				{
					args.add("--feature-file");
					args.add(dir + File.separator +LegacyStateFactory.FEATURE_FILE);
				} else if(new File(dir + File.separator +"instance-features.txt").exists())
				{
					args.add("--feature-file");
					args.add(dir + File.separator +"instance-features.txt");
				}
				jcom.parse(args.toArray(new String[0]));
				
				pis = scenOpts.getTrainingAndTestProblemInstances(dir, 0, 0, true, false, false, false).getTrainingInstances().getInstances();
				
			}
		}
			
		lsf.getStateDeserializer("it", Integer.MAX_VALUE, execConfig.getParamFile(), pis, execConfig, rh);
	}

	/**
	 * Scans directories and returns a list 
	 * @param dir
	 * @return
	 */
	private static Set<String> scanDirectories(String dirStr) {
		File dir = new File(dirStr).getAbsoluteFile();
		
		log.debug("Scanning directory {}", dir.getAbsolutePath());
		if (!dir.exists())
		{
			throw new ParameterException("Argument " + dir.getAbsolutePath()  + " does not exist");
		}
		if (!dir.canRead())
		{
			throw new ParameterException("Argument " + dir.getAbsolutePath()  + " cannot be read");
		}
		if (!dir.isDirectory())
		{
			throw new ParameterException("Argument " + dir.getAbsolutePath()  + " is not a directory");
		}
		
		Set<String> sd= scanDirectories(dir, new HashSet<String>());
		
		if(sd.isEmpty())
		{
			throw new ParameterException("Couldn't find any state files in " + dir.getAbsolutePath());
		} else
		{
			return sd;
		}
		
		
	}

	private static Set<String> scanDirectories(File dir, Set<String> absPathSearched)
	{
		
		if(absPathSearched.contains(dir.getAbsolutePath()))
		{
			return Collections.emptySet();
		} else
		{
			absPathSearched.add(dir.getAbsolutePath());
		}
		String s2 = LegacyStateFactory.getRunAndResultsFilename("", "(-it\\d+|)","","");
		
		Set<String> matchingDirectories = new HashSet<String>();
		for(String s : dir.list())
		{
			
			if(s.matches(s2))
			{
				log.debug("Directory contains saved SMAC data {}", dir);
				matchingDirectories.add(dir.getAbsolutePath());
			} 	
		}
		
		if(!matchingDirectories.isEmpty())
		{
			return matchingDirectories;
		}
		
		
		for(File f : dir.listFiles())
		{
			if(f.isDirectory() && f.canRead())
			{
				matchingDirectories.addAll(scanDirectories(f, absPathSearched));
			}
		}
		
		return matchingDirectories;
	}
}
