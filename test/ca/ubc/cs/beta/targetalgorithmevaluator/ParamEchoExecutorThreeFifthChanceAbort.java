package ca.ubc.cs.beta.targetalgorithmevaluator;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aeatk.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aeatk.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aeatk.configspace.ParamConfiguration.StringFormat;

public class ParamEchoExecutorThreeFifthChanceAbort {

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
						
			ParamConfigurationSpace configSpace = new ParamConfigurationSpace(TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt"));

			
			ParamConfiguration config = configSpace.getConfigurationFromString(sb.toString(), StringFormat.NODB_SYNTAX);
			
			String result = config.get("solved");
			String runtime = config.get("runtime");
			String runlength = config.get("runlength");
			String quality = config.get("quality");
			String resultSeed = config.get("seed");			
			
			/*long returnCutOffLength = instanceName.hashCode() + 37*instanceSpecificInfo.hashCode();*/

			
			Double runtimeSeconds = Double.valueOf(runtime);

			if(args[1].trim().equals("SLEEP"))
			{
				
				try {
					Thread.sleep((long) (runtimeSeconds* 1000));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			
			if(Math.random() < 0.6)
			{
				result = "ABORT";
			}
		
			//System.err.println("Process Running done");
			
			System.out.println("Result for ParamILS: " + result + "," + runtime + "," + runlength + "," + quality + "," + resultSeed + "\n");
			
		} catch(RuntimeException e)
		{
			System.out.println("Result for ParamILS: CRASHED, 0.000, 0, 0," + args[4] + "\n");
		}
		

	}

}
