package ca.ubc.cs.beta.fastrf;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.StructureGraphic.v1.DSutils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration.StringFormat;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.configspace.ParamFileHelper;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;

import ca.ubc.cs.beta.aclib.misc.random.SeedableRandomSingleton;
import ca.ubc.cs.beta.aclib.model.builder.AdaptiveCappingModelBuilder;
import ca.ubc.cs.beta.aclib.model.builder.BasicModelBuilder;
import ca.ubc.cs.beta.aclib.model.builder.ModelBuilder;
import ca.ubc.cs.beta.aclib.model.data.MaskCensoredDataAsUncensored;
import ca.ubc.cs.beta.aclib.model.data.PCAModelDataSanitizer;
import ca.ubc.cs.beta.aclib.model.data.SanitizedModelData;
import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;
import ca.ubc.cs.beta.aclib.options.RandomForestOptions;
import ca.ubc.cs.beta.aclib.options.ScenarioOptions;
import ca.ubc.cs.beta.aclib.probleminstance.InstanceListWithSeeds;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceHelper;
import ca.ubc.cs.beta.aclib.runhistory.RunHistory;
import ca.ubc.cs.beta.aclib.state.RandomPoolType;
import ca.ubc.cs.beta.aclib.state.StateDeserializer;
import ca.ubc.cs.beta.aclib.state.StateFactory;
import ca.ubc.cs.beta.aclib.state.legacy.LegacyStateFactory;
import ca.ubc.cs.beta.models.fastrf.RandomForest;
import ec.util.MersenneTwister;

public class fANOVATester {

	public Logger log = LoggerFactory.getLogger(getClass());
	public static ParamConfigurationSpace configSpace;
	public static InstanceListWithSeeds ilws;
	public static AlgorithmExecutionConfig dummyExecConfig = new AlgorithmExecutionConfig(null, null, null, false, false, 5000);
	private static String experimentDir = "/ubc/cs/home/h/hutter/disks/orc/home/hutter/experiments";
	
	@BeforeClass
/*
 * 	public static void beforeClass()
	{
		//File expDir = TestHelper.getTestFile("RandomForestFiles");
		//File paramFile = TestHelper.getTestFile("RandomForestFiles/satenstein-params-mixed.txt");
		File expDir = new File("/ubc/cs/home/s/seramage/experiments/run0");
		File paramFile = new File("/ubc/cs/home/s/seramage/experiments/algorithms/cplex/cplex12-params-milp-mixed-cont-disc.txt");
		configSpace = ParamFileHelper.getParamFileParser(paramFile, 1);

		try {
			//ilws = ProblemInstanceHelper.getInstances("cbmc_train_filename.txt", expDir.getAbsolutePath(),"cbmc_train_filename_feat.csv" , false);
			ilws = ProblemInstanceHelper.getInstances("instancelists/CL-scrambled-first1000.txt", "/ubc/cs/home/s/seramage/experiments/","instancelists/CL-scrambled-features.csv", false);
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
*/

