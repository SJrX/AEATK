package ca.ubc.cs.beta.ac.config;

import java.util.ArrayList;
import ca.ubc.cs.beta.smac.ac.runs.AlgorithmRun;

public interface PerformanceMetric<K extends AlgorithmRun>  {
	
	public double aggregate(ArrayList<Double> runs);
	

}
