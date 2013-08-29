package ca.ubc.cs.beta.targetalgorithmevaluator;

public class ReturnCodeTester {

	public static void main(String[] args)
	{
		System.err.println("Started");

		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		
		System.out.println("Command completed");
		System.exit(Integer.valueOf(args[0]));
	}
}
