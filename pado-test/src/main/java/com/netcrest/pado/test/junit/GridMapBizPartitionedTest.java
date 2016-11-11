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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.biz.IPathBiz.PathType;
import com.netcrest.pado.exception.EntryExistsException;
import com.netcrest.pado.exception.PadoException;

/**
 * Tests IGridMapBiz on a partitioned path. This class and
 * {@link GridMapBizReplicatedTest} are identical except that it tests a
 * partitioned path (test/partitioned). The test case results should be
 * identical except for some constraints enforced by the underlying data grid.
 * For example, the {@link IGridMapBiz#clear()} method may throw an
 * PadoException for partitioned paths because it may not be supported by the
 * underlying data grid.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings("unchecked")
public class GridMapBizPartitionedTest
{
	static IPado pado;
	static String appId = "test";
	static String GRID_PATH = "test/partitioned";
	static String ARRAY_GRID_PATH = "test/partitioned_array";
	static IGridMapBiz<String, String> gridMapBiz;
	static IGridMapBiz<String, String[][]> arrayMapBiz;
	static IPathBiz pathBiz;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		if (Pado.isClosed()) {
			Pado.connect("localhost:20000", true);
		}
		pado = Pado.login(appId, "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		gridMapBiz = catalog.newInstance(IGridMapBiz.class, GRID_PATH);
		arrayMapBiz = catalog.newInstance(IGridMapBiz.class, ARRAY_GRID_PATH);
		pathBiz = catalog.newInstance(IPathBiz.class);
		createPaths();
		putData();
		putArrayData();
	}

	private static void createPaths()
	{
		pathBiz.createPath(pado.getGridId(), GRID_PATH, PathType.PARTITION, true);
		pathBiz.createPath(pado.getGridId(), ARRAY_GRID_PATH, PathType.PARTITION, true);
	}

	// Put data
	private static void putData()
	{
		int count = 10;
		for (int i = 0; i < count; i++) {
			String key = "put" + i;
			String value = "putValue" + i;
			gridMapBiz.put(key, value);
		}
	}

	// Put double array data
	private static void putArrayData()
	{
		int accountCount = 10;
		for (int i = 0; i < accountCount; i++) {
			String key = "put" + i;
			int count = 10;
			String[][] array = new String[count][count];
			for (int j = 0; j < count; j++) {
				array[j] = new String[count];
				for (int k = 0; k < count; k++) {
					array[j][k] = "putValue" + k;
				}
			}
			arrayMapBiz.put(key, array);
		}
	}

	@Test
	public void testGetGridPath()
	{
		String gridPath = gridMapBiz.getGridPath();
		Assert.assertEquals(GRID_PATH, gridPath);
	}

	@Test
	public void testPutAlGetAlll()
	{
		int count = 10;

		// PutAll
		Map<String, String> map = new HashMap();
		for (int i = 0; i < count; i++) {
			String key = "putAll" + i;
			String value = "putAllValue" + i;
			map.put(key, value);
		}
		gridMapBiz.putAll(map);

		// GetAll
		Set<String> keySet = new HashSet();
		for (int i = 0; i < count; i++) {
			keySet.add("putAll" + i);
		}
		map = gridMapBiz.getAll(keySet);
		int expected = keySet.size();
		int actual = map.size();
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testPutGet()
	{
		// Put
		putData();

		// Get
		int count = 10;
		for (int i = 0; i < count; i++) {
			String key = "put" + i;
			String value = gridMapBiz.get(key);
			String expected = "putValue" + i;
			String actual = value;
			Assert.assertEquals(expected, actual);
		}
	}

	@Test
	public void testPutGetArray()
	{
		// Put
		putArrayData();

		// Delay some so that is fully put in to the grid
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// ignore
		}

		// Get
		int count = 10;
		for (int i = 0; i < count; i++) {
			String key = "put" + i;
			String value = gridMapBiz.get(key);
			String expected = "putValue" + i;
			String actual = value;
			Assert.assertEquals(expected, actual);
		}
	}

	@Test
	public void testSubscribe()
	{
		gridMapBiz.subscribeEntries(".*");
	}

	@Test
	public void testSize()
	{
		// Despite the claim made in Gemfire 7.x doc on Region.size(),
		// partitioned region size is not a distributed call and it returns
		// the local region size.
		int size = gridMapBiz.size();
		Assert.assertTrue(size == 0);
	}

	@Test
	public void testSizeRemote()
	{
		int size = gridMapBiz.size(true);
		Assert.assertTrue(size > 0);
	}

	@Test
	public void testIsEmpty()
	{
		boolean isEmpty = gridMapBiz.isEmpty();
		Assert.assertTrue(isEmpty);
	}

	public void testIsEmptyRemote()
	{
		boolean isEmpty = gridMapBiz.isEmpty(true);
		Assert.assertFalse(isEmpty);
	}

	/**
	 * Tests {@link IGridMapBiz#containsKey(Object)} which checks the local
	 * cache. The "test" grid path configured as cache-less, and therefore this
	 * it is always locally empty.
	 */
	@Test
	public void testContainsKey()
	{
		String key = "put1";
		boolean containsKey = gridMapBiz.containsKey(key);
		Assert.assertFalse(containsKey);

		key = "foo1";
		containsKey = gridMapBiz.containsKey(key);
		Assert.assertFalse(containsKey);
	}

	/**
	 * Tests {@link IGridMapBiz#containsKey(Object, boolean)} to check the
	 * remote cache.
	 */
	@Test
	public void testContainsKeyRemote()
	{
		String key = "put1";
		boolean containsKey = gridMapBiz.containsKey(key, true);
		Assert.assertTrue(containsKey);

		key = "foo1";
		containsKey = gridMapBiz.containsKey(key, true);
		Assert.assertFalse(containsKey);
	}

	/**
	 * Tests {@link IGridMapBiz#containsValue(Object)} which checks the local
	 * cache. The "test" grid path configured as cache-less, and therefore this
	 * it is always locally empty.
	 */
	@Test
	public void testContainsValue()
	{
		String value = "putValue1";
		boolean containsValue = gridMapBiz.containsValue(value);
		Assert.assertFalse(containsValue);

		value = "foo1";
		containsValue = gridMapBiz.containsValue(containsValue);
		Assert.assertFalse(containsValue);
	}

	/**
	 * Tests {@link IGridMapBiz#containsValue(Object, boolean)} to check the
	 * remote cache.
	 */
	@Test
	public void testContainsValueRemote()
	{
		String value = "putValue1";
		boolean containsValue = gridMapBiz.containsValue(value, true);
		Assert.assertTrue(containsValue);

		value = "foo1";
		containsValue = gridMapBiz.containsValue(containsValue, true);
		Assert.assertFalse(containsValue);
	}

	@Test
	public void testRemove()
	{
		String key = "put1";
		String expected = "putValue1";
		String actual = gridMapBiz.remove(key);
		Assert.assertNull(actual);

		// put back data
		putData();
	}

	@Test
	public void testCreate()
	{
		// doest not exist
		String key = "put1";
		String value = "putValue1";
		gridMapBiz.create(key, value);
		String expected = value;
		String actual = gridMapBiz.get(key);
		Assert.assertEquals(expected, actual);

		// already exists in the remote cache but it does not exist
		// in the local cache because the "test" path is cache-less
		try {
			gridMapBiz.create(key, value);
		} catch (EntryExistsException ex) {
			// ignore - expected
		}
	}

	@Test
	public void testCreateRemote()
	{
		// doest not exist
		String key = "put1";
		String value = "putValue1";
		gridMapBiz.create(key, value);
		String expected = value;
		String actual = gridMapBiz.get(key);
		Assert.assertEquals(expected, actual);

		// already exists in the remote cache. expects EntryExistsException
		try {
			gridMapBiz.create(key, value, true);
			Assert.fail("EntryExistsException not thrown");
		} catch (EntryExistsException ex) {
			// ignore - expected
		}
	}

	@Test
	public void testInvalidate()
	{
		String key = "put1";
		gridMapBiz.invalidate(key);
		// TODO: validate test

		// put back data
		putData();
	}

	/**
	 * Tests {@link IGridMapBiz#keySet()} which is always empty because the
	 * "test" path is cache-less.
	 */
	@Test
	public void testKeySet()
	{
		Set<String> keySet = gridMapBiz.keySet();
		Assert.assertTrue(keySet.size() == 0);
	}

	/**
	 * Tests {@link IGridMapBiz#values()} which is always empty because the
	 * "test" path is cache-less.
	 */
	@Test
	public void testValues()
	{
		Collection<String> values = gridMapBiz.values();
		Assert.assertTrue(values.size() == 0);
	}

	/**
	 * Tests {@link IGridMapBiz#entrySet()} which is always empty because the
	 * "test" path is cache-less.
	 */
	@Test
	public void testEntrySet()
	{
		Set<Map.Entry<String, String>> set = gridMapBiz.entrySet();
		Assert.assertTrue(set.size() == 0);
	}

	@Test
	public void testClear()
	{
		try {
			int size = gridMapBiz.size(true);
			Assert.assertTrue(size > 0);
			gridMapBiz.clear();
			size = gridMapBiz.size(true);
			Assert.assertTrue(size == 0);
		} catch (PadoException ex) {
			// PadoException may be thrown if the clear method is not supported
			// by the underlying data grid. For GemFire, partitioned region
			// will throw this exception.
			ex.printStackTrace();
		}

		// put data back for other tests
		putData();
	}

}
