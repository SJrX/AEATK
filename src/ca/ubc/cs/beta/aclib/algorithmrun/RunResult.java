package ca.ubc.cs.beta.aclib.algorithmrun;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

/**
 * Enumeration that represents all possible legal values of an AlgorithmRun
 * <p>
 * <b>Note:</b> All aliases should be specified in upper case in the enum 
 * declaration.
 * 
 * @see ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun
 *
 */
public enum RunResult {
	
	/**
	 * Signifies that the algorithm ran out of time and had to be cutoff
	 */
	TIMEOUT(0),
	
	/**
	 * Signifies that the algorithm completed successfully (and optionally found the result was SATISFIABLE) 
	 */
	SAT(1, "SAT","SATISFIABLE"),
	
	/**
	 * Signifies that the algorithm completed successfully, and that the target algorithm result was UNSATISFIABLE.
	 * <p>
	 * <b>NOTE:</b> SAT & UNSAT are hold overs from SAT solvers, neither of these are error conditions. If you are not running a SAT solver
	 * you should almost certainly report SAT when completed. 
	 */
	UNSAT(2, "UNSAT", "UNSATISFIABLE"),
	
	
	/**
	 * Signifies that the algorithm did not complete and unexpectedly crashed or failed.
	 */
	CRASHED(-1),

	
	/**
	 * Signifies not only that the algorithm failed unexpectedly but that it's probable that subsequent attempts are most likely going to fail also
	 * and that we should simply not continue with any attempts 
	 *
	 */
	ABORT(-2);
	
	/**
	 * Stores the numeric result code used in some serializations of run results
	 * @see ca.ubc.cs.beta.aclib.state.legacy.LegacyStateFactory
	 * @see ca.ubc.cs.beta.aclib.algorithmrun.ExistingAlgorithmRun
	 */
	private final int resultCode;
	
	/**
	 * Maps known synonyms of a RunResult for lookup by String
	 */
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
	
	/**
	 * Converts a String into a RunResult
	 * 
	 * @param key 			string to convert into a runresult
	 * @return 				runresult that represents the string
	 * @throws 				IllegalArgumentException when the string does not match any known runresult
	 */
	public static RunResult getAutomaticConfiguratorResultForKey(String key)
	{
		/*
		 * Note this method could be faster if just built the map to begin with
		 */
		key = key.toUpperCase();
		for(RunResult r : RunResult.values())
		{
			if(r.resultKey.contains(key))
			{
				return r;
			}
		}
		throw new IllegalArgumentException("No Match For Result from Automatic Configurator: " + key);
	}
	
	/**
	 * Converts a result code into a runresult
	 * 
	 * @param resultCode 		integer to be mapped back into a RunResult
	 * @return runresult	 	corresponding to the the resultcode
	 * @throws					IllegalArgumentException when the integer does not match any known runresult 			
	 */
	public static RunResult getAutomaticConfiguratorResultForCode(int resultCode)
	{
		/*
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
	
	/**
	 * 
	 * @return result code for this runresult 
	 */
	public int getResultCode()
	{
		return resultCode;
	}
	
	/**
	 * Returns a boolean determining whether this run is Successful (see corresponding link for more information as to what this is for)
	 * 
	 * @see ca.ubc.cs.beta.aclib.state.legacy.LegacyStateFactory
	 * @return <code>true</code> if and only if this is a successful run, false otherwise
	 */
	public boolean isSolved()
	{
		return this.resultCode == 1;
	}

	/**
	 * Returns the aliases for this Run Result
	 * @return a set containing all equivilant aliases for this result
	 */
	public Set<String> getAliases() {
		return Collections.unmodifiableSet(this.resultKey);
	}
	
	
}
