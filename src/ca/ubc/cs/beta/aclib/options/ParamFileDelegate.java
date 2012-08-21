package ca.ubc.cs.beta.aclib.options;

import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;

import com.beust.jcommander.Parameter;

/**
 * Delegate for ParamConfigurationSpace objects
 * 
 */
@UsageTextField(hiddenSection = true)
public class ParamFileDelegate extends AbstractOptions{
	
	@Parameter(names={"-p", "--paramFile","--paramfile"}, description="File containing Parameter Space of Execution", required=true)
	public String paramFile;

}
