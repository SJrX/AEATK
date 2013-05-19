package ca.ubc.cs.beta.aclib.targetalgorithmevaluator.random;

import org.mangosdk.spi.ProviderFor;

import ca.ubc.cs.beta.aclib.execconfig.AlgorithmExecutionConfig;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.BoundedTargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.decorators.SimulatedDelayTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aclib.targetalgorithmevaluator.factory.TargetAlgorithmEvaluatorFactory;

@ProviderFor(TargetAlgorithmEvaluatorFactory.class)
public class RandomResponseTargetAlgorithmEvaluatorFactory implements
		TargetAlgorithmEvaluatorFactory {

	
	@Override
	public String getName() {
		return "RANDOM";
	}

	@Override
	public TargetAlgorithmEvaluator getTargetAlgorithmEvaluator(
			AlgorithmExecutionConfig execConfig, AbstractOptions options) {
		RandomResponseTargetAlgorithmEvaluatorOptions randomOptions = (RandomResponseTargetAlgorithmEvaluatorOptions) options;
		
		TargetAlgorithmEvaluator tae =  new RandomResponseTargetAlgorithmEvaluator(execConfig,randomOptions);
		
		if(randomOptions.simulateDelay)
		{
			tae = new SimulatedDelayTargetAlgorithmEvaluatorDecorator(tae, randomOptions.observerFrequency);
		}
		
		if(randomOptions.cores > 0)
		{
			tae = new BoundedTargetAlgorithmEvaluator(tae, randomOptions.cores, execConfig);
		}
		
		return tae;
		
	}

	@Override
	public RandomResponseTargetAlgorithmEvaluatorOptions getOptionObject() {
		return new RandomResponseTargetAlgorithmEvaluatorOptions();
	}
	
	

}
