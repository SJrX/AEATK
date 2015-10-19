package ca.ubc.cs.beta.aeatk.algorithmrunresult.factory;

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.ExistingAlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunExecutionStatus;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.exceptions.IllegalWrapperOutputException;
import ca.ubc.cs.beta.aeatk.misc.logback.MarkerFilter;
import ca.ubc.cs.beta.aeatk.misc.logging.LoggingMarker;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.CommandLineAlgorithmRun;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class that is helpful for parsing lines of output and converting it into AlgorithmRunResult objects.
 *
 * Created by Steve Ramage <seramage@cs.ubc.ca> on 10/18/15
 */
@Immutable
public final class AlgorithmRunResultFactory {


    /**
     * Note: The new format is FAR more anal retentive about the format of the line
     * We don't use a regex, it must start with this string
     */
    public static final String ACLIB_FORMAT_RESULT_PREFIX ="Result of this algorithm run:";

    //maybe merge these one day
    public static final String AUTOMATIC_CONFIGURATOR_RESULT_REGEX = "^\\s*Result\\s*of\\s*(this)?\\s*[Aa]lgorithm\\s*[rR]un\\s*:";

    public static final String OLD_LEGACY_AUTOMATIC_CONFIGURATOR_RESULT_REGEX = "^\\s*(Final)?\\s*[Rr]esult\\s+(?:([Ff]or)|([oO]f))\\s+(?:(HAL)|(ParamILS)|(SMAC)|([tT]his [wW]rapper)):";

    /**
     * Compiled REGEX
     */
    private static final Pattern pattern = Pattern.compile(AlgorithmRunResultFactory.AUTOMATIC_CONFIGURATOR_RESULT_REGEX);

    private static final Pattern oldPattern = Pattern.compile(AlgorithmRunResultFactory.OLD_LEGACY_AUTOMATIC_CONFIGURATOR_RESULT_REGEX);

    private static final Logger log = LoggerFactory.getLogger(AlgorithmRunResultFactory.class);

    private static final Marker fullProcessOutputMarker = MarkerFactory.getMarker(LoggingMarker.FULL_PROCESS_OUTPUT.name());

    /**
     * Thread safe reading of JSON
     */
    private static final ObjectReader jsonReader = (new ObjectMapper()).reader(Map.class);


    public static final String RUNTIME_KEY = "runtime";

    public static final String COST_KEY = "cost";

    public static final String RUNLENGTH_KEY = "runlength";

    public static final String STATUS_KEY = "status";

    /**
     * @deprecated You probably want to use a dedicated key instead of this old field.
     */
    public static final String ADDITIONAL_RUN_DATA_KEY = "additional-run-data";

    public static final String SATISFIABILITY_KEY = "satisfiability";


