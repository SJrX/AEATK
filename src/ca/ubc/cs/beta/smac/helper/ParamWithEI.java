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
			throw new IllegalArgumentException("List of double and number of configurations must be the same");
		}
		
		List<ParamWithEI> list = new ArrayList<ParamWithEI>();
		for(int i=0; i < x.length; i++)
		{
			list.add(new ParamWithEI(x[i],c.get(i)));
		}
		return list;
	}
}
