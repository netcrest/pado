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

import javax.annotation.Resource;

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

	public StressTestBizImpl()
	{
	}

	@BizMethod
	public String __start(final Map<String, JsonLite> pathConfigMap, final int threadCountPerServer,
			final int loopCount, final boolean isIncludeObjectCreationTime)
	{
		String serverNum = PadoServerManager.getPadoServerManager().getServerNum();
		if (isComplete == false) {
			return "[serverNum=" + serverNum + "] Aborted. Another stress test already in progress.";
		}
		isComplete = false;

		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				try {
					ExecutorService es = Executors.newFixedThreadPool(threadCountPerServer);
					ArrayList<DataLoader> dataLoaderList = new ArrayList<DataLoader>();
					int pathCountPerLoop = pathConfigMap.size();
					for (int i = 0; i < loopCount; i++) {
						Collection<JsonLite> col = pathConfigMap.values();
						for (JsonLite pathConfig : col) {
							DataLoader dataLoader = new DataLoader(pathConfig, isIncludeObjectCreationTime);
							dataLoaderList.add(dataLoader);
						}
					}

					// Invoke all DataLoaders
					int dataLoaderCount = dataLoaderList.size();
					List<Future<String>> futureList = es.invokeAll(dataLoaderList);

					// Block till all DataLoaders are done
					int i = 0;
					for (Future<String> future : futureList) {
						String info = future.get();
						i++;
						Logger.info("[" + i + "/" + dataLoaderCount + "] " + info);
					}
					Logger.info("StressTestBiz: Complete. [loopCount=" + loopCount + ", pathCountPerLoop="
							+ pathCountPerLoop + "]");
				} catch (Exception ex) {
					Logger.error(ex);
				} finally {
					isComplete = true;
				}
			}
		});
		return "[ServerNum=" + serverNum + "] Stress test started.";
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
		String status = "[ServerNum=" + serverNum + ", IsComplete=" + isComplete + "]";
		return status;
	}

	private class DataLoader implements Callable<String>
	{
		JsonLite pathConfig;
		private boolean isIncludeObjectCreationTime = true;

		DataLoader(JsonLite pathConfig, boolean isIncludeObjectCreationTime)
		{
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
			this.isIncludeObjectCreationTime = isIncludeObjectCreationTime;
		}

		@Override
		public String call() throws Exception
		{
			String serverNum = PadoServerManager.getPadoServerManager().getServerNum();
			int serverCount = PadoServerManager.getPadoServerManager().getServerCount();
			String path = (String) pathConfig.get("Path");
			int totalEntryCount = (Integer) pathConfig.get("TotalEntryCount");
			int fieldCount = (Integer) pathConfig.get("FieldCount");
			int payloadSize = (Integer) pathConfig.get("PayloadSize");

			int entryCountPerServer = totalEntryCount / serverCount;
			int fieldSize = payloadSize / fieldCount;

			Region rootRegion = GemfirePadoServerManager.getPadoServerManager().getRootRegion();
			Region region = rootRegion.getSubregion(path);
			IPathBiz.PathType pathType = PathBizImpl.getPathType(region);

			IBulkLoader bulkLoader;
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
				bulkLoader = temporalBiz.getTemporalAdminBiz().createBulkLoader(1000);
				break;
			default:
				bulkLoader = new RegionBulkLoader(region);
				break;
			}

			JsonLite value;
			long startTime;
			long endTime;
			if (isIncludeObjectCreationTime) {
				startTime = System.currentTimeMillis();
				for (int i = 0; i < entryCountPerServer; i++) {
					String key = serverNum + i;
					value = createObject(fieldCount, fieldSize);
					bulkLoader.put(key, value);
				}
				bulkLoader.flush();
				endTime = System.currentTimeMillis();
			} else {
				value = createObject(fieldCount, fieldSize);
				startTime = System.currentTimeMillis();
				for (int i = 0; i < entryCountPerServer; i++) {
					String key = serverNum + i;
					bulkLoader.put(key, value);
				}
				bulkLoader.flush();
				endTime = System.currentTimeMillis();
			}

			long deltaInMsec = endTime - startTime;
			double deltaInSec = (double) deltaInMsec / 1000d;
			double rate = (double) entryCountPerServer / (double) deltaInSec;
			double latency = (double) ((double) deltaInMsec / (double) entryCountPerServer);
			DecimalFormat format = new DecimalFormat("#,###.00");
			String info = "StressTestBiz: ServerId=" + serverNum + ", Path=" + path + ", EntryCount="
					+ entryCountPerServer + ", PayloadSize=" + payloadSize + ", FieldCount=" + fieldCount
					+ ", FieldSize=" + fieldSize + ", IsIncludeObjectCreationTime=" + isIncludeObjectCreationTime
					+ ", TimeTookInSec=" + format.format(deltaInSec) + ", Rate(objects/sec)=" + format.format(rate)
					+ ", Latency(msec)=" + format.format(latency);
			return info;
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
}