    /**
     * Returns an AlgorithmRunResult object from the line input, trying it's best to detect the format.
     * @param line
     * @param runConfig
     * @param wallTimeInSeconds
     * @return
     */
    public static AlgorithmRunResult parseAutoDetectedFormat(String line, AlgorithmRunConfiguration runConfig, double wallTimeInSeconds)
    {
        if (line.startsWith(ACLIB_FORMAT_RESULT_PREFIX))
        {
            if (!line.contains("{"))
            {
                return parseLegacyFormat(line, runConfig, wallTimeInSeconds);
            }
            return parseForAclibFormat(line, runConfig, wallTimeInSeconds);
        } else
        {
            return parseLegacyFormat(line, runConfig, wallTimeInSeconds);
        }

    }
    /**
     * Returns an AlgorithmRunResult object from the line input if it conforms to the old format.
     *
     * The old format is best documented in the SMAC Manual for version 2.0-2.10
     *
     * It looks like (although there are variations)
     *
     * Result for ParamILS: <solved>, <runtime>, <runlength>, <quality>, <seed>, <additional run data>
     *
     * @param line input from algorithm execution
     * @param runConfig the run configuration associated with this output.
     * @param wallTimeInSeconds the amount of walltime in seconds that have occurred.
     * @return object representation or null if not found
     */
    public static AlgorithmRunResult parseLegacyFormat(String line, AlgorithmRunConfiguration runConfig, double wallTimeInSeconds)
    {

        Matcher matcher = pattern.matcher(line);

        Matcher matcher2 = oldPattern.matcher(line);

        if (!matcher.find() && !matcher2.find()) {
            return null;
        }

        String fullLine = line.trim();
        String additionalRunData = "";
        try
        {

            String acExecResultString = line.substring(line.indexOf(":")+1).trim();

            String[] results = acExecResultString.split(",");
            for(int i=0; i < results.length; i++)
            {
                results[i] = results[i].trim();
            }

            RunStatus acResult =  RunStatus.getAutomaticConfiguratorResultForKey(results[0]);

            if(!acResult.permittedByWrappers())
            {
                throw new IllegalArgumentException("The Run Result reported is NOT permitted to be output by a wrapper and is for internal AEATK/SMAC use only.");
            }

            String runtime = results[1].trim();
            String runLength = results[2].trim();
            String bestSolution = results[3].trim();
            String seed = results[4].trim();

            if(results.length == 6)
            {
                additionalRunData = results[5].trim();
            } else if(results.length > 6)
            {
                log.warn("Too many fields were encountered (expected 5 or 6) when parsing line (Additional Run Data cannot have commas): {}\n ",line);
            }

            double runLengthD = Double.valueOf(runLength);
            double runtimeD = Double.valueOf(runtime);
            double qualityD = Double.valueOf(bestSolution);
            long resultSeedD = Long.valueOf(seed);
            if(!MarkerFilter.log(fullProcessOutputMarker.getName()))
            {
                log.info("Algorithm Reported: {}" , line);
            }

            return new ExistingAlgorithmRunResult(runConfig, acResult, runtimeD, runLengthD, qualityD, resultSeedD,  additionalRunData, wallTimeInSeconds);

        } catch(NumberFormatException e)
        {

            //Numeric value is probably at fault

            Object[] args = {CommandLineAlgorithmRun.getTargetAlgorithmExecutionCommandAsString(runConfig), fullLine};
            log.error("Target Algorithm Call failed:{}\nResponse:{}\nComment: Most likely one of the values of runLength, runtime, quality could not be parsed as a Double, or the seed could not be parsed as a valid long", args);
            log.error("Exception that occured trying to parse result was: ", e);
            log.error("Run will be counted as {}", RunStatus.CRASHED);
            return  new ExistingAlgorithmRunResult(runConfig, RunStatus.CRASHED, runConfig.getAlgorithmExecutionConfiguration().getAlgorithmMaximumCutoffTime(), 0, 0, 0, "ERROR: Couldn't parse output from wrapper (invalid number format): " + e.getMessage(), wallTimeInSeconds);

        } catch(IllegalArgumentException e)
        { 	//The RunResult probably doesn't match anything
            //this.setCrashResult("Output:" + fullLine + "\n Exception Message: " + e.getMessage() + "\n Name:" + e.getClass().getCanonicalName());



            ArrayList<String> validValues = new ArrayList<String>();
            for(RunStatus r : RunStatus.values())
            {
                if(r.permittedByWrappers())
                {
                    validValues.addAll(r.getAliases());
                }
            }
            Collections.sort(validValues);

            String[] validArgs = validValues.toArray(new String[0]);


            Object[] args = { CommandLineAlgorithmRun.getTargetAlgorithmExecutionCommandAsString(runConfig), fullLine, Arrays.toString(validArgs)};
            log.error("Target Algorithm Call failed:{}\nResponse:{}\nComment: Most likely the Algorithm did not report a result string as one of: {}", args);
            log.error("Exception that occured trying to parse result was: ", e);
            log.error("Run will be counted as {}", RunStatus.CRASHED);
            return new ExistingAlgorithmRunResult(runConfig, RunStatus.CRASHED, runConfig.getAlgorithmExecutionConfiguration().getAlgorithmMaximumCutoffTime(), 0, 0, 0, "ERROR: Couldn't parse output from wrapper (not enough arguments): " + e.getMessage(), wallTimeInSeconds);
        } catch(ArrayIndexOutOfBoundsException e)
        {
            //There aren't enough commas in the output

            Object[] args = { CommandLineAlgorithmRun.getTargetAlgorithmExecutionCommandAsString(runConfig), fullLine};
            log.error("Target Algorithm Call failed:{}\nResponse:{}\nComment: Most likely the algorithm did not specify all of the required outputs that is <solved>,<runtime>,<runlength>,<quality>,<seed>", args);
            log.error("Exception that occurred trying to parse result was: ", e);
            log.error("Run will be counted as {}", RunStatus.CRASHED);
            return new ExistingAlgorithmRunResult(runConfig, RunStatus.CRASHED, runConfig.getAlgorithmExecutionConfiguration().getAlgorithmMaximumCutoffTime(), 0, 0, 0, "ERROR: Couldn't parse output from wrapper (problem with arguments): " + e.getMessage(), wallTimeInSeconds);
        }
    }

    /**
     * Returns an AlgorithmRunResult object from the line input if it conforms to the new format.
     * @param line
     * @return
     */
    public static AlgorithmRunResult parseForAclibFormat(String line, AlgorithmRunConfiguration runConfig, double wallTimeInSeconds)
    {
        if (!line.startsWith(ACLIB_FORMAT_RESULT_PREFIX))
        {
            return null;
        }

        String jsonObject = line.substring(ACLIB_FORMAT_RESULT_PREFIX.length()).trim();

        try {
            Map<String, Object> result = jsonReader.readValue(jsonObject);

            return new ExistingAlgorithmRunResult(runConfig, result, wallTimeInSeconds);
        } catch (IOException e) {
            log.error("Target Algorithm Call failed:{}\nResponse:{}\nComment: Most likely we could not find valid JSON in the result.", CommandLineAlgorithmRun.getTargetAlgorithmExecutionCommandAsString(runConfig), line);
            log.error("Exception that occurred trying to parse result was: ", e);
            log.error("Run will be counted as {}", RunExecutionStatus.CRASHED);
            //TODO change this to new constructor
            return new ExistingAlgorithmRunResult(runConfig, RunStatus.CRASHED, runConfig.getAlgorithmExecutionConfiguration().getAlgorithmMaximumCutoffTime(), 0, 0, 0, "ERROR: Couldn't parse output from wrapper (problem with arguments): " + e.getMessage(), wallTimeInSeconds);

        } catch (RuntimeException e)
        {
            log.error("Target Algorithm Call failed:{}\nResponse:{}\nComment: Most likely one of more outputs did not obey expected semantics.", CommandLineAlgorithmRun.getTargetAlgorithmExecutionCommandAsString(runConfig), line);
            log.error("Exception that occurred trying to parse result was: ", e);
            log.error("Run will be counted as {}", RunExecutionStatus.CRASHED);

            //TODO change this to new constructor
            return new ExistingAlgorithmRunResult(runConfig, RunStatus.CRASHED, runConfig.getAlgorithmExecutionConfiguration().getAlgorithmMaximumCutoffTime(), 0, 0, 0, "ERROR: Couldn't parse output from wrapper (problem with arguments): " + e.getMessage(), wallTimeInSeconds);
        }

    }



    private AlgorithmRunResultFactory()
    {
        //Don't instantiate this
    }
}
