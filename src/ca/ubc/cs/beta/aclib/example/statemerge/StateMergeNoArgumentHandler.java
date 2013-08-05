package ca.ubc.cs.beta.aclib.example.statemerge;

import ca.ubc.cs.beta.aclib.misc.options.NoArgumentHandler;

public class StateMergeNoArgumentHandler implements NoArgumentHandler {

	@Override
	public boolean handleNoArguments() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("state-merge is a utility that allows mergeing of state files.  ").append("\n\n");

		sb.append("  Basic Usage:\n");
		
		sb.append("--directories <dirToScan> --scenario-file <scenfile> --outdir <outdir>\n");
		sb.append("\n\tNote:\n");
		sb.append("\t\t<dirToScan> will be searched for run and result files if none are found it will recursively look in subdirectories until some are found\n");
		sb.append("\t\t<scenfile> scenario file to use\n");
		sb.append("\t\t<outdir> output directory to write the files to\n");
		sb.append("\n\nMore help is available with:\n");
		sb.append("  state-merge --help\n\n");
			  
	
		System.out.println(sb);
		return true;
	}

}
