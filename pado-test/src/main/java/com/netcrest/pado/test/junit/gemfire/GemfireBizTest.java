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
package com.netcrest.pado.test.junit.gemfire;

import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.query.FunctionDomainException;
import com.gemstone.gemfire.cache.query.NameResolutionException;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryInvocationTargetException;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.TypeMismatchException;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.gemfire.biz.biz.IGemfireBiz;
import com.netcrest.pado.temporal.ITemporalData;

/**
 * GemfireBizTest tests IGemfireBiz methods. This test class requires the
 * "mygrid" populated with account mock data.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "rawtypes" })
public class GemfireBizTest
{
	private static IPado pado;

	@BeforeClass
	public static void loginPado() throws PadoLoginException, Exception
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "test1", "test123".toCharArray());
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	/**
	 * Tests an OQL query.
	 * 
	 * @throws FunctionDomainException
	 * @throws TypeMismatchException
	 * @throws NameResolutionException
	 * @throws QueryInvocationTargetException
	 */
	@Test
	public void testOQL() throws FunctionDomainException, TypeMismatchException, NameResolutionException,
			QueryInvocationTargetException
	{
		IGemfireBiz gemfireBiz = pado.getCatalog().newInstance(IGemfireBiz.class);
		String queryString = "select * from /mygrid/account";
		Pool pool = gemfireBiz.getPool("mygrid");
		QueryService qs = pool.getQueryService();
		Query query = qs.newQuery(queryString);
		Object obj = query.execute();
		Assert.assertTrue(obj instanceof SelectResults);
		SelectResults sr = (SelectResults) obj;
		Assert.assertTrue("Result set size is 0. Populate the grid with account mock data.", sr.size() > 0);
		Iterator iterator = sr.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			Object dataObj = iterator.next();
			Assert.assertTrue("Data object not ITemporalData: " + dataObj.getClass().getName(), dataObj instanceof ITemporalData);
			ITemporalData temporalData = (ITemporalData) dataObj;
			Object value = temporalData.getValue();
			Assert.assertTrue("Value not JsonLite: " + value.getClass().getName(), value instanceof JsonLite);
		}
	}
}
