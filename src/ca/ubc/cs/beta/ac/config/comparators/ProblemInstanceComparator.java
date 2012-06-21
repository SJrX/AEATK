package ca.ubc.cs.beta.ac.config.comparators;

import java.util.Comparator;

import ca.ubc.cs.beta.probleminstance.ProblemInstance;

public class ProblemInstanceComparator implements Comparator<ProblemInstance> {

	@Override
	public int compare(ProblemInstance o1, ProblemInstance o2) {
		int difference = o1.getInstanceID() - o2.getInstanceID();
		if(difference == 0)
		{
			if (o1.equals(o2))
			{
				return 0;
			} else
			{
				return o1.getInstanceName().compareTo(o2.getInstanceName());
			}
		} else
		{
			return difference;
		}
	}

}
