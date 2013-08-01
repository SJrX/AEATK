package ca.ubc.cs.beta.aclib.misc.options;

public enum OptionLevel {

	//I didn't use an ordinal values, since they are very sensitive to ordering.
	//The values here don't mean anything they just assign order
	BASIC(0),
	INTERMEDIATE(1),
	ADVANCED(2),
	DEVELOPER(3);
	
	
	private final int level;
	OptionLevel(int level)
	{
		this.level = level;
	}
	
	public boolean higher(OptionLevel b)
	{
		return this.level > b.level;
	}
	
	
	
}
