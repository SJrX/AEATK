package ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.cli.callformatselector;

import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import net.jcip.annotations.Immutable;

/**
 * Created by Steve Ramage <seramage@cs.ubc.ca> on 10/18/15
 */
@Immutable
public abstract class CallFormatSelector {

    public abstract String[] getCallString(AlgorithmRunConfiguration runConfig, boolean paramArgumentsContainQuotes);

    public abstract AlgorithmRunResult getAlgorithmRunResult(String line, AlgorithmRunConfiguration runConfig, double wallClockTimeInSeconds);

    public abstract CallFormatSelector onAbortTry();

    public abstract CallFormatSelector onSuccessUse();

    /**
     *
     * @return If <code>true</code> you should consider using one of the {@link #onAbortTry()} or {@link #onSuccessUse()} selectors, depending on what your most recent algorithm did.
     */
    public abstract boolean shouldSwitch();

}
