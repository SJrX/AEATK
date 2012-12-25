package ca.ubc.cs.beta.targetalgorithmevaluator.ldlibrarypathfix;

public class CLICallee {

	
	
			
	public static void main(String[] args)
	{
		String seed = (args.length >= 4) ? args[4] : "0";
		
		System.out.println("Result for ParamILS: SAT, 0.000, 0, 0," + seed + ",LD_LIBRARY_PATH=" + System.getenv().get("LD_LIBRARY_PATH") +   " \n");
		
	}
}
