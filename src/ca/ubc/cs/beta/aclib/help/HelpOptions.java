package ca.ubc.cs.beta.aclib.help;

import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;

import com.beust.jcommander.Parameter;
@UsageTextField(hiddenSection=true)
/***
 * Options that present help to the user
 * <br/>
 * <b>Implementation Note:</b> Nothing every actually will check these values. This
 * help options objects really only sets the parameter names. To get this behaviour to fully work
 * you should pass the arguments through {@see ca.ubc.cs.beta.aclib.misc.jcommander.JCommanderHelper#checkForHelpAndVersion}
 * <br/>
 * You might ask why we have these options then, it is so that they are displayed.
 * 
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 */
public class HelpOptions extends AbstractOptions{
	
	/**
	 * Note most of these actually will never be read as we will silently scan for them in the input arguments to avoid logging
	 */
	@UsageTextField(defaultValues="", domain="")
	@Parameter(names={"--show-hidden","--showHiddenParameters"}, description="show hidden parameters that no one has use for, and probably just break SMAC (no-arguments)")
	public boolean showHiddenParameters = false;
	
	@UsageTextField(defaultValues="", domain="" )
	@Parameter(names={"--help","-?","/?","-h"}, description="show help")
	public boolean showHelp = false;
	
	@UsageTextField(defaultValues="", domain="")
	@Parameter(names={"-v","--version"}, description="print version and exit")
	public boolean showVersion = false;

	

}
