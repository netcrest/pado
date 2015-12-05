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
package com.netcrest.pado.biz.gemfire.impl;

import java.lang.reflect.Method;
import java.util.HashSet;

import com.gemstone.gemfire.StatisticDescriptor;
import com.gemstone.gemfire.Statistics;
import com.gemstone.gemfire.StatisticsFactory;
import com.gemstone.gemfire.StatisticsType;
import com.gemstone.gemfire.StatisticsTypeFactory;
import com.gemstone.gemfire.distributed.internal.DistributionStats;
import com.gemstone.gemfire.internal.StatisticsTypeFactoryImpl;
import com.netcrest.pado.biz.IBizStatistics;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.log.Logger;

public class BizStatisticsImpl implements IBizStatistics
{
	private StatisticsType type;
	private Statistics stats;
	private boolean isLocal = false;
	private boolean isEnabled = false;

	public BizStatisticsImpl(StatisticsFactory statFactory, Class<?> clazz, String statisticsId, String description)
	{
		isEnabled = PadoUtil.isProperty(Constants.PROP_STATISTICS_ENABLED);
		
		Method[] methods = clazz.getMethods();
		HashSet<String> set = new HashSet<String>(methods.length);
		for (Method method : methods) {
			if (method.getName().equals("getBizContext") == false) {
				set.add(method.getName());
			}
		}
	
		String[] methodNames = set.toArray(new String[set.size()]);
		StatisticsTypeFactory f = StatisticsTypeFactoryImpl.singleton();
		StatisticDescriptor[] descriptors = new StatisticDescriptor[methodNames.length * 2];
		for (int i = 0, j = 0; i < methodNames.length; i++) {
			descriptors[j++] = f.createIntGauge(methodNames[i] + "Count", "Method invocation count", "operations");
			descriptors[j++] = f.createLongCounter(methodNames[i] + "Time", "Method invocation latency", "nanoseconds");
		}
		try {
			type = f.createType(clazz.getSimpleName(), description, descriptors);
			this.stats = statFactory.createAtomicStatistics(type, statisticsId);
		} catch (Exception ex) {
			Logger.warning("Error occurred while creating statistics type: " + clazz.getCanonicalName() , ex);
		}	
	}
	
	public boolean isEnabled()
	{
		return isEnabled;
	}

	public void close()
	{
		this.stats.close();
	}

	public int getCount(String methodName)
	{
		return this.stats.getInt(methodName+"Count");
	}

	public long getTime(String methodName)
	{
		return this.stats.getLong(methodName+"Time");
	}

	public long startCount(String methodName)
	{
		if (isEnabled) {
			stats.incInt(methodName+"Count", 1);
			return getTime();
		} else {
			return 0;
		}
	}

	public void endCount(String methodName, long start)
	{
		if (isEnabled) {
			long end = getTime();
	
			// Increment number of methods invoked
			this.stats.incInt(methodName+"Count", 1);
	
			// Increment method invocation latency
			this.stats.incLong(methodName+"Time", end - start);
		}
	}
	
	public boolean isLocal()
	{
		return isLocal;
	}
	
	public void setLocal(boolean local)
	{
		this.isLocal = local;
	}

	protected long getTime()
	{
		return DistributionStats.getStatTime();
	}
}
