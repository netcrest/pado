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
package com.netcrest.pado.temporal.test.junit;

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
import com.netcrest.pado.temporal.test.TemporalLoader;

/**
 * PadoMapBizTest requires "mygrid".
 * @author dpark
 *
 */
public class PadoMapBizTest
{
	private static IPado pado;
	private static IGridMapBiz<ITemporalKey, ITemporalData> gridMapBiz;
	private static Set<ITemporalKey> temporalKeySet;
	
	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		gridMapBiz = catalog.newInstance(IGridMapBiz.class);
		gridMapBiz.getBizContext().getGridContextClient().setGridIds("grid1");
		gridMapBiz.setGridPath("test/partitioned");
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}
	
	@Test
	public void testGridMapBizPuts() throws Exception
	{
		TemporalLoader loader = new TemporalLoader();
		temporalKeySet = loader.loadPositionsByGridMap(gridMapBiz, 100);
		Assert.assertEquals(100, temporalKeySet.size());
	}
	
	@Test
	public void testGridMapBizGets() throws Exception
	{
		for (ITemporalKey key : temporalKeySet) {
			ITemporalData data = gridMapBiz.get(key);
			Assert.assertNotNull(data);
		}
	}
	
	@Test
	public void testGridMapBizPutAll() throws Exception
	{
		TemporalLoader loader = new TemporalLoader();
		temporalKeySet = loader.loadPositionsByPutAll(gridMapBiz, 1000);
		Assert.assertEquals(temporalKeySet.size(), temporalKeySet.size());
	}

	@Test
	public void testGridMapBizGetAll() throws Exception
	{
		Map<ITemporalKey, ITemporalData> map = gridMapBiz.getAll(temporalKeySet);
		Assert.assertEquals(temporalKeySet.size(), map.size());
		for (ITemporalKey key : temporalKeySet) {
			Assert.assertNotNull(map.get(key));
		}
	}
	
}
