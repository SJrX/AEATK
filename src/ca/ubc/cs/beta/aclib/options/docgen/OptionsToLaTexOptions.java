package ca.ubc.cs.beta.aclib.options.docgen;

import com.beust.jcommander.Parameter;

public class OptionsToLaTexOptions {
	
	@Parameter(names="--class", required = true, description="Class to generate auto configuration for")
	public String clazz;
	
	@Parameter(names="--file-to-write", required = true, description="Output File to append the bash completion to")
	public String outputFile;

	@Parameter(names="--show-tae-options", description="If true show the TAE options as well")
	public boolean tae = true;

}
