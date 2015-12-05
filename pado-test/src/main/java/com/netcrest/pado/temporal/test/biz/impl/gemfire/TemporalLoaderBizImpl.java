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
package com.netcrest.pado.temporal.test.biz.impl.gemfire;

import java.util.HashMap;
import java.util.Set;

import javax.annotation.Resource;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.IBizContextServer;
import com.netcrest.pado.IGridMapBizLink;
import com.netcrest.pado.annotation.BizMethod;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalClientFactory;
import com.netcrest.pado.temporal.TemporalUtil;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalData;
import com.netcrest.pado.temporal.gemfire.impl.GemfireTemporalKey;
import com.netcrest.pado.temporal.test.TradeFactory;
import com.netcrest.pado.util.GridUtil;

public class TemporalLoaderBizImpl
{
	@Resource
	IBizContextServer bizContext;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@BizMethod
	public String loadTradePerServer(final String path, final int perServerCount, final int batchSize)
	{
		if (path == null) {
			return PadoServerManager.getPadoServerManager().getServerId() + ": Error. Undefined path.";
		}

	
		ITemporalBiz temporalBiz = PadoServerManager.getPadoServerManager().getCatalog()
				.newInstance(ITemporalBiz.class, path);
		if (temporalBiz == null) {
			return PadoServerManager.getPadoServerManager().getServerId() + ": Error. Unabled to create ITemporalBiz.";
		}
		Region region = CacheFactory.getAnyInstance().getRegion(GridUtil.getFullPath(path));
		if (region == null) {
			return PadoServerManager.getPadoServerManager().getServerId() + ": Error. Undefined path: " + path;
		}
		if (region instanceof PartitionedRegion == false) {
			return PadoServerManager.getPadoServerManager().getServerId() + ": Error. Path must be partitioned.";
		}

		PartitionedRegion pr = (PartitionedRegion) region;
		Set<Integer> bucketIdSet = pr.getDataStore().getAllLocalPrimaryBucketIds();
		int totalBucketCount = pr.getAttributes().getPartitionAttributes().getTotalNumBuckets();
		int nextIdentityKeys[] = new int[bucketIdSet.size()];
		int i = 0;
		for (int bucketId : bucketIdSet) {
			nextIdentityKeys[i++] = bucketId;
		}

		long count = 0;
		int identityKeyCount = nextIdentityKeys.length;
		HashMap map = new HashMap(batchSize, 1f);
		while (count < perServerCount) {
			for (int j = 0; j < identityKeyCount; j++) {
				// Use bucket Ids to set the trade Ids so that trades
				// are placed in the same VM (server) that executes
				// this.

				long identityKey = nextIdentityKeys[j];
				JsonLite trade = TradeFactory.createTrade(identityKey);
				long startValidTime = System.currentTimeMillis();
				long endValidTime = TemporalUtil.MAX_TIME;
				long writtenTime = startValidTime;
				try {
					ITemporalKey tkey = new GemfireTemporalKey(identityKey, startValidTime, endValidTime, writtenTime, bizContext.getUserContext().getUsername());
//							clientFactory.createTemporalKey(identityKey, startValidTime, endValidTime,
//							writtenTime, bizContext.getUserContext().getUsername());
					ITemporalData data = new GemfireTemporalData(tkey, trade);
//							clientFactory.createTemporalData(tkey, trade);
					data.__getTemporalValue().setTemporalKey(tkey);
					map.put(tkey, data);
				} catch (Exception ex) {
					CacheFactory.getAnyInstance().getLogger().error("BulkLoader.put()", ex);
					return null;
				}
				if (map.size() % batchSize == 0) {
					pr.putAll(map);
					map.clear();
				}
				count++;
				if (count >= perServerCount) {
					break;
				}
				nextIdentityKeys[j] += totalBucketCount;
			}

		}
		if (map.size() > 0) {
			pr.putAll(map);
			map.clear();
		}

		// ITemporalBulkLoader bulkLoader =
		// temporalBiz.getTemporalAdminBiz().createBulkLoader(batchSize);
		// int index = 0;
		// int bucketCount = bucketIds.length;
		// while (index < perServerCount) {
		// for (int j = 0; j < bucketCount; j++) {
		// // Use bucket Ids to set the trade Ids so that trades
		// // are placed in the same VM (server) that executes
		// // this.
		// int tradeId = bucketIds[j] + index;
		// JsonLite trade = TradeFactory.createTrade(tradeId);
		// long startValidTime = System.currentTimeMillis();
		// long endValidTime = TemporalUtil.MAX_TIME;
		// long writtenTime = startValidTime;
		// bulkLoader.put(trade.getId(), trade, null, startValidTime,
		// endValidTime, writtenTime, false);
		// index++;
		// }
		// }
		// bulkLoader.flush();
		// }
		// });

		return PadoServerManager.getPadoServerManager().getServerId() + " loaded: " + count;
	}

