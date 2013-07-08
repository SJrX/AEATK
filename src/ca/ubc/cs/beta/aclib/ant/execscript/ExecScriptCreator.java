package ca.ubc.cs.beta.aclib.ant.execscript;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import ca.ubc.cs.beta.aclib.misc.jcommander.JCommanderHelper;
import ca.ubc.cs.beta.aclib.misc.returnvalues.ACLibReturnValues;

public class ExecScriptCreator {


public static void main(String[] args)
{
	ExecScriptCreatorOptions opts = new ExecScriptCreatorOptions();
	JCommander jcom = JCommanderHelper.getJCommanderAndCheckForHelp(args,opts);
	try {
		jcom.parse(args);
		
		if(!opts.skipClassCheck)
		{
			try {
				Class<?> foo = Class.forName(opts.clazz);
			} catch (ClassNotFoundException e) {
				throw new ParameterException("Error locating class: " + opts.clazz + " error: " + e.getClass().getCanonicalName() + ":" + e.getMessage() + "\n Maybe try using: --skip-class-check");
			}
		}
		
		
		File f = new File(opts.filename);
		
		if(f.isDirectory())
		{
			f = new File(opts.filename + File.separator + opts.nameOfProgram);
		} else if(!f.getParentFile().exists())
		{
			boolean created = f.getParentFile().mkdirs();
			
			if(!created)
			{
				throw new ParameterException("Could not create parent directory " + f.getParentFile());
			}
					
		}
		
		String script = getScript(opts.clazz,opts.nameOfProgram);
		
			
		System.out.println("***Script writing to: "  + f + " ***\n" + script + "****** ");
		
		FileWriter fWrite = new FileWriter(f);
		
		fWrite.write(script);
		fWrite.close();
	} catch(ParameterException e)
	{
		e.printStackTrace();
		System.exit(ACLibReturnValues.PARAMETER_EXCEPTION);
	} catch (IOException e) {
		e.printStackTrace();
		System.exit(ACLibReturnValues.OTHER_EXCEPTION);
	} 
	
	System.exit(ACLibReturnValues.SUCCESS);
	
	
	
	
}

public static String getScript(String javaClassName, String nameOfProgram)
{
	
	/*
#!/usr/bin/env bash
SMAC_MEMORY_INPUT=$SMAC_MEMORY
SMACMEM=1024
test "$SMAC_MEMORY_INPUT" -ge 1 2>&- && SMACMEM=$SMAC_MEMORY_INPUT
EXEC=ca.ubc.cs.beta.smac.executors.AutomaticConfigurator
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Starting with $SMACMEM MB of RAM"

for f in $DIR/*.jar
do
        jarconcat=$jarconcat:$f
done

jarconcat=${jarconcat:1}

exec java -Xmx"$SMACMEM"m -cp "$DIR/conf/:$jarconcat" $EXEC "$@"
*/

	StringBuilder sb = new StringBuilder();
	sb.append("#!/usr/bin/env bash").append("\n"); 
	sb.append("SMAC_MEMORY_INPUT=$SMAC_MEMORY").append("\n");
	sb.append("SMACMEM=1024").append("\n");
	sb.append("test \"$SMAC_MEMORY_INPUT\" -ge 1 2>&- && SMACMEM=$SMAC_MEMORY_INPUT").append("\n"); 
	sb.append("EXEC=").append(javaClassName).append("\n"); 
	sb.append("DIR=\"$( cd \"$( dirname \"${BASH_SOURCE[0]}\" )\" && pwd )\"").append("\n");
	
	sb.append("echo \"Starting ").append(nameOfProgram).append(" with $SMACMEM MB of RAM\"").append("\n"); 
	sb.append("\n");
	sb.append("for f in $DIR/*.jar").append("\n");
	sb.append("do").append("\n");
	        sb.append("\tjarconcat=$jarconcat:$f").append("\n");
	sb.append("done").append("\n");
	sb.append("jarconcat=${jarconcat:1}\n");
	sb.append("\n");
	sb.append("exec java -Xmx\"$SMACMEM\"m -cp \"$DIR/conf/:$jarconcat\" $EXEC \"$@\"").append("\n");
	
	return sb.toString();
}
}

