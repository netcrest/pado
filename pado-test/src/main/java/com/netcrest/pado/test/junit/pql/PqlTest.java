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
package com.netcrest.pado.test.junit.pql;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Test;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;
import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.IGridMapBiz;
import com.netcrest.pado.biz.IPqlBiz;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.pql.CompiledUnit;
import com.netcrest.pado.temporal.test.TemporalLoader;
import com.netcrest.pado.test.junit.pql.gemfire.Customer;
import com.netcrest.pado.test.junit.pql.gemfire.Order;
import com.netcrest.pado.util.GridUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PqlTest
{
	static TemporalLoader loader;

	static IPado pado;
	static IGridMapBiz gridMapBiz;
	static ITemporalBiz temporalBiz;
	static IPqlBiz pqlBiz;
	final static String GRID_PATH = "porfolio";

	private static final Random RANDOM = new Random();

	private static final int NUM_CUSTOMERS = 1000;

	private static final int NUM_ORDERS_PER_CUSTOMER = 10;

	// @BeforeClass
	public static void loginPado() throws PadoLoginException, Exception
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "test1", "test123".toCharArray());
		ICatalog catalog = pado.getCatalog();
		gridMapBiz = catalog.newInstance(IGridMapBiz.class);
		temporalBiz = catalog.newInstance(ITemporalBiz.class, GRID_PATH);
		pqlBiz = catalog.newInstance(IPqlBiz.class, "portfolio");

		// Set up data loader
		loader = new TemporalLoader();
		loader.addBankId("bank_a");
		loader.addBankId("bank_ab");

		// loadEntries(NUM_CUSTOMERS);
	}

	private static void loadEntries(int numEntries) throws Exception
	{
		IGridMapBiz customerGridMapBiz = pado.getCatalog().newInstance(IGridMapBiz.class, "customer");
		IGridMapBiz orderGridMapBiz = pado.getCatalog().newInstance(IGridMapBiz.class, "order");

		long start = 0, end = 0;
		start = System.currentTimeMillis();
		for (int i = 0; i < numEntries; i++) {
			String customerId = String.valueOf(i);
			customerGridMapBiz.put(customerId, new Customer(customerId, "name-" + customerId));
			for (int j = 0; j < NUM_ORDERS_PER_CUSTOMER; j++) {
				String orderId = customerId + "-" + String.valueOf(j);
				orderGridMapBiz.put(orderId, new Order(orderId, customerId));
			}
		}
		end = System.currentTimeMillis();
		System.out.println("Loaded " + numEntries + " customers in " + (end - start) + " ms");
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void tetCompiledUnit()
	{
		// String oql = "portfolio.SecId='pos_e.hello' x.y=1";
		String oql = "portfoli.SecId='NCRST.A' AND portfolio.Num>123.5 OR portfolio.Num2=1.4 OR portfolio.Description='hello.world'";
		CompiledUnit cu = new CompiledUnit(oql);
		System.out.println("        Query=" + oql);
		System.out.println("CompiledQuery=" + cu.getCompiledQuery());

		// String lucene = "portfolio?PortforlioName:\"hello.test\" AND Id:id";
		// CompiledUnit cu2 = new CompiledUnit(lucene);
		// System.out.println(" Query=" + lucene);
		// System.out.println("CompiledQuery=" + cu2.getCompiledQuery());

	}

	// @Test
	public void testPql()
	{
		JsonLite jl = (JsonLite) loader.createAccount("acct_a", false);
		KeyType keyType = jl.getKeyType();

		// String pql = "portfolio.get('AccountId')=${AccountId} AND
		// account.get('AccountId')=${AccountId}";
		// String pql = "portfolio.AccountId=${AccountId} AND
		// account.AccountId=${AccountId}";
		String pql = "portfolio.AccountId='acct_a'";
		// CompiledUnit cu = new CompiledUnit(pql, Portfolio.getKeyType());
		CompiledUnit cu = new CompiledUnit(pql);
		System.out.println("Compiled Query: " + cu.getCompiledQuery());
		String identityKeyQueryStatementWithTime = cu.getTemporalIdentityQuery(-1, -1);
		System.out.println("identityKeyQueryStatementWithTime=" + identityKeyQueryStatementWithTime);

		String gridPath = getGridPath(identityKeyQueryStatementWithTime);
		System.out.println("grid path=" + gridPath);

		// queryStatement =
		// "select distinct e1.key.IdentityKey from /mypado/portfolio.entrySet
		// e1, "
		// +
		// "/mypado/account.entrySet e2 " +
		// "where " +
		// "(e1.value.value['AccoutId']='acct_a' AND
		// e2.value.value['AccountId']='acct_a')";

		// queryStatement =
		// "select distinct * from /mypado/portfolio e1, /mypado/account e2 " +
		// "where " +
		// "(e1.value['AccoutId']='acct_a' AND e2.value['AccountId']='acct_a')";

		String queryStatement = "select distinct e1.value from /mypado/portfolio.entrySet e1, /mypado/account.entrySet e2 "
				+ "where  "
				+ "e1.value.value.get('AccountId')='acct_ihmhirnww' AND e2.value.value.get('AccountId')='acct_ihmhirnww'";
		// queryStatement =
		// "select distinct e1, e2 from /mypado/account e1, /mypado/portfolio e2
		// " +
		// "where " +
		// "e1.value.value.get('AccountId')=e2.value.value.get('AccountId')";
		queryStatement = "SELECT a FROM /mypado/account a, /mypado/portfolio p WHERE a.value.get('AccountId') = 'acct_ihmhirnww'";// p.value.get('AccountId')";//='acct_ihmhirnww'";//
																																	// AND
																																	// a.value.get('AccountId')='acct_ihmhirnww'";

		queryStatement = "SELECT p FROM /mypado/account a, /mypado/portfolio p "
				+ "WHERE a.value.get('AccountId') = p.value.get('AccountId') "
				+ "AND a.value.get('AccountId') = 'acct_a'";

		String customerId = String.valueOf(RANDOM.nextInt(NUM_CUSTOMERS));
		queryStatement = "SELECT c, o FROM /mypado/customer2 c, /mypado/order2 o WHERE c.id = o.customerId AND c.id = '"
				+ customerId + "'";
		// queryStatement = "SELECT c.key, o.key FROM /mypado/customer2.entrySet
		// c, /mypado/order2.entrySet o WHERE c.value.id = o.value.customerId
		// AND c.value.id = '" + customerId + "'";

		System.out.println("queryStatement=" + queryStatement);

		List list = pqlBiz.executeGemfireOql(queryStatement);
		for (int i = 0; i < list.size(); i++) {
			System.out.println("list[" + i + "]" + list.get(i));
		}
	}

	/**
	 * Returns all region paths found in the specified query string. I returns
	 * an empty string array if no region paths are found.
	 * 
	 * @param queryString
	 *            Query string with region paths
	 * @throws com.gemstone.gemfire.cache.query.QueryInvalidException
	 *             Thrown if the query string is invalid
	 */
	public static String[] getRegionPaths(String queryString)
	{
		QueryService qs = CacheFactory.getAnyInstance().getQueryService();
		Query query = qs.newQuery(queryString);
		DefaultQuery dq = (DefaultQuery) query;
		Set<String> regionPaths = dq.getRegionsInQuery(null);
		if (regionPaths != null) {
			return regionPaths.toArray(new String[regionPaths.size()]);
		}
		return new String[0];
	}

	/**
	 * Returns one of the region paths found in the specified query string. It
	 * returns null if a region path is not found.
	 * 
	 * @param queryString
	 *            Query string with region paths
	 * @throws com.gemstone.gemfire.cache.query.QueryInvalidException
	 *             Thrown if the query string is invalid
	 */
	public static String getRegionPath(String queryString)
	{
		QueryService qs = CacheFactory.getAnyInstance().getQueryService();
		Query query = qs.newQuery(queryString);
		DefaultQuery dq = (DefaultQuery) query;
		Set<String> regionPaths = dq.getRegionsInQuery(null);
		if (regionPaths != null) {
			for (String string : regionPaths) {
				return string;
			}
		}
		return null;
	}

	/**
	 * Returns one of the grid paths found in the specified query string.
	 * 
	 * @param queryString
	 *            Query string with region paths
	 * @throws com.gemstone.gemfire.cache.query.QueryInvalidException
	 *             Thrown if the query string is invalid
	 */
	public static String getGridPath(String queryString)
	{
		String fullPath = getRegionPath(queryString);
		if (fullPath == null) {
			return null;
		}
		return GridUtil.getChildPath(fullPath);

	}

	/**
	 * Returns all grid paths found in the specified query string. I returns an
	 * empty string array if no grid paths are found.
	 * 
	 * @param queryString
	 *            Query string with region paths
	 * @throws com.gemstone.gemfire.cache.query.QueryInvalidException
	 *             Thrown if the query string is invalid
	 */
	public static String[] getGridPaths(String queryString)
	{
		String fullPaths[] = getRegionPaths(queryString);
		String gridPaths[] = new String[fullPaths.length];
		for (int i = 0; i < gridPaths.length; i++) {
			gridPaths[i] = GridUtil.getChildPath(fullPaths[i]);
		}
		return gridPaths;
	}
}
