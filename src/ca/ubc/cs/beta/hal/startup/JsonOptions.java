package ca.ubc.cs.beta.hal.startup;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;

import ca.ubc.cs.beta.aclib.misc.jcommander.validator.FixedPositiveInteger;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;
import ca.ubc.cs.beta.aclib.options.MySQLOptions;

@UsageTextField(title="JSON Options", description="This specifies options for generating the HAL JSON file")
public class JsonOptions extends AbstractOptions {
	
	public enum DbType{
		MYSQL,
		SQLITE
	};
		
	@Parameter(names="--db", description = "The database containing the HAL data")
	public DbType database;
	
	@Parameter(names="--sqliteFile", description = "The file containging the SQLite DB")
	public String sqliteFile = null;
	
	@Parameter(names="--serverport", description="The port where HAL will run", validateWith=FixedPositiveInteger.class)
	public int serverport = 8080;
	
	@Parameter(names="--reload", description="Specifies whether imports are reloaded on startup")
	public boolean reload = false;
	
	@ParametersDelegate
	public MySQLOptions mysqlConf = new MySQLOptions();
	
	public String getJDBCString()
	{
		switch(this.database){
			case MYSQL:
			{
				if(this.mysqlConf.databaseName==null)
					throw new ParameterException("Must supply argument --mysqlDatabase in order to overwrite JSON file.");
				return "jdbc:mysql://"+this.mysqlConf.username+"@"+this.mysqlConf.host+":"+this.mysqlConf.port+"/"+this.mysqlConf.databaseName;
			}
			case SQLITE:
			{
				if(this.sqliteFile==null)
					throw new ParameterException("Must supply argument --sqliteFile in order to overwrite JSON file.");
				return "jdbc:sqlite:"+this.sqliteFile;
			}
			default:
				throw new IllegalStateException(this.database+" is an unknown database type.");
		}	
	}
	
}
