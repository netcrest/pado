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
package com.netcrest.pado.biz.gemfire;

import java.util.HashMap;
import java.util.Map;

import com.gemstone.gemfire.cache.CacheFactory;
import com.netcrest.pado.biz.IBizStatistics;
import com.netcrest.pado.biz.gemfire.impl.BizStatisticsImpl;

public class BizStatisticsManager
{
	private final static Map<String, IBizStatistics> statsMap = new HashMap<String, IBizStatistics>(10);
	
	public static IBizStatistics getBizStatistics(Class<?> clazz, String statisticsId, String description)
	{
		synchronized (statsMap) {
			IBizStatistics stats = statsMap.get(statisticsId);
			if (stats == null) {
				stats = new BizStatisticsImpl(CacheFactory.getAnyInstance().getDistributedSystem(), clazz, statisticsId, description);
				statsMap.put(statisticsId, stats);
			}
			return stats;
		}
	}
	
	public static IBizStatistics IBizStatistics(String statisticsId)
	{
		return statsMap.get(statisticsId);
	}
}
