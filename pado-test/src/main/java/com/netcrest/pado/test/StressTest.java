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
package com.netcrest.pado.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import com.netcrest.pado.IMessageListener;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.info.message.MessageType;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.test.biz.IStressTestBiz;

@SuppressWarnings({ "rawtypes" })
public class StressTest
{
	private IPado pado;
	private final java.util.logging.Logger perfLogger;
	private final java.util.logging.Logger driverLogger;

	public StressTest()
	{
		Pado.connect();
		pado = Pado.login();

		perfLogger = createLogger("perf/stress_test_perf.log", false);
		perfLogger.setUseParentHandlers(true);
		driverLogger = createLogger("perf/stress_test_driver.log", true);
		driverLogger.setUseParentHandlers(false);
	}

	private java.util.logging.Logger createLogger(String filePath, boolean suppressConsole)
	{
		java.util.logging.Logger logger = java.util.logging.Logger
				.getLogger(filePath);
		
//		if (suppressConsole) {
//			// suppress the logging output to the console
//			java.util.logging.Handler[] handlers = logger.getHandlers();
//			for (java.util.logging.Handler handler : handlers) {
//				if (handlers[0] instanceof java.util.logging.ConsoleHandler) {
//					logger.removeHandler(handler);
//				}
//			}
//		}
		

		logger.setLevel(java.util.logging.Level.INFO);
		try {
			java.util.logging.FileHandler fileTxt = new java.util.logging.FileHandler(filePath);
			java.util.logging.SimpleFormatter formatterTxt = new java.util.logging.SimpleFormatter();
			fileTxt.setFormatter(formatterTxt);
			logger.addHandler(fileTxt);
		} catch (Exception ex) {
			Logger.error(ex);
		}

		return logger;
	}

	@SuppressWarnings("unchecked")
	public void start() throws FileNotFoundException, IOException, InterruptedException
	{
		JsonLite config = new JsonLite(new File("etc/client/StressTest.json"));
		writeLine(config.toString(4, false, false));
		IStressTestBiz stressTestBiz = pado.getCatalog().newInstance(IStressTestBiz.class);
		Object[] paths = (Object[]) config.get("Paths");
		for (Object obj : paths) {
			JsonLite jl = (JsonLite) obj;
			String pathType = (String) jl.get("PathType");
			if (pathType != null) {
				IPathBiz.PathType pt = IPathBiz.PathType.valueOf(pathType.toUpperCase());
				jl.put("PathType", pt);
			}
			stressTestBiz.addPath(jl);
		}

		String testType = (String) config.get("TestType");
		if (testType == null) {
			testType = "BulkLoad";
		}

		int threadCountPerDriver = 5;
		int loopCount = 1;
		boolean isIncludeObjectCreationTime = false;
		int batchSize = 1000;
		if (config.get("ThreadCountPerDriver") != null) {
			threadCountPerDriver = (Integer) config.get("ThreadCountPerDriver");
		}
		if (config.get("LoopCount") != null) {
			loopCount = (Integer) config.get("LoopCount");
		}
		if (config.get("IsIncludeObjectCreationTime") != null) {
			isIncludeObjectCreationTime = (Boolean) config.get("IsIncludeObjectCreationTime");
		}
		if (config.get("BatchSize") != null) {
			batchSize = (Integer) config.get("BatchSize");
		}
		stressTestBiz.setThreadCountPerDriver(threadCountPerDriver);
		stressTestBiz.setLoopCount(loopCount);
		stressTestBiz.setIncludeObjectCreationTime(isIncludeObjectCreationTime);
		stressTestBiz.setBatchSize(batchSize);

		// Add PerfListener for collecting and aggregating results
		pado.addMessageListener(new PerfListener(threadCountPerDriver));

		List<String> statusList;
		if (testType.equalsIgnoreCase("BulkLoad")) {
			statusList = stressTestBiz.start();
		} else if (testType.equalsIgnoreCase("Query")) {
			statusList = stressTestBiz.startQuery();
		} else if (testType.equalsIgnoreCase("TX")) {
			statusList = stressTestBiz.startTx();
		} else {
			statusList = stressTestBiz.start();
		}
		for (String status : statusList) {
			writeLine(status);
		}

		while (true) {
			Thread.sleep(1000);
		}
	}

	public void close()
	{
		Pado.close();
	}

	class PerfListener implements IMessageListener
	{
		int testNum = 0;
		int threadCountPerDriver;
		DecimalFormat rateFormat = new DecimalFormat("#,###.00");
		DecimalFormat latencyFormat = new DecimalFormat("#,###.000");

		PerfListener(int threadCountPerDriver)
		{
			this.threadCountPerDriver = threadCountPerDriver;
		}

		HashMap<String, List<PerfMetrics>> metricMap = new HashMap<String, List<PerfMetrics>>();
		HashMap<String, List<JsonLite>> messageMap = new HashMap<String, List<JsonLite>>();

