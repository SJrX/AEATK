package ca.ubc.cs.beta.random;

import java.util.Random;

public class RandomLogger extends Random {

	
	private final Random realRandom;
	/**
	 * 
	 */
	private static final long serialVersionUID = 3102832250337012408L;
	
	public RandomLogger(Random r)
	{
		if(r == null)
		{
			throw new IllegalArgumentException();
		}
		this.realRandom = r;
	}
	
	protected int next(int bits)
	{
		throw new UnsupportedOperationException();
	}
	
	public boolean nextBoolean()
	{
		throw new UnsupportedOperationException();
	}
    
	public void	nextBytes(byte[] bytes)
	{
		throw new UnsupportedOperationException();
	}
    

	public double nextDouble()
	{
		throw new UnsupportedOperationException();
	}

	public float nextFloat()
	{
		throw new UnsupportedOperationException();
	}

	public double nextGaussian()
	{
		throw new UnsupportedOperationException();
	}

	public int nextInt()
	{
		int myInt = realRandom.nextInt();
		System.out.println("[RANDOM]: int requested and giving back " + myInt);
		return myInt;
	}

	public int nextInt(int n)
	{
	
		int myInt = realRandom.nextInt(n);
		System.out.println("[RANDOM]: int requested in interval (0," + n +") giving back:" + myInt);
		return myInt;
	}

	public long nextLong()
	{
		long myLong = realRandom.nextLong(); 
		System.out.println("[RANDOM]: long requested and giving back: " + myLong);
		return myLong;
	}

	public void setSeed(long seed)
	{
		if(realRandom == null)
		{
			//Called from constructor, we won't do anything.
		} else
		{
		realRandom.setSeed(seed);
		System.out.println("[RANDOM]: Seed reset to " + seed);
		}
	}

}
