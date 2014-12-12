package ca.ubc.cs.beta.configspace;

import java.util.List;

import org.junit.Test;

import ca.ubc.cs.beta.aeatk.misc.watch.AutoStartStopWatch;
import ca.ubc.cs.beta.aeatk.misc.watch.StopWatch;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParamFileHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ec.util.MersenneTwister;
import ec.util.MersenneTwisterFast;

public class ParamConfigurationForbiddenTest {

	public static ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a [-1,1] [0.5]\nb[-1,1] [0.5]\n");

	
	public static ParameterConfigurationSpace forbiddenConfigSpace = ParamFileHelper.getParamFileFromString("a [-1,1] [0]\nb[-1,1] [0]\nForbidden Expression: sqrt(a^2+b^2)>=1");
	
	
	public static ParameterConfigurationSpace forbiddenConfigSpaceTwo = ParamFileHelper.getParamFileFromString("a [-1,1] [0]\nb[-1,1] [0]\nForbidden Expression: sqrt(a^2+b^2)>=0.5 \nForbidden Expression: abs(b-a)>0.5");
	
	
	
	//public static ParameterConfigurationSpace forbiddenConfigSpace = ParamFileHelper.getParamFileFromString("a [0,1000000] [0]i\n b[0,1000000] [0]i\n c[0,1000000] [0]i\n Forbidden Expression: abs(c^3-(a^3+b^3))>0.9");
	
	@Test
	public void testNewFormatTwo()
	{
		System.out.println("Foo");
		benchMarkSpeed(forbiddenConfigSpaceTwo);
		System.out.println("Foo");
	}
	
	@Test
	public void testNewFormat()
	{
		System.out.println("Foo");
		benchMarkSpeed(forbiddenConfigSpace);
		System.out.println("Foo");
	}

	@Test
	public void testOldFormat()
	{
		
		benchMarkSpeed(configSpace);
		
	}
	
	
	public static void main(String[] args)
	{
		(new ParamConfigurationForbiddenTest()).testOldFormat();
	}
	/**
	 * 
	 */
	public void benchMarkSpeed(ParameterConfigurationSpace configSpace) {
		StopWatch watch = new AutoStartStopWatch();
		
		double x = 0;
		MersenneTwister mtf = new MersenneTwister();
		
		for(int i=0; i < 200000; i++)
		{
			
			x+=configSpace.getRandomParameterConfiguration(mtf).toValueArray()[0];
			
		}
		
		System.out.println(x+":" + watch.stop() / 1000.0);
		watch = new AutoStartStopWatch();
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		for(int i=0; i < 200000; i++)
		{
			
			List<ParameterConfiguration> paramConfiguration = config.getNeighbourhood(mtf, 4);
			double a = Double.valueOf(config.get("a"));
			double b = Double.valueOf(config.get("b"));
			//double c = Double.valueOf(config.get("c"));
			//System.out.println(i + ":"+ config.getFormattedParameterString() + ":"+ (c*c*c - (a*a*a+b*b*b))) ;
			//System.out.println(i + ":"+ config.getFormattedParameterString() + ":"+ Math.sqrt(a*a+b*b));
			config = paramConfiguration.get(mtf.nextInt(paramConfiguration.size()));
		}
		System.out.println("Walk:" + watch.stop() / 1000.0 + " => " + config.getFormattedParameterString());
	}
	
	
	

}
