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
		
		File file = new File(url.getPath()).getAbsoluteFile();
		return file;
	}
	
	
	public static String getJavaExecString()
	{
		
		
		StringBuilder b = new StringBuilder();
		b.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java -cp ");
		b.append(System.getProperty("java.class.path"));
		b.append(" ");
		
		return b.toString();
	}
	
}
