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

import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.biz.mon.ISysBiz;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.info.CacheInfo;
import com.netcrest.pado.info.GridPathInfo;
import com.netcrest.pado.info.PadoInfo;

public class BizMonTest
{
	private static IPado pado;
	private static ISysBiz sysBiz;
	
	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		System.setProperty("pado.properties", "etc/client/pado.properties");
		if (Pado.isClosed()) {
			Pado.connect("localhost:20000", true);
		}
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		sysBiz = pado.getCatalog().newInstance(ISysBiz.class);
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}
	
//	@Test
	public void testLogin() throws PadoLoginException
	{
		System.out.println("BizMonTest.testLogin()");
		System.out.println("----------------------");
		pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		String classNames[] = catalog.getAllBizClassNames();
		System.out.println();
		System.out.println("Catalog:");
		for (String className : classNames) {
			System.out.println("   " + className);
		}
		System.out.println();
		sysBiz = catalog.newInstance(ISysBiz.class);
		Assert.assertNotNull(sysBiz);
	}
	
	@Test
	public void testPadoInfo() throws PadoLoginException
	{
		System.out.println("BizMonTest.testPadoInfo()");
		System.out.println("------------------------");
		// this call always goes to Pado (BizType is set to PADO)
		PadoInfo padoInfo = sysBiz.getPadoInfo("sys");
		Assert.assertNotNull(padoInfo);
		System.out.println(padoInfo);
		System.out.println();
	}
	
	@Test
	public void testCacheInfo() throws PadoLoginException
	{
		System.out.println("BizMonTest.testCacheInfo()");
		System.out.println("--------------------------");
		// This call is directed to the default grid setup by Pado or the annotated grid
		CacheInfo cacheInfo = sysBiz.getCacheInfo();
		Assert.assertNotNull(cacheInfo);
		System.out.println(cacheInfo);
		System.out.println();
	}
	
	@Test
	public void getGridPathInfoMap()
	{
		System.out.println("BizMonTest.getGridPathInfoMap()");
		System.out.println("-------------------------------");
		Map<String, GridPathInfo> gridPathInfoMap = sysBiz.getGridPathInfoMap();
		Set<Map.Entry<String, GridPathInfo>> set = gridPathInfoMap.entrySet();
		for (Map.Entry<String, GridPathInfo> entry : set) {
			String gridPath = entry.getKey();
			GridPathInfo gridPathInfo = entry.getValue();
			System.out.println("gridPath=" + gridPath + ", gridPathInfo=" + gridPathInfo);
		}
		System.out.println();
	}
}
