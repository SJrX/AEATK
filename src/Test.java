import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.ExistingAlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.RunStatus;
import ca.ubc.cs.beta.aeatk.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aeatk.model.builder.BasicModelBuilder;
import ca.ubc.cs.beta.aeatk.model.builder.ModelBuilder;
import ca.ubc.cs.beta.aeatk.model.data.PCAModelDataSanitizer;
import ca.ubc.cs.beta.aeatk.options.RandomForestOptions;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParamFileHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistoryHelper;
import ca.ubc.cs.beta.models.fastrf.RandomForest;
import ec.util.MersenneTwister;

import java.util.*;

/**
 * Created by Steve Ramage <seramage@cs.ubc.ca> on 11/15/15
 */
public class Test {

    public static void main(String[] args) throws DuplicateRunException {



        doBuild(true, true);

        doBuild(false,true);
        doBuild(false,false);

    }

    private static void doBuild(boolean bActive, boolean conditional) throws DuplicateRunException {



        RunHistory rh = new NewRunHistory();

        String pcsFile = "a { 0, 1 } [0]\nb { 0 ,1 ,2 , 3 , 4 ,5 ,6, 7, 8, 9} [0]\n" + (conditional?"b | a in {1}\n":"");

        ParameterConfigurationSpace pcs = ParamFileHelper.getParamFileFromString(pcsFile);


        AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("foo","bar",pcs, false, false, 500);


        ProblemInstance pi = new ProblemInstance("foo",1);


        for(int i=0; i < 10; i++)
        {
            ParameterConfiguration config = pcs.getDefaultConfiguration();
            config.put("a",bActive?"1":"0");
            config.put("b", String.valueOf(i));

            ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, i);

            AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(pisp, config, execConfig );


            System.out.println(config.getFormattedParameterString());

            try {
                rh.append(new ExistingAlgorithmRunResult(rc, RunStatus.SAT, (i % 2 == 0) ? 0.75 : 0.5, 0, -1, -1, "", 0));
            } catch(DuplicateRunException e)
            {

                e.printStackTrace();
            }




        }

        ParameterConfiguration config = pcs.getDefaultConfiguration();

        config.put("a",bActive?"0":"1");
        config.put("b","0");

        System.out.println(config.getFormattedParameterString());
        ProblemInstanceSeedPair pisp = new ProblemInstanceSeedPair(pi, 100);
        AlgorithmRunConfiguration rc = new AlgorithmRunConfiguration(pisp, config, execConfig );
        rh.append(new ExistingAlgorithmRunResult(rc, RunStatus.SAT, 0.1 , 0, -1 , -1, "" ,0));


        //=== The following two sets are required to be sorted by instance and paramConfig ID.
        Set<ProblemInstance> all_instances = new LinkedHashSet<ProblemInstance>();
        all_instances.add(pisp.getProblemInstance());
        Set<ParameterConfiguration> paramConfigs = rh.getUniqueParamConfigurations();

        Set<ProblemInstance> runInstances=rh.getUniqueInstancesRan();
        ArrayList<Integer> runInstancesIdx = new ArrayList<Integer>(all_instances.size());

        //=== Get the instance feature matrix (X).
        int i=0;
        double[][] instanceFeatureMatrix = new double[all_instances.size()][];


        if(runInstances.contains(pi))
        {
            runInstancesIdx.add(i);
        }
        instanceFeatureMatrix[i] = pi.getFeaturesDouble();

        System.out.println("Instance Feature Matrix: \n"+  Arrays.deepToString(instanceFeatureMatrix) + "\n");


        //=== Get the parameter configuration matrix (Theta).
        double[][] thetaMatrix = new double[paramConfigs.size()][];
        i = 0;
        for(ParameterConfiguration pc : paramConfigs)
        {
            {
                thetaMatrix[i++] = pc.toValueArray();
            }
        }


        System.out.println("Theta Matrix: \n"+  Arrays.deepToString(thetaMatrix) + "\n");


        //=== Get an array of the order in which instances were used (TODO: same for Theta, from ModelBuilder)
        int[] usedInstanceIdxs = new int[runInstancesIdx.size()];
        for(int j=0; j <  runInstancesIdx.size(); j++)
        {
            usedInstanceIdxs[j] = runInstancesIdx.get(j);
        }


        List<AlgorithmRunResult> runs = rh.getAlgorithmRunsExcludingRedundant();
        double[] runResponseValues = RunHistoryHelper.getRunResponseValues(runs, rh.getRunObjective());

        System.out.println("Run Response Values: " + Arrays.toString(runResponseValues));
        boolean[] censored = RunHistoryHelper.getCensoredEarlyFlagForRuns(runs);




		/*
		System.out.println(Arrays.deepToString(pcs.getNameConditionsMapOp().get(1)));
		System.out.println(Arrays.deepToString(pcs.getNameConditionsMapParentsArray().get(1)));
		System.out.println(Arrays.deepToString(pcs.getNameConditionsMapParentsValues().get(1)));

		System.out.println(Arrays.toString(pcs.getDefaultConfiguration().toValueArray()));
		*/
        int numPCA = 0;

        //=== Sanitize the data.
        PCAModelDataSanitizer sanitizedData = new PCAModelDataSanitizer(instanceFeatureMatrix, thetaMatrix, numPCA, runResponseValues, false, rh.getParameterConfigurationInstancesRanByIndexExcludingRedundant(), censored, execConfig.getParameterConfigurationSpace());

        //=== Actually build the model.
        ModelBuilder mb;

        RandomForestOptions rfOptions = new RandomForestOptions();

        rfOptions.numTrees = 1;
        rfOptions.splitMin = 1;


        mb = new BasicModelBuilder(sanitizedData, rfOptions,2, new MersenneTwister(1));

        RandomForest tree = mb.getRandomForest();

        System.out.println("Hello");

        System.out.println("Tree size:" + tree.Trees[0].node.length);
    }

}
