/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado.tools.db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.security.AESCipher;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.tools.CsvFileImporter;
import com.netcrest.pado.tools.hazelcast.HazelcastCsvFileImporter;

public class DbManager
{
	public static enum ImportType
	{
		/**
		 * Downloads (imports) data immediately and then terminates.
		 */
		IMPORT_NOW_THEN_TERMINATE,
		
		/**
		 * Downloads (imports) data only on schedule.
		 */
		IMPORT_ON_SCHEDULE, 
		
		/**
		 * Downloads (imports) data immediately and then goes on schedule.
		 */
		IMPORT_NOW_THEN_ON_SCHEDULE, 
		
		/**
		 * Imports data files that have already been created before.
		 * This option does not download data.
		 */
		IMPORT_THEN_TERMINATE
	};

	public final static String PROP_DB_CONFIG_FILES = "db.config.files";
	public final static String PROP_DB_THREAD_POOL_SIZE = "db.thread.pool.size";

	private final static long DAY_IN_SEC = 24 * 3600;

	private static DbManager dbManager;

	private final static String DEFAULT_NULL_VALUE = "'\\N'";
	private final static String DEFAULT_DELIMITER_VALUE = "\t";

	// <GridId, Map<GridPath, DbConfigPathEx>>
	private Map<String, Map<String, DbConfigPathEx>> configMap = new HashMap<String, Map<String, DbConfigPathEx>>(10);
	private int threadPoolSize;
	private Properties csvProperties;
	private ImportType importType;

	private DbManager(ImportType importType) throws InvalidAttributeException, DbManagerException, IOException
	{
		this.importType = importType;
		init();
	}

	public static DbManager getDbManager()
	{
		return dbManager;
	}

	public static synchronized DbManager initialize(ImportType importType) throws InvalidAttributeException, DbManagerException, IOException
	{
		if (dbManager == null) {
			dbManager = new DbManager(importType);
		}
		return dbManager;
	}

