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
import com.netcrest.pado.exception.PadoLoginException;
import com.netcrest.pado.test.biz.IPartitionedRegionBiz;
import com.netcrest.pado.test.biz.impl.gemfire.PartitionedRegionBizImplLocalOverride;

import junit.framework.Assert;

/**
 * LdapSecurityTest tests LDAP authentication via Pado.
 * 
 * @author dpark
 *
 */
public class LdapSecurityTest
{
	private static final int PADO_COUNT = 4;
	private static IPado[] pados = new Pado[PADO_COUNT];
	private static IPartitionedRegionBiz[] prBiz = new IPartitionedRegionBiz[PADO_COUNT];
	private static IPartitionedRegionBiz[] prBizLocal = new IPartitionedRegionBiz[PADO_COUNT];

	@BeforeClass
	public static void loginPado() throws PadoLoginException
	{
		System.setProperty("gemfirePropertyFile", "etc/client/client.properties");
		System.setProperty("pado.properties", "etc/client/pado.properties");
		
		// LDAP authentication is independent of security enablement.
		System.setProperty("pado.security.enabled", "true");
		
		if (Pado.isClosed()) {
			Pado.connect("localhost:20000", true);
		}

		// Test multiple logins from the same JVM.
		testLogin0();
		testLogin1();
		testLogin2();
		testLogin3();
	}

	@AfterClass
	public static void closePado()
	{
		Pado.close();
	}

	private static void testLogin0() throws PadoLoginException
	{
		pados[0] = Pado.login("sys", "netcrest", "test1", "test123".toCharArray());
		ICatalog catalog = pados[0].getCatalog();

		// Local implementation PartitionedRegionBizLocalImpl is defined
		// by @BizClass.
		prBiz[0] = (IPartitionedRegionBiz) catalog.newInstance(IPartitionedRegionBiz.class);
		// Local implementation of IBiz that overrides the default local
		// implementation
		// designated by @BizClass.
		prBizLocal[0] = (IPartitionedRegionBiz) catalog.newInstanceLocal(IPartitionedRegionBiz.class,
				new PartitionedRegionBizImplLocalOverride());
	}

	private static void testLogin1() throws PadoLoginException
	{
		pados[1] = Pado.login("sys", "netcrest", "test2", "test123".toCharArray(),
				"security/client/client-user.keystore", "client-user", "client-user".toCharArray());
		ICatalog catalog = pados[1].getCatalog();

		// Local implementation PartitionedRegionBizLocalImpl is defined
		// by @BizClass.
		prBiz[1] = (IPartitionedRegionBiz) catalog.newInstance(IPartitionedRegionBiz.class);
		// Local implementation of IBiz that overrides the default local
		// implementation
		// designated by @BizClass.
		prBizLocal[1] = (IPartitionedRegionBiz) catalog.newInstanceLocal(IPartitionedRegionBiz.class,
				new PartitionedRegionBizImplLocalOverride());
	}

	private static void testLogin2() throws PadoLoginException
	{
		pados[2] = Pado.login("sys", "netcrest", "test3", "test123".toCharArray());
		ICatalog catalog = pados[2].getCatalog();

		// Local implementation PartitionedRegionBizLocalImpl is defined
		// by @BizClass.
		prBiz[2] = (IPartitionedRegionBiz) catalog.newInstance(IPartitionedRegionBiz.class);
		// Local implementation of IBiz that overrides the default local
		// implementation
		// designated by @BizClass.
		prBizLocal[2] = (IPartitionedRegionBiz) catalog.newInstanceLocal(IPartitionedRegionBiz.class,
				new PartitionedRegionBizImplLocalOverride());
	}

	private static void testLogin3() throws PadoLoginException
	{
		pados[3] = Pado.login("sys", "netcrest", "test4", "test123".toCharArray(),
				"security/client/client-user.keystore", "client-user", "client-user".toCharArray());
		ICatalog catalog = pados[3].getCatalog();

		// Local implementation PartitionedRegionBizLocalImpl is defined
		// by @BizClass.
		prBiz[3] = (IPartitionedRegionBiz) catalog.newInstance(IPartitionedRegionBiz.class);
		// Local implementation of IBiz that overrides the default local
		// implementation
		// designated by @BizClass.
		prBizLocal[3] = (IPartitionedRegionBiz) catalog.newInstanceLocal(IPartitionedRegionBiz.class,
				new PartitionedRegionBizImplLocalOverride());
	}

	@Test
	public void testPado()
	{
		Object token = pados[0].getToken();
		Assert.assertNotNull(token);
		System.out.println("testPado()");
		System.out.println("   token=" + token);
		System.out.println();
		ICatalog catalog = pados[0].getCatalog();
		Assert.assertNotNull(catalog);
		Assert.assertTrue(catalog.getAllBizClasses().length > 0);
		Assert.assertNotNull(catalog.getGridIds());
		Assert.assertTrue(catalog.getGridIds().length >= 0);
	}

	@Test
	public void testGetBucketIds()
	{
		int[] bucketIds = prBiz[0].getBucketIds();
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
		IGridContextClient gridContextClient = prBiz[0].getBizContext().getGridContextClient();
		gridContextClient.reset();
		Assert.assertTrue(gridContextClient.getRoutingKeys() == null);
		gridContextClient.setRoutingKeys(Collections.singleton(0));
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		Map<String, String> map = prBiz[0].getBucketMap(0);
		Assert.assertTrue(gridContextClient.getRoutingKeys() != null);
		Assert.assertNotNull(map);
		System.out.println("testGetBucketMap()");
		System.out.println("   " + map);
		System.out.println();
	}

	@Test
	public void testServerIdList0()
	{
		IBizContextClient bizContext = prBiz[0].getBizContext();
		List<String> serverIdList = prBiz[0].getServerIdList();
		Assert.assertNotNull(serverIdList);
		System.out.println("testServerIdList()");
		System.out.println("   " + serverIdList);
		System.out.println();
	}

	@Test
	public void testServerIdList1()
	{
		IBizContextClient bizContext = prBiz[1].getBizContext();
		List<String> serverIdList = prBiz[1].getServerIdList();
		Assert.assertNotNull(serverIdList);
		System.out.println("testServerIdList1()");
		System.out.println("   " + serverIdList);
		System.out.println();
	}

	@Test
	public void testServerIdList2()
	{
		IBizContextClient bizContext = prBiz[2].getBizContext();
		List<String> serverIdList = prBiz[2].getServerIdList();
		Assert.assertNotNull(serverIdList);
		System.out.println("testServerIdList2()");
		System.out.println("   " + serverIdList);
		System.out.println();
	}

	@Test
	public void testServerIdList3()
	{
		IBizContextClient bizContext = prBiz[3].getBizContext();
		List<String> serverIdList = prBiz[3].getServerIdList();
		Assert.assertNotNull(serverIdList);
		System.out.println("testServerIdList3()");
		System.out.println("   " + serverIdList);
		System.out.println();
	}
}
