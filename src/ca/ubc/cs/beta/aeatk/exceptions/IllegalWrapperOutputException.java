package ca.ubc.cs.beta.aeatk.exceptions;

import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResultKeyComparator;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class IllegalWrapperOutputException extends IllegalArgumentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8887570449032849445L;

	/**
	 *
	 * @param error 		 error with the result line
	 * @param resultLine	 result line text
	 */
	public IllegalWrapperOutputException(String error, String resultLine)
	{
		super("Illegal Wrapper Output Detected: " + error + " on result line: " + resultLine + " please consult the manual for more information");
	}

	/**
	 *
	 * @param error 	error that occured
	 * @param map		map of parameters from the wrapper
	 */
	public IllegalWrapperOutputException(String error, Map<String, ?> map)
	{
		//TreeMap to sort by keys.
		super("Illegal Wrapper Output Detected: " + error + " with result values:" + (new TreeMap<>(map))+ " please consult the manual for more information" );
	}

    class MyMap<K extends String,V> extends TreeMap<K,V>
    {
        public MyMap(Map<K, V> myMap)
        {
            super( AlgorithmRunResultKeyComparator.getInstance());
            this.putAll(myMap);
        }
    }
}
