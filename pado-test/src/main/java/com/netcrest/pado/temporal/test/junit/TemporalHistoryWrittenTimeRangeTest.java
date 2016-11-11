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
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.test.gemfire.PortfolioKey;

/**
 * TemporalHistoryWrittenTimeRangeTest requires "mygrid". Note that this test
 * runs interactively.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemporalHistoryWrittenTimeRangeTest
{
	private static IPado pado;
	private static ITemporalBiz<String, JsonLite> temporalBiz;
	private static ITemporalBiz<PortfolioKey, JsonLite> portfolioTemporalBiz;

	private static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

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
		temporalBiz.setGridPath("portfolio");
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
		portfolioTemporalBiz = catalog.newInstance(ITemporalBiz.class);
		portfolioTemporalBiz.setGridPath("portfolio");
		portfolioTemporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");

		putData();

	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	private static void putData() throws ParseException
	{
		temporalBiz.setGridPath("temporal");
		String startValidTimeStr = "01/01/2016";
		String endValidTimeStr = "07/31/2016";
		String writtenTimeStr = startValidTimeStr;
		long startValidTime = formatter.parse(startValidTimeStr).getTime();
		long endValidTime = formatter.parse(endValidTimeStr).getTime();
		long writtenTime = startValidTime;

		JsonLite jl = new JsonLite();
		jl.put("Name", "Test");
		jl.put("Description", "Desc 1");
		jl.put("StartValidTime", startValidTimeStr);
		jl.put("EndValidTime", endValidTimeStr);
		jl.put("WrittenTime", writtenTimeStr);
		temporalBiz.put("Test", jl, startValidTime, endValidTime, writtenTime, false);

		writtenTimeStr = "04/15/2016";
		jl.put("Description", "Desc 2");
		jl.put("WrittenTime", writtenTimeStr);
		writtenTime = formatter.parse(writtenTimeStr).getTime();
		temporalBiz.put("Test", jl, startValidTime, endValidTime, writtenTime, false);

		writtenTimeStr = "08/01/2016";
		jl.put("Description", "Desc 3");
		jl.put("WrittenTime", writtenTimeStr);
		writtenTime = formatter.parse(writtenTimeStr).getTime();
		temporalBiz.put("Test", jl, startValidTime, endValidTime, writtenTime, false);
	}

	private void searchHistoryValueWrittenTimeRange(String queryString, String validAtStr, String fromWrittenTimeStr,
			String toWrittenTimeStr, boolean isPrintResults) throws ParseException
	{
		temporalBiz.setReference(false);
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
		temporalBiz.setGridPath("temporal");
		long validAt = formatter.parse(validAtStr).getTime();
		long fromWrittenTime = formatter.parse(fromWrittenTimeStr).getTime();
		long toWrittenTime = formatter.parse(toWrittenTimeStr).getTime();
		long startTime = System.currentTimeMillis();
		List<JsonLite> list = temporalBiz.getValueHistoryWrittenTimeRangeList(queryString, validAt, fromWrittenTime,
				toWrittenTime);
		long delta = System.currentTimeMillis() - startTime;
		int size = 0;
		if (list != null) {
			size = list.size();
			if (isPrintResults) {
				int i = 0;
				for (JsonLite jl : list) {
					System.out.println(++i + ". " + jl.toString(2, true, false));
				}
			}
		}
		System.out.println("Total result set count: " + size);
		System.out.println("   Elapsed time (msec): " + delta);
		System.out.println();
	}

	@Test
	public void testLuceneValueHistoryWrittenTimeRange() throws ParseException
	{
		String queryString = "temporal?";
		String validAtStr = "01/01/2016";

		System.out.println("TemporalReferenceGetEntryResultSetTest.testLuceneQueryValueResultSetWrittenTimeRange()");
		System.out.println("--------------------------------------------------------------------------------------");
		searchHistoryValueWrittenTimeRange(queryString, validAtStr, "1/1/2016", "5/1/2017", true);
	}

	// @Test
	public void testOqlQueryValueResultSetWrittenTimeRange() throws ParseException
	{
		String queryString = "select distinct e.key.IdentityKey from /mygrid/portfolio.entrySet e where e.value.value['PortfolioId']='MyPortfolio'";
		// String queryString =
		// "select distinct e.key.IdentityKey from /mygrid/portfolio.entrySet e
		// where e.value.value['PortfolioId']='MyPortfolio'";
		String validAtStr = formatter.format(new Date());

		System.out.println("TemporalReferenceGetEntryResultSetTest.testLuceneQueryValueResultSetWrittenTimeRange()");
		System.out.println("--------------------------------------------------------------------------------------");
		searchHistoryValueWrittenTimeRange(queryString, validAtStr, "1/1/2014", "1/1/2017", false);
	}

	private void searchHistoryEntryWrittenTimeRange(String queryString, String validAtStr, String fromWrittenTimeStr,
			String toWrittenTimeStr, boolean isPrintResults) throws ParseException
	{
		temporalBiz.setReference(false);
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
		temporalBiz.setGridPath("temporal");
		long validAt = formatter.parse(validAtStr).getTime();
		long fromWrittenTime = formatter.parse(fromWrittenTimeStr).getTime();
		long toWrittenTime = formatter.parse(toWrittenTimeStr).getTime();
		long startTime = System.currentTimeMillis();
		List<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> list = temporalBiz.getEntryHistoryWrittenTimeRangeList(queryString, validAt, fromWrittenTime,
				toWrittenTime);
		long delta = System.currentTimeMillis() - startTime;
		int size = 0;
		if (list != null) {
			size = list.size();
			if (isPrintResults) {
				int i = 0;
				for (TemporalEntry<ITemporalKey<String>, ITemporalData<String>> te : list) {
					ITemporalKey tk = te.getTemporalKey();
					JsonLite jl = (JsonLite)te.getTemporalData().getValue();
					System.out.println(++i + ". " + tk);
					System.out.println(i + ". " + jl.toString(2, true, false));
				}
			}
		}
		System.out.println("Total result set count: " + size);
		System.out.println("   Elapsed time (msec): " + delta);
		System.out.println();
	}

	@Test
	public void testLuceneEntryHistoryWrittenTimeRange() throws ParseException
	{
		String queryString = "temporal?";
		String validAtStr = "01/01/2016";

		System.out.println("TemporalReferenceGetEntryResultSetTest.testLuceneQueryValueResultSetWrittenTimeRange()");
		System.out.println("--------------------------------------------------------------------------------------");
		searchHistoryEntryWrittenTimeRange(queryString, validAtStr, "1/1/2016", "5/1/2017", true);
	}

}
