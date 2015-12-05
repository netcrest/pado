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
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.server.VirtualPathEngine;

public class VirtualPathEngineTest
{
	private static IPado pado;

	@BeforeClass
	public static void loginPado() throws PadoLoginException, FileNotFoundException, IOException
	{
//		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
//		Pado.connect("localhost:20000", true);
//		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		
//		JsonLite vpd = new JsonLite(new File("db/vp/prms.vp_prms_customer_consignment_inventory.json"));
		JsonLite vpd = new JsonLite(new File("db/vp/asset.vp_alteryx.json"));
		VirtualPathEngine.getVirtualPathEngine().addVirtualPathDefinition(vpd);
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}


	@SuppressWarnings("rawtypes")
	@Test
	public void testVirtualPathEngine()
	{
		System.out.println("VirtualPathEngineTest.testVirtualPathEngine()");
		System.out.println("---------------------------------------------");
//		String pql = "prms/vp_prms_customer_consignment_inventory?IndUcn:2710";
		String pql = "asset/vp_alteryx?FISCPER:2014011 AND USLCHDID:P04005";
		List<JsonLite> list = VirtualPathEngine.getVirtualPathEngine().execute(pql, -1, -1);
		for (JsonLite jl : list) {
			System.out.println(jl);
		}
		System.out.println(list);
	}
}
