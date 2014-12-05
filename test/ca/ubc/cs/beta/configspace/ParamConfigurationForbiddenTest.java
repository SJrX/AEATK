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

	public static ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("a [0,10] [0.5]\nb[0,10] [0.5]\n");
	
	public static ParameterConfigurationSpace forbiddenConfigSpace = ParamFileHelper.getParamFileFromString("a [0,10] [0.5]\nb[0,10] [0.5]\nForbidden Expression:a^2+b^2>=1");
		
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
		
		for(int i=0; i < 100000; i++)
		{
			
			x+=configSpace.getRandomParameterConfiguration(mtf).toValueArray()[0];
			
		}
		
		System.out.println(x+":" + watch.stop() / 1000.0);
		watch = new AutoStartStopWatch();
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		for(int i=0; i < 100000; i++)
		{
			
			List<ParameterConfiguration> paramConfiguration = config.getNeighbourhood(mtf, 4);
			//System.out.println(i + ":"+ config.getFormattedParameterString());
			config = paramConfiguration.get(mtf.nextInt(paramConfiguration.size()));
		}
		System.out.println("Walk:" + watch.stop() / 1000.0 + " => " + config.getFormattedParameterString());
	}
	
	


}
