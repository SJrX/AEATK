
package ca.ubc.cs.beta.aclib.example.tae;

import org.mangosdk.spi.ProviderFor;

import ca.ubc.cs.beta.aclib.misc.version.AbstractVersionInfo;
import ca.ubc.cs.beta.aclib.misc.version.VersionInfo;

//This annotation generates the required files necessary for 
//SPI to work. See the Manual for more info on SPI.
//It is commented out because we don't actually want it to display when running 
//since this example is included in ACLib

//@ProviderFor(VersionInfo.class)
public class TargetAlgorithmEvaluatorRunnerVersionInfo extends AbstractVersionInfo {

	public TargetAlgorithmEvaluatorRunnerVersionInfo() {
		super("Target Algorithm Evaluator Runner Example", "Version 1.0", false);
	}

}
