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
package com.netcrest.pado.index.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.index.result.ResultItem;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class IndexMatrixOperationUtility
{
	/**
	 * Returns one of the region paths in the specified query string.
	 * 
	 * @param queryString
	 *            Query string with region paths
	 * @throws com.gemstone.gemfire.cache.query.QueryInvalidException
	 *             Thrown if the query string is invalid
	 */
	public static String getRegionPath(String queryString)
	{
		try {
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
		} catch (Exception ex) {
			throw new PadoException("Error occured while parsing OQL: " + queryString, ex);
		}
	}

	public static Region getRegionFromQuery(String queryString, RegionService regionService)
	{
		return getRegionFromName(getRegionPath(queryString), regionService);
	}

	public static Region getRegionFromName(String regionPath, RegionService regionService)
	{
		Region region;
		if (regionService == null) {
			region = CacheFactory.getAnyInstance().getRegion(regionPath);
		} else {
			region = regionService.getRegion(regionPath);
		}
		return region;
	}

	public static List<Object> getDataFromResultItemList(List<ResultItem<Object>> resultItems)
	{
		List<Object> dataList = new ArrayList<Object>(resultItems.size());
		for (ResultItem<Object> resultItem : resultItems) {
			dataList.add(resultItem.getItem());
		}
		return dataList;
	}

}
