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
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.pql.VirtualPath;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class VirtualPathClientTest
{

	private static IPado pado;
	
	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}
	
	@Test
	public void testVirtualPathClient_portfolio() throws FileNotFoundException, IOException
	{
		// Get input from the grid
		ITemporalBiz<String, JsonLite> temporalBiz = pado.getCatalog().newInstance(ITemporalBiz.class);
	
		// VirtualPathClient 
		File file = new File("db/vp/prms.vp_prms_customer_consignment_inventory_ref.json");
		JsonLite virtualPathDefinition = new JsonLite(file);
		VirtualPath vpc = new VirtualPath(virtualPathDefinition, pado);
		String input = "3001";
//		String input = "LastName:ppxwekk";
		List list = vpc.execute(input, -1, -1);
		
		// Print results
		System.out.println("Input: " + input);
		System.out.println("Output:");
		for (int i = 0; i < list.size(); i++) {
			JsonLite jl = (JsonLite)list.get(i);
			System.out.println("[" + i + "]");
			Set set = jl.keySet();
			for (Object key : set) {
				Object value = jl.get(key);
				if (key.equals("[ShipToUcn]")) {
					if (value != null) {
						List<JsonLite> list2 = (List<JsonLite>) value;
						System.out.println(key + " " + ((List)value).size());
						for (int j = 0; j < list2.size(); j++) {
							JsonLite jl2 = list2.get(j);
							printJsonLite(jl2, "   ");
						}
					} else {
						System.out.println(key + " " + value);
					}
				} else {
					System.out.println(key + " " + value);
				}
			}
//			System.out.println(jl.toString(4, false, false));
		}
	}
	
	void printJsonLite(JsonLite jl, String prefix)
	{
		Set<Map.Entry> set =  jl.entrySet();
		for (Map.Entry entry : set) {
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof List) {
				List<JsonLite> list2 = (List<JsonLite>)value;
				System.out.println(prefix + key + ": " + ((List)value).size());
				String prefix2 = prefix + "   ";
				String prefix3 = prefix2 + "   ";
				for (int j = 0; j < list2.size(); j++) {
					JsonLite jl2 = list2.get(j);
					System.out.println(prefix2 + "[" + j + "]");
					printJsonLite(jl2, prefix3);
				}
			} else {
				System.out.println(prefix + key + ": " + value);
			}
		}
	}
}
