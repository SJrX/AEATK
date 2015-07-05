package ca.ubc.cs.beta.initializationProcedure;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.eventsystem.EventManager;
import ca.ubc.cs.beta.aeatk.initialization.classic.ClassicInitializationProcedure;
import ca.ubc.cs.beta.aeatk.initialization.classic.ClassicInitializationProcedureOptions;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParamFileHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.InstanceListWithSeeds;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceHelper;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;
import ca.ubc.cs.beta.aeatk.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.ThreadSafeRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.ThreadSafeRunHistoryWrapper;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.analytic.AnalyticFunctions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.analytic.AnalyticTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.analytic.AnalyticTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.analytic.AnalyticTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.functionality.OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.termination.TerminationCondition;
import ca.ubc.cs.beta.aeatk.termination.ValueMaxStatus;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Steve Ramage <seramage@cs.ubc.ca>
 *
 */
public class ClassicInitializationProcedureTester {

    /**
     * Related to bug #2118
     */
    @Test
    public void testInitializationProcedure()
    {

        String f = TestHelper.getTestFile("instanceFiles/classicFormatValid.txt").getAbsolutePath();
        String instanceFilesRoot =  TestHelper.getTestFile("featureFiles/sugar-csc09.csv").getParentFile().getParentFile().toString();

        boolean checkOnDisk = true;


        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        PrintStream old = System.out;
        System.setOut(new PrintStream(bout));

        try
        {
            InstanceListWithSeeds ilws = ProblemInstanceHelper.getInstances(f, instanceFilesRoot + File.separator + ((checkOnDisk) ? "instances/" : "no-instances/"), null, !checkOnDisk);

            ThreadSafeRunHistory trh = new ThreadSafeRunHistoryWrapper(new NewRunHistory());

            ParameterConfigurationSpace pcs = ParamFileHelper.getParamFileFromString("x0 [-5,10] [2.5]\nx1 [0,15] [7.5]\n");



            ClassicInitializationProcedureOptions opts = new ClassicInitializationProcedureOptions();


            AnalyticTargetAlgorithmEvaluatorFactory targetAlgorithmEvaluatorFactory = new AnalyticTargetAlgorithmEvaluatorFactory();
            AnalyticTargetAlgorithmEvaluatorOptions taeOpts = targetAlgorithmEvaluatorFactory.getOptionObject();

            taeOpts.cores = 1;
            taeOpts.func = AnalyticFunctions.BRANINS;


            TargetAlgorithmEvaluator tae;


            tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(targetAlgorithmEvaluatorFactory.getTargetAlgorithmEvaluator(taeOpts));

            TerminationCondition termCond = new TerminationCondition() {
                @Override
                public boolean haveToStop() {
                    return false;
                }

                @Override
                public Collection<ValueMaxStatus> currentStatus() {
                    return null;
                }

                @Override
                public void registerWithEventManager(EventManager evtManager) {

                }

                @Override
                public void notifyRun(AlgorithmRunResult run) {

                }

                @Override
                public double getTunerTime() {
                    return 0;
                }

                @Override
                public String getTerminationReason() {
                    return null;
                }

                @Override
                public double getWallTime() {
                    return 0;
                }
            };


            SeedableRandomPool pool = new SeedableRandomPool(0);

            AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("","",pcs,true,false,5);

            /**
             * Tests that the number of runs we use is as expected.
             */
            final int randomNumberOfRuns = 20;
            opts.initialIncumbentRuns = randomNumberOfRuns;

            ClassicInitializationProcedure cip = new ClassicInitializationProcedure(trh, pcs.getDefaultConfiguration(),tae,opts,ilws.getSeedGen(),ilws.getInstances(),2000,termCond,5,pool,false,execConfig);
            cip.run();

            assertEquals("Expected 20 runs to be have been created ", randomNumberOfRuns, trh.getAlgorithmRunDataExcludingRedundant().size());
            assertEquals("Expected 20 runs to be have been created ", randomNumberOfRuns, tae.getRunCount());


            /**
             * Since there are only 10 different instance seeds available
             * we expect it to be capped lower
             */
            final int deterministicNumberOfRuns = 10;

            ilws = ProblemInstanceHelper.getInstances(f, instanceFilesRoot + File.separator + ((checkOnDisk) ? "instances/" : "no-instances/"), null, !checkOnDisk,true);

            trh = new ThreadSafeRunHistoryWrapper(new NewRunHistory());


            tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(targetAlgorithmEvaluatorFactory.getTargetAlgorithmEvaluator(taeOpts));
            cip = new ClassicInitializationProcedure(trh, pcs.getDefaultConfiguration(),tae,opts,ilws.getSeedGen(),ilws.getInstances(),2000,termCond,5,pool,false,execConfig);
            cip.run();



            assertEquals("Expected 10 runs to be have been created ", deterministicNumberOfRuns, trh.getAlgorithmRunDataExcludingRedundant().size());
            assertEquals("Expected 10 runs to be have been created ", deterministicNumberOfRuns, tae.getRunCount());


            /**
             * Limit based on the maximum number of incumbent runs.
             */
            final int maxNumberOfRuns = 5;

            ilws = ProblemInstanceHelper.getInstances(f, instanceFilesRoot + File.separator + ((checkOnDisk) ? "instances/" : "no-instances/"), null, !checkOnDisk,true);

            trh = new ThreadSafeRunHistoryWrapper(new NewRunHistory());


            tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(targetAlgorithmEvaluatorFactory.getTargetAlgorithmEvaluator(taeOpts));
            cip = new ClassicInitializationProcedure(trh, pcs.getDefaultConfiguration(),tae,opts,ilws.getSeedGen(),ilws.getInstances(),maxNumberOfRuns,termCond,5,pool,false,execConfig);
            cip.run();



            assertEquals("Expected 5 to be have been created ", maxNumberOfRuns, trh.getAlgorithmRunDataExcludingRedundant().size());
            assertEquals("Expected 5 runs to be have been created ", maxNumberOfRuns, tae.getRunCount());


            /**
             * Test the default of 1
             */

            opts.initialIncumbentRuns = 1;

            ilws = ProblemInstanceHelper.getInstances(f, instanceFilesRoot + File.separator + ((checkOnDisk) ? "instances/" : "no-instances/"), null, !checkOnDisk,true);

            trh = new ThreadSafeRunHistoryWrapper(new NewRunHistory());


            tae = new OutstandingEvaluationsTargetAlgorithmEvaluatorDecorator(targetAlgorithmEvaluatorFactory.getTargetAlgorithmEvaluator(taeOpts));
            cip = new ClassicInitializationProcedure(trh, pcs.getDefaultConfiguration(),tae,opts,ilws.getSeedGen(),ilws.getInstances(),maxNumberOfRuns,termCond,5,pool,false,execConfig);
            cip.run();



            assertEquals("Expected 1 to be have been created ", opts.initialIncumbentRuns, trh.getAlgorithmRunDataExcludingRedundant().size());
            assertEquals("Expected 1 runs to be have been created ", opts.initialIncumbentRuns, tae.getRunCount());



        } catch(IOException e)
        {
            e.printStackTrace();
        }



    }
}
