package ca.ubc.cs.beta.models.surrogate.helpers.csv;


public class CSVHelpers {
	public final ConfigCSVFileHelper configs, features, matrix;
	
	public CSVHelpers(ConfigCSVFileHelper configs, ConfigCSVFileHelper features, ConfigCSVFileHelper matrix)
	{
		this.configs = configs;
		this.features = features;
		this.matrix = matrix;
	}
}
