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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.test.data.Account;
import com.netcrest.pado.temporal.test.data.Portfolio;
import com.netcrest.pado.temporal.test.gemfire.PortfolioKey;

import junit.framework.Assert;

/**
 * TemporalReferenceGetEntryQueryResultSetTest requires "mygrid". Note that this
 * test runs interactively.
 * 
 * @author dpark
 * 
 */
public class TemporalReferenceGetEntryQueryResultSetTest
{
	private static IPado pado;
	@SuppressWarnings("rawtypes")
	private static ITemporalBiz<String, JsonLite> temporalBiz;
	@SuppressWarnings("rawtypes")
	private static ITemporalBiz<PortfolioKey, JsonLite> portfolioTemporalBiz;

	private static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

	@SuppressWarnings({ "unchecked" })
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

		// // Insert data
		// com.netcrest.pado.temporal.test.data.domain.Portfolio portfolio = new
		// com.netcrest.pado.temporal.test.data.domain.Portfolio();
		// portfolio.setAccountId("acct_test");
		// portfolio.setPortfolioName("Portfolio Test");
		// portfolio.setDescription("test1");
		// portfolio.setPortfolioId(PORTFOLIO_ID);
		// for (int i = 1; i <= 10; i++) {
		// portfolio.setDescription("test" + i);
		// String fromValidTimeStr = "08/01/2015";
		// if (i > 3) {
		// fromValidTimeStr = "08/03/2015";
		// } else if (i > 6) {
		// fromValidTimeStr = "08/05/2015";
		// }
		// String writtenDateStr = "08/0" + i + "/2015";
		// portfolioTemporalBiz.put(new PortfolioKey(portfolio.getPortfolioId(),
		// portfolio.getAccountId()), portfolio.toJsonLite(),
		// formatter.parse(fromValidTimeStr)
		// .getTime(), TemporalUtil.MAX_TIME,
		// formatter.parse(writtenDateStr).getTime(), false);
		//
		// }
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

//	@Test
	public void testQueryEntryResultSetWrittenTimeRange1() throws ParseException
	{
		// Now-relative search - range today's date. No record.
		Assert.assertEquals(0, searchWrittenTimeRange(null, null, null, null));
		Assert.assertEquals(0, searchWrittenTimeRange("08/01/2015", null, null, null));
		// Range is [8/1, today).
		Assert.assertEquals(1, searchWrittenTimeRange("08/01/2015", "08/01/2015", null, "test3"));

		Assert.assertEquals(1, searchWrittenTimeRange("08/01/2015", "08/01/2015", "08/10/2015", "test3"));
		Assert.assertEquals(1, searchWrittenTimeRange("08/01/2015", "08/02/2015", "08/10/2015", "test3"));
		Assert.assertEquals(1, searchWrittenTimeRange("08/03/2015", "08/01/2015", "08/31/2099", "test10"));
		Assert.assertEquals(0, searchWrittenTimeRange("08/01/2015", "08/10/2015", "08/10/2015", null));
	}

