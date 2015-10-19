package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.callformatselector;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.factory.AlgorithmRunResultFactory;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import net.jcip.annotations.Immutable;

import java.util.ArrayList;

/**
 * Created by Steve Ramage <seramage@cs.ubc.ca> on 10/18/15
 */
@Immutable
public class AclibCallFormat extends CallFormatSelector {

    private static final CallFormatSelector instance = new AclibCallFormat();

    public static CallFormatSelector getInstance()
    {
        return instance;
    }

    private AclibCallFormat()
    {

    }

    @Override
    public String[] getCallString(AlgorithmRunConfiguration runConfig, boolean paramArgumentsContainQuotes) {
        AlgorithmExecutionConfiguration execConfig = runConfig.getAlgorithmExecutionConfiguration();

        ArrayList<String> list;

        //Approximate size of the array is 2 * the number of parameters + O(1).
        list = new ArrayList<>(runConfig.getParameterConfiguration().getParameterConfigurationSpace().getParameterNames().size() * 2 + 10);
        list.addAll(runConfig.getAlgorithmExecutionConfiguration().getExecutableAndArguments());
        if (runConfig.getCutoffTime() < Double.MAX_VALUE) {
            list.add("--cutoff");
            list.add(String.valueOf(runConfig.getCutoffTime()));
        }

        if (!runConfig.getProblemInstanceSeedPair().getProblemInstance().getInstanceName().equals("no_instance")) {
            list.add("--instance");
            list.add(String.valueOf(runConfig.getProblemInstanceSeedPair().getProblemInstance().getInstanceName()));
        }

        if (!runConfig.getProblemInstanceSeedPair().getProblemInstance().getInstanceSpecificInformation().equals("0")) {
            list.add("--instance-specific-information");
            list.add(runConfig.getProblemInstanceSeedPair().getProblemInstance().getInstanceSpecificInformation());
        }

        if (!runConfig.getAlgorithmExecutionConfiguration().isDeterministicAlgorithm()) {
            list.add("--seed");
            list.add(String.valueOf(runConfig.getProblemInstanceSeedPair().getSeed()));
        }

        list.add("--config");

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
        return AlgorithmRunResultFactory.parseForAclibFormat(line, runConfig, wallClockTimeInSeconds);
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
        return "ACLIB";
    }

}
