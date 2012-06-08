package ca.ubc.cs.beta;

import java.io.File;
import java.net.URL;

public final class TestHelper {

	private TestHelper()
	{
		
	}
	
	public static File getTestFile(String s)
	{
		URL url = TestHelper.class.getClassLoader().getResource(s);
		
		File file = new File(url.getPath());
		return file;
	}
	
}
