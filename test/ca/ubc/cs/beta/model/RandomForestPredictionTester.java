package ca.ubc.cs.beta.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import ca.ubc.cs.beta.aeatk.algorithmexecutionconfiguration.AlgorithmExecutionConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunconfiguration.AlgorithmRunConfiguration;
import ca.ubc.cs.beta.aeatk.algorithmrunresult.AlgorithmRunResult;
import ca.ubc.cs.beta.aeatk.example.statemerge.StateMergeModelBuilder;
import ca.ubc.cs.beta.aeatk.exceptions.DuplicateRunException;
import ca.ubc.cs.beta.aeatk.model.ModelBuildingOptions;
import ca.ubc.cs.beta.aeatk.objectives.OverallObjective;
import ca.ubc.cs.beta.aeatk.options.RandomForestOptions;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParamFileHelper;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfiguration.ParameterStringFormat;
import ca.ubc.cs.beta.aeatk.parameterconfigurationspace.ParameterConfigurationSpace;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aeatk.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aeatk.random.SeedableRandomPool;
import ca.ubc.cs.beta.aeatk.runhistory.FileSharingRunHistoryDecorator;
import ca.ubc.cs.beta.aeatk.runhistory.NewRunHistory;
import ca.ubc.cs.beta.aeatk.runhistory.RunHistory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.analytic.AnalyticFunctions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.analytic.AnalyticTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.base.analytic.AnalyticTargetAlgorithmEvaluatorOptions;
import ca.ubc.cs.beta.models.fastrf.RandomForest;

public class RandomForestPredictionTester {

