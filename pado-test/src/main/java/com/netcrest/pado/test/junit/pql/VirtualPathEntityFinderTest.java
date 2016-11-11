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
package com.netcrest.pado.test.junit.pql;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.biz.impl.gemfire.VirtualPathEntityFinder;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.server.VirtualPathEngine;
import com.netcrest.pado.temporal.test.TemporalLoader;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class VirtualPathEntityFinderTest
{
	static TemporalLoader loader;

	static IPado pado;

	@BeforeClass
	public static void loginPado() throws PadoLoginException, Exception
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "test1", "test123".toCharArray());
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testVirtualPathEntityFinder() throws FileNotFoundException, IOException
	{
		// First prepare VirtualPathEngine which is used by the finder.
		File file = new File("db/vp/nw.vp_order.json");
		JsonLite vpd = new JsonLite(file);
		VirtualPathEngine.getVirtualPathEngine().addVirtualPathDefinition(pado.getCatalog(), vpd);
		
		// Get an order object from the grid to work with
		ITemporalBiz temporalBiz = pado.getCatalog().newInstance(ITemporalBiz.class, "nw/orders");
		JsonLite order = (JsonLite)temporalBiz.get("10357");
		
		System.out.println("VirtualPathEntityFinderTest.testVirtualPathEntityFinder()");
		System.out.println("--------------------------------------------------------");
		VirtualPathEntityFinder finder = new VirtualPathEntityFinder();
//		JsonLite keyMap = new JsonLite();
//		keyMap.put("CustomerID", "LILAS");
//		keyMap.put("OrderID", "10357");
//		keyMap.put("ShipVia", "3");
		JsonLite result = (JsonLite)finder.getReferences(pado.getCatalog(), "nw/vp_order", order, 1, -1, -1, null);
		System.out.println(result.toString(4, false, false));
	}
}
