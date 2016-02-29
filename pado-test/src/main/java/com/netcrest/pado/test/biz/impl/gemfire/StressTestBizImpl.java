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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Resource;

import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.info.message.MessageType;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class StressTestBizImpl
{
	@Resource
	private IBizContextServer bizContext;
	private static volatile boolean isComplete = true;

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
			java.util.logging.FileHandler fileTxt = new java.util.logging.FileHandler(
					homeDir + "/perf/driver_perf.log");
			java.util.logging.SimpleFormatter formatterTxt = new java.util.logging.SimpleFormatter();
			fileTxt.setFormatter(formatterTxt);
			perfLogger.addHandler(fileTxt);
		} catch (Exception ex) {
			Logger.error(ex);
		}
	}

	@BizMethod
	public String __start(final JsonLite request)
	{
		String testType = (String) request.get("TestType");
		if (testType.equalsIgnoreCase("BulkLoad")) {
			return startBulkLoad(request);
		} else if (testType.equalsIgnoreCase("Tx")) {
			return startTx(request);
		} else {
			return "Undefined TestType. Valid types are BulkLoad, TX";
		}
	}

	private String startBulkLoad(final JsonLite bulkLoadRequest)
	{
		final String serverName = PadoServerManager.getPadoServerManager().getServerName();
		if (isComplete == false) {
			return "[DriverName(ServerNme)=" + serverName + "] Aborted. Another stress test already in progress.";
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

				final int threadCountPerDriver = (Integer) bulkLoadRequest.get("ThreadCountPerDriver", 1);
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
					final int loopCount = (Integer) bulkLoadRequest.get("LoopCount", 1);
					ArrayList<BulkLoaderTask> dataLoaderTaskList = new ArrayList<BulkLoaderTask>();

					Object[] paths = (Object[]) bulkLoadRequest.get("Paths");
					int pathCountPerLoop = paths.length;
					boolean isIncludeObjectCreationTime = (Boolean) bulkLoadRequest.get("IsIncludeObjectCreationTime",
							false);
					int updateIntervalInMsec = (Integer) bulkLoadRequest.get("UpdateIntervalInMsec", 0);
					int batchSize = (Integer) bulkLoadRequest.get("UpdateIntervalInMsec", 1000);
					String keyPrefix = (String) bulkLoadRequest.get("KeyPrefix", "");

					// Each loop launches the number of threads defined by
					// threadCountPerDriver for each path. It publishes each
					// path results by aggregating thread results to the client.
					for (int loopNum = 1; loopNum <= loopCount; loopNum++) {

						// Run BulkLoaderTask in threads for each path.
						int pathNum = 0;
						for (Object obj : paths) {
							pathNum++;
							JsonLite pathConfig = (JsonLite) obj;
							dataLoaderTaskList.clear();
							for (int j = 1; j <= threadCountPerDriver; j++) {
								BulkLoaderTask bulkLoaderTask = new BulkLoaderTask(j, threadCountPerDriver, pathConfig,
										isIncludeObjectCreationTime, batchSize, updateIntervalInMsec, keyPrefix);
								dataLoaderTaskList.add(bulkLoaderTask);
							}
							// Invoke all DataLoaders
							List<Future<JsonLite>> futureList = es.invokeAll(dataLoaderTaskList);

							// Block till all threads are done. Collect
							// results into one list.
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
							int totalEntryCount = (Integer) perfInfo.get("TotalEntryCount");
							avgLatencyInMsec = totalLatencyInMsec / perfList.size();
							double rateObjectsPerSec = totalEntryCount / highTimeTookInSec;
							aggregateInfo.put("Token", bulkLoadRequest.get("Token"));
							aggregateInfo.put("TestType", bulkLoadRequest.get("TestType"));
							aggregateInfo.put("LoopNum", loopNum);
							aggregateInfo.put("LoopCount", loopCount);
							aggregateInfo.put("PathNum", pathNum);
							aggregateInfo.put("PathCount", pathCountPerLoop);
							aggregateInfo.put("HighTimeTookInSec", highTimeTookInSec);
							aggregateInfo.put("LatencyInMsec", avgLatencyInMsec);
							aggregateInfo.put("RateObjectsPerSec", rateObjectsPerSec);
							aggregateInfo.put("LowLatencyInMsec", lowLatencyInMsec);
							aggregateInfo.put("HighLatencyInMsec", highLatencyInMsec);
							aggregateInfo.put("DriverName", serverName);
							aggregateInfo.put("FieldSize", perfInfo.get("FieldSize"));
							aggregateInfo.put("PayloadSize", perfInfo.get("PayloadSize"));
							aggregateInfo.put("Path", perfInfo.get("Path"));
							aggregateInfo.put("IsIncludeObjectCreationTime",
									perfInfo.get("IsIncludeObjectCreationTime"));
							aggregateInfo.put("DriverNum", perfInfo.get("DriverNum"));
							aggregateInfo.put("DriverCount", perfInfo.get("DriverCount"));
							aggregateInfo.put("TotalEntryCount", perfInfo.get("TotalEntryCount"));
							aggregateInfo.put("FieldCount", perfInfo.get("FieldCount"));
							aggregateInfo.put("ThreadCountPerDriver", threadCountPerDriver);

							// Log perf results in the perf log file
							perfLogger.info(aggregateInfo.toString(4, false, false));

							// Publish the perf results to clients
							PadoServerManager.getPadoServerManager().putMessage(MessageType.GridStatus, aggregateInfo);
						}
						Logger.info("StressTestBiz: BulkLoad Tests Complete. [loopCount=" + loopCount
								+ ", pathCountPerLoop=" + pathCountPerLoop + "]");
					}
				} catch (Exception ex) {
					Logger.error(ex);
				} finally {
					isComplete = true;
					es.shutdown();
				}
			}
		});
		return "[DriverName(ServerName)=" + serverName + "] Stress test started.";
	}

	private String startTx(final JsonLite txRequest)
	{
		final String serverName = PadoServerManager.getPadoServerManager().getServerName();
		if (isComplete == false) {
			return "[DeriverName(ServerName)=" + serverName + "] Aborted. Another stress test already in progress.";
		}
		isComplete = false;
		final int serverCount = PadoServerManager.getPadoServerManager().getServerCount();

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

				Logger.info("StressTestBiz: Tx Tests Started");

				final int threadCountPerDriver = (Integer) txRequest.get("ThreadCountPerDriver");
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
					final int loopCount = (Integer) txRequest.get("LoopCount", 1);
					final int txCount = (Integer) txRequest.get("TxCount", 1000);
					ArrayList<TxTask> txTaskList = new ArrayList<TxTask>();

					int txCountPerServer = txCount / serverCount;
					int txCountPerThread = txCountPerServer / threadCountPerDriver;

					for (int j = 1; j <= threadCountPerDriver; j++) {
						TxTask txTask = new TxTask(txRequest, j, threadCountPerDriver, txCountPerThread);
						txTaskList.add(txTask);
					}

					for (int loopNum = 1; loopNum <= loopCount; loopNum++) {

						// Invoke all TxTasks
						List<Future<JsonLite>> futureList = es.invokeAll(txTaskList);

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
						int totalEntryCount = (Integer) perfInfo.get("TotalTxCount");
						avgLatencyInMsec = totalLatencyInMsec / perfList.size();
						double rateObjectsPerSec = totalEntryCount / highTimeTookInSec;
						aggregateInfo.put("Token", txRequest.get("Token"));
						aggregateInfo.put("TestType", txRequest.get("TestType"));
						aggregateInfo.put("LoopNum", loopNum);
						aggregateInfo.put("LoopCount", loopCount);
						aggregateInfo.put("HighTimeTookInSec", highTimeTookInSec);
						aggregateInfo.put("LatencyInMsec", avgLatencyInMsec);
						aggregateInfo.put("RateObjectsPerSec", rateObjectsPerSec);
						aggregateInfo.put("LowLatencyInMsec", lowLatencyInMsec);
						aggregateInfo.put("HighLatencyInMsec", highLatencyInMsec);
						aggregateInfo.put("FieldSize", perfInfo.get("FieldSize"));
						aggregateInfo.put("PayloadSize", perfInfo.get("PayloadSize"));
						aggregateInfo.put("Path", perfInfo.get("Path"));
						aggregateInfo.put("IsIncludeObjectCreationTime", perfInfo.get("IsIncludeObjectCreationTime"));
						aggregateInfo.put("DriverName", serverName);
						aggregateInfo.put("DriverNum", perfInfo.get("DriverNum"));
						aggregateInfo.put("DriverCount", perfInfo.get("DriverCount"));
						aggregateInfo.put("TotalTxCount", perfInfo.get("TotalTxCount"));
						aggregateInfo.put("ThreadCountPerDriver", threadCountPerDriver);

						// Log perf results in the perf log file
						perfLogger.info(aggregateInfo.toString(4, false, false));

						// Publish the perf results to clients
						PadoServerManager.getPadoServerManager().putMessage(MessageType.GridStatus, aggregateInfo);
					}
					Logger.info("StressTestBiz: Tx Tests Complete. [txCount=" + txCount + ", txCountPerServer="
							+ txCountPerServer + ", txCountPerThread=" + txCountPerThread + "]");

				} catch (Exception ex) {
					Logger.error(ex);
				} finally {
					isComplete = true;
					es.shutdown();
				}
			}
		});
		return "[DriverName(ServerName)=" + serverName + "] Stress test started.";
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
}
