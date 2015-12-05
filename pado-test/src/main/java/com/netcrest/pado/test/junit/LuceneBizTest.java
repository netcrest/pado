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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ILuceneBiz;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.index.service.GridQueryFactory;
import com.netcrest.pado.temporal.ITemporalData;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalData;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalUtil;
import com.netcrest.pado.temporal.test.data.Account;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class LuceneBizTest
{
	private static String GRID_PATH = "margin_node";
	private static String identityKey = "0004cba5\\-3bfd\\-402a\\-8097\\-f72dff1facd8";
	private static String validAt = "20140420";
	private static String asOf = "20140420";

	
	private static String QUERY_STRING = identityKey;
	
//	private static String QUERY_STRING = identityKey + " AND StartValidTime:[19750101 TO " + validAt + "] AND EndValidTime:{" + validAt  + " TO 30000101] AND " +
//			"StartWrittenTime:[19750101 TO " + asOf + "] AND EndWrittenTime:{" + asOf + " TO 30000101] AND LastName:x*";
//	
//	private static String QUERY_STRING = identityKey + " AND StartValidTime:[19750101 TO " + validAt + "] AND EndValidTime:{" + validAt  + " TO 30000101] AND " +
//													"StartWrittenTime:[19750101 TO " + asOf + "] AND EndWrittenTime:{" + asOf + " TO 30000101]" +
//													" AND LastName:x*";
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	// private final static SimpleDateFormat dateFormat = new
	// SimpleDateFormat("yyyyMMddHHddmmssSSS");

//	1. acct_aexpc 20100311 20110204 20100311 odyrsqmmd
//	2. acct_afa 20100610 20110506 20100610 xna
//	3. acct_affslu 20100311 20100908 20100311 xyt
//	4. acct_agriwg 20100311 20100809 20100311 xjtl
//	5. acct_altmkmrye 20100411 20101206 20100411 xacqiv
//	6. acct_ambqk 20100411 20110105 20100411 ogguwhdti
//	7. acct_amukqa 20100710 20101008 20100710 xxda

	private static IPado pado;
	private static ILuceneBiz luceneBiz;
	private static ITemporalBiz temporalBiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException, Exception
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		luceneBiz = catalog.newInstance(ILuceneBiz.class);
		temporalBiz = catalog.newInstance(ITemporalBiz.class, GRID_PATH);

		dateFormat.setTimeZone(TemporalUtil.TIME_ZONE);
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testQuery()
	{
		String gridId = pado.getCatalog().getGridService().getDefaultGridId();
		GridQuery criteria = GridQueryFactory.createGridQuery();
		criteria.setId("queryTestId");
		criteria.setAscending(true);
		criteria.setFetchSize(100);
		criteria.setOrdered(true);
		criteria.setQueryString(QUERY_STRING);
		criteria.setSortField(null);
		criteria.setForceRebuildIndex(true);
		criteria.setGridIds(gridId);
		criteria.setGridService(pado.getCatalog().getGridService());
		criteria.setFullPath(pado.getCatalog().getGridService().getFullPath(gridId, GRID_PATH));
		criteria.setProviderKey(Constants.PQL_PROVIDER_KEY);
		List<TemporalEntry> list = luceneBiz.searchTemporal(criteria);

		System.out.println("LuceneBizTest.testQuery()");
		System.out.println("-------------------------");
		List idList = new ArrayList();
		if (list != null) {
			for (TemporalEntry temporalEntry : list) {
				if (temporalEntry != null) {
					ITemporalKey tk = temporalEntry.getTemporalKey();
					idList.add(tk.getIdentityKey() + " " + dateFormat.format(new Date(tk.getStartValidTime())) + " "
							+ dateFormat.format(new Date(tk.getEndValidTime())) + " "
							+ dateFormat.format(new Date(tk.getWrittenTime())));
				}
			}
		}
		Collections.sort(idList);
		int i = 0;
		for (Object id : idList) {
			System.out.println(++i + ". " + id);
		}
		// System.out.println(list);
		System.out.println();
	}

	@Test
	public void testIdentityKeys()
	{
		Set set = luceneBiz.getTemporalIdentityKeySet(GRID_PATH, QUERY_STRING);

		System.out.println("LuceneBizTest.testIdentityKeys()");
		System.out.println("-------------------------------");
		TreeSet ts = new TreeSet(set);
		int i = 0;
		for (Object id : ts) {
			System.out.println(++i + ". " + id);
		}
		System.out.println();

	}

	@Test
	public void testTemporalKeys()
	{
		Set<ITemporalKey> set = luceneBiz.getTemporalKeySet(GRID_PATH, QUERY_STRING);
		Map<ITemporalKey, ITemporalData> map = temporalBiz.getTemporalAdminBiz().getAll(set);
		System.out.println("LuceneBizTest.testTemporalKeys()");
		System.out.println("--------------------------------");
		List idList = new ArrayList();
		Set<Map.Entry<ITemporalKey, ITemporalData>> entrySet = map.entrySet();
		for (Map.Entry<ITemporalKey, ITemporalData> entry : entrySet) {
			ITemporalKey tk = entry.getKey();
			ITemporalData data = entry.getValue();
			TemporalData x = (TemporalData) data;
			JsonLite jl = (JsonLite)x.getValue();
			idList.add(tk.getIdentityKey() + " " + dateFormat.format(new Date(tk.getStartValidTime())) + " "
					+ dateFormat.format(new Date(tk.getEndValidTime())) + " "
					+ dateFormat.format(new Date(tk.getWrittenTime())) + " " + jl.get(Account.KLastName));
		}
		
		Collections.sort(idList);
		int i = 0;
		for (Object id : idList) {
			System.out.println(++i + ". " + id);
		}
		System.out.println();

	}
	
	@Test
	public void testTemporalBiz() throws ParseException
	{
		System.out.println("LuceneBizTest.testTemporalBiz()");
		System.out.println("-------------------------------");
		
		TemporalDataList list = temporalBiz.getTemporalAdminBiz().getTemporalDataList(identityKey);
		if (list == null) {
			System.out.println("Temporal list not found for " + identityKey);
			return;
		}
		list.dump(dateFormat);
		
		long validAtTime = dateFormat.parse(validAt).getTime();
		long asOfTime = dateFormat.parse(asOf).getTime();
		TemporalEntry entry = temporalBiz.getEntry(identityKey, validAtTime, asOfTime);
		if (entry == null) {
			System.out.println("Not found");
		} else {
			printTemporalKey(entry.getTemporalKey());
		}
	}
	
	private void printTemporalKey(ITemporalKey tk)
	{
		System.out.println(tk.getIdentityKey() + " " + dateFormat.format(new Date(tk.getStartValidTime())) + " "
				+ dateFormat.format(new Date(tk.getEndValidTime())) + " "
				+ dateFormat.format(new Date(tk.getWrittenTime())));
	}
}
