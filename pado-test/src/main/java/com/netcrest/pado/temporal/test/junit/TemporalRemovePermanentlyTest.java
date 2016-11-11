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
import com.netcrest.pado.biz.IPathBiz;
import com.netcrest.pado.biz.IPathBiz.PathType;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.index.provider.lucene.DateTool;
import com.netcrest.pado.temporal.ITemporalKey;
import com.netcrest.pado.temporal.TemporalDataList;
import com.netcrest.pado.temporal.TemporalEntry;
import com.netcrest.pado.temporal.TemporalUtil;

/**
 * TemporalRemovePermanentlyTest creates a  path, "test/temporal", puts some data into
 * it, and attempts to permanently remove temporal entries.
 * 
 * @author dpark
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemporalRemovePermanentlyTest
{
	private static String gridPath = "test/temporal";
	private static IPado pado;
	private static ITemporalBiz<String, KeyMap> temporalBiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException, ParseException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		temporalBiz = catalog.newInstance(ITemporalBiz.class, gridPath);
	}

	private void initData() throws ParseException
	{
		IPathBiz pathBiz = pado.getCatalog().newInstance(IPathBiz.class);
		pathBiz.createPath(pado.getGridId(), "test/temporal", PathType.TEMPORAL, true);
		// Delay a bit to makes sure the path is created
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// ignore
		}
		temporalBiz.setGridPath(gridPath);
		JsonLite jl = new JsonLite();
		jl.put("Name", "Foo");
		jl.put("Id", "123");
		
		long startValidTime = DateTool.stringToTime("20160701");
		long endValidTime = TemporalUtil.MAX_TIME;
		long writtenTime = DateTool.stringToTime("20160718"); // yyyyMMddHHmmssSSS
		temporalBiz.put("key1", jl, startValidTime, endValidTime, writtenTime, false);
		jl.put("Name", "Yong");
		jl.put("Id", "456");
		writtenTime = DateTool.stringToTime("20160719"); // yyyyMMddHHmmssSSS
		temporalBiz.put("key1", jl, startValidTime, endValidTime, writtenTime, false);
		jl.put("Name", "Unknown");
		jl.put("Id", "789");
		writtenTime = DateTool.stringToTime("20160720"); // yyyyMMddHHmmssSSS
		temporalBiz.put("key1", jl, startValidTime, endValidTime, writtenTime, false);
		// Delay a bit to make sure temporal data is indexed
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	@AfterClass
	public static void closePado()
	{
		IPathBiz pathBiz = pado.getCatalog().newInstance(IPathBiz.class);
		pathBiz.clear(pado.getGridId(), gridPath, true);
		Pado.close();
	}

	@Test
	public void testRemovePermanentlyFirstEntryFoo() throws Exception
	{
		System.out.println("TemporalRemovePermanentlyTest.testRemovePermanentlyFirstEntryFoo()");
		System.out.println("------------------------------------------------------------------");
		removeEntry("Foo");
	}
	
	@Test
	public void testRemovePermanentlyMiddleEntryYong() throws Exception
	{
		System.out.println("TemporalRemovePermanentlyTest.testRemovePermanentlyMiddleEntryYong()");
		System.out.println("--------------------------------------------------------------------");
		removeEntry("Yong");
	}
	
	@Test
	public void testRemovePermanentlyLastEntryUnknown() throws Exception
	{
		System.out.println("TemporalRemovePermanentlyTest.testRemovePermanentlyLastEntryUnknown()");
		System.out.println("---------------------------------------------------------------------");
		removeEntry("Unknown");
	}
	
	private void removeEntry(String nameToRemove) throws ParseException
	{
		initData();
		
		// Get the temporal list and remove nameToRemove from the list.
		TemporalDataList list = temporalBiz.getTemporalAdminBiz().getTemporalDataList("key1");
		list.dump();
		
		List<TemporalEntry<String, JsonLite>> teList = list.getTemporalList();
		boolean removed = false;
		for (TemporalEntry<String, JsonLite> te : teList) {
			ITemporalKey tk = te.getTemporalKey();
			JsonLite jl = te.getValue();
			String name = (String) jl.get("Name");
			if (name.equals(nameToRemove)) {
				temporalBiz.getTemporalAdminBiz().removePermanently(tk);
				removed = true;
				break;
			}
		}
		Assert.assertTrue(nameToRemove + " not found", removed);

		// Get the temporal list again to see if the entry has been removed
		TemporalDataList list2 = temporalBiz.getTemporalAdminBiz().getTemporalDataList("key1");
		list2.dump();
		List<TemporalEntry<String, JsonLite>> teList2 = list2.getTemporalList();
		for (TemporalEntry<String, JsonLite> te : teList2) {
			JsonLite jl = te.getValue();
			String name = (String) jl.get("Name");
			Assert.assertNotEquals(name, nameToRemove);
		}
	}

}
