package ca.ubc.cs.beta.aclib.concurrent;

import java.util.concurrent.Semaphore;

/**
 * A standard semaphore that exposes the reducePermits() method so that resources can be
 * taken
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class ReducableSemaphore extends Semaphore
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1594134901120931617L;

	public ReducableSemaphore(int permits) {
		super(permits);

	}
	
	public ReducableSemaphore(int permits, boolean fair) {
		super(permits, fair);

	}
	
	public void reducePermits(int reduction)
	{
		super.reducePermits(reduction);
	}
	

}
