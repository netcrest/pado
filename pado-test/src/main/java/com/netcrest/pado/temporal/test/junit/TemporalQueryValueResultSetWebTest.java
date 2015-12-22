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
import com.netcrest.pado.index.service.IScrollableResultSet;

/**
 * TemporalQueryValueResultSetWebTest requires "mygrid". Note that this test
 * runs interactively.
 * 
 * @author dpark
 * 
 */
public class TemporalQueryValueResultSetWebTest
{
	private static IPado pado;
	@SuppressWarnings("rawtypes")
	private static ITemporalBiz<String, JsonLite> temporalBiz;

	@SuppressWarnings({ "unchecked" })
	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		// Security
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
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@SuppressWarnings("rawtypes")
	// @Test
	public void testGetValueResultSet() throws ParseException
	{
		String queryString = "account?a*";
		String orderBy = null;
		boolean orderAscending = true;
		// Date validAtDate = formatter.parse("8/26/2011");
		System.out.println("TemporalQueryValueResultSetWebTest.testGetValueResultSet()");
		System.out.println("----------------------------------------------------------");
		long startTime = System.currentTimeMillis();
		IScrollableResultSet<JsonLite> sr = (IScrollableResultSet<JsonLite>) temporalBiz.getValueResultSet(queryString,
				orderBy, orderAscending, 500, true);
		int page = 0;
		int i = 0;
		// total: 32447
		// sr.goToSet(31001);
		// Next #1
		System.out.println("Next sets #1:");
		do {
			List<JsonLite> list = sr.toList();
			i += list.size();
			System.out.println(++page + ":" + sr.getCurrentIndex() + " [" + sr.getViewStartIndex() + ", "
					+ sr.getViewEndIndex() + "] " + " [" + sr.getStartIndex() + ", " + sr.getEndIndex() + "] "
					+ list.size() + " " + i);
		} while (sr.nextSet());

		// Previous #1
		System.out.println();
		System.out.println("Previous sets #1:");
		while (sr.previousSet()) {
			List<JsonLite> list = sr.toList();
			System.out.println(page-- + ":" + sr.getCurrentIndex() + " [" + sr.getStartIndex() + ", "
					+ sr.getEndIndex() + "] " + list.size() + " " + i);
			i -= list.size();
		}

		// Next #2
		System.out.println();
		System.out.println("Next sets #2:");
		do {
			List<JsonLite> list = sr.toList();
			i += list.size();
			System.out.println(++page + ":" + sr.getCurrentIndex() + " [" + sr.getStartIndex() + ", "
					+ sr.getEndIndex() + "] " + list.size() + " " + i);
		} while (sr.nextSet());

		// Next #3
		System.out.println();
		System.out.println("Next sets #3:");
		// total: 32477
		sr.goToSet(31001);
		i = 0;
		page = 0;
		do {
			List<JsonLite> list = sr.toList();
			i += list.size();
			System.out.println(++page + ":" + sr.getCurrentIndex() + " [" + sr.getViewStartIndex() + ", "
					+ sr.getViewEndIndex() + "] " + " [" + sr.getStartIndex() + ", " + sr.getEndIndex() + "] "
					+ list.size() + " " + i);
		} while (sr.nextSet());

		System.out.println();
		System.out.println("Prev sets #2:");
		sr.goToSet(1111);
		i = 0;
		page = 0;
		while (sr.previousSet()) {
			List<JsonLite> list = sr.toList();
			i += list.size();
			System.out.println(++page + ":" + sr.getCurrentIndex() + " [" + sr.getViewStartIndex() + ", "
					+ sr.getViewEndIndex() + "] " + " [" + sr.getStartIndex() + ", " + sr.getEndIndex() + "] "
					+ list.size() + " " + i);
		}

		long delta = System.currentTimeMillis() - startTime;
		sr.close();
		System.out.println();
		System.out.println("              Total: " + i);
		System.out.println("Elapsed time (msec): " + delta);
		System.out.println();
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testGetQueryValues() throws ParseException
	{
		String queryString = "account?";
		// Date validAtDate = formatter.parse("8/26/2011");
		System.out.println("TemporalQueryValueResultSetWebTest.testGetQueryValues()");
		System.out.println("-------------------------------------------------------");
		long startTime = System.currentTimeMillis();
		temporalBiz.setGridPath("account");
		List list = temporalBiz.getQueryValues(queryString, -1, -1);
		long delta = System.currentTimeMillis() - startTime;
		System.out.println("          Retrieved: " + list.size());
		System.out.println("Elapsed time (msec): " + delta);
		System.out.println();
	}
}
