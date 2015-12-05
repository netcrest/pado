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

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.biz.IUtilBiz;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.info.WhichInfo;
import com.netcrest.pado.temporal.ITemporalData;

public class PartitionResolverTest
{
	private static IPado pado;
	private static IGridMapBiz gridMapBiz;
	private static IUtilBiz utilBiz;
	private final static String gridPath = "test/replicated";

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:21000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		gridMapBiz = catalog.newInstance(IGridMapBiz.class, gridPath);
		utilBiz = catalog.newInstance(IUtilBiz.class, gridPath);
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@SuppressWarnings("rawtypes")
	private void printResults(List list)
	{
		if (list == null || list.size() == 0) {
			System.out.println("   Query results not found");
		} else {
			for (int i = 0; i < list.size(); i++) {
				Object obj = list.get(i);
				if (obj instanceof ITemporalData) {
					((ITemporalData) obj).__getTemporalValue().deserializeAll();
				}
				System.out.println("   " + obj);
			}
		}
		System.out.println();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testRoutingKey()
	{
		System.out.println("PartitionResolverTest.testRoutingKey()");
		System.out.println("--------------------------------------");

		// All of the entries must be in the same VM because the routing key is
		// the same.
		
		// put
		for (int i = 1; i <= 10; i++) {
			String key = "key" + i;
			CacheKey actualKey = new CacheKey(key, "routingKey");
			gridMapBiz.put(actualKey, "Hello, world " + i);
		}

		// which
		System.out.println("which():");
		for (int i = 1; i <= 10; i++) {
			String key = "key" + i;
			List<WhichInfo> list = utilBiz.which(gridPath, key);
			for (int j = 0; j < list.size(); j++) {
				System.out.println("   " + j + ". " + list.get(j));
			}
		}
		System.out.println();
		
		// whichRoutingKey
		System.out.println("whichRoutingKey():");
		String routingKey = "routingKey";
		WhichInfo whichInfo = utilBiz.whichRoutingKey(gridPath, routingKey);
		System.out.println("   " + whichInfo);
		System.out.println();

		// query
		System.out.println("executeRoutingQueyr()");
		for (int i = 0; i < 100; i++) {
			List list = utilBiz.executeRoutingQuery("select * from /mock/" + gridPath, i + "");
			System.out.print("   " + i + ". ");
			printResults(list);
		}
		System.out.println();
	}
}
