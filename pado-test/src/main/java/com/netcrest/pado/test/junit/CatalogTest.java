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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IGridContextClient;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.biz.impl.gemfire.GridMapBizImplLocal;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.test.biz.IPartitionedRegionBiz;
import com.netcrest.pado.test.biz.IPartitionedRegionBizFuture;

/**
 * CatalogTest requires all grid0, grid1, grid2.
 * 
 * @author dpark
 * 
 */
public class CatalogTest
{
	private static IPado pado;
	private static IPartitionedRegionBiz prBiz;
	private static IPartitionedRegionBiz prBiz2;
	private static IPartitionedRegionBizFuture prBizFuture;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		System.setProperty("pado.properties", "etc/client/pado.properties");
		if (Pado.isClosed()) {
			Pado.connect("localhost:20000", true);
		}
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		prBiz = catalog.newInstance(IPartitionedRegionBiz.class);
		prBiz2 = catalog.newInstance(IPartitionedRegionBiz.class);
		prBizFuture = catalog.newInstance(IPartitionedRegionBizFuture.class);
		// place some data
		IGridMapBiz gridMapBiz = catalog.newInstanceLocal(IGridMapBiz.class,
				new GridMapBizImplLocal());
		gridMapBiz.setGridPath("test");
		for (int i = 0; i < 100; i++) {
			gridMapBiz.put("key" + i, "value" + i);
		}
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testPado()
	{
		Object token = pado.getToken();
		Assert.assertNotNull(token);
		System.out.println("testPado()");
		System.out.println("   token=" + token);
		System.out.println();
		ICatalog catalog = pado.getCatalog();
		Assert.assertNotNull(catalog);
		Assert.assertTrue(catalog.getAllBizClasses().length > 0);
		Assert.assertNotNull(catalog.getGridIds());
		Assert.assertTrue(catalog.getGridIds().length > 0);
	}

	@Test
	public void testCatalog()
	{
		ICatalog catalog = pado.getCatalog();
		String classNames[] = catalog.getAllBizClassNames();
		Assert.assertNotNull(classNames);
		Assert.assertTrue(classNames.length > 0);
		System.out.println("testCatalog()");
		System.out.println("   Registered IBiz classes (catalog.getAllBizClassNames())");
		for (String className : classNames) {
			System.out.println("      " + className);
		}
		System.out.println();
		Class[] bizClasses = catalog.getAllBizClasses();
		Assert.assertNotNull(classNames);
		Assert.assertTrue(classNames.length > 0);
		System.out.println("   Registered IBiz classes (catalog.getAllBizClasses())");
		for (Class clazz : bizClasses) {
			System.out.println("      " + clazz);
		}
		System.out.println();
	}

	@Test
	public void testServerIdList()
	{
		IBizContextClient bizContext = prBiz.getBizContext();
		List<String> serverIdList = prBiz.getServerIdList();
		Assert.assertNotNull(serverIdList);
		System.out.println("testServerIdList()");
		System.out.println("   " + serverIdList);
		System.out.println();
	}

