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
import com.netcrest.pado.biz.IUtilBiz;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.temporal.ITemporalData;

public class UtilBizTest
{
	private static IPado pado;
	private static IUtilBiz utilBiz;

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		// Security 
		System.setProperty("gemfireSecurityPropertyFile", "etc/client/gfsecurity.properties");
		System.setProperty("pado.security.aes.user.certificate", "etc/user.cer");
		System.setProperty("pado.security.enabled", "true");
		
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		Pado.connect("localhost:20000", true);
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		utilBiz = catalog.newInstance(IUtilBiz.class, "account");
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	private void printResults(List list)
	{
		if (list == null) {
			System.out.println("Query results not found");
		} else {
			for (int i = 0; i < list.size(); i++) {
				Object obj = list.get(i);
				if (obj instanceof ITemporalData) {
					((ITemporalData) obj).__getTemporalValue().deserializeAll();
				}
				System.out.println(obj);
			}
		}
		System.out.println();
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testRoutingQuery()
	{
		System.out.println("UtilBizTest.testRoutingQuery()");
		System.out.println("------------------------------");
		String queryString = "select distinct * from /mygrid/account where value.get('AccountId')='acct_a'";
		List list = utilBiz.executeRoutingQuery(queryString, "acct_a");
		printResults(list);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testRoutingQuery2()
	{
		System.out.println("UtilBizTest.testRoutingQuery()");
		System.out.println("------------------------------");
		String queryString = "select distinct * from /mygrid/account where value.get('AccountId')='acct_a'";
		List list = utilBiz.executeRoutingQuery(queryString, "acct_a");
		printResults(list);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testRoutingQuery3()
	{
		System.out.println("UtilBizTest.testRoutingQuery()");
		System.out.println("------------------------------");
		String queryString = "select distinct * from /mygrid/account where value.get('AccountId')='acct_a'";
		List list = utilBiz.executeRoutingQuery(queryString, "acct_a");
		printResults(list);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testQuery()
	{
		System.out.println("UtilBizTest.testQuery()");
		System.out.println("-----------------------");
		String queryString = "select distinct e1.value, e2.value from $1 e1, $2 e2 where e1.value.value.get('$3')=e2.value.value.get('$4')";
		List list = utilBiz.executeQuery(queryString);
		printResults(list);
	}
}
