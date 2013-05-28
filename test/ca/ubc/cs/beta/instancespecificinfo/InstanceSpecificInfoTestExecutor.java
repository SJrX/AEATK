package ca.ubc.cs.beta.instancespecificinfo;

public class InstanceSpecificInfoTestExecutor {

	public static void main(String[] args)
	{
		
		try 
		{
			String instanceName = args[0];
			String instanceSpecificInfo = args[1];
			/*String cutoffTime = args[2];
			String cutoffLength = args[3];*/
			String seed = args[4];
			
			long returnCutOffLength = Math.abs((long) instanceName.hashCode() + 37*instanceSpecificInfo.hashCode());
			System.out.println("Result for ParamILS: SAT, 0.27, " + returnCutOffLength + "," + 0 + "," + seed + "\n");
		} catch(RuntimeException e)
		{
			System.out.println("Result for ParamILS: CRASHED, 0.000, 0,0\n");
		}
	}
}
