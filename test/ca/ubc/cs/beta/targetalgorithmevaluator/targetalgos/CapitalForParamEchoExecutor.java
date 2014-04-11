package ca.ubc.cs.beta.targetalgorithmevaluator.targetalgos;

import java.util.Random;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration.ParameterStringFormat;
import ec.util.MersenneTwister;

public class CapitalForParamEchoExecutor {

	/**
	 * 
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		
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

			
			
			
			
			
			String[] rand = {"","","","","","","","","","","","",""};
			
			
			Random r = new MersenneTwister();
			
			
			String[] randChars = {" ", " ", " ", " ", " ", " ", " ", " ", "\t", " " , " "};
			for(int i=0; i < rand.length;i++)
			{
				int length = r.nextInt(20);
				for(int j = 0; j < length; j++ )
				{
					//Who cares about how slow this is
					//it's a test
						rand[i] += randChars[r.nextInt(randChars.length)];
				
					
				}
			}
			
			
			
			String[] resultStrings = { "Result for ParamILS", "Result For ParamILS", "Result For SMAC", "Final result for ParamILS", "Final Result for SMAC", "Final Result for HAL", "result of this wrapper", "result Of this wrapper", "Final result of SMAC", "result Of This Wrapper"};
			
			
			
			String resultString = resultStrings[r.nextInt(resultStrings.length)];
					
			System.out.println( rand[1] + resultString + ":" + rand[2] + result + rand[3] + "," + rand[4] + runtime + rand[5] + "," + rand[6] + runlength + rand[7] + "," +rand[8] + quality + rand[9] + "," + rand[10] +  resultSeed + rand[11] + "\n");
		} catch(RuntimeException e)
		{
			e.printStackTrace();
			System.out.println("Result for ParamILS: CRASHED, 0.000, 0, 0," + args[4] + "\n");
		}
		

	}

}
