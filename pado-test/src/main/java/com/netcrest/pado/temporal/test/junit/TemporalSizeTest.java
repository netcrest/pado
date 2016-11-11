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

/**
 * TemporalSizeTest requires "mygrid" with "nw/customers" data.
 * @author dpark
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemporalSizeTest
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
		
		temporalBizThreadPool = new BizThreadPool(catalog, ITemporalBiz.class, "nw/customers");
		temporalBiz = temporalBizThreadPool.getBiz();
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}
	
	@Test
	public void testSize()
	{
		int size = temporalBiz.size(-1, -1);
		System.out.println("size=" + size);
	}
	
	@Test
	public void testGetTemporalListCount()
	{
		int count = temporalBiz.getTemporalListCount();
		System.out.println("TemporalListCount=" + count);
	}
}
