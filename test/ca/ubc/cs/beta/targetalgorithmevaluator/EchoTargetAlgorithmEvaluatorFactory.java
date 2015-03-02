package ca.ubc.cs.beta.targetalgorithmevaluator;

import org.mangosdk.spi.ProviderFor;

import ca.ubc.cs.beta.aeatk.options.AbstractOptions;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.AbstractTargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.TargetAlgorithmEvaluatorFactory;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.functionality.SimulatedDelayTargetAlgorithmEvaluatorDecorator;
import ca.ubc.cs.beta.aeatk.targetalgorithmevaluator.decorators.resource.BoundedTargetAlgorithmEvaluator;

@ProviderFor(TargetAlgorithmEvaluatorFactory.class)
public class EchoTargetAlgorithmEvaluatorFactory extends AbstractTargetAlgorithmEvaluatorFactory  {

	@Override
	public String getName() {
		return "PARAMECHO";
	}

	@Override
	public TargetAlgorithmEvaluator getTargetAlgorithmEvaluator(AbstractOptions options) {
		
		EchoTargetAlgorithmEvaluatorOptions eOpt = (EchoTargetAlgorithmEvaluatorOptions) options;
		TargetAlgorithmEvaluator tae =  new EchoTargetAlgorithmEvaluator( eOpt);
		
		if(( (EchoTargetAlgorithmEvaluatorOptions) options).cores > 0)
		{
			tae = new SimulatedDelayTargetAlgorithmEvaluatorDecorator(tae,eOpt.observerFrequency,1);
			tae = new BoundedTargetAlgorithmEvaluator(tae, eOpt.cores);
		}
		
		return tae;
	}

	@Override
	public AbstractOptions getOptionObject() {
		return new EchoTargetAlgorithmEvaluatorOptions();
	}

}
