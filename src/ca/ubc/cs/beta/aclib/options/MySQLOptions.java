package ca.ubc.cs.beta.aclib.options;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import ca.ubc.cs.beta.aclib.misc.file.HomeFileUtils;
import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterFile;

@UsageTextField(title="MySQL Options", description="Options that control how to connect to the MySQL Server")
public class MySQLOptions extends AbstractOptions {

	@UsageTextField(defaultValues="~/.aclib/mysql.opt")
	@Parameter(names="--mysqlDefaultsFile", description="file that contains default settings for MySQL")
	@ParameterFile(ignoreFileNotExists = true) 
	public File mysqlDefaults = HomeFileUtils.getHomeFile(".aclib" + File.separator  + "mysql.opt");
	
	@Parameter(names={"--mysqlHostName","--mysql-hostname"}, description="Hostname of database server" )
	public String host;
	
	
	@Parameter(names={"--mysqlPassword","--mysql-password"}, description="Password of database server" )
	public String password;
	
	@Parameter(names={"--mysqlDatabase", "--mysqlDatabaseName", "--mysql-database"}, description="Name of Database" )
	public String databaseName = null;
	
	
	@Parameter(names={"--mysqlUsername","--mysql-username","--mysql_user"}, description="Username of the Database")
	public String username;
	
	@Parameter(names={"--mysqlPort", "--mysql-port"}, description="Port of database server")
	public int port;
	
	@Parameter(names="--mysqlParameterFile", description="MySQL Configuration Options")
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
