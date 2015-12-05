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
package com.netcrest.pado.index.provider;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.netcrest.pado.gemfire.GemfirePadoServerManager;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.gemfire.function.EntitySearchCacheRetrieveBaseFunction;
import com.netcrest.pado.index.gemfire.function.ServerEntitySearchFunction;
import com.netcrest.pado.index.helper.FunctionExecutor;
import com.netcrest.pado.index.helper.FunctionExecutor.Realm;
import com.netcrest.pado.index.helper.IndexMatrixOperationUtility;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.result.IGridResults;
import com.netcrest.pado.index.result.IndexBuilderMemberResultCollector;
import com.netcrest.pado.index.service.GridQuery;

/**
 * IndexMatrixServerPrivider is invoked by the grid (pado) that initiates
 * the query.
 */
public class IndexMatrixServerProvider extends AbstractIndexMatrixProvider {
	
	@Override
	public String getProviderId() {
		return Constants.SERVER_PROVIDER_KEY;
	}
	
	/**
	 * For server
	 */
	protected Region getRegion(GridQuery query)
	{
		if (query.getGridIds() != null && query.getGridIds().length == 1) {
			return GemfirePadoServerManager.getPadoServerManager().getServerRegion(query.getGridIds()[0]);
		} else {
			String regionPath = query.getFullPath();
			if (regionPath != null) {
				return IndexMatrixOperationUtility.getRegionFromName(regionPath, regionService);
			} else {
				return IndexMatrixOperationUtility.getRegionFromQuery(query.getQueryString(), regionService);
			}
		}
	}

	protected ResultCollector executeFunctionRetrieveEntities(GridQuery query, Set filterSet, Serializable argument, ResultCollector resultCollector) {
		Region region = getRegionClient(query);
		return FunctionExecutor.execute(Realm.REGION, region, null, null, filterSet, argument, resultCollector, EntitySearchCacheRetrieveBaseFunction.Id, null);
	}

	protected ResultCollector executeFunctionQueryKeyRemote(GridQuery query, Set filterSet, Serializable argument, ResultCollector resultCollector) {
		Region region = getRegion(query);
		return FunctionExecutor.execute(Realm.REGION, region, null, null, filterSet, argument, resultCollector, ServerEntitySearchFunction.Id, null);
	}
	
	protected ResultCollector executeFunctionQueryEntityRemote(GridQuery query, Set filterSet, Serializable argument, ResultCollector resultCollector) {
		Region region = getRegion(query);
		return FunctionExecutor.execute(Realm.REGION, region, null, null, filterSet, argument, resultCollector, ServerEntitySearchFunction.Id, null);
	}
	
//	protected ResultCollector executeFunctionQueryKeyRemote_old(GridQuery query, Set filterSet, Serializable argument, ResultCollector resultCollector) {
//		Region region = getRegion(query);
//		GridInfo gridInfo = PadoManager.getPadoManager().getGridInfoForFullPath(region.getFullPath());
//		Pool pool = PoolManager.find(gridInfo.getPoolName());
//		return FunctionExecutor.execute(getRealmForGrid(gridInfo, region.getFullPath()), region, pool, regionService, filterSet, argument, resultCollector, ServerEntitySearchFunction.Id, null);
//	}
//	
//	protected ResultCollector executeFunctionQueryEntityRemote_old(GridQuery query, Set filterSet, Serializable argument, ResultCollector resultCollector) {
//		Region region = getRegion(query);
//		GridInfo gridInfo = PadoManager.getPadoManager().getGridInfoForFullPath(region.getFullPath());
//		Pool pool = PoolManager.find(gridInfo.getPoolName());
//		return FunctionExecutor.execute(getRealmForGrid(gridInfo, region.getFullPath()), region, pool, regionService, filterSet, argument, resultCollector, ServerEntitySearchFunction.Id, null);
//	}
//	
	protected IGridResults executeQueryEntityRemote(GridQuery query) throws GridQueryException
	{
		IndexBuilderMemberResultCollector indexMemberResultCollector = new IndexBuilderMemberResultCollector(query,
				this);
		// Set filter to the target server. It is expected that the caller 
		// to provide ServerId, BucketId, RegionPath, IsServerQuery.
		// - RegionPath can be set explicitly as parameter. If not defined then
		//   the GridQuery.getQueryString() is used to determine the region path.
		// - IsServerQuery is true then all of the buckets in the server are queried,
		//   otherwise, only the specified bucket is queried.
		// - RoutingBucketId is mandatory
		Integer routingBucketId = (Integer)query.getParam("RoutingBucketId");
		Set filter = Collections.singleton(routingBucketId);
		
		ResultCollector rc = executeFunctionQueryEntityRemote(query, filter, query, indexMemberResultCollector);
		entityRun = indexMemberResultCollector;
		return (IGridResults) rc.getResult();
	}
}
