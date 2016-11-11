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
package com.netcrest.pado.test.junit.vp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.biz.IVirtualPathBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.VirtualPath2;
import com.netcrest.pado.server.VirtualPathEngine;

public class VirtualPathTest
{
	static String pqls[] = new String[] {
			// "customer_master?${Input}", "vp_consignment_inventory?${0}",
			// "ucn_master?IndUcn:${Input}.Ucn",
			// "prms_master?BillToUcn:${0}.ShipToUcn Xyz:${Input}.Ucn",
			// "consignment_inventory?CustomerNumber:${1}.PrmsCustomerNumber",
			"nw/orders?OrderID:${OrderID}", "nw/vp_products?Choco*" };

	private static IPado pado;
	private static ITemporalBiz temporalBiz;
	private static IVirtualPathBiz virtualPathBiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		temporalBiz = pado.getCatalog().newInstance(ITemporalBiz.class);
		virtualPathBiz = pado.getCatalog().newInstance(IVirtualPathBiz.class);
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testCu()
	{
		System.out.println("VirtualPathTest.testCu()");
		System.out.println("------------------------");
		for (int i = 0; i < pqls.length; i++) {
			CompiledUnit cu = new CompiledUnit(pqls[i]);
			System.out.println("[" + i + "] " + cu.getCompiledQuery());
			Object[] attributes = cu.getAttributes();
			System.out.println("    path=" + cu.getPaths()[0]);
			for (int j = 0; j < attributes.length; j++) {
				System.out.println("   [" + j + "] " + attributes[j]);
			}
		}
		System.out.println();
	}

