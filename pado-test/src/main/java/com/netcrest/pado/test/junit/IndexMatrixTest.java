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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gemstone.gemfire.cache.query.Struct;
import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IIndexMatrixBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.link.IIndexMatrixBizLink.QueryType;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalEntry;

/**
 * IndexMatrixTest requires the following grid:
 * <ul>
 * <li>mygrid
 * </ul>
 * You will first need to populate the grid with portfolio data by running the
 * following:
 * 
 * <pre>
 * bin_sh/client> ./temporal -all
 * </pre>
 * 
 * <i>Note that this test is interactive.</i>
 * <p>
 * 
 * @author dpark
 * 
 */
@SuppressWarnings("rawtypes")
public class IndexMatrixTest
{

	private static IPado pado;
	private static IIndexMatrixBiz imbiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		// Security 
//		System.setProperty("gemfireSecurityPropertyFile", "etc/client/gfsecurity.properties");
//		System.setProperty("pado.security.aes.user.certificate", "etc/user.cer");
//		System.setProperty("pado.security.enabled", "true");
		
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "test1", "test123".toCharArray());
		ICatalog catalog = pado.getCatalog();
		for (String className : catalog.getAllBizClassNames()) {
			System.out.println(className);
		}
		imbiz = catalog.newInstance(IIndexMatrixBiz.class, true /* initialize */);
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testOQL() throws PadoException, PadoLoginException, IOException
	{
		// Query OQL
		imbiz.setAscending(false);
		imbiz.setFetchSize(100);
		imbiz.setForceRebuildIndex(true);
		imbiz.setQueryType(QueryType.OQL);
		imbiz.setGridIds("mygrid");
		imbiz.setOrderByField("PortfolioId");

		int pageNum = 1;
		int row = 1;
		String input = "";
		IScrollableResultSet rs = null;
		do {
			System.out.println("Page " + pageNum);
			if (input.equals("n")) {
				if (rs.nextSet()) {
					pageNum++;
				}
			} else if (input.equals("p")) {
				if (rs.previousSet()) {
					pageNum--;
				}
			} else {
				rs = imbiz.execute("select * from /mygrid/portfolio");
				Assert.assertNotNull(rs);
			}
			List results = rs.toList();
			Assert.assertNotNull(results);
			int totalSize = rs.getTotalSize();
			System.out.println("   total size = " + totalSize + ", result set size = " + rs.getSetSize());
			row = rs.getStartIndex() + 1;
			for (int i = 0; i < results.size(); i++) {
				Object obj = results.get(i);
				if (obj instanceof ITemporalData) {
					ITemporalData data = (ITemporalData)results.get(i);
					System.out.println("   " + row++ + ". " + data.getValue());
				} else if (obj instanceof Struct) {
					Struct struct = (Struct) obj;
					String[] fieldNames = struct.getStructType().getFieldNames();
					for (int j = 0; j < fieldNames.length; j++) {
						if (j > 0) {
							System.out.print(", ");
						}
						System.out.print(fieldNames[j] + "=" + struct.get(fieldNames[j]));
					}
					System.out.println();
				} else {
					System.out.println("   " + row++ + ". " + obj);
				}
			}

			System.out
					.println("Enter to submit query again, 'n' to next page, 'p' to previous page, or 'q' + Enter to quit: ");
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(System.in));
			input = reader.readLine().trim();
			if (input.length() == 0) {
				pageNum = 1;
				row = 1;
			}
		} while (input.equals("q") == false);

		rs.close();
		System.out.println("Stopped");
	}
	
	@Test
	public void testPQL_OQL() throws PadoException, PadoLoginException, IOException
	{
		// Query OQL
		imbiz.setAscending(true);
		imbiz.setFetchSize(100);
		imbiz.setForceRebuildIndex(true);
		imbiz.setQueryType(QueryType.PQL);
		imbiz.setGridIds("mygrid");
		imbiz.setOrderByField("AccountName");

		int pageNum = 1;
		int row = 1;
		String input = "";
		IScrollableResultSet rs = null;
		do {
			System.out.println("Page " + pageNum);
			if (input.equals("n")) {
				if (rs.nextSet()) {
					pageNum++;
				}
			} else if (input.equals("p")) {
				if (rs.previousSet()) {
					pageNum--;
				}
			} else {
				
				// Execute IndexMatrix
				rs = imbiz.execute("account.AccountId='acct_b'");
				Assert.assertNotNull(rs);
			}
			List results = rs.toList();
			Assert.assertNotNull(results);
			int totalSize = rs.getTotalSize();
			System.out.println("   total size = " + totalSize + ", result set size = " + rs.getSetSize());
			row = rs.getStartIndex() + 1;
			for (int i = 0; i < results.size(); i++) {
				ITemporalData data = (ITemporalData)results.get(i);
				System.out.println("   " + row++ + ". " + data.getValue());
			}

			System.out
					.println("Enter to submit query again, 'n' to next page, 'p' to previous page, or 'q' + Enter to quit: ");
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(System.in));
			input = reader.readLine().trim();
			if (input.length() == 0) {
				pageNum = 1;
				row = 1;
			}
		} while (input.equals("q") == false);

		rs.close();
		System.out.println("Stopped");
	}
	
	@Test
	public void testPQL_OQL2() throws PadoException, PadoLoginException, IOException
	{
		// Query OQL
		imbiz.setAscending(true);
		imbiz.setFetchSize(100);
		imbiz.setForceRebuildIndex(true);
		imbiz.setQueryType(QueryType.PQL);
		imbiz.setGridIds("mygrid");
		imbiz.setOrderByField("PortfolioName");

		int pageNum = 1;
		int row = 1;
		String input = "";
		IScrollableResultSet rs = null;
		do {
			System.out.println("Page " + pageNum);
			if (input.equals("n")) {
				if (rs.nextSet()) {
					pageNum++;
				}
			} else if (input.equals("p")) {
				if (rs.previousSet()) {
					pageNum--;
				}
			} else {
				
				// Execute IndexMatrix
				rs = imbiz.execute("portfolio?");
				Assert.assertNotNull(rs);
			}
			List results = rs.toList();
			Assert.assertNotNull(results);
			int totalSize = rs.getTotalSize();
			System.out.println("   total size = " + totalSize + ", result set size = " + rs.getSetSize());
			row = rs.getStartIndex() + 1;
			for (int i = 0; i < results.size(); i++) {
				ITemporalData data = (ITemporalData)results.get(i);
				System.out.println("   " + row++ + ". " + data.getValue());
			}

			System.out
					.println("Enter to submit query again, 'n' to next page, 'p' to previous page, or 'q' + Enter to quit: ");
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(System.in));
			input = reader.readLine().trim();
			if (input.length() == 0) {
				pageNum = 1;
				row = 1;
			}
		} while (input.equals("q") == false);

		rs.close();
		System.out.println("Stopped");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPQL_Lucene() throws PadoException, PadoLoginException, IOException
	{
		// Query Lucene
		String queryString = "portfolio?PortfolioId:MyPortfolio";
//		String queryString = "portfolio?(PortfolioId:port_ab AND EndWrittenTime:[20121129 TO 20141118])";
//		String queryString = "portfolio?(Username:dpark AND EndWrittenTime:[20121129 TO 20141118])";
//		String queryString = "spare/spare?(InvName:MSTEC AND EndWrittenTime:[20121129 TO 20141118])";
//		String queryString = "spare/spare?(InvName:MSTEC)";

//		String orderBy = "EndWrittenTime";
		String orderBy = "WrittenTime";
//		String orderBy = "StartValidTime";
//		String orderBy = "EndValidTime";
//		String orderBy = "PortfolioId";
//		String orderBy = "AccountId";
//		String orderBy = "Description";
//		String orderBy = "PortfolioName";
		boolean isAscending = true;
		
		boolean isDate = orderBy.equals("WrittenTime") || orderBy.equals("EndWrittenTime") || orderBy.equals("StartValidTime") || orderBy.equals("EndValidTime");
		imbiz.setAscending(isAscending);
		imbiz.setFetchSize(100);
		imbiz.setForceRebuildIndex(true);
		imbiz.setQueryType(QueryType.PQL);
		imbiz.setOrderByField(orderBy);

		int row = 1;
		IScrollableResultSet rs = imbiz.execute(queryString);
		int totalSize = rs.getTotalSize();
		System.out.println("   total size = " + totalSize);
		do {
			List results = rs.toList();
			Assert.assertNotNull(results);
			row = rs.getStartIndex() + 1;
			Object prevItem = null;
			JsonLite prevJl = null;
			for (int i = 0; i < results.size(); i++) {
				TemporalEntry entry = (TemporalEntry)results.get(i);
				ITemporalKey tk = entry.getTemporalKey();
				ITemporalData data = entry.getTemporalData();
				JsonLite jl = (JsonLite)data.getValue();
				Comparable item = (Comparable)tk.getWrittenTime();
				if (orderBy.equals("WrittenTime") || orderBy.equals("EndWrittenTime")) {
					item = (Comparable)tk.getWrittenTime();
				} else if (orderBy.equals("StartValidTime")) {
					item = (Comparable)tk.getStartValidTime();
				} else if (orderBy.equals("EndValidTime")) {
					item = (Comparable)tk.getEndValidTime();
				} else if (orderBy.equals("Username")) {
					item = tk.getUsername();
				} else {
					item = (Comparable)jl.get(orderBy);
				}
				if (item == null) {
					System.err.println(orderBy + " field not found.");
					Assert.fail();
				}
				if (prevItem != null) {
					if (isAscending) {
						if (item.compareTo(prevItem) < 0) {
							printItems(row, jl, prevJl, item, prevItem, orderBy, isAscending, isDate);
//							Assert.fail();
						}
					} else {
						if (item.compareTo(prevItem) > 0) {
							printItems(row, jl, prevJl, item, prevItem, orderBy, isAscending, isDate);
//							Assert.fail();
						}
					}
				}
				row++;
				prevItem = item;
				prevJl = jl;
			}
		} while (rs.nextSet());
		rs.close();
		System.out.println("Complete");
	}
	
	private void printItems(int row, JsonLite jl, JsonLite prevJl, Object item, Object prevItem, String orderBy, boolean isAscending, boolean isDate)
	{
		System.err.println("Order: ascending=" + isAscending + ", order-by=" + orderBy);
		if (isDate) {
			System.err.println("   " + (row-1) + ". Prev Item: " + new Date((Long)prevItem) + ", " + prevJl);
			System.err.println("   " + row + ". Invalid Order: " + new Date((Long)item) + ", " + jl);
		} else {
			System.err.println("   " + (row-1) + ". Prev Item: " + prevItem + ", " + prevJl);
			System.err.println("   " + row + ". Invalid Order: " + item + ", " + jl);
		}
		System.err.println();
	}
	
	@Test
	public void testPQL_Lucene_Interactive() throws PadoException, PadoLoginException, IOException
	{
		// Query Lucene
//		String queryString = "portfolio?(PortfolioId:port_a AND EndWrittenTime:[20121129 TO 20141118])";
		String queryString = "portfolio?(Username:test1 AND EndWrittenTime:[20121129 TO 20141118])";

		imbiz.setAscending(false);
		imbiz.setFetchSize(100);
		imbiz.setForceRebuildIndex(true);
		imbiz.setQueryType(QueryType.PQL);
		imbiz.setOrderByField("EndWrittenTime");

		int pageNum = 1;
		int row = 1;
		String input = "";
		IScrollableResultSet rs = null;
		do {
			System.out.println("Page " + pageNum);
			if (input.equals("n")) {
				if (rs.nextSet()) {
					pageNum++;
				}
			} else if (input.equals("p")) {
				if (rs.previousSet()) {
					pageNum--;
				}
			} else {
				// Execute query
				rs = imbiz.execute(queryString);
				Assert.assertNotNull(rs);
			}
			List results = rs.toList();
			Assert.assertNotNull(results);
			int totalSize = rs.getTotalSize();
			System.out.println("   total size = " + totalSize + ", result set size = " + rs.getSetSize());
			row = rs.getStartIndex() + 1;
			for (int i = 0; i < results.size(); i++) {
				TemporalEntry entry = (TemporalEntry)results.get(i);
				ITemporalKey tk = entry.getTemporalKey();
				ITemporalData data = entry.getTemporalData();
				Date writtenTimeDate = new Date(tk.getWrittenTime());
				JsonLite jl = (JsonLite)data.getValue();
				System.out.println("   " + row++ + ". " + writtenTimeDate + ", " + jl);
			}

			System.out
					.println("Enter to submit query again, 'n' to next page, 'p' to previous page, or 'q' + Enter to quit: ");
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(System.in));
			input = reader.readLine().trim();
			if (input.length() == 0) {
				pageNum = 1;
				row = 1;
			}
		} while (input.equals("q") == false);

		rs.close();
		System.out.println("Stopped");
	}
}
