package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.prepostcommand;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

import ca.ubc.cs.beta.aclib.misc.jcommander.validator.ReadableDirectoryValidator;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;

@UsageTextField(hiddenSection = true)
public class PrePostCommandOptions extends AbstractOptions {

	@Parameter(names={"--preScenarioCommand","--pre_cmd"}, description="Command that will run on startup")
	public String preCommand;
	
	@Parameter(names={"--postScenarioCommand","--post_cmd"}, description="Command that will run on shutdown")
	public String postCommand;
	
	@Parameter(names="--exceptionOnPrePostCommand", description="Throw an abort ")
	public boolean exceptionOnError=false;
	
	@Parameter(names="--logOutput", description="Log all the output from the pre and post commands")
	public boolean logOutput = true;

	@UsageTextField(defaultValues="Current Working Directory")
	@Parameter(names="--prePostExecDir", description="Execution Directory for Pre/Post commands", converter=ReadableDirectoryValidator.class)
	public File directory = new File(".");
	
}
