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

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.ITemporalBiz;
import com.netcrest.pado.biz.util.BizThreadPool;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.temporal.ITemporalBizLink;
import com.netcrest.pado.temporal.TemporalEntry;

/**
 * TemporalGetTest requires "mygrid". Note that this test runs interactively.
 * @author dpark
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemporalGetTest
{
	private static IPado pado;
	private static ITemporalBizLink<Object, KeyMap> temporalBiz;
	
	private static BizThreadPool<ITemporalBizLink> temporalBizThreadPool;
	
	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "test1", "test123".toCharArray());
		ICatalog catalog = pado.getCatalog();
//		temporalBiz = catalog.newInstance(ITemporalBiz.class, "account");
		
		temporalBizThreadPool = new BizThreadPool(catalog, ITemporalBiz.class, "account");
		temporalBiz = temporalBizThreadPool.getBiz();
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	
	@Test
	public void testGet() throws Exception
	{
		List<TemporalEntry<Object, KeyMap>> temporalEntryList = temporalBiz.getQueryEntries("account?acct_a*", -1, -1);
		System.out.println("Identity Keys: ");
		for (TemporalEntry<Object, KeyMap> entry : temporalEntryList) {
			System.out.print(entry.getTemporalKey().getIdentityKey() + " ");
		}
		System.out.println();
		System.out.println();
		
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(System.in));
		String input = "";
		do {
			System.out.println("Enter Identity Key or hit Enter to quit: ");
			input = reader.readLine().trim();
			if (input.length() == 0) {
				break;
			}
			KeyMap account = temporalBiz.get(input);
			System.out.println("[identityKey=" + input + ", account=" + account + "]");
		} while (input.length() > 0);
		System.out.println("Stopped");
	}
	
}
