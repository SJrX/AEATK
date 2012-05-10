package ca.ubc.cs.beta.ac;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

public enum RunResult {
	
	TIMEOUT(0),
	SAT(1, "SAT","OK"),
	UNSAT(2),
	WRONG(3, "WRONG", "WRONG ANSWER"),
	CRASHED(-1),
	OK(1);
	
	private final int resultCode;
	private final Set<String> resultKey = new HashSet<String>();
	
	
	private RunResult(int resultCode)
	{
		this.resultCode = resultCode;
		this.resultKey.add(this.toString());
	}
	
	private RunResult(int resultCode, String... keys)
	{
		this.resultCode = resultCode;
		this.resultKey.addAll(Arrays.asList(keys));
	}
	
	
	public static RunResult getAutomaticConfiguratorResultForKey(String key)
	{
		/**
		 * Note this method could be faster if just built the map to begin with
		 */
		for(RunResult r : RunResult.values())
		{
			if(r.resultKey.contains(key))
			{
				return r;
			}
		}
		throw new IllegalArgumentException("No Match For Result from Automatic Configurator");
	}
	
	public static RunResult getAutomaticConfiguratorResultForCode(int resultCode)
	{
		/**
		 * Note this method could be faster if just built the map to begin with
		 */
		for(RunResult r : RunResult.values())
		{
			if(r.resultCode== resultCode)
			{
				return r;
			}
		}
		throw new IllegalArgumentException("No Match For Result from Automatic Configurator");
	}
	
	
	
	public int getResultCode()
	{
		return resultCode;
	}
	
	public boolean isSolved()
	{
		return this.resultCode == 1;
	}
	
	
}
