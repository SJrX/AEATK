package ca.ubc.cs.beta.ac.config.comparators;

import java.util.Comparator;

import ca.ubc.cs.beta.configspace.ParamConfiguration;

public class ParamConfigurationComparator implements Comparator<ParamConfiguration> {

	@Override
	public int compare(ParamConfiguration o1, ParamConfiguration o2) {
		// TODO Auto-generated method stub
		int difference =  o1.getFriendlyID() - o2.getFriendlyID();
		
		
		if(difference == 0)
		{
			if(o1.equals(o2))
			{
				return 0;
			} else
			{
				return o1.getFormattedParamString().compareTo(o2.getFormattedParamString());
			}
		} else
		{
			return difference;
		}
	}

}
