package ca.ubc.cs.beta.aclib.misc.watch;

/**
 * {@link StopWatch} that is already started
 * 
 * @author sjr
 *
 */
public class AutoStartStopWatch extends StopWatch {
	/**
	 * Creates the StopWatch
	 */
	public AutoStartStopWatch()
	{
		super();
		start();
	}

}
