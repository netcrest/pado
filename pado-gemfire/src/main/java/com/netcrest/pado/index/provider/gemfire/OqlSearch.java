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
package com.netcrest.pado.index.provider.gemfire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.cache.query.FunctionDomainException;
import com.gemstone.gemfire.cache.query.NameResolutionException;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryInvocationTargetException;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.TypeMismatchException;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;
import com.gemstone.gemfire.internal.cache.LocalDataSet;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.netcrest.pado.exception.PadoServerException;
import com.netcrest.pado.gemfire.util.GemfireGridUtil;
import com.netcrest.pado.index.helper.IndexMatrixOperationUtility;
import com.netcrest.pado.log.Logger;
import com.netcrest.pado.util.GridUtil;

public class OqlSearch
{
	private final static OqlSearch oqlSearch = new OqlSearch();

	/**
	 * Time query predicate. date range is (inclusive, exclusive). Input:
	 * full-path, validAtTime, asOfTime
	 */
	private final static String TIME_QUERY = "select e.key.IdentityKey from %s.entrySet e "
			+ "where e.key.StartValidTime<=%d AND %d<e.key.EndValidTime AND e.key.StartWrittenTime<=%d AND %d<e.key.EndWrittenTime";

	private final static String TIME_IDENTITY_KEY_SELECT = "select e.key.IdentityKey from %s.entrySet e ";
	private final static String TIME_QUERY_PREDICATE = "e.key.StartValidTime<=%d AND %d<e.key.EndValidTime AND e.key.StartWrittenTime<=%d AND %d<e.key.EndWrittenTime";

	private OqlSearch()
	{
	}

	public final static OqlSearch getOqlSearch()
	{
		return oqlSearch;
	}

