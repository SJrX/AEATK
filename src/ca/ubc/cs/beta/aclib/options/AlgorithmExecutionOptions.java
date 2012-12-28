package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.misc.jcommander.converter.BinaryDigitBooleanConverter;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.NonNegativeInteger;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.ReadableFileConverter;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;


/**
 * Options object that defines arguments for Target Algorithm Execution
 * @author sjr
 *
 */
@UsageTextField(title="Algorithm Execution Options", description="Options related to running the target algorithm")
public class AlgorithmExecutionOptions extends AbstractOptions {

	private static final String defaultSearchPath ;
	

	static{
		//==== This builds a giant string to search for other Target Algorithm Executors
		StringBuilder sb = new StringBuilder();
		String cwd = System.getProperty("user.dir");
		Set<String> files = new HashSet<String>();
		List<String> directoriesToSearch = new ArrayList<String>();
		
		directoriesToSearch.add(cwd);
		
		String[] classpath = System.getProperty("java.class.path").split(File.pathSeparator);
		
		String pluginDirectory = System.getProperty("user.dir");
		for(String location : classpath)
		{
			if(location.endsWith("aclib.jar"))
			{
				File f = new File(location);
				
				pluginDirectory = f.getParentFile().getAbsolutePath();
				break;
			}
				
		}
		
		pluginDirectory =new File(pluginDirectory) + File.separator + "plugins" + File.separator;
		
		directoriesToSearch.add(pluginDirectory);
		
		File pluginDir = new File(pluginDirectory);
		//We will look in the plugins directory and all sub directories, but not further
		if(pluginDir.exists())
		{
			for(File f : pluginDir.listFiles())
			{
				
				if(f.isDirectory())
				{
					directoriesToSearch.add(f.getAbsolutePath());
				}
			}
		}
		
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "MySQLDBTAE" + File.separator) ;
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "MySQLDBTAE" + File.separator + "lib" + File.separator) ;
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "MySQLDBTAE" + File.separator + "version" + File.separator) ;
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "MySQLDBTAE" + File.separator + "bin" + File.separator) ;
		directoriesToSearch.add(new File(cwd) + File.separator + "plugins" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "surrogates" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "RunDispatcher" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "surrogates" + File.separator + "bin" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "surrogates" + File.separator + "version" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "RunDispatcher" + File.separator + "bin" + File.separator) ;
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "surrogates" + File.separator + "lib" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "RunDispatcher" + File.separator + "lib" + File.separator) ;
		
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "RunDispatcher" + File.separator + "lib" + File.separator) ;
		
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "RunDispatcher" + File.separator + "version" + File.separator) ;
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
	
	
	@Parameter(names={"--algoExec", "--algo"}, description="command string to execute algorithm with", required=true)
	public String algoExec;
	
	@Parameter(names={"--execDir","--execdir"}, description="working directory to execute algorithm in", required=true)
	public String algoExecDir;
	
	@Parameter(names="--deterministic", description="treat the target algorithm as deterministic", converter=BinaryDigitBooleanConverter.class)
	public boolean deterministic;
	
	@UsageTextField(domain="")
	@Parameter(names={"--targetAlgorithmEvaluator","--tae"}, description="Target Algorithm Evaluator to use when making target algorithm calls")
	public String targetAlgorithmEvaluator = "CLI";

	@UsageTextField(defaultValues="<current working directory>/plugins/ amoung others" )
	@Parameter(names={"--targetAlgorithmEvaluatorSearchPath","--taeSP"}, description="location to look for other target algorithm evaluators [ See manual but generally you can ignore this ] ")
	public String taeSearchPath = defaultSearchPath;

	@Parameter(names="--logAllCallStrings", description="log every call string")
	public boolean logAllCallStrings = false;
	
	@Parameter(names="--logAllProcessOutput", description="log all process output")
	public boolean logAllProcessOutput = false;
	
	@Parameter(names="--abortOnCrash", description="treat algorithm crashes as an ABORT (Useful if algorithm should never CRASH). NOTE:  This only aborts if all retries fail.")
	public boolean abortOnCrash = false;

	@Parameter(names="--abortOnFirstRunCrash", description="if the first run of the algorithm CRASHED treat it as an ABORT, otherwise allow crashes.")
	public boolean abortOnFirstRunCrash = true;

	@Parameter(names="--retryTargetAlgorithmRunCount", description="number of times to retry an algorithm run before eporting crashed (NOTE: The original crashes DO NOT count towards any time limits, they are in effect lost). Additionally this only retries CRASHED runs, not ABORT runs, this is by design as ABORT is only for cases when we shouldn't bother further runs", validateWith=NonNegativeInteger.class)
	public int retryCount = 0;

	@Parameter(names={"--numConcurrentAlgoExecs","--maxConcurrentAlgoExecs","--numberOfConcurrentAlgoExecs"}, description="maximum number of concurrent target algorithm executions", validateWith=PositiveInteger.class)
	public int maxConcurrentAlgoExecs = 1;
	
	@UsageTextField(defaultValues="")
	@Parameter(names="--runHashCodeFile", description="file containing a list of run hashes one per line: Each line should be: \"Run Hash Codes: (Hash Code) After (n) runs\". The number of runs in this file need not match the number of runs that we execute, this file only ensures that the sequences never diverge. Note the n is completely ignored so the order they are specified in is the order we expect the hash codes in this version. Finally note you can simply point this at a previous log and other lines will be disregarded", converter=ReadableFileConverter.class)
	public File runHashCodeFile;

	@Parameter(names="--verifySAT", description="Check SAT/UNSAT/UNKNOWN responses against Instance specific information (if null then performs check if every instance has specific information in the following domain {SAT, UNSAT, UNKNOWN, SATISFIABLE, UNSATISFIABLE}")
	public Boolean verifySAT;
		
	
}
