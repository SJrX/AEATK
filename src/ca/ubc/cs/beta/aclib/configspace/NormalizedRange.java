package ca.ubc.cs.beta.aclib.configspace;

import java.io.Serializable;

/**
 * Maps a value on some interval to [0,1] and back
 * @see ParamConfigurationSpace
 */
public class NormalizedRange implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2338679156299268217L;
	
	private final double minUnnormalizedValue;
	
	private final double maxUnnormalizedValue;
	private final boolean normalizeToLog;
	private final boolean intValuesOnly;
	
	/**
	 * 
	 * @param minValue 			double for the minimum legal value in range
	 * @param maxValue			double for the maximum legal value in range
	 * @param normalizeToLog	<code>true</code> if we should take the log before normalizing, <code>false</code> otherwise
	 * @param intValuesOnly		<code>true</code> if the range is discrete (i.e. integers)
	 * 
	 * @throws IllegalArgumentException if minValue >= maxValue, any are infinite or NaN, or we are to log negative values.
	 */
	public NormalizedRange(double minValue, double maxValue, boolean normalizeToLog, boolean intValuesOnly)
	{
		
		this.normalizeToLog = normalizeToLog;
		this.intValuesOnly = intValuesOnly;
		
		if(normalizeToLog)
		{
			if ((minValue <= 0) || (maxValue <= 0))
			{
				throw new IllegalArgumentException("Log Scales cannot have negative or zero values in Param File: min:" + minValue + " max: " + maxValue);
			}
			
			minValue = Math.log10(minValue);
			maxValue = Math.log10(maxValue);
		}
		this.minUnnormalizedValue = minValue;
		this.maxUnnormalizedValue = maxValue;
		
		if(this.minUnnormalizedValue >= this.maxUnnormalizedValue)
		{
			throw new IllegalArgumentException("Min must be strictly less than max: " + minValue + " >= " + maxValue);
		}
		
		if(Double.isNaN(minValue) || Double.isInfinite(minValue))
		{
			throw new IllegalArgumentException("Min must be a real value");
		}
		
		if(Double.isNaN(maxValue) || Double.isInfinite(maxValue))
		{
			throw new IllegalArgumentException("Max must be a real value");
		}

	}
	
	/**
	 * 
	 * @param x number to normalize
	 * @return number in [0,1]
	 */
	public double normalizeValue(double x)
	{
		if(normalizeToLog)
		{
			x = Math.log10(x);
		}
		
		if (x < minUnnormalizedValue || x > maxUnnormalizedValue)
		{
			throw new IllegalArgumentException("Value is outside of domain [" + minUnnormalizedValue + "," + maxUnnormalizedValue + "]");
		}
		
		return (x - minUnnormalizedValue) / (maxUnnormalizedValue - minUnnormalizedValue);
	}
	
	/**
	 * 
	 * @param x number in [0,1]
	 * @return number in original range
	 */
	public double unnormalizeValue(double x)
	{
		
		if (x < 0 || x > 1)
		{
			throw new IllegalArgumentException("Value is outside of [0,1]");
		}
		
		double value; 
		if(normalizeToLog)
		{
			value = Math.pow(10, x*(maxUnnormalizedValue-minUnnormalizedValue) + minUnnormalizedValue);
		} else
		{
			value = x*(maxUnnormalizedValue-minUnnormalizedValue) + minUnnormalizedValue;
		}
		
		if(intValuesOnly)
		{
			return Math.round(value);
		} else
		{
			return value;
		}
	}
	
	@Override
	public String toString()
	{
		return "(NormalizeRange: {Min: " + minUnnormalizedValue + " Max: " + maxUnnormalizedValue + ((normalizeToLog) ? " LOG " : "") + ((intValuesOnly) ? " INT " : "") + "})";
	}

	/**
	 * 
	 * @return <code>true</true> if the range is only integers, false otherwise
	 */
	public boolean isIntegerOnly() {
		return intValuesOnly;
	}
	
	
}