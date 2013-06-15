package ca.ubc.cs.beta.aclib.options;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import ca.ubc.cs.beta.aclib.misc.jcommander.validator.NonNegativeInteger;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.random.SeedableRandomPool;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

@UsageTextField(hiddenSection=true)
public class SeedOptions extends AbstractOptions{

	@Parameter(names="--seedOffset", description="offset of numRun to use from seed (this plus --numRun should be less than INTEGER_MAX)")
	public int seedOffset = 0 ;
	
	@Parameter(names={"--numRun","--seed"}, required=true, description="number of this run (and seed)", validateWith=NonNegativeInteger.class)
	public int numRun = 0;
	
	@DynamicParameter(names="-S", description="Sets specific seeds (by name) in the random pool")
	public Map<String, String> initialSeedMap = new TreeMap<String, String>();
	
	
	public SeedableRandomPool getSeedableRandomPool()
	{
		
		Map<String, Integer> initSeeds = new HashMap<String, Integer>();
		for(Entry<String, String> ent : initialSeedMap.entrySet())
		{
		
			try {
				initSeeds.put(ent.getKey(),Integer.valueOf(ent.getValue()));
			} catch (NumberFormatException e)
			{
				throw new ParameterException("All Random Pool Seeds must be integer, key: " + ent.getKey() + " value: " + ent.getValue() + " doesn't seem to be one. ");
			}
			
			
		}
		
		return new SeedableRandomPool(numRun + seedOffset, initSeeds);
				
		
	}
}