	@SuppressWarnings("rawtypes")
	private int searchWrittenTimeRange(String validAtStr, String fromWrittenTimeStr, String toWrittenTimeStr,
			String expectedDesc) throws ParseException
	{
		// If no attributed query (i.e., no predicate after the question mark),
		// then all of the identity keys are searched.
		String queryString = "portfolio?";
		String orderBy = "PortfolioName";
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

	// @SuppressWarnings("rawtypes")
	// @Test
	@SuppressWarnings("rawtypes")
	public void testQueryEntryResultSetWrittenTimeRange() throws ParseException
	{
		String queryString = "portfolio?";
		String orderBy = "PortfolioName";
		boolean orderAcending = true;
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		System.out.println("TemporalReferenceGetEntryResultSetTest.testQueryEntryResultSetWrittenTimeRange()");
		System.out.println("-------------------------------------------------------------------------------");
		temporalBiz.setReference(false);
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
		String validAtStr = "08/10/2015";
		String fromWrittenTimeStr = "08/01/2015";
		String toWrittenTimeStr = "08/12/2015";
		long validAt = formatter.parse(validAtStr).getTime();
		long fromWrittenTime = formatter.parse(fromWrittenTimeStr).getTime();
		long toWrittenTime = formatter.parse(toWrittenTimeStr).getTime();
		long startTime = System.currentTimeMillis();
		IScrollableResultSet<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> sr = temporalBiz
				.getEntryWrittenTimeRangeResultSet(queryString, validAt, fromWrittenTime, toWrittenTime, orderBy,
						orderAcending, 100, true);
		long delta = System.currentTimeMillis() - startTime;

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

		System.out.println("Elapsed time (msec): " + delta);
		System.out.println();
	}

	// @Test
	@SuppressWarnings("rawtypes")
	public void testQueryValueResultSetWrittenTimeRange() throws ParseException
	{
		String queryString = "portfolio?";
		String orderBy = "PortfolioName";
		boolean orderAcending = true;
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		System.out.println("TemporalReferenceGetEntryResultSetTest.testQueryValueResultSetWrittenTimeRange()");
		System.out.println("--------------------------------------------------------------------------------");
		temporalBiz.setReference(false);
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
		long validAt = formatter.parse("8/26/2011").getTime();
		long fromWrittenTime = formatter.parse("8/20/2011").getTime();
		long toWrittenTime = formatter.parse("8/26/2011").getTime();
		long startTime = System.currentTimeMillis();
		IScrollableResultSet<JsonLite> sr = temporalBiz.getValueWrittenTimeRangeResultSet(queryString, validAt,
				fromWrittenTime, toWrittenTime, orderBy, orderAcending, 100, true);
		long delta = System.currentTimeMillis() - startTime;

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

		System.out.println("Elapsed time (msec): " + delta);
		System.out.println();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testQueryEntryResultSet() throws ParseException
	{
//		String queryString = "account?a*";
		String queryString = "select e.key.IdentityKey from /mygrid/account.entrySet e";// order by e.value.value['FirstName']";
		String orderBy = "FirstName";
		boolean orderAcending = true;
		// Date validAtDate = formatter.parse("8/26/2011");
		System.out.println("TemporalReferenceGetEntryResultSetTest.testQueryEntryResultSet()");
		System.out.println("----------------------------------------------------------------");
		temporalBiz.setReference(false);
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
		long startTime = System.currentTimeMillis();
		IScrollableResultSet<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> sr = temporalBiz
				.getEntryResultSet(queryString, orderBy, orderAcending, 100, true);
		long delta = System.currentTimeMillis() - startTime;

		HashSet<Object> identityKeySet = new HashSet<Object>();
		int page = 0;
		do {
			System.out.println("Set " + ++page);
			System.out.println("-------------------------");
			List<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> list = sr.toList();
			System.out.println(list.size());
			for (TemporalEntry<ITemporalKey<String>, ITemporalData<String>> entry : list) {
				Object ik = entry.getTemporalKey().getIdentityKey();
				if (identityKeySet.contains(ik)) {
					Assert.fail("Duplciate identity key: " + ik);
				}
				identityKeySet.add(ik);
			}
		} while (sr.nextSet());
		sr.close();

		temporalBiz = pado.getCatalog().newInstance(ITemporalBiz.class);
		temporalBiz.setReference(false);
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
		sr = temporalBiz.getEntryResultSet(queryString, orderBy, orderAcending, 100, true);
		delta = System.currentTimeMillis() - startTime;

		page = 0;
		int i = 0;
		do {
			System.out.println("Set " + ++page);
			System.out.println("-------------------------");
			List<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> list = sr.toList();
			System.out.println(list.size());
			for (TemporalEntry<ITemporalKey<String>, ITemporalData<String>> entry : list) {
				JsonLite jl = (JsonLite) entry.getValue();
//				System.out.println(++i + ". " + jl.toString(2, true, false));
				System.out.println(++i + ". " + jl.get("FirstName"));
			}
			System.out.println();
		} while (sr.nextSet());
		sr.close();

		System.out.println("Elapsed time (msec): " + delta);
		System.out.println();
	}

	// @Test
	public void testQueryEntryResultSetThreaded() throws ParseException, InterruptedException, ExecutionException
	{
		// Date validAtDate = formatter.parse("8/26/2011");
		System.out.println("TemporalReferenceGetEntryResultSetTest.testQueryEntryResultSetThreaded()");
		System.out.println("------------------------------------------------------------------------");

		int threadCount = 10;
		ExecutorService es = Executors.newFixedThreadPool(threadCount);
		List<TemporalBizCallable> list = new ArrayList<TemporalBizCallable>();
		for (int i = 0; i < threadCount; i++) {
			list.add(new TemporalBizCallable());
		}
		long startTime = System.currentTimeMillis();
		List<Future<Boolean>> futureList = es.invokeAll(list);
		for (Future<Boolean> future : futureList) {
			future.get();
		}
		long delta = System.currentTimeMillis() - startTime;
		System.out.println("Elapsed time (msec): " + delta);
		System.out.println();
	}

	class TemporalBizCallable implements Callable<Boolean>
	{
		String queryString = "portfolio?b*";
		String orderBy = null;
		boolean orderAcending = true;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Boolean call() throws Exception
		{
			ITemporalBiz temporalBiz = pado.getCatalog().newInstance(ITemporalBiz.class);
			temporalBiz.setReference(false);
			temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
			IScrollableResultSet<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> sr = temporalBiz
					.getEntryResultSet(queryString, orderBy, orderAcending, 100, false);

			int page = 0;
			List<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> list;
			do {
				System.out.println("Set " + ++page);
				System.out.println("-------------------------");
				list = sr.toList();
				System.out.println(list.size());
			} while (sr.nextSet());
			sr.close();
			return true;
		}
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testQueryValueResultSet() throws ParseException
	{
		String queryString = "account?acct_a*";
		String orderBy = "AccountId";
		boolean orderAscending = true;
		// Date validAtDate = formatter.parse("8/26/2011");
		System.out.println("TemporalReferenceGetEntryResultSetTest.testQueryValueResultSet()");
		System.out.println("---------------------------------------------------------------");
		temporalBiz.setReference(true);
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
		long startTime = System.currentTimeMillis();
		IScrollableResultSet<JsonLite> sr = temporalBiz.getValueResultSet(queryString, orderBy, orderAscending, 100,
				true);
		long delta = System.currentTimeMillis() - startTime;

		HashSet<String> identityKeySet = new HashSet<String>();
		int page = 0;
		int i = 0;
		do {
			System.out.println("Set " + ++page);
			System.out.println("-------------------------");
			List<JsonLite> list = sr.toList();
			for (JsonLite jl : list) {
				String ik = (String)jl.get(Account.KAccountId);
				if (identityKeySet.contains(ik)) {
					Assert.fail("Duplciate identity key: " + ik);
				}
				identityKeySet.add(ik);
				 System.out.println(++i + ". " + jl.toString(2, true, false));
			}
			System.out.println();
		} while (sr.nextSet());
		sr.close();
		System.out.println("Elapsed time (msec): " + delta);
		System.out.println();
	}

	// @Test
	@SuppressWarnings("rawtypes")
	public void testQueryValueResultSet2() throws ParseException
	{
		String queryString = "portfolio?port_a*";
		String orderBy = "PortfolioName";
		// Date validAtDate = formatter.parse("8/26/2011");
		Date validAtDate = new Date();
		Date asOfDate = new Date();
		System.out.println("TemporalReferenceGetEntryResultSetTest.testQueryValueResultSet2()");
		System.out.println("----------------------------------------------------------------");
		temporalBiz.setReference(true);
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
		long startTime = System.currentTimeMillis();
		List<JsonLite> list = temporalBiz.getQueryValues(queryString, validAtDate.getTime(), asOfDate.getTime());
		long delta = System.currentTimeMillis() - startTime;
		int i = 0;
		for (JsonLite jl : list) {
			// System.out.println(++i + ". " + jl.toString(2, true, false));
			System.out.println(++i + ". " + jl.get(orderBy));
		}
		System.out.println("Elapsed time (msec): " + delta);
		System.out.println();
	}
}
