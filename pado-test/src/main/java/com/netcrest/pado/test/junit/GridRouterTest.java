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

import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;

public class GridRouterTest
{
	private static IPado pado;
	private static IGridMapBiz<ITemporalKey, ITemporalData> padoMapBiz;
	private static Set<ITemporalKey> temporalKeySet;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		if (Pado.isClosed()) {
			Pado.connect("localhost:20000", true);
		}
		pado = Pado.login("app3", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		padoMapBiz = catalog.newInstance(IGridMapBiz.class);
		padoMapBiz.setGridPath("shared/position");
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testMapBizPut() throws Exception
	{
		System.out.println();
		System.out.println("GridRouterTest.testMapBizPut()");
		System.out.println("==============================");
		PositionLoader loader = new PositionLoader();
		temporalKeySet = loader.loadPositions(padoMapBiz, 10);
		Assert.assertNotNull(temporalKeySet);
		Assert.assertEquals(10, temporalKeySet.size());
	}

	@Test
	public void testGridMapBizPuts() throws Exception
	{
		System.out.println();
		System.out.println("GridRouterTest.testGridMapBizPutsGets()");
		System.out.println("======================================");
		
		// Puts
		PositionLoader loader = new PositionLoader();
		temporalKeySet = loader.loadPositions(padoMapBiz, 10);
		Assert.assertNotNull(temporalKeySet);
		Assert.assertEquals(10, temporalKeySet.size());

		// Gets
		for (ITemporalKey key : temporalKeySet) {
			ITemporalData data = padoMapBiz.get(key);
			Assert.assertNotNull(data);
		}
	}

	@Test
	public void testGridMapBizPutAllGetAll() throws Exception
	{
		System.out.println();
		System.out.println("GridRouterTest.testGridMapBizPutAllGetAll()");
		System.out.println("==========================================");
		
		// PutAll
		PositionLoader loader = new PositionLoader();
		temporalKeySet = loader.bulkLoadPositions(padoMapBiz, 100);
		Assert.assertNotNull(temporalKeySet);
		Assert.assertEquals(100, temporalKeySet.size());

		// GetAll
		Map<ITemporalKey, ITemporalData> map = padoMapBiz.getAll(temporalKeySet);
		Assert.assertEquals(100, map.size());
		for (ITemporalKey key : temporalKeySet) {
			Assert.assertNotNull(map.get(key));
		}
	}
}
