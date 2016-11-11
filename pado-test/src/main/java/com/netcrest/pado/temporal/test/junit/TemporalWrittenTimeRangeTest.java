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
import java.util.HashSet;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.test.data.Portfolio;
import com.netcrest.pado.temporal.test.gemfire.PortfolioKey;

/**
 * TemporalWrittenTimeRangeTest requires "mygrid". Note that this test runs
 * interactively.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemporalWrittenTimeRangeTest
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

	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	private static long getTime(String dateStr) throws ParseException
	{
		if (dateStr == null) {
			return -1;
		}
		return formatter.parse(dateStr).getTime();
	}

	// @Test
	public void testQueryEntryResultSetWrittenTimeRange1() throws ParseException
	{
		// Now-relative search - range today's date. No record.
		Assert.assertEquals(0, searchWrittenTimeRange("portfolio?", null, null, null, null));
		Assert.assertEquals(0, searchWrittenTimeRange("portfolio?", "08/01/2015", null, null, null));
		// Range is [8/1, today).
		Assert.assertEquals(1, searchWrittenTimeRange("portfolio?", "08/01/2015", "08/01/2015", null, "test3"));

		Assert.assertEquals(1, searchWrittenTimeRange("portfolio?", "08/01/2015", "08/01/2015", "08/10/2015", "test3"));
		Assert.assertEquals(1, searchWrittenTimeRange("portfolio?", "08/01/2015", "08/02/2015", "08/10/2015", "test3"));
		Assert.assertEquals(1, searchWrittenTimeRange("portfolio?", "08/03/2015", "08/01/2015", "08/31/2099", "test10"));
		Assert.assertEquals(0, searchWrittenTimeRange("portfolio?", "08/01/2015", "08/10/2015", "08/10/2015", null));
	}

	private int searchWrittenTimeRange(String queryString, String validAtStr, String fromWrittenTimeStr,
			String toWrittenTimeStr, String expectedDesc) throws ParseException
	{
		// If no attributed query (i.e., no predicate after the question mark),
		// then all of the identity keys are searched.
		String orderBy = null;
		boolean orderAcending = true;

		long validAt = getTime(validAtStr);
		long fromWrittenTime = getTime(fromWrittenTimeStr);
		long toWrittenTime = getTime(toWrittenTimeStr);
		IScrollableResultSet<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> sr = temporalBiz
				.getEntryWrittenTimeRangeResultSet(queryString, validAt, fromWrittenTime, toWrittenTime, orderBy,
						orderAcending, 100, true);

		if (validAt == -1) {
			validAt = System.currentTimeMillis();
		}
		if (fromWrittenTime == -1) {
			fromWrittenTime = System.currentTimeMillis();
		}
		if (toWrittenTime == -1) {
			toWrittenTime = System.currentTimeMillis();
		}
		do {
			List<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> list = sr.toList();
			for (TemporalEntry<ITemporalKey<String>, ITemporalData<String>> entry : list) {
				ITemporalKey tk = entry.getTemporalKey();
				JsonLite jl = (JsonLite) entry.getValue();
				boolean isRangeValid = tk.getStartValidTime() <= validAt && validAt < tk.getEndValidTime()
						&& fromWrittenTime <= tk.getWrittenTime() && tk.getWrittenTime() < toWrittenTime;
				boolean isValidValue = expectedDesc.equals(jl.get(Portfolio.KDescription));
				Assert.assertTrue(isRangeValid);
				Assert.assertTrue(isValidValue);
			}
		} while (sr.nextSet());
		sr.close();
		return sr.getTotalSize();
	}

	private void searchEntryResultSet(String queryString, String validAtStr, String fromWrittenTimeStr,
			String toWrittenTimeStr, boolean isPrintResults) throws ParseException
	{
		boolean orderAcending = true;
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		temporalBiz.setReference(false);
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
		long validAt = formatter.parse(validAtStr).getTime();
		long fromWrittenTime = formatter.parse(fromWrittenTimeStr).getTime();
		long toWrittenTime = formatter.parse(toWrittenTimeStr).getTime();
		long startTime = System.currentTimeMillis();
		IScrollableResultSet<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> sr = temporalBiz
				.getEntryWrittenTimeRangeResultSet(queryString, validAt, fromWrittenTime, toWrittenTime, null,
						orderAcending, 100, true);
		long delta = System.currentTimeMillis() - startTime;

		if (isPrintResults) {
			int page = 0;
			int i = 0;
			System.out.println("          ValidAt: " + validAtStr);
			System.out.println("WrittenTime Range: [" + fromWrittenTimeStr + ", " + toWrittenTimeStr + ")");
			do {
				System.out.println("Set " + ++page);
				System.out.println("-------------------------");
				List<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> list = sr.toList();
				System.out.println(list.size());
				for (TemporalEntry<ITemporalKey<String>, ITemporalData<String>> entry : list) {
					JsonLite jl = (JsonLite) entry.getValue();
					System.out.println(entry.getTemporalKey().toStringDate());
					System.out.println(++i + ". " + jl.toString(2, true, false));
				}
				System.out.println();
			} while (sr.nextSet());
			sr.close();
		} else {
			HashSet<Object> identityKeySet = new HashSet<Object>();
			do {
				List<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> list = sr.toList();
				for (TemporalEntry<ITemporalKey<String>, ITemporalData<String>> entry : list) {
					Object ik = entry.getTemporalKey().getIdentityKey();
					if (identityKeySet.contains(ik)) {
						Assert.fail("Duplciate identity key: " + ik);
					}
					identityKeySet.add(ik);
				}
			} while (sr.nextSet());
			sr.close();
		}
		System.out.println("Total result set count: " + sr.getTotalSize());
		System.out.println("   Elapsed time (msec): " + delta);
		System.out.println();
	}

	@Test
	public void testLuceneQueryEntryResultSetWrittenTimeRangeIterate() throws Exception
	{
		int numIterations = 100;
		int pauseTimePeriod = 100;
		String queryString = "portfolio?";
		String validAtStr = formatter.format(new Date());

		System.out
				.println("TemporalReferenceGetEntryResultSetTest.testLuceneQueryEntryResultSetWrittenTimeRangeIterate()");
		System.out
				.println("---------------------------------------------------------------------------------------------");
		System.out.println("                Iteration count: " + numIterations);
		System.out.println("Pause between iterations (msec): " + pauseTimePeriod);
		for (int i = 1; i <= numIterations; i++) {
			System.out.println("             Iteration: " + i);
			searchEntryResultSet(queryString, validAtStr, "1/1/2014", "1/1/2017", false);
			Thread.sleep(100);
		}
		System.out.println("Done!");
	}

	// @Test
	public void testLuceneQueryEntryResultSetWrittenTimeRange() throws ParseException
	{
		String queryString = "portfolio?";
		String validAtStr = formatter.format(new Date());

		System.out.println("TemporalReferenceGetEntryResultSetTest.testLuceneQueryEntryResultSetWrittenTimeRange()");
		System.out.println("-------------------------------------------------------------------------------------");
		searchEntryResultSet(queryString, validAtStr, "1/1/2014", "1/1/2017", false);
	}

	private void searchValueResultSet(String queryString, String validAtStr, String fromWrittenTimeStr,
			String toWrittenTimeStr, boolean isPrintResults) throws ParseException
	{
		String orderBy = null;
		boolean orderAcending = true;
		temporalBiz.setReference(false);
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
		temporalBiz.setGridPath("portfolio");
		long validAt = formatter.parse(validAtStr).getTime();
		long fromWrittenTime = formatter.parse(fromWrittenTimeStr).getTime();
		long toWrittenTime = formatter.parse(toWrittenTimeStr).getTime();
		long startTime = System.currentTimeMillis();
		IScrollableResultSet<JsonLite> sr = temporalBiz.getValueWrittenTimeRangeResultSet(queryString, validAt,
				fromWrittenTime, toWrittenTime, orderBy, orderAcending, 100, true);
		long delta = System.currentTimeMillis() - startTime;

		if (isPrintResults) {
			int page = 0;
			int i = 0;
			do {
				System.out.println("Set " + ++page);
				System.out.println("-------------------------");
				List<JsonLite> list = sr.toList();
				System.out.println(list.size());
				for (JsonLite jl : list) {
					System.out.println(++i + ". " + jl.toString(2, true, false));
				}
				System.out.println();
			} while (sr.nextSet());
			sr.close();
		}
		System.out.println("Total result set count: " + sr.getTotalSize());
		System.out.println("   Elapsed time (msec): " + delta);
		System.out.println();
	}

	// @Test
	public void testLuceneQueryValueResultSetWrittenTimeRange() throws ParseException
	{
		String queryString = "portfolio?";
		String validAtStr = formatter.format(new Date());

		System.out.println("TemporalReferenceGetEntryResultSetTest.testLuceneQueryValueResultSetWrittenTimeRange()");
		System.out.println("--------------------------------------------------------------------------------------");
		searchValueResultSet(queryString, validAtStr, "1/1/2014", "1/1/2017", false);
	}

	// @Test
	public void testOqlQueryValueResultSetWrittenTimeRange() throws ParseException
	{
		String queryString = "select distinct e.key.IdentityKey from /mygrid/portfolio.entrySet e where e.value.value['PortfolioId']='MyPortfolio'";
		// String queryString =
		// "select distinct e.key.IdentityKey from /mygrid/portfolio.entrySet e where e.value.value['PortfolioId']='MyPortfolio'";
		String validAtStr = formatter.format(new Date());

		System.out.println("TemporalReferenceGetEntryResultSetTest.testLuceneQueryValueResultSetWrittenTimeRange()");
		System.out.println("--------------------------------------------------------------------------------------");
		searchValueResultSet(queryString, validAtStr, "1/1/2014", "1/1/2017", false);
	}
}
