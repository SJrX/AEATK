package ca.ubc.cs.beta.targetalgorithmevaluator.massiveoutput;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration.StringFormat;

public class MassiveOutputParamEchoExecutor {

	/**
	 * 
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		StringBuilder massiveOutputLines = new StringBuilder();
		
		
		for(int j=0; j < 1; j++)
		{
			
		
			for(int i=0; i < 1024*1; i++)
			{				
				char c =  (char) (Math.random() * (127-32) + 32);
				for(int k=0; k < 1024; k++)
				{
					massiveOutputLines.append(c);
				}
			}

			massiveOutputLines.append("\n");
		}
		
		
		System.out.println(massiveOutputLines);

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
			
			
			
			
			
			ParamConfigurationSpace configSpace = new ParamConfigurationSpace(TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt"));
			
			
			
			
			ParamConfiguration config = configSpace.getConfigurationFromString(sb.toString(), StringFormat.NODB_SYNTAX);
			
			String result = config.get("solved");
			String runtime = config.get("runtime");
			String runlength = config.get("runlength");
			String quality = config.get("quality");
			String resultSeed = config.get("seed");			
			
			/*long returnCutOffLength = instanceName.hashCode() + 37*instanceSpecificInfo.hashCode();*/

			
			
			System.out.println("Result for ParamILS: " + result + "," + runtime + "," + runlength + "," + quality + "," + resultSeed + "\n");
		} catch(RuntimeException e)
		{
			System.out.println("Result for ParamILS: CRASHED, 0.000, 0, 0," + args[4] + "\n");
		}
		

	}

}
