package ca.ubc.cs.beta.aclib.smac;

import ca.ubc.cs.beta.aclib.misc.options.NoArgumentHandler;

public class SMACNoArgHandler implements NoArgumentHandler{
	
	@Override
	public boolean handleNoArguments() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("SMAC (http://www.cs.ubc.ca/labs/beta/Projects/SMAC/) is an automatic configurator that allows users to automatically tune algorithm configuration spaces.").append("\n\n");

		sb.append("  Basic Usage:\n");
		sb.append("  smac --scenarioFile <file> --seed 1\n\n");
	
		sb.append("  Skipping Validation:\n");
		sb.append("  smac --scenarioFile <file> --seed 1  --doValidation false\n\n");

		sb.append("  Full version information is available with :\n");
		sb.append("  smac -v\n\n");
		
		sb.append("  A full command line reference is available with:\n");
		sb.append("  smac --help\n\n");
			  
		sb.append("Please check the documentation to directory (doc/) for a manual.pdf, and the quickstart.pdf which will show you how to get up and running right away.");
		sb.append("\n\n  See also:\n\n");
			  
		sb.append("  algo-test - for testing the wrapper\n");
		sb.append("  smac-validate - for stand alone validation\n");

		System.out.println(sb.toString());
		return true;
	}

}
