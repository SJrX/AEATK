package ca.ubc.cs.beta.models.surrogate.helpers.csv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.ParameterException;

public class ConfigCSVFileHelper implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9193977861142611700L;
	
	
	private final List<String[]> data;
	private final Map<String, Integer> keyIndexMap =  new HashMap<String, Integer>();
	
	@Deprecated
	private final boolean header;
	private final int dataOffsetFromTop;
	private final int dataOffsetFromLeft;
	
	
	
	
	public ConfigCSVFileHelper(List<String[]> data)
	{
		this(data, true);
	}
	
	
	public ConfigCSVFileHelper(List<String[]> data, boolean header)
	{
		this(data,(header) ? 1 : 0, 0);
		
	}
	
	public ConfigCSVFileHelper(List<String[]> data, int dataOffsetFromTop, int dataOffsetFromLeft)
	{
		//Trim all inputs
		for(String[] arr : data)
		{
			for(int i=0; i < arr.length; i++)
			{
				arr[i] = arr[i].trim();
			}
		}
		
		this.data = data;
		this.dataOffsetFromTop = dataOffsetFromTop;
		this.dataOffsetFromLeft = dataOffsetFromLeft;
		
		
		this.header = (dataOffsetFromTop == 1);
		if (data.size() == 0)
		{
			throw new ParameterException("Parameter File empty");
		}
	
		if(this.header)
		{
			String[] keys = data.get(0);
			for(int i=0; i < keys.length; i++)
			{
				keyIndexMap.put(keys[i], i);
			}
			//dataOffsetFromTop = 1;
		} else
		{
			//dataOffsetFromTop = 0;
		}
	
		
	}
	
	public int getIntegerRawValue(int row,int column)
	{
		if (data.get(row) == null)
		{
			throw new IllegalArgumentException("Invalid Row");
		}
		
		return Integer.valueOf(data.get(row)[column]);
	}
	
	
	
	/**
	 * Returns row of CSV file for key.
	 * @param key
	 * @param row
	 * @return
	 */
	public int getIntegerValue( int row,String key)
	{
		
		
		if (keyIndexMap.get(key) == null)
		{
			throw new IllegalArgumentException("Invalid Key");
		}
		
		return getIntegerRawValue(row, keyIndexMap.get(key));
	}
	
	
	public double getDoubleRawValue(int row, int column)
	{
		
		if (data.get(row) == null)
		{
			throw new IllegalArgumentException("Invalid Row");
		}
		
		return Double.valueOf(data.get(row)[column]);
		
	}
	
	
	
	/**
	 * Returns row of CSV file for key.
	 * @param key
	 * @param row - data is indexed at 1 (Due to implementation 0 will return the key).
	 * @return
	 */
	public double getDoubleValue(int row, String key)
	{
		
		if (keyIndexMap.get(key) == null)
		{
			throw new IllegalArgumentException("Invalid Key");
		}
		
		return getDoubleRawValue( row, keyIndexMap.get(key));
	}
	
	
	
	public String getStringValue(int row, String key)
	{
		if (keyIndexMap.get(key) == null)
		{
			throw new IllegalArgumentException("Invalid Key");
		}
		return getStringRawValue(row,keyIndexMap.get(key));
	}
	
	public String getStringRawValue(int row, int column)
	{
		if (data.get(row) == null)
		{
			throw new IllegalArgumentException("Invalid Row");
		}
	
		return data.get(row)[column].trim(); 
		
		
	}
	
	
	public String getStringDataValue(int row, int column)
	{
		return getStringRawValue(row + dataOffsetFromTop, column + dataOffsetFromLeft);
	}
	
	public String getStringDataValue(int row, String key)
	{
		int column = keyIndexMap.get(key);
		return getStringRawValue(row + dataOffsetFromTop, column);
	}
	
	public Integer getIntegerDataValue(int row, int column)
	{
		return getIntegerRawValue(row + dataOffsetFromTop, column + dataOffsetFromLeft);
	}
	
	public Double getDoubleDataValue(int row, int column)
	{
		return getDoubleRawValue(row + dataOffsetFromTop, column + dataOffsetFromLeft);
	}
	
	
	
	
	public int getIndexOfFirstData()
	{
		return dataOffsetFromTop;
	}
	
	
	public int getNumberOfDataRows()
	{
		return data.size() - dataOffsetFromTop;
	}
	
	
	public int getNumberOfDataColumns()
	{
		return data.get(0).length - dataOffsetFromLeft;
	}
	
	/**
	 * Returns the string values for a certain column (starting from index 1). 
	 * @param column - Column index to return
	 * @return
	 */
	private List<String> getRawColumn(int column)
	{
		List<String> results = new LinkedList<String>();
	
		for(int i=dataOffsetFromTop; i< data.size(); i++)
		{
			results.add(data.get(i)[column]);
		}
		return results;
	}
	
	
	
	private int getKeyColumn(String key)
	{
		if (!keyIndexMap.containsKey(key))
		{
			throw new IllegalArgumentException("Invalid Key");
		}
		return keyIndexMap.get(key);
	}
	
	/**
	 * Returns the string values for a certain column (starting from index 1). 
	 * @param column - Column index to return
	 * @return
	 */
	public List<String> getColumn(String key)
	{
		return getRawColumn(keyIndexMap.get(key));
	}

	public List<String> getDataColumn(int column)
	{
		return getRawColumn(column + dataOffsetFromLeft);
	}
	/**
	 * This gives out a reference for speed, it should not be modified by the client
	 * @param index
	 * @return
	 */
	private String[] get(int index)
	{
		return data.get(index);
	}

	
	public String[] getDataRow(int index)
	{
		String[] results =  get(index + dataOffsetFromTop);
		return Arrays.copyOfRange(results, dataOffsetFromLeft, results.length);	
	}

	public int getNumberOfDataColumns(int row) {
		return get(row).length - dataOffsetFromLeft;
		
	}


	public String getDataKeyByIndex(int i) {
		return get(0)[i+dataOffsetFromLeft];
		
	}
	
	/**
	 * Returns an array of the IN ORDER listing of keys
	 * @return
	 */
	public String[] getDataKeyList()
	{
		List<String> dataList = new ArrayList<String>(get(0).length);
		for(int i =dataOffsetFromLeft; i < get(0).length; i++)
		{
			dataList.add(get(0)[i].trim());
			
		}
		return (String[]) dataList.toArray( new String[0]);
	}


	public List<String> getRowKeys() {

		return getRawColumn(0);
	}
	
	public String getKeyForDataColumn(int col)
	{
		return get(0)[col+dataOffsetFromLeft];
	}

	public String getKeyForDataRow(int i) {
		return (get(i+dataOffsetFromTop))[0];
	}
	

	public int getDataColumnForKey(String key) {
		return getKeyColumn(key) - dataOffsetFromLeft;
		
	}


	
	
}
