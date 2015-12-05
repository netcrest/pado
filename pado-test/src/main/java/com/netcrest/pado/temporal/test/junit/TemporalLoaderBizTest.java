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

import java.util.Collections;
import java.util.List;
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
import com.netcrest.pado.temporal.ITemporalBulkLoader;
import com.netcrest.pado.temporal.TemporalUtil;
import com.netcrest.pado.temporal.test.TradeFactory;
import com.netcrest.pado.temporal.test.biz.ITemporalLoaderBiz;
import com.netcrest.pado.temporal.test.biz.ITemporalLoaderBizFuture;

/**
 * TemporalLoaderBizTest requires the grid "mock" or a grid with the "trade"
 * path.
 * 
 * @author dpark
 * 
 */
public class TemporalLoaderBizTest
{
	private static IPado pado;
	private static ITemporalLoaderBizFuture temporalLoaderBizFuture;
	private static ITemporalLoaderBiz temporalLoaderBiz;
	private static ITemporalBiz temporalBiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:21000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		String[] bizClassNames = catalog.getAllBizClassNames();
		System.out.println("Catalog:");
		for (int i = 0; i < bizClassNames.length; i++) {
			System.out.println("   " + bizClassNames[i]);
		}
		System.out.println();
		temporalLoaderBizFuture = catalog.newInstance(ITemporalLoaderBizFuture.class);
		temporalLoaderBiz = catalog.newInstance(ITemporalLoaderBiz.class);
		temporalBiz = catalog.newInstance(ITemporalBiz.class, "trade");
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testLoaderFuture() throws Exception
	{
		System.out.println("TemporalLoaderBizTest.testLoaderFuture()");
		System.out.println("----------------------------------------");
		String path = "trade";
		int totalCount = 100000;
		int batchSize = 10000;
		Future<List<String>> future = temporalLoaderBizFuture.loadTrades(path, totalCount, batchSize);

		List<String> responses = future.get();
		Collections.sort(responses);
		int i = 0;
		for (String response : responses) {
			i++;
			System.out.println(i + ". " + response);
		}
		System.out.println();
	}

	//@Test
	public void testLoaderBiz() throws Exception
	{
		System.out.println("TemporalLoaderBizTest.testLoaderBiz()");
		System.out.println("-------------------------------------");
		String path = "trade";
		int totalCount = 50000;
		int batchSize = 10000;
		List<String> responses = temporalLoaderBiz.loadTrades(path, totalCount, batchSize);
		Collections.sort(responses);
		int i = 0;
		for (String response : responses) {
			i++;
			System.out.println(i + ". " + response);
		}
		System.out.println();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
//	@Test
	public void testLoaderClient() throws Exception
	{
		System.out.println("TemporalLoaderBizTest.testLoaderClient()");
		System.out.println("----------------------------------------");
		int totalCount = 10000;
		int batchSize = 1000;
		ITemporalBulkLoader bulkLoader = temporalBiz.getTemporalAdminBiz().createBulkLoader(batchSize);
		for (long j = 0; j < totalCount; j++) {
			// Use bucket Ids to set the trade Ids so that trades
			// are placed in the same VM (server) that executes
			// this.
			long tradeId = j;
			JsonLite trade = TradeFactory.createTrade(tradeId);
			long startValidTime = System.currentTimeMillis();
			long endValidTime = TemporalUtil.MAX_TIME;
			long writtenTime = startValidTime;
			bulkLoader.put(tradeId, trade, null, startValidTime, endValidTime, writtenTime, false);
		}
		bulkLoader.flush();
		System.out.println("Trade objects loaded: " + totalCount);
		System.out.println();
	}

}
