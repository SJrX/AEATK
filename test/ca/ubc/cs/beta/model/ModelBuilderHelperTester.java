package ca.ubc.cs.beta.model;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.ExistingAlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aeatk.misc.debug.DebugUtil;
import ca.ubc.cs.beta.aeatk.model.ModelBuildingOptions;
import ca.ubc.cs.beta.aeatk.model.builder.BasicModelBuilder;
import ca.ubc.cs.beta.aeatk.model.builder.ModelBuilder;
import ca.ubc.cs.beta.aeatk.model.data.PCAModelDataSanitizer;
import ca.ubc.cs.beta.aeatk.model.helper.ModelBuilderHelper;
import ca.ubc.cs.beta.aeatk.options.RandomForestOptions;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParamFileHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;
import ca.ubc.cs.beta.aeatk.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistoryHelper;
import ca.ubc.cs.beta.models.fastrf.RandomForest;
import ec.util.MersenneTwister;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


/**
 * Created by Steve Ramage <seramage@cs.ubc.ca> on 11/15/15
 */
public class ModelBuilderHelperTester {

    private static final SeedableRandomPool pool = new SeedableRandomPool(System.currentTimeMillis());

    @AfterClass
    public static void afterClass()
    {
        pool.logUsage();
    }


    @Test
    public void testCategoricalInOperator()
    {

        String pcsFile = "independent categorical { 0, 1} [0]\n dependent categorical { 0, 1, 2, 3, 4, 5, 6, 7 , 8 ,9 } [0] \n dependent | independent in { 1 }";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }

    @Test
    public void testCategoricalEqualOperator()
    {

        String pcsFile = "independent categorical { 0, 1} [0]\n dependent categorical { 0, 1, 2, 3, 4, 5, 6, 7 , 8 ,9 } [0] \n dependent | independent == 1 ";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }

    @Test
    public void testCategoricalNotEqualOperator()
    {

        String pcsFile = "independent categorical { 0, 1} [0]\n dependent categorical { 0, 1, 2, 3, 4, 5, 6, 7 , 8 ,9 } [0] \n dependent | independent != 0 ";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }

    @Test
    public void testOrdinalInOperator()
    {

        String pcsFile = "independent ordinal { 0, 1} [0]\n dependent ordinal { 0, 1, 2, 3, 4, 5, 6, 7 , 8 ,9 } [0] \n dependent | independent in { 1 }";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }


    @Test
    public void testOrdinalEqualOperator()
    {

        String pcsFile = "independent ordinal { 0, 1} [0]\n dependent ordinal { 0, 1, 2, 3, 4, 5, 6, 7 , 8 ,9 } [0] \n dependent | independent == 1 ";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }

    @Test
    public void testOrdinalNotEqualOperator()
    {

        String pcsFile = "independent ordinal { 0, 1} [0]\n dependent ordinal { 0, 1, 2, 3, 4, 5, 6, 7 , 8 ,9 } [0] \n dependent | independent != 0 ";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }

    @Test
    public void testOrdinalGreaterThanOperator()
    {

        String pcsFile = "independent ordinal { 0, 1} [0]\n dependent ordinal { 0, 1, 2, 3, 4, 5, 6, 7 , 8 ,9 } [0] \n dependent | independent > 0 ";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }

    @Test
    public void testIntegerInOperator()
    {

        String pcsFile = "independent integer [0,1] [0]\n dependent integer [0,9] [0] \n dependent | independent in { 1 }";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }


    @Test
    public void testIntegerEqualOperator()
    {

        String pcsFile = "independent integer [0,1] [0]\n dependent integer [0,9] [0] \n dependent | independent == 1 ";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }

    @Test
    public void testIntegerNotEqualOperator()
    {

        String pcsFile = "independent integer [0,1] [0]\n dependent integer [0,9] [0] \n dependent | independent != 0 ";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }

    @Test
    public void testIntegerGreaterThanOperator()
    {

        String pcsFile = "independent integer [0,1] [0]\n dependent integer [0,9] [0] \n dependent | independent > 0 ";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }


    @Test
    public void testRealInOperator()
    {

        String pcsFile = "independent real [0,1] [0]\n dependent real [0,9] [0] \n dependent | independent in { 1 }";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }


