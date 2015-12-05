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

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.temporal.test.biz.ITemporalServerBiz;

/**
 * TemporalServerBizTest requires "mygrid". It tests server-side ITemporalBiz
 * invocations.
 * @author dpark
 *
 */
@SuppressWarnings({ "rawtypes" })
public class TemporalServerBizTest
{
	private static IPado pado;
	private static ITemporalServerBiz temporalServerBiz;
	
	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "test1", "test123".toCharArray());
		ICatalog catalog = pado.getCatalog();
		temporalServerBiz = catalog.newInstance(ITemporalServerBiz.class);
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}
	
	@Test
	public void testNonPureClientQuery()
	{
		boolean executed = false;
		try {
			temporalServerBiz.executeQueryFromServer("account?acct_a*");
			Assert.fail("Exception expected but non-pure client query executed successfully.");
		} catch (PadoException ex) {
		}
	}
	
	@Test
	public void testServerOQL()
	{
		boolean executed = false;
		List results = temporalServerBiz.executeOQLFromServer("select * from /mygrid/account");
		System.out.println(results.size());
	}
}
