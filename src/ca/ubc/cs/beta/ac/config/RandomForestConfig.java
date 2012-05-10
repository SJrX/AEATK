package ca.ubc.cs.beta.ac.config;

import java.io.File;
import java.lang.reflect.Field;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.validators.PositiveInteger;

public class RandomForestConfig {

	@Parameter(names = "--splitMin", description = "Minimum number of elements needed to split a node ", validateWith=PositiveInteger.class )
	public int splitMin = 10;

	@Parameter(names = "--fullTreeBootstrap", description = "Bootstrap all data points into trees")
	public boolean fullTreeBootstrap = false;

	@Parameter(names = {"--storeDataInLeaves"}, description = "Store full data in leaves of trees")
	public boolean storeDataInLeaves = false;
	
	@Parameter(names = {"--logModel"}, description = "Store data in Log Normal form")
	public boolean logModel = false;

	@Parameter(names = {"--nTrees"}, description = "Number of Trees in Random Forest", validateWith=PositiveInteger.class)
	public int numTrees = 10;
	
	@Parameter(names="--minVariance", description="Minimum allowed variance")
	public double minVariance = Math.pow(10,-14);

	@Parameter(names="--ratioFeatures", description="Number of features to consider when building Regression Forest")
	public double ratioFeatures = 5.0/6.0;

	@Parameter(names="--preprocessMarginal", description="Build Random Forest with Preprocessed Marginal")
	public boolean preprocessMarginal = false;

	public String toString()
	{
		
	
		StringBuilder sb = new StringBuilder();
		
		sb.append("RF Config\n");
		try {
		for(Field f : this.getClass().getDeclaredFields())
		{
			if(f.getAnnotation(Parameter.class) != null)
			sb.append(f.getName());
			sb.append(" = ");
			
			Class<?> o = f.getType();
			if(o.isPrimitive())
			{
				sb.append(f.get(this).toString());
			} else
			{
				Object obj = f.get(this);
				if(obj == null)
				{
					sb.append("null");
				} else if(obj instanceof File)
				{
					sb.append(((File) obj).getAbsolutePath());
				} else if (obj instanceof String)
				{
					sb.append(obj);
				} else if (obj instanceof Long)
				{
					sb.append(obj.toString());
				} else if(obj instanceof Integer)
				{
					sb.append(obj.toString());
				} else if (obj instanceof Enum)
				{
					sb.append(((Enum) obj).name());
				} else if (obj instanceof RandomForestConfig)
				{
					sb.append(obj.toString());
				}
				else {
					//We throw this because we have no guarantee that toString() is meaningful
					throw new IllegalArgumentException("Failed to convert type configuration option to a string " + f.getName() + "=" +  obj + " type: " + o) ;
				}
			}
			sb.append("\n");
		}
		return sb.toString();
		} catch(RuntimeException e)
		{
			throw e;
			
		} catch(Exception e)
		{
			throw new RuntimeException(e); 
		}
	}
}
