package ca.ubc.cs.beta.aclib.misc.version;

import org.mangosdk.spi.ProviderFor;


@ProviderFor(VersionInfo.class)
public class JavaVersionInfo extends AbstractVersionInfo {

	public JavaVersionInfo() 
	{
		super("JRE", System.getProperty("java.vm.name") + " (" + System.getProperty("java.version") + ")", false);
	}

}
