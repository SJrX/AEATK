package ca.ubc.cs.beta.smac.history;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

/**
 * Helper class that roughly allows you to associate with entries an index. Roughly this is a proto-List/Set that lets you get the index 
 * of an object in O(1) time. Additionally each element can appear only once in the list.
 * 
 * This class is not thread safe
 * 
 * @author seramage
 *
 * @param <V>
 */
public class KeyObjectManager<V> {

	BidiMap bidiMap;

	private int nextID;
	
	public KeyObjectManager()
	{
		this(1);
	}
	
	public KeyObjectManager(int firstID)
	{
		nextID = firstID;
		bidiMap = new DualHashBidiMap();
	}
	
	public void add(Integer id, V obj)
	{
		
		if(bidiMap.containsKey(id) || bidiMap.containsValue(obj))
		{
			if(bidiMap.containsKey(id))
			{
				System.out.println("bidiMap contains key " + id +  " already");
				System.out.println("Value : " + bidiMap.get(id));
			}
			
			if(bidiMap.containsValue(obj))
			{
				System.out.println("bidiMap contains value " + obj +  " already");
				System.out.println("Key: " + bidiMap.getKey(obj));
			}
			
			if(bidiMap.get(id).equals(obj)) 
			{
				return;
			} else
			{
				if(bidiMap.containsKey(id)) throw new IllegalArgumentException("Cannot replace index");
				if(bidiMap.containsValue(obj)) throw new IllegalArgumentException("Cannot replace value");
			}
		} 
		
		bidiMap.put(id, obj);
	}
	
	/**
	 * Appends a record to the KeyObjectManager
	 * NOTE: You cannot use this method and the add() method together, and get consistent results
	 * @param obj
	 * @return
	 */
	public int append(V obj)
	{
		add(nextID++, obj);
		return nextID-1;
	}
	
	@SuppressWarnings("unchecked")
	public V getValue(int key)
	{
		return (V) bidiMap.get(key);
	}
	
	public int getKey(V obj)
	{
		return (Integer) bidiMap.getKey(obj);
	}
	
	public int getOrCreateKey(V obj)
	{
		if(bidiMap.containsValue(obj))
		{
			return (Integer) bidiMap.getKey(obj);
		} else
		{
			return append(obj);
		}
	}

	public int size() {
	
		return bidiMap.size();
	}
}