	@SuppressWarnings({ "rawtypes" })
	@Test
	public void testVpCustomerLocal() throws FileNotFoundException, IOException
	{
		System.out.println("VirtualPathTest.testVpCustomerLocal()");
		System.out.println("-----------------------------------");

		// String input = "Choco*";
		// File file = new File("db/vp/nw.vp_product.json");
		String input = "LILAS";
		File file = new File("db/vp/nw.vp_customer.json");
		JsonLite virtualPathDefinition = new JsonLite(file);
		System.out.println(virtualPathDefinition.toString(4, false, false));
		VirtualPath2 vp = new VirtualPath2(virtualPathDefinition, pado);

		// Execute
		long startTime = System.currentTimeMillis();
		List list = vp.execute(-1, -1, input);
		long timeTook = System.currentTimeMillis() - startTime;
		System.out.println("Input:");
		System.out.println("   " + input);
		System.out.println("Output:");
		for (int i = 0; i < list.size(); i++) {
			JsonLite jl = (JsonLite) list.get(i);
			System.out.println("[" + i + "] " + jl.toString(4, false, false));
		}
		System.out.println("Elapsed Time (msec): " + timeTook);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testVpCustomerRemote() throws FileNotFoundException, IOException
	{
		System.out.println("VirtualPathTest.testVpCustomerRemote()");
		System.out.println("------------------------------------");

		// String queryString = "nw/vp_product?Choco*";
		String queryString = "nw/vp_customer?LILAS";
		long startTime = System.currentTimeMillis();
		List<JsonLite> list = temporalBiz.getQueryValues(queryString, -1, -1);
		long timeTook = System.currentTimeMillis() - startTime;

		System.out.println("Query:");
		System.out.println("   " + queryString);
		System.out.println("Output:");
		if (list == null) {
			System.out.println("   null");
		} else {
			for (int i = 0; i < list.size(); i++) {
				JsonLite jl = (JsonLite) list.get(i);
				System.out.println("[" + i + "] " + jl.toString(4, false, false));
			}
		}
		System.out.println("Elapsed Time (msec): " + timeTook);
	}

	@SuppressWarnings({ "rawtypes" })
	@Test
	public void testVpProductLocal() throws FileNotFoundException, IOException
	{
		System.out.println("VirtualPathTest.testVpProductLocal()");
		System.out.println("-----------------------------------");

		String input = "(\"13\" \"68\" \"17\")";
		input = "ProductID:" + input;
		File file = new File("db/vp/nw.vp_product.json");
		JsonLite virtualPathDefinition = new JsonLite(file);
		System.out.println(virtualPathDefinition.toString(4, false, false));
		VirtualPath2 vp = new VirtualPath2(virtualPathDefinition, pado);

		// Execute
		long startTime = System.currentTimeMillis();
		List list = vp.execute(-1, -1, input);
		long timeTook = System.currentTimeMillis() - startTime;
		System.out.println("Input:");
		System.out.println("   " + input);
		System.out.println("Output:");
		for (int i = 0; i < list.size(); i++) {
			JsonLite jl = (JsonLite) list.get(i);
			System.out.println("[" + i + "] " + jl.toString(4, false, false));
		}
		System.out.println("Elapsed Time (msec): " + timeTook);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testVpProductRemote() throws FileNotFoundException, IOException
	{
		System.out.println("VirtualPathTest.testVpProductRemote()");
		System.out.println("------------------------------------");

		String input = "(\"13\" \"68\" \"17\")";
		input = "ProductID:" + input;
		String queryString = "nw/vp_product?" + input;
		long startTime = System.currentTimeMillis();
		List<JsonLite> list = temporalBiz.getQueryValues(queryString, -1, -1);
		long timeTook = System.currentTimeMillis() - startTime;

		System.out.println("Query:");
		System.out.println("   " + queryString);
		System.out.println("Output:");
		if (list == null) {
			System.out.println("   null");
		} else {
			for (int i = 0; i < list.size(); i++) {
				JsonLite jl = (JsonLite) list.get(i);
				System.out.println("[" + i + "] " + jl.toString(4, false, false));
			}
		}
		System.out.println("Elapsed Time (msec): " + timeTook);
	}

	@SuppressWarnings({ "rawtypes" })
	@Test
	public void testVpEntityOrderLocal() throws FileNotFoundException, IOException
	{
		System.out.println("VirtualPathTest.testVpEntityOrderLocal()");
		System.out.println("----------------------------------------");

		String input[] = new String[] { "VICTE"/* CustomerID */, "10251"/* OrderID */, "1"/* ShipVia */ };
		File file = new File("db/vp/nw.vp_order.json");
		JsonLite virtualPathDefinition = new JsonLite(file);
		System.out.println(virtualPathDefinition.toString(4, false, false));

		// Prepare virtual path engine
		VirtualPathEngine.getVirtualPathEngine().addVirtualPathDefinition(pado.getCatalog(), virtualPathDefinition);

		// Test VirtualPath
		VirtualPath2 vp = new VirtualPath2(virtualPathDefinition, pado);

		// Execute
		long startTime = System.currentTimeMillis();
		List list = vp.executeEntity(-1, -1, input);
		long timeTook = System.currentTimeMillis() - startTime;
		System.out.println("Input:");
		printInput(input);

		System.out.println("Output:");
		for (int i = 0; i < list.size(); i++) {
			JsonLite jl = (JsonLite) list.get(i);
			System.out.println("[" + i + "] " + jl.toString(4, false, false));
		}
		System.out.println("Elapsed Time (msec): " + timeTook);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testVpEntityOrderRemote() throws FileNotFoundException, IOException
	{
		System.out.println("VirtualPathTest.testVpEntityOrderRemote()");
		System.out.println("-----------------------------------------");

		// String input[] = new String[] { "VICTE"/*CustomerID*/,
		// "10251"/*OrderID*/, "1"/*ShipVia*/};
		// String virtualPath = "nw/vp_order";
		String input[] = new String[] { "10251"/* OrderID */ };
		String virtualPath = "nw/vp_order2";
		int depth = 2;
		long startTime = System.currentTimeMillis();
		IScrollableResultSet<JsonLite> srs = virtualPathBiz.executeEntity(virtualPath, depth, -1, -1, input);
		long timeTook = System.currentTimeMillis() - startTime;

		System.out.println("VirtualPath:");
		System.out.println("   " + virtualPath);
		System.out.println("Input:");
		printInput(input);
		System.out.println("Output:");
		if (srs == null) {
			System.out.println("   null");
		} else {
			do {
				List<JsonLite> list = srs.toList();
				int i = 0;
				for (JsonLite jl : list) {
					System.out.println("[" + i++ + "] " + jl.toString(4, false, false));
				}
			} while (srs.nextSet());
		}
		System.out.println("Elapsed Time (msec): " + timeTook);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testVpOrderRemote() throws FileNotFoundException, IOException
	{
		System.out.println("VirtualPathTest.testVpOrderRemote()");
		System.out.println("-----------------------------------");

		String input = "VICTE";
		String virtualPath = "nw/vp_order";
		long startTime = System.currentTimeMillis();
		IScrollableResultSet<JsonLite> srs = virtualPathBiz.execute(virtualPath, -1, -1, input);
		long timeTook = System.currentTimeMillis() - startTime;

		System.out.println("VirtualPath:");
		System.out.println("   " + virtualPath);
		System.out.println("Input:");
		printInput(input);
		System.out.println("Output:");
		if (srs == null) {
			System.out.println("   null");
		} else {
			do {
				List<JsonLite> list = srs.toList();
				int i = 0;
				for (JsonLite jl : list) {
					System.out.println("[" + i++ + "] " + jl.toString(4, false, false));
				}
			} while (srs.nextSet());
		}
		System.out.println("Elapsed Time (msec): " + timeTook);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testVpOrderTemporalBizRemote() throws FileNotFoundException, IOException
	{
		System.out.println("VirtualPathTest.testVpOrderTemporalBizRemote()");
		System.out.println("--------------------------------------------");

		String input = "VICTE";
		String queryString = "nw/vp_order2?CustomerID:" + input;
		long startTime = System.currentTimeMillis();
		temporalBiz.setDepth(3);
		List<JsonLite> list = temporalBiz.getQueryValues(queryString, -1, -1);
		long timeTook = System.currentTimeMillis() - startTime;

		System.out.println("Query:");
		System.out.println("   " + queryString);
		System.out.println("Output:");
		if (list == null) {
			System.out.println("   null");
		} else {
			for (int i = 0; i < list.size(); i++) {
				JsonLite jl = (JsonLite) list.get(i);
				System.out.println("[" + i + "] " + jl.toString(4, false, false));
			}
		}
		System.out.println("Elapsed Time (msec): " + timeTook);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testVpOrderTemporalBizRemoteScrolled() throws FileNotFoundException, IOException
	{
		System.out.println("VirtualPathTest.testVpOrderTemporalBizRemoteScrolled()");
		System.out.println("------------------------------------------------------");

		String input = "72";
		String queryString = "stitch/nw/product_order_entity?" + input;
		long startTime = System.currentTimeMillis();
		temporalBiz.setDepth(-1);
		IScrollableResultSet<JsonLite> srs = temporalBiz.getValueResultSet(queryString, null, true, 100, true);
		long timeTook = System.currentTimeMillis() - startTime;

		System.out.println("Query:");
		System.out.println("   " + queryString);
		System.out.println("Output:");
		if (srs == null) {
			System.out.println("   null");
		} else {
			do {
				List list = srs.toList();

				for (int i = 0; i < list.size(); i++) {
					JsonLite jl = (JsonLite) list.get(i);
					System.out.println("[" + i + "] " + jl.toString(4, false, false));
				}
			} while (srs.nextSet());
		}
		System.out.println("Elapsed Time (msec): " + timeTook);
	}

	private void printInput(String... args)
	{
		int i = 0;
		System.out.print("   ");
		for (String arg : args) {
			if (i > 0) {
				System.out.print(", ");
			}
			System.out.print(arg);
			i++;
		}
		System.out.println();
	}

}
