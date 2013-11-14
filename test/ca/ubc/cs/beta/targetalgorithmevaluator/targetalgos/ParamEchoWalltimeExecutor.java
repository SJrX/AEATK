package ca.ubc.cs.beta.targetalgorithmevaluator.targetalgos;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.base.cli.CommandLineAlgorithmRun;

public class ParamEchoWalltimeExecutor {

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
						
			ParamConfigurationSpace configSpace = new ParamConfigurationSpace(TestHelper.getTestFile("paramFiles/paramEchoParamFileWalltime.txt"));

			
			ParamConfiguration config = configSpace.getConfigurationFromString(sb.toString(), StringFormat.NODB_SYNTAX);
			
			String result = config.get("solved");
			String runtime = config.get("runtime");
			String runlength = config.get("runlength");
			String quality = config.get("quality");
			String resultSeed = config.get("seed");	
			String walltime = config.get("walltime");
			
			/*long returnCutOffLength = instanceName.hashCode() + 37*instanceSpecificInfo.hashCode();*/

			
			
			try {
				//Paranoid check to ensure the environment variable is set.
				Integer.valueOf(System.getenv(CommandLineAlgorithmRun.CONCURRENT_TASK_ID));
			} catch(RuntimeException e)
			{
				System.out.println("Result for ParamILS: CRASHED, 0.000, 0, 0," + args[4] + ", No Task ID Detected\n");
				return;
			}
			
			Double walltimeSeconds = Double.valueOf(walltime);

			if(args[1].trim().equals("SLEEP"))
			{
				
				try {
					Thread.sleep((long) (walltimeSeconds* 1000) + 150);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			
		
			//System.err.println("Process Running done");
			System.out.println("Result for ParamILS: " + result + "," + runtime + "," + runlength + "," + quality + "," + resultSeed + "\n");
			
		} catch(RuntimeException e)
		{
			System.out.println("Result for ParamILS: CRASHED, 0.000, 0, 0," + args[4] + "\n");
		}
		

	}

}