	@Test
	public void testParabola()
	{
		
		double TWO_PI = 6.3;
		
		
		System.out.print("{");
		for(int i=0; i < 50; i++)
		{
			System.out.print(TWO_PI/50.0*i + ",");
		}
		System.out.println("}");
		ProblemInstance pi = new ProblemInstance("dummy",1);
		
		
		StateMergeModelBuilder mb = new StateMergeModelBuilder();
		
	
		RandomForestOptions rfOptions = new RandomForestOptions();
		
		rfOptions.fullTreeBootstrap = true;
		rfOptions.splitMin = 10;
		rfOptions.numTrees = 10;
		rfOptions.logModel = false;
		
		
		ModelBuildingOptions mbOptions = new ModelBuildingOptions();
		
		//ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("x0 r [0,6.3] [3.14]");
		
		//ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileFromString("x0 c {0.0,0.0063,0.0126,0.0189,0.0252,0.0315,0.0378,0.0441,0.0504,0.0567,0.063,0.0693,0.0756,0.0819,0.0882,0.0945,0.1008,0.1071,0.1134,0.1197,0.126,0.1323,0.1386,0.1449,0.1512,0.1575,0.1638,0.1701,0.1764,0.1827,0.189,0.1953,0.2016,0.2079,0.2142,0.2205,0.2268,0.2331,0.2394,0.2457,0.252,0.2583,0.2646,0.27090000000000003,0.2772,0.2835,0.2898,0.29610000000000003,0.3024,0.3087,0.315,0.32130000000000003,0.3276,0.3339,0.3402,0.34650000000000003,0.3528,0.3591,0.3654,0.37170000000000003,0.378,0.3843,0.3906,0.39690000000000003,0.4032,0.4095,0.4158,0.42210000000000003,0.4284,0.4347,0.441,0.44730000000000003,0.4536,0.4599,0.4662,0.47250000000000003,0.4788,0.4851,0.4914,0.49770000000000003,0.504,0.5103,0.5166,0.5229,0.5292,0.5355,0.5418000000000001,0.5481,0.5544,0.5607,0.567,0.5733,0.5796,0.5859,0.5922000000000001,0.5985,0.6048,0.6111,0.6174,0.6237,0.63,0.6363,0.6426000000000001,0.6489,0.6552,0.6615,0.6678,0.6741,0.6804,0.6867,0.6930000000000001,0.6993,0.7056,0.7119,0.7182,0.7245,0.7308,0.7371,0.7434000000000001,0.7497,0.756,0.7623,0.7686,0.7749,0.7812,0.7875,0.7938000000000001,0.8001,0.8064,0.8127,0.819,0.8253,0.8316,0.8379,0.8442000000000001,0.8505,0.8568,0.8631,0.8694,0.8757,0.882,0.8883,0.8946000000000001,0.9009,0.9072,0.9135,0.9198,0.9261,0.9324,0.9387,0.9450000000000001,0.9513,0.9576,0.9639,0.9702,0.9765,0.9828,0.9891,0.9954000000000001,1.0017,1.008,1.0143,1.0206,1.0269,1.0332,1.0395,1.0458,1.0521,1.0584,1.0647,1.071,1.0773,1.0836000000000001,1.0899,1.0962,1.1025,1.1088,1.1151,1.1214,1.1277,1.134,1.1403,1.1466,1.1529,1.1592,1.1655,1.1718,1.1781,1.1844000000000001,1.1907,1.197,1.2033,1.2096,1.2159,1.2222,1.2285,1.2348,1.2411,1.2474,1.2537,1.26,1.2663,1.2726,1.2789,1.2852000000000001,1.2915,1.2978,1.3041,1.3104,1.3167,1.323,1.3293,1.3356,1.3419,1.3482,1.3545,1.3608,1.3671,1.3734,1.3797,1.3860000000000001,1.3923,1.3986,1.4049,1.4112,1.4175,1.4238,1.4301,1.4364,1.4427,1.449,1.4553,1.4616,1.4679,1.4742,1.4805,1.4868000000000001,1.4931,1.4994,1.5057,1.512,1.5183,1.5246,1.5309,1.5372,1.5435,1.5498,1.5561,1.5624,1.5687,1.575,1.5813,1.5876000000000001,1.5939,1.6002,1.6065,1.6128,1.6191,1.6254,1.6317,1.638,1.6443,1.6506,1.6569,1.6632,1.6695,1.6758,1.6821,1.6884000000000001,1.6947,1.701,1.7073,1.7136,1.7199,1.7262,1.7325,1.7388,1.7451,1.7514,1.7577,1.764,1.7703,1.7766,1.7829,1.7892000000000001,1.7955,1.8018,1.8081,1.8144,1.8207,1.827,1.8333,1.8396,1.8459,1.8522,1.8585,1.8648,1.8711,1.8774,1.8837,1.8900000000000001,1.8963,1.9026,1.9089,1.9152,1.9215,1.9278,1.9341,1.9404,1.9467,1.953,1.9593,1.9656,1.9719,1.9782,1.9845,1.9908000000000001,1.9971,2.0034,2.0097,2.016,2.0223,2.0286,2.0349,2.0412,2.0475,2.0538,2.0601,2.0664,2.0727,2.079,2.0853,2.0916,2.0979,2.1042,2.1105,2.1168,2.1231,2.1294,2.1357,2.142,2.1483,2.1546,2.1609,2.1672000000000002,2.1735,2.1798,2.1861,2.1924,2.1987,2.205,2.2113,2.2176,2.2239,2.2302,2.2365,2.2428,2.2491,2.2554,2.2617,2.268,2.2743,2.2806,2.2869,2.2932,2.2995,2.3058,2.3121,2.3184,2.3247,2.331,2.3373,2.3436,2.3499,2.3562,2.3625,2.3688000000000002,2.3751,2.3814,2.3877,2.394,2.4003,2.4066,2.4129,2.4192,2.4255,2.4318,2.4381,2.4444,2.4507,2.457,2.4633,2.4696,2.4759,2.4822,2.4885,2.4948,2.5011,2.5074,2.5137,2.52,2.5263,2.5326,2.5389,2.5452,2.5515,2.5578,2.5641,2.5704000000000002,2.5767,2.583,2.5893,2.5956,2.6019,2.6082,2.6145,2.6208,2.6271,2.6334,2.6397,2.646,2.6523,2.6586,2.6649,2.6712,2.6775,2.6838,2.6901,2.6964,2.7027,2.709,2.7153,2.7216,2.7279,2.7342,2.7405,2.7468,2.7531,2.7594,2.7657,2.7720000000000002,2.7783,2.7846,2.7909,2.7972,2.8035,2.8098,2.8161,2.8224,2.8287,2.835,2.8413,2.8476,2.8539,2.8602,2.8665,2.8728,2.8791,2.8854,2.8917,2.898,2.9043,2.9106,2.9169,2.9232,2.9295,2.9358,2.9421,2.9484,2.9547,2.961,2.9673,2.9736000000000002,2.9799,2.9862,2.9925,2.9988,3.0051,3.0114,3.0177,3.024,3.0303,3.0366,3.0429,3.0492,3.0555,3.0618,3.0681,3.0744,3.0807,3.087,3.0933,3.0996,3.1059,3.1122,3.1185,3.1248,3.1311,3.1374,3.14,3.15,3.1563,3.1626,3.1689,3.1752000000000002,3.1815,3.1878,3.1941,3.2004,3.2067,3.213,3.2193,3.2256,3.2319,3.2382,3.2445,3.2508,3.2571,3.2634,3.2697,3.276,3.2823,3.2886,3.2949,3.3012,3.3075,3.3138,3.3201,3.3264,3.3327,3.339,3.3453,3.3516,3.3579,3.3642,3.3705,3.3768000000000002,3.3831,3.3894,3.3957,3.402,3.4083,3.4146,3.4209,3.4272,3.4335,3.4398,3.4461,3.4524,3.4587,3.465,3.4713,3.4776,3.4839,3.4902,3.4965,3.5028,3.5091,3.5154,3.5217,3.528,3.5343,3.5406,3.5469,3.5532,3.5595,3.5658,3.5721,3.5784000000000002,3.5847,3.591,3.5973,3.6036,3.6099,3.6162,3.6225,3.6288,3.6351,3.6414,3.6477,3.654,3.6603,3.6666,3.6729,3.6792,3.6855,3.6918,3.6981,3.7044,3.7107,3.717,3.7233,3.7296,3.7359,3.7422,3.7485,3.7548,3.7611,3.7674,3.7737,3.7800000000000002,3.7863,3.7926,3.7989,3.8052,3.8115,3.8178,3.8241,3.8304,3.8367,3.843,3.8493,3.8556,3.8619,3.8682,3.8745,3.8808,3.8871,3.8934,3.8997,3.906,3.9123,3.9186,3.9249,3.9312,3.9375,3.9438,3.9501,3.9564,3.9627,3.969,3.9753,3.9816000000000003,3.9879000000000002,3.9942,4.0005,4.0068,4.0131,4.0194,4.0257,4.032,4.0383000000000004,4.0446,4.0509,4.0572,4.0635,4.0698,4.0761,4.0824,4.0887,4.095,4.1013,4.1076,4.1139,4.1202,4.1265,4.1328,4.1391,4.1454,4.1517,4.158,4.1643,4.1706,4.1769,4.1832,4.1895,4.1958,4.2021,4.2084,4.2147,4.221,4.2273,4.2336,4.2399000000000004,4.2462,4.2525,4.2588,4.2651,4.2714,4.2777,4.284,4.2903,4.2966,4.3029,4.3092,4.3155,4.3218,4.3281,4.3344000000000005,4.3407,4.347,4.3533,4.3596,4.3659,4.3722,4.3785,4.3848,4.3911,4.3974,4.4037,4.41,4.4163,4.4226,4.4289,4.4352,4.4415000000000004,4.4478,4.4541,4.4604,4.4667,4.473,4.4793,4.4856,4.4919,4.4982,4.5045,4.5108,4.5171,4.5234,4.5297,4.536,4.5423,4.5486,4.5549,4.5612,4.5675,4.5738,4.5801,4.5864,4.5927,4.599,4.6053,4.6116,4.6179,4.6242,4.6305,4.6368,4.6431000000000004,4.6494,4.6557,4.662,4.6683,4.6746,4.6809,4.6872,4.6935,4.6998,4.7061,4.7124,4.7187,4.725,4.7313,4.7376000000000005,4.7439,4.7502,4.7565,4.7628,4.7691,4.7754,4.7817,4.788,4.7943,4.8006,4.8069,4.8132,4.8195,4.8258,4.8321,4.8384,4.8447000000000005,4.851,4.8573,4.8636,4.8699,4.8762,4.8825,4.8888,4.8951,4.9014,4.9077,4.914,4.9203,4.9266,4.9329,4.9392,4.9455,4.9518,4.9581,4.9644,4.9707,4.977,4.9833,4.9896,4.9959,5.0022,5.0085,5.0148,5.0211,5.0274,5.0337,5.04,5.0463000000000005,5.0526,5.0589,5.0652,5.0715,5.0778,5.0841,5.0904,5.0967,5.103,5.1093,5.1156,5.1219,5.1282,5.1345,5.1408000000000005,5.1471,5.1534,5.1597,5.166,5.1723,5.1786,5.1849,5.1912,5.1975,5.2038,5.2101,5.2164,5.2227,5.229,5.2353,5.2416,5.2479000000000005,5.2542,5.2605,5.2668,5.2731,5.2794,5.2857,5.292,5.2983,5.3046,5.3109,5.3172,5.3235,5.3298,5.3361,5.3424,5.3487,5.355,5.3613,5.3676,5.3739,5.3802,5.3865,5.3928,5.3991,5.4054,5.4117,5.418,5.4243,5.4306,5.4369,5.4432,5.4495000000000005,5.4558,5.4621,5.4684,5.4747,5.481,5.4873,5.4936,5.4999,5.5062,5.5125,5.5188,5.5251,5.5314,5.5377,5.5440000000000005,5.5503,5.5566,5.5629,5.5692,5.5755,5.5818,5.5881,5.5944,5.6007,5.607,5.6133,5.6196,5.6259,5.6322,5.6385,5.6448,5.6511000000000005,5.6574,5.6637,5.67,5.6763,5.6826,5.6889,5.6952,5.7015,5.7078,5.7141,5.7204,5.7267,5.733,5.7393,5.7456,5.7519,5.7582,5.7645,5.7708,5.7771,5.7834,5.7897,5.796,5.8023,5.8086,5.8149,5.8212,5.8275,5.8338,5.8401,5.8464,5.8527000000000005,5.859,5.8653,5.8716,5.8779,5.8842,5.8905,5.8968,5.9031,5.9094,5.9157,5.922,5.9283,5.9346,5.9409,5.9472000000000005,5.9535,5.9598,5.9661,5.9724,5.9787,5.985,5.9913,5.9976,6.0039,6.0102,6.0165,6.0228,6.0291,6.0354,6.0417,6.048,6.0543000000000005,6.0606,6.0669,6.0732,6.0795,6.0858,6.0921,6.0984,6.1047,6.111,6.1173,6.1236,6.1299,6.1362,6.1425,6.1488,6.1551,6.1614,6.1677,6.174,6.1803,6.1866,6.1929,6.1992,6.2055,6.2118,6.2181,6.2244,6.2307,6.237,6.2433,6.2496,6.2559000000000005,6.2622,6.2685,6.2748,6.2811,6.2874,6.2937} [3.14]");
		
		ParameterConfigurationSpace categoricalConfigSpace = ParamFileHelper.getParamFileFromString("x0 o {0.0,0.126,0.252,0.378,0.504,0.63,0.756,0.882,1.008,1.134,1.26,1.3860000000000001,1.512,1.638,1.764,1.8900000000000001,2.016,2.142,2.268,2.394,2.52,2.646,2.7720000000000002,2.898,3.024,3.15,3.276,3.402,3.528,3.654,3.7800000000000002,3.906,4.032,4.158,4.284,4.41,4.536,4.662,4.788,4.914,5.04,5.166,5.292,5.418,5.5440000000000005,5.67,5.796,5.922,6.048,6.174,} [3.15]");
		
		ParameterConfigurationSpace configSpace = ParamFileHelper.getParamFileParser("/home/sjr/git/SMAC-Java/sineplusone/sineplusone.pcs");
		//configSpace = categoricalConfigSpace;
		boolean adaptiveCapping = false;
		SeedableRandomPool pool = new SeedableRandomPool(System.currentTimeMillis());
		
		
		
		AnalyticTargetAlgorithmEvaluatorFactory fact = new AnalyticTargetAlgorithmEvaluatorFactory();
		
		AnalyticTargetAlgorithmEvaluatorOptions options = fact.getOptionObject();
		
		options.func = AnalyticFunctions.SINEPLUSONE;
		TargetAlgorithmEvaluator tae = fact.getTargetAlgorithmEvaluator(options);
		
		AlgorithmExecutionConfiguration execConfig = new AlgorithmExecutionConfiguration("test", "/home/sjr/git/SMAC-Java/deployables", configSpace,false, true, 2);
		
		Random rand = pool.getRandom("runs");
		
		RunHistory rh = new NewRunHistory();
		
		rh = new FileSharingRunHistoryDecorator(rh, new File("/home/sjr/git/SMAC-Java/sineplusone/smac-output"), 1000, Collections.singletonList(pi), 1000, false);
		
		List<AlgorithmRunConfiguration> runs = new ArrayList<>();
		for(int i=0; i < 100000; i++)
		{
			AlgorithmRunConfiguration runConfig = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pi, -1),5, configSpace.getRandomParameterConfiguration(rand), execConfig);
			runs.add(runConfig);
		}
		
		ParameterConfiguration config = configSpace.getDefaultConfiguration();
		config.put("x0", String.valueOf(Math.PI * 3.0 / 2.0));
		
		
		
		AlgorithmRunConfiguration runConfig2 = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pi, -1),5, config, execConfig);
		runs.add(runConfig2);
		
		
		List<AlgorithmRunResult> results = tae.evaluateRun(runs);
		
		Set<String> ts = new TreeSet<String>();
		
		System.out.println(results.size());
		for(AlgorithmRunResult run : results)
		{
			ts.add(run.getAlgorithmRunConfiguration().getParameterConfiguration().getFormattedParameterString() + "=>" + run.getRuntime() + "," );
			
			try {
				rh.append(run);
			} catch (DuplicateRunException e) {
				//Test
				//e.printStackTrace();
			}
		}
		
		for(String s : ts)
		{
			System.out.println(s);
		}
		
		
		System.out.println("Building model");
		mb.learnModel(Collections.singletonList(pi), rh, configSpace, rfOptions, mbOptions, 2, OverallObjective.MEAN, adaptiveCapping, pool);
		RandomForest rf = mb.getPreparedForest();
		
		System.out.println("Done Building Model");
		
		int[] treeIndxsUsed = {0};
		
		
		
		
	
		System.out.println("x,y,y^");
		Set<String> lines = new TreeSet<String>();
		for(int i=0; i < 1000; i++)
		{
			
			double[][] Theta = new double[1][1];

			AlgorithmRunConfiguration runConfig = new AlgorithmRunConfiguration(new ProblemInstanceSeedPair(pi, -1),5, configSpace.getParameterConfigurationFromString("-x0 '"+categoricalConfigSpace.getRandomParameterConfiguration(rand).get("x0")+"'", ParameterStringFormat.NODB_SYNTAX), execConfig);
			
			Theta[0] = runConfig.getParameterConfiguration().toValueArray();
			
			double[][] responseValues = RandomForest.applyMarginal(rf, treeIndxsUsed, Theta);
			
			//System.out.println(runConfig.getParameterConfiguration().toValueArray()[0] + "=>" + Theta[0][0]);
			
			//lines.add(runConfig.getParameterConfiguration().get("x0")+ "," + tae.evaluateRun(runConfig).get(0).getRuntime() + "," + responseValues[0][0]); 
			
			lines.add(runConfig.getParameterConfiguration().get("x0")+ "," + tae.evaluateRun(runConfig).get(0).getRuntime() + "," + responseValues[0][0]);
		}
		
		for(String line : lines)
		{
			System.out.println(line);
		}
		//System.out.println(RandomForest.apply(rf, X)[0][0]);
		
		
		
		
		
		
		
		
		
		
		
	
		/*
		 * 	
				int[] tree_indxs_used = new int[10];
				for(int i=0; i < smo.rfo.numTrees; i++)
				{
					tree_indxs_used[i]= i;
				}
				
				double[][] Theta = new double[1][];
				
			
				double bestMean = Double.POSITIVE_INFINITY;
				
				for(ParameterConfiguration config : maxConfigs)
				{
					Theta[0] = config.toValueArray();
					
					double[][] ypred = ;
					
					log.trace("Incumbent {} has predicted mean {}", config, ypred[0]);
					if(ypred[0][0] < bestMean)
					{
						newIncumbent = config;
						bestMean = ypred[0][0];
					}
					
					
				}
		 */
		
	}
}
