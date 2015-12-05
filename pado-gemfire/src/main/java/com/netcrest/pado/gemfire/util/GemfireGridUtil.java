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
package com.netcrest.pado.gemfire.util;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolFactory;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.util.GridUtil;

public class GemfireGridUtil extends GridUtil
{
	/**
	 * Returns the pool identified by the specified pool name. If not found, it
	 * creates one based on the specified locators.
	 * 
	 * @param poolName
	 *            Pool name
	 * @param locators
	 *            Locators in the form of
	 *            &lt;host1&gt;:&lt;port1&gt;[@&lt;serverGroup
	 *            &gt;],&lt;host2&gt;:&lt;port2&gt;[@&lt;serverGroup&gt;]. Note
	 *            that the only the first server group name is used if
	 *            specified. The others are ignored.
	 * @param singleHopEnabled
	 *            true to set single hop, false to disable single hop.
	 * @return
	 */
	public static Pool getPool(String poolName, String locators, boolean multiuserAuthenticationEnabled,
			boolean singleHopEnabled, boolean subscriptionEnabled)
	{
		Pool pool = PoolManager.find(poolName);
		if (pool != null) {
			return pool;
		}
		if (locators == null) {
			return null;
		}

		locators = locators.trim();
		String locatorsSplit[] = locators.split(",");
		PoolFactory factory = PoolManager.createFactory();
		factory.setPRSingleHopEnabled(singleHopEnabled);
		factory.setMultiuserAuthentication(multiuserAuthenticationEnabled);
		factory.setSubscriptionEnabled(subscriptionEnabled);
		factory.setReadTimeout(300000); // Override the default read time of 10 sec to 5 min.
		String serverGroup = null;
		for (String locatorToken : locatorsSplit) {
			String tokens[] = locatorToken.split(":");
			if (tokens.length > 0) {
				String host = tokens[0];
				if (tokens.length > 1) {
					String tokens2[] = tokens[1].split("@");
					int port = Integer.parseInt(tokens2[0]);
					if (serverGroup == null && tokens2.length > 1) {
						serverGroup = tokens[1];
						factory.setServerGroup(serverGroup);
					}
					factory.addLocator(host, port);
				}
			}
		}

		try {
			pool = factory.create(poolName);
			return pool;
		} catch (Exception e) {
			throw new PadoException("Unable to create a connection pool. [locators=" + locators + ", poolName="
					+ poolName + ", multiuserAuthenticationEnabled=]" + multiuserAuthenticationEnabled
					+ ", singleHopEnabled=" + singleHopEnabled + ", subscriptionEnabled=" + subscriptionEnabled + "]",
					e);
		}
	}

	/**
	 * Returns the region of the specified grid path. It returns null if the
	 * region is undefined. It returns null if the grid path is undefined or
	 * this VM is a pure client. Use this method only in the server side.
	 * 
	 * @param gridPath
	 *            Grid path
	 */
	@SuppressWarnings("rawtypes")
	public static Region getRegion(String gridPath)
	{
		String fullPath = getFullPath(gridPath);
		if (fullPath == null) {
			return null;
		}
		return CacheFactory.getAnyInstance().getRegion(fullPath);
	}
}
