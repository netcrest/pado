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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.annotation.RouterType;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.biz.IUtilBiz;
import com.netcrest.pado.exception.PadoLoginException;

public class GridRoutingTableTest
{
	private static IPado pado;
	private static IGridMapBiz gridMapBiz;
	private static IUtilBiz utilBiz;
	
	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		if (Pado.isClosed()) {
			Pado.connect("localhost:20000", true);
		}
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}
	
	@Test
	public void testLogin() throws PadoLoginException
	{
		pado = Pado.login("app1", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		String classNames[] = catalog.getAllBizClassNames();
		System.out.println();
		System.out.println("Catalog:");
		for (String className : classNames) {
			System.out.println("   " + className);
		}
		System.out.println();
		gridMapBiz = catalog.newInstance(IGridMapBiz.class);
		Assert.assertNotNull(gridMapBiz);
		utilBiz = catalog.newInstance(IUtilBiz.class);
		Assert.assertNotNull(utilBiz);
	}
	
	@Test
	public void testSharedRef()
	{
		gridMapBiz.setGridPath("shared/ref");
		gridMapBiz.put("k1", "hello, world");
		String val = (String)gridMapBiz.get("k1");
		Assert.assertNotNull(val);
		System.out.println(val);
	}
	
	@Test
	public void testEchoCost()
	{
		// invoke a grid that has the least cost determined by the grid routing table
		utilBiz.getBizContext().reset();
		for (int i = 0; i < 5; i++) {
			String message = utilBiz.echo("Echo COST: Greetings!");
			System.out.println(message);
		}
	}
	
	@Test
	public void testEchoLocation()
	{
		// invoke a grid in the "uk" location that has the least cost
		utilBiz.getBizContext().reset();
		utilBiz.getBizContext().getGridContextClient().setGridLocation("uk");
		utilBiz.getBizContext().getGridContextClient().setRouterType(RouterType.LOCATION);
		for (int i = 0; i < 5; i++) {
			String message = utilBiz.echo("Echo LOCATION: " + utilBiz.getBizContext().getGridContextClient().getGridLocation());
			System.out.println(message);
		}
	}
}
