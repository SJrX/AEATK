package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.misc.jcommander.converter.BinaryDigitBooleanConverter;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.NonNegativeInteger;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.ReadableFileConverter;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;


/**
 * Options object that defines arguments for Target Algorithm Execution
 * @author sjr
 *
 */
public class AlgorithmExecutionOptions extends AbstractOptions {

	private static final String defaultSearchPath ;
	
	static{
		//==== This builds a giant string to search for other Target Algorithm Executors
		StringBuilder sb = new StringBuilder();
		String cwd = System.getProperty("user.dir");
		Set<String> files = new HashSet<String>();
		List<String> directoriesToSearch = new ArrayList<String>();
		
		directoriesToSearch.add(cwd);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "plugins" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "surrogates" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "RunDispatcher" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "surrogates" + File.separator + "bin" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "RunDispatcher" + File.separator + "bin" + File.separator) ;
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "surrogates" + File.separator + "lib" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "RunDispatcher" + File.separator + "lib" + File.separator) ;
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "rundispatcher" + File.separator);
		
		directoriesToSearch.add(System.getProperty("java.class.path"));
		for(String dirName : directoriesToSearch)
		{
			File dir = new File(dirName);
			sb.append(dirName);
			sb.append(File.pathSeparator);
			if(dir.exists())
			{
				if(dir.canRead())
				{
					if(dir.isDirectory())
					{
						for(String fileName : dir.list())
						{
							if(fileName.trim().endsWith(".jar"))
							{
								if(!files.contains(fileName))
								{
									sb.append(dir.getAbsolutePath());
									sb.append(File.separator);
									sb.append(fileName);
									sb.append(File.pathSeparator);
									
									//System.out.println("Adding " + fileName);
									files.add(fileName);
								}
							}
						}
					}	
				}
			}
		}
		
		defaultSearchPath = sb.toString();
		
	}
	@Parameter(names={"--algoExec", "--algo"}, description="Command String to execution", required=true)
	public String algoExec;
	
	@Parameter(names={"--execDir","--execdir"}, description="Working directory to execute algorithm in", required=true)
	public String algoExecDir;
	
	@Parameter(names="--deterministic", description="Whether the target algorithm is deterministic  (Supports integers for backwards compatibility)", converter=BinaryDigitBooleanConverter.class)
	public boolean deterministic;
	
	@Parameter(names={"--targetAlgorithmEvaluator","--tae"}, description="System we should use to dispatch algorithm requests [ See manual but CLI  (via Command Line) is default and generally what you want ]")
	public String targetAlgorithmEvaluator = "CLI";

	@Parameter(names={"--targetAlgorithmEvaluatorSearchPath","--taeSP"}, description="Where we should look for other target algorithm evaluators [ See manual but generally you can ignore this ] ")
	public String taeSearchPath = defaultSearchPath;
	

	@Parameter(names="--logAllCallStrings", description="Output in the log every call string")
	public boolean logAllCallStrings = false;
	
	@Parameter(names="--logAllProcessOutput", description="Log all process output")
	public boolean logAllProcessOutput = false;
	
	@Parameter(names="--abortOnCrash", description="Treat algorithm crashes as ABORT (Useful if the algorithm really should never CRASH)")
	public boolean abortOnCrash = false;

	@Parameter(names="--abortOnFirstRunCrash", description="If the first run of the algorithm CRASHED treat it as an ABORT, otherwise leave it alone")
	public boolean abortOnFirstRunCrash = true;

	@Parameter(names="--retryTargetAlgorithmRunCount", description="Number of times to retry an algorithm run before eporting crashed (NOTE: The original crashes DO NOT count towards any time limits, they are in effect lost). Additionally this only retries CRASHED runs, not ABORT runs, this is by design as ABORT is only for cases when we shouldn't bother further runs", validateWith=NonNegativeInteger.class)
	public int retryCount = 0;

	@Parameter(names="--maxConcurrentAlgoExecs", description="Maximum number of concurrent target algorithm executions", validateWith=PositiveInteger.class)
	public int maxConcurrentAlgoExecs = 1;
	
	@Parameter(names="--runHashCodeFile", description="File containing a list of Run Hashes one per line (Either with just the format on each line, or with the following text per line: \"Run Hash Codes: (Hash Code) After (n) runs\". The number of runs in this file need not match the number of runs that we execute, this file only ensures that the sequences never diverge. Note the n is completely ignored so the order they are specified in is the order we expect the hash codes in this version", converter=ReadableFileConverter.class)
	public File runHashCodeFile;
		
	/*
	public AlgorithmExecutionConfig getAlgorithmExecutionConfig(ParamConfigurationSpace p, File experimentDir)
	{
		
		if(!new File(algoExecDir).isAbsolute())
		{
			algoExecDir = experimentDir.getAbsolutePath() + File.separator +algoExecDir; 
		}
		
		return new AlgorithmExecutionConfig(algoExec, algoExecDir, p, false, deterministic);
	}
	
	*/
}
