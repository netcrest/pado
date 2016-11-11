/*
 * Copyright (c) 2013-2016 Netcrest Technologies, LLC. All rights reserved.
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
import java.util.Calendar;
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
import com.netcrest.pado.temporal.TemporalEntry;

/**
 * TemporalOrderByTest tests the "ORDER BY" key word.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemporalOrderByTest
{
	private static IPado pado;
	private static ITemporalBiz<String, JsonLite> temporalBiz;

	private String orderByFieldName = "CustomerID";

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
		temporalBiz.setGridPath("customers");
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	private void printResults(List<JsonLite> resultList, String fieldName)
	{
		if (resultList == null) {
			System.out.println("resultList=null");
		} else {
			int i = 0;
			for (JsonLite jl : resultList) {
				System.out.println("[" + i++ + "] " + jl.get(fieldName));
			}
		}
	}

	private void printTemporalEntryResults(List<TemporalEntry<String, JsonLite>> resultList, String fieldName)
	{
		if (resultList == null) {
			System.out.println("resultList=null");
		} else {
			int i = 0;
			for (TemporalEntry<String, JsonLite> te : resultList) {
				System.out.println("[" + i++ + "] " + te.getValue().get(fieldName));
			}
		}
	}

	@Test
	public void testQueryValuesOrderBy() throws ParseException
	{
		System.out.println("TemporalOrderByTest.testQueryValuesOrderBy()");
		System.out.println("--------------------------------------------");
		String queryStatement = "nw/customers?(CustomerID:B* CustomerID:C*) order by " + orderByFieldName + " ASC";
		List<JsonLite> resultList = temporalBiz.getQueryValues(queryStatement, -1, -1);
		System.out.println(queryStatement);
		printResults(resultList, orderByFieldName);
		System.out.println();

		// DESC
		queryStatement = "nw/customers?(CustomerID:B* CustomerID:C*) order by " + orderByFieldName + " DESC";
		resultList = temporalBiz.getQueryValues(queryStatement, System.currentTimeMillis(), System.currentTimeMillis());
		System.out.println(queryStatement);
		printResults(resultList, orderByFieldName);

	}

	@Test
	public void testQueryEntryOrderBy() throws ParseException
	{
		System.out.println("TemporalOrderByTest.testQueryEntryOrderBy()");
		System.out.println("-------------------------------------------");

		// ASC
		String queryStatement = "nw/customers?(CustomerID:B* CustomerID:C*) order by " + orderByFieldName + " ASC";
		List<TemporalEntry<String, JsonLite>> resultList = temporalBiz.getQueryEntries(queryStatement, -1, -1);
		System.out.println(queryStatement);
		printTemporalEntryResults(resultList, orderByFieldName);
		System.out.println();

		// DESC
		queryStatement = "nw/customers?(CustomerID:B* CustomerID:C*) order by " + orderByFieldName + " DESC";
		resultList = temporalBiz.getQueryEntries(queryStatement, -1, -1);
		System.out.println(queryStatement);
		printTemporalEntryResults(resultList, orderByFieldName);
	}
}