	@SuppressWarnings({ "rawtypes" })
	private void init() throws InvalidAttributeException, DbManagerException, IOException
	{
		// The schduler's properties file should be different from
		// import_csv as it contains different directory paths.
		String schedulerPropertiesFilePath = System.getProperty("pado.csv.properities", "etc/client/scheduler.properties");
		System.setProperty("pado.csv.properties", schedulerPropertiesFilePath);
		csvProperties = loadSchedulerProperties();

		String schedulerDir = PadoUtil.getProperty(Constants.PROP_SCHEDULER_DIR, Constants.DEFAULT_SCHEDULER_DIR);
		File schedulerEtcDir = new File(schedulerDir, "etc");
		String dbConfigFilesStr = PadoUtil.getProperty(PROP_DB_CONFIG_FILES);

		File dbConfigFiles[];
		if (dbConfigFilesStr == null) {
			dbConfigFiles = schedulerEtcDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname)
				{
					return pathname.getName().endsWith(".json");
				}
			});
		} else {
			String[] split = dbConfigFilesStr.split(",");
			HashSet<File> set = new HashSet<File>(split.length, 1f);
			for (String fileName : split) {
				File file = new File(schedulerEtcDir, fileName);
				if (file.getName().endsWith(".json") && file.exists()) {
					set.add(file);
				}
			}
			dbConfigFiles = set.toArray(new File[set.size()]);
		}

		if (dbConfigFiles == null) {
			return;
		}

		for (File file : dbConfigFiles) {
			try {
				JsonLite config = new JsonLite(file);
				String pw = (String) config.get("Password");
				try {
					if (pw == null) {
						pw = null;
					} else {
						pw = AESCipher.decryptUserTextToText(pw);
					}
				} catch (Exception e) {
					throw new DbManagerException("Invalid encrypted password: " + file.getAbsolutePath(), e);
				}
				String poolName = file.getAbsolutePath();
				DbThreadConnectionPoolManager.getDbConnectionPoolManager().createPool(poolName,
						(String) config.get("Url"), (String) config.get("Driver"), (String) config.get("User"), pw);
				String nullValue = (String) config.get("Null");
				String delimiter = (String) config.get("Delimiter");
				Object value = config.get("FetchSize");
				int fetchSize = 0;
				if (value != null) {
					if (value instanceof Number) {
						fetchSize = (Integer)value;
					} else {
						fetchSize = Integer.parseInt(value.toString());
					}
				}
				Object[] jls = (Object[]) config.get("Paths");
				if (jls != null) {
					for (Object obj : jls) {
						JsonLite configPath = (JsonLite) obj;
						initConfigPath(config, configPath, file, nullValue, delimiter, fetchSize);
					}
				}
			} catch (FileNotFoundException e) {
				// This should never occur
				Logger.error("DB config file not found: " + file.getAbsolutePath());
			} catch (IOException e) {
				Logger.error("Invalid config file (ignored): " + file.getAbsolutePath(), e);
			}
		}

		// Thread pool size
		String threadPoolSizeStr = PadoUtil.getProperty(PROP_DB_THREAD_POOL_SIZE, "4");
		try {
			threadPoolSize = Integer.parseInt(threadPoolSizeStr);
		} catch (NumberFormatException ex) {
			threadPoolSize = 4;
		}
	}

	@SuppressWarnings("rawtypes")
	private void initConfigPath(JsonLite config, JsonLite configPath, File configFile, String nullValue,
			String delimiter, int fetchSize) throws InvalidAttributeException
	{
		Map<String, DbConfigPathEx> map = configMap.get(config.get("GridId"));
		if (map == null) {
			map = new HashMap<String, DbConfigPathEx>(10);
			configMap.put((String) config.get("GridId"), map);
		}
		String schedulerDir = PadoUtil.getProperty("scheduler.dir", "data/scheduler");
		File schedulerDirFile = new File(schedulerDir);
		String gridId = (String) config.get("GridId");
		map.put((String) configPath.get("Path"), new DbConfigPathEx(configPath, configFile, schedulerDirFile, gridId,
				nullValue, delimiter, fetchSize));
	}

	public void importData(ImportType type, boolean isImport, boolean isDaemonThread) throws IOException, InterruptedException
	{
		switch (type) {
		case IMPORT_THEN_TERMINATE:
			importCsv();
			break;
			
		case IMPORT_NOW_THEN_TERMINATE:
			importAll(isImport);
			Logger.info("Import complete.");
			break;

		case IMPORT_NOW_THEN_ON_SCHEDULE:
			importAll(isImport);
			schedule(isImport, isDaemonThread);
			break;

		case IMPORT_ON_SCHEDULE:
		default:
			schedule(isImport, isDaemonThread);
			break;
		}
	}

	private List<ScheduledConfig> scheduledList;

	static class ScheduledConfig implements Comparable<ScheduledConfig>
	{
		enum Day
		{
			SUNDAY(Calendar.SUNDAY), MONDAY(Calendar.MONDAY), TUESDAY(Calendar.TUESDAY), WEDNESDAY(Calendar.WEDNESDAY), THURSDAY(
					Calendar.THURSDAY), FRIDAY(Calendar.FRIDAY), SATURDAY(Calendar.SATURDAY);

			int calendarDay;

			private Day(int day)
			{
				this.calendarDay = day;
			}

			public int getCalendarDay()
			{
				return calendarDay;
			}

			public static Day getDay(int calendarDay)
			{
				Day[] days = values();
				for (Day day : days) {
					if (day.getCalendarDay() == calendarDay) {
						return day;
					}
				}
				return null;
			}
		};

		DbConfigPathEx configPathEx;
		Day day;
		String timeStr;
		long timeInSec;

		ScheduledConfig(DbConfigPathEx configPathEx, String day, String timeStr, long timeInSec)
		{
			this.configPathEx = configPathEx;
			this.timeInSec = timeInSec;
			this.timeStr = timeStr;
			this.day = Day.valueOf(day.trim().toUpperCase());
		}

		@Override
		public int compareTo(ScheduledConfig o)
		{
			if (day.ordinal() > o.day.ordinal()) {
				return 1;
			} else if (day.ordinal() < o.day.ordinal()) {
				return -1;
			} else if (timeInSec > o.timeInSec) {
				return 1;
			} else if (timeInSec < o.timeInSec) {
				return -1;
			} else {
				return 0;
			}
		}

		/**
		 * Returns a delay in msec from now before this config to be executed.
		 */
		public long getDelayInMsec()
		{
			long delayInSec;
			Calendar calendar = Calendar.getInstance();
			int nowCalendarDay = calendar.get(Calendar.DAY_OF_WEEK);
			long nowTimeInSec = getTimeInSec(calendar);
			int scCalendarDay = day.getCalendarDay();
			if (nowCalendarDay == scCalendarDay) {
				if (nowTimeInSec > timeInSec) {
					// already passed the scheduled time. No delay. Execute it
					// immediately. This may occur is the previous import
					// execution took time that overlapped this task.
					delayInSec = 0;
				} else {
					delayInSec = timeInSec - nowTimeInSec;
				}
			} else if (nowCalendarDay < scCalendarDay) {
				int numDays = scCalendarDay - nowCalendarDay - 1;
				long daysInSec = numDays * DAY_IN_SEC;
				delayInSec = daysInSec + (DAY_IN_SEC - nowTimeInSec) + timeInSec;
			} else {
				int numDays = (Calendar.SATURDAY - nowCalendarDay) + (scCalendarDay - Calendar.SUNDAY);
				long daysInSec = numDays * DAY_IN_SEC;
				delayInSec = daysInSec + (DAY_IN_SEC - nowTimeInSec) + timeInSec;
			}
			return delayInSec * 1000;
		}
	}

	private void schedule(boolean isImport, boolean isDaemonThread)
	{
		scheduledList = new ArrayList<ScheduledConfig>();

		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Set<Map.Entry<String, Map<String, DbConfigPathEx>>> set = configMap.entrySet();
		for (Map.Entry<String, Map<String, DbConfigPathEx>> entry : set) {
			Map<String, DbConfigPathEx> map = entry.getValue();
			Set<Map.Entry<String, DbConfigPathEx>> set2 = map.entrySet();
			for (Map.Entry<String, DbConfigPathEx> entry2 : set2) {
				DbConfigPathEx configPathEx = entry2.getValue();
				String dayStr = (String) configPathEx.configPath.get("Day");
				String time = (String) configPathEx.configPath.get("Time");
				String[] daySplit = dayStr.split(",");
				String[] timeSplit = time.split(",");
				for (String day : daySplit) {
					for (String t : timeSplit) {
						t = t.trim();
						try {
							Date date = formatter.parse(t);
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(date);
							long timeInSec = getTimeInSec(calendar);
							scheduledList.add(new ScheduledConfig(configPathEx, day.trim(), t, timeInSec));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		Collections.sort(scheduledList);
		logScheduledList();

		Thread schedulerThread = new Thread(new Scheduler(isImport), "Pado-DbImportScheduler");
		schedulerThread.setDaemon(isDaemonThread);
		schedulerThread.start();
	}

	private void logScheduledList()
	{
		StringBuffer buffer = new StringBuffer(2000);
		buffer.append("DB import tasks scheduled:\n");
		for (int i = 0; i < scheduledList.size(); i++) {
			ScheduledConfig sc = scheduledList.get(i);
			buffer.append(i + 1);
			buffer.append(". ");
			buffer.append(getLogHeader(sc) + "\n");
		}
		Logger.config(buffer.toString());
	}

	private String getLogHeader(ScheduledConfig scheduledConfig)
	{
		StringBuffer buffer = new StringBuffer(80);
		buffer.append(scheduledConfig.day);
		buffer.append(" ");
		buffer.append(scheduledConfig.timeStr);
		buffer.append(" ");
		buffer.append(scheduledConfig.configPathEx.gridId);
		buffer.append("//");
		buffer.append(scheduledConfig.configPathEx.gridPath);
		buffer.append(" [");
		buffer.append(scheduledConfig.configPathEx.queryString);
		buffer.append("]");
		return buffer.toString();
	}

	class Scheduler implements Runnable
	{
		boolean shouldRun = true;
		boolean isImport;
		
		Scheduler(boolean isImport)
		{
			this.isImport = isImport;
		}

		public synchronized void run()
		{
			// Schedule the first one
			ScheduledConfig sc = null;
			long delay = 2000;
			List<ScheduledConfig> list = getNowScheduledConfigList();
			if (list.size() > 0) {
				sc = list.get(0);
				delay = sc.getDelayInMsec();
			}

			try {
				while (shouldRun) {
					StringBuffer buffer = new StringBuffer(2000);
					buffer.append("Next scheduled tasks:\n");
					for (int i = 0; i < list.size(); i++) {
						sc = list.get(i);
						buffer.append(i + 1);
						buffer.append(". ");
						buffer.append(getLogHeader(sc));
						buffer.append("\n");
					}
					Logger.info(buffer.toString());
					wait(delay);
					execute(list);
					if (isImport) {
						try {
							importCsv();
						} catch (IOException e) {
							Logger.error(e);
						}
					}
					
					// Schedule next
					if (sc == null) {
						list = getNowScheduledConfigList();
					} else {
						list = getNextScheduledConfigList(sc.day, sc.timeInSec);
						if (list.size() > 0) {
							ScheduledConfig newSc = list.get(0);

							// If the next one is scheduled after 5 min then
							// close DB connections to free resources.
							Calendar calendar = Calendar.getInstance();
							if (newSc.day.getCalendarDay() != calendar.get(Calendar.DAY_OF_WEEK)
									|| Math.abs(newSc.timeInSec - getTimeInSec(calendar)) > 300) {
								try {
									DbThreadConnectionPoolManager.getDbConnectionPoolManager().close();
								} catch (SQLException e) {
									Logger.warning("DB connection pools: Error occurred while closing connections", e);
								}
							}

							sc = newSc;
							delay = sc.getDelayInMsec();
						} else {
							delay = 2000;
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private void execute(List<ScheduledConfig> list)
		{
			if (list == null || list.size() == 0) {
				return;
			}
			List<ImportScheduledTask> importList = new ArrayList<ImportScheduledTask>(list.size());
			for (ScheduledConfig config : list) {
				importList.add(new ImportScheduledTask(config));
			}

			try {
				ExecutorService es = Executors.newFixedThreadPool(threadPoolSize, new ThreadFactory() {
					int threadNum = 1;
		            public Thread newThread(Runnable r) {
		                Thread t = new Thread(r, "Pado-DbManager-" + threadNum++);
		                t.setDaemon(true);
		                return t;
		            }
		        });
				long startTime = System.currentTimeMillis();
				List<Future<ImportScheduledTask>> futureList = es.invokeAll(importList);
				for (int i = 0; i < futureList.size(); i++) {
					Future<ImportScheduledTask> future = futureList.get(i);
					try {
						ImportScheduledTask importTask = future.get();
						String timeTookStr;
						if (importTask.elapsedTimeInSec > 300) {
							timeTookStr = importTask.elapsedTimeInSec / 60 + " min.";
						} else {
							timeTookStr = importTask.elapsedTimeInSec + " sec.";
						}
						Logger.info((i + 1) + ". " + getLogHeader(importTask.scheduledConfig)
								+ " Time took to import data: " + timeTookStr);

					} catch (ExecutionException e) {
						Logger.warning(e);
					}
				}
				long totalElapsedTimeInSec = (System.currentTimeMillis() - startTime) / 1000;
				if (totalElapsedTimeInSec > 300) {
					Logger.info("Total time toook to import data for " + futureList.size() + " grid paths: "
							+ totalElapsedTimeInSec / 60 + " min.");
				} else {
					Logger.info("Total time toook to import data for " + futureList.size() + " grid paths: "
							+ totalElapsedTimeInSec + " sec.");
				}
			} catch (InterruptedException e) {
				Logger.warning(e);
			}
		}

		public void terminate()
		{
			shouldRun = false;
		}

	}

	private static long getTimeInSec(Calendar calendar)
	{
		return calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60
				+ calendar.get(Calendar.SECOND);
	}

	/**
	 * Returns a list of scheduled config objects to be scheduled as of current
	 * time.
	 */
	private List<ScheduledConfig> getNowScheduledConfigList()
	{
		Calendar calendar = Calendar.getInstance();
		int calendarDay = calendar.get(Calendar.DAY_OF_WEEK);
		ScheduledConfig.Day day = ScheduledConfig.Day.getDay(calendarDay);
		long timeInSec = getTimeInSec(calendar);
		return getNextScheduledConfigList(day, timeInSec);
	}

	/**
	 * Returns a list of the next scheduled config objects that come after the
	 * specified day and time. If it reaches the end of scheduledList then it
	 * begins from the top, i.e., it effectively turns scheduledList into a
	 * circular list. The returned list contains all config objects with the
	 * same day and time.
	 * 
	 * @param day
	 *            As-of Day
	 * @param timeInSec
	 *            As-of time in seconds
	 * @return Always returns a non-null list. It may be empty if scheduledList
	 *         is empty.
	 */
	private List<ScheduledConfig> getNextScheduledConfigList(ScheduledConfig.Day day, long timeInSec)
	{
		List<ScheduledConfig> list = new ArrayList<ScheduledConfig>(10);
		if (scheduledList.size() > 0) {
			ScheduledConfig firstFoundSc = null;
			for (int i = 0; i < scheduledList.size(); i++) {
				ScheduledConfig sc = scheduledList.get(i);
				if (sc.day.ordinal() > day.ordinal() || (sc.day.ordinal() == day.ordinal() && sc.timeInSec > timeInSec)) {
					if (list.size() == 0) {
						firstFoundSc = sc;
						list.add(sc);
					} else {
						if (firstFoundSc.day == sc.day && firstFoundSc.timeInSec == sc.timeInSec) {
							list.add(sc);
						} else {
							break;
						}
					}
				}
			}

			// If the list is 0 then start from the beginning (circular list)
			if (list.size() == 0) {
				ScheduledConfig.Day day2 = ScheduledConfig.Day.SUNDAY;
				long timeInSec2 = 0;
				return getNextScheduledConfigList(day2, timeInSec2);
			}
		}
		return list;
	}

	private void importAll(boolean isImport) throws IOException, InterruptedException
	{
		ArrayList<DownloadTask> list = new ArrayList<DownloadTask>();
		Set<Map.Entry<String, Map<String, DbConfigPathEx>>> set = configMap.entrySet();
		for (Map.Entry<String, Map<String, DbConfigPathEx>> entry : set) {
			String gridId = entry.getKey();
			Map<String, DbConfigPathEx> map = entry.getValue();
			Set<Map.Entry<String, DbConfigPathEx>> set2 = map.entrySet();
			for (Map.Entry<String, DbConfigPathEx> entry2 : set2) {
				String gridPath = entry2.getKey();
				list.add(new DownloadTask(gridId, gridPath));
			}
		}

		ExecutorService es = Executors.newFixedThreadPool(threadPoolSize, new ThreadFactory() {
			int threadNum = 1;
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "Pado-DbManager-" + threadNum++);
                t.setDaemon(true);
                return t;
            }
        });
		try {
			long startTime = System.currentTimeMillis();
			List<Future<DownloadTask>> futureList = es.invokeAll(list);
			for (int i = 0; i < futureList.size(); i++) {
				Future<DownloadTask> future = futureList.get(i);
				try {
					DownloadTask importTask = future.get();
					if (importTask.elapsedTimeInSec > 300) {
						Logger.info(i + 1 + ". [" + importTask.gridId + "//" + importTask.gridPath + " Size="
								+ importTask.rowCount + "]: Time took to import data: " + importTask.elapsedTimeInSec
								/ 60 + " min.");
					} else {
						Logger.info(i + 1 + ". [" + importTask.gridId + "//" + importTask.gridPath + " Size="
								+ importTask.rowCount + "]: Time took to import data: " + importTask.elapsedTimeInSec
								+ " sec.");
					}
				} catch (ExecutionException e) {
					Logger.warning(e);
				}
			}
			long totalElapsedTimeInSec = (System.currentTimeMillis() - startTime) / 1000;
			if (totalElapsedTimeInSec > 300) {
				Logger.info("Total time toook to import data for " + futureList.size() + " grid paths: "
						+ totalElapsedTimeInSec / 60 + " min.");
			} else {
				Logger.info("Total time toook to import data for " + futureList.size() + " grid paths: "
						+ totalElapsedTimeInSec + " sec.");
			}
		} finally {
			es.shutdown();
		}

		if (isImport) {
			importCsv();
		}

	}

	private void importCsv() throws IOException, InterruptedException
	{
		boolean isHazelcast = Boolean.getBoolean("pado.hazelcast.enabled");
		
		if (isHazelcast) {
			HazelcastCsvFileImporter csvImporter = new HazelcastCsvFileImporter(csvProperties);
			csvImporter.importData(false, false, -1, -1, false);
			csvImporter.close();
		} else {
			CsvFileImporter csvImporter = new CsvFileImporter(csvProperties);
			csvImporter.importData(false, false, -1, -1, false);
			csvImporter.close();
		}
	}

	private Properties loadSchedulerProperties() throws IOException
	{
		String csvPropertiesPath = System.getProperty("pado.csv.properties");
		File file = new File(csvPropertiesPath);
		FileReader reader = null;
		Properties csvProperties = new Properties();
		try {
			reader = new FileReader(file);
			csvProperties.load(reader);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return csvProperties;
	}

	class DownloadTask implements Callable<DownloadTask>
	{
		String gridId;
		String gridPath;
		int rowCount;
		long elapsedTimeInSec;

		DownloadTask(String gridId, String gridPath)
		{
			this.gridId = gridId;
			this.gridPath = gridPath;
		}

		@Override
		public DownloadTask call() throws Exception
		{
			long startTime = System.currentTimeMillis();
			rowCount = downloadData(gridId, gridPath);
			elapsedTimeInSec = (System.currentTimeMillis() - startTime) / 1000;
			return this;
		}
	}

	class ImportScheduledTask implements Callable<ImportScheduledTask>
	{
		ScheduledConfig scheduledConfig;
		long elapsedTimeInSec;
		int rowCount;

		ImportScheduledTask(ScheduledConfig scheduledConfig)
		{
			this.scheduledConfig = scheduledConfig;
		}

		@Override
		public ImportScheduledTask call() throws Exception
		{
			long startTime = System.currentTimeMillis();
			rowCount = downloadData(scheduledConfig.configPathEx);
			elapsedTimeInSec = (System.currentTimeMillis() - startTime) / 1000;
			return this;
		}
	}

	private int downloadData(String gridId, String gridPath) throws SQLException, IOException
	{
		Map<String, DbConfigPathEx> map = configMap.get(gridId);
		if (map == null) {
			return 0;
		}
		DbConfigPathEx configPathEx = map.get(gridPath);
		return downloadData(configPathEx);
	}

	/**
	 * Imports data from the data source defined by the specified DbConfigPathEx
	 * object.
	 * 
	 * @param configPathEx
	 *            DbConfigPath extension
	 * @return Number of rows imported
	 * @throws SQLException
	 *             Thrown if an SQL exception occurs
	 * @throws IOException
	 *             Thrown if an exception occurs while write to the data file.
	 */
	private int downloadData(DbConfigPathEx configPathEx) throws SQLException, IOException
	{
		DbThreadConnectionPool pool = DbThreadConnectionPoolManager.getDbConnectionPoolManager().getPool(
				configPathEx.poolName);
		if (pool == null) {
			Logger.error("Specified connection pool does not exist: " + configPathEx.poolName + ". DB operation failed. " + configPathEx);
			return 0;
		}
		Connection conn = pool.getConnection();

		PreparedStatement statement = conn.prepareStatement(configPathEx.queryString);
		statement.setFetchSize(configPathEx.fetchSize);
		ResultSet resultSet = statement.executeQuery();
		String columns[] = configPathEx.columns;
		// String setters[] = configPathEx.setters;

		// Dump to output file
		File outputFile = configPathEx.getImportFilePath();
		if (outputFile.exists() == false) {
			outputFile.getParentFile().mkdirs();
			outputFile.createNewFile();
		}
		OutputStream outputStream = new FileOutputStream(outputFile);
		Writer ow = new OutputStreamWriter(outputStream, Charset.forName("US-ASCII"));
		BufferedWriter writer = new BufferedWriter(ow);
		int count = 0;

		try {
			// Writer header
			writer.write((String) configPathEx.configPath.get("Columns"));
			writer.write("\n");

			// Write rows
			while (resultSet.next()) {
				for (int i = 0; i < configPathEx.columns.length; i++) {
					if (i > 0) {
						writer.write(configPathEx.delimiter);
					}
					Object obj = resultSet.getObject(columns[i]);
					if (obj == null) {
						writer.write(configPathEx.nullValue);
					} else {
						String value = obj.toString();
						if (value.contains(",")) {
							writer.write("\"");
							writer.write(value);
							writer.write("\"");
						} else {
							writer.write(value);
						}
					}
				}
				writer.write("\n");
				count++;
			}
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}

		return count;
	}

	@SuppressWarnings("rawtypes")
	class DbConfigPathEx
	{
		String poolName;
		JsonLite configPath;
		String[] setters;
		String[] getters;
		String[] columns;
		Class valueObjectClass;
		KeyType keyType = null;
		String queryString;
		String insertString;
		File outputDirFile;
		File importDirFile;
		String gridId;
		String gridPath;
		File schemaFile;
		String nullValue;
		String delimiter;
		int fetchSize = 0; // Default to 0, i.e., DB determines the size.

		DbConfigPathEx(JsonLite configPath, File configFile, File outputDirFile, String gridId, String nullValue,
				String delimiter, int fetchSize) throws InvalidAttributeException
		{
			this.configPath = configPath;
			this.queryString = (String) configPath.get("Query");
			this.insertString = (String) configPath.get("Insert");
			this.outputDirFile = outputDirFile;
			this.importDirFile = new File(outputDirFile, "import");
			if (this.importDirFile.exists() == false) {
				importDirFile.mkdirs();
			}
			this.gridId = gridId;
			this.gridPath = (String) configPath.get("Path");
			this.nullValue = nullValue;
			if (this.nullValue == null) {
				this.nullValue = DEFAULT_NULL_VALUE;
			}
			this.delimiter = delimiter;
			if (this.delimiter == null) {
				this.delimiter = DEFAULT_DELIMITER_VALUE;
			}
			this.fetchSize = fetchSize;
			this.poolName = configFile.getAbsolutePath();

			// Setters
			setters = split("Setters", (String) configPath.get("Setters"), configFile);

			// Getters
			getters = split("Getters", (String) configPath.get("Getters"), configFile);
			// if (getters == null) {
			// throw new InvalidAttributeException("Getters undefined: " +
			// configFile.getAbsolutePath());
			// }

			// Columns
			if (configPath.get("Columns") == null || configPath.get("Columns").equals("*")) {
				columns = getters;
			} else {
				columns = split("Columns", (String) configPath.get("Columns"), configFile);
			}

			// if (columns.length != getters.length) {
			// throw new
			// InvalidAttributeException("Numbers of columns and getters do not match: Columns="
			// + columns.length + ", Getters=" + getters.length + ". " +
			// configFile.getAbsolutePath());
			// }

			// ValueClass
			String className = (String) configPath.get("ValueClass");
			// if (className == null) {
			// throw new
			// InvalidAttributeException("ValueClass undefined in the config file: "
			// + configFile.getAbsolutePath());
			// }
			if (className != null) {
				try {
					valueObjectClass = Class.forName(className);
				} catch (ClassNotFoundException e1) {
					throw new InvalidAttributeException("ValueClass " + className + " undefined: "
							+ configFile.getAbsolutePath(), e1);
				}
			}

			// KeyTypeClass
			className = (String) configPath.get("KeyTypeClass");
			if (className != null) {
				Class clazz;
				try {
					clazz = Class.forName(className);
				} catch (ClassNotFoundException e) {
					throw new InvalidAttributeException("KeyTypeClass " + className + " undefined: "
							+ configFile.getAbsolutePath());
				}
				if (clazz.isEnum()) {
					Object constants[] = clazz.getEnumConstants();
					if (constants.length > 0) {
						if (constants[0] instanceof KeyType) {
							keyType = (KeyType) constants[0];
						}
					}
				}
			}

			// See if an empty valueObjectClass can be created
			try {
				createValueObject();
			} catch (Exception e) {
				throw new InvalidAttributeException("ValueClass cannot be created: " + configFile.getAbsolutePath(), e);
			}

			if (importType != ImportType.IMPORT_NOW_THEN_TERMINATE) {
				String gridPath = (String) configPath.get("Path");
				String schemaFileName = gridId + "." + gridPath.replaceAll("/", "-") + ".schema";
				File schemaFileDir = new File(outputDirFile, "schema");
				schemaFile = new File(schemaFileDir, schemaFileName);
				if (schemaFile.exists() == false) {
					throw new InvalidAttributeException("Schema file does not exist: " + schemaFile.getAbsolutePath());
				}
			}
		}

		private String[] split(String attributeName, String value, File file) throws InvalidAttributeException
		{
			String[] split = null;
			String[] retSplit = null;
			if (configPath.get(attributeName) != null) {
				split = ((String) configPath.get(attributeName)).split(",");
			}
			if (split != null) {
				retSplit = new String[split.length];
				for (int i = 0; i < split.length; i++) {
					retSplit[i] = split[i].trim();
				}

				for (int i = 0; i < retSplit.length; i++) {
					if (retSplit[i].length() == 0) {
						throw new InvalidAttributeException("Invalid " + attributeName + " attribute: "
								+ file.getAbsolutePath() + ". Attribute may not be an empty String: Index=" + i + ", "
								+ value);
					}
				}
			}
			return retSplit;
		}

		Object createValueObject() throws InstantiationException, IllegalAccessException
		{
			if (valueObjectClass == null) {
				return null;
			}
			if (valueObjectClass == JsonLite.class) {
				return new JsonLite(keyType);
			} else {
				return valueObjectClass.newInstance();
			}
		}

		File getImportFilePath()
		{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String suffix = formatter.format(new Date());
			String filePath = (String) configPath.get("Path");
			File file = null;
			if (filePath != null) {
				filePath = filePath.replaceAll("/", "-");
				file = new File(importDirFile, gridId + "." + filePath + ".v" + suffix + ".csv");
			}
			return file;
		}

		@Override
		public String toString()
		{
			return "DbConfigPathEx [poolName=" + poolName + ", configPath=" + configPath + ", queryString="
					+ queryString + ", insertString=" + insertString + ", outputDirFile=" + outputDirFile
					+ ", importDirFile=" + importDirFile + ", gridId=" + gridId + ", gridPath=" + gridPath
					+ ", schemaFile=" + schemaFile + "]";
		}
	}
}
