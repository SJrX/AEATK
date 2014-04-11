package ca.ubc.cs.beta.targetalgorithmevaluator;

import java.util.Random;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration.ParameterStringFormat;

public class ParamEchoExecutorWithGibberish {

	
	
	
	private static final String[] randomGibberish =
		{
			"How come wrong numbers are never busy?",
			"Do people in Australia call the rest of the world 'up over'?",
			"Does that screwdriver really belong to Phillip?",
			"Can a stupid person be a smart-ass?",
			"Does killing time damage eternity?",
			"Why doesn't Tarzan have a beard?",
			"Why is it called lipstick if you can still move your lips?",
			"Why is it that night falls but day breaks?",
			"Why is the third hand on a clock called a second hand?",
			"Why is it that when you're driving and looking for an address you turn down the volume on the radio?",
			"Why is lemon juice made with artificial flavor and dishwashing liquid made with real lemons?",
			"Are part-time band leaders semi-conductors?",
			"Can you buy an entire chess set in a pawn-shop?",
			"Day light savings time - why are they saving it and where do they keep it?",
			"Did Noah keep his bees in archives?",
			"Do jellyfish get gas from eating jellybeans?",
			"Do pilots take crash-courses?",
			"Do Roman paramedics refer to IV's as \"4's\"?",
			"Do stars clean themselves with meteor showers?",
			"Do you think that when they asked George Washington for ID that he just whipped out a quarter?",
		};
	/**
	 * 
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		Random r = new Random();
		try 
		{
			String instanceName = args[0];
			String instanceSpecificInfo = args[1];
			String cutoffTime = args[2];
			String cutoffLength = args[3];
			String seed = args[4];
			StringBuilder sb = new StringBuilder();
			for(int i=5; i < args.length; i++)
			{
				sb.append(args[i]).append(" ");
			}
			
			
			
			ParameterConfigurationSpace configSpace = new ParameterConfigurationSpace(TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt"));
			
			
			
			
			ParameterConfiguration config = configSpace.getParameterConfigurationFromString(sb.toString(), ParameterStringFormat.NODB_SYNTAX);
			
			String result = config.get("solved");
			String runtime = config.get("runtime");
			String runlength = config.get("runlength");
			String quality = config.get("quality");
			String resultSeed = config.get("seed");			
			
			/*long returnCutOffLength = instanceName.hashCode() + 37*instanceSpecificInfo.hashCode();*/

		
			
			
			System.out.println("Result for ParamILS: " + result + "," + runtime + "," + runlength + "," + quality + "," + resultSeed + "," + randomGibberish[r.nextInt(randomGibberish.length)]+ "\n");
		} catch(RuntimeException e)
		{
			System.out.println("Result for ParamILS: CRASHED, 0.000, 0, 0," + args[4] + "," + randomGibberish[r.nextInt(randomGibberish.length)] + "\n");
		}
		

	}

}
