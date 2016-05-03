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
package com.netcrest.pado.index.gemfire.function;

import java.util.Collections;
import java.util.List;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;
import com.gemstone.gemfire.internal.cache.LocalDataSet;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.gemfire.function.AbstractEntitySearchFunction;
import com.netcrest.pado.index.gemfire.function.IEntitySearchFunction;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.internal.IndexMatrixUtil;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.server.PadoServerManager;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalManager;

/**
 * The TemporalEntitySearchFunction called by GridSearch server to build
 * IndexMatrix for a Temporal style query
 * 
 */
public class TemporalEntitySearchFunction extends AbstractEntitySearchFunction implements Function, Declarable,
		IEntitySearchFunction
{
	private static final long serialVersionUID = 1L;
	
	public final static String Id = "TemporalEntitySearchFunction";

	@Override
	public String getId()
	{
		return Id;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List queryLocal(GridQuery criteria, FunctionContext context) throws GridQueryException
	{
		try {
			TemporalManager tm = TemporalManager.getTemporalManager(criteria.getFullPath());
			
			// Determine the limit based on the number of servers
			int limit = criteria.getServerLimit();

			// If tm is not defined or it is empty then the data might be
			// non-temporal. In that case, use OQL.
			if (tm == null || tm.getTemporalListCount() == 0) {

				// Do not allow search of the results region
				if (criteria.getFullPath().equals(IndexMatrixUtil.getProperty(Constants.PROP_REGION_RESULTS))){
					return null;
				}
				Region region = CacheFactory.getAnyInstance().getRegion(criteria.getFullPath());
				if (region == null) {
					return null;
				}

				List list;
				String queryString = "select e.key, e.value from " + region.getFullPath() + ".entrySet e";
				
				// Apply limit if defined based on the number of servers
				if (limit > 0) {
					queryString = queryString + " limit " + limit;
				}
				QueryService qs = CacheFactory.getAnyInstance().getQueryService();
				DefaultQuery query = (DefaultQuery) qs.newQuery(queryString);
				SelectResults sr;
				if (region instanceof PartitionedRegion) {
					LocalDataSet localDS = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(region);
					sr = (SelectResults) localDS.executeQuery(query, null, null);
				} else {
					sr = (SelectResults) query.execute();
				}
				list = sr.asList();
				return list;
			} else {
				List<TemporalEntry> list = tm.getLastTemporalEntryList(limit);
				if (list != null && criteria.isOrdered() && criteria.isAscending()) {
					Collections.sort(list);
				}
				return list;
			}
		} catch (Exception e) {
			getLogger().warning("TemporalEntitySearchFunction.queryLocal(): ", e);
			throw new GridQueryException(e);
		}
	}
}
