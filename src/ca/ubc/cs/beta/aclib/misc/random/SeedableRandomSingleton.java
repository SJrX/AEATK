package ca.ubc.cs.beta.aclib.misc.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import ec.util.MersenneTwister;

/**
 * Seedable Random Singleton
 *
 *	This class stores access to a random singleton in what is a giant anti-pattern. This will go away in the future.
 * 
 * <b>THIS CLASS IS NOT THREAD SAFE</b>
 * 
 * @author seramage
 * @deprecated
 */
public class SeedableRandomSingleton  {

	
	/**
	 * 
	 */
	private static Long seed;
	
	private static boolean init = false;
	private static Random rand = null;
	
	private static final boolean log = false;
	private static final boolean mersenne = true;
	private SeedableRandomSingleton()
	{
		//This class cannot be instantiated.
		throw new IllegalStateException();
	}
	
	public static void setSeed(long seed)
	{
		if (!init)
		{
			SeedableRandomSingleton.seed = seed;
			init();
		} else
		{
			//throw new IllegalStateException();
		}
	}
	
	public static void setRandom(Random r)
	{
		rand = r;
	}
	
	public static long getSeed()
	{
		return seed;
	}
	
	
	
	public static synchronized void reinit()
	{
		System.out.println("[INFO]: Reinitializing seed");
		if(!init)
		{
			init();
		}
		
		if(mersenne)
		{
			System.out.println("[INFO]: Mersenne Twister PRNG Enabled");
			rand = new MersenneTwister(seed);
		} else
		{
			System.out.println("[INFO]: Java PRNG Enabled");
			rand = new Random(seed);
		}
		
		if(log)
		{
			System.out.println("[INFO]: Random Logging Enabled");
			rand = new RandomLogger(rand);
		} else
		{
			System.out.println("[INFO]: Random Logging Disabled");
		}
		
	
	}
	
	private static synchronized void init()
	{
		if(init)
		{
			throw new IllegalStateException();
		}
		
		init = true; 
		
		if (seed == null)
		{
			seed = System.currentTimeMillis();
			System.out.println("[INFO]: Seed chosen randomly to:" + seed);
		} else
		{
			System.out.println("[INFO]: Seed set via CLI to:" + seed);
		}
		
		if(mersenne )
		{
			System.out.println("[INFO]: Mersenne Twister PRNG Enabled");
			rand = new MersenneTwister(seed);
		} else
		{
			System.out.println("[INFO]: Java PRNG Enabled");
			rand = new Random(seed);
		}
		
		if(log)
		{
			System.out.println("[INFO]: Random Logging Enabled");
			rand = new RandomLogger(rand);
		} else
		{
			System.out.println("[INFO]: Random Logging Disabled");
		}
		
		
		
		
	}
	
	
	public static Random getRandom()
	{

		
		if (rand == null)
		{
			init();
		}
		
		return rand;	
		
	}
	
	public static int[] getPermutation(int n, int offset)
	{
		if (n < 0)
		{
			throw new IllegalArgumentException();
		}
		
		int[] perm = new int[n];
		
		for(int i=0; i < n; i++)
		{
			perm[i] = i + offset;
		}
		
		for(int i=n-1; i > 0 ; i--)
		{
			int rndIdx = rand.nextInt(i+1);
			int tmp = perm[i];
			perm[i] = perm[rndIdx];
			perm[rndIdx] = tmp;
		}
		
		return perm;
	}
	
	
	
	public static void permuteList(List list, int[] permutations)
	{
		if(permutations.length != list.size())
		{
			throw new IllegalArgumentException("List length and permutation length are not equal");
		}
		List<Object> tmpList = new ArrayList<Object>(permutations.length);
		for(int i=0; i < permutations.length; i++ )
		{
			tmpList.add(list.get(permutations[i]));
		}
		list.clear();
		list.addAll( tmpList);
	}
		
		
		
		
}


