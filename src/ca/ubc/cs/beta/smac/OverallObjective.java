package ca.ubc.cs.beta.smac;

import java.util.Collection;

import org.apache.commons.math.stat.StatUtils;

public enum OverallObjective {
	MEAN,
	MEDIAN,
	Q90,
	ADJ_MEAN,
	MEAN1000,
	MEAN10,
	GEOMEAN;
	
	
	
	
		
		

	public double aggregate(Collection<Double> c, double cutoffTime)
	{
		double[] values = new double[c.size()];
		int i=0;
		for(double d : c)
		{
			values[i] = d;
			switch(this)
			{
			case MEAN10:
				values[i] = (values[i] >= cutoffTime) ? values[i] * 10 : values[i];
				break;
			case MEAN1000:
				values[i] = (values[i] >= cutoffTime) ? values[i] * 1000 : values[i];
				break;
				
			}
			
			
			i++;
		}
		
		switch(this)
		{
		case MEAN:
		case MEAN10:
		case MEAN1000:
			
			return StatUtils.mean(values);
		case MEDIAN:
			return StatUtils.percentile(values, 0.5);
		case Q90:
			return StatUtils.percentile(values, 0.9);
		case GEOMEAN:
			return StatUtils.geometricMean(values);
		case ADJ_MEAN:
			
		
			
		default:
			throw new UnsupportedOperationException(this.toString() + " is not a supported aggregation method");
		}
		
		
	}

	public double getPenaltyFactor() {
		switch(this)
		{
		case MEAN:
		case GEOMEAN:
			
			return 1;
		case MEAN10:
			return 10;
		case MEAN1000:
			return 1000;
		default: 
			throw new UnsupportedOperationException(this.toString() + " is not a supported aggregation method");
		}
	}
}
