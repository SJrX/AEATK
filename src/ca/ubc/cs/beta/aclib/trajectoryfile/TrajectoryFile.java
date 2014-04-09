package ca.ubc.cs.beta.aclib.trajectoryfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;

/**
 * Provides a representation of a trajectory file
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
@Immutable
public class TrajectoryFile implements Comparable<TrajectoryFile>{

	private final File location;
	
	private final List<TrajectoryFileEntry> tfes;
	
	public TrajectoryFile(File location, List<TrajectoryFileEntry> tfes)
	{
		this.location = location;
		this.tfes = Collections.unmodifiableList(new ArrayList<TrajectoryFileEntry>(tfes));
	}
	
	public File getLocation()
	{
		return location;
	}
	
	public List<TrajectoryFileEntry> getTrajectoryFileEntries()
	{
		return tfes;
	}
	
	public int hashCode()
	{
		return location.hashCode();
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof TrajectoryFile)
		{
			return ((TrajectoryFile) o).location.equals(location);
		} else
		{
			return false;
		}
	}

	@Override
	public int compareTo(TrajectoryFile o) {

		return location.compareTo(o.location);
	}

}

