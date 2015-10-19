package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.callformatselector;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.factory.AlgorithmRunResultFactory;
import ca.ubc.cs.beta.aeatk.misc.string.SplitQuotedString;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import net.jcip.annotations.Immutable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Steve Ramage <seramage@cs.ubc.ca> on 10/18/15
 */
@Immutable
public class LegacyCallFormat extends CallFormatSelector {

    private static final CallFormatSelector instance = new LegacyCallFormat();

    public static CallFormatSelector getInstance()
    {
        return instance;
    }

    protected LegacyCallFormat()
    {

    }

    @Override
    public String[] getCallString(AlgorithmRunConfiguration runConfig, boolean paramArgumentsContainQuotes) {

        AlgorithmExecutionConfiguration execConfig = runConfig.getAlgorithmExecutionConfiguration();

        ArrayList<String> list;

        list = new ArrayList<>(execConfig.getExecutableAndArguments());

        list.add(runConfig.getProblemInstanceSeedPair().getProblemInstance().getInstanceName());
        list.add(runConfig.getProblemInstanceSeedPair().getProblemInstance().getInstanceSpecificInformation());
        list.add(String.valueOf(runConfig.getCutoffTime()));
        list.add(String.valueOf(Integer.MAX_VALUE));
        list.add(String.valueOf(runConfig.getProblemInstanceSeedPair().getSeed()));

        ParameterConfiguration.ParameterStringFormat f = ParameterConfiguration.ParameterStringFormat.NODB_SYNTAX;

        final String valueDelimiter = (paramArgumentsContainQuotes) ?  f.getValueDelimeter() : "";

        for(String key : runConfig.getParameterConfiguration().getActiveParameters() )
        {
            if(!f.getKeyValueSeperator().equals(" ") || !f.getGlue().equals(" "))
            {
                throw new IllegalStateException("Key Value separator or glue is not a space, and this means the way we handle this logic won't work currently");
            }
            list.add(f.getPreKey() + key);


            list.add(valueDelimiter + runConfig.getParameterConfiguration().get(key)  + valueDelimiter);

        }

        return list.toArray(new String[list.size()]);
    }

    @Override
    public AlgorithmRunResult getAlgorithmRunResult(String line, AlgorithmRunConfiguration runConfig, double wallClockTimeInSeconds) {
        return AlgorithmRunResultFactory.parseLegacyFormat(line, runConfig, wallClockTimeInSeconds);
    }

    @Override
    public CallFormatSelector onAbortTry() {
        return null;
    }

    @Override
    public CallFormatSelector onSuccessUse() {
        return null;
    }

    @Override
    public boolean shouldSwitch() {
        return false;
    }

    public String toString()
    {
        return "LEGACY";
    }

}
