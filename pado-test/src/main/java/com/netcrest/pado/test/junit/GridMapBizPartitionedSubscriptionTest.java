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
package com.netcrest.pado.test.junit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.EntryEvent;
import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IEntryListener;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IGridMapBiz;

/**
 * Tests entry subscriptions via IGridMapBiz on a partitioned path. This class
 * and {@link GridMapBizReplicatedSubscriptionTest} are identical except that it
 * tests a partitioned path (test/partitioned). The test case results should be
 * identical.
 * 
 * @author dpark
 * 
 */
public class GridMapBizPartitionedSubscriptionTest
{
	static IPado pado;
	static String appId = "test";
	static String GRID_PATH = "test/partitioned";
	static IGridMapBiz<String, String> gridMapBiz;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login(appId, "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		gridMapBiz = catalog.newInstance(IGridMapBiz.class, GRID_PATH);
		gridMapBiz.addEntryListener(new IEntryListener<Object, Object>() {

			@Override
			public void onCreate(EntryEvent<Object, Object> event)
			{
				System.out.println("onCreate(): key=" + event.getKey() + ", value=" + event.getValue());
			}

			@Override
			public void onUpdate(EntryEvent<Object, Object> event)
			{
				System.out.println("onUpdate(): key=" + event.getKey() + ", value=" + event.getValue());
			}

			@Override
			public void onRemove(EntryEvent<Object, Object> event)
			{
				System.out.println("onRemove(): key=" + event.getKey() + ", value=" + event.getValue());
			}

			@Override
			public void onInvalidate(EntryEvent<Object, Object> event)
			{
				System.out.println("onInvalidate(): key=" + event.getKey() + ", value=" + event.getValue());
			}
		});
	}
	
	@Test
	public void testSubscribeKey()
	{
		gridMapBiz.subscribeKeys("putValue1");
		gridMapBiz.subscribeKeys("putValue2");
	}

	@Test
	public void testSubscribeEntry()
	{
		gridMapBiz.subscribeEntries(".*");
	}
	
	@AfterClass
	public static void testWaitForever()
	{
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (true);
	}

}
