package ca.ubc.cs.beta.configspace;

import java.io.Serializable;

public class NormalizedRange implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2338679156299268217L;
	private final double min;
	private final double max;
	private final boolean logScale;

	public NormalizedRange(double min, double max, boolean logScale)
	{
		
		this.logScale = logScale;
		
		if(logScale)
		{
			if ((min <= 0) || (max <= 0))
			{
				throw new IllegalArgumentException("Log Scales cannot have negative values in Param File: min:" + min + " max: " + max);
			}
			
			min = Math.log10(min);
			max = Math.log10(max);
		}
		this.min = min;
		this.max = max;
		
		if(this.min >= this.max)
		{
			throw new IllegalArgumentException("Min must be strictly less than max: " + min + " >= " + max);
		}
		
		if(Double.isNaN(min) || Double.isInfinite(min))
		{
			throw new IllegalArgumentException("Min must be a real value");
		}
		
		if(Double.isNaN(max) || Double.isInfinite(max))
		{
			throw new IllegalArgumentException("Max must be a real value");
		}

	}
	
	public double normalizeValue(double x)
	{
		if(logScale)
		{
			x = Math.log10(x);
		}
		
		if (x < min || x > max)
		{
			throw new IllegalArgumentException("Value is outside of domain [" + min + "," + max + "]");
		}
		
		return (x - min) / (max - min);
	}
	
	public double unnormalizeValue(double x)
	{
		
		if (x < 0 || x > 1)
		{
			throw new IllegalArgumentException("Value is outside of [0,1]");
		}
		
		if(logScale)
		{
			return Math.pow(10, x*(max-min) + min);
		} else
		{
			return x*(max-min) + min;
		}
	}
	
	public String toString()
	{
		return "(NormalizeRange: {Min: " + min + " Max: " + max + "})";
	}
	
	
}