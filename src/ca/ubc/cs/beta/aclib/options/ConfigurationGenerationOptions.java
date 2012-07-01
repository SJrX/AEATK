package ca.ubc.cs.beta.aclib.options;

import java.io.File;

import ca.ubc.cs.beta.aclib.misc.jcommander.validator.FileNewAndWritableValidator;
import ca.ubc.cs.beta.aclib.misc.jcommander.validator.ReadableFileConverter;

import com.beust.jcommander.Parameter;

/**
 * Options object for Configuration Generator Utilities
 * @author sjr
 *
 */
public class ConfigurationGenerationOptions {
	
	@Parameter(names = {"-p", "--paramData"}, description = "File containing the parameter space", required=true, converter=ReadableFileConverter.class)
	public File parameterFile;
	
	@Parameter(names = {"-o","--outputFile"}, description = "File to output the resulting data to.", required=false, converter=FileNewAndWritableValidator.class)
	public File outputFile = new File("trajectory-file.txt");
	

	@Parameter(names = {"-n", "--numberOfConfigs"}, description = "Number of Configurations")
	public int numberOfConfigs = 100;
	

	@Parameter(names = {"--seed"}, description = "Seed used for PRNG [0 means don't use a Seed]")
	public long seed = 0;
	
}
