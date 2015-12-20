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

/**
 * TemporalEntrySearchTest requires "mygrid". You must first populate the grid
 * by running {@link TemporalAddData} once.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemporalEntrySearchTest
{
	private static IPado pado;
	private static ITemporalBiz<String, JsonLite> temporalBiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException, ParseException
	{
		// Security -- Comment out the following to disable security
		// System.setProperty("gemfireSecurityPropertyFile",
		// "etc/client/gfsecurity.properties");
		// System.setProperty("pado.security.aes.user.certificate",
		// "etc/user.cer");
		// System.setProperty("pado.security.enabled", "true");

		System.setProperty("gemfirePropertyFile", "../deploy/pado_0.4.0-B1/etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "test1", "test123".toCharArray());
		ICatalog catalog = pado.getCatalog();
		temporalBiz = catalog.newInstance(ITemporalBiz.class);
		temporalBiz.setGridPath("temporal");
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testLuceneEntry() throws ParseException
	{

		System.out.println("testLuceneEntry");
		System.out.println("===============");
		String pql = "temporal?Flag:N";
		executeQueryEntry(pql);
	}
	
	@Test
	public void testOqlEntry() throws ParseException
	{
		System.out.println("testOqlEntry");
		System.out.println("============");
		String pql = "temporal.Flag='N'";
		executeQueryEntry(pql);
	}
	
	private void executeQueryEntry(String pql)
	{
		IScrollableResultSet<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> sr = temporalBiz
				.getEntryResultSet(pql, -1, -1, null, true, 100, true);
		printEntryScrollableResultSet(sr);
		
		Assert.assertTrue(sr.getTotalSize() == 1);
		sr.goToSet(0);
		List<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> list = sr.toList();
		for (TemporalEntry<ITemporalKey<String>, ITemporalData<String>> entry : list) {
			JsonLite jl = (JsonLite) entry.getValue();
			String flag = (String)jl.get("Flag");
			Assert.assertTrue(flag.equals("N"));
		}
	}

	@Test
	public void testLuceneValue() throws ParseException
	{
		System.out.println("testLuceneValue");
		System.out.println("===============");
		String pql = "temporal?Flag:N";
		executeQueryValue(pql);
	}
	
	@Test
	public void testOqlValue() throws ParseException
	{
		System.out.println("testOqlValue");
		System.out.println("============");
		String pql = "temporal.Flag='N'";
		executeQueryValue(pql);
	}
	
	private void executeQueryValue(String pql)
	{
		IScrollableResultSet<JsonLite> sr = temporalBiz.getValueResultSet(pql, -1, -1, null, true, 100, true);
		printValueScrollableResultSet(sr);
		
		Assert.assertTrue(sr.getTotalSize() == 1);
		sr.goToSet(0);
		List<JsonLite> list = sr.toList();
		for (JsonLite jl : list) {
			String flag = (String)jl.get("Flag");
			Assert.assertTrue(flag.equals("N"));
		}
	}

	@Test
	public void testLueneEntryWrittenTimeRange() throws ParseException
	{
		System.out.println("testLueneEntryWrittenTimeRange");
		System.out.println("===============================");
		String pql = "temporal?Flag:Y";
		long validAtTime = -1;
		// 11/03/2015 11:53:03.396
		long fromWrittenTime = TemporalAddData.WRITTEN_TIME1 - 1;
		// 11/03/2015 11:53:03.730
		long toWrittenTime = TemporalAddData.WRITTEN_TIME2 + 1;
		executeQueryEntryWrittenTimeRange(pql, validAtTime, fromWrittenTime, toWrittenTime);
	}
	
	@Test
	public void testOqlEntryWrittenTimeRange() throws ParseException
	{
		System.out.println("testOqlEntryWrittenTimeRange");
		System.out.println("============================");
		String pql = "temporal.Flag='Y'";
		long validAtTime = -1;
		// 11/03/2015 11:53:03.396
		long fromWrittenTime = TemporalAddData.WRITTEN_TIME1 - 1;
		// 11/03/2015 11:53:03.730
		long toWrittenTime = TemporalAddData.WRITTEN_TIME2 + 1;
		executeQueryEntryWrittenTimeRange(pql, validAtTime, fromWrittenTime, toWrittenTime);
	}
	
	@Test
	public void testLuceneValueWrittenTimeRange() throws ParseException
	{
		System.out.println("testLuceneValueWrittenTimeRange");
		System.out.println("===============================");
		String pql = "temporal?Flag:Y";
		long validAtTime = -1;
		// 11/03/2015 11:53:03.396
		long fromWrittenTime = TemporalAddData.WRITTEN_TIME1 - 1;
		// 11/03/2015 11:53:03.730
		long toWrittenTime = TemporalAddData.WRITTEN_TIME2 + 1;

		executeQueryEntryWrittenTimeRange(pql, validAtTime, fromWrittenTime, toWrittenTime);
	}
	
	@Test
	public void testOqlValueWrittenTimeRange() throws ParseException
	{
		System.out.println("testOqlValueWrittenTimeRange");
		System.out.println("============================");
		String pql = "temporal.Flag='Y'";
		long validAtTime = -1;
		// 11/03/2015 11:53:03.396
		long fromWrittenTime = TemporalAddData.WRITTEN_TIME1 - 1;
		// 11/03/2015 11:53:03.730
		long toWrittenTime = TemporalAddData.WRITTEN_TIME2 + 1;

		executeQueryEntryWrittenTimeRange(pql, validAtTime, fromWrittenTime, toWrittenTime);
	}
	
	private void executeQueryEntryWrittenTimeRange(String pql, long validAtTime, long fromWrittenTime, long toWrittenTime)
	{
		IScrollableResultSet<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> sr = temporalBiz
				.getEntryWrittenTimeRangeResultSet(pql, -1, fromWrittenTime, toWrittenTime, null, true, 100, true);
		printEntryScrollableResultSet(sr);
		
		Assert.assertTrue(sr.getTotalSize() == 1);
		sr.goToSet(0);
		List<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> list = sr.toList();
		for (TemporalEntry<ITemporalKey<String>, ITemporalData<String>> entry : list) {
			JsonLite jl = (JsonLite) entry.getValue();
			String flag = (String)jl.get("Flag");
			Assert.assertTrue(flag.equals("Y"));
		}
	}

	private void printValueScrollableResultSet(IScrollableResultSet<JsonLite> sr)
	{
		int page = 0;
		int i = 0;
		do {
			System.out.println("Set " + ++page);
			System.out.println("-------------------------");
			List<JsonLite> list = sr.toList();
			for (JsonLite jl : list) {
				System.out.println(++i + ". " + jl.toString(2, true, false));
			}
			System.out.println();
		} while (sr.nextSet());
		sr.close();
	}

	private void printEntryScrollableResultSet(
			IScrollableResultSet<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> sr)
	{
		int page = 0;
		int i = 0;
		do {
			System.out.println("Set " + ++page);
			System.out.println("-------------------------");
			List<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> list = sr.toList();
			for (TemporalEntry<ITemporalKey<String>, ITemporalData<String>> entry : list) {
				JsonLite jl = (JsonLite) entry.getValue();
				System.out.println(entry.getTemporalKey().toStringDate());
				System.out.println(++i + ". " + jl.toString(2, true, false));
			}
			System.out.println();
		} while (sr.nextSet());
		sr.close();
	}
}
