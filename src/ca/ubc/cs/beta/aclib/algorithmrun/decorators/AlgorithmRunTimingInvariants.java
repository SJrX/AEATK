package ca.ubc.cs.beta.aclib.algorithmrun.decorators;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;

/**
 * Corrects output from misbehaiving wrappers
 * 
 * Specifically it ensures that:
 * 
 * SAT results are always less than the captime.
 * 
 * @author seramage
 *
 */
public class AlgorithmRunTimingInvariants extends AbstractAlgorithmRunDecorator {

	public AlgorithmRunTimingInvariants(AlgorithmRun run) {
		super(run);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4108923561335725916L;
	
	
	
	

	
}
