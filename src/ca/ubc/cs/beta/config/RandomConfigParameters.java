package ca.ubc.cs.beta.config;

import java.io.File;

import ca.ubc.cs.beta.models.surrogate.helpers.jcommander.validator.FileNewAndWritableValidator;
import ca.ubc.cs.beta.models.surrogate.helpers.jcommander.validator.ReadableFileConverter;

import com.beust.jcommander.Parameter;

public class RandomConfigParameters {
	
	@Parameter(names = {"-p", "--paramData"}, description = "File containing the parameter space", required=true, converter=ReadableFileConverter.class)
	public File parameterFile;
	
	@Parameter(names = {"-o","--outputFile"}, description = "File to output the resulting data to.", required=false, converter=FileNewAndWritableValidator.class)
	public File outputFile = new File("trajectory-file.txt");
	

	@Parameter(names = {"-n", "--numberOfConfigs"}, description = "Number of Configurations")
	public int numberOfConfigs = 100;
	

	@Parameter(names = {"--seed"}, description = "Seed used for PRNG [0 means don't use a Seed]")
	public long seed = 0;
	
}
