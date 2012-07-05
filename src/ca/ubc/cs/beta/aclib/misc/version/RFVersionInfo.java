package ca.ubc.cs.beta.aclib.misc.version;

import org.mangosdk.spi.ProviderFor;

@ProviderFor(VersionInfo.class)
public class RFVersionInfo extends AbstractVersionInfo {
	public RFVersionInfo()
	{
		super("Random Forest Library", "fastrf-version.txt", true);
	}
}
