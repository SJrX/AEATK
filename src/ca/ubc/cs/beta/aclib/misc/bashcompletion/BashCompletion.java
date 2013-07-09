package ca.ubc.cs.beta.aclib.misc.bashcompletion;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.beust.jcommander.JCommander;

import ca.ubc.cs.beta.aclib.misc.options.UsageSection;
import ca.ubc.cs.beta.aclib.options.docgen.UsageSectionGenerator;

/**
 * Generates bash autocompletion script for options
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class BashCompletion {
	
	public static void main(String[] args) throws Exception
	{
		try {
			BashCompletionOptions opts = new BashCompletionOptions();
			JCommander jcom = new JCommander(opts,true, true);
			jcom.parse(args);
			
			
			Object obj = Class.forName(opts.clazz).newInstance();
		
			List<UsageSection> sections = UsageSectionGenerator.getUsageSections(obj);
			
			
			String completionScript = bash(sections, opts.commandName);
			
			FileWriter fw = new FileWriter(new File(opts.outputFile),true);
			
			fw.write(completionScript);
			fw.flush();
			fw.close();
			System.out.println("Bash completion script for " + opts.commandName + " written to: " + (new File(opts.outputFile)).getAbsolutePath() + "");
			System.exit(0);
		} catch(Throwable t)
		{
			System.err.println("Couldn't generate bash completion script");
			t.printStackTrace();
			
			System.exit(1);
		}
		
	}
	
	public static String bash(List<UsageSection> sections, String commandName)
	{
		StringWriter s = new StringWriter();
		PrintWriter pw = new PrintWriter(s);
		
		
		
		
		
		
		
		SortedSet<String> sorted = new TreeSet<String>();
		
		for(UsageSection sec : sections)
		{
			for(String attr : sec)
			{
				//pw.append(attr);
				sorted.addAll(Arrays.asList(sec.getAttributeAliases(attr).replaceAll(","," ").split(" ")));
			}
		}
		

		for(String key : sorted)
		{
			if(key.trim().startsWith("--"))
			{
				pw.append(key);
				pw.append(" ");
			}
			
		}
		
		
		String bashScriptPrefix = "#Taken from http://www.debian-administration.org/article/An_introduction_to_bash_completion_part_2\n"+
		"_"+commandName+"()\n" +  
		"{\n" +
		"   local cur prev opts\n"+
		"    COMPREPLY=()\n"+
		"    cur=\"${COMP_WORDS[COMP_CWORD]}\"\n"+
		"    prev=\"${COMP_WORDS[COMP_CWORD-1]}\"\n"+
		"    opts=\""+s.toString()+"\"\n"+
		"\n"+
		"    if [[ ${cur} == -* ]] ; then\n"+
		"        COMPREPLY=( $(compgen -W \"${opts}\" -- ${cur}) )\n"+
		"        return 0\n"+
		"    fi\n"+
		"}\n"+
		"complete -F _"+commandName+" "+commandName + "\n\n";
		
		return bashScriptPrefix;
	}
	

}
