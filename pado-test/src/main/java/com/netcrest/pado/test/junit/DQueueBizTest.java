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
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IDQueueBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DQueueBizTest
{
	private static IPado pado;
	private static IDQueueBiz<JsonLite> dqueueBiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		dqueueBiz = catalog.newInstance(IDQueueBiz.class, "order", "mygrid");
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testDQueue()
	{
		JsonLite order = new JsonLite();
		order.put("OrderId", 1);
		order.put("Price", 10.0);
		order.put("Quantity", 1);
		order.put("LastName", "Smith");
		order.put("FirstName", "Joe");
		order.put("CustomerNumber", 123);

		dqueueBiz.offer("order", order);
	}
}
