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
package com.netcrest.pado.demo.cache;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.CacheException;
import com.gemstone.gemfire.cache.CacheExistsException;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.Locator;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;
import com.netcrest.pado.gemfire.util.InstantiatorClassLoader;

public class CacheBase
{
	/**
	 * The system region that holds system-level data for configuration and
	 * real-time state updates.
	 */
	public final static String PROPERTY_SYSTEM_REGION_PATH = "systemRegionPath";

	protected DistributedSystem distributedSystem;
	// protected Cache cache;
	protected GemFireCacheImpl cache;
	protected LogWriter logger;

	public static void startLocator(String address, int port, String logFile) throws IOException
	{
		InetAddress inetAddress = InetAddress.getByName(address);
		Locator.startLocatorAndDS(port, new File(logFile), inetAddress, new Properties());
	}

	public CacheBase()
	{
	}

	protected void initializeCache() throws CacheException, IOException
	{

		try {
			InstantiatorClassLoader.loadDataSerializables();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// open(getCacheProperties());
		open();
	}

	public void open() throws IOException
	{
		try {
			cache = (GemFireCacheImpl) new CacheFactory().create();
		} catch (CacheExistsException ex) {
			cache = (GemFireCacheImpl) CacheFactory.getAnyInstance();
		}
		if (cache != null) {
			logger = cache.getLogger();
		}

	}

	protected void open(Properties prop, Boolean daffyDisabled) throws IOException
	{
		// Create distributed system properties

		// Connect to the distributed system
		// distributedSystem = DistributedSystem.connect(properties);

		try {
			// Create cache
			// cache = CacheFactory.create(distributedSystem);
			// cache=new ClientCacheFactory().create();
			cache = (GemFireCacheImpl) CacheFactory.getAnyInstance();

			// cache.
			// cache.setLockLease(10); // 10 second time out

			// *int bridgeServerPort = Integer.getInteger("bridgeServerPort",
			// 0).intValue();
			/*
			 * String groups = System.getProperty("serverGroups"); String[]
			 * serverGroups = null; if (groups != null) { serverGroups =
			 * groups.split(","); }
			 */
			/*
			 * if (bridgeServerPort != 0) { cache.setIsServer(true);
			 * com.gemstone.gemfire.cache.server.CacheServer server =
			 * cache.addCacheServer(); server.setPort(bridgeServerPort);
			 * server.setNotifyBySubscription(false);
			 * server.setGroups(serverGroups); server.start(); }
			 */
		} catch (CacheExistsException ex) {
			cache = (GemFireCacheImpl) CacheFactory.getAnyInstance();
		}
		if (cache != null) {
			logger = cache.getLogger();
		}
	}

	protected void close()
	{
		if (cache != null) {
			cache.close();
		}
	}

	public DistributedSystem getDistributedSystem()
	{
		return distributedSystem;
	}

	public ClientCache getGemFireCache()
	{
		return cache;
	}

	public void waitForever() throws InterruptedException
	{
		Object obj = new Object();
		synchronized (obj) {
			obj.wait();
		}
	}

	public LogWriter getLogger()
	{
		return cache.getLogger();
	}

	public static void main(String[] args) throws Exception
	{
		CacheBase base = new CacheBase();
		base.initializeCache();
	}

	public ClientCache getCache()
	{
		return cache;
	}

}
