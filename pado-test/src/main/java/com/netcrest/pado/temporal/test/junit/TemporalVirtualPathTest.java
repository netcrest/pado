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

import java.text.ParseException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemporalVirtualPathTest
{
	private static IPado pado;
	private static ITemporalBiz<String, JsonLite> temporalBiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		temporalBiz = catalog.newInstance(ITemporalBiz.class);
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}
	
	private void printResults(List<JsonLite> list)
	{	
		if (list == null) {
			System.out.println("null");
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			JsonLite jl = (JsonLite)list.get(i);
			System.out.println("[" + i + "] " + jl.toString(4, false, false));
		}
	}

	@Test
	public void testVirtualPathQuery() throws ParseException
	{
		System.out.println("TemporalVirtualPathTest.testVirtualPathQuery()");
		System.out.println("----------------------------------------------");
		temporalBiz.getBizContext().getGridContextClient().setGridIds("go");
		temporalBiz.setGridPath("prms/vp_prms_customer_consignment_inventory");
		long startTime = System.currentTimeMillis();
		List<JsonLite> list = temporalBiz.getQueryValues("prms/vp_prms_customer_consignment_inventory?ShipToUcn:(2669 1123003)", -1, -1);
		long delta = System.currentTimeMillis() - startTime;
		printResults(list);

		System.out.println("Elapsed time (msec): " + delta);
		System.out.println();
	}
}
