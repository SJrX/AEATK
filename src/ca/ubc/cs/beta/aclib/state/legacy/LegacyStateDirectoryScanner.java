package ca.ubc.cs.beta.aclib.state.legacy;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility application that scans a directory and determines what iterations we can restore
 * @author sjr
 *
 */
public class LegacyStateDirectoryScanner {

	public static void main(String[] args)
	{
		if(args.length != 1)
		{
			System.out.println("Shows what iterations are restorable for a given directory");
			System.out.println("Usage java " + LegacyStateDirectoryScanner.class.getCanonicalName() + " <directory to scan>" );
			return;
		}
		String path = args[0];
		
		File restoreDirectory = new File(path);
		
		Set<String> filenames = new HashSet<String>();
		
		filenames.addAll(Arrays.asList(restoreDirectory.list()));
		
		System.out.println("Scanning directory " + path);
		File[] files = restoreDirectory.listFiles();
		Arrays.sort(files);
		for(File f : files)
		{
			int iteration = LegacyStateFactory.readIterationFromObjectFile(f);
			if(iteration != -1)
			{
				System.out.println(f.getAbsolutePath().substring(restoreDirectory.getAbsolutePath().length()+1) + " => Iteration " + iteration);
			}
		}
		
		return;
	}
}