	public String getTimePredicate(String fullPath, long validAtTime, long asOfTime)
	{
		if (validAtTime == -1) {
			validAtTime = System.currentTimeMillis();
		}
		if (asOfTime == -1) {
			asOfTime = System.currentTimeMillis();
		}
		return String.format(TIME_QUERY_PREDICATE, fullPath, validAtTime, asOfTime);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List searchLocal(String queryString) throws FunctionDomainException, TypeMismatchException,
			NameResolutionException, QueryInvocationTargetException
	{
		Region region = IndexMatrixOperationUtility.getRegionFromQuery(queryString, null);
		if (region == null) {
			return null;
		}
		SelectResults sr;
		QueryService qs = CacheFactory.getAnyInstance().getQueryService();
		DefaultQuery query = (DefaultQuery) qs.newQuery(queryString);
		if (region instanceof PartitionedRegion) {
			LocalDataSet localDS = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(region);
			sr = (SelectResults) localDS.executeQuery(query, null, null);
		} else {
			sr = (SelectResults) query.execute();
		}
		return sr.asList();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List executeQuery(String queryString)
	{
		List list = null;
		Region accountRegion = GemfireGridUtil.getRegion("test/partitioned");
		LocalDataSet accountLocalDataSet = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(accountRegion);
		QueryService qs = accountRegion.getRegionService().getQueryService();
		DefaultQuery query = (DefaultQuery) qs.newQuery(queryString);
		Object[] params = new Object[] { accountLocalDataSet };
		try {
			SelectResults sr = (SelectResults) accountLocalDataSet.executeQuery(query, params,
					accountLocalDataSet.getBucketSet());
			list = sr.asList();
		} catch (Exception ex) {
			Logger.error(ex);
		}
		return list;
	}

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public List executeJoin_old(String queryString)
	{
		// queryString =
		// "select distinct e1.value, e2.value from $1 e1, $2 e2 where e1.value.get('$3')=e2.value.get('$4')";
		Cache cache = CacheFactory.getAnyInstance();
		Region accountRegion = GemfireGridUtil.getRegion("account");
		List list = null;
		Map<String, Region<?, ?>> colocatedRegionMap = PartitionRegionHelper.getColocatedRegions(accountRegion);
		Region portfolioRegion = colocatedRegionMap.get(GridUtil.getFullPath("portfolio"));
		if (portfolioRegion != null) {
			LocalDataSet accountDataSet = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(accountRegion);
			LocalDataSet portfolioDataSet = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(portfolioRegion);
			QueryService qs = accountRegion.getRegionService().getQueryService();
			DefaultQuery query = (DefaultQuery) qs.newQuery(queryString);
			Object[] params = new Object[] { accountDataSet.entrySet(), portfolioDataSet.entrySet(), "AccountId",
					"AccountId" };
			try {
				SelectResults r = (SelectResults) accountDataSet.executeQuery(query, params,
						portfolioDataSet.getBucketSet());
				list = r.asList();
			} catch (Exception ex) {
				throw new PadoServerException(ex);
			}
		} else {
			Logger.warning("Paritioned region margin_node and margin_node_group_member cannot be joined in query because they are not colocated. Query failed.");
		}
		return list;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List executeJoin_old2(String oql)
	{
		// queryString =
		// "select distinct e1.value, e2.value from $1 e1, $2 e2 where e1.value.get('$3')=e2.value.get('$4')";
		Cache cache = CacheFactory.getAnyInstance();
		String regionPaths[] = getRegionPaths(oql);
		if (regionPaths.length == 0) {
			return null;
		}

		Region region = cache.getRegion(regionPaths[0]);
		LocalDataSet localDataSet[] = new LocalDataSet[regionPaths.length];
		List list = null;
		Map<String, Region<?, ?>> colocatedRegionMap = PartitionRegionHelper.getColocatedRegions(region);
		localDataSet[0] = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(region);
		for (int i = 1; i < regionPaths.length; i++) {
			Region region2 = colocatedRegionMap.get(regionPaths[i]);
			if (region2 == null) {
				return null;
			}
			localDataSet[i] = (LocalDataSet) PartitionRegionHelper.getLocalPrimaryData(region2);
		}
		QueryService qs = region.getRegionService().getQueryService();
		DefaultQuery query = (DefaultQuery) qs.newQuery(oql);
		Object[] params = localDataSet;

		try {
			SelectResults r = (SelectResults) localDataSet[0].executeQuery(query, params,
					localDataSet[1].getBucketSet());
			list = r.asList();
		} catch (Exception ex) {
			throw new PadoServerException(ex);
		}

		return list;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public List executeOql(RegionFunctionContext rfc, String oql)
	{
		// queryString =
		// "select distinct e1.value, e2.value from $1 e1, $2 e2 where e1.value.get('$3')=e2.value.get('$4')";
		Cache cache = CacheFactory.getAnyInstance();
		String regionPaths[] = getRegionPaths(oql);
		if (regionPaths.length == 0) {
			return null;
		}
		List list = null;
		QueryService qs = cache.getQueryService();
		Query query = qs.newQuery(oql);

		try {
			SelectResults r = (SelectResults)query.execute(rfc);
			list = r.asList();
		} catch (Exception ex) {
			throw new PadoServerException(ex);
		}

		return list;
	}

	/**
	 * Returns all region paths found in the specified query string. I returns
	 * an empty string array if no region paths are found.
	 * 
	 * @param queryString
	 *            Query string with region paths
	 * @throws com.gemstone.gemfire.cache.query.QueryInvalidException
	 *             Thrown if the query string is invalid
	 */
	public static String[] getRegionPaths(String queryString)
	{
		QueryService qs = CacheFactory.getAnyInstance().getQueryService();
		Query query = qs.newQuery(queryString);
		DefaultQuery dq = (DefaultQuery) query;
		Set<String> regionPathSet = dq.getRegionsInQuery(null);
		String regionPaths[];
		if (regionPathSet != null) {
			ArrayList<RegionPathInfo> regionPathInfoList = new ArrayList<RegionPathInfo>(regionPathSet.size());
			regionPaths = new String[regionPathSet.size()];
			for (String regionPath : regionPathSet) {
				RegionPathInfo info = new RegionPathInfo();
				info.regionPath = regionPath;
				info.index = queryString.indexOf(regionPath);
				regionPathInfoList.add(info);
			}
			Collections.sort(regionPathInfoList);
			for (int i = 0; i < regionPaths.length; i++) {
				regionPaths[i] = regionPathInfoList.get(i).regionPath;
			}
		} else {
			regionPaths = new String[0];
		}
		return regionPaths;
	}

	static class RegionPathInfo implements Comparable<RegionPathInfo>
	{
		String regionPath;
		int index;

		@Override
		public int compareTo(RegionPathInfo o)
		{
			if (index < o.index) {
				return -1;
			} else if (index > o.index) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	/**
	 * Returns one of the region paths found in the specified query string. It
	 * returns null if a region path is not found.
	 * 
	 * @param queryString
	 *            Query string with region paths
	 * @throws com.gemstone.gemfire.cache.query.QueryInvalidException
	 *             Thrown if the query string is invalid
	 */
	public static String getRegionPath(String queryString)
	{
		QueryService qs = CacheFactory.getAnyInstance().getQueryService();
		Query query = qs.newQuery(queryString);
		DefaultQuery dq = (DefaultQuery) query;
		Set<String> regionPaths = dq.getRegionsInQuery(null);
		if (regionPaths != null) {
			for (String string : regionPaths) {
				return string;
			}
		}
		return null;
	}

	/**
	 * Returns one of the grid paths found in the specified query string.
	 * 
	 * @param queryString
	 *            Query string with region paths
	 * @throws com.gemstone.gemfire.cache.query.QueryInvalidException
	 *             Thrown if the query string is invalid
	 */
	public static String getGridPath(String queryString)
	{
		String fullPath = getRegionPath(queryString);
		if (fullPath == null) {
			return null;
		}
		return GridUtil.getChildPath(fullPath);

	}

	/**
	 * Returns all grid paths found in the specified query string. I returns an
	 * empty string array if no grid paths are found.
	 * 
	 * @param queryString
	 *            Query string with region paths
	 * @throws com.gemstone.gemfire.cache.query.QueryInvalidException
	 *             Thrown if the query string is invalid
	 */
	public static String[] getGridPaths(String queryString)
	{
		String fullPaths[] = getRegionPaths(queryString);
		String gridPaths[] = new String[fullPaths.length];
		for (int i = 0; i < gridPaths.length; i++) {
			gridPaths[i] = GridUtil.getChildPath(fullPaths[i]);
		}
		return gridPaths;
	}
}