	public String loadTradePerServer_temporalBiz(final String path, final int perServerCount, final int batchSize)
	{
		if (path == null) {
			return PadoServerManager.getPadoServerManager().getServerId() + ": Error. Undefined path.";
		}

		// // Spawn a thread to do this
		// Executors.newSingleThreadExecutor().execute(new Runnable() {
		//
		// public void run()
		// {
		ITemporalBiz temporalBiz = PadoServerManager.getPadoServerManager().getCatalog()
				.newInstance(ITemporalBiz.class, path);
		if (temporalBiz == null) {
			return PadoServerManager.getPadoServerManager().getServerId() + ": Errro. Unabled to create ITemporalBiz.";
		}
		PartitionedRegion pr = (PartitionedRegion) temporalBiz.getTemporalAdminBiz().getGridMapBiz().getNativeMap();
		Set<Integer> bucketIdSet = pr.getDataStore().getAllLocalPrimaryBucketIds();
		int totalBucketCount = pr.getAttributes().getPartitionAttributes().getTotalNumBuckets();
		int nextIdentityKeys[] = new int[bucketIdSet.size()];
		int i = 0;
		for (int bucketId : bucketIdSet) {
			nextIdentityKeys[i++] = bucketId;
		}

		IGridMapBizLink gridMapBiz = temporalBiz.getTemporalAdminBiz().getGridMapBiz();
		TemporalClientFactory clientFactory = temporalBiz.getTemporalAdminBiz().getTemporalClientFactory();
		long count = 0;
		int identityKeyCount = nextIdentityKeys.length;
		HashMap map = new HashMap(batchSize, 1f);
		while (count < perServerCount) {
			for (int j = 0; j < identityKeyCount; j++) {
				// Use bucket Ids to set the trade Ids so that trades
				// are placed in the same VM (server) that executes
				// this.

				long identityKey = nextIdentityKeys[j];
				JsonLite trade = TradeFactory.createTrade(identityKey);
				long startValidTime = System.currentTimeMillis();
				long endValidTime = TemporalUtil.MAX_TIME;
				long writtenTime = startValidTime;
				try {
					ITemporalKey tkey = clientFactory.createTemporalKey(identityKey, startValidTime, endValidTime,
							writtenTime, bizContext.getUserContext().getUsername());
					ITemporalData data = clientFactory.createTemporalData(tkey, trade);
					data.__getTemporalValue().setTemporalKey(tkey);
					map.put(tkey, data);
				} catch (Exception ex) {
					CacheFactory.getAnyInstance().getLogger().error("BulkLoader.put()", ex);
					return null;
				}
				if (map.size() % batchSize == 0) {
					gridMapBiz.putAll(map);
					map.clear();
				}
				count++;
				if (count >= perServerCount) {
					break;
				}
				nextIdentityKeys[j] += totalBucketCount;
			}

		}
		if (map.size() > 0) {
			gridMapBiz.putAll(map);
			map.clear();
		}

		// ITemporalBulkLoader bulkLoader =
		// temporalBiz.getTemporalAdminBiz().createBulkLoader(batchSize);
		// int index = 0;
		// int bucketCount = bucketIds.length;
		// while (index < perServerCount) {
		// for (int j = 0; j < bucketCount; j++) {
		// // Use bucket Ids to set the trade Ids so that trades
		// // are placed in the same VM (server) that executes
		// // this.
		// int tradeId = bucketIds[j] + index;
		// JsonLite trade = TradeFactory.createTrade(tradeId);
		// long startValidTime = System.currentTimeMillis();
		// long endValidTime = TemporalUtil.MAX_TIME;
		// long writtenTime = startValidTime;
		// bulkLoader.put(trade.getId(), trade, null, startValidTime,
		// endValidTime, writtenTime, false);
		// index++;
		// }
		// }
		// bulkLoader.flush();
		// }
		// });

		return PadoServerManager.getPadoServerManager().getServerId() + " loaded: " + count;
	}
}
