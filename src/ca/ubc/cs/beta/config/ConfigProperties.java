package ca.ubc.cs.beta.config;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.PropertyDefiner;
import ch.qos.logback.core.status.Status;

public class ConfigProperties implements PropertyDefiner {
	
	private final String property;
	public ConfigProperties(String property)
	{
		this.property = property;
	}
	
	@Override
	public String getPropertyValue() {
		// TODO Auto-generated method stub
		return property; 
	}
	

	@Override
	public void addError(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addError(String arg0, Throwable arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addInfo(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addInfo(String arg0, Throwable arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addStatus(Status arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addWarn(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addWarn(String arg0, Throwable arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContext(Context arg0) {
		// TODO Auto-generated method stub

	}



}