    @Test
    public void testRealEqualOperator()
    {

        String pcsFile = "independent real [0,1] [0]\n dependent real [0,9] [0] \n dependent | independent == 1 ";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }

    @Test
    public void testRealNotEqualOperator()
    {

        String pcsFile = "independent real [0,1] [0]\n dependent real [0,9] [0] \n dependent | independent != 0 ";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }

    @Test
    public void testRealrGreaterThanOperator()
    {

        String pcsFile = "independent real [0,1] [0]\n dependent real [0,9] [0] \n dependent | independent > 0 ";
        buildModelAndValidatedPCSAndActive(true, pcsFile);
        buildModelAndValidatedPCSAndActive(false, pcsFile);
    }

    private void buildModelAndValidatedPCSAndActive(boolean active, String pcsFile) {
        ParameterConfigurationSpace pcs = ParamFileHelper.getParamFileFromString(pcsFile);

        List<ParameterConfiguration> configs = new ArrayList<>();

        for(int i=0; i< 10; i++)
        {
            ParameterConfiguration config = pcs.getDefaultConfiguration();
            config.put("independent", active?"1":"0");
            config.put("dependent", String.valueOf(i));
            configs.add(config);
        }


        double[][] allX = getXArray(configs);
        System.out.println(Arrays.deepToString(allX));
        RandomForest forest = doBuild(pcs, active);


        double[][] result = RandomForest.apply(forest, allX);

        for(int i=0; i < result.length; i++)
        {
            assertEquals("Config " +configs.get(i).getFormattedParameterString() + " should have prediction ~" + i,active?Double.valueOf(configs.get(i).get("dependent")):4.5, result[i][0], 0.00001);
        }
    }


    public double[][] getXArray(List<ParameterConfiguration> configs)
    {

        //We basically need a 2d array of value array, but an extra column for the feature vector.
        double[][] X = new double[10][configs.get(0).toValueArray().length + 1];
        for(int i=0;i < configs.size(); i++)
        {

            for(int j = 0; j < configs.get(0).toValueArray().length; j++)
            {
                X[i][j] = configs.get(i).toValueArray()[j];
            }

        }

        return X;
    }

    public RandomForest doBuild(ParameterConfigurationSpace pcs, boolean independentActive)
    {

        RunHistory rh = new NewRunHistory();



        AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("foo","bar",pcs, false, false, 500);


        ProblemInstance pi = new ProblemInstance("foo",1);


        for(int i=0; i < 10; i++)
        {
            ParameterConfiguration config = pcs.getDefaultConfiguration();

            config.put("dependent", String.valueOf(i));
            config.put("independent", independentActive ? "1":"0");

            ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, i);

            AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(pisp, config, execConfig );


            System.out.println(config.getFormattedParameterString());

            try {
                rh.append(new ExistingAlgorithmRunResult(rc, RunStatus.SAT, i, 0, -1, -1, "", 0));
            } catch(DuplicateRunException e)
            {
                e.printStackTrace();;
                fail("Shouldn't have happened");
            }

        }

        ParameterConfiguration config = pcs.getDefaultConfiguration();

        config.put("independent",independentActive?"0":"1");
        config.put("dependent","9");

        System.out.println(config.getFormattedParameterString());
        ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, 100);
        AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(pisp, config, execConfig );
        try
        {
            rh.append(new ExistingAlgorithmRunResult(rc, RunStatus.SAT, 100 , 0, -1 , -1, "" ,0));
        } catch(DuplicateRunException e)
        {
            e.printStackTrace();;
            fail("Shouldn't have happened");
        }


        ModelBuildingOptions mbOptions = new ModelBuildingOptions();

        RandomForestOptions rfOptions = new RandomForestOptions();

        rfOptions.logModel = false;
        rfOptions.numTrees = 1;
        rfOptions.splitMin = 1;
        rfOptions.ratioFeatures = 1;

        RandomForest forest = ModelBuilderHelper.getModelBuilder(rh,pcs,Collections.singletonList(pi),mbOptions,rfOptions, pool.getRandom(DebugUtil.getCurrentMethodName()),false,0,false,1).getRandomForest();

        System.out.println(Arrays.deepToString(RandomForest.apply(forest,new double[][]{ {1,1,0}})));
        System.out.println("Hello");

        System.out.println("Tree size:" + forest.Trees[0].node.length);

        return forest;
    }
}
