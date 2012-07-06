package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;

import com.beust.jcommander.Parameter;


/**
 * Options object that defines arguments for Target Algorithm Execution
 * @author sjr
 *
 */
public class AlgorithmExecutionOptions extends AbstractOptions {

	private static final String defaultSearchPath ;
	
	static{
		
		StringBuilder sb = new StringBuilder();
		String cwd = System.getProperty("user.dir");
		
		List<String> directoriesToSearch = new ArrayList<String>();
		
		directoriesToSearch.add(cwd);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "surrogates" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "RunDispatcher" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "surrogates" + File.separator + "bin" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "RunDispatcher" + File.separator + "bin" + File.separator) ;
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "surrogates" + File.separator + "lib" + File.separator);
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "RunDispatcher" + File.separator + "lib" + File.separator) ;
		directoriesToSearch.add(new File(cwd).getParent() + File.separator + "rundispatcher" + File.separator);
		
		
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
								sb.append(dir.getAbsolutePath());
								sb.append(File.separator);
								sb.append(fileName);
								sb.append(File.pathSeparator);
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
	
	@Parameter(names="--deterministic", description="Whether the target algorithm is deterministic (0 no, 1 yes) [An integer due to backwards compatibility]")
	public int deterministic = 0;
	
	@Parameter(names={"--targetAlgorithmEvaluator","--tae"}, description="System we should use to dispatch algorithm requests [ See manual but CLI  (via Command Line) is default and generally what you want ]")
	public String targetAlgorithmEvaluator = "CLI";

	@Parameter(names={"--targetAlgorithmEvaluatorSearchPath","--taeSP"}, description="Where we should look for other target algorithm evaluators [ See manual but generally you can ignore this ] ")
	public String taeSearchPath = defaultSearchPath;
	
	public AlgorithmExecutionConfig getAlgorithmExecutionConfig(ParamConfigurationSpace p)
	{
		return new AlgorithmExecutionConfig(algoExec, algoExecDir, p, false, (deterministic > 0));
	}
	
	
}
