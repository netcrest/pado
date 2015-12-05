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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.test.TemporalLoader;
import com.netcrest.pado.temporal.test.data.Account;
import com.netcrest.pado.temporal.test.data.Portfolio;

/**
 * TemporalReferenceGetTest requires "mygrid". Note that this test runs
 * interactively.
 * 
 * @author dpark
 * 
 */
public class TemporalReferenceGetTest
{
	private static IPado pado;
	@SuppressWarnings("rawtypes")
	private static ITemporalBiz<String, KeyMap> temporalBiz;
	@SuppressWarnings("rawtypes")
	private static ITemporalBiz<String, KeyMap> portfolioBiz;
	private static TemporalLoader loader;
	@SuppressWarnings("rawtypes")
	private static Set<TemporalEntry<String, KeyMap>> temporalEntrySet;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		portfolioBiz = catalog.newInstance(ITemporalBiz.class, "portfolio");
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@SuppressWarnings({ "rawtypes" })
	@Test
	public void testPortfolioReferenceGet() throws ParseException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		portfolioBiz.setReference(true);
		Date validAtDate = new Date();
		// Date validAtDate = formatter.parse("8/26/2011");
		System.out.println("TemporalGetTest.testPortfolioReferenceGet()");
		System.out.println("-------------------------------------------");
		Date asOfDate = new Date();
		long startTime = System.currentTimeMillis();
		KeyMap value = portfolioBiz.get("a", validAtDate.getTime());
		long delta = System.currentTimeMillis() - startTime;
		JsonLite jl = (JsonLite) value;
		System.out.println(jl.toString(2, true, false));
		System.out.println();
		System.out.println("Elapsed time (msec): " + delta);
		System.out.println();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	// @Test
	public void testPortfolioReferenceGet2()
	{
		System.out.println("TemporalGetTest.testPortfolioReferenceGet2()");
		System.out.println("-------------------------------------------");
		portfolioBiz.setReference(true);
		long startTime = System.currentTimeMillis();
		KeyMap value = portfolioBiz.get("ab");
		long delta = System.currentTimeMillis() - startTime;
		JsonLite jl = (JsonLite) value;
		System.out.println("Portfolio:");
		System.out.println(jl.toString(2, false, false));
		System.out.println("Account:");
		JsonLite account = (JsonLite) jl.getReference(Portfolio.KAccountId);
		System.out.println(account.toString(2, false, false));
		System.out.println();
		System.out.println("Bank:");
		JsonLite bank = (JsonLite) account.getReference(Account.KBankId);
		if (bank == null) {
			System.out.println("null");
		} else {
			System.out.println(bank.toString(2, false, false));
		}
		System.out.println();
		List<JsonLite> positions = (List<JsonLite>) jl.getReference(Portfolio.KPositions);
		System.out.println("Positions:");
		if (positions == null) {
			System.out.println("null");
		} else {
			int index = 1;
			for (JsonLite jsonLite : positions) {
				System.out.println(index + ". Position:");
				System.out.println(jsonLite.toString(2, false, false));
				JsonLite jl3 = (JsonLite) jsonLite.getReference(Portfolio.KAccountId);
				System.out.println(index + ". Position Account:");
				index++;
				if (jl3 == null) {
					System.out.println("null");
				} else {
					System.out.println(jl3.toString(2, false, false));
				}
				System.out.println();
			}
		}
		System.out.println("Elapsed time (msec): " + delta);
		System.out.println();
	}

}
