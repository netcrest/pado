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
package com.netcrest.pado.test.junit.hpim;

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
 * TemporalScrollTest tests IndexMatrix pagination methods. It requires the
 * following grid:
 * <ul>
 * <li>mygrid
 * </ul>
 * You will first need to populate the grid with account data by running the
 * following:
 * 
 * <pre>
 * bin_sh/client> ./temporal -account 250 100
 * </pre>
 * 
 * <i>Note that this test is interactive.</i>
 * <p>
 * 
 * @author dpark
 * 
 */
@SuppressWarnings("rawtypes")
public class TemporalScrollTest
{
	private static IPado pado;
	private static ITemporalBiz<String, JsonLite> temporalBiz;

	@SuppressWarnings("unchecked")
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
		temporalBiz.setGridPath("temporal");
		temporalBiz.getBizContext().getGridContextClient().setGridIds("mygrid");
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	@Test
	public void testScrollNextSet() throws ParseException
	{
		System.out.println("testScrollNextSet");
		System.out.println("=================");
		String pql = "account?";

		IScrollableResultSet<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> sr = temporalBiz
				.getEntryResultSet(pql, -1, -1, null, true, 100, true);
		printEntryScrollableResultSet_NextSet(sr);
	}

	private void printEntryScrollableResultSet_NextSet(
			IScrollableResultSet<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> sr)
	{
		System.out.println("Total size: " + sr.getTotalSize());
		System.out.println("Set Number | Set Count | Set Size | List Size");
		int lastSetNum = sr.getTotalSize() / sr.getFetchSize() + (sr.getTotalSize() % sr.getFetchSize() > 0 ? 1 : 0);
		int lastSetSize;
		if (lastSetNum <= 1) {
			lastSetSize = sr.getTotalSize();
		} else {
			lastSetSize = sr.getTotalSize() % sr.getFetchSize();
		}
		int setNum;
		int setSize;
		do {
			setNum = sr.getSetNumber();
			setSize = sr.getSetSize();
			List<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> list = sr.toList();
			System.out.println(setNum + " | " + sr.getSetCount() + " | " + setSize + " | " + list.size());

		} while (sr.nextSet());
		sr.close();
		Assert.assertTrue(setNum == lastSetNum);
		Assert.assertTrue(setSize == lastSetSize);
	}

	@Test
	public void testPageSearch() throws Exception
	{
		System.out.println("testPageSearch");
		System.out.println("==============");
		String pql = "account?";
		int pageSize = 100;
		IScrollableResultSet<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> sr = temporalBiz
				.getEntryResultSet(pql, null, true, pageSize, true);
		int totalSize = sr.getTotalSize();
		int maxPage = totalSize / pageSize + (totalSize % pageSize > 0 ? 1 : 0);
		int size = sr.toList().size();
		int pos = (maxPage - 1) * pageSize;
		int left = totalSize - pos;
		System.out.println("maxPage:" + maxPage + " totalSize:" + totalSize + " pos:" + pos + " size:" + size
				+ " left:" + left);
		System.out.println();
		boolean bl = sr.goToSet(pos);
		int lastSetSize = sr.toList().size();
		Assert.assertTrue(bl);
		Assert.assertTrue(left == lastSetSize);
	}

	@Test
	public void testScrollGoToSet() throws ParseException
	{
		System.out.println("testScrollGoToSet");
		System.out.println("=================");
		String pql = "account?";

		IScrollableResultSet<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> sr = temporalBiz
				.getEntryResultSet(pql, -1, -1, null, true, 100, true);
		printEntryScrollableResultSet_GoToSet(sr);
	}

	private void printEntryScrollableResultSet_GoToSet(
			IScrollableResultSet<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> sr)
	{
		System.out.println("Total size: " + sr.getTotalSize());
		System.out.println("Set Count: " + sr.getSetCount());
		System.out.println("SetNum | getSetNumber | getSetCount | getSetSize | getListSize");
		int lastSetNum = sr.getTotalSize() / sr.getFetchSize() + (sr.getTotalSize() % sr.getFetchSize() > 0 ? 1 : 0);
		int lastSetSize;
		if (lastSetNum <= 1) {
			lastSetSize = sr.getTotalSize();
		} else {
			lastSetSize = sr.getTotalSize() % sr.getFetchSize();
		}
		int setNum = 0;
		int setSize = 0;
		int index = 0;
		sr.goToSet(index);
		while (index < sr.getTotalSize()) {
			setNum = sr.getSetNumber();
			setSize = sr.getSetSize();
			List<TemporalEntry<ITemporalKey<String>, ITemporalData<String>>> list = sr.toList();
			System.out.println(index + " | " + setNum + " | " + sr.getSetCount() + " | " + setSize + " | "
					+ list.size());
			index += sr.getFetchSize();
			sr.goToSet(index);
		}
		sr.close();
		System.out.println();
		Assert.assertTrue(setNum == lastSetNum);
		Assert.assertTrue(setSize == lastSetSize);
	}

}
