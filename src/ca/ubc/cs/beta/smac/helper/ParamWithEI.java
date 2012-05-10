package ca.ubc.cs.beta.smac.helper;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.cs.beta.configspace.ParamConfiguration;

public class ParamWithEI extends AssociatedValue<Double, ParamConfiguration> {

	public ParamWithEI(Double t, ParamConfiguration v) {
		super(t, v);
	}
	
	public static List<ParamWithEI> merge(double[] x, List<ParamConfiguration> c)
	{
		if (x.length != c.size())
		{
			throw new IllegalArgumentException("List of doublse and number of configurations must be the same");
			
		}
		
		List<ParamWithEI> list = new ArrayList<ParamWithEI>();
		for(int i=0; i < x.length; i++)
		{
			list.add(new ParamWithEI(x[i],c.get(i)));
		}
		return list;
	}
	
	/* Not sure why most of this code was commented out, in the most recent iteration it was
	 * everything commented out except the call to super and the eventually return.
	 * I guess this code should be deleted
	public int compareTo(ParamWithEI o) {
		int compValue =  super.compareTo(o);
		
		if(compValue == 0)
		{
		
			double[] myArray = this.getValue().toValueArray();
			double[] oArray = o.getValue().toValueArray();
			
			
			if(myArray.length != oArray.length)
			{
				throw new IllegalStateException("Not comparing on the same parameter space");
			}
			
			for(int i=0; i < myArray.length; i++)
			{
				double diff = myArray[i] - oArray[i];
				if(diff == 0) continue;
				if(diff < 0) 
				{
					return -1;
				} else
				{
					return 1;
				}
			}
			
			
		} 
		
		return compValue;
		
	}
	*/

}
