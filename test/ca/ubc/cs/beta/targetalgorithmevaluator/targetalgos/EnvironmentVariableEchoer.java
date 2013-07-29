package ca.ubc.cs.beta.targetalgorithmevaluator.targetalgos;

import java.util.Map.Entry;

public class EnvironmentVariableEchoer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		for(Entry<String, String> ent : System.getenv().entrySet())
		{
			System.out.println(ent.getKey() + "=" + ent.getValue());
		}
		
		System.out.println("Result for ParamILS: SAT, 0,0,0,1");
	}

}
