package ca.ubc.cs.beta.smac.helper;

/**
 * Helper class that allows associating a value to another one temporarily (useful for return values)
 * @author seramage
 *
 * @param <T>
 * @param <V>
 */
public class AssociatedValue<T extends Comparable<T>,V> implements Comparable<AssociatedValue<T,?>>{
	
	private final T Tobj;
	private final V Vobj;
	public AssociatedValue(T t, V v)
	{
		Tobj = t;
		Vobj = v;
	}
	
	public V getValue()
	{
		return Vobj;
	}
	
	public T getAssociatedValue()
	{
		return Tobj;
	}

	@Override
	public int compareTo(AssociatedValue<T, ?> o) {
		return Tobj.compareTo(o.Tobj);
	}
	
	public String toString()
	{
		return Tobj.toString() + " => " + Vobj.toString();
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof AssociatedValue<?,?>)
		{
			AssociatedValue<?,?> oValue = (AssociatedValue<?, ?>) o;
			return Tobj.equals(oValue.Tobj);
		} else
		{
			return false;
		}
	}



}
