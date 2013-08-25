package ca.ubc.cs.beta.aclib.example.verifyscenario;

import ca.ubc.cs.beta.aclib.misc.options.NoArgumentHandler;

public class VerifyScenarioNoArgumentHandler implements NoArgumentHandler {

	@Override
	public boolean handleNoArguments() {
		System.out.println("Verify Scenario Utility");
		System.out.println("\n\n\tUsage:\n");
		System.out.println("\t\tverify-scenario --scenarios <scenario1> <scenario2> <scenario3> ....");
		System.out.println("Skip instance check on disk");
		System.out.println("\t\tverify-instances false --scenarios <scenario1> <scenario2>");
		return true;
	}

	

}
