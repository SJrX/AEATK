package ca.ubc.cs.beta.aclib.trajectoryfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.beust.jcommander.Parameter;

import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;

@UsageTextField(hiddenSection = true)
public class TrajectoryFileOptions extends AbstractOptions{

	@Parameter(names={"--trajectory-files","--trajectory-file","--trajectoryFile"}, description="Trajectory file(s) to read configurations from", variableArity = true)
	public List<String> trajectoryFiles = new ArrayList<String>();
	
	
	@Parameter(names={"--trajectory-use-tunertime-if-no-walltime","--useTunerTimeIfNoWallTime"}, description="Use the tuner time as walltime if there is no walltime in the trajectory file")
	public boolean useTunerTimeIfNoWallTime;

	/**
	 * 
	 * @deprecated returns the first and only the first trajectory file specified
	 */
	public List<TrajectoryFileEntry> parseTrajectoryFile(ParamConfigurationSpace configSpace) throws FileNotFoundException, IOException {
		
		if(trajectoryFiles.size() > 0)
		{
			return TrajectoryFileParser.parseTrajectoryFileAsList(new File(trajectoryFiles.get(0)), configSpace, useTunerTimeIfNoWallTime);
		} else
		{
			throw new IllegalArgumentException("No trajectory file specified (precondition of calling this");
		}
	}
	
	
	public Set<TrajectoryFile> parseTrajectoryFiles(ParamConfigurationSpace configSpace) throws FileNotFoundException, IOException 
	{
		Set<TrajectoryFile> tfes = new TreeSet<TrajectoryFile>();
		//Tries to auto detect the number of the run from the file name, otherwise it starts at 1000000 and finds new runs
		for(String trajFile : trajectoryFiles)
		{
			tfes.add(new TrajectoryFile(new File(trajFile),TrajectoryFileParser.parseTrajectoryFileAsList(new File(trajFile), configSpace, useTunerTimeIfNoWallTime)));
		}
		
		return tfes;
	}
	
	
	
	
}
