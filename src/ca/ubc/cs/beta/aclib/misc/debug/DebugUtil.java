package ca.ubc.cs.beta.aclib.misc.debug;

public final class DebugUtil {
	public static String getCurrentMethodName()
	{
		Exception e = new Exception();
		try {
			return e.getStackTrace()[1].getMethodName();
		} catch(RuntimeException e2)
		{
			return "Unknown Method";
		}
	}
	
	private DebugUtil()
	{
		
	}
}
