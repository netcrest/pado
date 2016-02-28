package com.netcrest.pado.test.biz.impl.gemfire;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.Callable;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.CacheTransactionManager;
import com.gemstone.gemfire.cache.Region;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.server.PadoServerManager;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TxTask implements Callable<JsonLite>
{
	private int threadNum;
	private int threadCount;
	private int txCountPerThread;
	JsonLite txRequest;
	private boolean txEnabled = false;
	private boolean isIncludeObjectCreationTime = true;
	private String keyPrefix = "";

	TxTask(JsonLite txRequest, int threadNum, int threadCount, int txCountPerThread)
	{
		this.txRequest = txRequest;
		this.threadNum = threadNum;
		this.threadCount = threadCount;
		this.txCountPerThread = txCountPerThread;
		this.isIncludeObjectCreationTime = (Boolean) txRequest.get("IsIncludeObjectCreationTime", false);
		this.txEnabled = (Boolean) txRequest.get("TxEnabled", false);
		this.keyPrefix = (String) txRequest.get("KeyPrefix", "");
	}

	@Override
	public JsonLite call() throws Exception
	{
		Cache cache = CacheFactory.getAnyInstance();
		String serverNum = PadoServerManager.getPadoServerManager().getServerNum();
		Object[] txTaskRequestItems = (Object[]) txRequest.get("TxTaskItems");
		GemfireTxTaskItem[] gemfireTaskItems = new GemfireTxTaskItem[txTaskRequestItems.length];
		List<JsonLite>[] putBatches = new List[gemfireTaskItems.length];

		// Create object batch for each PUT is IsPputPrevGetValue is false

		int i = 0;
		for (Object obj : txTaskRequestItems) {
			JsonLite jl = (JsonLite) obj;
			GemfireTxTaskItem item = new GemfireTxTaskItem();
			String fullPath = (String) jl.get("Path");
			item.region = cache.getRegion(fullPath);
			int payloadSize = (Integer) jl.get("PayloadSize", 1024);
			item.fieldCount = (Integer) jl.get("FieldCount", 20);
			item.fieldSize = payloadSize / item.fieldCount;
			String itemType = (String) jl.get("ItemType", "Put");
			item.type = TxItemType.valueOf(itemType.toUpperCase());
			item.isPutPrevGetValue = (Boolean) jl.get("PutPrevGetValue", false);
			gemfireTaskItems[i] = item;

			if (item.type == TxItemType.PUT) {
				putBatches[i] = StressTestUtil.createBatchOfObjects(100, item.fieldCount, item.fieldSize);
			}
			i++;
		}

		int startIndex = (threadNum - 1) * txCountPerThread + 1;
		int endIndex = startIndex + txCountPerThread - 1;
		long elapsedTime = runGemfire(gemfireTaskItems, putBatches, serverNum, startIndex, endIndex);
		return getTxPerfInfoPerThread(serverNum, threadNum, threadCount, isIncludeObjectCreationTime, txCountPerThread,
				txCountPerThread * threadCount, elapsedTime);
	}

	private JsonLite getTxPerfInfoPerThread(String serverNum, int threadNum, int threadCount,
			boolean isIncludeObjectCreationTime, int txCountPerThread, int totalTxCount, long deltaInMsec)
	{
		double deltaInSec = (double) deltaInMsec / 1000d;
		double rate = (double) txCountPerThread / (double) deltaInSec;
		double latency = (double) ((double) deltaInMsec / (double) txCountPerThread);
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
		jl.put("TxCountPerThread", txCountPerThread); // int
		jl.put("ThreadNum", threadNum); // int
		jl.put("ThreadCount", threadCount); // int
		jl.put("TotalTxCount", totalTxCount); // int
		jl.put("IsIncludeObjectCreationTime", isIncludeObjectCreationTime); // boolean
		jl.put("TimeTookInMsec", deltaInMsec); // long
		jl.put("TimeTookInSec", deltaInSec); // double
		jl.put("RateObjectsPerSec", rate); // double
		jl.put("RateObjectsPerSecString", format.format(rate)); // String
		jl.put("LatencyInMsec", latency); // double
		jl.put("LatencyInMsecString", latencyFormat.format(latency)); // String

		return jl;
	}

	private long runGemfire(GemfireTxTaskItem[] items, List<JsonLite>[] putBatches, String serverNum, int startIndex,
			int endIndex)
	{
		long startTime;
		long endTime;

		if (txEnabled) {

			// Use TransactionManager

			CacheTransactionManager txMgr = CacheFactory.getAnyInstance().getCacheTransactionManager();
			startTime = System.currentTimeMillis();
			for (int i = startIndex; i <= endIndex; i++) {
				txMgr.begin();
				transact(items, putBatches, serverNum, i);
				txMgr.commit();
			}
			endTime = System.currentTimeMillis();

		} else {

			// Do NOT use TransactionManager

			startTime = System.currentTimeMillis();
			if (items.length == 1) {

				// If one item then do not compare isPut
				GemfireTxTaskItem item = items[0];
				singleOperation(item, putBatches, serverNum, startIndex, endIndex);

			} else {

				// Multiple operations
				for (int i = startIndex; i <= endIndex; i++) {
					transact(items, putBatches, serverNum, i);
				}
			}
			endTime = System.currentTimeMillis();
		}
		return endTime - startTime;
	}

	void transact(GemfireTxTaskItem[] items, List<JsonLite>[] putBatches, String serverNum, int index)
	{
		JsonLite value;
		JsonLite getValue = null;
		for (int i = 0; i < items.length; i++) {
			GemfireTxTaskItem item = items[i];
			String key = keyPrefix + serverNum + index;
			switch (item.type) {
			case GET:
				getValue = item.region.get(key);
				break;
			case REMOVE:
				item.region.registerInterest(key);
				break;
			case PUT:
			default:
				if (item.isPutPrevGetValue) {
					if (getValue == null) {
						if (isIncludeObjectCreationTime) {
							value = StressTestUtil.createObject(item.fieldCount, item.fieldSize);
						} else {
							int j = index % putBatches[i].size();
							value = putBatches[i].get(j);
						}
					} else {
						value = getValue;
					}
				} else {
					if (isIncludeObjectCreationTime) {
						value = StressTestUtil.createObject(item.fieldCount, item.fieldSize);
					} else {
						int j = index % putBatches[i].size();
						value = putBatches[i].get(j);
					}
				}
				item.region.put(key, value);
				break;
			}
		}
	}

	void singleOperation(GemfireTxTaskItem item, List<JsonLite>[] putBatches, String serverNum, int startIndex,
			int endIndex)
	{
		JsonLite value;
		switch (item.type) {
		case GET:
			for (int i = startIndex; i <= endIndex; i++) {
				String key = keyPrefix + serverNum + i;
				value = item.region.get(key);
			}
			break;
		case REMOVE:
			for (int i = startIndex; i <= endIndex; i++) {
				String key = keyPrefix + serverNum + i;
				value = item.region.remove(key);
			}
			break;
		case PUT:
		default:
			for (int i = startIndex; i <= endIndex; i++) {
				String key = keyPrefix + serverNum + i;
				if (isIncludeObjectCreationTime) {
					value = StressTestUtil.createObject(item.fieldCount, item.fieldSize);
				} else {
					int j = i % putBatches[i].size();
					value = putBatches[i].get(j);
				}
				item.region.put(key, value);
			}
			break;
		}
	}
}

class GemfireTxTaskItem
{
	@SuppressWarnings("rawtypes")
	Region<String, JsonLite> region;
	int fieldCount;
	int fieldSize;
	TxItemType type = TxItemType.PUT;
	boolean isPutPrevGetValue;
}

enum TxItemType
{
	PUT, GET, REMOVE
}
