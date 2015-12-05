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

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ILuceneBiz;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.index.gemfire.service.GridQueryService;
import com.netcrest.pado.index.internal.Constants;
import com.netcrest.pado.index.service.GridQuery;
import com.netcrest.pado.index.service.GridQueryFactory;
import com.netcrest.pado.index.service.IGridQueryService;
import com.netcrest.pado.index.service.IScrollableResultSet;

public class LuceneTest
{
	private static IPado pado;
	private static ILuceneBiz luceneBiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		luceneBiz = catalog.newInstance(ILuceneBiz.class);
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testBuildAllIndexes()
	{
		luceneBiz.buildAllIndexes();
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testQuery()
	{
		IGridQueryService qs = GridQueryService.getGridQueryService();
		GridQuery criteria = GridQueryFactory.createGridQuery();
		criteria.setId("queryTestId");
		criteria.setAscending(true);
		criteria.setFetchSize(100);
		criteria.setOrdered(true);
		criteria.setQueryString("t*");
		criteria.setSortField(null);
		criteria.setForceRebuildIndex(true);
		criteria.setGridIds("mygrid-us");
		criteria.setGridService(pado.getCatalog().getGridService());
		criteria.setFullPath("/mygrid/position");
		criteria.setProviderKey(Constants.PQL_PROVIDER_KEY);
		IScrollableResultSet sr = (IScrollableResultSet) qs.query(criteria);
		int page = 0;
		do {
			System.out.println("Set " + ++page);
			System.out.println("-------------------------");
			List list = sr.toList();
			int i = 0;
			for (Object object : list) {
				System.out.println(++i + ". " + object);
			}
			System.out.println();
		} while (sr.nextSet());
		sr.close();
	}
}
