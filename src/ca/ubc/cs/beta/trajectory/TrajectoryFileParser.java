package ca.ubc.cs.beta.trajectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import ca.ubc.cs.beta.configspace.ParamConfiguration;
import ca.ubc.cs.beta.configspace.ParamConfigurationSpace;
import ca.ubc.cs.beta.models.surrogate.helpers.csv.ConfigCSVFileHelper;
import ca.ubc.cs.beta.smac.helper.AssociatedValue;

public class TrajectoryFileParser {

	private static final Logger log = LoggerFactory.getLogger(TrajectoryFileParser.class);
	
	/**
	 * Parses a SMAC Trajectory file, this file should have `name='value'` pairs in every column starting from the 5th 
	 * @param configs - CSV File To Parse
	 * @param configSpace - Config Space to parse from
	 * @return SkipListMap that maps the time of the incumbent to a <cpuOverhead, incumbent> pair.
	 */
	private static ConcurrentSkipListMap<Double, TrajectoryFileEntry> parseSMACTrajectoryFile(ConfigCSVFileHelper configs, ParamConfigurationSpace configSpace)
	{
		ConcurrentSkipListMap<Double,  TrajectoryFileEntry> skipList = new ConcurrentSkipListMap<Double, TrajectoryFileEntry>();
		for(int i=0; i < configs.getNumberOfDataRows(); i++)
		{
			
		
			String time = configs.getStringDataValue(i, 0);
			
			StringBuilder sb = new StringBuilder();
			
			String[] dataRow =  configs.getDataRow(i);
			ParamConfiguration configObj = configSpace.getEmptyConfiguration();
			
			for(int j=5; j < dataRow.length; j++)
			{
				String[] splitValues = dataRow[j].split("=");
				configObj.put(splitValues[0], splitValues[1].replaceAll("'", ""));
				sb.append("").append(dataRow[j]).append(" ");
			}
			//System.out.println(time + "=>" + sb.toString());
			Double overhead = Double.valueOf(dataRow[4]);
			Double empericalPerformance = Double.valueOf(dataRow[1]);

			TrajectoryFileEntry tfe = new TrajectoryFileEntry(configObj, overhead, empericalPerformance);
			
			skipList.put(Double.valueOf(time), tfe);
			
		}
		return skipList;
	}
	/**
	 * Parses a ParamILS Trajectory file, starting from column 5 the values of all parameters should be specified, the order of values must be alphabetical
	 * @param configs - CSV Configuration Hleper
	 * @param configSpace - Configuration Space to draw examples from
	 * @return SkipListMap that maps the time of the incumbent to a <cpuOverhead, incumbent> pair.
	 */
	private static ConcurrentSkipListMap<Double, TrajectoryFileEntry> parseParamILSTrajectoryFile(ConfigCSVFileHelper configs, ParamConfigurationSpace configSpace)
	{
		ConcurrentSkipListMap<Double,  TrajectoryFileEntry> skipList = new ConcurrentSkipListMap<Double, TrajectoryFileEntry>();
		List<String> paramNames = new ArrayList<String>(configSpace.getParameterNames());
		Collections.sort(paramNames);
		
		for(int i=0; i < configs.getNumberOfDataRows(); i++)
		{
			
		
			String time = configs.getStringDataValue(i, 0);
			
			StringBuilder sb = new StringBuilder();
			
			String[] dataRow =  configs.getDataRow(i);
			ParamConfiguration configObj = configSpace.getEmptyConfiguration();
			
			int dataOffset = 5;
			for(int j=0; j < paramNames.size(); j++)
			{
				configObj.put(paramNames.get(j), dataRow[j+dataOffset]);
			}
			//System.out.println(time + "=>" + sb.toString());
			Double overhead = Double.valueOf(dataRow[4]);
			Double empericalPerformance = Double.valueOf(dataRow[1]);

			
			TrajectoryFileEntry tfe = new TrajectoryFileEntry(configObj, overhead, empericalPerformance);
			
			skipList.put(Double.valueOf(time), tfe);
			
		}
		return skipList;
	}
	
	/**
	 * Parses a Trajectory File (both SMAC and ParamILS Formats)
	 * 
	 * NOTE: SMAC is tried first
	 * 
	 * 
	 * @param configs - CSV File representing the trajectory file
	 * @param configSpace - Configuration Space to create Configurations in
	 * @return SkipListMap that maps the time of the incumbent to a <cpuOverhead, incumbent> pair.
	 */
	public static ConcurrentSkipListMap<Double, TrajectoryFileEntry> parseTrajectoryFile(ConfigCSVFileHelper configs, ParamConfigurationSpace configSpace)
	{
		ConcurrentSkipListMap<Double,TrajectoryFileEntry> skipList;
		
		try {
			skipList = TrajectoryFileParser.parseSMACTrajectoryFile(configs, configSpace);
		} catch(ArrayIndexOutOfBoundsException e )
		{
			log.info("Trajectory File is not in SMAC Format, falling back to ParamILS Format");
			skipList = TrajectoryFileParser.parseParamILSTrajectoryFile(configs, configSpace);
		}
		return skipList;
		
		
	}
	

	/**
	 * Parses a Trajectory File (both SMAC and ParamILS Formats)
	 * 
	 * NOTE: SMAC is tried first
	 * 
	 * @param trajectoryFile - Trajectory file to parse
	 * @param configSpace - Configuration Space to create Configurations in
	 * @return 
	 * @return SkipListMap that maps the time of the incumbent to a <cpuOverhead, incumbent> pair.
	 * @throws FileNotFoundException, IOException 
	 */
	
	public static ConcurrentSkipListMap<Double, TrajectoryFileEntry> parseTrajectoryFile(File trajectoryFile, ParamConfigurationSpace configSpace) throws FileNotFoundException, IOException
	{
		CSVReader configCSV = new CSVReader(new FileReader(trajectoryFile));
		
		ConfigCSVFileHelper configs = new ConfigCSVFileHelper(configCSV.readAll(),1,0);
		
		return parseTrajectoryFile(configs, configSpace);
	}
	
	public static class TrajectoryFileEntry
	{
		private final ParamConfiguration config;
		private final double empericalPerformance;
		private final double acOverhead;
		
		public TrajectoryFileEntry(ParamConfiguration config, double acOverhead, double empericalPerformance)
		{
			this.config = config;
			this.empericalPerformance = empericalPerformance;
			this.acOverhead = acOverhead;
		}
		
		public ParamConfiguration getConfiguration()
		{
			return config;
		}
		
		public double getEmpericalPerformance()
		{
			return empericalPerformance;
		}
		
		public double getACOverhead()
		{
			return acOverhead;
		}
	}
}
