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
package com.netcrest.pado.test.junit.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.netcrest.pado.IBizContextClient;
import com.netcrest.pado.ICatalog;
import com.netcrest.pado.IGridContextClient;
import com.netcrest.pado.IPado;
import com.netcrest.pado.Pado;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.test.biz.IPartitionedRegionBiz;

import junit.framework.Assert;

/**
 * ReconnectTest tests reconnection to Pado by logging in and logging out
 * repeatedly.
 * 
 * @author dpark
 *
 */
public class ReconnectTest
{

	private static IPado pado;
	private static IPartitionedRegionBiz prBiz;
	
	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/pado/client.properties");
		System.setProperty("pado.security.aes.user.certificate", "etc/user.cer");
		
		testConnect();
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}
	
	@Test
	private static void testConnect() throws PadoException, PadoLoginException
	{
		Pado.connect("localhost:20000", true);
		pado = Pado.login("app1", "netcrest", "dpark", "dpark".toCharArray());
		Assert.assertNotNull(pado);
		ICatalog catalog = pado.getCatalog();
		Assert.assertNotNull(catalog);
		prBiz = (IPartitionedRegionBiz) catalog.newInstance(IPartitionedRegionBiz.class);
		Assert.assertNotNull(prBiz);
	}
	
	@Test
	public void testReconnectException() throws PadoException, PadoLoginException
	{
		try {
			Pado.connect("localhost:20000", true);
			Assert.assertTrue("Pado.connect() did not throw an expected PadoException.", false);
		} catch (PadoException ex) {
			// exception expected. must close first.
			System.out.println("The following exception was expected. Pado must be closed first before reconnection.");
			ex.printStackTrace();
		}
		Pado.close();
		Pado.connect("localhost:20000", true);
		pado = Pado.login("app1", "netcrest", "dpark", "dpark".toCharArray());
		Assert.assertNotNull(pado);
		ICatalog catalog = pado.getCatalog();
		Assert.assertNotNull(catalog);
		prBiz = (IPartitionedRegionBiz) catalog.newInstance(IPartitionedRegionBiz.class);
		Assert.assertNotNull(prBiz);
	}
	
	@Test
	public void testLoginException() throws PadoException, PadoLoginException
	{
		Pado.close();
		try {
			pado = Pado.login("app1", "netcrest", "dpark", "dpark".toCharArray());
			Assert.assertTrue("Pado.login() did not throw an expected LoginException.", false);
		} catch (Exception ex) {
			System.out.println("The following exception was expected. Login is not permitted without proper connection.");
			ex.printStackTrace();
		}
		Pado.connect("localhost:20000", true);
		pado = Pado.login("app1", "netcrest", "dpark", "dpark".toCharArray());
		Assert.assertNotNull(pado);
		ICatalog catalog = pado.getCatalog();
		Assert.assertNotNull(catalog);
		prBiz = (IPartitionedRegionBiz) catalog.newInstance(IPartitionedRegionBiz.class);
		Assert.assertNotNull(prBiz);
	}
	
	@Test
	public void testLogout() throws PadoException, PadoLoginException
	{
		ICatalog oldCatalog = pado.getCatalog();
		
		// logout via static method
		Pado.logoutUser("dpark");
		Assert.assertTrue(pado.isLoggedOut());
		Assert.assertNull(pado.getCatalog());
		Assert.assertNull(pado.getToken());
		Assert.assertTrue(oldCatalog.getAllBizClasses().length == 0);
		Assert.assertTrue(oldCatalog.getAllBizClassNames().length == 0);
		Assert.assertTrue(oldCatalog.getGridIds().length == 0);
		
		// login again for next test
		IPado oldPado = pado;
		pado = Pado.login("app1", "netcrest", "dpark", "dpark".toCharArray());
		Assert.assertNotNull(pado);
		// a new instance expected
		Assert.assertEquals(false, oldPado == pado);
		Assert.assertEquals(false, oldCatalog == pado.getCatalog());
	}
	
	@Test
	public void testLogout2() throws PadoException, PadoLoginException
	{
		ICatalog oldCatalog = pado.getCatalog();
		
		// logout via instance method
		pado.logout();
		Assert.assertTrue(pado.isLoggedOut());
		Assert.assertNull(pado.getCatalog());
		Assert.assertNull(pado.getToken());
		Assert.assertTrue(oldCatalog.getAllBizClasses().length == 0);
		Assert.assertTrue(oldCatalog.getAllBizClassNames().length == 0);
		Assert.assertTrue(oldCatalog.getGridIds().length == 0);
		
		// login again for next test
		IPado oldPado = pado;
		pado = Pado.login("app1", "netcrest", "dpark", "dpark".toCharArray());
		Assert.assertNotNull(pado);
		// a new instance expected
		Assert.assertEquals(false, oldPado == pado);
		Assert.assertEquals(false, oldCatalog == pado.getCatalog());
	}
	
	@Test
	public void testReconnect1() throws PadoException, PadoLoginException
	{
		Pado.close();
		Pado.connect("localhost:20000", true);
		pado = Pado.login("app1", "netcrest", "dpark", "dpark".toCharArray());
		Assert.assertNotNull(pado);
		ICatalog catalog = pado.getCatalog();
		Assert.assertNotNull(catalog);
		prBiz = (IPartitionedRegionBiz) catalog.newInstance(IPartitionedRegionBiz.class);
		Assert.assertNotNull(prBiz);
	}
	
	@Test
	public void testReconnect2() throws PadoException, PadoLoginException
	{
		Pado.close();
		Pado.connect("localhost:20000", true);
		pado = Pado.login("app1", "netcrest", "dpark", "dpark".toCharArray());
		Assert.assertNotNull(pado);
		ICatalog catalog = pado.getCatalog();
		Assert.assertNotNull(catalog);
		prBiz = (IPartitionedRegionBiz) catalog.newInstance(IPartitionedRegionBiz.class);
		Assert.assertNotNull(prBiz);
	}
	
	@Test
	public void testGetBucketIds()
	{
		int[] bucketIds = prBiz.getBucketIds();
		Assert.assertNotNull(bucketIds);
		System.out.println("testGetBucketIds()");
		System.out.print("   ");
		for (int i : bucketIds) {
			System.out.print(i + ", ");
		}
		System.out.println();
		System.out.println();
	}
	
	@Test
	public void testGetBucketMap()
	{
		IGridContextClient gridContextClient = prBiz.getBizContext().getGridContextClient();
		gridContextClient.reset();
		Assert.assertTrue(gridContextClient.getRoutingKeys() == null);
		gridContextClient.setRoutingKeys(Collections.singleton(0));
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		Map<String, String> map = prBiz.getBucketMap(0);
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		Assert.assertNotNull(map);
		System.out.println("testGetBucketMap()");
		System.out.println("   " + map);
		System.out.println();
	}
	
	@Test
	public void testServerIdList()
	{
		IBizContextClient bizContext = prBiz.getBizContext();
		List<String> serverIdList = prBiz.getServerIdList();
		Assert.assertNotNull(serverIdList);
		System.out.println("testServerIdList()");
		System.out.println("   " + serverIdList);
		System.out.println();
	}

}
