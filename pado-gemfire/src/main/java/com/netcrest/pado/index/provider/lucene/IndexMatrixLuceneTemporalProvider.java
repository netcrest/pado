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
package com.netcrest.pado.index.provider.lucene;

import java.io.Serializable;
import java.util.Set;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.netcrest.pado.gemfire.GemfirePadoServerManager;
import com.netcrest.pado.index.helper.FunctionExecutor;
import com.netcrest.pado.index.helper.FunctionExecutor.Realm;
import com.netcrest.pado.index.helper.IndexMatrixOperationUtility;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.provider.AbstractIndexMatrixProvider;
import com.netcrest.pado.index.service.GridQuery;

/**
 * IndexMatrix Provider for Temporal
 * 
 */
public class IndexMatrixLuceneTemporalProvider extends AbstractIndexMatrixProvider
{
	@Override
	public String getProviderId()
	{
		return Constants.PQL_PROVIDER_KEY;
	}

	/**
	 * For server
	 */
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
	
	@Override
	protected ResultCollector executeFunctionRetrieveEntities(GridQuery query, Set filterSet, Serializable argument,
			ResultCollector resultCollector)
	{
		Region region = getRegionClient(query);
		return FunctionExecutor.execute(Realm.REGION, region, null, null, filterSet, null, resultCollector,
				LuceneTemporalSearchFunction.Id, null);
	}

	@Override
	protected ResultCollector executeFunctionQueryKeyRemote(GridQuery query, Set filterSet, Serializable argument,
			ResultCollector resultCollector)
	{
		Region region = getRegion(query);
		return FunctionExecutor.execute(Realm.REGION, region, null, null, filterSet, argument, resultCollector,
				LuceneTemporalQueryFunction.Id, null);
	}

	@Override
	protected ResultCollector executeFunctionQueryEntityRemote(GridQuery query, Set filterSet, Serializable argument,
			ResultCollector resultCollector)
	{
		Region region = getRegion(query);
		return FunctionExecutor.execute(Realm.REGION, region, null, null, null, argument, resultCollector,
				LuceneTemporalQueryFunction.Id, null);
	}
	
//	@Override
//	protected ResultCollector executeFunctionRetrieveEntities(GridQuery query, Set filterSet, Serializable argument,
//			ResultCollector resultCollector)
//	{
//		Region region = getRegion(query);
//		GridInfo gridInfo = PadoManager.getPadoManager().getGridInfoForFullPath(region.getFullPath());
//		Pool pool = PoolManager.find(gridInfo.getPoolName());
//		return FunctionExecutor.execute(getRealmForGrid(gridInfo, region.getFullPath()), region, pool, regionService, filterSet, null, resultCollector,
//				LuceneTemporalSearchFunction.Id, null);
//	}
//
//	@Override
//	protected ResultCollector executeFunctionQueryKeyRemote(GridQuery query, Set filterSet, Serializable argument,
//			ResultCollector resultCollector)
//	{
//		Region region = getRegion(query);
//		GridInfo gridInfo = PadoManager.getPadoManager().getGridInfoForFullPath(region.getFullPath());
//		Pool pool = PoolManager.find(gridInfo.getPoolName());
//		return FunctionExecutor.execute(getRealmForGrid(gridInfo, region.getFullPath()), region, pool, regionService, filterSet, argument, resultCollector,
//				LuceneTemporalQueryFunction.Id, null);
//	}
//
//	@Override
//	protected ResultCollector executeFunctionQueryEntityRemote(GridQuery query, Set filterSet, Serializable argument,
//			ResultCollector resultCollector)
//	{
//		Region region = getRegion(query);
//		GridInfo gridInfo = PadoManager.getPadoManager().getGridInfoForFullPath(region.getFullPath());
//		Pool pool = PoolManager.find(gridInfo.getPoolName());
//		return FunctionExecutor.execute(getRealmForGrid(gridInfo, region.getFullPath()), region, pool, regionService, null, argument, resultCollector,
//				LuceneTemporalQueryFunction.Id, null);
//	}
}