  	public static void beforeClass()
	{
		ScenarioOptions scenOpts = new ScenarioOptions();
		JCommander com = new JCommander(scenOpts, true, true);
		String[] scenFileName = {"--scenarioFile", experimentDir + "/scenarios/SPEAR-IBM-1day-complete-discrete.txt"};
		com.parse(scenFileName);

//		File expDir = new File(experimentDir + "/smac-output/SMACout_F/v2.04.00-development-380_AAAI_CPLEX12-CLS-1day-5000-discrete-adaptiveCappingtrue/state-run0");
		configSpace = ParamFileHelper.getParamFileParser(experimentDir + "/" + scenOpts.paramFileDelegate.paramFile);
		
		try {
			ilws = ProblemInstanceHelper.getInstances(scenOpts.instanceFile,experimentDir, scenOpts.instanceFeatureFile, scenOpts.checkInstanceFilesExist, 0, scenOpts.algoExecOptions.deterministic);
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	
	/**
	 * Constructs a tree and runs functional ANOVA.
	 */
	@Test
	public void testFANOVA()
	{
		RandomForestOptions rfOptions = new RandomForestOptions();
		
//		File restoreDirectory = new File("/ubc/cs/home/s/seramage/experiments/run0");
		File restoreDirectory = new File(experimentDir + "/smac-output/SMACout_F/v2.04.00-development-380_AAAI_SPEAR-IBM-1day-complete-discrete-adaptiveCappingtrue/state-run0");
//		File restoreDirectory = new File(experimentDir + "/smac-output/SMACout_F/v2.04.00-development-380_AAAI_CPLEX12-CLS-1day-5000-discrete-adaptiveCappingtrue/state-run0");
		assertTrue(restoreDirectory.exists());
		
		StateFactory sf = new LegacyStateFactory(null, restoreDirectory.getAbsolutePath());
		
		OverallObjective intraObjective = OverallObjective.MEAN;
		//Old iteration was 1106
//		StateDeserializer sd = sf.getStateDeserializer("it", 1106, configSpace, intraObjective, OverallObjective.MEAN, RunObjective.RUNTIME, ilws.getInstances(), dummyExecConfig);
		StateDeserializer sd = sf.getStateDeserializer("it", 16, configSpace, intraObjective, OverallObjective.MEAN, RunObjective.RUNTIME, ilws.getInstances(), dummyExecConfig);
		
		assertNotNull(sd.getRunHistory());
		List<ProblemInstance> instances = ilws.getInstances();
		RunHistory runHistory = sd.getRunHistory();
		
		//==== Format data properly.
		
		//=== The following two sets are required to be sorted by instance and paramConfig ID.
		Set<ProblemInstance> all_instances = new LinkedHashSet<ProblemInstance>(instances);
		Set<ParamConfiguration> paramConfigs = runHistory.getUniqueParamConfigurations();
		
		Set<ProblemInstance> runInstances=runHistory.getUniqueInstancesRan();
		ArrayList<Integer> runInstancesIdx = new ArrayList<Integer>(all_instances.size());
		
		//=== Get the instance feature matrix (X).
		int i=0; 
		double[][] instanceFeatureMatrix = new double[all_instances.size()][];
		for(ProblemInstance pi : all_instances)
		{
			if(runInstances.contains(pi))
			{
				runInstancesIdx.add(i);
			}
			instanceFeatureMatrix[i] = pi.getFeaturesDouble();
			i++;
		}

		//=== Get the parameter configuration matrix (Theta).
		double[][] thetaMatrix = new double[paramConfigs.size()][];
		i = 0;
		for(ParamConfiguration pc : paramConfigs)
		{
			thetaMatrix[i++] = pc.toValueArray();
		}

		//=== Get an array of the order in which instances were used (TODO: same for Theta, from ModelBuilder) 
		int[] usedInstanceIdxs = new int[runInstancesIdx.size()]; 
		for(int j=0; j <  runInstancesIdx.size(); j++)
		{
			usedInstanceIdxs[j] = runInstancesIdx.get(j);
		}
		
		double[] runResponseValues = runHistory.getRunResponseValues();
		
		
		
		for(int j=0; j < runResponseValues.length; j++)
		{ //=== Not sure if I Should be penalizing runs prior to the model
			// but matlab sure does
			if(runResponseValues[j] >= dummyExecConfig.getAlgorithmCutoffTime())
			{	
				runResponseValues[j] = runResponseValues[j] *10;
			}
		}
	
	
	
		//SeedableRandomSingleton.setRandom(sd.getPRNG(RandomPoolType.SEEDABLE_RANDOM_SINGLETON));
		
		
		
		List<AlgorithmRun> o =  runHistory.getAlgorithmRuns();
		for(AlgorithmRun run : o)
		{
			if(run.getRunConfig().getParamConfiguration().equals(sd.getIncumbent()))
			{
				log.info("Instance {} Runtime {}:", run.getRunConfig().getProblemInstanceSeedPair().getInstance().getInstanceID(), run.getRuntime());
			}
		}
		

		rfOptions.numTrees = 10;
		System.out.println(Arrays.toString(sd.getIncumbent().toValueArray()));
		System.out.println("Incumbent: " + runHistory.getThetaIdx(sd.getIncumbent()));
		StringWriter sWriter = new StringWriter();
		PrintWriter pWriter = new PrintWriter(sWriter);
		for(int j=0; j < sd.getIncumbent().toValueArray().length; j++)
		{
			pWriter.format("%20d", j);
			if(j+1 != sd.getIncumbent().toValueArray().length) pWriter.append(",");
		}
		System.out.println("  :" + sWriter.toString());
		/*System.out.println("IA:"+sd.getIncumbent().getFormattedParamString(StringFormat.FIXED_WIDTH_ARRAY_STRING_SYNTAX));
		System.out.println("CA:"+runHistory.getAllParameterConfigurationsRan().get(7).getFormattedParamString(StringFormat.FIXED_WIDTH_ARRAY_STRING_SYNTAX));
		System.out.println("IH:"+sd.getIncumbent().getFormattedParamString(StringFormat.FIXED_WIDTH_ARRAY_STRING_MASK_INACTIVE_SYNTAX));
		System.out.println("CH:"+runHistory.getAllParameterConfigurationsRan().get(7).getFormattedParamString(StringFormat.FIXED_WIDTH_ARRAY_STRING_MASK_INACTIVE_SYNTAX));
		*/
		rfOptions.ignoreConditionality = false;
		
		

		//=== Sanitize the data.
		SanitizedModelData sanitizedData = new PCAModelDataSanitizer(instanceFeatureMatrix, thetaMatrix, 7, runResponseValues, usedInstanceIdxs, true, runHistory.getParameterConfigurationInstancesRanByIndex(), runHistory.getCensoredFlagForRuns(), configSpace);
		
		
		//=== Build the actual model.
		ModelBuilder mb = new AdaptiveCappingModelBuilder(sanitizedData, rfOptions, new MersenneTwister(0), 2, 5000, intraObjective.getPenaltyFactor());
		
		
		RandomForest preparedforest = mb.getPreparedRandomForest();
		
		
		//== Apply Marginal
		double[][] incumbent = { sd.getIncumbent().toValueArray() };
		
		int[] treeIdxsToUse = new int[preparedforest.numTrees];
		for(int j=0; j <  preparedforest.numTrees; j++)
		{
			treeIdxsToUse[j]=j;
		}
		
		
		double[][] predictions  = RandomForest.applyMarginal(preparedforest, treeIdxsToUse, incumbent);
		System.out.println("ID:"+runHistory.getThetaIdx(sd.getIncumbent()));
		
		System.out.println("predictions for incumbent: " + Arrays.deepToString(predictions) );
		System.out.println("fmin: " + runHistory.getEmpiricalCost(sd.getIncumbent(), runHistory.getUniqueInstancesRan(), dummyExecConfig.getAlgorithmCutoffTime()));
		
		//System.out.println("Hello");	
	}
}
