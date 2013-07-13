package ca.ubc.cs.beta.aclib.logging;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import ca.ubc.cs.beta.aclib.misc.options.UsageTextField;
import ca.ubc.cs.beta.aclib.options.AbstractOptions;

import com.beust.jcommander.Parameter;
/**
 * Parameter delegate that initializes the logback logger
 * <br>
 * <b>Usage:</b> Prior to any Loggers being created call the init() method, it will set the appropriate System properties
 * that can be read in an appropriate logback.xml file that is on the classpath (generally in conf/)
 * <p>
 * There is an example logback.xml file at the end of this file. The expectation is that files will be written as
 * log-runN.txt 
 * 
 * <b>Tip:</b> You can set debug="true" at the top of your logback.xml file, and then step through your code
 * when the debug messages print for the first time you have created your first logger. <br/>
 * 
 * 
 * @author Steve Ramage <seramage@cs.ubc.ca>
 *
 */
@UsageTextField(hiddenSection=true)
public class LoggingOptions extends AbstractOptions{

	@Parameter(names="--consoleLogLevel",description="default log level of console output (this cannot be more verbose than the logLevel)")
	public LogLevel consoleLogLevel = LogLevel.INFO;
	
	@Parameter(names="--logLevel",description="Log Level for SMAC")
	public LogLevel logLevel = LogLevel.DEBUG;	
	
	//This isn't meant to be an option, you can simply change this value before calling initializeLogging
	public boolean suppressLogLevelConsistencyWarning = false;
	
	public void initializeLogging(String completeOutputDir, int numRun)
	{
	
		if(completeOutputDir == null)
		{
			completeOutputDir = (new File("")).getAbsolutePath();
		} 
			
	
		System.setProperty("OUTPUTDIR",completeOutputDir);
		System.setProperty("NUMRUN", String.valueOf(numRun));
		System.setProperty("STDOUT-LEVEL", consoleLogLevel.name());
		System.setProperty("ROOT-LEVEL",logLevel.name());
		
		
		
		String logLocation = getLogLocation(completeOutputDir, numRun);
		
		System.setProperty("RUNLOG", logLocation);
		System.setProperty("ERRLOG", getErrorLogLocation(completeOutputDir,numRun));
		System.setProperty("WARNLOG", getWarnLogLocation(completeOutputDir,numRun));
		
		System.out.println("*****************************\nLogging to: " + logLocation +  "\n*****************************");
		
		//Generally has the format: ${OUTPUTDIR}/${RUNGROUPDIR}/log-run${NUMRUN}.txt
		Logger log = LoggerFactory.getLogger(LoggingOptions.class);
		log.info("Logging to: {}",logLocation);
		
		
		if(!suppressLogLevelConsistencyWarning && logLevel.lessVerbose(consoleLogLevel))
		{
			log.warn("The console has been set to be more verbose than the log. This is generally an error, except if you have modified the logback.xml to have certain loggers be more specific");
			
		}
			
		
	}
	
	public String getLogLocation(String completeOutputDir, int numRun)
	{

		if(completeOutputDir == null)
		{
			completeOutputDir = (new File("")).getAbsolutePath();
		} 
		return completeOutputDir + File.separator+  "log-run" + numRun+ ".txt";
	}
	
	public String getErrorLogLocation(String completeOutputDir, int numRun)
	{
		

		if(completeOutputDir == null)
		{
			completeOutputDir = (new File("")).getAbsolutePath();
		} 
		return completeOutputDir + File.separator+  "log-err" + numRun+ ".txt";
	}
	
	public String getWarnLogLocation(String completeOutputDir, int numRun)
	{

		if(completeOutputDir == null)
		{
			completeOutputDir = (new File("")).getAbsolutePath();
		} 
		
		return completeOutputDir + File.separator+  "log-warn" + numRun+ ".txt";
		
	}

}
/*
<?xml version="1.0" encoding="UTF-8" ?>
<configuration debug="false" >
  <appender name="FILE" class="ch.qos.logback.core.FileAppender">
  <file>${RUNLOG}.txt</file>
  <append>false</append>
  <encoder>
        <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
  </encoder>
</appender>
  
  
  <appender name="FILE-WARN" class="ch.qos.logback.core.FileAppender">
  <file>${WARNLOG}.txt</file>
  <append>false</append>
  <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        <level>WARN</level>
  </filter>
  <encoder>
        <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  
  
  <appender name="FILE-ERR" class="ch.qos.logback.core.FileAppender">
  <file>${ERRLOG}.txt</file>
  <append>false</append>
  <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        <level>ERROR</level>
  </filter>
  <encoder>
        <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  

  
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        <level>${STDOUT-LEVEL}</level>
    </filter>
    <encoder>
      <pattern>[%-5level] %msg%n</pattern>
    </encoder>
    
  </appender>
  
  <root level="${ROOT-LEVEL}">
    <appender-ref ref="STDOUT" />
    <appender-ref ref="FILE" />
    <appender-ref ref="FILE-WARN"/>
    <appender-ref ref="FILE-ERR"/>
  </root>
    
</configuration>
*/