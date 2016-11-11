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
import java.util.Set;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.netcrest.pado.gemfire.GemfirePadoServerManager;
import com.netcrest.pado.index.gemfire.function.EntitySearchCacheRetrieveBaseFunction;
import com.netcrest.pado.index.gemfire.function.TemporalEntitySearchFunction;
import com.netcrest.pado.index.helper.FunctionExecutor;
import com.netcrest.pado.index.helper.FunctionExecutor.Realm;
import com.netcrest.pado.index.helper.IndexMatrixOperationUtility;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.info.GridInfo;

public class IndexMatrixTemporalProvider extends AbstractIndexMatrixProvider
{

	@Override
	public String getProviderId()
	{
		return Constants.TEMPORAL_PROVIDER_KEY;
	}

	/**
	 * For server only. Returns the grid region used to route the request if 
	 * the grid ID is specified in the passed-in query. Note that it uses
	 * the first grid ID in {@link GridQuery#getGridIds()}. If the grid ID is 
	 * not defined then it returns the region matching {@link GridQuery#getFullPath()}.
	 * If the region path is not defined then it returns the region matching
	 * the region path defined in {@link GridQuery#getQueryString()}.
	 */
	@SuppressWarnings("rawtypes")
	@Override
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
	// Extension point for plugin local test provider
	@SuppressWarnings("rawtypes")
	protected ResultCollector executeFunctionRetrieveEntities(GridQuery query, Set filterSet, Serializable argument,
			ResultCollector resultCollector)
	{
		Region region = getRegionClient(query);
		return FunctionExecutor.execute(Realm.REGION, region, null, null, filterSet, argument,
				resultCollector, EntitySearchCacheRetrieveBaseFunction.Id, null);
	}

	/**
	 * Executes remote query for building indexes after the inital call of 
	 * {@link #executeFunctionQueryEntityRemote(GridQuery, Set, Serializable, ResultCollector)}.
	 * Note that this method always performs onRegion. The initial call determines 
	 * onRegion filters.
	 */
	@SuppressWarnings("rawtypes")
	protected ResultCollector executeFunctionQueryKeyRemote(GridQuery query, Set filterSet, Serializable argument,
			ResultCollector resultCollector)
	{
		Region region = getRegion(query);
		return FunctionExecutor.execute(Realm.REGION, region, null, null, filterSet, argument,
				resultCollector, TemporalEntitySearchFunction.Id, null);
	}

	/**
	 * Executes the initial index builder's function. This method determines
	 * the function invocation routing information (onRegion, onServer, etc.)
	 * initially to target the grid and its members. If the query is targeted
	 * for a pado then it is sent to all of grid members using the
	 * server region (__pado/server) via onRegion without the filter. If the 
	 * query is targeted for a non-pado, i.e., no GridInfo defined, then
	 * the target grid must be supplied via {@link GridQuery#getGridIds()}. 
	 * The first grid ID in that array is used to determine the target grid.
	 * If all fails, then it returns null.
	 */
	@SuppressWarnings("rawtypes")
	protected ResultCollector executeFunctionQueryEntityRemote(GridQuery query, Set filterSet, Serializable argument,
			ResultCollector resultCollector)
	{
		Region region = getRegion(query);
		if (region == null) {
			return null;
		}
		// the region returned by getRegion(query) is not necessarily the 
		// region that is being queried. It could be the router (grid) region.
		// This means gridInfo can never be null.
		GridInfo gridInfo = GemfirePadoServerManager.getPadoServerManager().getGridInfoForFullPath(region.getFullPath());
		// pool may or may not be used, i.e., if onRegion or regionService is not null.
		Pool pool = PoolManager.find(gridInfo.getConnectionName());
		return FunctionExecutor.execute(getRealmForGrid(gridInfo, query), region, pool, regionService, filterSet, argument,
				resultCollector, TemporalEntitySearchFunction.Id, null);
	}
}
