package com.netcrest.pado.test.biz.impl.gemfire;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
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

@SuppressWarnings({ "rawtypes", "unchecked" })
public class BulkLoaderTask implements Callable<JsonLite>
{
	private BizThreadPool<ITemporalBizLink> temporalBizThreadPool = new BizThreadPool(
			PadoServerManager.getPadoServerManager().getCatalog(), ITemporalBiz.class);

	private int threadNum;
	private int threadCount;
	JsonLite pathConfig;
	private boolean isIncludeObjectCreationTime = true;
	private int batchSize = 1000;
	private int updateIntervalInMsec = 0;
	private String keyPrefix = "";

	BulkLoaderTask(int threadNum, int threadCount, JsonLite pathConfig, boolean isIncludeObjectCreationTime,
			int batchSize, int updateIntervalInMsec, String keyPrefix)
	{
		this.threadNum = threadNum;
		this.threadCount = threadCount;
		// Set default values for fieldCount, payloadSize, totalEntryCount
		this.pathConfig = pathConfig;
		this.isIncludeObjectCreationTime = isIncludeObjectCreationTime;
		this.batchSize = batchSize;
		this.updateIntervalInMsec = updateIntervalInMsec;
		this.keyPrefix = keyPrefix;
		if (keyPrefix != null) {
			this.keyPrefix = keyPrefix;
		}
	}

	@Override
	public JsonLite call() throws Exception
	{
		String serverNum = PadoServerManager.getPadoServerManager().getServerNum();
		int serverCount = PadoServerManager.getPadoServerManager().getServerCount();
		String path = (String) pathConfig.get("Path", "stress/test1");
		int totalEntryCount = (Integer) pathConfig.get("TotalEntryCount", 1000);
		int fieldCount = (Integer) pathConfig.get("FieldCount", 10);
		int payloadSize = (Integer) pathConfig.get("PayloadSize", 1024);
		int batchSize = (Integer) pathConfig.get("BatchSize", this.batchSize);
		int updateIntervalInMsec = (Integer) pathConfig.get("UpdateIntervalInMsec", this.updateIntervalInMsec);

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
				bulkLoader = new RegionBulkLoaderThrottled(region, serverNum, path, threadNum, threadCount, payloadSize,
						fieldCount, fieldSize, isIncludeObjectCreationTime, entryCountPerDriver, updateIntervalInMsec);
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

			// Include object creation time. Create a object just
			// before inserting into the grid.
			startTime = System.currentTimeMillis();
			for (int i = startIndex; i <= endIndex; i++) {
				String key = keyPrefix + serverNum + i;
				value = StressTestUtil.createObject(fieldCount, fieldSize);
				bulkLoader.put(key, value);
			}
			bulkLoader.flush();
			endTime = System.currentTimeMillis();
		} else {

			// Do NOT include the object creation time. Create
			// all objects up front in a batch and reuse them.
			List<JsonLite> batch = StressTestUtil.createBatchOfObjects(batchSize, fieldCount, fieldSize);
			startTime = System.currentTimeMillis();
			for (int i = startIndex; i <= endIndex; i++) {
				String key = keyPrefix + serverNum + i;
				int index = i % batch.size();
				bulkLoader.put(key, batch.get(index));
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
}

@SuppressWarnings({ "rawtypes", "unchecked" })
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