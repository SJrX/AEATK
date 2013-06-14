package ca.ubc.cs.beta.aclib.misc.debug;

import java.lang.reflect.Method;

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
	
	public static void main(String[] args)
	{
		System.out.println(getCurrentMethodName());
	}
	
	private DebugUtil()
	{
		
	}
}
