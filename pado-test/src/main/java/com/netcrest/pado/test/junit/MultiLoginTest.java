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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.test.biz.IPartitionedRegionBiz;

public class MultiLoginTest
{
	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		System.setProperty("pado.properties", "etc/client/pado.properties");
		if (Pado.isClosed()) {
			Pado.connect("localhost:20000", false);
		}
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}
	
	@Test
	public void testLogin1() throws Exception
	{
		IPado pado = Pado.login("sys", "netcrest", "dpark", "dpark".toCharArray());
		ICatalog catalog = pado.getCatalog();
		
		// Local implementation PartitionedRegionBizLocalImpl is defined 
		// by @BizClass.
		IPartitionedRegionBiz prBiz = catalog.newInstance(IPartitionedRegionBiz.class);
		int x = 1;
		int y = 2;
		
		// Invoke the remote biz method, which returns the result of x + y.
		int z = prBiz.add(x, y);
		Assert.assertTrue( z == x + y);
	}
	
	@Test
	public void testLogin2() throws Exception
	{
		IPado pado = Pado.login("sys", "netcrest", "foo", "foo".toCharArray());
		ICatalog catalog = pado.getCatalog();
		
		// Local implementation PartitionedRegionBizLocalImpl is defined 
		// by @BizClass.
		IPartitionedRegionBiz prBiz = catalog.newInstance(IPartitionedRegionBiz.class);
		int x = 1;
		int y = 2;
		
		// Invoke the remote biz method, which returns the result of x + y.
		int z = prBiz.add(x, y);
		Assert.assertTrue( z == x + y);
	}
	
	@Test
	public void testLogin3() throws Exception
	{
		IPado pado = Pado.login("sys", "netcrest", "foo", "foo".toCharArray());
		ICatalog catalog = pado.getCatalog();
		
		// Local implementation PartitionedRegionBizLocalImpl is defined 
		// by @BizClass.
		IPartitionedRegionBiz prBiz = catalog.newInstance(IPartitionedRegionBiz.class);
		int x = 1;
		int y = 2;
		
		// Invoke the remote biz method, which returns the result of x + y.
		int z = prBiz.add(x, y);
		Assert.assertTrue( z == x + y);
	}

	@Test
	public void testLogin4() throws Exception
	{
		IPado pado = Pado.login("sys", "netcrest", "yong", "yong".toCharArray());
		ICatalog catalog = pado.getCatalog();
		
		// Local implementation PartitionedRegionBizLocalImpl is defined 
		// by @BizClass.
		IPartitionedRegionBiz prBiz = catalog.newInstance(IPartitionedRegionBiz.class);
		int x = 1;
		int y = 2;
		
		// Invoke the remote biz method, which returns the result of x + y.
		int z = prBiz.add(x, y);
		Assert.assertTrue( z == x + y);
	}
}
