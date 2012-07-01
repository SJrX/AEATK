package ca.ubc.cs.beta.aclib.runhistory;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

/**
 * Helper class that roughly allows you to associate with entries an index. This is a proto-List/Set that lets you get the index 
 * of an object in O(1) time. Additionally each element can appear only once in the list.
 * 
 * This class is not thread safe
 * 
 * @author seramage
 *
 * @param <V> object to associate the id with
 */
public class KeyObjectManager<V> {

	/**
	 * Bidirectional / Bijective Map (enforces unique values, and allows O(1) lookup of key from Value)
	 */
	private final BidiMap bidiMap;

	private int nextID;
	
	/**
	 * Default Constructor 
	 */
	public KeyObjectManager()
	{
		this(1);
	}
	
	/**
	 * Constructor
	 * @param firstID firstID to assign to objects
	 */
	public KeyObjectManager(int firstID)
	{
		nextID = firstID;
		bidiMap = new DualHashBidiMap();
	}
	
	/**
	 * Add the object to Collection with the specified id
	 * @param id 	id to associate with object
	 * @param obj	object to store
	 */
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
	 * Appends an object to the map
	 * <b>NOTE:</b> You cannot use this method and the add() method together, and get consistent results
	 * @param obj	object to add
	 * @return	the id associated with the object
	 */
	public int append(V obj)
	{
		add(nextID++, obj);
		return nextID-1;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Gets the value from the collection
	 * @param key
	 * @return object associated with the key
	 */
	public V getValue(int key)
	{
		return (V) bidiMap.get(key);
	}
	
	/**
	 * Gets the key for a given object
	 * @param obj object to find key of
	 * @return	key
	 */
	public int getKey(V obj)
	{
		return (Integer) bidiMap.getKey(obj);
	}
	
	/**
	 * Get or create the key if it doesn't already exist
	 * @param obj	object to store
	 * @return	key
	 */
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

	/**
	 * Size of the collection
	 * @return number of entities we have mapped
	 */
	public int size() {
		return bidiMap.size();
	}
}