	@Test
	public void testServerIdListFuture()
	{
		Future<List<String>> future = prBizFuture.getServerIdList();
		try {
			Thread.sleep(1000);
			List<String> serverIdList = future.get();
			Assert.assertNotNull(serverIdList);
			System.out.println("testServerIdListFuture()");
			System.out.println("   " + serverIdList);
			System.out.println();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testServerIdListManyGrids()
	{
		prBiz.getBizContext().getGridContextClient().setGridIds("grid1", "grid2");
		Map<String, List<String>> map = prBiz.getServerIdMap();
		// IGridCollector gridCollector =
		// prBiz.getBizContext().getGridContextClient().getGridCollector();
		// Map<String, List<String>> map = (Map<String,
		// List<String>>)gridCollector.getResult();
		// Assert.assertNotNull(serverIdList);
		Assert.assertNotNull(map);
		System.out.println("testServerIdListManyGrids()");
		// System.out.println("   " + serverIdList);
		System.out.println("   map=" + map);
		System.out.println();
		prBiz.getBizContext().getGridContextClient().reset();
	}

	@Test
	public void testGetBucketIds()
	{
		prBiz.getBizContext().getGridContextClient().setGridIds("grid1", "grid2");
		int[] bucketIds = prBiz.getBucketIds();
		Assert.assertNotNull(bucketIds);
		// Assert.assertTrue(bucketIds.length == 20);
		System.out.println("testGetBucketIds()");
		System.out.print("   ");
		Arrays.sort(bucketIds);
		for (int i : bucketIds) {
			System.out.print(i + ", ");
		}
		System.out.println();
		System.out.println();
	}

	@Test
	public void testGetBucketIdsFuture()
	{
		Future<int[]> future = prBizFuture.getBucketIds();
		try {
			Thread.sleep(1000);
			int[] bucketIds = future.get();
			Assert.assertNotNull(bucketIds);
			// Assert.assertTrue(bucketIds.length == 20);
			System.out.println("testGetBucketIdsFuture()");
			System.out.print("   ");
			Arrays.sort(bucketIds);
			for (int i : bucketIds) {
				System.out.print(i + ", ");
			}
			System.out.println();
			System.out.println();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetBucketIdsFiltered()
	{
		Set<String> routingKeys = new HashSet();
		routingKeys.add("key1");
		routingKeys.add("key2");
		routingKeys.add("key3");
		routingKeys.add("key4");
		routingKeys.add("key5");
		routingKeys.add("key6");
		IGridContextClient gridContextClient = prBiz.getBizContext().getGridContextClient();
		gridContextClient.reset();
		Assert.assertTrue(gridContextClient.getRoutingKeys() == null);
		gridContextClient.setRoutingKeys(routingKeys);
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		int[] bucketIds = prBiz.getBucketIds();
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		Assert.assertNotNull(bucketIds);
		Assert.assertTrue(bucketIds.length > 0);
		System.out.println("testGetBucketIdsFiltered()");
		System.out.print("   ");
		for (int i : bucketIds) {
			System.out.print(i + ", ");
		}
		System.out.println();
		System.out.println();
	}

	@Test
	public void testGetBucketIds2()
	{
		int[] bucketIds = prBiz2.getBucketIds();
		Assert.assertNotNull(bucketIds);
		// Assert.assertTrue(bucketIds.length == 20);
		System.out.println("testGetBucketIds2()");
		System.out.print("   ");
		Arrays.sort(bucketIds);
		for (int i : bucketIds) {
			System.out.print(i + ", ");
		}
		System.out.println();
		System.out.println();
	}

	@Test
	public void testGetBucketMap()
	{
		IGridContextClient gridContextClient = prBiz.getBizContext().getGridContextClient();
		gridContextClient.reset();
		Assert.assertTrue(gridContextClient.getRoutingKeys() == null);
		gridContextClient.setRoutingKeys(Collections.singleton(0));
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		Map<String, String> map = prBiz.getBucketMap(0);
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		Assert.assertNotNull(map);
		System.out.println("testGetBucketMap()");
		System.out.println("   " + map);
		System.out.println();
	}

	@Test
	public void testGetBucketMapFuture()
	{
		IGridContextClient gridContextClient = prBizFuture.getBizContext().getGridContextClient();
		gridContextClient.reset();
		Assert.assertTrue(gridContextClient.getRoutingKeys() == null);
		gridContextClient.setRoutingKeys(Collections.singleton(0));
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		Future<Map<String, String>> future = prBizFuture.getBucketMap(0);
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		try {
			Thread.sleep(1000);
			Map<String, String> map = future.get(10000, TimeUnit.MILLISECONDS);
			Assert.assertNotNull(map);
			System.out.println("testGetBucketMap()");
			System.out.println("   " + map);
			System.out.println();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPutEntry()
	{
		IGridContextClient gridContextClient = prBiz.getBizContext().getGridContextClient();
		gridContextClient.reset();
		Assert.assertTrue(gridContextClient.getRoutingKeys() == null);
		gridContextClient.setRoutingKeys(Collections.singleton("single1"));
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		prBiz.putEntry("single1", "single1");
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		System.out.println("testPutEntry()");
		System.out.println("   success");
		System.out.println();
	}

	@Test
	public void testPutEntryFuture()
	{
		IGridContextClient gridContextClient = prBizFuture.getBizContext().getGridContextClient();
		gridContextClient.reset();
		Assert.assertTrue(gridContextClient.getRoutingKeys() == null);
		gridContextClient.setRoutingKeys(Collections.singleton("single1"));
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		prBizFuture.putEntry("single1", "single1");
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		System.out.println("testPutEntry()");
		System.out.println("   success");
		System.out.println();
	}
}
