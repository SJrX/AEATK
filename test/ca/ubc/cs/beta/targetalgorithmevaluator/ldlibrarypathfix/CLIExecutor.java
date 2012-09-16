package ca.ubc.cs.beta.targetalgorithmevaluator.ldlibrarypathfix;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.io.output.NullOutputStream;

import ca.ubc.cs.beta.TestHelper;
import ca.ubc.cs.beta.aclib.algorithmrun.AlgorithmRun;
import ca.ubc.cs.beta.aclib.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstance;
import ca.ubc.cs.beta.aclib.probleminstance.ProblemInstanceSeedPair;
import ca.ubc.cs.beta.aclib.runconfig.RunConfig;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.CommandLineTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ec.util.MersenneTwister;

public class CLIExecutor {

	public static void main(String[] args)
	{
		PrintStream out = System.out;
		//System.out.println("Suppressing Stream");
		//System.setOut(new PrintStream(new NullOutputStream()));
		//System.out.println("Suppressed Stream");
		File paramFile = TestHelper.getTestFile("paramFiles/paramEchoParamFile.txt");
		ParamConfigurationSpace configSpace = new ParamConfigurationSpace(paramFile, new MersenneTwister());

		for(Entry<String, String> ent : System.getenv().entrySet())
		{
			System.out.println(ent.getKey() + "=>" + ent.getValue());
		}
		
		
		StringBuilder b = new StringBuilder();
		
		String execString = TestHelper.getJavaExecString();
		
		execString += CLICallee.class.getCanonicalName();
		
		AlgorithmExecutionConfig execConfig = new AlgorithmExecutionConfig(execString, System.getProperty("user.dir"), configSpace, false, false, 500);

		TargetAlgorithmEvaluator tae = new CommandLineTargetAlgorithmEvaluator(execConfig, false);
		
		RunConfig rc = new RunConfig(new ProblemInstanceSeedPair(new ProblemInstance("SomeInstance"), 3), 42, configSpace.getRandomConfiguration());
		
		
		AlgorithmRun run = tae.evaluateRun(rc).get(0);
		System.setOut(out);
		System.out.println(run.getAdditionalRunData());

		
		
		
		
	}
	
	
}
