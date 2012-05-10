package ca.ubc.cs.beta.smac.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.stat.StatUtils;

import ca.ubc.cs.beta.random.SeedableRandomSingleton;

public class ArrayMathOps {

	public static double exp(double x)
	{
		return Math.pow(Math.E, x);
	}
	
	
	public static double log(double x)
	{
		return Math.log(x);
	}
	
	public static double pow(double x, double y)
	{
		return Math.pow(x, y);
	}
	
	public static double sqrt(double x)
	{
		return Math.sqrt(x);
	}
	
	
	public static double[] sqrt(double[] x)
	{
		double[] y = new double[x.length];
		for(int i=0; i < x.length; i++)
		{
			y[i] = Math.sqrt(x[i]);
		}
		return y;
	}	
	
	
	public static double[] times( double x1, double[] x2)
	{
		double[] y = new double[x2.length];
		for(int i=0; i < x2.length; i++)
		{
			y[i] = x1*x2[i];
		}
		return y;
	}	
	
	public static double[] pow(double base, double[] exp)
	{
		double[] y = new double[exp.length];
		for(int i=0; i < exp.length; i++)
		{
			y[i] = Math.pow(base, exp[i]);
		}
		return y;
	}
	
	public static double[] exp(double[] x)
	{
		return pow(Math.E, x);
	}
	
	
	public static double[][] transpose(double[][] matrix)
	{
		double[][] transpose = new double[matrix[0].length][matrix.length];
		for(int i=0; i < transpose.length; i++)
		{
			for(int j=0; j < transpose[0].length; j++)
			{
				transpose[i][j] = matrix[j][i];
			}
		}
		
		return transpose;
	}
	
	public static int matlabHashCode(double[][] matrix)
	{
		String s = Arrays.deepToString(matrix);
		if(s.length() > 250)
		{
			s = s.substring(0,249)+ "...";
		}http://shop.lenovo.com/SEUILibrary/controller/e/webca/LenovoPortal/en_CA/catalog.workflow:category.details?current-catalog-id=12F0696583E04D86B9B79B0FEC01C087&current-category-id=A328080E436749CB4CEAE7C4428846A7&action=init
	
		//System.out.println("HASH=>" + s);
		return Math.abs(Arrays.deepHashCode(matrix)) % 32462867;  //Some prime around 2^25 (to prevent overflows in computation)
		
		
		
		
	}
	
	public static <X> List<X> permute(List<X> list)
	{
		List<X> newList = new ArrayList<X>(list.size());
		
		int[] perms = SeedableRandomSingleton.getPermutation(list.size(), 0);
		for(int i=0; i < list.size(); i++)
		{
			newList.add(list.get(perms[i]));
		}
		
		return newList;
	}
	
	public static double[][] copy(double[][] matrix)
	{
		double[][] newMatrix = new double[matrix.length][];
		for(int i=0; i < matrix.length; i++)
		{
			newMatrix[i] = matrix[i].clone();
		}
		return newMatrix;
	}


	public static double[] stripNans(double[] values)
	{
		int count = 0;
		for(int i=0; i < values.length; i++)
		{
			if(!Double.isNaN(values[i]))
			{
				count++;
			}
		}
		
		
		double[] newVals = new double[count];
		count=0;
		for(int i=0; i < values.length; i++)
		{
			if(!Double.isNaN(values[i]))
			{
				newVals[count] = values[i];
				count++;
			}
		}
		
		return newVals;
		
		
	}
	
	
	public static double meanIgnoreNaNs(double[] values) {
		return StatUtils.mean(stripNans(values));
	}


	public static double stdDevIgnoreNaNs(double[] values) {
		return Math.sqrt(StatUtils.variance(stripNans(values)));
	}


	public static double[] normalize(double[] values, double mean, double stdDev) {
		values = values.clone();
		for(int i=0; i < values.length; i++)
		{
			if(Double.isNaN(values[i])) continue; 

			values[i] = (values[i] - mean);
			if(stdDev > 0)
			{
				values[i] /= stdDev;
			}
		}
		return values;
	}


	public static double[] abs(double[] values) {
		values = values.clone();
		for(int i=0; i < values.length; i++)
		{
			values[i] = Math.abs(values[i]);
		}
		
		return values;
	}


	public static double max(double[] values) {
		if(values.length == 0) return Double.NEGATIVE_INFINITY;
		double max = values[0];
		for(int i=1; i < values.length; i++)
		{
			if(values[i] > max)
			{
				max = values[i];
			}
		}
		
		return max;
		
	}

	

	public static double maxIgnoreNaNs(double[] values) {
		return max(stripNans(values));
	}
}
