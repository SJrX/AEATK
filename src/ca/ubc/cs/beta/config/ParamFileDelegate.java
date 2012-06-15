package ca.ubc.cs.beta.config;

import com.beust.jcommander.Parameter;

public class ParamFileDelegate extends AbstractConfigToString{
	
	@Parameter(names={"-p", "--paramFile","--paramfile"}, description="File containing Parameter Space of Execution", required=true)
	public String paramFile;
	

}
