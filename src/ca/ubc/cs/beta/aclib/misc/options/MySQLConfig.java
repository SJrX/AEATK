package ca.ubc.cs.beta.aclib.misc.options;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import ca.ubc.cs.beta.aclib.options.AbstractOptions;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterFile;

public class MySQLConfig extends AbstractOptions {

	@Parameter(names="--mysqlHostName", description="Hostname of database server" )
	public String host = "arrowdb.cs.ubc.ca";
	
	/*
	 * Do not make a parameter until the no password is fixed
	 * and this should read it from a file.
	@Parameter(names="--mysqlPassword", description="Password of database server" )
	*/
	public String password = null;
	
	@Parameter(names="--mysqlDatabaseName", description="Name of Database" )
	public String databaseName = "mysql_db_tae";
	
	
	@Parameter(names="--mysqlUsername", description="Username of the Database")
	public String username = "hutter";
	
	@Parameter(names="--mysqlPort", description="Port of database server")
	public int port=4040;
	
	@Parameter(names="--mysqlParameters", description="MySQL Configuration Options")
	@ParameterFile
	public File mysqlParamFile = null;

	public Connection getConnection()
	{
		String url="jdbc:mysql://" + host + ":" + port + "/" + databaseName;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			return DriverManager.getConnection(url,username, password);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
			
		}
		
		
	}
}
