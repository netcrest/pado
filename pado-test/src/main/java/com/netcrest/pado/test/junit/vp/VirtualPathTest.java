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
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.pql.VirtualPath;

public class VirtualPathTest
{
	static String pqls[] = new String[] {
	// "customer_master?${Input}", "vp_consignment_inventory?${0}",
	// "ucn_master?IndUcn:${Input}.Ucn",
	// "prms_master?BillToUcn:${0}.ShipToUcn Xyz:${Input}.Ucn",
	// "consignment_inventory?CustomerNumber:${1}.PrmsCustomerNumber",
	"master_ucn?2669" };

	private static IPado pado;
	private static ITemporalBiz temporalBiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		temporalBiz = pado.getCatalog().newInstance(ITemporalBiz.class);
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	// @Test
	public void testCU()
	{
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
	public void testVCULocal() throws FileNotFoundException, IOException
	{
//		String input = "FISCPER:2014011 AND USLCHDID:P04005";
		String input = "USLCHDID:P04005";
		File file = new File("db/vp/asset.vp_alteryx.json");
		JsonLite virtualPathDefinition = new JsonLite(file);
		System.out.println(virtualPathDefinition.toString(4, false, false));
		VirtualPath vp = new VirtualPath(virtualPathDefinition, pado);
		System.out.println(vp);

		// Execute
		List list = vp.execute(input, -1, -1);
		
		System.out.println("VirtualPathTest.testVCULocal()");
		System.out.println("------------------------------");
		System.out.println("Input:");
		System.out.println("   " + input);
		System.out.println("Output:");
		for (int i = 0; i < list.size(); i++) {
			JsonLite jl = (JsonLite) list.get(i);
			System.out.println("   [" + i + "] " + jl.toString(4, false, false));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
//	@Test
	public void testVCURemote() throws FileNotFoundException, IOException
	{
		String queryString = "asset/vp_alteryx?FISCPER:2014011 AND USLCHDID:P04005";
		List<JsonLite> list = temporalBiz.getQueryValues(queryString, -1, -1);
		
		System.out.println("VirtualPathTest.testVCURemote()");
		System.out.println("-------------------------------");
		System.out.println("Query:");
		System.out.println("   " + queryString);
		System.out.println("Output:");
		if (list == null) {
			System.out.println("   null");
		} else {
			for (int i = 0; i < list.size(); i++) {
				JsonLite jl = (JsonLite) list.get(i);
				System.out.println("   [" + i + "] " + jl.toString(4, false, false));
			}
		}
	}

}
