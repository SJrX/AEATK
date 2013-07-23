package ca.ubc.cs.beta.aclib.ant.execscript;

import java.io.File;

import com.beust.jcommander.Parameter;

import ca.ubc.cs.beta.aclib.options.AbstractOptions;

public class ExecScriptCreatorOptions extends AbstractOptions {

	@Parameter(names="--class", description="Name of java class", required=true)
	public String clazz;
	
	@Parameter(names="--skip-class-check", description="Don't actually check if the class exists")
	public boolean skipClassCheck;
	
	@Parameter(names="--name", description="Name of program", required=true)
	public String nameOfProgram;

	@Parameter(names="--file-to-write", description="File to output script to (if directory will use name of program as a name)")
	public String filename = (new File("")).getAbsolutePath();
	
	@Parameter(names="--bat-file", description="Also output a windows .bat file")
	public boolean batFile = true;
	
}
