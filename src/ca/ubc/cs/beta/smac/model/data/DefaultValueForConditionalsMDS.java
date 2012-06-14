package ca.ubc.cs.beta.smac.model.data;

import ca.ubc.cs.beta.configspace.ParamConfigurationSpace;

public class DefaultValueForConditionalsMDS extends RawSanitizedModelData {

	public DefaultValueForConditionalsMDS(double[][] instanceFeatures,
			double[][] paramValues, double[] responseValues,
			int[] usedInstancesIdxs, boolean logModel,
			ParamConfigurationSpace configSpace) {
		super(instanceFeatures, paramValues, responseValues, usedInstancesIdxs, logModel, configSpace);

		double[] defaultValues = configSpace.getDefaultConfiguration().toValueArray();
		
		//=== Replace NaNs by default values.
		for (int i = 0; i < paramValues.length; i++) {
			for (int j = 0; j < defaultValues.length; j++) {
				if (Double.isNaN(paramValues[i][j])){
					this.configs[i][j] = defaultValues[j]; // TODO: Steve will fix this, so there is no need for "this." anymore. 
				}
			}
		}
	}
	
	//=== This one is for calling by Matlab.
	public DefaultValueForConditionalsMDS(double[][] instanceFeatures,
			double[][] paramValues, double[] responseValues,
			int[] usedInstancesIdxs, boolean logModel,
			double[] defaultValues) {
		super(instanceFeatures, paramValues, responseValues, usedInstancesIdxs, logModel);

		//=== Replace NaNs by default values.
		for (int i = 0; i < paramValues.length; i++) {
			for (int j = 0; j < defaultValues.length; j++) {
				if (Double.isNaN(paramValues[i][j])){
					this.configs[i][j] = defaultValues[j]; // TODO: Steve will fix this, so there is no need for "this." anymore. 
				}
			}
		}
	}

}
