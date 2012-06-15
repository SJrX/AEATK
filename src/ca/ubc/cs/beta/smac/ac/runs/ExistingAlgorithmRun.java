package ca.ubc.cs.beta.smac.ac.runs;

import org.slf4j.Logger;
import ca.ubc.cs.beta.ac.RunResult;

import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.ac.config.RunConfig;
import ca.ubc.cs.beta.config.AlgorithmExecutionConfig;
/**
 * Class reperesents an Existing Algorithm Run that we can load 
 * @author seramage
 *
 */
public class ExistingAlgorithmRun extends AbstractAlgorithmRun {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7798477429606839878L;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	
	
	public ExistingAlgorithmRun(AlgorithmExecutionConfig execConfig,
			RunConfig instanceConfig, String result) {
		super(execConfig, instanceConfig);
		this.rawResultLine = resultLine;
		this.runCompleted = true;
		String[] resultLine = result.split(", ");
		
		try
		{
			
			this.acResult = RunResult.getAutomaticConfiguratorResultForCode(Integer.valueOf(resultLine[0]));
			this.runtime = Double.valueOf(resultLine[1]);
			this.runLength = Double.valueOf(resultLine[2]);
			this.quality = Double.valueOf(resultLine[3]);
			this.resultSeed = Long.valueOf(resultLine[4]);
			this.resultLine = result;
			
			runResultWellFormed = true;
		} catch(ArrayIndexOutOfBoundsException e)
		{ 
			Object[] args = { execConfig, instanceConfig, result} ;
			
			log.info("Malformed Run Result for Execution (ArrayIndexOutOfBoundsException): {}, Instance: {}, Result: {}", args);
			log.info("Exception:",e);
			runResultWellFormed = false;
		}catch(NumberFormatException e)
		{
			//There was a problem with the output, we just set this flag
			//e.printStackTrace();
			Object[] args = { execConfig, instanceConfig, result} ;
			log.info("Malformed Run Result for Execution (NumberFormatException): {}, Instance: {}, Result: {}", args);
			log.info("Exception:",e);
			
			runResultWellFormed = false;
			
		}
		
		

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
