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

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.configspace.ParamConfiguration;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.configspace.ParamFileHelper;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;

import ca.ubc.cs.beta.aclib.model.builder.AdaptiveCappingModelBuilder;
import ca.ubc.cs.beta.aclib.model.builder.ModelBuilder;
import ca.ubc.cs.beta.aclib.model.data.PCAModelDataSanitizer;
import ca.ubc.cs.beta.aclib.model.data.SanitizedModelData;
import ca.ubc.cs.beta.aclib.objectives.OverallObjective;
import ca.ubc.cs.beta.aclib.objectives.RunObjective;
import ca.ubc.cs.beta.aclib.options.RandomForestOptions;
import ca.ubc.cs.beta.aclib.probleminstance.InstanceListWithSeeds;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceHelper;
import ca.ubc.cs.beta.aclib.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aclib.runhistory.RunHistory;
import ca.ubc.cs.beta.aclib.state.StateDeserializer;
import ca.ubc.cs.beta.aclib.state.StateFactory;
import ca.ubc.cs.beta.aclib.state.legacy.LegacyStateFactory;
import ca.ubc.cs.beta.models.fastrf.RandomForest;
import ec.util.MersenneTwister;

public class RegtreeBuildTester {

	public Logger log = LoggerFactory.getLogger(getClass());
	public static ParamConfigurationSpace configSpace;
	
	public static InstanceListWithSeeds ilws;
	
	public static AlgorithmExecutionConfig dummyExecConfig = new AlgorithmExecutionConfig(null, null, null, false, false, 5000);
	@BeforeClass
	public static void beforeClass()
	{
		//File expDir = TestHelper.getTestFile("RandomForestFiles");
		
		//File paramFile = TestHelper.getTestFile("RandomForestFiles/satenstein-params-mixed.txt");
		
		
		//File expDir = new File("/ubc/cs/home/s/seramage/experiments/run0");
		
		File paramFile = new File("/ubc/cs/home/s/seramage/experiments/algorithms/cplex/cplex12-params-milp-mixed-cont-disc.txt");
		
		
		configSpace = ParamFileHelper.getParamFileParser(paramFile);
		
		
		
		
		try {
			//ilws = ProblemInstanceHelper.getInstances("cbmc_train_filename.txt", expDir.getAbsolutePath(),"cbmc_train_filename_feat.csv" , false);
			ilws = ProblemInstanceHelper.getInstances("instancelists/CL-scrambled-first1000.txt", "/ubc/cs/home/s/seramage/experiments/","instancelists/CL-scrambled-features.csv", false);
			
		} catch (IOException e) {
			
			throw new RuntimeException();
		}
	}
	
	/**
	 * Constructs a tree with splitMin 1, 
	 * fullTreeBootstram 1 and ensures that the variance predicted is zero.
	 */
	@Test
	public void testMarginalVarianceIsZero()
	{
		
		//SeedableRandomSingleton.setSeed(1344372857334l);
		RandomForestOptions rfOptions = new RandomForestOptions();
		
		//rfOptions.splitMin = 1;
		//rfOptions.fullTreeBootstrap = true;
		
		//File restoreDirectory = TestHelper.getTestFile("RandomForestFiles/state-run9");
		File restoreDirectory = new File("/ubc/cs/home/s/seramage/experiments/run0");
		assertTrue(restoreDirectory.exists());
		
		
		StateFactory sf = new LegacyStateFactory(null, restoreDirectory.getAbsolutePath());
		
		OverallObjective intraObjective = OverallObjective.MEAN;
		//Old iteration was 1106
		StateDeserializer sd = sf.getStateDeserializer("it", 1106, configSpace, ilws.getInstances(), dummyExecConfig,new NewRunHistory( OverallObjective.MEAN10,OverallObjective.MEAN, RunObjective.RUNTIME));
		
		
		
		
		//assertNotNull(sd.getIncumbent());	
		
		assertNotNull(sd.getRunHistory());
		
		List<ProblemInstance> instances = ilws.getInstances();
		RunHistory runHistory = sd.getRunHistory();
		
		//=== Actually build the model.
		
			
		//==== Format Data Properly
		
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
					SanitizedModelData sanitizedData = new PCAModelDataSanitizer(instanceFeatureMatrix, thetaMatrix, 7, runResponseValues, usedInstanceIdxs, true, runHistory.getParameterConfigurationInstancesRanByIndex(), runHistory.getCensoredEarlyFlagForRuns(), configSpace);
					
					
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
					if(predictions[0][1] > Math.pow(10, -13))
					{
						//DSutils.show(new RegTreeNode(0, mb.getRandomForest().Trees[2], configSpace, instances.subList(0,11), sd.getIncumbent(), sanitizedData.getPCAFeatures()), 180, 80);
						//DSutils.show(new RegTreeNode(0, preparedforest.Trees[2], configSpace, instances, sd.getIncumbent(),null), 180, 80);
						
						fail("Variance is " + predictions[0][1]);
						
					}

				
				
				
				
				//System.out.println("Hello");
		
	}
}
