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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;
import com.gemstone.gemfire.internal.cache.LocalDataSet;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.index.exception.GridQueryException;
import com.netcrest.pado.index.helper.IndexMatrixOperationUtility;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.server.PadoServerManager;

/**
 * The ServerEntitySearchFunction called by GridSearch server to build
 * IndexMatrix for a Server style query
 * 
 */
public class ServerEntitySearchFunction extends AbstractEntitySearchFunction implements Function, Declarable,
		IEntitySearchFunction
{
	private static final long serialVersionUID = 1L;
	
	public final static String Id = "ServerEntitySearchFunction";
	
	private final static Pattern limitPattern = Pattern.compile("(?i) limit ");

	@Override
	public String getId()
	{
		return Id;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List queryLocal(GridQuery criteria, FunctionContext context) throws GridQueryException
	{
		String queryString = criteria.getQueryString();
		String regionPath = IndexMatrixOperationUtility.getRegionPath(queryString);
		Region region = CacheFactory.getAnyInstance().getRegion(regionPath);
		if (region == null) {
			return null;
		}
		
		// Apply limit if defined based on the number of servers
		int limit = criteria.getLimit();
		if (limit > 0) {
			limit = (int)Math.ceil(criteria.getLimit() / PadoServerManager.getPadoServerManager().getServerCount());
		}
		if (limit >= 0) {
			Matcher matcher = limitPattern.matcher(queryString);
			if (matcher.find() == false) {
				queryString = queryString + " limit " + limit;
			}
		}

		try {
			QueryService qs = CacheFactory.getAnyInstance().getQueryService();
			if (region instanceof PartitionedRegion) {
				DefaultQuery query = (DefaultQuery) qs.newQuery(queryString);
				Boolean isServerQuery = (Boolean)criteria.getParam("IsServerQuery");
				if (isServerQuery != null && isServerQuery) {
//					Region localDataSet = PartitionRegionHelper.getLocalPrimaryData(region);
					LocalDataSet localDS = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(region);
					SelectResults sr = (SelectResults) localDS.executeQuery(query, null, null);
					return sr.asList();
				} else {
					// Single bucket query
					Integer bucketId = (Integer)criteria.getParam("BucketId");
					if (bucketId == null) {
						return null;
					}
					PartitionedRegion pr = (PartitionedRegion)region;
					Set<Integer> bucketSet = Collections.singleton(bucketId);
					LocalDataSet localDS = new LocalDataSet(pr, bucketSet);
					SelectResults sr = (SelectResults) localDS.executeQuery(query, null, null);
					return sr.asList();
				}
			} else {
				Query query = qs.newQuery(queryString);
				SelectResults sr = (SelectResults) query.execute();
				return sr.asList();
			}
		} catch (Exception ex) {
			Logger.warning(ex);
			throw new GridQueryException(ex);
		}
	}

}