		@Override
		public void messageReceived(MessageType messageType, Object message)
		{
			if (messageType == MessageType.GridStatus) {
				if (message instanceof JsonLite) {
					JsonLite jl = (JsonLite) message;
					String path = (String) jl.get("Path");
					List<PerfMetrics> list = metricMap.get(path);
					if (list == null) {
						list = new ArrayList<PerfMetrics>();
						metricMap.put(path, list);
					}
					List<JsonLite> messageList = messageMap.get(path);
					if (messageList == null) {
						messageList = new ArrayList<JsonLite>();
						messageMap.put(path, messageList);
					}
					messageList.add(jl);
					if (path != null) {
						PerfMetrics pm = new PerfMetrics();
						pm.highTimeTookInSec = (Double) jl.get("HighTimeTookInSec");
						pm.latencyInMsec = (Double) jl.get("LatencyInMsec");
						pm.rateObjectsPerSec = (Double) jl.get("RateObjectsPerSec");
						pm.driverCount = (Integer) jl.get("DriverCount");
						pm.totalEntryCount = (Integer) jl.get("TotalEntryCount");
						pm.lowLatencyInMsec = (Double) jl.get("LowLatencyInMsec");
						pm.highLatencyInMsec = (Double) jl.get("HighLatencyInMsec");
						list.add(pm);

						if (pm.driverCount == list.size()) {
							int driverCount = pm.driverCount;
							int fieldSize = (Integer) jl.get("FieldSize");
							int fieldCount = (Integer) jl.get("FieldCount");
							int payloadSize = (Integer) jl.get("PayloadSize");

							double highTimeTookInSec = Double.MIN_VALUE;
							double lowLatencyInMsec = Double.MAX_VALUE;
							double highLatencyInMsec = Double.MIN_VALUE;
							double totalLatency = 0d;
							int totalEntryCount = 0;

							for (PerfMetrics pm2 : list) {
								totalLatency += pm2.latencyInMsec;
								totalEntryCount += pm2.totalEntryCount;

								if (highTimeTookInSec < pm2.highTimeTookInSec) {
									highTimeTookInSec = pm2.highTimeTookInSec;
								}
								if (lowLatencyInMsec > pm2.lowLatencyInMsec) {
									lowLatencyInMsec = pm2.lowLatencyInMsec;
								}
								if (highLatencyInMsec < pm2.highLatencyInMsec) {
									highLatencyInMsec = pm2.highLatencyInMsec;
								}
								if (lowLatencyInMsec > highLatencyInMsec) {
									lowLatencyInMsec = highLatencyInMsec;
								}
								if (highLatencyInMsec < lowLatencyInMsec) {
									highLatencyInMsec = lowLatencyInMsec;
								}
							}
							double aggregateRate = totalEntryCount / highTimeTookInSec;
							double avgLatency = totalLatency / list.size();
							testNum++;
							StringBuffer buffer = new StringBuffer(2000);
							buffer.append("\n");
							buffer.append(testNum + ". BulkLoad Test");
							buffer.append("\n                            Path:" + path);
							buffer.append("\n PayloadSize (approximate chars): " + payloadSize);
							buffer.append("\n           Averge Latency (msec): " + latencyFormat.format(avgLatency));
							buffer.append("\n              Low Latency (msec): " + latencyFormat.format(lowLatencyInMsec));
							buffer.append("\n             High Latency (msec): " + latencyFormat.format(highLatencyInMsec));
							buffer.append("\n  Aggregate Throughput (obj/sec): " + rateFormat.format(aggregateRate));
							buffer.append("\n                 TotalEntryCount: " + totalEntryCount);
							buffer.append("\n             Elapsed Time (sec): " + latencyFormat.format(highTimeTookInSec));
							buffer.append("\n                     DriverCount: " + driverCount);
							buffer.append("\n            ThreadCountPerDriver: " + threadCountPerDriver);
							buffer.append("\n               FieldSize (chars): " + fieldSize);
							buffer.append("\n                      FieldCount: " + fieldCount);

							// Log perf metrics
							perfLogger.log(Level.INFO, buffer.toString());

							// Log each driver message
							for (JsonLite jl2 : messageList) {
								String driverNum = (String) jl.get("DriverNum");
								driverCount = (Integer) jl.get("DriverCount");
								driverLogger.log(Level.INFO, testNum + ". BulkLoad Test Driver [" + driverNum + "/"
										+ driverCount + "]\n" + jl2.toString(4, false, false));
							}

							// Clean up the maps
							metricMap.remove(path);
							messageMap.remove(path);
						}
					}

				}
			}
		}

		class PerfMetrics
		{
			double highTimeTookInSec;
			double latencyInMsec;
			double rateObjectsPerSec;
			int driverCount;
			int totalEntryCount;
			double lowLatencyInMsec;
			double highLatencyInMsec;
		}

	}

	private static void writeLine()
	{
		System.out.println();
	}

	private static void writeLine(String line)
	{
		System.out.println(line);
	}

	private static void write(String str)
	{
		System.out.print(str);
	}

	private static void usage()
	{
		writeLine();
		writeLine("Usage:");
		writeLine("   StressTest [-?]");
		writeLine();
		writeLine("   Default: StressTest");
		writeLine("   StressTest writes mock data into the grid by all servers.");
		writeLine("      - Set test criteria in etc/client/StressTest.json");
		writeLine("      - Each server outputs results in perf/perf.txt");
		writeLine();
		System.exit(0);
	}

	public static void main(String[] args) throws Exception
	{
		String arg;
		for (int i = 0; i < args.length; i++) {
			arg = args[i];
			if (arg.equalsIgnoreCase("-?")) {
				usage();
			}
		}

		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		StressTest client = new StressTest();
		client.start();
		client.close();
	}
}
