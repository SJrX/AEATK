package ca.ubc.cs.beta.aclib.algorithmrun;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
/**
 * Class that is used to take an existing algorithm run (from for instance a string), and create an AlgorithmRun object
 * @author seramage
 */
public class ExistingAlgorithmRun extends AbstractAlgorithmRun {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7798477429606839878L;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	
	/**
	 * Default Constructor
	 * @param execConfig		execution configuration of the object
	 * @param runConfig			run configuration we are executing
	 * @param result			result string to parse. The format of this is currently everything after the : in the result line of {@link CommandLineAlgorithmRun}. We support both the String for the RunResult, as well as the Status Code
	 */
	public ExistingAlgorithmRun(AlgorithmExecutionConfig execConfig, RunConfig runConfig, String result) {
		super(execConfig, runConfig);
		//this.rawResultLine = resultLine;
		//this.runCompleted = true;
		String[] resultLine = result.split(",");
		
		try
		{
			RunResult acResult;
			try {
				acResult = RunResult.getAutomaticConfiguratorResultForCode(Integer.valueOf(resultLine[0]));
			} catch(NumberFormatException e)
			{
				acResult = RunResult.getAutomaticConfiguratorResultForKey(resultLine[0]);
			}
			
			
			double runtime = Double.valueOf(resultLine[1].trim());
			double runLength = Double.valueOf(resultLine[2].trim());
			double quality = Double.valueOf(resultLine[3].trim());
			long resultSeed = Long.valueOf(resultLine[4].trim());
			String additionalRunData = "";
			if(resultLine.length == 6)
			{
				additionalRunData = resultLine[5].trim();
			}
			
			
			this.setResult(acResult, runtime, runLength, quality, resultSeed, result, additionalRunData);
			
			
		} catch(ArrayIndexOutOfBoundsException e)
		{ 
			Object[] args = { execConfig, runConfig, result} ;
			
			log.info("Malformed Run Result for Execution (ArrayIndexOutOfBoundsException): {}, Instance: {}, Result: {}", args);
			log.info("Exception:",e);
			this.setAbortResult(e.getMessage());
		}catch(NumberFormatException e)
		{
			//There was a problem with the output, we just set this flag

			Object[] args = { execConfig, runConfig, result} ;
			log.info("Malformed Run Result for Execution (NumberFormatException): {}, Instance: {}, Result: {}", args);
			log.info("Exception:",e);
			this.setAbortResult( e.getMessage());
			
			
		}
		
		

	}

	@Override
	public void run() {
		//NO OP

	}

}
