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
package com.netcrest.pado.test.biz.impl.gemfire;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.biz.impl.gemfire.PathBizImpl;
import com.netcrest.pado.biz.util.BizThreadPool;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.gemfire.GemfirePadoServerManager;
import com.netcrest.pado.gemfire.util.RegionBulkLoader;
import com.netcrest.pado.info.message.MessageType;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.util.IBulkLoader;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class StressTestBizImpl
{
	@Resource
	private IBizContextServer bizContext;
	private static volatile boolean isComplete = true;
	private BizThreadPool<ITemporalBizLink> temporalBizThreadPool = new BizThreadPool(
			PadoServerManager.getPadoServerManager().getCatalog(), ITemporalBiz.class);

	private final java.util.logging.Logger perfLogger;

	public StressTestBizImpl()
	{
		perfLogger = java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
		// suppress the logging output to the console
		java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
		java.util.logging.Handler[] handlers = rootLogger.getHandlers();
		for (java.util.logging.Handler handler : handlers) {
			if (handlers[0] instanceof java.util.logging.ConsoleHandler) {
				rootLogger.removeHandler(handler);
			}
		}
		perfLogger.setLevel(java.util.logging.Level.INFO);
		try {
			String homeDir = PadoUtil.getProperty(Constants.PROP_HOME_DIR);
			java.util.logging.FileHandler fileTxt = new java.util.logging.FileHandler(homeDir + "/perf/driver_perf.log");
			java.util.logging.SimpleFormatter formatterTxt = new java.util.logging.SimpleFormatter();
			fileTxt.setFormatter(formatterTxt);
			perfLogger.addHandler(fileTxt);
		} catch (Exception ex) {
			Logger.error(ex);
		}
	}

	@BizMethod
	public String __start(final Map<String, JsonLite> pathConfigMap, final int threadCountPerDriver,
			final int loopCount, final boolean isIncludeObjectCreationTime)
	{
		String serverNum = PadoServerManager.getPadoServerManager().getServerNum();
		if (isComplete == false) {
			return "[DriverNum(ServerNum)=" + serverNum + "] Aborted. Another stress test already in progress.";
		}
		isComplete = false;

		Executors.newSingleThreadExecutor(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r)
			{
				Thread thread = Executors.defaultThreadFactory().newThread(r);
				thread.setName("StressTestBizLauncherThread");
				thread.setDaemon(true);
				return thread;
			}
		}).execute(new Runnable() {
			@Override
			public void run()
			{

				Logger.info("StressTestBiz: BulkLoad Tests Started");

				ExecutorService es = Executors.newFixedThreadPool(threadCountPerDriver, new ThreadFactory() {

					@Override
					public Thread newThread(Runnable r)
					{
						Thread thread = Executors.defaultThreadFactory().newThread(r);
						thread.setName("StressTestBizThread");
						thread.setDaemon(true);
						return thread;
					}

				});
				try {
					ArrayList<DataLoader> dataLoaderList = new ArrayList<DataLoader>();
					int pathCountPerLoop = pathConfigMap.size();
					for (int k = 0; k < loopCount; k++) {
						Collection<JsonLite> col = pathConfigMap.values();
						for (JsonLite pathConfig : col) {
							for (int j = 1; j <= threadCountPerDriver; j++) {
								DataLoader dataLoader = new DataLoader(j, threadCountPerDriver, pathConfig,
										isIncludeObjectCreationTime);
								dataLoaderList.add(dataLoader);
							}
							// Invoke all DataLoaders
							List<Future<JsonLite>> futureList = es.invokeAll(dataLoaderList);

							// Block till all threads are done. Collect
							// results in to one list.
							ArrayList<JsonLite> perfList = new ArrayList<JsonLite>();
							for (Future<JsonLite> future : futureList) {
								JsonLite perfInfo = future.get();
								perfList.add(perfInfo);
							}

							// Iterate the results to compute average latency
							// and aggregate rate.
							JsonLite aggregateInfo = new JsonLite();
							double lowLatencyInMsec = Double.MAX_VALUE;
							double highLatencyInMsec = Double.MIN_VALUE;
							double avgLatencyInMsec = 0d;
							double totalLatencyInMsec = 0d;
							double highTimeTookInSec = Double.MIN_VALUE;
							for (JsonLite perfInfo : perfList) {
								double latencyInMsec = (Double) perfInfo.get("LatencyInMsec");
								double timeTookInSec = (Double) perfInfo.get("TimeTookInSec");
								totalLatencyInMsec += latencyInMsec;
								if (lowLatencyInMsec > latencyInMsec) {
									lowLatencyInMsec = latencyInMsec;
								}
								if (highLatencyInMsec < latencyInMsec) {
									highLatencyInMsec = latencyInMsec;
								}
								if (highTimeTookInSec < timeTookInSec) {
									highTimeTookInSec = timeTookInSec;
								}
							}
							JsonLite perfInfo = perfList.get(0);
							int totalEntryCount = (Integer)perfInfo.get("TotalEntryCount");
							avgLatencyInMsec = totalLatencyInMsec / perfList.size();
							double rateObjectsPerSec = totalEntryCount / highTimeTookInSec;
							aggregateInfo.put("HighTimeTookInSec", highTimeTookInSec);
							aggregateInfo.put("LatencyInMsec", avgLatencyInMsec);
							aggregateInfo.put("RateObjectsPerSec", rateObjectsPerSec);
							aggregateInfo.put("LowLatencyInMsec", lowLatencyInMsec);
							aggregateInfo.put("HighLatencyInMsec", highLatencyInMsec);
							aggregateInfo.put("ServerName", perfInfo.get("ServerName"));
							aggregateInfo.put("FieldSize", perfInfo.get("FieldSize"));
							aggregateInfo.put("PayloadSize", perfInfo.get("PayloadSize"));
							aggregateInfo.put("Path", perfInfo.get("Path"));
							aggregateInfo.put("IsIncludeObjectCreationTime",
									perfInfo.get("IsIncludeObjectCreationTime"));
							aggregateInfo.put("ServerNum", perfInfo.get("ServerNum"));
							aggregateInfo.put("DriverNum", perfInfo.get("DriverNum"));
							aggregateInfo.put("DriverCount", perfInfo.get("DriverCount"));
							aggregateInfo.put("ServerCount", perfInfo.get("ServerCount"));
							aggregateInfo.put("TotalEntryCount", perfInfo.get("TotalEntryCount"));
							aggregateInfo.put("FieldCount", perfInfo.get("FieldCount"));
							aggregateInfo.put("ThreadCountPerDriver", threadCountPerDriver);

							// Log perf results in the perf log file
							perfLogger.info(aggregateInfo.toString(4, false, false));

							// Publish the perf results to clients
							PadoServerManager.getPadoServerManager().putMessage(MessageType.GridStatus, aggregateInfo);
						}
						Logger.info("StressTestBiz: BulkLoad Tests Complete. [loopCount=" + loopCount + ", pathCountPerLoop="
								+ pathCountPerLoop + "]");
					}
				} catch (Exception ex) {
					Logger.error(ex);
				} finally {
					isComplete = true;
					es.shutdown();
				}
			}
		});
		return "[DriverNum(ServerNum)=" + serverNum + "] Stress test started.";
	}
	
	@BizMethod
	public String __startQuery(final Map<String, JsonLite> pathConfigMap, final int threadCountPerDriver,
			final int loopCount, final boolean isIncludeObjectCreationTime)
	{
		String serverNum = PadoServerManager.getPadoServerManager().getServerNum();
		if (isComplete == false) {
			return "[DriverNum(ServerNum)=" + serverNum + "] Aborted. Another stress test already in progress.";
		}
		isComplete = false;

		Executors.newSingleThreadExecutor(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r)
			{
				Thread thread = Executors.defaultThreadFactory().newThread(r);
				thread.setName("StressTestBizLauncherThread");
				thread.setDaemon(true);
				return thread;
			}
		}).execute(new Runnable() {
			@Override
			public void run()
			{

				Logger.info("StressTestBiz: Query Tests Started");

				ExecutorService es = Executors.newFixedThreadPool(threadCountPerDriver, new ThreadFactory() {

					@Override
					public Thread newThread(Runnable r)
					{
						Thread thread = Executors.defaultThreadFactory().newThread(r);
						thread.setName("StressTestBizThread");
						thread.setDaemon(true);
						return thread;
					}

				});
				try {
					ArrayList<DataLoader> dataLoaderList = new ArrayList<DataLoader>();
					int pathCountPerLoop = pathConfigMap.size();
					for (int k = 0; k < loopCount; k++) {
						Collection<JsonLite> col = pathConfigMap.values();
						for (JsonLite pathConfig : col) {
							for (int j = 1; j <= threadCountPerDriver; j++) {
								DataLoader dataLoader = new DataLoader(j, threadCountPerDriver, pathConfig,
										isIncludeObjectCreationTime);
								dataLoaderList.add(dataLoader);
							}
							// Invoke all DataLoaders
							List<Future<JsonLite>> futureList = es.invokeAll(dataLoaderList);

							// Block till all threads are done. Collect
							// results in to one list.
							ArrayList<JsonLite> perfList = new ArrayList<JsonLite>();
							for (Future<JsonLite> future : futureList) {
								JsonLite perfInfo = future.get();
								perfList.add(perfInfo);
							}

							// Iterate the results to compute average latency
							// and aggregate rate.
							JsonLite aggregateInfo = new JsonLite();
							double lowLatencyInMsec = Double.MAX_VALUE;
							double highLatencyInMsec = Double.MIN_VALUE;
							double avgLatencyInMsec = 0d;
							double totalLatencyInMsec = 0d;
							double highTimeTookInSec = Double.MIN_VALUE;
							for (JsonLite perfInfo : perfList) {
								double latencyInMsec = (Double) perfInfo.get("LatencyInMsec");
								double timeTookInSec = (Double) perfInfo.get("TimeTookInSec");
								totalLatencyInMsec += latencyInMsec;
								if (lowLatencyInMsec > latencyInMsec) {
									lowLatencyInMsec = latencyInMsec;
								}
								if (highLatencyInMsec < latencyInMsec) {
									highLatencyInMsec = latencyInMsec;
								}
								if (highTimeTookInSec < timeTookInSec) {
									highTimeTookInSec = timeTookInSec;
								}
							}
							JsonLite perfInfo = perfList.get(0);
							int totalEntryCount = (Integer)perfInfo.get("TotalEntryCount");
							avgLatencyInMsec = totalLatencyInMsec / perfList.size();
							double rateObjectsPerSec = totalEntryCount / highTimeTookInSec;
							aggregateInfo.put("HighTimeTookInSec", highTimeTookInSec);
							aggregateInfo.put("LatencyInMsec", avgLatencyInMsec);
							aggregateInfo.put("RateObjectsPerSec", rateObjectsPerSec);
							aggregateInfo.put("LowLatencyInMsec", lowLatencyInMsec);
							aggregateInfo.put("HighLatencyInMsec", highLatencyInMsec);
							aggregateInfo.put("ServerName", perfInfo.get("ServerName"));
							aggregateInfo.put("FieldSize", perfInfo.get("FieldSize"));
							aggregateInfo.put("PayloadSize", perfInfo.get("PayloadSize"));
							aggregateInfo.put("Path", perfInfo.get("Path"));
							aggregateInfo.put("IsIncludeObjectCreationTime",
									perfInfo.get("IsIncludeObjectCreationTime"));
							aggregateInfo.put("ServerNum", perfInfo.get("ServerNum"));
							aggregateInfo.put("DriverNum", perfInfo.get("DriverNum"));
							aggregateInfo.put("DriverCount", perfInfo.get("DriverCount"));
							aggregateInfo.put("ServerCount", perfInfo.get("ServerCount"));
							aggregateInfo.put("TotalEntryCount", perfInfo.get("TotalEntryCount"));
							aggregateInfo.put("FieldCount", perfInfo.get("FieldCount"));
							aggregateInfo.put("ThreadCountPerDriver", threadCountPerDriver);

							// Log perf results in the perf log file
							perfLogger.info(aggregateInfo.toString(4, false, false));

							// Publish the perf results to clients
							PadoServerManager.getPadoServerManager().putMessage(MessageType.GridStatus, aggregateInfo);
						}
						Logger.info("StressTestBiz: Query Tests Complete. [loopCount=" + loopCount + ", pathCountPerLoop="
								+ pathCountPerLoop + "]");
					}
				} catch (Exception ex) {
					Logger.error(ex);
				} finally {
					isComplete = true;
					es.shutdown();
				}
			}
		});
		return "[DriverNum(ServerNum)=" + serverNum + "] Stress test started.";
	}

	@BizMethod
	public boolean isComplete()
	{
		return isComplete;
	}

	@BizMethod
	public String getStatus()
	{
		String serverNum = PadoServerManager.getPadoServerManager().getServerNum();
		boolean isComplete = isComplete();
		String status = "[DriverNum(ServerNum)=" + serverNum + ", IsComplete=" + isComplete + "]";
		return status;
	}

	private Region getRegion(String fullPath)
	{
		Cache cache = CacheFactory.getAnyInstance();
		return cache.getRegion(fullPath);
	}

	private JsonLite getPerfInfo(String serverNum, String path, int threadNum, int threadCount, int payloadSize,
			int fieldCount, int fieldSize, boolean isIncludeObjectCreationTime, int count, int totalEntryCount,
			long deltaInMsec)
	{
		double deltaInSec = (double) deltaInMsec / 1000d;
		double rate = (double) count / (double) deltaInSec;
		double latency = (double) ((double) deltaInMsec / (double) count);
		DecimalFormat format = new DecimalFormat("#,###.00");
		DecimalFormat latencyFormat = new DecimalFormat("#,###.000");

		JsonLite jl = new JsonLite();
		String serverName = PadoServerManager.getPadoServerManager().getServerName();
		int serverCount = PadoServerManager.getPadoServerManager().getServerCount();
		jl.put("ServerName", serverName); // String
		jl.put("ServerNum", serverNum); // String
		jl.put("DriverNum", serverNum); // String
		jl.put("ServerCount", serverCount); // int
		jl.put("DriverCount", serverCount); // int
		jl.put("Path", path); // String
		jl.put("EntryCount", count); // int
		jl.put("ThreadNum", threadNum); // int
		jl.put("ThreadCount", threadCount); // int
		jl.put("TotalEntryCount", totalEntryCount); // int
		jl.put("PayloadSize", payloadSize); // int
		jl.put("FieldCount", fieldCount); // int
		jl.put("FieldSize", fieldSize); // int
		jl.put("IsIncludeObjectCreationTime", isIncludeObjectCreationTime); // boolean
		jl.put("TimeTookInMsec", deltaInMsec); // long
		jl.put("TimeTookInSec", deltaInSec); // double
		jl.put("RateObjectsPerSec", rate); // double
		jl.put("RateObjectsPerSecString", format.format(rate)); // String
		jl.put("LatencyInMsec", latency); // double
		jl.put("LatencyInMsecString", latencyFormat.format(latency)); // String

		return jl;
	}

	private class DataLoader implements Callable<JsonLite>
	{
		private int threadNum;
		private int threadCount;
		JsonLite pathConfig;
		private boolean isIncludeObjectCreationTime = true;

		DataLoader(int threadNum, int threadCount, JsonLite pathConfig, boolean isIncludeObjectCreationTime)
		{
			this.threadNum = threadNum;
			this.threadCount = threadCount;
			// Set default values for fieldCount, payloadSize, totalEntryCount
			this.pathConfig = pathConfig;
			if (pathConfig.get("FieldCount") == null) {
				pathConfig.put("FieldCount", 20);
			}
			if (pathConfig.get("PayloadSize") == null) {
				pathConfig.put("PayloadSize", 1024);
			}
			if (pathConfig.get("TotalEntryCount") == null) {
				pathConfig.put("TotalEntryCount", 10000);
			}
			if (pathConfig.get("BatchSize") == null) {
				pathConfig.put("BatchSize", 1000);
			}
			if (pathConfig.get("UpdateIntervalInMsec") == null) {
				pathConfig.put("UpdateIntervalInMsec", 0);
			}
			this.isIncludeObjectCreationTime = isIncludeObjectCreationTime;
		}

		@Override
		public JsonLite call() throws Exception
		{
			String serverNum = PadoServerManager.getPadoServerManager().getServerNum();
			int serverCount = PadoServerManager.getPadoServerManager().getServerCount();
			String path = (String) pathConfig.get("Path");
			int totalEntryCount = (Integer) pathConfig.get("TotalEntryCount");
			int fieldCount = (Integer) pathConfig.get("FieldCount");
			int payloadSize = (Integer) pathConfig.get("PayloadSize");
			int batchSize = (Integer) pathConfig.get("BatchSize");
			int updateIntervalInMsec = (Integer) pathConfig.get("UpdateIntervalInMsec");

			int entryCountPerDriver = totalEntryCount / serverCount;
			int fieldSize = payloadSize / fieldCount;

			IBulkLoader bulkLoader;

			// If path is full path then assume GemFire region. This allows the
			// server to
			// act as a client to a GemFire cluster.
			if (path.startsWith("/")) {

				// Client-to-cluster (Must configure region in server.xml with
				// client pool)
				Region region = getRegion(path);
				if (updateIntervalInMsec > 0) {
					bulkLoader = new RegionBulkLoaderThrottled(region, serverNum, path, threadNum, threadCount,
							payloadSize, fieldCount, fieldSize, isIncludeObjectCreationTime, entryCountPerDriver,
							updateIntervalInMsec);
					((RegionBulkLoaderThrottled) bulkLoader).setStartTime(System.currentTimeMillis());
				} else {
					bulkLoader = new RegionBulkLoader(region);
				}
				bulkLoader.setBatchSize(batchSize);

			} else {

				// Server-to-cluster
				Region rootRegion = GemfirePadoServerManager.getPadoServerManager().getRootRegion();
				Region region = rootRegion.getSubregion(path);
				IPathBiz.PathType pathType = PathBizImpl.getPathType(region);

				switch (pathType) {
				case TEMPORAL:
				case TEMPORAL_PERSISTENT:
				case TEMPORAL_PERSISTENT_OVERFLOW:
				case TEMPORAL_OVERFLOW:
				case TEMPORAL_LUCENE:
				case TEMPORAL_LUCENE_PERSISTENT:
				case TEMPORAL_LUCENE_PERSISTENT_OVERFLOW:
				case TEMPORAL_LUCENE_OVERFLOW:
					ITemporalBizLink<String, JsonLite> temporalBiz = temporalBizThreadPool.getBiz();
					temporalBiz.setGridPath(path);
					bulkLoader = temporalBiz.getTemporalAdminBiz().createBulkLoader(batchSize);
					break;
				default:
					if (updateIntervalInMsec > 0) {
						bulkLoader = new RegionBulkLoaderThrottled(region, serverNum, path, threadNum, threadCount,
								payloadSize, fieldCount, fieldSize, isIncludeObjectCreationTime, entryCountPerDriver,
								updateIntervalInMsec);
						((RegionBulkLoaderThrottled) bulkLoader).setStartTime(System.currentTimeMillis());
					} else {
						bulkLoader = new RegionBulkLoader(region);
					}
					bulkLoader.setBatchSize(batchSize);
					break;
				}
			}

			JsonLite value;
			long startTime;
			long endTime;
			int entryCountPerThread = entryCountPerDriver / threadCount;
			int startIndex = (threadNum - 1) * entryCountPerThread + 1;
			int endIndex = startIndex + entryCountPerThread - 1;
			if (isIncludeObjectCreationTime) {
				startTime = System.currentTimeMillis();
				for (int i = startIndex; i <= endIndex; i++) {
					String key = serverNum + i;
					value = createObject(fieldCount, fieldSize);
					bulkLoader.put(key, value);
				}
				bulkLoader.flush();
				endTime = System.currentTimeMillis();
			} else {
				value = createObject(fieldCount, fieldSize);
				startTime = System.currentTimeMillis();
				for (int i = startIndex; i <= endIndex; i++) {
					String key = serverNum + i;
					bulkLoader.put(key, value);
				}
				bulkLoader.flush();
				endTime = System.currentTimeMillis();
			}

			// If throttled then return the mid point perf info from the
			// collection.
			if (bulkLoader instanceof RegionBulkLoaderThrottled) {
				long totalTimeTookInMsec = ((RegionBulkLoaderThrottled) bulkLoader).getTotalTimeTookInMsec();
				return getPerfInfo(serverNum, path, threadNum, threadCount, payloadSize, fieldCount, fieldSize,
						isIncludeObjectCreationTime, entryCountPerThread, entryCountPerDriver, totalTimeTookInMsec);
			} else {
				return getPerfInfo(serverNum, path, threadNum, threadCount, payloadSize, fieldCount, fieldSize,
						isIncludeObjectCreationTime, entryCountPerThread, entryCountPerDriver, endTime - startTime);
			}
		}

		private JsonLite createObject(int fieldCount, int fieldSize)
		{
			JsonLite jl = new JsonLite();
			for (int i = 0; i < fieldCount; i++) {
				jl.put("f" + i, createField(fieldSize));
			}
			return jl;
		}

		private String createField(int fieldSize)
		{
			StringBuffer buffer = new StringBuffer(fieldSize);
			for (int i = 0; i < fieldSize; i++) {
				buffer.append('a');
			}
			return buffer.toString();
		}
	}

	class RegionBulkLoaderThrottled<K, V> extends RegionBulkLoader<K, V>
	{
		int flushNum = 0;
		int totalEntryCount = 0;
		long startTime;
		long totalTimeTookInMsec;
		ArrayList<JsonLite> perfList = new ArrayList<JsonLite>();

		String serverNum;
		String path;
		int threadNum;
		int threadCount;
		int payloadSize;
		int fieldCount;
		int fieldSize;
		boolean isIncludeObjectCreationTime;
		int entryCountPerDriver;
		long updateIntervalInMsec;

		RegionBulkLoaderThrottled(Region region, String serverNum, String path, int threadNum, int threadCount,
				int payloadSize, int fieldCount, int fieldSize, boolean isIncludeObjectCreationTime,
				int entryCountPerDriver, long updateIntervalInMsec)
		{
			super(region);
			this.serverNum = serverNum;
			this.path = path;
			this.threadNum = threadNum;
			this.threadCount = threadCount;
			this.payloadSize = payloadSize;
			this.fieldCount = fieldCount;
			this.fieldSize = fieldSize;
			this.isIncludeObjectCreationTime = isIncludeObjectCreationTime;
			this.entryCountPerDriver = entryCountPerDriver;
			this.updateIntervalInMsec = updateIntervalInMsec;
		}

		public void setStartTime(long startTime)
		{
			this.startTime = startTime;
		}

		/**
		 * Returns the total time took to complete all of the batches.
		 */
		public long getTotalTimeTookInMsec()
		{
			return totalTimeTookInMsec;
		}

		@Override
		public void flush()
		{
			int count = map.size();
			if (count > 0 && region != null) {
				region.putAll(map);
				long endTime = System.currentTimeMillis();
				totalTimeTookInMsec += (endTime - startTime);
				map.clear();
				try {
					Thread.sleep(updateIntervalInMsec);
				} catch (InterruptedException ex) {
					Logger.error(ex);
				}
				startTime = System.currentTimeMillis();
			}
		}
	}
}
