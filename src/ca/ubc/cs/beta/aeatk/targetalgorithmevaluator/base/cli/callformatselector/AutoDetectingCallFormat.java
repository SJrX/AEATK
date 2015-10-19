package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.callformatselector;

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.factory.AlgorithmRunResultFactory;
import net.jcip.annotations.Immutable;

/**
 * Created by Steve Ramage <seramage@cs.ubc.ca> on 10/18/15
 */
@Immutable
public class AutoDetectingCallFormat extends LegacyCallFormat {

    private static final CallFormatSelector instance = new AutoDetectingCallFormat();

    public static CallFormatSelector getInstance()
    {
        return instance;
    }

    private AutoDetectingCallFormat()
    {
        super();
    }

    @Override
    public AlgorithmRunResult getAlgorithmRunResult(String line, AlgorithmRunConfiguration runConfig, double wallClockTimeInSeconds) {
        return AlgorithmRunResultFactory.parseAutoDetectedFormat(line, runConfig, wallClockTimeInSeconds);
    }
    @Override
    public CallFormatSelector onAbortTry() {
        return AclibCallFormat.getInstance();
    }

    @Override
    public CallFormatSelector onSuccessUse() {
        return LegacyCallFormat.getInstance();
    }

    @Override
    public boolean shouldSwitch() {
        return true;
    }

    public String toString()
    {
        return "AUTO-DETECT-LEGACY";
    }

}
