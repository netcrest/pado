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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;

/**
 * TemporalEntrySearchTest requires "mygrid".
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemporalAddData
{
	private static IPado pado;
	private static ITemporalBiz<String, JsonLite> temporalBiz;

	public static long START_VALID_TIME1 = 1446569583396l;
	public static long END_VALID_TIME1 = 4102376400000l;
	public static long WRITTEN_TIME1 = 1446569583396l;
	
	public static long START_VALID_TIME2 = 1446569583730l;
	public static long END_VALID_TIME2 = 4102376400000l;
	public static long WRITTEN_TIME2 = 1446569583730l;

	@BeforeClass
	public static void loginPado() throws PadoLoginException, ParseException
	{
		// Security -- Comment out the following to disable security
		// System.setProperty("gemfireSecurityPropertyFile",
		// "etc/client/gfsecurity.properties");
		// System.setProperty("pado.security.aes.user.certificate",
		// "etc/user.cer");
		// System.setProperty("pado.security.enabled", "true");

		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "test1", "test123".toCharArray());
		ICatalog catalog = pado.getCatalog();
		temporalBiz = catalog.newInstance(ITemporalBiz.class);
		temporalBiz.setGridPath("temporal");
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testAddTemporalData() throws ParseException
	{
		JsonLite jl = new JsonLite();
		jl.put("Flag", "N");
		temporalBiz.put("1", jl, START_VALID_TIME1, END_VALID_TIME1, WRITTEN_TIME1, false);
		jl.put("Flag", "Y");
		temporalBiz.put("1", jl, START_VALID_TIME2, END_VALID_TIME2, WRITTEN_TIME2, false);
	}
}
