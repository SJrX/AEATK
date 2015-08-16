package ca.ubc.cs.beta.aeatk.example.jsonexecutor;

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.misc.jcommander.JCommanderHelper;
import ca.ubc.cs.beta.aeatk.misc.returnvalues.AEATKReturnValues;
import ca.ubc.cs.beta.aeatk.options.AbstractOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorCallback;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorRunObserver;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.WaitableTAECallback;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple utility class that provides the ability to execute a single run against a <code>TargetAlgorithmEvaluator</code>.
 *
 * This class serves two purposes: 
 * <p>
 * From a usage perspective, people should be able to test their wrappers or target algorithms easily
 * <p>
 * From a documentation perspective, this class should serve as an example for using TargetAlgorithmEvaluators and other aspects AEATK.
 * 
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
public class JSONExecutor 
{

	//SLF4J Logger object (not-initialized on start up in case command line options want to change it)
	private static Logger log;
	
	public static void main(String[] args)
	{

		JSONExecutorOptions mainOptions = new JSONExecutorOptions();
		
		
		//Map object that for each available TargetAlgorithmEvaluator gives it's associated options object
		Map<String,AbstractOptions> taeOptions = mainOptions.taeOptions.getAvailableTargetAlgorithmEvaluators();

		try {
			
			//Parses the options given in the args array and sets the values
			JCommander jcom;
			try {
				//This will check for help and version arguments 
				jcom = JCommanderHelper.parseCheckingForHelpAndVersion(args, mainOptions,taeOptions);
				
				//Does any setup work necessary to setup logger.
				mainOptions.logOpts.initializeLogging();
			} finally
			{
				//Initialize the logger *AFTER* the JCommander objects have been parsed
				//So that options that take effect
				log = LoggerFactory.getLogger(JSONExecutor.class);
			}


			try( TargetAlgorithmEvaluator tae = mainOptions.taeOptions.getTargetAlgorithmEvaluator( taeOptions) ) {
				
				log.info("Waiting for an array of AlgorithmRunConfiguration in JSON format to be recieved on STDIN. You can use the algo-test utility --print-json true to get example JSON.");
				JsonFactory jfactory = new JsonFactory();
				
				ObjectMapper map = new ObjectMapper(jfactory);
				SimpleModule sModule = new SimpleModule("MyModule", new Version(1, 0, 0, null));
				map.registerModule(sModule);

				
				JsonParser jParser = jfactory.createParser(System.in);
				
				
				List<AlgorithmRunConfiguration> runsToDo = new ArrayList<AlgorithmRunConfiguration>(Arrays.asList(map.readValue(jParser, AlgorithmRunConfiguration[].class)));
			
				final AtomicBoolean outputCompleted = new AtomicBoolean(false);
				TargetAlgorithmEvaluatorRunObserver taeRunObserver = new TargetAlgorithmEvaluatorRunObserver()
				{

					@Override
					public void currentStatus(
							List<? extends AlgorithmRunResult> runs) {
						int total = runs.size();
						int completed = 0;
						int notStarted = 0;
						int started = 0;
						for(AlgorithmRunResult run : runs)
						{
							if(run.isRunCompleted())
							{
								completed++;
							} else if (run.getRuntime() > 0 || run.getWallclockExecutionTime() > 0)
							{
								started++;
							} else
							{
								notStarted++;
							}
							
						}
						if(!outputCompleted.get())
						{
							log.info("Current Run Status, Total: {} , Started: {} , Completed: {}, Not Started: {}", total, started, completed, notStarted);
						}
						
					}
					
				};

				WaitableTAECallback taeCallback = new WaitableTAECallback(new TargetAlgorithmEvaluatorCallback(){

					@Override
					public void onSuccess(List<AlgorithmRunResult> runs) {
						ObjectMapper map = new ObjectMapper();
						JsonFactory factory = new JsonFactory();
						factory.setCodec(map);


						try {
                            ByteArrayOutputStream bout = new ByteArrayOutputStream();
                            JsonGenerator g = factory.createGenerator(bout);

                            SimpleModule sModule = new SimpleModule("MyModule", new Version(1, 0, 0, null));
                            map.configure(SerializationFeature.INDENT_OUTPUT, true);

                            map.registerModule(sModule);

                            List<AlgorithmRunResult> results2 = new ArrayList<>(runs);
                            g.writeObject(results2);
                            g.flush();

                            System.out.flush();
                            System.out.println("******JSON******\n" + bout.toString("UTF-8") + "\n********");
                            System.out.flush();
                        } catch(Exception e)
                        {
                            e.printStackTrace();
                        }

					}

					@Override
					public void onFailure(RuntimeException e) {
                            e.printStackTrace();
					}
				});


				if(mainOptions.printStatus)
				{
					log.info("Periodically printing status, use --print-status false to disable");
					tae.evaluateRunsAsync(runsToDo, taeCallback, taeRunObserver);
				} else
				{
				    tae.evaluateRunsAsync(runsToDo, taeCallback);
				}



                if(!tae.areRunsPersisted() || mainOptions.waitForPersistedRunCompletion)
                {
                    log.debug("Waiting until validation completion");
                    taeCallback.waitForCompletion();
                } else
                {

                    // Presumably if the runs were done, then we already notified the callback.
                    if ((tae.getNumberOfOutstandingBatches() > 1) && mainOptions.printStatus)
                    {
                        System.out.flush();
                        System.out.println("Runs Submitted... Re-execute this utility to get the results.");
                        System.out.flush();
                    }
                }

                outputCompleted.set(true);

			} 
			

		} catch(ParameterException e)
		{	
			log.error(e.getMessage());
			if(log.isDebugEnabled())
			{
				log.error("Stack trace:",e);
			}
            System.exit(AEATKReturnValues.PARAMETER_EXCEPTION);
		} catch(Exception e)
		{
            System.exit(AEATKReturnValues.OTHER_EXCEPTION);
		}
	}
}
