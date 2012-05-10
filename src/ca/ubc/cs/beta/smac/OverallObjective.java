package ca.ubc.cs.beta.smac;

import java.util.Collection;

import org.apache.commons.math.stat.StatUtils;

public enum OverallObjective {
	MEAN,
	MEDIAN,
	Q90,
	ADJ_MEAN,
	MEAN1000;
	
	
	
		
		

	public double aggregate(Collection<Double> c, double cutoffTime)
	{
		double[] values = new double[c.size()];
		int i=0;
		for(double d : c)
		{
			values[i] = d;
			i++;
		}
		
		switch(this)
		{
		case MEAN:
			return StatUtils.mean(values);
		case MEDIAN:
			return StatUtils.percentile(values, 0.5);
		case Q90:
			return StatUtils.percentile(values, 0.9);
		case ADJ_MEAN:
			
		case MEAN1000:
			
		default:
			throw new UnsupportedOperationException(this.toString() + " is not a supported aggregation method");
		}
		
		
	}
}
