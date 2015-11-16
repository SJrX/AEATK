package ca.ubc.cs.beta.aeatk.model.helper;

import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.model.ModelBuildingOptions;
import ca.ubc.cs.beta.aeatk.model.builder.AdaptiveCappingModelBuilder;
import ca.ubc.cs.beta.aeatk.model.builder.BasicModelBuilder;
import ca.ubc.cs.beta.aeatk.model.builder.ModelBuilder;
import ca.ubc.cs.beta.aeatk.model.data.MaskCensoredDataAsUncensored;
import ca.ubc.cs.beta.aeatk.model.data.MaskInactiveConditionalParametersWithDefaults;
import ca.ubc.cs.beta.aeatk.model.data.PCAModelDataSanitizer;
import ca.ubc.cs.beta.aeatk.model.data.SanitizedModelData;
import ca.ubc.cs.beta.aeatk.options.RandomForestOptions;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistoryHelper;

import java.util.*;

/**
 * Created by Steve Ramage <seramage@cs.ubc.ca> on 11/15/15
 */
public class ModelBuilderHelper {

    public static ModelBuilder getModelBuilder(RunHistory runHistory, ParameterConfigurationSpace configSpace, List<ProblemInstance> instances, ModelBuildingOptions mbOptions, RandomForestOptions randomForestOptions, Random randomForestBuildingPRNG, boolean adaptiveCapping, int numPCA, boolean logModel, double subsamplePercentage) {
        //=== The following two sets are required to be sorted by instance and paramConfig ID.
        Set<ProblemInstance> all_instances = new LinkedHashSet<ProblemInstance>(instances);
        Set<ParameterConfiguration> paramConfigs = runHistory.getUniqueParamConfigurations();

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
        for(ParameterConfiguration pc : paramConfigs)
        {
            if(mbOptions.maskInactiveConditionalParametersAsDefaultValue)
            {
                thetaMatrix[i++] = pc.toComparisonValueArray();
            } else
            {
                thetaMatrix[i++] = pc.toValueArray();
            }
        }

        //=== Get an array of the order in which instances were used (TODO: same for Theta, from ModelBuilder)
        int[] usedInstanceIdxs = new int[runInstancesIdx.size()];
        for(int j=0; j <  runInstancesIdx.size(); j++)
        {
            usedInstanceIdxs[j] = runInstancesIdx.get(j);
        }


        List<AlgorithmRunResult> runs = runHistory.getAlgorithmRunsExcludingRedundant();
        double[] runResponseValues = RunHistoryHelper.getRunResponseValues(runs, runHistory.getRunObjective());
        boolean[] censored = RunHistoryHelper.getCensoredEarlyFlagForRuns(runs);

        if(mbOptions.maskCensoredDataAsKappaMax)
        {
            for(int j=0; j < runResponseValues.length; j++)
            {
                if(censored[j])
                {
                    runResponseValues[j] = runHistory.getAlgorithmExecutionConfiguration().getAlgorithmMaximumCutoffTime();
                }
            }
        }


        for(int j=0; j < runResponseValues.length; j++)
        { //=== Not sure if I Should be penalizing runs prior to the model
            // but matlab sure does

            switch(runHistory.getRunObjective())
            {
                case RUNTIME:
                    if(runResponseValues[j] >=  runHistory.getAlgorithmExecutionConfiguration().getAlgorithmMaximumCutoffTime())
                    {
                        runResponseValues[j] = runHistory.getAlgorithmExecutionConfiguration().getAlgorithmMaximumCutoffTime() * runHistory.getIntraInstanceObjective().getPenaltyFactor();
                    }
                    break;
                case QUALITY:

                    break;
                default:
                    throw new IllegalArgumentException("Not sure what objective this is: " + runHistory.getRunObjective());
            }

        }

        //=== Sanitize the data.
        SanitizedModelData sanitizedData = new PCAModelDataSanitizer(instanceFeatureMatrix, thetaMatrix, numPCA, runResponseValues, logModel, runHistory.getParameterConfigurationInstancesRanByIndexExcludingRedundant(), censored, configSpace);


        if(mbOptions.maskCensoredDataAsUncensored)
        {
            sanitizedData = new MaskCensoredDataAsUncensored(sanitizedData);
        }

        if(mbOptions.maskInactiveConditionalParametersAsDefaultValue)
        {
            sanitizedData = new MaskInactiveConditionalParametersWithDefaults(sanitizedData, configSpace);
        }

        //=== Actually build the model.
        ModelBuilder mb;

        if(adaptiveCapping)
        {
            mb = new AdaptiveCappingModelBuilder(sanitizedData,randomForestOptions, randomForestBuildingPRNG, mbOptions.imputationIterations, runHistory.getAlgorithmExecutionConfiguration().getAlgorithmMaximumCutoffTime(), runHistory.getIntraInstanceObjective().getPenaltyFactor(), subsamplePercentage);
        } else
        {
            mb = new BasicModelBuilder(sanitizedData, randomForestOptions,subsamplePercentage, randomForestBuildingPRNG);
        }
        return mb;
    }
}
